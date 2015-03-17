
package me.tgmerge.such98.fragment;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import org.apache.http.Header;

import me.tgmerge.such98.R;
import me.tgmerge.such98.custom.SuchApp;
import me.tgmerge.such98.util.APIUtil;
import me.tgmerge.such98.util.CacheUtil;
import me.tgmerge.such98.util.HelperUtil;
import me.tgmerge.such98.util.ImageUtil;
import me.tgmerge.such98.util.XMLUtil;

public class NewMessageDialogFragment extends DialogFragment implements View.OnClickListener {

    private static final String ARG_PARAM_RECEIVER_NAME = "receiverName";
    private static final String ARG_PARAM_CONTENT = "content";
    private static final String ARG_PARAM_TITLE = "title";

    private String mParamReceiverName;
    private String mParamContent;
    private String mParamTitle;

    View mThisView;

    public static NewMessageDialogFragment newInstance(String paramReceiverName, String paramTitle, String paramContent) {
        NewMessageDialogFragment fragment = new NewMessageDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM_RECEIVER_NAME, paramReceiverName);
        args.putString(ARG_PARAM_TITLE, paramTitle);
        args.putString(ARG_PARAM_CONTENT, paramContent);
        fragment.setArguments(args);
        return fragment;
    }

    public NewMessageDialogFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParamReceiverName = getArguments().getString(ARG_PARAM_RECEIVER_NAME, "");
            mParamContent = getArguments().getString(ARG_PARAM_CONTENT);
            mParamTitle = getArguments().getString(ARG_PARAM_TITLE);
        }
        setStyle(STYLE_NO_TITLE, 0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mThisView = inflater.inflate(R.layout.dialog_fragment_new_message, container, false);

        getDialog().setCanceledOnTouchOutside(false);

        mThisView.findViewById(R.id.new_message_send).setOnClickListener(this);

        return mThisView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (mParamReceiverName == null || mParamReceiverName.equals("")) {
            HelperUtil.errorToast("Invalid name/id.");
        }

        if (mParamTitle != null) {
            ((EditText) mThisView.findViewById(R.id.new_message_title)).setText(mParamTitle);
        }
        if (mParamContent != null) {
            ((EditText) mThisView.findViewById(R.id.new_message_content)).setText(mParamContent);
        }

        final ImageView imgAvatar = (ImageView) mThisView.findViewById(R.id.new_message_avatar);

        new APIUtil.GetNameUser(getActivity(), mParamReceiverName, new APIUtil.APICallback() {
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

    private boolean isLoading = false;

    private final void setProgressLoading() {
        isLoading = true;
        mThisView.findViewById(R.id.progress_bar).setVisibility(View.VISIBLE);
    }

    private final void setProgressFinished() {
        mThisView.findViewById(R.id.progress_bar).setVisibility(View.GONE);
        isLoading = false;
    }

    private void sendMessage() {
        String msgTitle = ((EditText) mThisView.findViewById(R.id.new_message_title)).getText().toString();
        String msgContent = ((EditText) mThisView.findViewById(R.id.new_message_content)).getText().toString();

        if (msgContent.equals("")) {
            HelperUtil.errorToast("消息内容不能为空");
            return;
        }

        if (mParamReceiverName.equals("") || mParamReceiverName.equals("系统消息") || mParamReceiverName.equals("匿名")) {
            HelperUtil.errorToast("错误的收件人：" + mParamReceiverName);
            return;
        }

        setProgressLoading();
        new APIUtil.PostMessage(getActivity(), mParamReceiverName, msgTitle, msgContent, new APIUtil.APICallback() {
            @Override
            public void onSuccess(int statCode, Header[] headers, byte[] body) {
                HelperUtil.debugToast("消息发送成功");
                dismiss();
            }

            @Override
            public void onFailure(int statCode, Header[] headers, byte[] body, Throwable error) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
                dialog.setTitle("回复失败")
                        .setMessage("网络错误, code=" + statCode + ", err=" + error)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .show();
                setProgressFinished();
            }
        }).execute();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.new_message_send:
                sendMessage();
                break;
        }
    }
}
