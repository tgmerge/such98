package me.tgmerge.such98.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import org.apache.http.Header;

import me.tgmerge.such98.R;
import me.tgmerge.such98.adapter.PostsAdapter;
import me.tgmerge.such98.adapter.RecyclerSwipeAdapter;
import me.tgmerge.such98.custom.SuchApp;
import me.tgmerge.such98.util.APIUtil;
import me.tgmerge.such98.util.ActivityUtil;
import me.tgmerge.such98.util.HelperUtil;
import me.tgmerge.such98.util.XMLUtil;


public class PostsFragment extends RecyclerSwipeFragment {

    public static PostsFragment newInstance(int paramId, int paramPos) {
        PostsFragment fragment = new PostsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PARAM_ID, paramId);
        args.putInt(ARG_PARAM_POS, paramPos);
        fragment.setArguments(args);
        return fragment;
    }

    private final XMLUtil.TopicInfo mTopicInfo = new XMLUtil.TopicInfo();

    protected PostsAdapter createAdapter() {
        return new PostsAdapter(getActivity(), null, null);
    }

    protected void initialLoad() {
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

                // todo prevent rotate screen -> crash
                if (getActivity() == null) {
                    return;
                }

                // loading topic info: finished
                getActivity().setTitle(mTopicInfo.Title);
                if (mAdapter instanceof PostsAdapter) {
                    ((PostsAdapter) mAdapter).setTopicInfo(mTopicInfo);
                }
                mRecyclerView.setEnabled(true);
                //setProgressFinished();

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
                HelperUtil.errorToast(SuchApp.getStr(R.string.general_on_api_failure_toast_text, statCode, error.toString()));
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
            case R.id.action_new:
                ActivityUtil.openNewPostDialog(activity, mParamId, "", "");
                return true;
            case R.id.action_toFirstPage:
                ActivityUtil.Action.postFragmentFirstPage(activity, containerId, mParamId);
                return true;
            case R.id.action_toLastPage:
                ActivityUtil.Action.postFragmentLastPage(activity, containerId, mParamId);
                return true;
            case R.id.action_toItem:
                ActivityUtil.openGotoFloorDialog(activity, containerId, mParamId, mTopicInfo.ReplyCount + 1);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // - - -

    protected int getMaxPosToLoad() {
        return mTopicInfo.ReplyCount;
    }

    protected void load(final boolean loadPrevious, final RecyclerSwipeAdapter adapter, int posToLoad, int sizeToLoad) {

        final PostsAdapter postsAdapter = (PostsAdapter) adapter;

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
                    postsAdapter.appendDataFront(postsInfo);
                    mPreviousPage --;
                } else {
                    postsAdapter.appendData(postsInfo);
                    mNextPage ++;
                }
                setProgressFinished();
            }

            @Override
            public void onFailure(int statCode, Header[] headers, byte[] body, Throwable error) {
                HelperUtil.errorToast(SuchApp.getStr(R.string.general_on_api_failure_toast_text, statCode, error.toString()));
                setProgressFinished();
            }
        }

        setProgressLoading();
        HelperUtil.debugToast(SuchApp.getStr(R.string.fragment_posts_loading_item, posToLoad, posToLoad + sizeToLoad));
        new APIUtil.GetTopicPost(getActivity(), mParamId, posToLoad, null, sizeToLoad, new Callback()).execute();

    }
}