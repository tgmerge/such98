package me.tgmerge.such98;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.androidquery.util.XmlDom;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EncodingUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by tgmerge on 2/5.
 * handle cc98 OAuth login process.
 * 使用:
 *     先用OAuthutil(任意上下文ctx, OAuth参数)初始化实例
 *     再用fire(活动上下文ctx, webview, 成功后开启的活动, 失败后开启的活动)在webView中打开oauth过程
 *
 *     其后可以用getInstance()返回实例，如果没有初始化过，该方法返回null
 *     用getAccessToken()获取access token
 *     用clearToken()清除token
 *     用refreshToken(Activity)刷新token
 */
public class OAuthUtil {

    // - 初始化 -
    // 上下文，存储，OAuth参数

    static private OAuthUtil mInstance = null;
    private Context mCtx;

    private String mAuthorizeURL;
    private String mTokenURL;
    private String mClientID;
    private String mClientSecret;
    private String mScope;
    public String mWebViewInitURL = "http://localhost/start";
    private String mWebViewRedirURL = "http://localhost/redir";
    private Class<?> mSuccActivity;
    private Class<?> mFailActivity;

    private SharedPreferences mSharedPref;
    private String mFileKey;
    private String mKey_accessToken = "accessToken";
    private String mKey_refreshToken = "refreshToken";

    private final String JSINTERFACE = "HTMLOUT";

    // - 类方法 -

    // ctx
    protected OAuthUtil(Context ctx, String authorizeURL, String tokenURL, String scope,
                        String clientID, String clientSecret) {
        logDebug("OAuthUtil: initializing, authURL = " + authorizeURL);
        mInstance = this;
        this.mCtx = ctx;
        this.mAuthorizeURL = authorizeURL;
        this.mTokenURL = tokenURL;
        this.mScope = scope;
        this.mClientID = clientID;
        this.mClientSecret = clientSecret;
        this.mFileKey = this.getClass().getPackage().getName() + "." + this.getClass().getName();
        this.mSharedPref = mCtx.getSharedPreferences(this.mFileKey, mCtx.MODE_PRIVATE);
    }

    // 如果类没有用OAuthUtil()初始化过,
    // getInstance()将返回null, 需要在使用时判断
    protected static final OAuthUtil getInstance() {
        logDebug("getInstance: returning " + mInstance);
        return mInstance;
    }

    // - 公开方法 -

    // 在webView中开启OAuth2 authorization code过程。
    // 如果成功，将存储access token和refresh token，并开启succActivity
    // 如果失败，将开启failActivity
    @SuppressWarnings("unused")
    protected final void fire(Context ctx, WebView webView, Class<?> succActivity, Class<?> failActivity) {
        logDebug("fire: firing on " + webView.toString());
        this.mCtx = ctx;
        this.mSuccActivity = succActivity;
        this.mFailActivity = failActivity;
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new OAuthWebViewClient());
        webView.addJavascriptInterface(new MyJSInterface(), JSINTERFACE);
        webView.loadUrl(mWebViewInitURL);
    }

    protected final String getAccessToken() {
        logDebug("getAccessToken: returning");
        return mSharedPref.getString(this.mKey_accessToken, "");
    }

    @SuppressWarnings("unused")
    protected final void clearToken() {
        logDebug("clearToken: clearing");
        setToken("", "");
    }

    // 刷新access token。
    // 如果成功，将更新access token
    // 如果失败， todo
    @SuppressWarnings("unused")
    protected final void refreshToken(final Activity ctx) {
        logDebug("refreshToken: refreshing token");

/*
        // todo I'm out... javax.net.ssl.SSLPeerUnverifiedException: No peer certificate
        // todo I'll use an invisible webview to handle this
        new Thread(new Runnable() {
            @Override
            public void run() {
                // http request
                HttpClient client = new DefaultHttpClient();
                HttpPost post = new HttpPost(that.mTokenURL);
                List<NameValuePair> pairs = new ArrayList<>();
                pairs.add(new BasicNameValuePair("grant_type", "refresh_token"));
                pairs.add(new BasicNameValuePair("refresh_token", getRefreshToken()));
                pairs.add(new BasicNameValuePair("client_id", mClientID));
                pairs.add(new BasicNameValuePair("client_secret", mClientSecret));
                try{
                    post.setEntity(new UrlEncodedFormEntity(pairs));
                    HttpResponse response = client.execute(post);
                    that.logDebug("XXX response " + response.toString());
                } catch (IOException e) {
                    that.logError("refreshToken error: " + e.toString());
                }
            }
        }).start();
*/

        Thread th = new Thread() {
            public void run() {

                ctx.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        WebView webView = new WebView(ctx);
                        webView.getSettings().setJavaScriptEnabled(true);
                        webView.setWebViewClient(new OAuthWebViewClient());
                        webView.addJavascriptInterface(new MyRefreshTokenJSInterface(), JSINTERFACE);

                        String postLoad = "grant_type=refresh_token";
                        postLoad += "&refresh_token=" + getRefreshToken();
                        postLoad += "&client_id=" + mClientID;
                        postLoad += "&client_secret=" + mClientSecret;

                        webView.postUrl(mTokenURL, EncodingUtils.getBytes(postLoad, "UTF-8"));
                    }
                });
            }
        };

        th.start();
        // todo
    }

    // 在使用refreshToken()、使用fire()时传递此对象，覆盖onSuccess和onFail方法
    // 它们会在获取/刷新token成功或失败时被调用
    protected class OnTokenStoredCallBack {
        public void onSuccess() {}
        public void onFail() {}
    }

    // - 私有类 -

    private final class MyJSInterface {
        @JavascriptInterface
        @SuppressWarnings("unused")
        public void processHTML(String html) {
            processTokenHTML(html, true);
        }
    }

    private final class MyRefreshTokenJSInterface {
        @JavascriptInterface
        @SuppressWarnings("unused")
        public void processHTML(String html) {
            processTokenHTML(html, false);
        }
    }

    private final class OAuthWebViewClient extends WebViewClient {
        private static final int STATE_INIT = 0, STATE_SEND1 = 1, STATE_RECV1 = 2, STATE_SEND2 = 3,
                                 STATE_RECV2 = 4, STATE_AFTER_RECV2 = 5, STATE_REFRESH = 6;
        private int mState = STATE_INIT;

        @Override
        public void onPageFinished(WebView view, String url) {
            if (this.mState == STATE_AFTER_RECV2 && url.startsWith(mTokenURL) || this.mState == STATE_REFRESH) {
                view.loadUrl("javascript:" + JSINTERFACE + ".processHTML(document.documentElement.outerHTML);");
            }
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            if (this.mState == STATE_INIT && url.startsWith(mTokenURL)) {

                logDebug("OAuthWebViewClient: " + "loading token refreshing URL");
                // token refreshing on the way
                this.mState = STATE_REFRESH;

            } else if (this.mState == STATE_INIT && url.startsWith(mWebViewInitURL)) {

                this.mState = STATE_SEND1;

                String authURL = mAuthorizeURL + "?";
                authURL += "response_type=code";
                authURL += "&client_id=" + mClientID;
                authURL += "&state=" + mFileKey;
                authURL += "&redirect_uri=" + mWebViewRedirURL;
                authURL += "&scope=" + mScope;
                view.loadUrl(authURL);

                this.mState = STATE_RECV1;

            } else if (this.mState == STATE_RECV1 && url.startsWith(mAuthorizeURL)) {

            } else if (this.mState == STATE_RECV1 && url.startsWith(mWebViewRedirURL)) {

                this.mState = STATE_SEND2;

                String authCode = "";

                Matcher m = Pattern.compile("code=([^$&]+)").matcher(url);
                if (m.find()) {
                    authCode = m.group(1);
                } else {
                    // token not found, login failed
                    startFailActivity(mCtx, mFailActivity);
                }

                String postLoad = "grant_type=authorization_code";
                postLoad += "&code=" + authCode;
                postLoad += "&client_id=" + mClientID;
                postLoad += "&client_secret=" + mClientSecret;
                postLoad += "&redirect_uri=" + mWebViewRedirURL;
                postLoad += "&state=" + mFileKey;

                view.postUrl(mTokenURL, EncodingUtils.getBytes(postLoad, "UTF-8"));

                this.mState = STATE_RECV2;

            } else if (this.mState == STATE_RECV2 && url.startsWith(mTokenURL)) {

                this.mState = STATE_AFTER_RECV2;

            } else {

                logError("webViewClient onPageStarted: strange state, mState=" + mState + ", url=" + url);
            }
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return false;
        }
    }

    // - 私有方法 -

    private final void processTokenHTML(String html, boolean willStartActivity) {
        logDebug("processTokenHTML: processing " + html.substring(0, 10));
        Matcher m1 = Pattern.compile("\"access_token\":\"([^\"},]+)\"").matcher(html);
        Matcher m2 = Pattern.compile("\"refresh_token\":\"([^\"},]+)\"").matcher(html);
        if (m1.find() && m2.find()) {
            setToken(m1.group(1), m2.group(1));
            if (willStartActivity) {
                startSuccActivity(mCtx, mSuccActivity);
            }
        } else {
            if (willStartActivity) {
                startFailActivity(mCtx, mFailActivity);
            }
        }
    }

    private final void setToken(String accessToken, String refreshToken) {
        logDebug("setToken: access = " + (accessToken.length() > 10 ? accessToken.substring(0, 10) : accessToken));
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putString(this.mKey_accessToken, accessToken);
        editor.putString(this.mKey_refreshToken, refreshToken);
        editor.commit();
    }

    private static final void startSuccActivity(Context ctx, Class<?> activity) {
        logDebug("startSuccActivity: starting " + activity.toString());
        ctx.startActivity(new Intent(ctx, activity));
    }

    private static final void startFailActivity(Context ctx, Class<?> activity) {
        logDebug("startFailActivity: starting " + activity.toString());
        ctx.startActivity(new Intent(ctx, activity));
    }

    private final String getRefreshToken() {
        return mSharedPref.getString(this.mKey_refreshToken, "");
    }

    private static final void logDebug(String info) {
        Log.d("OAuthUtil", info);
    }

    private static final void logError(String info) {
        Log.d("OAuthUtil", info);
    }
}