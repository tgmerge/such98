package me.tgmerge.such98.util;

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
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import me.tgmerge.such98.R;

/**
 * Created by tgmerge on 2/16.
 * Provides methods for loading and displaying image (i.e. avatar)
 */
public class ImageUtil {

    public static final ImageLoader getImageLoader(Context ctx) {
        if (!ImageLoader.getInstance().isInited()) {
            ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(ctx.getApplicationContext())
                    .denyCacheImageMultipleSizesInMemory()
                    .threadPoolSize(2)
                    .memoryCacheSize(2 * 1024 * 1024)
                    .diskCacheFileCount(500)
                    .build();

            ImageLoader.getInstance().init(config);
        }
        return ImageLoader.getInstance();
    }


    public static final void setImage(Context ctx, final ImageView imgView, String url) {
        setImage(ctx, imgView, 0, url);
    }


    public static final void setImage(Context ctx, final ImageView imgView, final int roundRadius, String url) {
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.ic_dots_horizontal_white_48dp)
                .showImageOnFail(R.drawable.ic_close_white_48dp)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .displayer(roundRadius > 0 ? new RoundedBitmapDisplayer(roundRadius) : new SimpleBitmapDisplayer())
                .build();
        ImageUtil.getImageLoader(ctx).displayImage(url, imgView, options, new SimpleImageLoadingListener() {
            public void onLoadingComplete(String uri, View view, Bitmap loadedImage) {
                if (roundRadius > 0) {
                    imgView.setPadding(0, 0, 0, 0);
                }
            }
        });
    }


    // Download image, load it on ImageView in a PostViewHolder.
    //   It's a async-task, so isRecyclable flag of PostViewHolder should be set to false first,
    //     to prevent problems from recycling viewHolder before image is downloaded and set.
    //   Once the image is set, or failed to load, the isRecyclable flag should be set to true.
    public static final void setViewHolderImage(Context ctx, final RecyclerView.ViewHolder viewHolder, final ImageView imgView, int roundRadius, String url, final boolean handleRecyclable) {
        if (handleRecyclable) {
            viewHolder.setIsRecyclable(false);
        }
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.ic_dots_horizontal_white_48dp)
                .showImageOnFail(R.drawable.ic_close_white_48dp)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .displayer(new RoundedBitmapDisplayer(roundRadius))
                .build();
        ImageUtil.getImageLoader(ctx).displayImage(url, imgView, options, new SimpleImageLoadingListener() {
            public void onLoadingComplete(String uri, View view, Bitmap loadedImage) {
                imgView.setPadding(0, 0, 0, 0);
                if (handleRecyclable) {
                    viewHolder.setIsRecyclable(true);
                }
            }

            public void onLoadingFailed(String uri, View view, FailReason reason) {
                if (handleRecyclable) {
                    viewHolder.setIsRecyclable(true);
                }
            }

            public void onLoadingCancelled(String uri, View view) {
                if (handleRecyclable) {
                    viewHolder.setIsRecyclable(true);
                }
            }
        });
    }
}
