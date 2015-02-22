package me.tgmerge.such98.fragment.boards;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import me.tgmerge.such98.R;
import me.tgmerge.such98.util.ActivityUtil;
import me.tgmerge.such98.util.HelperUtil;


class BoardViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public ImageView icon;
    public TextView name;
    public TextView isCategory;
    public TextView description;
    public TextView openAsNotCat;

    public int data_boardId;
    public boolean data_isCat;

    public BoardViewHolder(View itemLayoutView) {
        super(itemLayoutView);
        icon = (ImageView) itemLayoutView.findViewById(R.id.img);
        name = (TextView) itemLayoutView.findViewById(R.id.text_name);
        isCategory = (TextView) itemLayoutView.findViewById(R.id.text_isCategory);
        description = (TextView) itemLayoutView.findViewById(R.id.text_description);
        openAsNotCat = (TextView) itemLayoutView.findViewById(R.id.text_openAsNotCat);

        // set item & inner view click listener
        itemLayoutView.setOnClickListener(this);
        openAsNotCat.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        HelperUtil.generalDebug("ShowBoardsFragment", "onClick " + v.toString());
        if (v instanceof TextView) {
            // click on the text view, force show topics
            HelperUtil.generalDebug("ShowBoardsFragment", "Force open as board: " + data_boardId + ", " + data_isCat);
            ActivityUtil.openShowTopicsActivity(v.getContext(), data_boardId, 0, false);
        } else if (v instanceof RelativeLayout) {
            // click on whole item, starting new activity
            HelperUtil.generalDebug("ShowBoardsFragment", "Clicked: " + data_boardId + ", " + data_isCat);
            if (data_isCat) {
                // clicked board is a category, start ShowBoardsActivity
                ActivityUtil.openShowBoardsActivity(v.getContext(), data_boardId, 0, false);
            } else {
                // clicked board has no sub-boards, start ShowTopicsActivity
                ActivityUtil.openShowTopicsActivity(v.getContext(), data_boardId, 0, false);
            }
        }
    }
}
