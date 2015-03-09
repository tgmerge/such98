package me.tgmerge.such98.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import me.tgmerge.such98.R;

/**
 * Created by tgmerge on 3/9.
 */
public class MessageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public ImageView icon;
    public TextView title;
    public TextView type;
    public TextView content;

    public int data_messageId;

    public MessageViewHolder(View itemLayoutView) {
        super(itemLayoutView);
        icon = (ImageView) itemLayoutView.findViewById(R.id.img);
        title = (TextView) itemLayoutView.findViewById(R.id.text_title);
        type = (TextView) itemLayoutView.findViewById(R.id.text_type);
        content = (TextView) itemLayoutView.findViewById(R.id.text_content);

        itemLayoutView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v instanceof RelativeLayout) {
            // todo
        }
    }
}
