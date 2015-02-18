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
 * Provides methods for loading and displaying image (i.e. avatar)
 */
public class ImageUtil {

    public static final ImageLoader getImageLoader(Context ctx) {
        return getImageLoader(ctx, 80);
    }

    public static final ImageLoader getImageLoader(Context ctx, int roundRadius) {
        if (!ImageLoader.getInstance().isInited()) {
            DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                    .showImageOnFail(R.drawable.ic_close_white_48dp)
                    .cacheInMemory(true)
                    .cacheOnDisk(true)
                    .displayer(new RoundedBitmapDisplayer(roundRadius))
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


    public static final void setImage(Activity act, final ImageView imgView, int roundRadius, String url) {
        ImageUtil.getImageLoader(act, roundRadius).displayImage(url, imgView, new SimpleImageLoadingListener() {
            public void onLoadingComplete(String uri, View view, Bitmap loadedImage) {
                imgView.setPadding(0, 0, 0, 0);
            }
        });
    }


    // Download image, load it on ImageView in a ViewHolder.
    //   It's a async-task, so isRecyclable flag of ViewHolder should be set to false first,
    //     to prevent problems from recycling viewHolder before image is downloaded and set.
    //   Once the image is set, or failed to load, the isRecyclable flag should be set to true.
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
