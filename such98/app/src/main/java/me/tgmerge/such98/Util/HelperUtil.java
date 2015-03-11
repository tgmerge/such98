package me.tgmerge.such98.util;

import android.content.Context;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import me.tgmerge.such98.custom.SuchApp;

/**
 * Created by tgmerge on 2/13.
 * Some misc utility methods
 */
public final class HelperUtil {
    public static final void generalDebug(String cat, String msg) {
        Log.d(cat, ((Looper.getMainLooper().equals(Looper.myLooper())) ? "[UI]" : "[notUI]") + msg);
    }

    public static final void generalError(String cat, String msg) {
        Log.e(cat, ((Looper.getMainLooper().equals(Looper.myLooper())) ? "[UI]" : "[notUI]") + msg);
    }

    public static final void errorToast(CharSequence msg) {
        generalError("HelperUtil", "errorToast: " + msg);
        Context context = SuchApp.getContext();
        if (context != null) {
            Toast.makeText(SuchApp.getContext(), "错误:" + msg, Toast.LENGTH_LONG).show();
        }
    }

    public static final void debugToast(CharSequence msg) {
        generalDebug("HelperUtil", "debugToast: " + msg);
        Context context = SuchApp.getContext();
        if (context != null) {
            Toast.makeText(SuchApp.getContext(), msg, Toast.LENGTH_SHORT).show();
        }
    }
}
