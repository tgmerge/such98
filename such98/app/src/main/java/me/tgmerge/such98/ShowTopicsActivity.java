package me.tgmerge.such98;

import android.app.Activity;
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
import android.widget.TextView;

import org.apache.http.Header;


/* Intent params:
 * id - Board ID to be shown
 *      value:
 *          ID_NEW(-1)    - show new topics
 *          ID_HOT(-2)    - how hot topics
 *          others(id)    - show specified board's topic
 * startPos - initial position for paging, default=0
 *      value:
 *          int           - start position
 */
public class ShowTopicsActivity extends ActionBarActivity {

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_show_topics, menu);
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
    public final static int ID_NEW = -1;
    public final static int ID_HOT = -2;

    public final static String INTENT_STARTPOS = "startPos";
    public final static int ITEM_PER_PAGE = 10;

    public final static String INTENT_TITLE = "title";

    private boolean isLoading = false;
    private boolean isNoMoreItem = false;

    private int lastLoadStartPos = -1;

    private RecyclerView recyclerView = null;
    private LinearLayoutManager layoutManager = null;
    private ShowTopicsAdapter adapter = null;

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
        final int intentType = intent.getIntExtra(INTENT_ID, 0);

        int intentStartPos = intent.getIntExtra(INTENT_STARTPOS, 0);
        final int pageStart = (this.lastLoadStartPos < 0) ? intentStartPos: this.lastLoadStartPos + ITEM_PER_PAGE;
        final int pageSize = ITEM_PER_PAGE;

        String title = intent.getStringExtra(INTENT_TITLE);
        if (title == null) {
            title = "Topics id=" + intentType;
        }
        setTitle(title);

        final Activity that = this;

        class Callback implements APIUtil.APICallback {
            @Override
            public void onSuccess(int statCode, Header[] headers, byte[] body) {
                String s = new String(body);

                XMLUtil.ArrayOf<?> topicInfoArr;

                if (intentType == ID_HOT) {
                    // Hot topics
                    topicInfoArr = new XMLUtil.ArrayOf<XMLUtil.HotTopicInfo>(XMLUtil.HotTopicInfo.class);
                } else {
                    // Other topics
                    topicInfoArr = new XMLUtil.ArrayOf<XMLUtil.TopicInfo>(XMLUtil.TopicInfo.class);
                }

                try {
                    topicInfoArr.parse(s);
                } catch (Exception e) {
                    HelperUtil.errorToast(that, "Parse exception:" + e.toString());
                    e.printStackTrace();
                }
                if (topicInfoArr.size() == 0) {
                    // no more loading - -
                    isNoMoreItem = true;
                    HelperUtil.debugToast(that, "Already at last page");
                } else {
                    // append data
                    adapter.appendData(topicInfoArr);
                }
                lastLoadStartPos = pageStart;
                setProgressFinished();
            }

            @Override
            public void onFailure(int statCode, Header[] headers, byte[] body, Throwable error) {
                HelperUtil.errorToast(that, "Network failed, code=" + statCode + ", info=" + (body == null ? "null" : new String(body)));
            }
        }

        if (intentType == ID_NEW) {

            // Show new topics
            new APIUtil.GetNewTopic(this, pageStart, null, pageSize, new Callback()).execute();

        } else if (intentType == ID_HOT) {

            // show hot topics
            new APIUtil.GetHotTopic(this, pageStart, null, pageSize, new Callback()).execute();

        } else {

            // show topics by board id
            new APIUtil.GetBoardTopic(this, intentType, pageStart, null, pageSize, new Callback()).execute();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_topics);

        // config RecyclerView
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        // config RV's LayoutManager
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        // config RecyclerView's Adapter
        adapter = new ShowTopicsAdapter(null);
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


    private static class ShowTopicsAdapter extends RecyclerView.Adapter<ShowTopicsAdapter.ViewHolder> {

        private XMLUtil.ArrayOf<? extends XMLUtil.XMLObj> mData;
        private Class mDataItemClass;

        public final void setData(XMLUtil.ArrayOf<? extends XMLUtil.XMLObj> data) {
            if (data == null) {
                mDataItemClass = null;
                mData = data;
                notifyDataSetChanged();
                return;
            }

            Class itemClass = data.getItemClass();
            if (itemClass != XMLUtil.HotTopicInfo.class && itemClass != XMLUtil.TopicInfo.class) {
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

            if (mDataItemClass == XMLUtil.HotTopicInfo.class) {
                ((XMLUtil.ArrayOf<XMLUtil.HotTopicInfo>) mData).append((XMLUtil.ArrayOf<XMLUtil.HotTopicInfo>) data);
            } else {
                ((XMLUtil.ArrayOf<XMLUtil.TopicInfo>) mData).append((XMLUtil.ArrayOf<XMLUtil.TopicInfo>) data);
            }

            notifyDataSetChanged();
        }

        public ShowTopicsAdapter(XMLUtil.ArrayOf<? extends XMLUtil.XMLObj> data) {
            if (data == null) {
                return;
            }

            Class itemClass = data.getItemClass();
            if (itemClass != XMLUtil.HotTopicInfo.class && itemClass != XMLUtil.TopicInfo.class) {
                return;
            }
            mDataItemClass = itemClass;
            mData = data;
        }


        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View itemLayoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_topic_card, parent, false);
            return new ViewHolder(itemLayoutView);
        }


        @Override
        public void onBindViewHolder(ViewHolder viewHolder, int position) {
            if (mDataItemClass == XMLUtil.TopicInfo.class) {
                XMLUtil.ArrayOf<XMLUtil.TopicInfo> thisData = (XMLUtil.ArrayOf<XMLUtil.TopicInfo>) mData;
                XMLUtil.TopicInfo dataItem = thisData.get(position);
                viewHolder.title.setText(dataItem.Title);
                viewHolder.authorInfo.setText(dataItem.AuthorName);
                viewHolder.lastPostInfo.setText(dataItem.LastPostInfo.UserName);
            } else {
                XMLUtil.ArrayOf<XMLUtil.HotTopicInfo> thisData = (XMLUtil.ArrayOf<XMLUtil.HotTopicInfo>) mData;
                XMLUtil.HotTopicInfo dataItem = thisData.get(position);
                viewHolder.title.setText(dataItem.Title);
                viewHolder.authorInfo.setText(dataItem.AuthorName);
                viewHolder.lastPostInfo.setText(dataItem.BoardName + ", " + dataItem.ParticipantCount + "人参与");
            }
        }

        @Override
        public int getItemCount() {
            return (mData == null) ? 0 : mData.size();
        }


        public static class ViewHolder extends RecyclerView.ViewHolder {

            public TextView title;
            public TextView authorInfo;
            public TextView lastPostInfo;

            public ViewHolder(View itemLayoutView) {
                super(itemLayoutView);
                title = (TextView) itemLayoutView.findViewById(R.id.text_title);
                authorInfo = (TextView) itemLayoutView.findViewById(R.id.text_authorInfo);
                lastPostInfo = (TextView) itemLayoutView.findViewById(R.id.text_lastPostInfo);
            }
        }
    }
}
