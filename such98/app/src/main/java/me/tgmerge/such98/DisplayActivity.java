package me.tgmerge.such98;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.http.Header;

import java.util.Vector;

import me.tgmerge.such98.Util.APIUtil;
import me.tgmerge.such98.Util.OAuthUtil;


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

        textToken.setText(OAuthUtil.getInstance().getAccessToken());

        if (testNo < tests.size()) {
            Toast.makeText(that, "Test #" + testNo + ", " + tests.get(testNo).getClass().getName(), Toast.LENGTH_LONG).show();
            tests.get(testNo).execute();
            testNo++;
        } else {
            Toast.makeText(that, "Test all done", Toast.LENGTH_LONG).show();
        }
    }

    public void onReloginButtonClicked(View view) {
        OAuthUtil oa = OAuthUtil.getInstance();
        if (oa != null) {
            oa.clearToken();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }

    public void onRefreshTokenButtonClicked(View view) {
        final OAuthUtil oa = OAuthUtil.getInstance();
        if (oa != null) {
            Toast.makeText(this, "Old token:" + oa.getAccessToken(), Toast.LENGTH_LONG).show();
            oa.refreshToken(this, new OAuthUtil.OAuthCallback() {
                @Override
                public void onSuccess() {
                    Toast.makeText(that, "New token:" + oa.getAccessToken(), Toast.LENGTH_LONG).show();
                }

                @Override
                public void onFailure() {
                    Toast.makeText(that, "refresh token failed", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    public void onCheckMeClicked(View view) {
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
        new APIUtil.GetMe(this, new MyCallback()).execute();
    }

    public void act1Clicked(View view) {
        startActivity(new Intent(this, ShowBoardsActivity.class));
    }

    public void act2Clicked(View view) {
        Intent intent = new Intent(this, ShowBoardsActivity.class);
        intent.putExtra(ShowBoardsActivity.INTENT_KEY_ID, ShowBoardsActivity.ID_CUSTOM);
        startActivity(intent);
    }

    public void act3Clicked(View view) {
        Intent intent = new Intent(this, ShowBoardsActivity.class);
        intent.putExtra(ShowBoardsActivity.INTENT_KEY_ID, 2);
        startActivity(intent);
    }

    public void act4Clicked(View view) {
        Intent intent = new Intent(this, ShowTopicsActivity.class);
        intent.putExtra(ShowTopicsActivity.INTENT_KEY_ID, 100);
        startActivity(intent);
    }

    public void act5Clicked(View view) {
        Intent intent = new Intent(this, ShowTopicsActivity.class);
        intent.putExtra(ShowTopicsActivity.INTENT_KEY_ID, ShowTopicsActivity.ID_NEW);
        startActivity(intent);
    }

    public void act6Clicked(View view) {
        Intent intent = new Intent(this, ShowTopicsActivity.class);
        intent.putExtra(ShowTopicsActivity.INTENT_KEY_ID, ShowTopicsActivity.ID_HOT);
        startActivity(intent);
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
