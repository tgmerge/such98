package me.tgmerge.such98;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.androidquery.util.XmlDom;


public class DisplayActivity extends ActionBarActivity {

    private AQuery aq;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);

        aq = new AQuery(this);

        String token = NetUtil.getInstance(this).getAccessToken();
        aq.find(R.id.editText_token).text(token);

        // TODO some test here
        test();
    }

    public void test() {
        Context that = this;
        //NetUtil.getInstance(this).clearAccessToken();
        //NetUtil.callCcAPI(new NetUtil.GetNewTopic(aq, 0, null, 10, that, "callbackMethod"));
        //NetUtil.callCcAPI(new NetUtil.GetBoardTopic(aq, 100, 10, null, 10, that, "callbackMethod"));
        //NetUtil.callCcAPI(new NetUtil.GetHotTopic(aq, 0, null, 10, that, "callbackMethod"));
        //NetUtil.callCcAPI(new NetUtil.GetTopic(aq, 4473926, that, "callbackMethod"));
        //NetUtil.callCcAPI(new NetUtil.PostTopicPost(aq, 2803718, "re", "post", that, "callbackMethod"));
        //NetUtil.callCcAPI(new NetUtil.GetTopicPost(aq, 2803718, 0, null, 10, that, "callbackMethod"));
        //NetUtil.callCcAPI(new NetUtil.GetPost(aq, 786144012, that, "callbackMethod"));
        //NetUtil.callCcAPI(new NetUtil.GetNameUser(aq, "tgmerge", that, "callbackMethod"));
        //NetUtil.callCcAPI(new NetUtil.GetIdUser(aq, "389794", that, "callbackMethod"));
        //NetUtil.callCcAPI(new NetUtil.GetRootBoard(aq, 0, null, 10, that, "callbackMethod"));
        // failed NetUtil.callCcAPI(new NetUtil.GetSubBoards(aq, 6, 0, null, 10, that, "callbackMethod"));
        //NetUtil.callCcAPI(new NetUtil.GetBoard(aq, 6, that, "callbackMethod"));
        //int[] a = {6, 100};
        //NetUtil.callCcAPI(new NetUtil.GetMultiBoards(aq, a, 0, null, 10, that, "callbackMethod"));
        //NetUtil.callCcAPI(new NetUtil.GetBasicMe(aq, that, "callbackMethod"));
        //NetUtil.callCcAPI(new NetUtil.GetCustomBoardsMe(aq, that, "callbackMethod"));
        //NetUtil.callCcAPI(new NetUtil.GetMe(aq, that, "callbackMethod"));
        //NetUtil.callCcAPI(new NetUtil.GetSystemSetting(aq, that, "callbackMethod"));
        //NetUtil.callCcAPI(new NetUtil.GetUserMessage(aq, "tgmerge", NetUtil.GetUserMessage.FILTER_SEND, 0, null, 10, that, "callbackMethod"));
        //NetUtil.callCcAPI(new NetUtil.PostMessage(aq, "tgmerge", "testTitle", "testContent", this, "callbackMethod"));
    }

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
                NetUtil.getInstance(this).clearAccessToken();
                Intent intent = new Intent(this, LoginActivity.class);
                finish();
                startActivity(intent);
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
