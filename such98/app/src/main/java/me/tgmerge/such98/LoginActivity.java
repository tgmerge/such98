package me.tgmerge.such98;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.androidquery.AQuery;


public class LoginActivity extends ActionBarActivity {

    private AQuery aq;

    public final static String LOGIN_STATUS = "me.tgmerge.such98.LoginActivity.LOGINSTATUS";
    public final static int LOGIN_STATUS_SUCCESS = 200;
    public final static int LOGIN_STATUS_FAIL = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        aq = new AQuery(this);

        String token = NetUtil.getInstance(this).getAccessToken();
        if (token != null && !(token.equals(""))) {
            startActivity(new Intent(this, DisplayActivity.class));
        }
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
        startActivity(new Intent(this, LoginPageActivity.class));
    }
}
