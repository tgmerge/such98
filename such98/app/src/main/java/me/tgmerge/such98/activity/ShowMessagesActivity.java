package me.tgmerge.such98.activity;

import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;

import me.tgmerge.such98.R;
import me.tgmerge.such98.fragment.MessagesFragment;
import me.tgmerge.such98.fragment.DrawerFragment;
import me.tgmerge.such98.util.ActivityUtil;

public class ShowMessagesActivity extends ActionBarActivity {

    private DrawerFragment mDrawerFragment;

    public static final String INTENT_KEY_FILTER = "filter";
    public static final String INTENT_KEY_START_POS = "pos";

    private int mIntentFilter;
    private int mIntentPos;
    private int mFragmentContentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_and_drawer);

        mDrawerFragment = (DrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);

        mIntentFilter = getIntent().getIntExtra(INTENT_KEY_FILTER, MessagesFragment.PARAM_FILTER_NONE);
        mIntentPos = getIntent().getIntExtra(INTENT_KEY_START_POS, MessagesFragment.PARAM_POS_BEGINNING);
        mFragmentContentId = R.id.container;

        mDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        ActivityUtil.loadMessagesFragment(this, mFragmentContentId, mIntentFilter, mIntentPos);
    }
}
