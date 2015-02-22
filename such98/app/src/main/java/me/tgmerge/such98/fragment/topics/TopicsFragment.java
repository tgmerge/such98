package me.tgmerge.such98.fragment.topics;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.apache.http.Header;

import me.tgmerge.such98.R;
import me.tgmerge.such98.util.APIUtil;
import me.tgmerge.such98.util.HelperUtil;
import me.tgmerge.such98.util.XMLUtil;


public class TopicsFragment extends Fragment {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM_ID = "id";
    private static final String ARG_PARAM_POS = "pos";

    private int mParamId = 0;
    private int mParamPos = 0;

    // consts for mParamId;
    public static final int ID_NEW = -1, ID_HOT = -2;

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
    public static TopicsFragment newInstance(int paramId, int paramPos) {
        TopicsFragment fragment = new TopicsFragment();
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
        thisView = inflater.inflate(R.layout.fragment_topics, container, false);
        return thisView;
    }


    // onAttach: called at the very first
    // http://developer.android.com/training/basics/fragments/communicating.html
    //           todo register parent activity as listeners
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    // onDetach: call at the very last
    //           todo recycle resources, setting them to null will be fine
    //           todo clear listeners references
    @Override
    public void onDetach() {
        super.onDetach();
    }


    // onActivityCreated: the activity has finished its "onCreated"
    //                    this will be also called when fragment replace happens.
    // see http://segmentfault.com/blog/shiyongdanshuiyu/1190000000650573
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final RecyclerView recyclerView = (RecyclerView) thisView.findViewById(R.id.recyclerView);

        final LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        final TopicsAdapter adapter = new TopicsAdapter(null);
        recyclerView.setAdapter(adapter);

        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView view, int dx, int dy) {
                if (!isLoading && !isNoMoreItem) {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int pastVisibleItems = layoutManager.findFirstVisibleItemPosition();
                    if ((visibleItemCount + pastVisibleItems) >= totalItemCount) {
                        HelperUtil.debugToast(getActivity(), "Loading more...");
                        loadAPage(adapter);
                    }
                }
            }
        });

        recyclerView.setItemAnimator(new DefaultItemAnimator());

        int intentBoardId = mParamId;
        final XMLUtil.BoardInfo boardInfo = new XMLUtil.BoardInfo();

        if (intentBoardId == ID_HOT) {
            boardInfo.Id = ID_HOT;
            boardInfo.Name = "热门主题";
            getActivity().setTitle(boardInfo.Name);
            loadAPage(adapter);
        } else if (intentBoardId == ID_NEW) {
            boardInfo.Id = ID_NEW;
            boardInfo.Name = "最新主题";
            getActivity().setTitle(boardInfo.Name);
            loadAPage(adapter);
        } else {
            new APIUtil.GetBoard(getActivity(), intentBoardId, new APIUtil.APICallback() {
                @Override
                public void onSuccess(int statCode, Header[] headers, byte[] body) {
                    try {
                        boardInfo.parse(new String(body));
                    } catch (Exception e) {
                        e.printStackTrace();
                        onFailure(-1, headers, body, e);
                    }
                    getActivity().setTitle(boardInfo.Name);
                    loadAPage(adapter);
                }

                @Override
                public void onFailure(int statCode, Header[] headers, byte[] body, Throwable error) {
                    HelperUtil.errorToast(getActivity(), "Error: " + "code=" + statCode + ", error=" + error.toString());
                }
            }).execute();
        }
    }


    // - - -

    private boolean isLoading = false;
    private boolean isNoMoreItem = false;

    private int lastLoadStartPos = -1;

    public static final int ITEM_PER_PAGE = 10;

    // - - -

    private final void setProgressLoading() {
        isLoading = true;
        thisView.findViewById(R.id.progress_bar).setVisibility(View.VISIBLE);
    }

    private final void setProgressFinished() {
        thisView.findViewById(R.id.progress_bar).setVisibility(View.GONE);
        isLoading = false;
    }

    private final void loadAPage(final TopicsAdapter adapter) {
        setProgressLoading();

        final int intentId = mParamId;
        int intentStartPos = mParamPos;

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
                    HelperUtil.debugToast(getActivity(), "Already at last page");
                } else {
                    adapter.appendData(topicInfoArr);
                    lastLoadStartPos = pageStart;
                }
                setProgressFinished();
            }

            @Override
            public void onFailure(int statCode, Header[] headers, byte[] body, Throwable error) {
                setProgressFinished();
                HelperUtil.errorToast(getActivity(), "Error, code=" + statCode + ", error=" + error.toString());
            }
        }

        if (intentId == ID_NEW) {
            // Show new topics
            new APIUtil.GetNewTopic(getActivity(), pageStart, null, ITEM_PER_PAGE, new Callback()).execute();

        } else if (intentId == ID_HOT) {
            // show hot topics
            new APIUtil.GetHotTopic(getActivity(), pageStart, null, ITEM_PER_PAGE, new Callback()).execute();

        } else {
            // show topics by board id
            new APIUtil.GetBoardTopic(getActivity(), intentId, pageStart, null, ITEM_PER_PAGE, new Callback()).execute();
        }
    }


}
