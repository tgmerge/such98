package me.tgmerge.such98.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;

import me.tgmerge.such98.R;
import me.tgmerge.such98.custom.SuchApp;
import me.tgmerge.such98.util.ActivityUtil;
import me.tgmerge.such98.util.HelperUtil;
import me.tgmerge.such98.util.OAuthUtil;


public class LoginActivity extends ActionBarActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // long click test button to open test activity
        (findViewById(R.id.button_test)).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                startActivity(new Intent(LoginActivity.this, TestActivity.class));
                return true;
            }
        });
    }

    public void onLoginButtonClicked(View view) {
        if (OAuthUtil.getAccessToken().equals("")) {
            HelperUtil.debugToast(SuchApp.getStr(R.string.activity_login_no_token));
            startActivity(new Intent(this, LoginPageActivity.class));
        } else {
            HelperUtil.debugToast(SuchApp.getStr(R.string.activity_login_has_token));
            ActivityUtil.Action.showCustomBoards(this, true);
        }
    }
}
