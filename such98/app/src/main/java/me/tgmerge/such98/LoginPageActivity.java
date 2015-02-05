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
        OAuthUtil oa = new OAuthUtil(
                this,
                "https://login.cc98.org/OAuth/Authorize",
                "https://login.cc98.org/OAuth/Token",
                "all*",
                "17bd1fe0-39e7-488f-ac6a-071c86e1f083",
                "fdf5b427-918e-4237-9897-838eefe478f8"
        );
        WebView webView = (WebView) findViewById(R.id.webView_login);
        oa.fire(webView, LoginActivity.class);
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

}
