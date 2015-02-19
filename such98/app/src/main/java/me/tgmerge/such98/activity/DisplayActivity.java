package me.tgmerge.such98.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import org.apache.http.Header;

import java.util.Vector;

import me.tgmerge.such98.R;
import me.tgmerge.such98.util.APIUtil;
import me.tgmerge.such98.util.ActivityUtil;
import me.tgmerge.such98.util.HelperUtil;
import me.tgmerge.such98.util.OAuthUtil;


public class DisplayActivity extends ActionBarActivity {

    Vector<APIUtil.APIRequest> tests;
    int testNo = 0;
    EditText text;
    EditText textToken;
    Activity that;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);

        text = (EditText) findViewById(R.id.editText);
        textToken = (EditText) findViewById(R.id.editText_token);
        that = this;
    }


    public void onTestButtonClicked(View view) {

        if (tests == null) {

            tests = new Vector<>();

            class MyCallback implements APIUtil.APICallback {
                @Override
                public void onSuccess(int statCode, Header[] headers, byte[] body) {
                    if (body != null) {
                        text.setText(new String(body));
                    } else {
                        text.setText("Empty response, statcode=" + statCode);
                    }
                }

                @Override
                public void onFailure(int statCode, Header[] headers, byte[] body, Throwable error) {
                    text.setText("ERROR " + statCode + ", " + error.toString());
                }
            }

            // topic
            tests.add(new APIUtil.GetNewTopic(this, 0, null, 10, new MyCallback()));
            tests.add(new APIUtil.GetBoardTopic(this, 100, 0, null, 10, new MyCallback()));
            tests.add(new APIUtil.GetHotTopic(this, 0, null, 10, new MyCallback()));
            // untested: PostBoardTopic
            tests.add(new APIUtil.GetTopic(this, 4473926, new MyCallback()));
            // post
            // untested: tests.add(new APIUtil.PostTopicPost(this, 2803718, "title", "content", new MyCallback()));
            tests.add(new APIUtil.GetTopicPost(this, 2803718, 0, null, 10, new MyCallback()));
            tests.add(new APIUtil.GetPost(this, 786144012, new MyCallback()));
            // untested: tests.add(new APIUtil.PutPost();
            // user
            tests.add(new APIUtil.GetNameUser(this, "tgmerge", new MyCallback()));
            tests.add(new APIUtil.GetIdUser(this, 389794, new MyCallback()));
            // board
            tests.add(new APIUtil.GetRootBoard(this, 0, null, 10, new MyCallback()));
            tests.add(new APIUtil.GetSubBoards(this, 6, 0, null, 5, new MyCallback()));
            tests.add(new APIUtil.GetBoard(this, 6, new MyCallback()));
            int[] a = {6, 100};
            tests.add(new APIUtil.GetMultiBoards(this, a, 0, null, 10, new MyCallback()));
            // me
            tests.add(new APIUtil.GetMe(this, new MyCallback()));
            tests.add(new APIUtil.GetBasicMe(this, new MyCallback()));
            tests.add(new APIUtil.GetCustomBoardsMe(this, new MyCallback()));
            // untested: put me
            // System config
            tests.add(new APIUtil.GetSystemSetting(this, new MyCallback()));
            // message
            tests.add(new APIUtil.GetMessage(this, 23878541, new MyCallback()));
            tests.add(new APIUtil.GetUserMessage(this, "tgmerge", APIUtil.GetUserMessage.FILTER_SEND, 0, null, 10, new MyCallback()));
            tests.add(new APIUtil.DeleteMessage(this, 23880005, new MyCallback()));
            // untested: put message
            tests.add(new APIUtil.PostMessage(this, "tgmerge", "testTitle", "testContent", new MyCallback()));
        }

        textToken.setText(OAuthUtil.getAccessToken());

        if (testNo < tests.size()) {
            HelperUtil.debugToast(that, "Test #" + testNo + ", " + tests.get(testNo).getClass().getName());
            tests.get(testNo).execute();
            testNo++;
        } else {
            HelperUtil.debugToast(that, "Test all done");
        }
    }

    public void onReloginButtonClicked(View view) {
        ActivityUtil.Action.relogin(this, true);
    }

    public void onRefreshTokenButtonClicked(View view) {
        final OAuthUtil.OAuthManager oam = OAuthUtil.newOAuthManager(this);
        String token = OAuthUtil.getAccessToken();
        HelperUtil.debugToast(this, "Old token: " + (token.length() > 10 ? token.substring(0, 10) : token));
        oam.refreshToken(new OAuthUtil.OAuthManager.OAuthCallback() {
            @Override
            public void onSuccess() {
                String newToken = OAuthUtil.getAccessToken();
                HelperUtil.debugToast(that, "New token:" + (newToken.length() > 10 ? newToken.substring(0, 10) : newToken));
            }

            @Override
            public void onFailure() {
                HelperUtil.errorToast(that, "refresh token failed");
            }
        });
    }

    public void onCheckMeClicked(View view) {
        new APIUtil.GetMe(this, new APIUtil.APICallback() {
            @Override
            public void onSuccess(int statCode, Header[] headers, byte[] body) {
                if (body != null) {
                    text.setText(new String(body));
                } else {
                    text.setText("Empty response, statcode=" + statCode);
                }
            }
            @Override
            public void onFailure(int statCode, Header[] headers, byte[] body, Throwable error) {
                text.setText("ERROR " + statCode + ", " + error.toString());
            }
        }).execute();
    }

    public void act1Clicked(View view) {
        ActivityUtil.Action.showRootBoard(this);
    }

    public void act2Clicked(View view) {
        ActivityUtil.Action.showCustomBoards(this, false);
    }

    public void act3Clicked(View view) {
        ActivityUtil.openShowBoardsActivity(this, 2, 0, false);
    }

    public void act4Clicked(View view) {
        ActivityUtil.openShowTopicsActivity(this, 100, 0, false);
    }

    public void act5Clicked(View view) {
        ActivityUtil.Action.showNewTopics(this);
    }

    public void act6Clicked(View view) {
        ActivityUtil.Action.showHotTopics(this);
    }

    public void act7Clicked(View view) {
        startActivity(new Intent(this, ShowBoardsActivity.class));
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
