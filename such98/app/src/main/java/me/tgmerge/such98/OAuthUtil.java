package me.tgmerge.such98;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.apache.http.util.EncodingUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by tgmerge on 2/5.
 * handle cc98 OAuth login process.
 * only authorization_code method
 */
public class OAuthUtil {

    static private OAuthUtil mInstance;
    private Context mCtx;

    // for auth server
    private String mAuthorizeURL;
    private String mTokenURL;
    private String mClientID;
    private String mClientSecret;
    private String mScope;
    public String mWebViewInitURL = "http://localhost/start";
    private String mWebViewRedirURL = "http://localhost/redir";

    // for storage
    private String mFileKey;
    private String mKey_accessToken = "accessToken";
    private String mKey_refreshToken = "refreshToken";

    private final String JSINTERFACE = "HTMLOUT";

    protected OAuthUtil(Context ctx, String authorizeURL, String tokenURL, String scope,
                        String clientID, String clientSecret) {
        mInstance = this;
        this.mCtx = ctx.getApplicationContext();
        this.mAuthorizeURL = authorizeURL;
        this.mTokenURL = tokenURL;
        this.mScope = scope;
        this.mClientID = clientID;
        this.mClientSecret = clientSecret;
        this.mFileKey = this.getClass().getPackage().getName() + "." + this.getClass().getName();
    }

    private class MyJSInterface
    {
        @JavascriptInterface
        @SuppressWarnings("unused")
        public void processHTML(String html)
        {
            Log.d("INTERFACE", html);
            Matcher m1 = Pattern.compile("\"access_token\":\"([^\"},])+\"").matcher(html);
            Matcher m2 = Pattern.compile("\"refresh_token\":\"([^\"},])+\"").matcher(html);
            if (m1.find() && m2.find()) {
                setToken(m1.group(1), m2.group(1));
            }
        }
    }

    class OAuthWebViewClient extends WebViewClient {
        private static final int STATE_INIT = 0, STATE_SEND1 = 1, STATE_RECV1 = 2, STATE_SEND2 = 3,
                                 STATE_RECV2 = 4, STATE_AFTER_RECV2 = 5;
        private int mState = STATE_INIT;

        @Override
        public void onPageFinished(WebView view, String url) {
            if (this.mState == STATE_AFTER_RECV2 && url.startsWith(mTokenURL)) {
                view.loadUrl("javascript:" + JSINTERFACE + ".processHTML(document.documentElement.outerHTML);");
            }
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap faviconl) {
            if (this.mState == STATE_INIT && url.startsWith(mWebViewInitURL)) {

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
                    // todo AuthCode failed!
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

                // todo into some strange status and url
            }

            return;
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return false;
        }
    }

    private final void setToken(String accessToken, String refreshToken) {
        SharedPreferences sharedPref = mCtx.getSharedPreferences(this.mFileKey, mCtx.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(this.mKey_accessToken, accessToken);
        editor.putString(this.mKey_refreshToken, refreshToken);
        editor.commit();
    }

    protected final String getAccessToken() {
        return "";
    }

    protected final String refreshToken() {
        // todo
        return "";
    }

    // main method to call.
    // config webview, add some method to handle OAuth, then load OAuth url
    protected final void fire(WebView webView) {
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new OAuthWebViewClient());
        webView.addJavascriptInterface(new MyJSInterface(), JSINTERFACE);
        webView.loadUrl(mWebViewInitURL);
    }
}
