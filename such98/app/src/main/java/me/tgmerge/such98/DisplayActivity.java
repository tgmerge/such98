package me.tgmerge.such98;

import android.content.Context;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

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
        //NetUtil.callCcAPI(new NetUtil.GetHotTopic(aq, 0, null, 10, that, "callbackMethod"));
        //NetUtil.callCcAPI(new NetUtil.GetNewTopic(aq, 0, null, 10, that, "callbackMethod"));
        //NetUtil.callCcAPI(new NetUtil.GetBoardTopic(aq, 100, 0, null, 10, that, "callbackMethod"));
        //NetUtil.callCcAPI(new NetUtil.GetTopic(aq, 4484560, that, "callbackMethod"));
        //NetUtil.callCcAPI(new NetUtil.PostTopicPost(aq, 2803718, "所以说", "为啥json有问题?/=\"\'", that, "callbackMethod"));

        /*AjaxCallback<XmlDom> a = new AjaxCallback<XmlDom>();
        a.header("Accept", "application/xml").header("Content-Type", "application/xml");
        a.url("http://baidu.com/");
        a.type(XmlDom.class);
        a.weakHandler(this, "callbackMethod");
        a.param("Title", "this is title!");
        a.param("Content", "this is content!");
        aq.ajax(a);*/
    }

    public void callbackMethod(String url, XmlDom xml, AjaxStatus status) {
        if (xml != null) {
            aq.find(R.id.editText).text(xml.toString());
        } else {
            String msg = "";
            msg += "Null XML\n" + url;
            msg += "\nerror: " + status.getError();
            msg += "\ncode: " + status.getCode();
            msg += "\nmessage" + status.getMessage();
            aq.find(R.id.editText).text(msg);
        }
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
