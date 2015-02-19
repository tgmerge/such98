package me.tgmerge.such98.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.apache.http.Header;

import me.tgmerge.such98.R;
import me.tgmerge.such98.util.APIUtil;
import me.tgmerge.such98.util.ActivityUtil;
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
        thisView.findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
    }

    private final void setProgressFinished() {
        thisView.findViewById(R.id.progressBar).setVisibility(View.GONE);
        isLoading = false;
    }

    private final void loadAPage(final ShowTopicsAdapter adapter) {
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
            HelperUtil.generalDebug("ShowTopicsFragment", "onClick! " + v.toString());
            if (v instanceof RelativeLayout) {
                // click on whole item
                HelperUtil.generalDebug("ShowTopicsFragment", "Clicked: " + data_topicId);
                ActivityUtil.openShowPostsActivity(v.getContext(), data_topicId, 0, false);
            }
        }
    }
}
