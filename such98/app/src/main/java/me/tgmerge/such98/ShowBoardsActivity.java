package me.tgmerge.such98;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import org.apache.http.Header;
import java.util.Arrays;


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
public class ShowBoardsActivity extends ActionBarActivity {

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


    public final static String INTENT_ID = "id";
    public final static int ID_ROOT = 0;
    public final static int ID_CUSTOM = -1;

    public final static String INTENT_STARTPOS = "startPos";
    public final static int ITEM_PER_PAGE = 10;

    private boolean isLoading = false;
    private boolean isNoMoreItem = false;

    private int lastLoadStartPos = -1;

    private RecyclerView recyclerView = null;
    private LinearLayoutManager layoutManager = null;
    private ShowBoardsAdapter adapter = null;

    final Activity that = this;

    private final void setProgressLoading() {
        isLoading = true;
        findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
    }

    private final void setProgressFinished() {
        findViewById(R.id.progressBar).setVisibility(View.GONE);
        isLoading = false;
    }

    private final void loadAPage() {

        setProgressLoading();

        Intent intent = getIntent();
        int intentType = intent.getIntExtra(INTENT_ID, 0);

        int intentStartPos = intent.getIntExtra(INTENT_STARTPOS, 0);
        final int pageStart = (this.lastLoadStartPos < 0) ? intentStartPos : this.lastLoadStartPos + ITEM_PER_PAGE;
        final int pageSize = ITEM_PER_PAGE;

        final Activity that = this;

        class Callback implements APIUtil.APICallback {
            @Override
            public void onSuccess(int statCode, Header[] headers, byte[] body) {
                String s = new String(body);
                XMLUtil.ArrayOf<XMLUtil.BoardInfo> boardInfoArr = new XMLUtil.ArrayOf<>(XMLUtil.BoardInfo.class);
                try {
                    boardInfoArr.parse(s);
                } catch (Exception e) {
                    HelperUtil.errorToast(that, "Parse exception:" + e.toString());
                    e.printStackTrace();
                }
                if (boardInfoArr.size() == 0) {
                    // no more loading!
                    isNoMoreItem = true;
                    HelperUtil.debugToast(that, "Already at last page");
                } else {
                    // append data
                    adapter.appendData(boardInfoArr);
                }
                lastLoadStartPos = pageStart;
                setProgressFinished();
            }

            @Override
            public void onFailure(int statCode, Header[] headers, byte[] body, Throwable error) {
                HelperUtil.errorToast(that, "Network failed, code=" + statCode + ", info=" + (body == null ? "null" : new String(body)));
            }
        }

        if (intentType == ID_ROOT) {

            // Show root board
            setTitle("Root Board @" + pageStart);
            new APIUtil.GetRootBoard(this, pageStart, null, pageSize, new Callback()).execute();

        } else if (intentType == ID_CUSTOM) {

            // Show custom boards
            setTitle("Custom Boards @" + pageStart);
            new APIUtil.GetCustomBoardsMe(this, new APIUtil.APICallback() {
                @Override
                public void onSuccess(int statCode, Header[] headers, byte[] body) {
                    String s = new String(body);
                    XMLUtil.ArrayOfint boardIdVec = new XMLUtil.ArrayOfint();
                    try {
                        boardIdVec.parse(s);
                    } catch (Exception e) {
                        HelperUtil.errorToast(that, "GetCustomBoardsMe parse exception:" + e.toString());
                        e.printStackTrace();
                    }

                    int[] ids = new int[boardIdVec.values.size()];
                    for (int i = 0; i < boardIdVec.values.size(); i ++) {
                        ids[i] = boardIdVec.values.get(i);
                    }

                    int[] rangedIds = Arrays.copyOfRange(ids, pageStart, pageStart + pageSize);

                    new APIUtil.GetMultiBoards(that, rangedIds, pageStart, null, pageSize, new Callback()).execute();
                }

                @Override
                public void onFailure(int statCode, Header[] headers, byte[] body, Throwable error) {
                    HelperUtil.errorToast(that, "GetCustomBoardsMe failed, code=" + statCode + ", info=" + new String(body));
                }
            }).execute();

        } else {

            // Show board by id
            setTitle("Board #" + intentType + " @" + pageStart);
            new APIUtil.GetSubBoards(this, intentType, pageStart, null, pageSize, new Callback()).execute();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_boards);

        // config RecyclerView
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        // config RecyclerView's LayoutManager
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        // config RecyclerView's Adapter
        adapter = new ShowBoardsAdapter(null);
        recyclerView.setAdapter(adapter);

        // config RecyclerView's OnScrollListener
        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView view, int dx, int dy) {

                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int pastVisibleItems = layoutManager.findFirstVisibleItemPosition();

                if ((visibleItemCount+pastVisibleItems) >= totalItemCount && !isLoading && !isNoMoreItem) {
                    // scroll to bottom, has more item, not loading - we're going to load another page
                    HelperUtil.debugToast(that, "Loading...");
                    loadAPage();
                }
            }

        });

        // config RecyclerView's ItemAnimator
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        loadAPage();
    }


    private static class ShowBoardsAdapter extends RecyclerView.Adapter<ShowBoardsAdapter.ViewHolder> {

        private XMLUtil.ArrayOf<XMLUtil.BoardInfo> mData;

        public final void setData(XMLUtil.ArrayOf<XMLUtil.BoardInfo> data) {
            mData = data;
            notifyDataSetChanged();
        }

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


        // Create new views, invoked by the layout manager
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            // create a new view
            View itemLayoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_board_card, parent, false);
            return new ViewHolder(itemLayoutView);
        }


        // Replace the contents of a view, invoked by the layout manager
        @Override
        public void onBindViewHolder(ViewHolder viewHolder, int position) {

            XMLUtil.BoardInfo dataItem = mData.get(position);

            viewHolder.name.setText(dataItem.Name);
            viewHolder.isCategory.setText(dataItem.IsCategory ? "分类" : "");
            viewHolder.description.setText(dataItem.Description);

            viewHolder.data_boardId = dataItem.Id;
            viewHolder.data_isCat = dataItem.IsCategory;
            viewHolder.data_boardName = dataItem.Name;
        }


        // Return the size of items data, invoked by the layout manager
        @Override
        public int getItemCount() {
            return (mData == null) ? 0 : mData.size();
        }


        // inner class to hold a reference to each item of RecyclerView
        public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            public TextView name;
            public TextView isCategory;
            public TextView description;
            public int data_boardId;
            public boolean data_isCat;
            public String data_boardName;

            public ViewHolder(View itemLayoutView) {
                super(itemLayoutView);
                name = (TextView) itemLayoutView.findViewById(R.id.text_name);
                isCategory = (TextView) itemLayoutView.findViewById(R.id.text_isCategory);
                description = (TextView) itemLayoutView.findViewById(R.id.text_description);

                itemLayoutView.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                if (v instanceof RelativeLayout) {
                    HelperUtil.generalDebug("ShowBoardsActivity", "Clicked: " + data_boardId + ", " + data_isCat + ", " + data_boardName);

                    Context ctx = v.getContext();
                    Intent intent;

                    // click on whole item, starting new activity!
                    if (data_isCat) {
                        // clicked board is a category, start ShowBoardsActivity
                        intent = new Intent(ctx, ShowBoardsActivity.class);
                        intent.putExtra(ShowBoardsActivity.INTENT_ID, data_boardId);
                    } else {
                        // clicked board has no sub-boards, start ShowTopicsActivity
                        intent = new Intent(ctx, ShowTopicsActivity.class);
                        intent.putExtra(ShowTopicsActivity.INTENT_ID, data_boardId);
                    }

                    ctx.startActivity(intent);
                }
            }
        }
    }
}
