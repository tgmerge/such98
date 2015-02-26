package me.tgmerge.such98.adapter;

import android.app.Activity;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.apache.http.Header;

import me.tgmerge.such98.R;
import me.tgmerge.such98.custom.SuchApp;
import me.tgmerge.such98.viewholder.PostViewHolder;
import me.tgmerge.such98.util.APIUtil;
import me.tgmerge.such98.util.BBUtil;
import me.tgmerge.such98.util.CacheUtil;
import me.tgmerge.such98.util.ImageUtil;
import me.tgmerge.such98.util.XMLUtil;

public class PostsAdapter extends RecyclerSwipeAdapter<XMLUtil.PostInfo, PostViewHolder> {

    private XMLUtil.ArrayOf<XMLUtil.PostInfo> mData;
    private XMLUtil.TopicInfo mTopicInfo;
    private Activity mAct;

    private boolean mIsNeverLoaded = true;
    private SwipeRefreshLayout mSwipeLayout = null;

    public final void setTopicInfo(XMLUtil.TopicInfo topicInfo) {
        mTopicInfo = topicInfo;
    }

    public final void setSwipeLayout(SwipeRefreshLayout swipeLayout) {
        mSwipeLayout = swipeLayout;
    }

    public final void appendData(XMLUtil.ArrayOf<XMLUtil.PostInfo> data) {
        int oldItemCount = 0;
        if (mData == null) {
            mData = data;
        } else {
            oldItemCount = mData.size();
            mData.append(data);
        }
        notifyItemRangeInserted(oldItemCount, data.size());
    }

    public final void appendDataFront(XMLUtil.ArrayOf<XMLUtil.PostInfo> data) {
        if (mData == null) {
            mData = data;
        } else {
            mData.appendFront(data);
        }
        notifyItemRangeInserted(0, data.size());
    }

    public PostsAdapter(Activity act, XMLUtil.TopicInfo topicInfo, XMLUtil.ArrayOf<XMLUtil.PostInfo> data) {
        mData = data;
        mTopicInfo = topicInfo;
        mAct = act;
    }

    @Override
    public PostViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemLayoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(itemLayoutView);
    }

    @Override
    public void onBindViewHolder(final PostViewHolder viewHolder, int position) {

        if (mIsNeverLoaded) {
            if (mSwipeLayout != null) {
                mSwipeLayout.setEnabled(true);
            }
            mIsNeverLoaded = false;
        }

        final XMLUtil.PostInfo dataItem = mData.get(position);

        viewHolder.data_topicInfo = mTopicInfo;
        viewHolder.data_postInfo = mData.get(position);

        viewHolder.title.setText(dataItem.Floor != 1 && dataItem.Title.length() == 0 ? SuchApp.getStr(R.string.adapter_posts_default_title, dataItem.Floor) : dataItem.Title);
        viewHolder.authorInfo.setText(SuchApp.getStr(R.string.adapter_posts_author_info, dataItem.UserName, dataItem.Time));
        BBUtil.setBBcodeToTextView(viewHolder.content, mAct, dataItem.Content); // todo prevent to process every time

        // todo brute way to solve:
        //      some of the viewHolder.content is not selectable
        viewHolder.setIsRecyclable(false);
        viewHolder.content.setMovementMethod(LinkMovementMethod.getInstance());

        if (dataItem.Floor == 1) {
            viewHolder.replyInfo.setVisibility(View.VISIBLE);
            viewHolder.replyInfo.setText(SuchApp.getStr(R.string.adapter_posts_reply_info, viewHolder.data_topicInfo.HitCount, viewHolder.data_topicInfo.ReplyCount));
        }

        // PostViewHolder 异步加载图像： 加载之前设置viewHolder.setRecyclable(false)
        //                         加载之后设置viewHolder.setRecyclable(true)
        viewHolder.avatar.setImageResource(R.drawable.ic_dots_horizontal_white_36dp);
        String avaUrl = CacheUtil.id_avaUrlCache.get(dataItem.UserId);
        if (avaUrl != null) {
            ImageUtil.setViewHolderImage(mAct, viewHolder, viewHolder.avatar, 80, avaUrl, false);
        } else {
            //viewHolder.setIsRecyclable(false); // todo pair with (true)
            new APIUtil.GetIdUser(mAct, dataItem.UserId, new APIUtil.APICallback() {
                @Override
                public void onSuccess(int statCode, Header[] headers, byte[] body) {
                    XMLUtil.UserInfo info = new XMLUtil.UserInfo();
                    try {
                        info.parse(new String(body));
                    } catch (Exception e) {
                        e.printStackTrace();
                        onFailure(-1, headers, body, e);
                        return;
                    }
                    String newAvaUrl = info.PortraitUrl.startsWith("http") ? info.PortraitUrl : ("http://www.cc98.org/" + info.PortraitUrl);
                    CacheUtil.id_avaUrlCache.put(info.Id, newAvaUrl);
                    ImageUtil.setViewHolderImage(mAct, viewHolder, viewHolder.avatar, 80, newAvaUrl, false);
                    //viewHolder.setIsRecyclable(true);
                }

                @Override
                public void onFailure(int statCode, Header[] headers, byte[] body, Throwable error) {
                    viewHolder.avatar.setImageResource(R.drawable.ic_close_white_36dp);
                    //viewHolder.setIsRecyclable(true);
                }
            }).execute();
        }

        // reset toolbar status
        viewHolder.imgShowPostAction.setVisibility(View.VISIBLE);
        viewHolder.postActionBar.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return (mData == null) ? 0 : mData.size();
    }
}