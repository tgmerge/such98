package me.tgmerge.such98.activity;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import me.tgmerge.such98.R;
import me.tgmerge.such98.util.ImageUtil;

public class ViewImageActivity extends ActionBarActivity {

    public static final String INTENT_KEY_URL = "url";

    String mIntentUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_image);

        mIntentUrl = getIntent().getStringExtra(INTENT_KEY_URL);

        ImageUtil.setImage(this, (ImageView) findViewById(R.id.image), mIntentUrl);
    }
}
