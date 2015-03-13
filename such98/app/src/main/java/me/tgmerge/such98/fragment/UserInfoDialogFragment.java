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
import me.tgmerge.such98.util.CacheUtil;
import me.tgmerge.such98.util.HelperUtil;
import me.tgmerge.such98.util.ImageUtil;
import me.tgmerge.such98.util.TextUtil;
import me.tgmerge.such98.util.XMLUtil;

public class UserInfoDialogFragment extends DialogFragment implements View.OnClickListener {

    private static final String ARG_PARAM_USER_ID = "id";
    private static final String ARG_PARAM_USER_NAME = "name";

    private int mParamUserId;
    private String mParamUserName;

    XMLUtil.UserInfo mUserInfo = new XMLUtil.UserInfo();

    View mThisView;

    public static UserInfoDialogFragment newInstance(int paramUserId) {
        UserInfoDialogFragment fragment = new UserInfoDialogFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PARAM_USER_ID, paramUserId);
        fragment.setArguments(args);
        return fragment;
    }

    public static UserInfoDialogFragment newInstance(String paramUserName) {
        UserInfoDialogFragment fragment = new UserInfoDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM_USER_NAME, paramUserName);
        fragment.setArguments(args);
        return fragment;
    }

    public UserInfoDialogFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParamUserId = getArguments().getInt(ARG_PARAM_USER_ID, 0);
            mParamUserName = getArguments().getString(ARG_PARAM_USER_NAME, "");
        }
        setStyle(STYLE_NO_TITLE, 0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mThisView = inflater.inflate(R.layout.dialog_fragment_user_info, container, false);

        getDialog().setCanceledOnTouchOutside(false);

        mThisView.findViewById(R.id.user_info_send_message).setOnClickListener(this);

        return mThisView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        APIUtil.APICallback callback = new APIUtil.APICallback() {
            @Override
            public void onSuccess(int statCode, Header[] headers, byte[] body) {
                try {
                    mUserInfo.parse(new String(body));
                } catch (Exception e) {
                    e.printStackTrace();
                    onFailure(-1, headers, body, e);
                    return;
                }
                setUserInfoValues(mUserInfo);
            }

            @Override
            public void onFailure(int statCode, Header[] headers, byte[] body, Throwable error) {
                HelperUtil.errorToast(SuchApp.getStr(R.string.general_on_api_failure_toast_text, statCode, error.toString()));
            }
        };

        if (mParamUserId != 0) {
            new APIUtil.GetIdUser(getActivity(), mParamUserId, callback).execute();
        } else if (mParamUserName != null && !mParamUserName.equals("")) {
            new APIUtil.GetNameUser(getActivity(), mParamUserName, callback).execute();
        } else {
            HelperUtil.errorToast("Invalid name/id.");
        }
    }

    private final void setUserInfoValues(XMLUtil.UserInfo userInfo) {
        String avaUrl = userInfo.PortraitUrl.startsWith("http") ? userInfo.PortraitUrl : ("http://www.cc98.org/" + userInfo.PortraitUrl);
        CacheUtil.id_avaUrlCache.put(userInfo.Id, avaUrl);
        ImageUtil.setImage(getActivity(), (ImageView) mThisView.findViewById(R.id.user_info_avatar), 160, avaUrl);
        ((TextView) mThisView.findViewById(R.id.text_user_info_header_name)).setText(userInfo.Name);
        ((TextView) mThisView.findViewById(R.id.user_info_id)).setText(String.valueOf(userInfo.Id));
        ((TextView) mThisView.findViewById(R.id.user_info_name)).setText(userInfo.Name);
        ((TextView) mThisView.findViewById(R.id.user_info_title)).setText(userInfo.Title);
        ((TextView) mThisView.findViewById(R.id.user_info_fraction)).setText(userInfo.Faction);
        ((TextView) mThisView.findViewById(R.id.user_info_groupname)).setText(userInfo.GroupName);
        ((TextView) mThisView.findViewById(R.id.user_info_gender)).setText(userInfo.Gender.equals("Male") ? "男" : userInfo.Gender.equals("Female") ? "女" : userInfo.Gender);
        ((TextView) mThisView.findViewById(R.id.user_info_birthday)).setText(TextUtil.longTimeString(userInfo.Birthday));
        ((TextView) mThisView.findViewById(R.id.user_info_is_online)).setText(userInfo.IsOnline ? "在线" : "离线");
        ((TextView) mThisView.findViewById(R.id.user_info_register_name)).setText(TextUtil.longTimeString(userInfo.RegisterTime));
        ((TextView) mThisView.findViewById(R.id.user_info_last_logon_time)).setText(TextUtil.longTimeString(userInfo.LastLogOnTime));
        ((TextView) mThisView.findViewById(R.id.user_info_post_count)).setText(String.valueOf(userInfo.PostCount));
        ((TextView) mThisView.findViewById(R.id.user_info_level)).setText(userInfo.Level);
        ((TextView) mThisView.findViewById(R.id.user_info_prestige)).setText(String.valueOf(userInfo.Prestige));
        ((TextView) mThisView.findViewById(R.id.user_info_email_address)).setText(userInfo.EmailAddress);
        ((TextView) mThisView.findViewById(R.id.user_info_homepage_url)).setText(userInfo.HomePageUrl);
        ((TextView) mThisView.findViewById(R.id.user_info_qq)).setText(userInfo.QQ);
        ((TextView) mThisView.findViewById(R.id.user_info_msn)).setText(userInfo.Msn);
        TextView signature = (TextView) mThisView.findViewById(R.id.user_info_signature);
        TextUtil.setBBcodeToTextView(signature, getActivity(), userInfo.SignatureCode);
        signature.setMovementMethod(LinkMovementMethod.getInstance());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.user_info_send_message:
                if (mParamUserName == null || mParamUserName.equals("")) {
                    HelperUtil.errorToast("Error: UserInfoDialogFragment没有传入用户名orz");
                } else {
                    ActivityUtil.openNewMessageDialog(getActivity(), mParamUserName, "");
                }
                break;
        }
    }
}
