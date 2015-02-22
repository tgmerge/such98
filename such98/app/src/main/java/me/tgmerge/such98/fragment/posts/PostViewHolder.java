package me.tgmerge.such98.fragment.posts;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import me.tgmerge.such98.R;
import me.tgmerge.such98.util.ActivityUtil;
import me.tgmerge.such98.util.HelperUtil;
import me.tgmerge.such98.util.XMLUtil;

class PostViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public ImageView avatar;
    public TextView title;
    public TextView authorInfo;
    public TextView replyInfo;
    public TextView content;

    public ImageView imgReply;
    public ImageView imgQuote;

    public XMLUtil.TopicInfo data_topicInfo;
    public XMLUtil.PostInfo data_postInfo;

    public PostViewHolder(View itemLayoutView) {
        super(itemLayoutView);
        avatar = (ImageView) itemLayoutView.findViewById(R.id.img);
        title = (TextView) itemLayoutView.findViewById(R.id.text_title);
        authorInfo = (TextView) itemLayoutView.findViewById(R.id.text_authorInfo);
        replyInfo = (TextView) itemLayoutView.findViewById(R.id.text_replyInfo);
        content = (TextView) itemLayoutView.findViewById(R.id.text_content);
        imgReply = (ImageView) itemLayoutView.findViewById(R.id.image_reply);
        imgQuote = (ImageView) itemLayoutView.findViewById(R.id.image_quote);

        // set item click listener
        imgReply.setOnClickListener(this);
        imgQuote.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        HelperUtil.generalDebug("PostsFragment", "onClick " + v.toString());
        switch (v.getId()) {
            case R.id.image_reply:
                String replyTitle = (data_postInfo.Floor == 1) ? "" : "回复 " + data_postInfo.UserName + "(#" + data_postInfo.Floor + ")";
                ActivityUtil.openNewPostDialog(v.getContext(), data_topicInfo.Id, replyTitle, "");
                break;
            case R.id.image_quote:
                String quoteTitle = "回复 " + data_postInfo.UserName + "(#" + data_postInfo.Floor + ")";
                ActivityUtil.openNewPostDialog(v.getContext(), data_topicInfo.Id, quoteTitle,
                        "[quotex][i]> " + data_postInfo.UserName + "@" + data_postInfo.Time + "(#" + data_postInfo.Floor + ")[/i]\n" +
                                data_postInfo.Content + "[/quotex]\n\n");
        }
    }
}