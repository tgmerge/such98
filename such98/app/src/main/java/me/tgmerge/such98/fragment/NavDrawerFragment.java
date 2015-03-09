package me.tgmerge.such98.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
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
import me.tgmerge.such98.util.XMLUtil;

/**
 * Nav drawer fragment.
 */
public class NavDrawerFragment extends Fragment implements View.OnClickListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    // root view of this fragment
    View mThisView = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mThisView = inflater.inflate(R.layout.fragment_drawer, container, false);

        // Register on click listener
        mThisView.findViewById(R.id.drawer_avatar_circle).setOnClickListener(this);
        mThisView.findViewById(R.id.drawer_function_root_board).setOnClickListener(this);
        mThisView.findViewById(R.id.drawer_function_custom_board).setOnClickListener(this);
        mThisView.findViewById(R.id.drawer_function_hot_topic).setOnClickListener(this);
        mThisView.findViewById(R.id.drawer_function_new_topic).setOnClickListener(this);
        mThisView.findViewById(R.id.drawer_function_message_all).setOnClickListener(this);
        mThisView.findViewById(R.id.drawer_function_message_send).setOnClickListener(this);
        mThisView.findViewById(R.id.drawer_function_message_receive).setOnClickListener(this);
        mThisView.findViewById(R.id.drawer_function_logout).setOnClickListener(this);

        return mThisView;
    }


    // flags, showing drawer's loading state
    private boolean drawerIsLoading = false;
    private boolean drawerIsSet = false;

    private DrawerLayout mDrawerLayout;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (!drawerIsLoading && !drawerIsSet) {
            setDrawer();
        }
    }

    public void setUp(int fragmentId, DrawerLayout drawerLayout) {
        mDrawerLayout = drawerLayout;

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.drawer_avatar_circle:
                HelperUtil.debugToast("Avatar clicked");
                break;
            case R.id.drawer_function_root_board:
                ActivityUtil.Action.showRootBoard(getActivity(), true);
                break;
            case R.id.drawer_function_custom_board:
                ActivityUtil.Action.showCustomBoards(getActivity(), true);
                break;
            case R.id.drawer_function_hot_topic:
                ActivityUtil.Action.showHotTopics(getActivity(), true);
                break;
            case R.id.drawer_function_new_topic:
                ActivityUtil.Action.showNewTopics(getActivity(), true);
                break;
            case R.id.drawer_function_message_all:
                ActivityUtil.Action.showMessagesAll(getActivity(), false);
                break;
            case R.id.drawer_function_message_receive:
                ActivityUtil.Action.showMessagesReceive(getActivity(), false);
                break;
            case R.id.drawer_function_message_send:
                ActivityUtil.Action.showMessagesSend(getActivity(), false);
                break;
            case R.id.drawer_function_logout:
                ActivityUtil.Action.logout(getActivity(), true);
                break;
        }
        mThisView.findViewById(R.id.drawer_avatar_circle).setOnClickListener(this);
    }

    // - - -

    private void setDrawer() {
        drawerIsLoading = true;
        final ImageView imgView = (ImageView) mThisView.findViewById(R.id.drawer_avatar);
        final TextView textUserName = (TextView) mThisView.findViewById(R.id.drawer_user_name);
        final TextView textUserInfo = (TextView) mThisView.findViewById(R.id.drawer_user_desc);
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
                ImageUtil.setImage(getActivity(), imgView, 128, avaUrl);
                textUserName.setText(info.Name);
                textUserInfo.setText(info.Title);
                drawerIsSet = true;
            }

            @Override
            public void onFailure(int statCode, Header[] headers, byte[] body, Throwable error) {
                drawerIsLoading = false;
                HelperUtil.errorToast(SuchApp.getStr(R.string.general_on_api_failure_toast_text, statCode, error.toString()));
            }
        }).execute();
    }
}
