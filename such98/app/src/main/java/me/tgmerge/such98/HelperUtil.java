package me.tgmerge.such98;

import android.content.Context;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by tgmerge on 2/13.
 */
public final class HelperUtil {
    public static final void generalDebug(String cat, String msg) {
        Log.d(cat, ((Looper.getMainLooper().equals(Looper.myLooper())) ? "[UI]" : "[notUI]") + msg);
    }

    public static final void generalError(String cat, String msg) {
        Log.e(cat, ((Looper.getMainLooper().equals(Looper.myLooper())) ? "[UI]" : "[notUI]") + msg);
    }

    public static final void errorToast(Context ctx, CharSequence msg) {
        Toast.makeText(ctx, "ERROR:" + msg, Toast.LENGTH_LONG).show();
    }

    public static final void debugToast(Context ctx, CharSequence msg) {
        Toast.makeText(ctx, "INFO:" + msg, Toast.LENGTH_LONG).show();
    }
}
