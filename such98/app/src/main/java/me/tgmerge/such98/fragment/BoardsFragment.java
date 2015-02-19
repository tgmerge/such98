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

import java.util.Arrays;

import me.tgmerge.such98.R;
import me.tgmerge.such98.util.APIUtil;
import me.tgmerge.such98.util.ActivityUtil;
import me.tgmerge.such98.util.HelperUtil;
import me.tgmerge.such98.util.XMLUtil;


public class BoardsFragment extends Fragment {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM_ID = "id";
    private static final String ARG_PARAM_POS = "pos";

    private int mParamId = 0;
    private int mParamPos = 0;

    // consts for mParamId;
    public static final int ID_ROOT = 0, ID_CUSTOM = -1;

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
    public static BoardsFragment newInstance(int paramId, int paramPos) {
        BoardsFragment fragment = new BoardsFragment();
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
        thisView = inflater.inflate(R.layout.fragment_boards, container, false);
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

        // RecyclerView
        final RecyclerView recyclerView = (RecyclerView) thisView.findViewById(R.id.recyclerView);

        // RecyclerView's LayoutManager
        final LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        // RecyclerView's Adapter
        final ShowBoardsAdapter adapter = new ShowBoardsAdapter(null);
        recyclerView.setAdapter(adapter);

        // RecyclerView's OnScrollListener
        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView view, int dx, int dy) {
                if (!isLoading && !isNoMoreItem) {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int pastVisibleItems = layoutManager.findFirstVisibleItemPosition();
                    if ((visibleItemCount + pastVisibleItems) >= totalItemCount) {
                        // scroll to bottom, has more item, not loading - we're going to load another page
                        HelperUtil.debugToast(getActivity(), "Loading more...");
                        loadAPage(adapter);
                    }
                }
            }
        });

        // RecyclerView's ItemAnimator
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        // get boardInfo XML of intentId
        int intentBoardId = mParamId;
        final XMLUtil.BoardInfo boardInfo = new XMLUtil.BoardInfo();

        if (intentBoardId == ID_CUSTOM) {
            boardInfo.Id = ID_CUSTOM;
            boardInfo.ChildBoardCount = 10;
            boardInfo.Name = "个人定制";
            getActivity().setTitle(boardInfo.Name);
            loadAPage(adapter);
        } else if (intentBoardId == ID_ROOT) {
            boardInfo.Id = ID_ROOT;
            boardInfo.ChildBoardCount = -1;
            boardInfo.Name = "主版面";
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

                    // load first page
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

    private final void loadAPage(final ShowBoardsAdapter adapter) {
        // show "loading" circle
        setProgressLoading();

        // page params from intent
        int intentId = mParamId;
        int intentStartPos = mParamPos;

        // "pageStart" and "pageSize" params for APIUtil
        final int pageStart = (this.lastLoadStartPos < 0) ? intentStartPos : this.lastLoadStartPos + ITEM_PER_PAGE;

        // callback class for APIUtil
        class Callback implements APIUtil.APICallback {
            @Override
            public void onSuccess(int statCode, Header[] headers, byte[] body) {
                XMLUtil.ArrayOf<XMLUtil.BoardInfo> boardInfoArr = new XMLUtil.ArrayOf<>(XMLUtil.BoardInfo.class);
                try {
                    boardInfoArr.parse(new String(body));
                } catch (Exception e) {
                    e.printStackTrace();
                    onFailure(-1, headers, body, e);
                }

                if (boardInfoArr.size() == 0) {
                    // no more item...
                    isNoMoreItem = true;
                    HelperUtil.debugToast(getActivity(), "End of list");
                } else {
                    // some item arrived
                    adapter.appendData(boardInfoArr);
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

        if (intentId == ID_ROOT) {
            // Show root board
            new APIUtil.GetRootBoard(getActivity(), pageStart, null, ITEM_PER_PAGE, new Callback()).execute();

        } else if (intentId == ID_CUSTOM) {
            // Show custom boards
            new APIUtil.GetCustomBoardsMe(getActivity(), new APIUtil.APICallback() {
                @Override
                public void onSuccess(int statCode, Header[] headers, byte[] body) {
                    XMLUtil.ArrayOfint boardIdVec = new XMLUtil.ArrayOfint();
                    try {
                        boardIdVec.parse(new String(body));
                    } catch (Exception e) {
                        e.printStackTrace();
                        onFailure(-1, headers, body, e);
                    }

                    int[] ids = new int[boardIdVec.values.size()];
                    for (int i = 0; i < boardIdVec.values.size(); i++) {
                        ids[i] = boardIdVec.values.get(i);
                    }
                    int[] rangedIds = Arrays.copyOfRange(ids, pageStart, pageStart + ITEM_PER_PAGE);

                    new APIUtil.GetMultiBoards(getActivity(), rangedIds, pageStart, null, ITEM_PER_PAGE, new Callback()).execute();
                }

                @Override
                public void onFailure(int statCode, Header[] headers, byte[] body, Throwable error) {
                    setProgressFinished();
                    HelperUtil.errorToast(getActivity(), "Error, code=" + statCode + ", error=" + error.toString());
                }
            }).execute();

        } else {
            // Show board by id
            new APIUtil.GetSubBoards(getActivity(), intentId, pageStart, null, ITEM_PER_PAGE, new Callback()).execute();
        }
    }

    private static class ShowBoardsAdapter extends RecyclerView.Adapter<ViewHolder> {

        private XMLUtil.ArrayOf<XMLUtil.BoardInfo> mData;

        public final void appendData(XMLUtil.ArrayOf<XMLUtil.BoardInfo> data) {
            if (mData == null) {
                mData = data;
            } else {
                mData.append(data);
            }
            notifyDataSetChanged();
        }

        public ShowBoardsAdapter(XMLUtil.ArrayOf<XMLUtil.BoardInfo> data) {
            mData = data;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemLayoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_board_card, parent, false);
            return new ViewHolder(itemLayoutView);
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, int position) {
            XMLUtil.BoardInfo dataItem = mData.get(position);

            viewHolder.icon.setImageResource(dataItem.IsCategory ? R.drawable.ic_folder_multiple_outline_white_36dp : R.drawable.ic_folder_outline_white_36dp);
            viewHolder.name.setText(dataItem.Name);
            viewHolder.isCategory.setText(dataItem.IsCategory ? "分类" : "");
            viewHolder.description.setText(dataItem.Description);

            viewHolder.data_boardId = dataItem.Id;
            viewHolder.data_isCat = dataItem.IsCategory;

            // todo API有问题，于是在所有“分类”版面下显示一个“强制按帖子版面打开”的选项……
            viewHolder.openAsNotCat.setVisibility(dataItem.IsCategory ? View.VISIBLE : View.GONE);
        }

        @Override
        public int getItemCount() {
            return (mData == null) ? 0 : mData.size();
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public ImageView icon;
        public TextView name;
        public TextView isCategory;
        public TextView description;
        public TextView openAsNotCat;

        public int data_boardId;
        public boolean data_isCat;

        public ViewHolder(View itemLayoutView) {
            super(itemLayoutView);
            icon = (ImageView) itemLayoutView.findViewById(R.id.image_icon);
            name = (TextView) itemLayoutView.findViewById(R.id.text_name);
            isCategory = (TextView) itemLayoutView.findViewById(R.id.text_isCategory);
            description = (TextView) itemLayoutView.findViewById(R.id.text_description);
            openAsNotCat = (TextView) itemLayoutView.findViewById(R.id.text_openAsNotCat);

            // set item & inner view click listener
            itemLayoutView.setOnClickListener(this);
            openAsNotCat.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            HelperUtil.generalDebug("ShowBoardsFragment", "onClick " + v.toString());
            if (v instanceof TextView) {
                // click on the text view, force show topics
                HelperUtil.generalDebug("ShowBoardsFragment", "Force open as board: " + data_boardId + ", " + data_isCat);
                ActivityUtil.openShowTopicsActivity(v.getContext(), data_boardId, 0, false);
            } else if (v instanceof RelativeLayout) {
                // click on whole item, starting new activity
                HelperUtil.generalDebug("ShowBoardsFragment", "Clicked: " + data_boardId + ", " + data_isCat);
                if (data_isCat) {
                    // clicked board is a category, start ShowBoardsActivity
                    ActivityUtil.openShowBoardsActivity(v.getContext(), data_boardId, 0, false);
                } else {
                    // clicked board has no sub-boards, start ShowTopicsActivity
                    ActivityUtil.openShowTopicsActivity(v.getContext(), data_boardId, 0, false);
                }
            }
        }
    }
}
