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
import android.widget.TextView;

import org.apache.http.Header;

import me.tgmerge.such98.Util.APIUtil;
import me.tgmerge.such98.Util.HelperUtil;
import me.tgmerge.such98.Util.XMLUtil;


public class ShowPostsActivity extends ActionBarActivity {

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_show_posts, menu);
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
    public final static String INTENT_KEY_STARTPOS = "startPos";
    public final static int ITEM_PER_PAGE = 10;
    public final static String INTENT_KEY_TITLE = "title";

    private boolean isLoading = false;
    private boolean isNoMoreItem = false;

    private int lastLoadStartPos = -1;

    private RecyclerView recyclerView = null;
    private LinearLayoutManager layoutManager = null;
    private ShowPostsAdapter adapter = null;

    private final Activity that = this;

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
        final int intentId = intent.getIntExtra(INTENT_KEY_ID, 0);
        int intentStartPos = intent.getIntExtra(INTENT_KEY_STARTPOS, 0);

        final int pageStart = (this.lastLoadStartPos < 0) ? intentStartPos : this.lastLoadStartPos + ITEM_PER_PAGE;

        String title = intent.getStringExtra(INTENT_KEY_TITLE);
        if (title == null) {
            title = "Topic id=" + intentId;
        }
        setTitle(title);

        class Callback implements APIUtil.APICallback {
            @Override
            public void onSuccess(int statCode, Header[] headers, byte[] body) {
                XMLUtil.ArrayOf<XMLUtil.PostInfo> postInfoArr = new XMLUtil.ArrayOf<>(XMLUtil.PostInfo.class);

                try {
                    postInfoArr.parse(new String(body));
                } catch (Exception e) {
                    HelperUtil.errorToast(that, "Parse exception:" + e.toString());
                    e.printStackTrace();
                }

                if (postInfoArr.size() == 0) {
                    isNoMoreItem = true;
                    HelperUtil.debugToast(that, "Already at last page");
                } else {
                    adapter.appendData(postInfoArr);
                    lastLoadStartPos = pageStart;
                }
                setProgressFinished();
            }

            @Override
            public void onFailure(int statCode, Header[] headers, byte[] body, Throwable error) {
                setProgressFinished();
                HelperUtil.errorToast(that, "Network failed, code=" + statCode + ", info=" + (body == null ? "null" : new String(body)));
            }
        }

        new APIUtil.GetTopicPost(this, intentId, pageStart, null, ITEM_PER_PAGE, new Callback()).execute();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_posts);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new ShowPostsAdapter(null);
        recyclerView.setAdapter(adapter);

        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView view, int dx, int dy) {
                if (!isLoading && !isNoMoreItem) {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int pastVisibleItems = layoutManager.findFirstVisibleItemPosition();
                    if ((visibleItemCount + pastVisibleItems) >= totalItemCount) {
                        HelperUtil.debugToast(that, "Loading...");
                        loadAPage();
                    }
                }
            }
        });

        recyclerView.setItemAnimator(new DefaultItemAnimator());

        loadAPage();
    }


    private static class ShowPostsAdapter extends RecyclerView.Adapter<ShowPostsAdapter.ViewHolder> {

        private XMLUtil.ArrayOf<XMLUtil.PostInfo> mData;

        public final void appendData(XMLUtil.ArrayOf<XMLUtil.PostInfo> data) {
            if (mData == null) {
                mData = data;
            } else {
                mData.append(data);
            }
            notifyDataSetChanged();
        }


        public ShowPostsAdapter(XMLUtil.ArrayOf<XMLUtil.PostInfo> data) {
            mData = data;
        }


        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemLayoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post_card, parent, false);
            return new ViewHolder(itemLayoutView);
        }


        @Override
        public void onBindViewHolder(ViewHolder viewHolder, int position) {
            XMLUtil.PostInfo dataItem = mData.get(position);

            viewHolder.title.setText(dataItem.Title);
            viewHolder.authorInfo.setText(dataItem.UserName + " @ " + dataItem.Time);
            viewHolder.replyInfo.setText("unknown");
            viewHolder.content.setText(dataItem.Content);
        }


        @Override
        public int getItemCount() {
            return (mData == null) ? 0 : mData.size();
        }


        public static class ViewHolder extends RecyclerView.ViewHolder {

            public TextView title;
            public TextView authorInfo;
            public TextView replyInfo;
            public TextView content;

            public ViewHolder(View itemLayoutView) {
                super(itemLayoutView);
                title = (TextView) itemLayoutView.findViewById(R.id.text_title);
                authorInfo = (TextView) itemLayoutView.findViewById(R.id.text_authorInfo);
                replyInfo = (TextView) itemLayoutView.findViewById(R.id.text_replyInfo);
                content = (TextView) itemLayoutView.findViewById(R.id.text_content);
            }
        }
    }
}