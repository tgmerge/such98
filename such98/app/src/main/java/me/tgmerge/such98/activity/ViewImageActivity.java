package me.tgmerge.such98.activity;

import android.graphics.Bitmap;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import me.tgmerge.such98.R;
import me.tgmerge.such98.util.ImageUtil;
import uk.co.senab.photoview.PhotoViewAttacher;

public class ViewImageActivity extends ActionBarActivity {

    public static final String INTENT_KEY_URL = "url";

    String mIntentUrl;
    ImageView mImageView;
    PhotoViewAttacher mAttacher;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_activity_view_image, menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_image);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mIntentUrl = getIntent().getStringExtra(INTENT_KEY_URL);
        mImageView = (ImageView) findViewById(R.id.image);

        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.ic_dots_horizontal_grey600_48dp)
                .showImageOnFail(R.drawable.ic_close_grey600_48dp)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .build();
        ImageUtil.getImageLoader(this).displayImage(mIntentUrl, mImageView, options, new SimpleImageLoadingListener() {
            public void onLoadingComplete(String uri, View view, Bitmap loadedImage) {
                mAttacher = new PhotoViewAttacher(mImageView);
            }
        });
    }
}
