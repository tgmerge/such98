package me.tgmerge.such98.adapter;

import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import me.tgmerge.such98.R;
import me.tgmerge.such98.custom.SuchApp;
import me.tgmerge.such98.viewholder.BoardViewHolder;
import me.tgmerge.such98.util.XMLUtil;

/**
* Created by tgmerge on 2/22.
*/
public class BoardsAdapter extends RecyclerSwipeAdapter<XMLUtil.BoardInfo, BoardViewHolder> {

    private XMLUtil.ArrayOf<XMLUtil.BoardInfo> mData;

    private boolean mIsNeverLoaded = true;
    private SwipeRefreshLayout mSwipeLayout = null;

    @Override
    public void setSwipeLayout(SwipeRefreshLayout swipeLayout) {
        mSwipeLayout = swipeLayout;
    }

    public final void appendData(XMLUtil.ArrayOf<XMLUtil.BoardInfo> data) {
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
    public void appendDataFront(XMLUtil.ArrayOf<XMLUtil.BoardInfo> data) {
        if (mData == null) {
            mData = data;
        } else {
            mData.appendFront(data);
        }
        notifyItemRangeInserted(0, data.size());
    }

    public BoardsAdapter(XMLUtil.ArrayOf<XMLUtil.BoardInfo> data) {
        mData = data;
    }

    @Override
    public BoardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemLayoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_board, parent, false);
        return new BoardViewHolder(itemLayoutView);
    }

    @Override
    public void onBindViewHolder(BoardViewHolder viewHolder, int position) {

        if (mIsNeverLoaded) {
            if (mSwipeLayout != null) {
                mSwipeLayout.setEnabled(true);
            }
            mIsNeverLoaded = false;
        }

        XMLUtil.BoardInfo dataItem = mData.get(position);

        viewHolder.icon.setImageResource(dataItem.IsCategory ? R.drawable.ic_folder_multiple_outline_white_36dp : R.drawable.ic_folder_outline_white_36dp);
        viewHolder.name.setText(dataItem.Name);
        viewHolder.isCategory.setText(dataItem.IsCategory ? SuchApp.getStr(R.string.adapter_boards_is_category) : SuchApp.getStr(R.string.adapter_boards_is_not_category));
        viewHolder.description.setText(Html.fromHtml(dataItem.Description));

        viewHolder.data_boardId = dataItem.Id;
        viewHolder.data_isCat = dataItem.IsCategory;
    }

    @Override
    public int getItemCount() {
        return (mData == null) ? 0 : mData.size();
    }
}
