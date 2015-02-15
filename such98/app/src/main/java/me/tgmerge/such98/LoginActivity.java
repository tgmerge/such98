package me.tgmerge.such98;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import me.tgmerge.such98.Util.OAuthUtil;


public class LoginActivity extends ActionBarActivity {

    private OAuthUtil oa;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        oa = OAuthUtil.getInstance();
        if (oa == null) {
            oa = new OAuthUtil(
                    this,
                    "https://login.cc98.org/OAuth/Authorize",
                    "https://login.cc98.org/OAuth/Token",
                    "getuserinfo* getpost* setpost* getmessage* setmessage*",
                    "17bd1fe0-39e7-488f-ac6a-071c86e1f083",
                    "fdf5b427-918e-4237-9897-838eefe478f8");
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
        if (oa.getAccessToken().equals("")) {
            Toast.makeText(this, "LoginActivity: token not exists", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, LoginPageActivity.class));
        } else {
            Toast.makeText(this, "LoginActivity: token exists", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, DisplayActivity.class));
        }
    }
}
