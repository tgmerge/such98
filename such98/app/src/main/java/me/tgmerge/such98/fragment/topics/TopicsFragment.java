package me.tgmerge.such98.fragment.topics;

import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import org.apache.http.Header;

import me.tgmerge.such98.R;
import me.tgmerge.such98.fragment.base.RecyclerSwipeAdapter;
import me.tgmerge.such98.fragment.base.RecyclerSwipeFragment;
import me.tgmerge.such98.util.APIUtil;
import me.tgmerge.such98.util.ActivityUtil;
import me.tgmerge.such98.util.HelperUtil;
import me.tgmerge.such98.util.XMLUtil;


public class TopicsFragment extends RecyclerSwipeFragment {

    public static final int PARAM_ID_NEW = -1;
    public static final int PARAM_ID_HOT = -2;

    public static TopicsFragment newInstance(int paramId, int paramPos) {
        TopicsFragment fragment = new TopicsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PARAM_ID, paramId);
        args.putInt(ARG_PARAM_POS, paramPos);
        fragment.setArguments(args);
        return fragment;
    }

    XMLUtil.BoardInfo mBoardInfo = new XMLUtil.BoardInfo();

    protected RecyclerSwipeAdapter createAdapter() {
        return new TopicsAdapter(null);
    }

    protected void initialLoad() {

        final TopicsAdapter topicsAdapter = (TopicsAdapter) mAdapter;

        if (mParamId == PARAM_ID_HOT) {
            mBoardInfo.Id = PARAM_ID_HOT;
            mBoardInfo.Name = "热门主题";
            mBoardInfo.TotalTopicCount = 10;
            getActivity().setTitle(mBoardInfo.Name);
            mPreviousPage = -1;
            mNextPage = 0;
            loadNextPage(topicsAdapter);
        } else if (mParamId == PARAM_ID_NEW) {
            mBoardInfo.Id = PARAM_ID_NEW;
            mBoardInfo.Name = "最新主题";
            mBoardInfo.TotalTopicCount = 500; // todo verify
            getActivity().setTitle(mBoardInfo.Name);
            mPreviousPage = -1;
            mNextPage = 0;
            loadNextPage(topicsAdapter);
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

                    // todo prevent rotate screen -> crash
                    if (getActivity() == null) {
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
                        loadNextPage(topicsAdapter);
                    } else if (mParamPos == PARAM_POS_END) {
                        mPreviousPage = maxPage;
                        mNextPage = maxPage + 1;
                        loadPreviousPage(topicsAdapter);
                    } else {
                        mNextPage = mParamPos/ITEM_PER_PAGE;
                        mPreviousPage = mNextPage - 1;
                        loadNextPage(topicsAdapter);
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
            case R.id.action_new:
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

    protected int getMaxPosToLoad() {
        return mBoardInfo.TotalTopicCount - 1;
    }

    protected void load(final boolean loadPrevious, final RecyclerSwipeAdapter adapter, int posToLoad, int sizeToLoad) {

        final TopicsAdapter topicsAdapter = (TopicsAdapter) adapter;

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
                    topicsAdapter.appendDataFront(topicsInfo);
                    mPreviousPage --;
                } else {
                    topicsAdapter.appendData(topicsInfo);
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
