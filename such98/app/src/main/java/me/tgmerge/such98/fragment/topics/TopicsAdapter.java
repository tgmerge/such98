package me.tgmerge.such98.fragment.topics;

import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import me.tgmerge.such98.R;
import me.tgmerge.such98.util.XMLUtil;

class TopicsAdapter extends RecyclerView.Adapter<TopicViewHolder> {

    private XMLUtil.ArrayOf<? extends XMLUtil.XMLObj> mData;
    private Class mDataItemClass;

    private boolean mIsNeverLoaded = true;
    private SwipeRefreshLayout mSwipeLayout = null;

    public final void setSwipeLayout(SwipeRefreshLayout swipeLayout) {
        mSwipeLayout = swipeLayout;
    }

    public final void setData(XMLUtil.ArrayOf<? extends XMLUtil.XMLObj> data) {
        Class itemClass = (data == null) ? null : data.getItemClass();
        if (itemClass != null && itemClass != XMLUtil.HotTopicInfo.class && itemClass != XMLUtil.TopicInfo.class) {
            return;
        }
        mDataItemClass = itemClass;
        mData = data;
        notifyDataSetChanged();
    }

    public final void appendData(XMLUtil.ArrayOf<? extends XMLUtil.XMLObj> data) {
        if (mData == null) {
            setData(data);
            return;
        }

        if (data.getItemClass() != mDataItemClass) {
            return;
        }

        int oldItemCount = mData.size();

        if (mDataItemClass == XMLUtil.HotTopicInfo.class) {
            ((XMLUtil.ArrayOf<XMLUtil.HotTopicInfo>) mData).append((XMLUtil.ArrayOf<XMLUtil.HotTopicInfo>) data);
        } else {
            ((XMLUtil.ArrayOf<XMLUtil.TopicInfo>) mData).append((XMLUtil.ArrayOf<XMLUtil.TopicInfo>) data);
        }

        notifyItemRangeInserted(oldItemCount, data.size());
    }

    public final void appendDataFront(XMLUtil.ArrayOf<? extends XMLUtil.XMLObj> data) {
        if (mData == null) {
            setData(data);
            return;
        }

        if (data.getItemClass() != mDataItemClass) {
            return;
        }

        if (mDataItemClass == XMLUtil.HotTopicInfo.class) {
            ((XMLUtil.ArrayOf<XMLUtil.HotTopicInfo>) mData).appendFront((XMLUtil.ArrayOf<XMLUtil.HotTopicInfo>) data);
        } else {
            ((XMLUtil.ArrayOf<XMLUtil.TopicInfo>) mData).appendFront((XMLUtil.ArrayOf<XMLUtil.TopicInfo>) data);
        }

        notifyItemRangeInserted(0, data.size());
    }

    public TopicsAdapter(XMLUtil.ArrayOf<? extends XMLUtil.XMLObj> data) {
        Class itemClass = (data == null) ? null : data.getItemClass();
        if (itemClass != null && itemClass != XMLUtil.HotTopicInfo.class && itemClass != XMLUtil.TopicInfo.class) {
            return;
        }
        mDataItemClass = itemClass;
        mData = data;
    }


    @Override
    public TopicViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemLayoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_topic_card, parent, false);
        return new TopicViewHolder(itemLayoutView);
    }


    @Override
    public void onBindViewHolder(TopicViewHolder viewHolder, int position) {

        if (mIsNeverLoaded) {
            if (mSwipeLayout != null) {
                mSwipeLayout.setEnabled(true);
            }
            mIsNeverLoaded = false;
        }

        if (mDataItemClass == XMLUtil.TopicInfo.class) {
            XMLUtil.ArrayOf<XMLUtil.TopicInfo> thisData = (XMLUtil.ArrayOf<XMLUtil.TopicInfo>) mData;
            XMLUtil.TopicInfo dataItem = thisData.get(position);

            viewHolder.icon.setImageResource(dataItem.TopState.equals(XMLUtil.TopicInfo.TOPSTATE_NONE)
                    ? R.drawable.ic_comment_text_outline_white_36dp
                    : R.drawable.ic_comment_alert_outline_white_36dp);

            viewHolder.title.setText(dataItem.Title);
            viewHolder.authorInfo.setText(dataItem.AuthorName + " @ " + dataItem.CreateTime);
            viewHolder.lastPostInfo.setText(dataItem.LastPostInfo.UserName + " @ " + dataItem.LastPostInfo.Time);

            viewHolder.data_topicId = dataItem.Id;

        } else {
            XMLUtil.ArrayOf<XMLUtil.HotTopicInfo> thisData = (XMLUtil.ArrayOf<XMLUtil.HotTopicInfo>) mData;
            XMLUtil.HotTopicInfo dataItem = thisData.get(position);

            viewHolder.icon.setImageResource(R.drawable.ic_comment_fire_outline_white_36dp);

            viewHolder.title.setText(dataItem.Title);
            viewHolder.authorInfo.setText(dataItem.AuthorName + " @ " + dataItem.CreateTime);
            viewHolder.lastPostInfo.setText(dataItem.BoardName + ", " + dataItem.ParticipantCount + "人参与");

            viewHolder.data_topicId = dataItem.Id;
        }
    }

    @Override
    public int getItemCount() {
        return (mData == null) ? 0 : mData.size();
    }
}
