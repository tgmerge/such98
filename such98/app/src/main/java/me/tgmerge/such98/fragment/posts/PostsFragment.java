package me.tgmerge.such98.fragment.posts;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import org.apache.http.Header;

import me.tgmerge.such98.R;
import me.tgmerge.such98.util.APIUtil;
import me.tgmerge.such98.util.ActivityUtil;
import me.tgmerge.such98.util.HelperUtil;
import me.tgmerge.such98.util.XMLUtil;


public class PostsFragment extends Fragment {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM_ID = "id";
    private static final String ARG_PARAM_POS = "pos";

    public static final int PARAM_POS_BEGINNING = 0;
    public static final int PARAM_POS_END = -1;

    // should be bigger than top margin of recyclerView items
    // TODO solve this
    private static final int SWIPE_ENABLE_RANGE = 20;

    private int mParamId = 0;
    private int mParamPos = 0;

    // root view of this fragment
    View mThisView = null;

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
        setHasOptionsMenu(true);
    }

    // onCreateView: inflate the layout for this fragment.
    //               saving layout in mThisView for further usage in class methods
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mThisView = inflater.inflate(R.layout.fragment_posts, container, false);
        return mThisView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_fragment_posts, menu);
    }

    RecyclerView mRecyclerView;
    LinearLayoutManager mLayoutManager;
    SwipeRefreshLayout mSwipeLayout;
    PostsAdapter mAdapter;

    private final XMLUtil.TopicInfo mTopicInfo = new XMLUtil.TopicInfo();

    // onActivityCreated: the activity has finished its "onCreated"
    //                    this will be also called when fragment replace happens.
    // see http://segmentfault.com/blog/shiyongdanshuiyu/1190000000650573
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mHasPreviousPage = true;
        mHasNextPage = true;

        mRecyclerView = (RecyclerView) mThisView.findViewById(R.id.recyclerView);
        mSwipeLayout = (SwipeRefreshLayout) mThisView.findViewById(R.id.swipe_container);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mAdapter = new PostsAdapter(getActivity(), null, null);

        // config recycler view
        mRecyclerView.setEnabled(false);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView view, int dx, int dy) {
                if (mSwipeLayout.isRefreshing()) {
                    return;
                }

                if (mHasPreviousPage) {
                    int firstChildTop = mRecyclerView.getChildAt(0).getTop();
                    int firstVisiblePos = mLayoutManager.findFirstVisibleItemPosition();
                    if (firstChildTop > 0 && firstChildTop < SWIPE_ENABLE_RANGE && firstVisiblePos == 0) {
                        // scrolled to top?
                        mSwipeLayout.setEnabled(true);
                        return;
                    }
                }

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
                if (mSwipeLayout.isEnabled()) {
                    mSwipeLayout.setEnabled(false);
                }
            }
        });

        // config adapter
        mAdapter.setSwipeLayout(mSwipeLayout);

        // on refresh listener is only used to load previous page.
        // "load next page" is triggered by recycler view's "scroll to bottom" event,
        // in which indicator of swipeLayout is shown by isRefreshing(), then "load next page"
        // is called manually.
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

                // loading topic info: finished
                getActivity().setTitle(mTopicInfo.Title);
                mRecyclerView.setEnabled(true);
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

                if(!isLoaded) {
                    mSwipeLayout.setEnabled(true);
                    isLoaded = true;
                }
            }

            @Override
            public void onFailure(int statCode, Header[] headers, byte[] body, Throwable error) {
                HelperUtil.errorToast(getActivity(), "Error: " + "code=" + statCode + ", error=" + error.toString());
            }
        }).execute();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Activity activity = getActivity();
        int containerId = ((View) mThisView.getParent()).getId();
        switch (item.getItemId()) {
            case R.id.action_refresh:
                ActivityUtil.reloadFragment(activity, containerId);
                return true;
            case R.id.action_new_reply:
                ActivityUtil.openNewPostDialog(activity, containerId, "", "");
                return true;
            case R.id.action_toFirstPage:
                ActivityUtil.Action.postFragmentFirstPage(activity, containerId, mParamId);
                return true;
            case R.id.action_toLastPage:
                ActivityUtil.Action.postFragmentLastPage(activity, containerId, mParamId);
                return true;
            case R.id.action_toFloor:
                ActivityUtil.openGotoFloorDialog(activity, containerId, mParamId, mTopicInfo.ReplyCount + 1);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // - - -

    public static final int ITEM_PER_PAGE = 10;

    private int mPreviousPage = -1;
    private int mNextPage = -1;

    private boolean mHasPreviousPage;
    private boolean mHasNextPage;

    private boolean isLoaded = false;

    private final void setProgressLoading() {
        mSwipeLayout.setRefreshing(true);
        mSwipeLayout.setEnabled(false);
    }

    private final void setProgressFinished() {
        mSwipeLayout.setRefreshing(false);
        mSwipeLayout.setEnabled(false);
    }

    private final void loadNextPage(PostsAdapter adapter) {
        loadPage(false, adapter);
    }

    private final void loadPreviousPage(PostsAdapter adapter) {
        loadPage(true, adapter);
    }

    // boolean loadPrevious:
    // true: previous
    // false: next
    private final void loadPage(final boolean loadPrevious, final PostsAdapter adapter) {

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
                    adapter.appendDataFront(mTopicInfo, postsInfo);
                    mPreviousPage --;
                } else {
                    adapter.appendData(mTopicInfo, postsInfo);
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
}