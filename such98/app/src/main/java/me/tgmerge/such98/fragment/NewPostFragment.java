package me.tgmerge.such98.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.preference.DialogPreference;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import org.apache.http.Header;

import me.tgmerge.such98.R;
import me.tgmerge.such98.util.APIUtil;
import me.tgmerge.such98.util.CacheUtil;
import me.tgmerge.such98.util.HelperUtil;
import me.tgmerge.such98.util.ImageUtil;
import me.tgmerge.such98.util.XMLUtil;

/**
 * A simple {@link Fragment} subclass.
 */
public class NewPostFragment extends DialogFragment implements View.OnClickListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM_TOPIC_ID = "topicId";
    private static final String ARG_PARAM_TITLE = "title";
    private static final String ARG_PARAM_CONTENT = "content";

    // TODO: Rename and change types of parameters
    private int mParamTopicId;
    private String mParamTitle;
    private String mParamContent;

    View mThisView;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     */
    // TODO: Rename and change types and number of parameters
    public static NewPostFragment newInstance(int paramTopicId, String paramTitle, String paramContent) {
        NewPostFragment fragment = new NewPostFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PARAM_TOPIC_ID, paramTopicId);
        args.putString(ARG_PARAM_TITLE, paramTitle);
        args.putString(ARG_PARAM_CONTENT, paramContent);
        fragment.setArguments(args);
        return fragment;
    }

    public NewPostFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParamTopicId = getArguments().getInt(ARG_PARAM_TOPIC_ID);
            mParamTitle = getArguments().getString(ARG_PARAM_TITLE);
            mParamContent = getArguments().getString(ARG_PARAM_CONTENT);
        }
        setStyle(STYLE_NO_TITLE, 0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mThisView = inflater.inflate(R.layout.fragment_new_post, container, false);

        // clicking outside won't close dialog
        getDialog().setCanceledOnTouchOutside(false);

        // Register on click listener
        mThisView.findViewById(R.id.image_send).setOnClickListener(this);

        return mThisView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (mParamTitle != null) {
            ((EditText) mThisView.findViewById(R.id.edit_title)).setText(mParamTitle);
        }
        if (mParamContent != null) {
            ((EditText) mThisView.findViewById(R.id.edit_content)).setText(mParamContent);
        }

        final ImageView imgAvatar = (ImageView) mThisView.findViewById(R.id.image_icon);

        new APIUtil.GetMe(getActivity(), new APIUtil.APICallback() {
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
                HelperUtil.errorToast(getActivity(), "NewPostFragment: Network error on fetching user info. code=" + statCode + ", error=" + error.toString());
            }
        }).execute();
    }

    private boolean isLoading = false;

    private final void setProgressLoading() {
        isLoading = true;
        mThisView.findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
    }

    private final void setProgressFinished() {
        mThisView.findViewById(R.id.progressBar).setVisibility(View.GONE);
        isLoading = false;
    }

    /** on send image click:
     *    check if content&title is empty
     *    set progress loading
     *    send
     *        onSuccess:
     *          toast success
     *          close dialog
     *        onFail
     *          confirm failed
     *          set progress finished
     */
    private void sendPost() {
        String postTitle = ((EditText) mThisView.findViewById(R.id.edit_title)).getText().toString();
        String postContent = ((EditText) mThisView.findViewById(R.id.edit_content)).getText().toString();

        if (postContent.equals("")) {
            HelperUtil.errorToast(getActivity(), "Post content cannot be empty");
            return;
        }

        if (mParamTopicId <= 0) {
            HelperUtil.errorToast(getActivity(), "Invalid topic ID");
            return;
        }

        setProgressLoading();
        new APIUtil.PostTopicPost(getActivity(), mParamTopicId, postTitle, postContent, new APIUtil.APICallback() {
            @Override
            public void onSuccess(int statCode, Header[] headers, byte[] body) {
                HelperUtil.debugToast(getActivity(), "Reply sent successful");
                // close dialog
                dismiss();
            }

            @Override
            public void onFailure(int statCode, Header[] headers, byte[] body, Throwable error) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
                dialog.setTitle("Alert")
                        .setMessage("Reply failure. stat=" + statCode + ", error=" + error.toString())
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
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
            case R.id.image_send:
                sendPost();
        }
    }
}
