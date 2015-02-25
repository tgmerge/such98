package me.tgmerge.such98.custom;

import android.content.Context;

/**
 * Created by tgmerge on 2/19.
 * Application.
 * Provide a static way to get Application context.
 */
public class SuchApplication extends android.app.Application {

    private static SuchApplication instance;

    public SuchApplication() {
        instance = this;
    }

    public static Context getContext() {
        return instance;
    }

}
