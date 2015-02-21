package me.tgmerge.such98.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.apache.http.Header;

import me.tgmerge.such98.R;
import me.tgmerge.such98.util.APIUtil;
import me.tgmerge.such98.util.ActivityUtil;
import me.tgmerge.such98.util.BBUtil;
import me.tgmerge.such98.util.CacheUtil;
import me.tgmerge.such98.util.HelperUtil;
import me.tgmerge.such98.util.ImageUtil;
import me.tgmerge.such98.util.XMLUtil;


public class PostsFragment extends Fragment {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM_ID = "id";
    private static final String ARG_PARAM_POS = "pos";

    public static final int PARAM_POS_BEGINNING = 0;
    public static final int PARAM_POS_END = -1;

    private int mParamId = 0;
    private int mParamPos = 0;

    private final XMLUtil.TopicInfo mTopicInfo = new XMLUtil.TopicInfo();

    // root view of this fragment
    View thisView = null;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param paramId  The fragment will show board #paramId (ID_ROOT and ID_CUSTOM is also valid)
     * @param paramPos Items from #paramPos will be shown at the beginning
     * @return A new instance of fragment BoardsFragment.
     */
    public static PostsFragment newInstance(int paramId, int paramPos) {
        PostsFragment fragment = new PostsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PARAM_ID, paramId);
        args.putInt(ARG_PARAM_POS, paramPos);
        fragment.setArguments(args);
        return fragment;
    }

    // onCreate: params will be set to member variables.
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParamId = getArguments().getInt(ARG_PARAM_ID);
            mParamPos = getArguments().getInt(ARG_PARAM_POS);
        }
    }

    // onCreateView: inflate the layout for this fragment.
    //               saving layout in thisView for further usage in class methods
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        thisView = inflater.inflate(R.layout.fragment_posts, container, false);
        return thisView;
    }

    RecyclerView mRecyclerView;
    LinearLayoutManager mLayoutManager;
    SwipeRefreshLayout mSwipeLayout;
    ShowPostsAdapter mAdapter;

    // onActivityCreated: the activity has finished its "onCreated"
    //                    this will be also called when fragment replace happens.
    // see http://segmentfault.com/blog/shiyongdanshuiyu/1190000000650573
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // config recycler view
        mRecyclerView = (RecyclerView) thisView.findViewById(R.id.recyclerView);

        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new ShowPostsAdapter(null);
        mRecyclerView.setAdapter(mAdapter);

        // on refresh listener is only used to load previous page.
        // "load next page" is triggered by recycler view's "scroll to bottom" event,
        // in which indicator of swipeLayout is shown by isRefreshing(), then "load next page"
        // is called manually.
        mSwipeLayout = (SwipeRefreshLayout) thisView.findViewById(R.id.swipe_container);
        mSwipeLayout.setEnabled(false);
        mSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadPreviousPage(mAdapter);
            }
        });

        // loading topic info...
        setProgressLoading();

        new APIUtil.GetTopic(getActivity(), mParamId, new APIUtil.APICallback() {
            @Override
            public void onSuccess(int statCode, Header[] headers, byte[] body) {
                // save topic info into fragment
                try {
                    mTopicInfo.parse(new String(body));
                } catch (Exception e) {
                    e.printStackTrace();
                    onFailure(-1, headers, body, e);
                    return;
                }
                // set title
                getActivity().setTitle(mTopicInfo.Title);

                mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {

                    @Override
                    public void onScrolled(RecyclerView view, int dx, int dy) {
                        if (mSwipeLayout.isRefreshing()) {
                            return;
                        }

                        // scrolled to top?
                        if (mHasPreviousPage) {
                            int firstChildTop = mRecyclerView.getChildAt(0).getTop();
                            int firstVisiblePos = mLayoutManager.findFirstVisibleItemPosition();
                            if (firstChildTop > 0 && firstChildTop < 20 && firstVisiblePos == 0) {
                                // scrolled to top?
                                mSwipeLayout.setEnabled(true);
                                return;
                            }
                        }

                        // scrolled to bottom?
                        if (mHasNextPage) {
                            int visibleItemCount = mLayoutManager.getChildCount();
                            int totalItemCount = mLayoutManager.getItemCount();
                            int pastVisibleItems = mLayoutManager.findFirstVisibleItemPosition();
                            if ((visibleItemCount + pastVisibleItems) >= totalItemCount) {
                                // scrolled to bottom?
                                setProgressLoading();
                                loadNextPage(mAdapter);
                                return;
                            }
                        }

                        // otherwise...
                        mSwipeLayout.setEnabled(false);
                    }
                });

                // loading topic info: finished
                setProgressFinished();


                // first load
                int maxPage = mTopicInfo.ReplyCount / ITEM_PER_PAGE;

                // set initial loading position
                if (mParamPos == PARAM_POS_BEGINNING) {
                    mPreviousPage = -1;
                    mNextPage = 0;
                    loadNextPage(mAdapter);
                } else if (mParamPos == PARAM_POS_END) {
                    mPreviousPage = maxPage;
                    mNextPage = maxPage + 1;
                    loadPreviousPage(mAdapter);
                } else {
                    mNextPage = mParamPos/ITEM_PER_PAGE;
                    mPreviousPage = mNextPage - 1;
                    loadNextPage(mAdapter);
                }
            }

            @Override
            public void onFailure(int statCode, Header[] headers, byte[] body, Throwable error) {
                HelperUtil.errorToast(getActivity(), "Error: " + "code=" + statCode + ", error=" + error.toString());
            }
        }).execute();
    }


    // - - -

    private final void setProgressLoading() {
        mSwipeLayout.setRefreshing(true);
        mSwipeLayout.setEnabled(false);
    }

    private final void setProgressFinished() {
        mSwipeLayout.setRefreshing(false);
        mSwipeLayout.setEnabled(false);
    }

    public static final int ITEM_PER_PAGE = 10;

    private int mPreviousPage = -1;
    private int mNextPage = -1;

    private boolean mHasPreviousPage = true;
    private boolean mHasNextPage = true;

    private boolean isLoaded = false;

    private final void loadNextPage(ShowPostsAdapter adapter) {
        loadPage(false, adapter);
    }

    private final void loadPreviousPage(ShowPostsAdapter adapter) {
        loadPage(true, adapter);
    }


    // loadprevious true: previous false: next
    private final void loadPage(final boolean loadPrevious, final ShowPostsAdapter adapter) {

        final int posToLoad = (loadPrevious) ? mPreviousPage*ITEM_PER_PAGE : mNextPage*ITEM_PER_PAGE;
        final int sizeToLoad = ITEM_PER_PAGE;

        if (loadPrevious && (!mHasPreviousPage || posToLoad < 0)) {
            HelperUtil.debugToast(getActivity(), "Already at first page");
            mHasPreviousPage = false;
            setProgressFinished();
            return;
        } else if (!loadPrevious && (!mHasNextPage || posToLoad > mTopicInfo.ReplyCount)) {
            HelperUtil.debugToast(getActivity(), "Already at last page");
            mHasNextPage = false;
            setProgressFinished();
            return;
        }

        class Callback implements APIUtil.APICallback {
            @Override
            public void onSuccess(int statCode, Header[] headers, byte[] body) {
                XMLUtil.ArrayOf<XMLUtil.PostInfo> postsInfo = new XMLUtil.ArrayOf<>(XMLUtil.PostInfo.class);
                try {
                    postsInfo.parse(new String(body));
                } catch (Exception e) {
                    e.printStackTrace();
                    onFailure(-1, headers, body, e);
                    return;
                }
                if (loadPrevious) {
                    adapter.appendDataFront(postsInfo);
                    mPreviousPage --;
                } else {
                    adapter.appendData(postsInfo);
                    mNextPage ++;
                }
                setProgressFinished();
            }

            @Override
            public void onFailure(int statCode, Header[] headers, byte[] body, Throwable error) {
                HelperUtil.errorToast(getActivity(), "Error, code=" + statCode + ", error=" + error.toString());
                setProgressFinished();
            }
        }

        setProgressLoading();
        HelperUtil.debugToast(getActivity(), "Loading #" + posToLoad + " - #" + (posToLoad + sizeToLoad) + "...");
        new APIUtil.GetTopicPost(getActivity(), mParamId, posToLoad, null, sizeToLoad, new Callback()).execute();
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

        public final void appendDataFront(XMLUtil.ArrayOf<XMLUtil.PostInfo> data) {
            if (mData == null) {
                mData = data;
            } else {
                mData.appendFront(data);
            }
            notifyItemRangeInserted(0, data.size());
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

            if (!isLoaded) {
                mSwipeLayout.setEnabled(true);
                isLoaded = true;
            }

            final XMLUtil.PostInfo dataItem = mData.get(position);

            viewHolder.data_topicInfo = mTopicInfo;
            viewHolder.data_postInfo = mData.get(position);

            viewHolder.title.setText(dataItem.Floor != 1 && dataItem.Title.length() == 0 ? "回复 #" + dataItem.Floor : dataItem.Title);
            viewHolder.authorInfo.setText(dataItem.UserName + " @ " + dataItem.Time);
            BBUtil.setBBcodeToTextView(viewHolder.content, dataItem.Content); // todo prevent to process every time

            if (dataItem.Floor == 1) {
                viewHolder.replyInfo.setVisibility(View.VISIBLE);
                viewHolder.replyInfo.setText(viewHolder.data_topicInfo.HitCount + " 次点击, " + viewHolder.data_topicInfo.ReplyCount + " 次回复");
            } else {
                viewHolder.replyInfo.setVisibility(View.GONE);
            }

            // ViewHolder 异步加载图像： 加载之前设置viewHolder.setRecyclable(false)
            //                         加载之后设置viewHolder.setRecyclable(true)
            String avaUrl = CacheUtil.id_avaUrlCache.get(dataItem.UserId);
            if (avaUrl != null) {
                ImageUtil.setViewHolderImage(getActivity(), viewHolder, viewHolder.avatar, avaUrl);
            } else {
                viewHolder.setIsRecyclable(false); // todo pair with (true)
                new APIUtil.GetIdUser(getActivity(), dataItem.UserId, new APIUtil.APICallback() {
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
                        ImageUtil.setViewHolderImage(getActivity(), viewHolder, viewHolder.avatar, newAvaUrl);
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


    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public ImageView avatar;
        public TextView title;
        public TextView authorInfo;
        public TextView replyInfo;
        public TextView content;

        public ImageView imgReply;
        public ImageView imgQuote;

        public XMLUtil.TopicInfo data_topicInfo;
        public XMLUtil.PostInfo data_postInfo;

        public ViewHolder(View itemLayoutView) {
            super(itemLayoutView);
            avatar = (ImageView) itemLayoutView.findViewById(R.id.image_icon);
            title = (TextView) itemLayoutView.findViewById(R.id.text_title);
            authorInfo = (TextView) itemLayoutView.findViewById(R.id.text_authorInfo);
            replyInfo = (TextView) itemLayoutView.findViewById(R.id.text_replyInfo);
            content = (TextView) itemLayoutView.findViewById(R.id.text_content);
            imgReply = (ImageView) itemLayoutView.findViewById(R.id.image_reply);
            imgQuote = (ImageView) itemLayoutView.findViewById(R.id.image_quote);

            // set item click listener
            imgReply.setOnClickListener(this);
            imgQuote.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            HelperUtil.generalDebug("PostsFragment", "onClick " + v.toString());
            switch (v.getId()) {
                case R.id.image_reply:
                    String replyTitle = (data_postInfo.Floor == 1) ? "" : "回复 " + data_postInfo.UserName + "(#" + data_postInfo.Floor + ")";
                    ActivityUtil.openNewPostDialog(v.getContext(), data_topicInfo.Id, replyTitle, "");
                    break;
                case R.id.image_quote:
                    String quoteTitle = "回复 " + data_postInfo.UserName + "(#" + data_postInfo.Floor + ")";
                    ActivityUtil.openNewPostDialog(v.getContext(), data_topicInfo.Id, quoteTitle,
                            "[quotex][i]> " + data_postInfo.UserName + "@" + data_postInfo.Time + "(#" + data_postInfo.Floor + ")[/i]\n" +
                                    data_postInfo.Content + "[/quotex]\n\n");
            }
        }
    }

}
