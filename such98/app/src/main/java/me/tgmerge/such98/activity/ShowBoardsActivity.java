package me.tgmerge.such98.activity;

import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;

import me.tgmerge.such98.R;
import me.tgmerge.such98.fragment.BoardsFragment;
import me.tgmerge.such98.fragment.NavDrawerFragment;
import me.tgmerge.such98.util.ActivityUtil;

public class ShowBoardsActivity extends ActionBarActivity {

    private NavDrawerFragment mNavigationDrawerFragment;

    public static final String INTENT_KEY_ID = "id";
    public static final String INTENT_KEY_START_POS = "pos";

    private int mIntentId;
    private int mIntentPos;
    private int mFragmentContentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base_fragment_drawer);

        mNavigationDrawerFragment = (NavDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);

        mIntentId = getIntent().getIntExtra(INTENT_KEY_ID, 0);
        mIntentPos = getIntent().getIntExtra(INTENT_KEY_START_POS, BoardsFragment.PARAM_POS_BEGINNING);
        mFragmentContentId = R.id.container;

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        ActivityUtil.loadBoardsFragment(this, mFragmentContentId, mIntentId, mIntentPos);
    }
}
