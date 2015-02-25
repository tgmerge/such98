package me.tgmerge.such98.custom;

import android.content.ActivityNotFoundException;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.view.MotionEvent;
import android.widget.TextView;
import android.widget.Toast;

import me.tgmerge.such98.R;
import me.tgmerge.such98.util.HelperUtil;

/**
 * Created by tgmerge on 2/25.
 */
public class MultiTypeLinkMovementMethod extends LinkMovementMethod {
    @Override
    public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event) {
        try {
            return super.onTouchEvent(widget, buffer, event) ;
        } catch(ActivityNotFoundException ex) {
            HelperUtil.errorToast(SuchApp.getStr(R.string.custom_MultiTypeLinkMovementMethod_cannot_load_link));
            return true;
        }
    }

    public static MovementMethod getInstance() {
        if (sInstance == null)
            sInstance = new MultiTypeLinkMovementMethod();

        return sInstance;
    }

    private static MultiTypeLinkMovementMethod sInstance;
}
