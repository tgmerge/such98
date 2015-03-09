package me.tgmerge.such98.adapter;

import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import me.tgmerge.such98.R;
import me.tgmerge.such98.util.XMLUtil;
import me.tgmerge.such98.viewholder.MessageViewHolder;

/**
 * Created by tgmerge on 3/9.
 */
public class MessagesAdapter extends RecyclerSwipeAdapter<XMLUtil.MessageInfo, MessageViewHolder> {

    private XMLUtil.ArrayOf<XMLUtil.MessageInfo> mData;

    private boolean mIsNeverLoaded = true;
    private SwipeRefreshLayout mSwipeLayout = null;

    @Override
    public void setSwipeLayout(SwipeRefreshLayout swipeLayout) {
        mSwipeLayout = swipeLayout;
    }

    public final void appendData(XMLUtil.ArrayOf<XMLUtil.MessageInfo> data) {
        int oldItemCount = 0;
        if (mData == null) {
            mData = data;
        } else {
            oldItemCount = mData.size();
            mData.append(data);
        }
        notifyItemRangeInserted(oldItemCount, data.size());
    }

    @Override
    public void appendDataFront(XMLUtil.ArrayOf<XMLUtil.MessageInfo> data) {
        if (mData == null) {
            mData = data;
        } else {
            mData.appendFront(data);
        }
        notifyItemRangeInserted(0, data.size());
    }

    public MessagesAdapter(XMLUtil.ArrayOf<XMLUtil.MessageInfo> data) {
        mData = data;
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemLayoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
        return new MessageViewHolder(itemLayoutView);
    }

    @Override
    public void onBindViewHolder(MessageViewHolder viewHolder, int position) {

        if (mIsNeverLoaded) {
            if (mSwipeLayout != null) {
                mSwipeLayout.setEnabled(true);
            }
            mIsNeverLoaded = false;
        }

        XMLUtil.MessageInfo dataItem = mData.get(position);

        // todo set icon img

        viewHolder.title.setText(dataItem.Title);

        String type = "";
        if (dataItem.IsDraft) {
            type += " 草稿";
        }
        if (dataItem.IsRead) {
            type += " 已读";
        }
        viewHolder.type.setText(type);

        viewHolder.content.setText(dataItem.SenderName + " -> " + dataItem.ReceiverName);

        viewHolder.data_messageId = 0; // todo not supported by API
    }

    @Override
    public int getItemCount() {
        return (mData == null) ? 0 : mData.size();
    }
}
