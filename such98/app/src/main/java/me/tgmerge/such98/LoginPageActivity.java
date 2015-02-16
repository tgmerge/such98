package me.tgmerge.such98;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;

import me.tgmerge.such98.Util.ActivityUtil;
import me.tgmerge.such98.Util.HelperUtil;
import me.tgmerge.such98.Util.OAuthUtil;


public class LoginPageActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);

        final OAuthUtil.OAuthManager oam = OAuthUtil.newOAuthManager(this);
        final Context that = this;

        WebView webView = (WebView) findViewById(R.id.webView_login);
        oam.fire(webView, new OAuthUtil.OAuthManager.OAuthCallback() {
            @Override
            public void onSuccess() {
                String token = OAuthUtil.getAccessToken(that);
                HelperUtil.debugToast(that, "Login success, token=" + (token.length() > 10 ? token.substring(0, 10) : token));
                ActivityUtil.openShowBoardsActivity(that, ShowBoardsActivity.ID_CUSTOM, 0, "定制版面");
            }

            @Override
            public void onFailure() {
                HelperUtil.errorToast(that, "Login failed");
                startActivity(new Intent(that, LoginActivity.class));
            }
        });
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