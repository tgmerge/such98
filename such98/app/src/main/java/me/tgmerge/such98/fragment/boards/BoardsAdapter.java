package me.tgmerge.such98.fragment.boards;

import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import me.tgmerge.such98.R;
import me.tgmerge.such98.fragment.base.RecyclerSwipeAdapter;
import me.tgmerge.such98.util.XMLUtil;

/**
* Created by tgmerge on 2/22.
*/
class BoardsAdapter extends RecyclerSwipeAdapter<XMLUtil.BoardInfo, BoardViewHolder> {

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
        View itemLayoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_board_card, parent, false);
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
        viewHolder.isCategory.setText(dataItem.IsCategory ? "分类" : "");
        viewHolder.description.setText(dataItem.Description);

        viewHolder.data_boardId = dataItem.Id;
        viewHolder.data_isCat = dataItem.IsCategory;

        // todo API有问题，于是在所有“分类”版面下显示一个“强制按帖子版面打开”的选项……
        viewHolder.openAsNotCat.setVisibility(dataItem.IsCategory ? View.VISIBLE : View.GONE);
    }

    @Override
    public int getItemCount() {
        return (mData == null) ? 0 : mData.size();
    }
}
