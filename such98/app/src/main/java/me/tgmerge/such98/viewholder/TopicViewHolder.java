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
    public TextView authorName;
    public TextView boardName;
    public TextView postTime;
    public TextView replyInfo;

    public int data_topicId;

    public TopicViewHolder(View itemLayoutView) {
        super(itemLayoutView);
        icon = (ImageView) itemLayoutView.findViewById(R.id.img);
        title = (TextView) itemLayoutView.findViewById(R.id.text_title);
        authorName = (TextView) itemLayoutView.findViewById(R.id.text_author_name);
        boardName = (TextView) itemLayoutView.findViewById(R.id.text_board_name);
        postTime = (TextView) itemLayoutView.findViewById(R.id.text_post_time);
        replyInfo = (TextView) itemLayoutView.findViewById(R.id.text_reply_info);

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
