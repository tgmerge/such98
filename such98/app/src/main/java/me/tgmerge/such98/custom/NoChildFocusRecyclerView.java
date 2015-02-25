package me.tgmerge.such98.custom;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by tgmerge on 2/24.
 */
public class NoChildFocusRecyclerView extends RecyclerView {
    public NoChildFocusRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public NoChildFocusRecyclerView(Context context) {
        super(context);
    }

    public NoChildFocusRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void requestChildFocus(View child, View focused) {
        if (focused instanceof TextView) {
            return;
        }
        super.requestChildFocus(child, focused);
    }
}
