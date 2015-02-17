package me.tgmerge.such98;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
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

import me.tgmerge.such98.Util.APIUtil;
import me.tgmerge.such98.Util.ActivityUtil;
import me.tgmerge.such98.Util.HelperUtil;
import me.tgmerge.such98.Util.XMLUtil;


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
public class ShowTopicsActivity extends BaseDrawerActivity {

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


    public final static String INTENT_KEY_ID = "id";
    public final static int ID_NEW = -1, ID_HOT = -2;
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

    private final void loadAPage(final ShowTopicsAdapter adapter) {
        setProgressLoading();

        Intent intent = getIntent();
        final int intentId = intent.getIntExtra(INTENT_KEY_ID, 0);
        int intentStartPos = intent.getIntExtra(INTENT_KEY_START_POS, 0);

        final int pageStart = (this.lastLoadStartPos < 0) ? intentStartPos : this.lastLoadStartPos + ITEM_PER_PAGE;

        class Callback implements APIUtil.APICallback {
            @Override
            public void onSuccess(int statCode, Header[] headers, byte[] body) {
                XMLUtil.ArrayOf<?> topicInfoArr;

                if (intentId == ID_HOT) {
                    // Hot topics
                    topicInfoArr = new XMLUtil.ArrayOf<XMLUtil.HotTopicInfo>(XMLUtil.HotTopicInfo.class);
                } else {
                    // Other topics
                    topicInfoArr = new XMLUtil.ArrayOf<XMLUtil.TopicInfo>(XMLUtil.TopicInfo.class);
                }

                try {
                    topicInfoArr.parse(new String(body));
                } catch (Exception e) {
                    e.printStackTrace();
                    onFailure(-1, headers, body, e);
                }

                if (topicInfoArr.size() == 0) {
                    isNoMoreItem = true;
                    HelperUtil.debugToast(that, "Already at last page");
                } else {
                    adapter.appendData(topicInfoArr);
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

        if (intentId == ID_NEW) {
            // Show new topics
            new APIUtil.GetNewTopic(this, pageStart, null, ITEM_PER_PAGE, new Callback()).execute();

        } else if (intentId == ID_HOT) {
            // show hot topics
            new APIUtil.GetHotTopic(this, pageStart, null, ITEM_PER_PAGE, new Callback()).execute();

        } else {
            // show topics by board id
            new APIUtil.GetBoardTopic(this, intentId, pageStart, null, ITEM_PER_PAGE, new Callback()).execute();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_show_topics);
        getLayoutInflater().inflate(R.layout.activity_show_topics, frameLayout);


        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        final ShowTopicsAdapter adapter = new ShowTopicsAdapter(null);
        recyclerView.setAdapter(adapter);

        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView view, int dx, int dy) {
                if (!isLoading && !isNoMoreItem) {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int pastVisibleItems = layoutManager.findFirstVisibleItemPosition();
                    if ((visibleItemCount + pastVisibleItems) >= totalItemCount) {
                        HelperUtil.debugToast(that, "Loading more...");
                        loadAPage(adapter);
                    }
                }
            }
        });

        recyclerView.setItemAnimator(new DefaultItemAnimator());

        int intentBoardId = getIntent().getIntExtra(INTENT_KEY_ID, 0);
        final XMLUtil.BoardInfo boardInfo = new XMLUtil.BoardInfo();

        if (intentBoardId == ID_HOT) {
            boardInfo.Id = ID_HOT;
            boardInfo.Name = "热门主题";
            setTitle(boardInfo.Name);
            loadAPage(adapter);
        } else if (intentBoardId == ID_NEW) {
            boardInfo.Id = ID_NEW;
            boardInfo.Name = "最新主题";
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
                    loadAPage(adapter);
                }

                @Override
                public void onFailure(int statCode, Header[] headers, byte[] body, Throwable error) {
                    HelperUtil.errorToast(that, "Error: " + "code=" + statCode + ", error=" + error.toString());
                }
            }).execute();
        }
    }


    private class ShowTopicsAdapter extends RecyclerView.Adapter<ViewHolder> {

        private XMLUtil.ArrayOf<? extends XMLUtil.XMLObj> mData;
        private Class mDataItemClass;

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

            if (mDataItemClass == XMLUtil.HotTopicInfo.class) {
                ((XMLUtil.ArrayOf<XMLUtil.HotTopicInfo>) mData).append((XMLUtil.ArrayOf<XMLUtil.HotTopicInfo>) data);
            } else {
                ((XMLUtil.ArrayOf<XMLUtil.TopicInfo>) mData).append((XMLUtil.ArrayOf<XMLUtil.TopicInfo>) data);
            }

            notifyDataSetChanged();
        }

        public ShowTopicsAdapter(XMLUtil.ArrayOf<? extends XMLUtil.XMLObj> data) {
            Class itemClass = (data == null) ? null : data.getItemClass();
            if (itemClass != null && itemClass != XMLUtil.HotTopicInfo.class && itemClass != XMLUtil.TopicInfo.class) {
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

                viewHolder.icon.setImageResource(dataItem.TopState.equals(XMLUtil.TopicInfo.TOPSTATE_NONE)
                                                 ? R.drawable.ic_comment_text_outline_white_36dp
                                                 : R.drawable.ic_comment_alert_outline_white_36dp);

                viewHolder.title.setText(dataItem.Title);
                viewHolder.authorInfo.setText(dataItem.AuthorName + " @ " + dataItem.CreateTime);
                viewHolder.lastPostInfo.setText(dataItem.LastPostInfo.UserName + " - " + dataItem.LastPostInfo.ContentSummary);

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

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public ImageView icon;
        public TextView title;
        public TextView authorInfo;
        public TextView lastPostInfo;

        public int data_topicId;

        public ViewHolder(View itemLayoutView) {
            super(itemLayoutView);
            icon = (ImageView) itemLayoutView.findViewById(R.id.image_icon);
            title = (TextView) itemLayoutView.findViewById(R.id.text_title);
            authorInfo = (TextView) itemLayoutView.findViewById(R.id.text_authorInfo);
            lastPostInfo = (TextView) itemLayoutView.findViewById(R.id.text_lastPostInfo);

            itemLayoutView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            HelperUtil.generalDebug("ShowTopicsActivity", "onClick! " + v.toString());
            if (v instanceof RelativeLayout) {
                // click on whole item
                HelperUtil.generalDebug("ShowTopicsActivity", "Clicked: " + data_topicId);
                ActivityUtil.openShowPostsActivity(v.getContext(), data_topicId, 0);
            }
        }
    }
}
