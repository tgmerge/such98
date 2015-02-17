package me.tgmerge.such98.Util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import me.tgmerge.such98.R;

/**
 * Created by tgmerge on 2/16.
 */
public class ImageUtil {

    public static final ImageLoader getImageLoader(Context ctx) {
        if (!ImageLoader.getInstance().isInited()) {
            DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                    .showImageOnFail(R.drawable.ic_close_white_48dp)
                    .cacheInMemory(true)
                    .cacheOnDisk(true)
                    .displayer(new RoundedBitmapDisplayer(80))
                    .build();

            ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(ctx.getApplicationContext())
                    .threadPoolSize(2)
                    .diskCacheFileCount(500)
                    .defaultDisplayImageOptions(defaultOptions)
                    .build();

            ImageLoader.getInstance().init(config);
        }
        return ImageLoader.getInstance();
    }

    // it's a async- task, so isRecyclable showld be set to false first
    // to prevent problem caused by recycling viewHolder before image downloaded
    public static final void setViewHolderImage(Activity act, final RecyclerView.ViewHolder viewHolder, final ImageView imgView, String url) {
        viewHolder.setIsRecyclable(false);
        ImageUtil.getImageLoader(act).displayImage(url, imgView, new SimpleImageLoadingListener() {
            public void onLoadingComplete(String uri, View view, Bitmap loadedImage) {
                imgView.setPadding(0, 0, 0, 0);
                viewHolder.setIsRecyclable(true);
            }

            public void onLoadingFailed(String uri, View view, FailReason reason) {
                viewHolder.setIsRecyclable(true);
            }

            public void onLoadingCancelled(String uri, View view) {
                viewHolder.setIsRecyclable(true);
            }
        });
    }
}
