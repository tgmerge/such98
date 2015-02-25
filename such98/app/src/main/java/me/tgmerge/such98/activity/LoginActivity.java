package me.tgmerge.such98.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;

import me.tgmerge.such98.R;
import me.tgmerge.such98.util.ActivityUtil;
import me.tgmerge.such98.util.HelperUtil;
import me.tgmerge.such98.util.OAuthUtil;


public class LoginActivity extends ActionBarActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    public void onLoginButtonClicked(View view) {
        if (OAuthUtil.getAccessToken().equals("")) {
            HelperUtil.debugToast(this, "LoginActivity: no token");
            startActivity(new Intent(this, LoginPageActivity.class));
        } else {
            HelperUtil.debugToast(this, "LoginActivity: token exists");
            ActivityUtil.Action.showCustomBoards(this, true);
        }
    }

    public void onTestButtonClicked(View view) {
        startActivity(new Intent(this, TestActivity.class));
    }
}
