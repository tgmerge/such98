package me.tgmerge.such98.custom;

import android.app.Application;
import android.content.Context;

/**
 * Created by tgmerge on 2/19.
 * Application.
 * Provide a static way to get Application context.
 */
public final class SuchApp extends Application {

    private static SuchApp instance;

    public SuchApp() {
        instance = this;
    }

    public static  Context getContext() {
        return instance;
    }

    public static String getStr(int resId) {
        return instance.getResources().getString(resId);
    }

    public static  String getStr(int resId, Object... formatArgs) {
        return instance.getResources().getString(resId, formatArgs);
    }

}
