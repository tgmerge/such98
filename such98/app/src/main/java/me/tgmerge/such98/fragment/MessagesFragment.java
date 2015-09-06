package me.tgmerge.such98.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import org.apache.http.Header;

import me.tgmerge.such98.R;
import me.tgmerge.such98.adapter.MessagesAdapter;
import me.tgmerge.such98.adapter.RecyclerSwipeAdapter;
import me.tgmerge.such98.custom.SuchApp;
import me.tgmerge.such98.util.APIUtil;
import me.tgmerge.such98.util.ActivityUtil;
import me.tgmerge.such98.util.HelperUtil;
import me.tgmerge.such98.util.XMLUtil;

public class MessagesFragment extends RecyclerSwipeFragment {

    public static final int PARAM_FILTER_NONE = 0;
    public static final int PARAM_FILTER_SEND = 1;
    public static final int PARAM_FILTER_RECEIVE = 2;
    public static final int PARAM_FILTER_BOTH = 3;

    protected static final String ARG_PARAM_FILTER = "filter";

    private int mParamFilter;

    public static MessagesFragment newInstance(int paramFilter, int paramPos) {
        MessagesFragment fragment = new MessagesFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PARAM_FILTER, paramFilter);
        args.putInt(ARG_PARAM_POS, paramPos);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected RecyclerSwipeAdapter createAdapter() {
        return new MessagesAdapter(null);
    }

    @Override
    protected void initialLoad() {

        final MessagesAdapter messagesAdapter = (MessagesAdapter) mAdapter;
        mParamFilter = getArguments().getInt(ARG_PARAM_FILTER);

        String actTitle = "";

        if (mParamFilter == PARAM_FILTER_SEND) {
            actTitle = "发件箱";
        } else if (mParamFilter == PARAM_FILTER_RECEIVE) {
            actTitle = "收件箱";
        } else if (mParamFilter == PARAM_FILTER_BOTH) {
            actTitle = "所有消息";
        } else {
            actTitle = "? msg";
        }

        getActivity().setTitle(actTitle);

        mPreviousPage = -1;
        mNextPage = 0;
        loadNextPage(messagesAdapter);
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
                ActivityUtil.openNewMessageDialog(activity,"" , "", "");
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected int getMaxPosToLoad() {
        return 100000; // todo ???
    }

    @Override
    protected void load(final boolean loadPrevious, RecyclerSwipeAdapter adapter, int posToLoad, int sizeToLoad) {

        final MessagesAdapter messagesAdapter = (MessagesAdapter) adapter;

        HelperUtil.debugToast("正在加载消息 " + posToLoad + " - " + (posToLoad + sizeToLoad) + " …");
        // todo check username
        new APIUtil.GetUserMessage(getActivity(), "", mParamFilter, posToLoad, null, sizeToLoad, new APIUtil.APICallback() {
            @Override
            public void onSuccess(int statCode, Header[] headers, byte[] body) {
                XMLUtil.ArrayOf<XMLUtil.MessageInfo> messagesInfo = new XMLUtil.ArrayOf<>(XMLUtil.MessageInfo.class);
                try {
                    messagesInfo.parse(new String(body));
                } catch (Exception e) {
                    e.printStackTrace();
                    onFailure(-1, headers, body, e);
                    return;
                }

                if (!loadPrevious && messagesInfo.size() == 0) {
                    mHasNextPage = false;
                }

                if (loadPrevious) {
                    mAdapter.appendDataFront(messagesInfo);
                    mPreviousPage --;
                } else {
                    mAdapter.appendData(messagesInfo);
                    mNextPage ++;
                }
                setProgressFinished();
            }

            @Override
            public void onFailure(int statCode, Header[] headers, byte[] body, Throwable error) {
                setProgressFinished();
                ActivityUtil.defaultOnApiFailure(getActivity(), statCode, headers, body, error);
            }
        }).execute();
    }
}
