package me.tgmerge.such98.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.apache.http.util.EncodingUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.tgmerge.such98.R;
import me.tgmerge.such98.SuchApplication;

/**
 * Usage:
 *
 * 1. Config:
 *   add "authorize_url", "token_url", "scope", "client_id", "client_secret" into string.xml resources
 *   i.e. client_info.xml
 *
 * 2. Login:
 *   OAuthUtil.OAuthManager oam = new OAuthUtil.OAuthManager(Context ctx); - instance of manager
 *   oam.fire(WebView webview, OAuthCallback callback);                     - start oauth process in webview
 *
 * -- Refresh token(todo not working due to bugs of cc98 API?):
 *   OAuthUtil.OAuthManager oam = new OAuthUtil.OAuthManager(Context ctx); - instance of manager
 *   oam.refreshToken(OAuthCallback callback);                              - will create an invisible webview
 *                                                                            on current Activity and refresh
 *   (due to certification issue, token refreshing can't be achieved by https request without webview)
 *
 * -- Get access token:
 *   OAuthUtil.getAccessToken(Context ctx);
 *
 * -- Clear access token:
 *   OAuthUtil.clearAccessToken(Context ctx);
 */
public class OAuthUtil {

    /**
     * @param ctx
     * @return OAuthManager instance configured by values in xml resource file,
     *         for logging-in or refreshing token
     */
    public static final OAuthManager newOAuthManager(Context ctx) {
        return new OAuthManager(ctx,
                                ctx.getString(R.string.authorize_url),
                                ctx.getString(R.string.token_url),
                                ctx.getString(R.string.scope),
                                ctx.getString(R.string.client_id),
                                ctx.getString(R.string.client_secret));
    }


    public static final class OAuthManager {

        private Context mCtx;

        private String mAuthorizeURL;
        private String mTokenURL;
        private String mClientID;
        private String mClientSecret;
        private String mScope;
        private final String INIT_URL = "http://localhost/start";
        private final String REDIR_URL = "http://localhost/redir";
        private final String JS_INTERFACE = "HTMLOUT";


        public OAuthManager(Context ctx, String authorizeURL, String tokenURL, String scope,
                            String clientID, String clientSecret) {
            logDebug("OAuthUtil: initializing, authURL = " + authorizeURL);
            this.mCtx = ctx;
            this.mAuthorizeURL = authorizeURL;
            this.mTokenURL = tokenURL;
            this.mScope = scope;
            this.mClientID = clientID;
            this.mClientSecret = clientSecret;
        }

        // 在webView中开启OAuth2 authorization code过程。
        // 如果成功，将存储access token和refresh token，并开启succActivity
        // 如果失败，将开启failActivity
        @SuppressWarnings("unused")
        public final void fire(WebView webView, OAuthCallback callback) {
            logDebug("fire: firing on " + webView.toString());
            webView.getSettings().setJavaScriptEnabled(true);
            webView.setWebViewClient(new OAuthWebViewClient());
            webView.addJavascriptInterface(new MyJSInterface(webView, callback), JS_INTERFACE);
            webView.loadUrl(INIT_URL);
        }


        // 刷新access token。
        // 根据返回调用callback的onSuccess或onFailure方法
        // I'm out... javax.net.ssl.SSLPeerUnverifiedException: No peer certificate
        // I'll use an invisible webview to handle this
        @SuppressWarnings("unused")
        public final void refreshToken(final OAuthCallback callback) {

            logDebug("refreshToken: refreshing token");

            // 在ctx上创建一个webview，并执行更新token的工作
            // good way to run code on ui thread
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    WebView webView = new WebView(mCtx);

                    webView.getSettings().setJavaScriptEnabled(true);
                    webView.setWebViewClient(new OAuthWebViewClient());
                    webView.addJavascriptInterface(new MyJSInterface(webView, callback), JS_INTERFACE);
                    String postLoad = "grant_type=refresh_token";
                    postLoad += "&refresh_token=" + getRefreshToken();
                    postLoad += "&client_id=" + mClientID;
                    postLoad += "&client_secret=" + mClientSecret;
                    logDebug("refreshToken: postLoad:" + postLoad.substring(0, 20));
                    webView.postUrl(mTokenURL, EncodingUtils.getBytes(postLoad, "UTF-8"));
                }
            });
        }


        private static final void processTokenHTML(String html, OAuthCallback callback) {
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


        // 在使用refreshToken()、使用fire()时传递此对象，覆盖onSuccess和onFail方法
        // 它们会在获取/刷新token成功或失败时被调用
        public static interface OAuthCallback {
            public void onSuccess(); // todo maybe add a context param?
            public void onFailure();
        }


        private final class OAuthWebViewClient extends WebViewClient {
            private static final int STATE_INIT = 0, STATE_SEND1 = 1, STATE_RECV1 = 2, STATE_SEND2 = 3,
                    STATE_RECV2 = 4, STATE_AFTER_RECV2 = 5, STATE_REFRESH = 6;
            private int mState = STATE_INIT;

            @Override
            public void onPageFinished(WebView view, String url) {
                if (this.mState == STATE_AFTER_RECV2 && url.startsWith(mTokenURL) || this.mState == STATE_REFRESH) {
                    logDebug("OAuthWebViewClient: " + "page finished, loading js");
                    view.loadUrl("javascript:" + JS_INTERFACE + ".processHTML(document.documentElement.outerHTML);");
                }
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                if (this.mState == STATE_INIT && url.startsWith(mTokenURL)) {

                    logDebug("OAuthWebViewClient: " + "loading token refreshing URL");
                    // token refreshing on the way
                    this.mState = STATE_REFRESH;

                } else if (this.mState == STATE_INIT && url.startsWith(INIT_URL)) {

                    this.mState = STATE_SEND1;

                    String authURL = mAuthorizeURL + "?";
                    authURL += "response_type=code";
                    authURL += "&client_id=" + mClientID;
                    authURL += "&state=" + FILE_KEY;
                    authURL += "&redirect_uri=" + REDIR_URL;
                    authURL += "&scope=" + mScope;
                    view.loadUrl(authURL);

                    this.mState = STATE_RECV1;

                } else if (this.mState == STATE_RECV1 && url.startsWith(mAuthorizeURL)) {

                } else if (this.mState == STATE_RECV1 && url.startsWith(REDIR_URL)) {

                    this.mState = STATE_SEND2;

                    String authCode = "";

                    Matcher m = Pattern.compile("code=([^$&]+)").matcher(url);
                    if (m.find()) {
                        authCode = m.group(1);
                    } else {
                        // token not found, login failed
                        // todo do something to reflect callback!
                    }

                    String postLoad = "grant_type=authorization_code";
                    postLoad += "&code=" + authCode;
                    postLoad += "&client_id=" + mClientID;
                    postLoad += "&client_secret=" + mClientSecret;
                    postLoad += "&redirect_uri=" + REDIR_URL;
                    postLoad += "&state=" + FILE_KEY;

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


        private final class MyJSInterface {
            private OAuthCallback mCallback;
            private WebView mWebView;

            public MyJSInterface(WebView webView, OAuthCallback callback) {
                mCallback = callback;
                mWebView = webView;
            }

            // 所有WebView得到的HTML内容经过这个方法处理
            //      由于是JS线程调用这个方法的，所以将不在UI线程上
            //      processTokenHTML将调用两个回调方法，它们在UI线程上比较好
            //      所以用runOnUiThread调用processTokenHTML
            @JavascriptInterface
            @SuppressWarnings("unused")
            public void processHTML(final String html) {
                logDebug("processHTML: " + (html == null ? "null" : "..."));

                // 处理access token结束后，删除webview
                // run on ui thread
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        processTokenHTML(html, mCallback);
                        logDebug("processHTML: destroying webview");
                        mWebView.destroy();
                        mCtx = null;
                    }
                });
            }
        }
    }


    // - - - util - - -


    private static final String FILE_KEY = OAuthUtil.class.getName();
    private static final String KEY_ACCESS_TOKEN = "accessToken";
    private static final String KEY_REFRESH_TOKEN = "refreshToken";


    public static final void clearToken() {
        logDebug("clearToken: clearing");
        setToken("", "");
    }


    private static final void setToken(String accessToken, String refreshToken) {
        logDebug("setToken: access = " + (accessToken.length() > 10 ? accessToken.substring(0, 10) : accessToken));
        logDebug("setToken: refresh = " + (refreshToken.length() > 10 ? refreshToken.substring(0, 10) : refreshToken));
        SharedPreferences.Editor editor = SuchApplication.getContext().getSharedPreferences(FILE_KEY, Context.MODE_PRIVATE).edit();
        editor.putString(KEY_ACCESS_TOKEN, accessToken);
        editor.putString(KEY_REFRESH_TOKEN, refreshToken);
        editor.apply();
    }


    public static final String getAccessToken() {
        SharedPreferences pref = SuchApplication.getContext().getSharedPreferences(FILE_KEY, Context.MODE_PRIVATE);
        String accessToken = pref.getString(KEY_ACCESS_TOKEN, "");
        logDebug("getAccessToken: " + accessToken.substring(0, accessToken.length() > 10 ? 10 : accessToken.length() / 2));
        return accessToken;
    }


    private static final String getRefreshToken() {
        SharedPreferences pref = SuchApplication.getContext().getSharedPreferences(FILE_KEY, Context.MODE_PRIVATE);
        String refreshToken = pref.getString(KEY_REFRESH_TOKEN, "");
        logDebug("getRefreshToken: " + refreshToken.substring(0, refreshToken.length() > 10 ? 10 : refreshToken.length() / 2));
        return refreshToken;
    }


    private static final void logDebug(String msg) {
        HelperUtil.generalDebug("OAuthUtil", msg);
    }


    private static final void logError(String msg) {
        HelperUtil.generalError("OAuthUtil", msg);
    }
}