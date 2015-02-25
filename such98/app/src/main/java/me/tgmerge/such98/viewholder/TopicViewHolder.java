package me.tgmerge.such98.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import me.tgmerge.such98.R;
import me.tgmerge.such98.util.ActivityUtil;
import me.tgmerge.such98.util.HelperUtil;

public class TopicViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public ImageView icon;
    public TextView title;
    public TextView authorInfo;
    public TextView lastPostInfo;

    public int data_topicId;

    public TopicViewHolder(View itemLayoutView) {
        super(itemLayoutView);
        icon = (ImageView) itemLayoutView.findViewById(R.id.img);
        title = (TextView) itemLayoutView.findViewById(R.id.text_title);
        authorInfo = (TextView) itemLayoutView.findViewById(R.id.text_authorInfo);
        lastPostInfo = (TextView) itemLayoutView.findViewById(R.id.text_lastPostInfo);

        itemLayoutView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        HelperUtil.generalDebug("ShowTopicsFragment", "onClick! " + v.toString());
        if (v instanceof RelativeLayout) {
            // click on whole item
            HelperUtil.generalDebug("ShowTopicsFragment", "Clicked: " + data_topicId);
            ActivityUtil.openShowPostsActivity(v.getContext(), data_topicId, 0, false);
        }
    }
}
