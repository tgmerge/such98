package me.tgmerge.such98.fragment.topics;

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
import me.tgmerge.such98.fragment.RecyclerSwipeAdapter;
import me.tgmerge.such98.util.APIUtil;
import me.tgmerge.such98.util.ActivityUtil;
import me.tgmerge.such98.util.HelperUtil;
import me.tgmerge.such98.util.XMLUtil;


public class TopicsFragment extends Fragment {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM_ID = "id";
    private static final String ARG_PARAM_POS = "pos";

    public static final int PARAM_POS_BEGINNING = 0;
    public static final int PARAM_POS_END = -1;

    public static final int PARAM_ID_NEW = -1;
    public static final int PARAM_ID_HOT = -2;

    // should be bigger than top margin of recyclerView items
    // TODO solve this
    private static final int SWIPE_ENABLE_RANGE = 20;

    private int mParamId = 0;
    private int mParamPos = 0;

    // root view of this fragment
    View mThisView = null;

    public static TopicsFragment newInstance(int paramId, int paramPos) {
        TopicsFragment fragment = new TopicsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PARAM_ID, paramId);
        args.putInt(ARG_PARAM_POS, paramPos);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParamId = getArguments().getInt(ARG_PARAM_ID);
            mParamPos = getArguments().getInt(ARG_PARAM_POS);
        }
        if (mParamId != PARAM_ID_HOT && mParamId != PARAM_ID_NEW) {
            setHasOptionsMenu(true);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mThisView = inflater.inflate(R.layout.fragment_topics, container, false);
        return mThisView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_fragment_topics, menu);
    }

    RecyclerView mRecyclerView;
    LinearLayoutManager mLayoutManager;
    SwipeRefreshLayout mSwipeLayout;
    RecyclerSwipeAdapter<XMLUtil.TopicInfo, TopicViewHolder> mAdapter;

    XMLUtil.BoardInfo mBoardInfo = new XMLUtil.BoardInfo();

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mHasPreviousPage = true;
        mHasNextPage = true;

        mRecyclerView = (RecyclerView) mThisView.findViewById(R.id.recyclerView);
        mSwipeLayout = (SwipeRefreshLayout) mThisView.findViewById(R.id.swipe_container);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mAdapter = new TopicsAdapter(null);

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

        mSwipeLayout.setEnabled(false);
        mSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadPreviousPage(mAdapter);
            }
        });

        setProgressLoading();

        if (mParamId == PARAM_ID_HOT) {
            mBoardInfo.Id = PARAM_ID_HOT;
            mBoardInfo.Name = "热门主题";
            mBoardInfo.TotalTopicCount = 10;
            getActivity().setTitle(mBoardInfo.Name);
            mPreviousPage = -1;
            mNextPage = 0;
            loadNextPage(mAdapter);
        } else if (mParamId == PARAM_ID_NEW) {
            mBoardInfo.Id = PARAM_ID_NEW;
            mBoardInfo.Name = "最新主题";
            mBoardInfo.TotalTopicCount = 500; // todo verify
            getActivity().setTitle(mBoardInfo.Name);
            mPreviousPage = -1;
            mNextPage = 0;
            loadNextPage(mAdapter);
        } else {
            new APIUtil.GetBoard(getActivity(), mParamId, new APIUtil.APICallback() {
                @Override
                public void onSuccess(int statCode, Header[] headers, byte[] body) {
                    try {
                        mBoardInfo.parse(new String(body));
                    } catch (Exception e) {
                        e.printStackTrace();
                        onFailure(-1, headers, body, e);
                        return;
                    }
                    // loading topic info: finished
                    getActivity().setTitle(mBoardInfo.Name);
                    mRecyclerView.setEnabled(true);
                    setProgressFinished();

                    // first load
                    int maxPage = mBoardInfo.TotalTopicCount / ITEM_PER_PAGE;

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
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Activity activity = getActivity();
        int containerId = ((View) mThisView.getParent()).getId();
        switch (item.getItemId()) {
            case R.id.action_refresh:
                ActivityUtil.reloadFragment(activity, containerId);
                return true;
            case R.id.action_new_topic:
                //ActivityUtil.openNewTopicDialog(activity, containerId, "", "");
                return true;
            case R.id.action_toFirstPage:
                ActivityUtil.Action.topicFragmentFirstPage(activity, containerId, mParamId);
                return true;
            case R.id.action_toLastPage:
                ActivityUtil.Action.topicFragmentLastPage(activity, containerId, mParamId);
                return true;
            case R.id.action_toItem:
                ActivityUtil.openGotoTopicItemDialog(activity, containerId, mParamId, mBoardInfo.TotalTopicCount);
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

    // - - -

    private final void setProgressLoading() {
        mSwipeLayout.setRefreshing(true);
        mSwipeLayout.setEnabled(false);
    }

    private final void setProgressFinished() {
        mSwipeLayout.setRefreshing(false);
        mSwipeLayout.setEnabled(false);
    }


    private final void loadNextPage(RecyclerSwipeAdapter<XMLUtil.TopicInfo, TopicViewHolder> adapter) {
        loadPage(false, adapter);
    }

    private final void loadPreviousPage(RecyclerSwipeAdapter<XMLUtil.TopicInfo, TopicViewHolder> adapter) {
        loadPage(true, adapter);
    }

    private final void loadPage(final boolean loadPrevious, final RecyclerSwipeAdapter<XMLUtil.TopicInfo, TopicViewHolder> adapter) {

        final int posToLoad = (loadPrevious) ? mPreviousPage*ITEM_PER_PAGE : mNextPage*ITEM_PER_PAGE;
        final int sizeToLoad = ITEM_PER_PAGE;

        if (loadPrevious && (!mHasPreviousPage || posToLoad < 0)) {
            HelperUtil.debugToast(getActivity(), "Already at first page");
            mHasPreviousPage = false;
            setProgressFinished();
            return;
        } else if (!loadPrevious && (!mHasNextPage || posToLoad > mBoardInfo.TotalTopicCount - 1)) {
            HelperUtil.debugToast(getActivity(), "Already at last page");
            mHasNextPage = false;
            setProgressFinished();
            return;
        }

        class Callback implements APIUtil.APICallback {
            @Override
            public void onSuccess(int statCode, Header[] headers, byte[] body) {
                XMLUtil.ArrayOf<XMLUtil.TopicInfo> topicsInfo = new XMLUtil.ArrayOf<>(XMLUtil.TopicInfo.class);

                String str = new String(body);

                try {
                    if (mParamId == PARAM_ID_HOT) {
                        // Hot topics
                        XMLUtil.ArrayOf<XMLUtil.HotTopicInfo> hotInfo = new XMLUtil.ArrayOf<>(XMLUtil.HotTopicInfo.class);
                        hotInfo.parse(str);
                        topicsInfo.append(hotInfo);
                    } else {
                        // Other topics
                        XMLUtil.ArrayOf<XMLUtil.TopicInfo> info = new XMLUtil.ArrayOf<>(XMLUtil.TopicInfo.class);
                        info.parse(str);
                        topicsInfo.append(info);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    onFailure(-1, headers, body, e);
                    return;
                }

                if (loadPrevious) {
                    adapter.appendDataFront(topicsInfo);
                    mPreviousPage --;
                } else {
                    adapter.appendData(topicsInfo);
                    mNextPage ++;
                }
                setProgressFinished();
            }

            @Override
            public void onFailure(int statCode, Header[] headers, byte[] body, Throwable error) {
                setProgressFinished();
                HelperUtil.errorToast(getActivity(), "Error, code=" + statCode + ", error=" + error.toString());
            }
        }

        HelperUtil.debugToast(getActivity(), "Loading #" + posToLoad + " - #" + (posToLoad + sizeToLoad) + "...");
        if (mParamId == PARAM_ID_NEW) {
            // Show new topics
            new APIUtil.GetNewTopic(getActivity(), posToLoad, null, sizeToLoad, new Callback()).execute();

        } else if (mParamId == PARAM_ID_HOT) {
            // show hot topics
            new APIUtil.GetHotTopic(getActivity(), posToLoad, null, sizeToLoad, new Callback()).execute();

        } else {
            // show topics by board id
            new APIUtil.GetBoardTopic(getActivity(), mParamId, posToLoad, null, sizeToLoad, new Callback()).execute();
        }
    }


}
