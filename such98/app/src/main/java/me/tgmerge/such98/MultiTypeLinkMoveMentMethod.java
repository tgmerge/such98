package me.tgmerge.such98;

import android.content.ActivityNotFoundException;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.view.MotionEvent;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by tgmerge on 2/25.
 */
public class MultiTypeLinkMoveMentMethod extends LinkMovementMethod {
    @Override
    public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event) {
        try {
            return super.onTouchEvent(widget, buffer, event) ;
        } catch(ActivityNotFoundException ex) {
            Toast.makeText(widget.getContext(), "Could not load link", Toast.LENGTH_LONG).show();
            return true;
        }
    }

    public static MovementMethod getInstance() {
        if (sInstance == null)
            sInstance = new MultiTypeLinkMoveMentMethod();

        return sInstance;
    }

    private static MultiTypeLinkMoveMentMethod sInstance;
}
