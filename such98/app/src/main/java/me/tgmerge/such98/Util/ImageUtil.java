package me.tgmerge.such98.Util;

import android.content.Context;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

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
}
