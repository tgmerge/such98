package me.tgmerge.such98.activity;

import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import me.tgmerge.such98.R;
import me.tgmerge.such98.fragment.PostsFragment;
import me.tgmerge.such98.fragment.NavDrawerFragment;
import me.tgmerge.such98.util.ActivityUtil;

public class ShowPostsActivity extends ActionBarActivity {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavDrawerFragment mNavigationDrawerFragment;

    public static final String INTENT_KEY_ID = "id";
    public static final String INTENT_KEY_START_POS = "pos";

    private int mIntentId;
    private int mIntentPos;
    private int mFragmentContentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_display);

        mNavigationDrawerFragment = (NavDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);

        mIntentId = getIntent().getIntExtra(INTENT_KEY_ID, 0);
        mIntentPos = getIntent().getIntExtra(INTENT_KEY_START_POS, PostsFragment.PARAM_POS_BEGINNING);
        mFragmentContentId = R.id.container;

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        // Set up the content.
        ActivityUtil.Action.addPostFragment(this, mFragmentContentId, mIntentId, mIntentPos);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_show_posts, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_refresh:
                ActivityUtil.Action.reloadFragment(this, mFragmentContentId);
                break;
            case R.id.action_reply:
                ActivityUtil.openNewPostDialog(this, mIntentId, "", "");
                return true;
            case R.id.action_toFirstPage:
                ActivityUtil.Action.postGotoFirstPage(this, mFragmentContentId, mIntentId);
                return true;
            case R.id.action_toLastPage:
                ActivityUtil.Action.postGotoLastPage(this, mFragmentContentId, mIntentId);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
