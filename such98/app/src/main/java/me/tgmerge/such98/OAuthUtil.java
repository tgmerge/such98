package me.tgmerge.such98;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.apache.http.util.EncodingUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by tgmerge on 2/5.
 * handle cc98 OAuth login process.
 * 使用:
 *     用OAuthutil(Activity ctx, OAuth参数)初始化实例
 *
 *     其后，用getInstance()返回实例，如果没有初始化过，该方法返回null
 *
 *     用fire(活动上下文ctx, webview, OAuthCallback)在webView中打开oauth过程
 *         其中，覆盖OAuthCallback的两个方法onSuccess和onFailure，刷新/获取token之后，根据情况会调用它们
 *
 *     用getAccessToken()获取access token
 *
 *     用clearToken()清除token
 *
 *     用refreshToken(Activity, OAuthCallback)刷新token
 *
 * 算是能用了
 */
public class OAuthUtil {

    // - 初始化 -
    // 上下文，存储，OAuth参数

    static private OAuthUtil mInstance = null;
    private Activity mCtx;

    private String mAuthorizeURL;
    private String mTokenURL;
    private String mClientID;
    private String mClientSecret;
    private String mScope;
    public String mWebViewInitURL = "http://localhost/start";
    private String mWebViewRedirURL = "http://localhost/redir";

    private SharedPreferences mSharedPref;
    private String mFileKey;
    private String mKey_accessToken = "accessToken";
    private String mKey_refreshToken = "refreshToken";

    private final String JSINTERFACE = "HTMLOUT";

    // - 类方法 -

    // ctx
    protected OAuthUtil(Activity ctx, String authorizeURL, String tokenURL, String scope,
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
    protected final void fire(Activity ctx, WebView webView, OAuthCallback callback) {
        logDebug("fire: firing on " + webView.toString());
        this.mCtx = ctx;
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new OAuthWebViewClient());
        webView.addJavascriptInterface(new MyJSInterface(webView, callback), JSINTERFACE);
        webView.loadUrl(mWebViewInitURL);
    }

    @SuppressWarnings("unused")
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
    protected final void refreshToken(final Activity ctx, final OAuthCallback callback) {
        // todo I'm out... javax.net.ssl.SSLPeerUnverifiedException: No peer certificate
        // todo I'll use an invisible webview to handle this

        logDebug("refreshToken: refreshing token");

        mCtx = ctx;

        // 在ctx上创建一个webview，并执行更新token的工作
        ctx.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                WebView webView = new WebView(ctx);
                webView.getSettings().setJavaScriptEnabled(true);
                webView.setWebViewClient(new OAuthWebViewClient());
                webView.addJavascriptInterface(new MyJSInterface(webView, callback), JSINTERFACE);
                String postLoad = "grant_type=refresh_token";
                postLoad += "&refresh_token=" + getRefreshToken();
                postLoad += "&client_id=" + mClientID;
                postLoad += "&client_secret=" + mClientSecret;
                logDebug("refreshToken: postLoad:" + postLoad);
                logDebug("refreshToken: encoded postload: "+ new String(EncodingUtils.getBytes(postLoad, "UTF-8")));
                webView.postUrl(mTokenURL, EncodingUtils.getBytes(postLoad, "UTF-8"));
            }
        });
    }

    // 在使用refreshToken()、使用fire()时传递此对象，覆盖onSuccess和onFail方法
    // 它们会在获取/刷新token成功或失败时被调用
    protected static interface OAuthCallback {
        public void onSuccess();
        public void onFailure();
    }

    // - 私有类 -

    private final class MyJSInterface {
        private OAuthCallback mCallback;
        private WebView mWebView;

        public MyJSInterface(WebView webView, OAuthCallback callback) {
            mCallback = callback;
            mWebView = webView;
        }

        // 注意：由于是JS线程调用这个方法的，所以将不在UI线程上
        //      processTokenHTML将调用两个回调方法，它们在UI线程上比较好
        //      所以用urnOnUiThread调用processTokenHTML
        @JavascriptInterface
        @SuppressWarnings("unused")
        public void processHTML(final String html) {
            logDebug("processHTML: " + (html == null ? html : html));

            // 处理access token结束后，删除webview
            mCtx.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    processTokenHTML(html, mCallback);
                    logDebug("processHTML: destroying webview");
                    mWebView.destroy();
                }
            });
        }
    }

    private final class OAuthWebViewClient extends WebViewClient {
        private static final int STATE_INIT = 0, STATE_SEND1 = 1, STATE_RECV1 = 2, STATE_SEND2 = 3,
                                 STATE_RECV2 = 4, STATE_AFTER_RECV2 = 5, STATE_REFRESH = 6;
        private int mState = STATE_INIT;

        @Override
        public void onPageFinished(WebView view, String url) {
            if (this.mState == STATE_AFTER_RECV2 && url.startsWith(mTokenURL) || this.mState == STATE_REFRESH) {
                logDebug("OAuthWebViewClient: " + "page finished, loading js");
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
                    // startFailActivity(mCtx, mFailActivity);
                    // todo do something to reflect callback!
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

    private final void processTokenHTML(String html, OAuthCallback callback) {
        logDebug("processTokenHTML: processing " + html.substring(0, 10));
        Matcher m1 = Pattern.compile("\"access_token\":\"([^\"},]+)\"").matcher(html);
        Matcher m2 = Pattern.compile("\"refresh_token\":\"([^\"},]+)\"").matcher(html);
        if (m1.find() && m2.find()) {
            setToken(m1.group(1), m2.group(1));
            callback.onSuccess();
        } else {
            callback.onFailure();
        }
    }

    private final void setToken(String accessToken, String refreshToken) {
        logDebug("setToken: access = " + (accessToken.length() > 10 ? accessToken.substring(0, 10) : accessToken));
        logDebug("setToken: refresh = " + (refreshToken.length() > 10 ? refreshToken.substring(0, 10) : refreshToken));
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putString(this.mKey_accessToken, accessToken);
        editor.putString(this.mKey_refreshToken, refreshToken);
        editor.commit();
    }

    private final String getRefreshToken() {
        logDebug("getRefreshToken: " +  mSharedPref.getString(this.mKey_refreshToken, ""));
        return mSharedPref.getString(this.mKey_refreshToken, "");
    }


    private static final void logDebug(String msg) {
        HelperUtil.generalDebug("OAuthUtil", msg);
    }


    private static final void logError(String msg) {
        HelperUtil.generalError("OAuthUtil", msg);
    }

}