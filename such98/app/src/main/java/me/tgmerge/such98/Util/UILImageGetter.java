package me.tgmerge.such98.Util;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import me.tgmerge.such98.R;

public class UILImageGetter implements Html.ImageGetter {
    Context c;
    TextView container;

    public UILImageGetter(View textView, Context context) {
        this.c = context;
        this.container = (TextView) textView;
    }

    @Override
    public Drawable getDrawable(String source) {
        UrlImageDownloader urlDrawable = new UrlImageDownloader(c.getResources(), source);
        urlDrawable.drawable = c.getResources().getDrawable(R.drawable.ic_close_black_48dp);

        ImageLoader.getInstance().loadImage(source, new SimpleListener(urlDrawable));
        return urlDrawable;
    }

    private class SimpleListener extends SimpleImageLoadingListener {
        UrlImageDownloader urlImageDownloader;

        public SimpleListener(UrlImageDownloader downloader) {
            super();
            urlImageDownloader = downloader;
        }

        @Override
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
            int width = loadedImage.getWidth();
            int height = loadedImage.getHeight();

            int newWidth = width;
            int newHeight = height;

            if (width > container.getWidth()) {
                newWidth = container.getWidth();
                newHeight = (newWidth * height) / width;
            }

            if (view != null) {
                view.getLayoutParams().width = newWidth;
                view.getLayoutParams().height = newHeight;
            }

            Drawable result = new BitmapDrawable(c.getResources(), loadedImage);
            result.setBounds(0, 0, newWidth, newHeight);

            urlImageDownloader.setBounds(0, 0, newWidth, newHeight);
            urlImageDownloader.drawable = result;

            container.setHeight((container.getHeight() + result.getIntrinsicHeight()));
            container.invalidate();
        }
    }

    private class UrlImageDownloader extends BitmapDrawable {
        public Drawable drawable;

        public UrlImageDownloader(Resources res, String filepath) {
            super(res, filepath);
            drawable = new BitmapDrawable(res, filepath);
        }

        @Override
        public void draw(Canvas canvas) {
            if (drawable != null) {
                drawable.draw(canvas);
            }
        }
    }
}