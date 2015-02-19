package me.tgmerge.such98;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.apache.http.Header;

import java.util.Arrays;

import me.tgmerge.such98.Util.APIUtil;
import me.tgmerge.such98.Util.ActivityUtil;
import me.tgmerge.such98.Util.HelperUtil;
import me.tgmerge.such98.Util.XMLUtil;


/* Intent params:
 * id - Board ID to be shown, default=0
 *      value:
 *          ID_ROOT(0)    - show root board's sub-boards
 *          ID_CUSTOM(-1) - show custom boards
 *          others(id)    - show specified board's sub-boards
 * startPos - initial position for paging, default=0
 *      value:
 *          int           - start position
 */
public class ShowBoardsActivity extends BaseDrawerActivity {

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_show_boards, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public final static String INTENT_KEY_ID = "id";
    public final static int ID_ROOT = 0, ID_CUSTOM = -1;
    public final static String INTENT_KEY_START_POS = "startPos";
    public final static int ITEM_PER_PAGE = 10;

    private boolean isLoading = false;
    private boolean isNoMoreItem = false;

    private int lastLoadStartPos = -1;

    private final Activity that = this;

    private final void setProgressLoading() {
        isLoading = true;
        findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
    }

    private final void setProgressFinished() {
        findViewById(R.id.progressBar).setVisibility(View.GONE);
        isLoading = false;
    }

    private final void loadAPage(final ShowBoardsAdapter adapter) {
        // show "loading" circle
        setProgressLoading();

        // page params from intent
        Intent intent = getIntent();
        int intentId = intent.getIntExtra(INTENT_KEY_ID, 0);
        int intentStartPos = intent.getIntExtra(INTENT_KEY_START_POS, 0);

        // "pageStart" and "pageSize" params for APIUtil
        final int pageStart = (this.lastLoadStartPos < 0) ? intentStartPos : this.lastLoadStartPos + ITEM_PER_PAGE;

        // callback class for APIUtil
        class Callback implements APIUtil.APICallback {
            @Override
            public void onSuccess(int statCode, Header[] headers, byte[] body) {
                XMLUtil.ArrayOf<XMLUtil.BoardInfo> boardInfoArr = new XMLUtil.ArrayOf<>(XMLUtil.BoardInfo.class);
                try {
                    boardInfoArr.parse(new String(body));
                } catch (Exception e) {
                    e.printStackTrace();
                    onFailure(-1, headers, body, e);
                }

                if (boardInfoArr.size() == 0) {
                    // no more item...
                    isNoMoreItem = true;
                    HelperUtil.debugToast(that, "End of list");
                } else {
                    // some item arrived
                    adapter.appendData(boardInfoArr);
                    lastLoadStartPos = pageStart;
                }
                setProgressFinished();
            }

            @Override
            public void onFailure(int statCode, Header[] headers, byte[] body, Throwable error) {
                setProgressFinished();
                HelperUtil.errorToast(that, "Error, code=" + statCode + ", error=" + error.toString());
            }
        }

        if (intentId == ID_ROOT) {
            // Show root board
            new APIUtil.GetRootBoard(this, pageStart, null, ITEM_PER_PAGE, new Callback()).execute();

        } else if (intentId == ID_CUSTOM) {
            // Show custom boards
            new APIUtil.GetCustomBoardsMe(this, new APIUtil.APICallback() {
                @Override
                public void onSuccess(int statCode, Header[] headers, byte[] body) {
                    XMLUtil.ArrayOfint boardIdVec = new XMLUtil.ArrayOfint();
                    try {
                        boardIdVec.parse(new String(body));
                    } catch (Exception e) {
                        e.printStackTrace();
                        onFailure(-1, headers, body, e);
                    }

                    int[] ids = new int[boardIdVec.values.size()];
                    for (int i = 0; i < boardIdVec.values.size(); i++) {
                        ids[i] = boardIdVec.values.get(i);
                    }
                    int[] rangedIds = Arrays.copyOfRange(ids, pageStart, pageStart + ITEM_PER_PAGE);

                    new APIUtil.GetMultiBoards(that, rangedIds, pageStart, null, ITEM_PER_PAGE, new Callback()).execute();
                }

                @Override
                public void onFailure(int statCode, Header[] headers, byte[] body, Throwable error) {
                    setProgressFinished();
                    HelperUtil.errorToast(that, "Error, code=" + statCode + ", error=" + error.toString());
                }
            }).execute();

        } else {
            // Show board by id
            new APIUtil.GetSubBoards(this, intentId, pageStart, null, ITEM_PER_PAGE, new Callback()).execute();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_show_boards, frameLayout);
        //setContentView(R.layout.activity_show_boards);

        // RecyclerView
        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        // RecyclerView's LayoutManager
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        // RecyclerView's Adapter
        final ShowBoardsAdapter adapter = new ShowBoardsAdapter(null);
        recyclerView.setAdapter(adapter);

        // RecyclerView's OnScrollListener
        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView view, int dx, int dy) {
                if (!isLoading && !isNoMoreItem) {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int pastVisibleItems = layoutManager.findFirstVisibleItemPosition();
                    if ((visibleItemCount + pastVisibleItems) >= totalItemCount) {
                        // scroll to bottom, has more item, not loading - we're going to load another page
                        HelperUtil.debugToast(that, "Loading more...");
                        loadAPage(adapter);
                    }
                }
            }
        });

        // RecyclerView's ItemAnimator
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        // get boardInfo XML of intentId
        int intentBoardId = getIntent().getIntExtra(INTENT_KEY_ID, 0);
        final XMLUtil.BoardInfo boardInfo = new XMLUtil.BoardInfo();

        if (intentBoardId == ID_CUSTOM) {
            boardInfo.Id = ID_CUSTOM;
            boardInfo.ChildBoardCount = 10;
            boardInfo.Name = "个人定制";
            setTitle(boardInfo.Name);
            loadAPage(adapter);
        } else if (intentBoardId == ID_ROOT) {
            boardInfo.Id = ID_ROOT;
            boardInfo.ChildBoardCount = -1;
            boardInfo.Name = "主版面";
            setTitle(boardInfo.Name);
            loadAPage(adapter);
        } else {
            new APIUtil.GetBoard(this, intentBoardId, new APIUtil.APICallback() {
                @Override
                public void onSuccess(int statCode, Header[] headers, byte[] body) {
                    try {
                        boardInfo.parse(new String(body));
                    } catch (Exception e) {
                        e.printStackTrace();
                        onFailure(-1, headers, body, e);
                    }
                    setTitle(boardInfo.Name);

                    // load first page
                    loadAPage(adapter);
                }

                @Override
                public void onFailure(int statCode, Header[] headers, byte[] body, Throwable error) {
                    HelperUtil.errorToast(that, "Error: " + "code=" + statCode + ", error=" + error.toString());
                }
            }).execute();
        }
    }


    private static class ShowBoardsAdapter extends RecyclerView.Adapter<ViewHolder> {

        private XMLUtil.ArrayOf<XMLUtil.BoardInfo> mData;

        public final void appendData(XMLUtil.ArrayOf<XMLUtil.BoardInfo> data) {
            if (mData == null) {
                mData = data;
            } else {
                mData.append(data);
            }
            notifyDataSetChanged();
        }

        public ShowBoardsAdapter(XMLUtil.ArrayOf<XMLUtil.BoardInfo> data) {
            mData = data;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemLayoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_board_card, parent, false);
            return new ViewHolder(itemLayoutView);
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, int position) {
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


    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public ImageView icon;
        public TextView name;
        public TextView isCategory;
        public TextView description;
        public TextView openAsNotCat;

        public int data_boardId;
        public boolean data_isCat;

        public ViewHolder(View itemLayoutView) {
            super(itemLayoutView);
            icon = (ImageView) itemLayoutView.findViewById(R.id.image_icon);
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
            HelperUtil.generalDebug("ShowBoardsActivity", "onClick " + v.toString());
            if (v instanceof TextView) {
                // click on the text view, force show topics
                HelperUtil.generalDebug("ShowBoardsActivity", "Force open as board: " + data_boardId + ", " + data_isCat);
                ActivityUtil.openShowTopicsActivity(v.getContext(), data_boardId, 0, false);
            } else if (v instanceof RelativeLayout) {
                // click on whole item, starting new activity
                HelperUtil.generalDebug("ShowBoardsActivity", "Clicked: " + data_boardId + ", " + data_isCat);
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
}
