package me.tgmerge.such98.fragment;

import android.app.DialogFragment;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.apache.http.Header;

import me.tgmerge.such98.R;
import me.tgmerge.such98.custom.SuchApp;
import me.tgmerge.such98.util.APIUtil;
import me.tgmerge.such98.util.ActivityUtil;
import me.tgmerge.such98.util.TextUtil;
import me.tgmerge.such98.util.CacheUtil;
import me.tgmerge.such98.util.HelperUtil;
import me.tgmerge.such98.util.ImageUtil;
import me.tgmerge.such98.util.XMLUtil;

public class MessageDialogFragment extends DialogFragment implements View.OnClickListener {

    private static final String ARG_PARAM_SENDER_NAME = "sender";
    private static final String ARG_PARAM_RECEIVER_NAME = "receiver";
    private static final String ARG_PARAM_TITLE = "title";
    private static final String ARG_PARAM_CONTENT = "content";
    private static final String ARG_PARAM_IS_DRAFT = "isdraft";
    private static final String ARG_PARAM_IS_READ = "isread";
    private static final String ARG_PARAM_SEND_TIME = "sendtime";

    private String mParamSenderName;
    private String mParamReceiverName;
    private String mParamTitle;
    private String mParamContent;
    private boolean mParamIsDraft;
    private boolean mParamIsRead;
    private String mParamSendTime;

    View mThisView;

    public static MessageDialogFragment newInstance(String senderName, String receiverName, String title, String content, boolean isDraft, boolean isRead, String sendTime) {
        MessageDialogFragment fragment = new MessageDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM_SENDER_NAME, senderName);
        args.putString(ARG_PARAM_RECEIVER_NAME, receiverName);
        args.putString(ARG_PARAM_TITLE, title);
        args.putString(ARG_PARAM_CONTENT, content);
        args.putBoolean(ARG_PARAM_IS_DRAFT, isDraft);
        args.putBoolean(ARG_PARAM_IS_READ, isRead);
        args.putString(ARG_PARAM_SEND_TIME, sendTime);
        fragment.setArguments(args);
        return fragment;
    }

    public MessageDialogFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParamSenderName = getArguments().getString(ARG_PARAM_SENDER_NAME);
            mParamReceiverName = getArguments().getString(ARG_PARAM_RECEIVER_NAME);
            mParamTitle = getArguments().getString(ARG_PARAM_TITLE);
            mParamContent = getArguments().getString(ARG_PARAM_CONTENT);
            mParamIsDraft = getArguments().getBoolean(ARG_PARAM_IS_DRAFT);
            mParamIsRead = getArguments().getBoolean(ARG_PARAM_IS_READ);
            mParamSendTime = getArguments().getString(ARG_PARAM_SEND_TIME);
        }
        setStyle(STYLE_NO_TITLE, 0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mThisView = inflater.inflate(R.layout.dialog_fragment_message, container, false);

        getDialog().setCanceledOnTouchOutside(false);

        mThisView.findViewById(R.id.img_msg_reply).setOnClickListener(this);
        mThisView.findViewById(R.id.msg_avatar).setOnClickListener(this);

        return mThisView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final ImageView imgAvatar = (ImageView) mThisView.findViewById(R.id.msg_avatar);

        if (mParamTitle != null) {
            ((TextView) mThisView.findViewById(R.id.text_msg_title)).setText(mParamTitle);
        }
        if (mParamContent != null) {
            TextView contentTextView = (TextView) mThisView.findViewById(R.id.text_msg_content);
            TextUtil.setBBcodeToTextView(contentTextView, getActivity(), mParamContent);
            contentTextView.setMovementMethod(LinkMovementMethod.getInstance());
        }
        if (mParamSenderName != null) {
            if (mParamSenderName.equals("")) {
                ((TextView) mThisView.findViewById(R.id.text_msg_sender)).setText("系统消息");
            } else {
                ((TextView) mThisView.findViewById(R.id.text_msg_sender)).setText(mParamSenderName);
                new APIUtil.GetNameUser(getActivity(), mParamSenderName, new APIUtil.APICallback() {
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
                        String avaUrl = info.PortraitUrl.startsWith("http") ? info.PortraitUrl : ("http://www.cc98.org/" + info.PortraitUrl);
                        CacheUtil.id_avaUrlCache.put(info.Id, avaUrl);
                        ImageUtil.setImage(getActivity(), imgAvatar, 80, avaUrl);
                    }

                    @Override
                    public void onFailure(int statCode, Header[] headers, byte[] body, Throwable error) {
                        HelperUtil.errorToast(SuchApp.getStr(R.string.general_on_api_failure_toast_text, statCode, error.toString()));

                    }
                }).execute();
            }
        }
        if (mParamReceiverName != null) {
            ((TextView) mThisView.findViewById(R.id.text_msg_receiver)).setText("to " + mParamReceiverName);
        }
        if (mParamSendTime != null) {
            ((TextView) mThisView.findViewById(R.id.text_msg_time)).setText(TextUtil.longTimeString(mParamSendTime));
        }

    }

    private boolean isLoading = false;

    private final void setProgressLoading() {
        isLoading = true;
        mThisView.findViewById(R.id.progress_bar).setVisibility(View.VISIBLE);
    }

    private final void setProgressFinished() {
        mThisView.findViewById(R.id.progress_bar).setVisibility(View.GONE);
        isLoading = false;
    }

    private void replyMsg() {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_msg_reply:
                replyMsg();
                break;
            case R.id.msg_avatar:
                ActivityUtil.openUserInfoDialog(getActivity(), mParamSenderName);
                break;
        }
    }
}
