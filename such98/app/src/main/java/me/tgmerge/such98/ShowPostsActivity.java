package me.tgmerge.such98;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import org.apache.http.Header;
import me.tgmerge.such98.Util.APIUtil;
import me.tgmerge.such98.Util.BBUtil;
import me.tgmerge.such98.Util.CacheUtil;
import me.tgmerge.such98.Util.HelperUtil;
import me.tgmerge.such98.Util.ImageUtil;
import me.tgmerge.such98.Util.XMLUtil;


public class ShowPostsActivity extends BaseDrawerActivity {

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
    public final static String INTENT_KEY_START_POS = "startPos";
    public final static int ITEM_PER_PAGE = 10;

    private boolean isLoading = false;
    private boolean isNoMoreItem = false;

    private int lastLoadStartPos = -1;

    // Adapter 使用这两个值的时候，必然已经被设置过了
    private int mIntentId;
    private final XMLUtil.TopicInfo mTopicInfo = new XMLUtil.TopicInfo();

    private final Activity that = this;

    private final void setProgressLoading() {
        isLoading = true;
        findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
    }

    private final void setProgressFinished() {
        findViewById(R.id.progressBar).setVisibility(View.GONE);
        isLoading = false;
    }

    private final void loadAPage(final ShowPostsAdapter adapter) {
        setProgressLoading();

        Intent intent = getIntent();
        int intentId = intent.getIntExtra(INTENT_KEY_ID, 0);
        mIntentId = intentId;
        int intentStartPos = intent.getIntExtra(INTENT_KEY_START_POS, 0);

        final int pageStart = (this.lastLoadStartPos < 0) ? intentStartPos : this.lastLoadStartPos + ITEM_PER_PAGE;

        class Callback implements APIUtil.APICallback {
            @Override
            public void onSuccess(int statCode, Header[] headers, byte[] body) {
                XMLUtil.ArrayOf<XMLUtil.PostInfo> postInfoArr = new XMLUtil.ArrayOf<>(XMLUtil.PostInfo.class);
                try {
                    postInfoArr.parse(new String(body));
                } catch (Exception e) {
                    e.printStackTrace();
                    onFailure(-1, headers, body, e);
                }

                if (postInfoArr.size() == 0) {
                    isNoMoreItem = true;
                    HelperUtil.debugToast(that, "End of list");
                } else {
                    adapter.appendData(postInfoArr);
                    lastLoadStartPos = pageStart;
                }
                setProgressFinished();
            }

            @Override
            public void onFailure(int statCode, Header[] headers, byte[] body, Throwable error) {
                setProgressFinished();
                HelperUtil.errorToast(that, "Error, code=" + statCode + ", error=" + error.toString());            }
        }

        new APIUtil.GetTopicPost(this, intentId, pageStart, null, ITEM_PER_PAGE, new Callback()).execute();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_show_posts);
        getLayoutInflater().inflate(R.layout.activity_show_posts, frameLayout);

        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        final ShowPostsAdapter adapter = new ShowPostsAdapter(null);
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
                        loadAPage(adapter);
                    }
                }
            }
        });

        recyclerView.setItemAnimator(new DefaultItemAnimator());

        int intentTopicId = getIntent().getIntExtra(INTENT_KEY_ID, 0);

        new APIUtil.GetTopic(this, intentTopicId, new APIUtil.APICallback() {
            @Override
            public void onSuccess(int statCode, Header[] headers, byte[] body) {
                try {
                    mTopicInfo.parse(new String(body));
                    loadAPage(adapter);
                } catch (Exception e) {
                    e.printStackTrace();
                    onFailure(-1, headers, body, e);
                }
                setTitle(mTopicInfo.Title);
            }

            @Override
            public void onFailure(int statCode, Header[] headers, byte[] body, Throwable error) {
                HelperUtil.errorToast(that, "Error: " + "code=" + statCode + ", error=" + error.toString());
            }
        }).execute();
    }


    private class ShowPostsAdapter extends RecyclerView.Adapter<ViewHolder> {

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
        public void onBindViewHolder(final ViewHolder viewHolder, int position) {
            final XMLUtil.PostInfo dataItem = mData.get(position);

            viewHolder.isCreated = 1;

            viewHolder.title.setText(dataItem.Floor != 1 && dataItem.Title.length() == 0 ? "回复 #" + dataItem.Floor : dataItem.Title);
            viewHolder.authorInfo.setText(dataItem.UserName + " @ " + dataItem.Time);
            BBUtil.setBBcodeToTextView(that, viewHolder.content, dataItem.Content); // todo prevent to process every time

            if (dataItem.Floor == 1) {
                viewHolder.replyInfo.setVisibility(View.VISIBLE);
                viewHolder.replyInfo.setText(mTopicInfo.HitCount + " 次点击, " + mTopicInfo.ReplyCount + " 次回复");
            } else {
                viewHolder.replyInfo.setVisibility(View.GONE);
            }

            // ViewHolder 异步加载图像： 加载之前设置viewHolder.setRecyclable(false)
            //                         加载之后设置viewHolder.setRecyclable(true)
            String avaUrl = CacheUtil.id_avaUrlCache.get(dataItem.UserId);
            if (avaUrl != null) {
                ImageUtil.setViewHolderImage(that, viewHolder, viewHolder.avatar, avaUrl);
            } else {
                viewHolder.setIsRecyclable(false); // todo pair with (true)
                new APIUtil.GetIdUser(that, dataItem.UserId, new APIUtil.APICallback() {
                    @Override
                    public void onSuccess(int statCode, Header[] headers, byte[] body) {
                        XMLUtil.UserInfo info = new XMLUtil.UserInfo();
                        try {
                            info.parse(new String(body));
                        } catch (Exception e) {
                            e.printStackTrace();
                            onFailure(-1, headers, body, e);
                            return;
                        }
                        String newAvaUrl = info.PortraitUrl.startsWith("http") ? info.PortraitUrl : ("http://www.cc98.org/" + info.PortraitUrl);
                        CacheUtil.id_avaUrlCache.put(info.Id, newAvaUrl);
                        ImageUtil.setViewHolderImage(that, viewHolder, viewHolder.avatar, newAvaUrl);
                        viewHolder.setIsRecyclable(true);
                    }

                    @Override
                    public void onFailure(int statCode, Header[] headers, byte[] body, Throwable error) {
                        viewHolder.avatar.setImageResource(R.drawable.ic_close_white_36dp);
                        viewHolder.setIsRecyclable(true);
                    }
                }).execute();
            }
        }

        @Override
        public int getItemCount() {
            return (mData == null) ? 0 : mData.size();
        }
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView avatar;

        public TextView title;
        public TextView authorInfo;
        public TextView replyInfo;
        public TextView content;

        public int isCreated = 0;

        public ViewHolder(View itemLayoutView) {
            super(itemLayoutView);
            avatar = (ImageView) itemLayoutView.findViewById(R.id.image_icon);
            title = (TextView) itemLayoutView.findViewById(R.id.text_title);
            authorInfo = (TextView) itemLayoutView.findViewById(R.id.text_authorInfo);
            replyInfo = (TextView) itemLayoutView.findViewById(R.id.text_replyInfo);
            content = (TextView) itemLayoutView.findViewById(R.id.text_content);
        }
    }
}