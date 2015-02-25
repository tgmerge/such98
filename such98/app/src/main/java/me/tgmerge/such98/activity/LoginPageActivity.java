package me.tgmerge.such98.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;

import me.tgmerge.such98.R;
import me.tgmerge.such98.custom.SuchApp;
import me.tgmerge.such98.util.ActivityUtil;
import me.tgmerge.such98.util.HelperUtil;
import me.tgmerge.such98.util.OAuthUtil;


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
                String token = OAuthUtil.getAccessToken();
                HelperUtil.debugToast(SuchApp.getStr(R.string.activity_login_page_login_success, (token.length() > 10 ? token.substring(0, 10) : token)));
                ActivityUtil.Action.showCustomBoards(that, true);
            }

            @Override
            public void onFailure() {
                HelperUtil.errorToast(SuchApp.getStr(R.string.activity_login_page_login_failure));
                startActivity(new Intent(that, LoginActivity.class));
            }
        });
    }

}