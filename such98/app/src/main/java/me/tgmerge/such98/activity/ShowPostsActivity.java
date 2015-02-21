package me.tgmerge.such98.activity;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import me.tgmerge.such98.R;
import me.tgmerge.such98.fragment.PostsFragment;
import me.tgmerge.such98.fragment.NavDrawerFragment;

public class ShowPostsActivity extends ActionBarActivity {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavDrawerFragment mNavigationDrawerFragment;

    public static final String INTENT_KEY_ID = "id";
    public static final String INTENT_KEY_START_POS = "pos";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_display);

        mNavigationDrawerFragment = (NavDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        // Set up the content.
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.add(R.id.container, PostsFragment.newInstance(
                        getIntent().getIntExtra(INTENT_KEY_ID, 0),
                        getIntent().getIntExtra(INTENT_KEY_START_POS, 0))
        );
        transaction.commit();
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
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_toLastPage) {
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.replace(R.id.container, PostsFragment.newInstance(
                    getIntent().getIntExtra(INTENT_KEY_ID, 0),
                    PostsFragment.PARAM_POS_END
            ));
            transaction.commit();
            return true;
        } else if (id == R.id.action_toFirstPage) {
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.replace(R.id.container, PostsFragment.newInstance(
                    getIntent().getIntExtra(INTENT_KEY_ID, 0),
                    PostsFragment.PARAM_POS_BEGINNING
            ));
            transaction.commit();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
