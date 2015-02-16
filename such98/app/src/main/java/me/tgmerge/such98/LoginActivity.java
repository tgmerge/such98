package me.tgmerge.such98;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import me.tgmerge.such98.Util.ActivityUtil;
import me.tgmerge.such98.Util.HelperUtil;
import me.tgmerge.such98.Util.OAuthUtil;


public class LoginActivity extends ActionBarActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
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

    public void onLoginButtonClicked(View view) {
        if (OAuthUtil.getAccessToken(this).equals("")) {
            HelperUtil.debugToast(this, "LoginActivity: no token");
            startActivity(new Intent(this, LoginPageActivity.class));
        } else {
            HelperUtil.debugToast(this, "LoginActivity: token exists");
            ActivityUtil.openShowBoardsActivity(this, ShowBoardsActivity.ID_CUSTOM, 0, "定制版面");
        }
    }

    public void onTestButtonClicked(View view) {
        startActivity(new Intent(this, DisplayActivity.class));
    }
}
