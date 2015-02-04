package me.tgmerge.such98;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.androidquery.AQuery;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class LoginPageActivity extends ActionBarActivity {

    private AQuery aq;

    private static final String OAUTH_URL = "https://login.cc98.org/OAuth/Authorize?response_type=%1$s&client_id=%2$s&scope=%3$s&state=%4$s&redirect_uri=%5$s";
    private static final String REDIRECT_URI = "http://localhost/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);

        aq = new AQuery(this);

        final WebView webView = (WebView) findViewById(R.id.webView_login);
        CustomWebViewClient client = new CustomWebViewClient();
        client.activityContext = this;
        webView.setWebViewClient(client);
        String url = String.format(
                LoginPageActivity.OAUTH_URL,
                NetUtil.encodeURIComponent("token"),
                NetUtil.encodeURIComponent(getString(R.string.appConfig_clientID)),
                NetUtil.encodeURIComponent("all*"),
                NetUtil.encodeURIComponent(getString(R.string.app_name)),
                NetUtil.encodeURIComponent(LoginPageActivity.REDIRECT_URI));
        webView.loadUrl(url);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login_page, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    class CustomWebViewClient extends WebViewClient {
        protected Context activityContext = null;

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url.startsWith(LoginPageActivity.REDIRECT_URI)) {
                Intent intent = new Intent(this.activityContext, LoginActivity.class);
                Pattern pattern = Pattern.compile("access_token=([^$&]+)");
                Matcher matcher = pattern.matcher(url);
                if (matcher.find()) {
                    String token = matcher.group(1);
                    SharedPreferences sharedPref = activityContext.getSharedPreferences(
                            getString(R.string.appConfig_preferenceFileKey_settings),
                            activityContext.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString(
                            getString(R.string.appConfig_valueKey_settings_accessToken),
                            token);
                    editor.commit();
                } else {
                    intent.putExtra(LoginActivity.LOGIN_STATUS, LoginActivity.LOGIN_STATUS_FAIL);
                }
                startActivity(intent);
            }
            // TODO: if failed then display failed message
            return false;
        }
    }
}
