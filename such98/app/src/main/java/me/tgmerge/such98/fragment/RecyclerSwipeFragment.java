package me.tgmerge.such98.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import me.tgmerge.such98.custom.NoChildFocusRecyclerView;
import me.tgmerge.such98.R;
import me.tgmerge.such98.adapter.RecyclerSwipeAdapter;
import me.tgmerge.such98.util.HelperUtil;

public abstract class RecyclerSwipeFragment extends Fragment {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    protected static final String ARG_PARAM_ID = "id";
    protected static final String ARG_PARAM_POS = "pos";

    public static final int PARAM_POS_BEGINNING = 0;
    public static final int PARAM_POS_END = -1;

    // should be bigger than top margin of recyclerView items
    // TODO solve this
    protected static final int SWIPE_ENABLE_RANGE = 20;

    protected int mParamId = 0;
    protected int mParamPos = 0;

    // root view of this fragment
    protected View mThisView = null;

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
        mThisView = inflater.inflate(R.layout.fragment_recycler_swipe, container, false);
        return mThisView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_fragment_recycler_swipe, menu);
    }

    protected NoChildFocusRecyclerView mRecyclerView;
    protected LinearLayoutManager mLayoutManager;
    protected SwipeRefreshLayout mSwipeLayout;
    protected RecyclerSwipeAdapter mAdapter;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mHasPreviousPage = true;
        mHasNextPage = true;

        mRecyclerView = (NoChildFocusRecyclerView) mThisView.findViewById(R.id.recyclerView);
        mSwipeLayout = (SwipeRefreshLayout) mThisView.findViewById(R.id.swipe_container);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mAdapter = createAdapter();

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
                    if (firstChildTop >= 0 && firstChildTop < SWIPE_ENABLE_RANGE && firstVisiblePos == 0) {
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
        mSwipeLayout.setColorSchemeResources(R.color.swipe_refresh_1, R.color.swipe_refresh_2, R.color.swipe_refresh_3);

        // loading topic info...
        setProgressLoading();

        initialLoad();
    }

    // todo implement this, return an adapter(extends RecyclerSwipeAdapter) for fragment
    abstract protected RecyclerSwipeAdapter createAdapter();

    // todo implement this, fetch category data and load data in category for the first time
    // e.g. for PostsFragment:
    //      get TopicInfo
    //      set mPreviousPage and mNextPage
    //      set title
    //      loadNextPage / loadPreviousPage
    abstract protected void initialLoad();

    // - - - load page

    public static final int ITEM_PER_PAGE = 10;

    protected int mPreviousPage = -1;
    protected int mNextPage = -1;

    protected boolean mHasPreviousPage;
    protected boolean mHasNextPage;

    protected boolean isLoaded = false;

    protected final void setProgressLoading() {
        mSwipeLayout.setEnabled(true);
        mSwipeLayout.setRefreshing(true);
    }

    protected final void setProgressFinished() {
        mSwipeLayout.setRefreshing(false);
        mSwipeLayout.setEnabled(false);
    }

    protected final void loadNextPage(RecyclerSwipeAdapter adapter) {
        loadPage(false, adapter);
    }

    protected final void loadPreviousPage(RecyclerSwipeAdapter adapter) {
        loadPage(true, adapter);
    }

    // boolean loadPrevious:
    // true: previous
    // false: next
    protected final void loadPage(final boolean loadPrevious, final RecyclerSwipeAdapter adapter) {
        setProgressLoading();

        final int posToLoad = (loadPrevious) ? mPreviousPage*ITEM_PER_PAGE : mNextPage*ITEM_PER_PAGE;
        final int sizeToLoad = ITEM_PER_PAGE;

        if (loadPrevious && (!mHasPreviousPage || posToLoad < 0)) {
            HelperUtil.debugToast("Already at first page");
            mHasPreviousPage = false;
            setProgressFinished();
            return;
        } else if (!loadPrevious && (!mHasNextPage || posToLoad > getMaxPosToLoad())) {
            HelperUtil.debugToast("Already at last page");
            mHasNextPage = false;
            setProgressFinished();
            return;
        }

        HelperUtil.generalDebug("RecyclerSwipeFragment", "Loading #" + posToLoad + " - #" + (posToLoad + sizeToLoad) + "...");
        load(loadPrevious, adapter, posToLoad, sizeToLoad);
    }

    abstract protected int getMaxPosToLoad();

    abstract protected void load(final boolean loadPrevious, final RecyclerSwipeAdapter adapter, int posToLoad, int sizeToLoad);
}