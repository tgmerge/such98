package me.tgmerge.such98;

import android.content.Context;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxStatus;
import com.androidquery.util.XmlDom;


public class DisplayActivity extends ActionBarActivity {

    private AQuery aq;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);

        aq = new AQuery(this);

        String token = APIUtil.getInstance(this).getAccessToken();
        aq.find(R.id.editText_token).text(token);

        // TODO some test here
        test();
    }

    public void test() {
        Context that = this;
        //APIUtil.getInstance(this).clearAccessToken();
        //APIUtil.callCcAPI(new APIUtil.GetNewTopic(aq, 0, null, 10, that, "callbackMethod"));
        //APIUtil.callCcAPI(new APIUtil.GetBoardTopic(aq, 100, 10, null, 10, that, "callbackMethod"));
        //APIUtil.callCcAPI(new APIUtil.GetHotTopic(aq, 0, null, 10, that, "callbackMethod"));
        //APIUtil.callCcAPI(new APIUtil.GetTopic(aq, 4473926, that, "callbackMethod"));
        //APIUtil.callCcAPI(new APIUtil.PostTopicPost(aq, 2803718, "re", "post", that, "callbackMethod"));
        //APIUtil.callCcAPI(new APIUtil.GetTopicPost(aq, 2803718, 0, null, 10, that, "callbackMethod"));
        //APIUtil.callCcAPI(new APIUtil.GetPost(aq, 786144012, that, "callbackMethod"));
        //APIUtil.callCcAPI(new APIUtil.GetNameUser(aq, "tgmerge", that, "callbackMethod"));
        //APIUtil.callCcAPI(new APIUtil.GetIdUser(aq, "389794", that, "callbackMethod"));
        //APIUtil.callCcAPI(new APIUtil.GetRootBoard(aq, 0, null, 10, that, "callbackMethod"));
        // failed APIUtil.callCcAPI(new APIUtil.GetSubBoards(aq, 6, 0, null, 10, that, "callbackMethod"));
        //APIUtil.callCcAPI(new APIUtil.GetBoard(aq, 6, that, "callbackMethod"));
        //int[] a = {6, 100};
        //APIUtil.callCcAPI(new APIUtil.GetMultiBoards(aq, a, 0, null, 10, that, "callbackMethod"));
        //APIUtil.callCcAPI(new APIUtil.GetBasicMe(aq, that, "callbackMethod"));
        //APIUtil.callCcAPI(new APIUtil.GetCustomBoardsMe(aq, that, "callbackMethod"));
        //APIUtil.callCcAPI(new APIUtil.GetMe(aq, that, "callbackMethod"));
        APIUtil.callCcAPI(new APIUtil.GetSystemSetting(aq, that, "callbackMethod"));
        //APIUtil.callCcAPI(new APIUtil.GetUserMessage(aq, "tgmerge", APIUtil.GetUserMessage.FILTER_SEND, 0, null, 10, that, "callbackMethod"));
        //APIUtil.callCcAPI(new APIUtil.PostMessage(aq, "tgmerge", "testTitle", "testContent", this, "callbackMethod"));
        OAuthUtil oa = OAuthUtil.getInstance();
        aq.find(R.id.editText).text(oa.getAccessToken());
        //oa.refreshToken(this);
        //aq.find(R.id.editText_token).text(oa.getAccessToken());
/*
        AjaxCallback<String> cb = new AjaxCallback<String>();

        cb.type(String.class)
                .weakHandler(this, "handler1")
                .url("http://api.cc98.org/")
                .param("grant_type", "refresh_token")
                .param("refresh_token", "www")
                .param("client_id", "www")
                .param("client_secret", "www");
        aq.ajax(cb);
*/
    }
/*
    public void handler1(String url, String object, AjaxStatus status) {
        Log.d("callback!","callback!" + object + status.toString());
    }
*/
    public void callbackMethod(String url, XmlDom xml, AjaxStatus status) {

        String msg;

        if (xml != null) {
            // successful ajax call, show status code and content
            msg = "Success\n";
            msg += status.getCode() + "\n";
            msg += xml.toString() + "\n";
        } else {
            // ajax error, show some info
            msg = "Fail";
            msg += "\nerror: " + status.getError();
            msg += "\ncode:" + status.getCode();
            msg += "\nmessage" + status.getMessage();

            int code = status.getCode();
            String message = status.getMessage();
            if (code == 401 || code == 403) {
                // token is expired or something, redo the login
                Toast.makeText(this, code + " " + message + ", re-login", Toast.LENGTH_LONG).show();

                // finish this activity, and start login activity
                OAuthUtil.getInstance().clearToken();
                //Intent intent = new Intent(this, LoginActivity.class);
                //finish();
                //startActivity(intent);
            }
        }
        aq.find(R.id.editText).text(msg);
    }

    public void onRetryButtonClicked(View view) {
        test();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_display, menu);
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
