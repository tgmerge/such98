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

    private int mParamId = 0;
    private int mParamPos = 0;

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
                        HelperUtil.debugToast(getActivity(), "Loading...");
                        loadAPage(adapter);
                    }
                }
            }
        });

        recyclerView.setItemAnimator(new DefaultItemAnimator());

        int intentTopicId = mParamId;

        new APIUtil.GetTopic(getActivity(), intentTopicId, new APIUtil.APICallback() {
            @Override
            public void onSuccess(int statCode, Header[] headers, byte[] body) {
                try {
                    mTopicInfo.parse(new String(body));
                    loadAPage(adapter);
                } catch (Exception e) {
                    e.printStackTrace();
                    onFailure(-1, headers, body, e);
                }
                getActivity().setTitle(mTopicInfo.Title);
            }

            @Override
            public void onFailure(int statCode, Header[] headers, byte[] body, Throwable error) {
                HelperUtil.errorToast(getActivity(), "Error: " + "code=" + statCode + ", error=" + error.toString());
            }
        }).execute();
    }


    // - - -

    private boolean isLoading = false;
    private boolean isNoMoreItem = false;

    private int lastLoadStartPos = -1;

    public static final int ITEM_PER_PAGE = 10;

    // Adapter 使用这两个值的时候，必然已经被设置过了
    private int mIntentId;
    private final XMLUtil.TopicInfo mTopicInfo = new XMLUtil.TopicInfo();


    // - - -

    private final void setProgressLoading() {
        isLoading = true;
        thisView.findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
    }

    private final void setProgressFinished() {
        thisView.findViewById(R.id.progressBar).setVisibility(View.GONE);
        isLoading = false;
    }


    private final void loadAPage(final ShowPostsAdapter adapter) {
        setProgressLoading();

        int intentId = mParamId;
        int intentStartPos = mParamPos;

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
                    HelperUtil.debugToast(getActivity(), "End of list");
                } else {
                    adapter.appendData(postInfoArr);
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

        new APIUtil.GetTopicPost(getActivity(), intentId, pageStart, null, ITEM_PER_PAGE, new Callback()).execute();
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

            viewHolder.topicInfo = mTopicInfo;
            viewHolder.postInfo = mData.get(position);

            viewHolder.title.setText(dataItem.Floor != 1 && dataItem.Title.length() == 0 ? "回复 #" + dataItem.Floor : dataItem.Title);
            viewHolder.authorInfo.setText(dataItem.UserName + " @ " + dataItem.Time);
            BBUtil.setBBcodeToTextView(viewHolder.content, dataItem.Content); // todo prevent to process every time

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

        public XMLUtil.TopicInfo topicInfo;
        public XMLUtil.PostInfo postInfo;

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
                    String replyTitle = (postInfo.Floor == 1) ? "" : "回复 " + postInfo.UserName + "(#" + postInfo.Floor + ")";
                    ActivityUtil.openNewPostDialog(v.getContext(), topicInfo.Id, replyTitle, "");
                    break;
                case R.id.image_quote:
                    String quoteTitle = "回复 " + postInfo.UserName + "(#" + postInfo.Floor + ")";
                    ActivityUtil.openNewPostDialog(v.getContext(), topicInfo.Id, quoteTitle,
                            "[quotex][i]> " + postInfo.UserName + "@" + postInfo.Time + "(#" + postInfo.Floor + ")[/i]\n" +
                            postInfo.Content + "[/quotex]\n\n");
            }
        }
    }

}
