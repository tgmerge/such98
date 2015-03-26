package me.tgmerge.such98.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.InputType;
import android.widget.EditText;

import org.apache.http.Header;

import me.tgmerge.such98.R;
import me.tgmerge.such98.activity.LoginActivity;
import me.tgmerge.such98.activity.LoginPageActivity;
import me.tgmerge.such98.activity.ShowBoardsActivity;
import me.tgmerge.such98.activity.ShowMessagesActivity;
import me.tgmerge.such98.activity.ShowPostsActivity;
import me.tgmerge.such98.activity.ShowTopicsActivity;
import me.tgmerge.such98.activity.ViewImageActivity;
import me.tgmerge.such98.custom.SuchApp;
import me.tgmerge.such98.fragment.BoardsFragment;
import me.tgmerge.such98.fragment.MessageDialogFragment;
import me.tgmerge.such98.fragment.MessagesFragment;
import me.tgmerge.such98.fragment.NewMessageDialogFragment;
import me.tgmerge.such98.fragment.NewPostDialogFragment;
import me.tgmerge.such98.fragment.NewTopicDialogFragment;
import me.tgmerge.such98.fragment.PostsFragment;
import me.tgmerge.such98.fragment.TopicsFragment;
import me.tgmerge.such98.fragment.UserInfoDialogFragment;

/**
 * Created by tgmerge on 2/15.
 * Provides easy way for performing action(in Action subclass)
 * and methods for starting Activities.
 */
public final class ActivityUtil {

    public static final class Action {

        // activity
        public static final void showHotTopics(Context ctx) { showHotTopics(ctx, false); }
        public static final void showHotTopics(Context ctx, boolean clearTask) {
            openShowTopicsActivity(ctx, TopicsFragment.PARAM_ID_HOT, 0, clearTask);
        }

        public static final void showNewTopics(Context ctx) { showNewTopics(ctx, false); }
        public static final void showNewTopics(Context ctx, boolean clearTask) {
            openShowTopicsActivity(ctx, TopicsFragment.PARAM_ID_NEW, 0, clearTask);
        }

        public static final void showRootBoard(Context ctx) { showRootBoard(ctx, false); }
        public static final void showRootBoard(Context ctx, boolean clearTask) {
            openShowBoardsActivity(ctx, BoardsFragment.PARAM_ID_ROOT, 0, clearTask);
        }

        public static final void showCustomBoards(Context ctx) { showCustomBoards(ctx, false); }
        public static final void showCustomBoards(Context ctx, boolean clearTask) {
            openShowBoardsActivity(ctx, BoardsFragment.PARAM_ID_CUSTOM, 0, clearTask);
        }

        public static final void relogin(Context ctx) { relogin(ctx, true); }
        public static final void relogin(Context ctx, boolean clearTask) {
            OAuthUtil.clearToken();
            openLoginPageActivity(ctx, clearTask);
        }

        public static final void logout(Context ctx) { logout(ctx, true); }
        public static final void logout(Context ctx, boolean clearTask) {
            OAuthUtil.clearToken();
            openLoginActivity(ctx, clearTask);
        }

        public static final void myInfo(Context ctx) { myInfo(ctx, false); }
        public static final void myInfo(Context ctx, boolean clearTask) {
            // todo
        }

        public static final void showMessagesAll(Context ctx) { showMessagesAll(ctx, false); }
        public static final void showMessagesAll(Context ctx, boolean clearTask) {
            openShowMessagesActivity(ctx, MessagesFragment.PARAM_FILTER_BOTH, 0, clearTask);
        }

        public static final void showMessagesReceive(Context ctx) { showMessagesReceive(ctx, false); }
        public static final void showMessagesReceive(Context ctx, boolean clearTask) {
            openShowMessagesActivity(ctx, MessagesFragment.PARAM_FILTER_RECEIVE, 0, clearTask);
        }

        public static final void showMessagesSend(Context ctx) { showMessagesSend(ctx, false); }
        public static final void showMessagesSend(Context ctx, boolean clearTask) {
            openShowMessagesActivity(ctx, MessagesFragment.PARAM_FILTER_SEND, 0, clearTask);
        }

        public static final void setting(Context ctx) { setting(ctx, false); }
        public static final void setting(Context ctx, boolean clearTask) {
            // todo
        }

        // fragment
        public static final void postFragmentFirstPage(Activity act, int containerId, int topicId) {
            loadPostsFragment(act, containerId, topicId, PostsFragment.PARAM_POS_BEGINNING);
        }

        public static final void postFragmentLastPage(Activity act, int containerId, int topicId) {
            loadPostsFragment(act, containerId, topicId, PostsFragment.PARAM_POS_END);
        }

        public static final void postFragmentToFloor(Activity act, int containerId, int topicId, int pos) {
            loadPostsFragment(act, containerId, topicId, pos);
        }

        public static final void topicFragmentFirstPage(Activity act, int containerId, int boardId) {
            loadTopicsFragment(act, containerId, boardId, TopicsFragment.PARAM_POS_BEGINNING);
        }

        public static final void topicFragmentLastPage(Activity act, int containerId, int boardId) {
            loadTopicsFragment(act, containerId, boardId, TopicsFragment.PARAM_POS_END);
        }

        public static final void topicFragmentToItem(Activity act, int containerId, int topicId, int pos) {
            loadTopicsFragment(act, containerId, topicId, pos);
        }


    }

    // activity

    public static final void openShowMessagesActivity(Context ctx, int filter, int startPos, boolean clearTask) {
        logDebug("Starting ShowMessagesActivity, filter=" + filter + ", startPos=" + startPos);
        Intent intent = new Intent(ctx, ShowMessagesActivity.class);
        intent.putExtra(ShowMessagesActivity.INTENT_KEY_FILTER, filter);
        intent.putExtra(ShowMessagesActivity.INTENT_KEY_START_POS, startPos);
        if (clearTask) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        } else {
            markIntentAsNotRoot(intent);
        }
        ctx.startActivity(intent);
    }

    public static final void openShowBoardsActivity(Context ctx, int id, int startPos, boolean clearTask) {
        logDebug("Starting ShowBoardsActivity, id=" + id + ", startPos=" + startPos);
        Intent intent = new Intent(ctx, ShowBoardsActivity.class);
        intent.putExtra(ShowBoardsActivity.INTENT_KEY_ID, id);
        intent.putExtra(ShowBoardsActivity.INTENT_KEY_START_POS, startPos);
        if (clearTask) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        } else {
            markIntentAsNotRoot(intent);
        }
        ctx.startActivity(intent);
    }

    public static final void openShowTopicsActivity(Context ctx, int id, int startPos, boolean clearTask) {
        logDebug("Starting ShowTopicsActivity, id=" + id + ", startPos=" + startPos);
        Intent intent = new Intent(ctx, ShowTopicsActivity.class);
        intent.putExtra(ShowTopicsActivity.INTENT_KEY_ID, id);
        intent.putExtra(ShowTopicsActivity.INTENT_KEY_START_POS, startPos);
        if (clearTask) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        } else {
            markIntentAsNotRoot(intent);
        }
        ctx.startActivity(intent);
    }

    public static final void openShowPostsActivity(Context ctx, int id, int startPos, boolean clearTask) {
        logDebug("Starting ShowPostsActivity, id=" + id + ", startPos=" + startPos);
        Intent intent = new Intent(ctx, ShowPostsActivity.class);
        intent.putExtra(ShowPostsActivity.INTENT_KEY_ID, id);
        intent.putExtra(ShowPostsActivity.INTENT_KEY_START_POS, startPos);
        if (clearTask) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        } else {
            markIntentAsNotRoot(intent);
        }
        ctx.startActivity(intent);
    }

    public static final void openLoginPageActivity(Context ctx, boolean clearTask) {
        logDebug("Starting LoginPageActivity");
        Intent intent = new Intent(ctx, LoginPageActivity.class);
        if (clearTask) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        } else {
            markIntentAsNotRoot(intent);
        }
        ctx.startActivity(intent);
    }

    public static final void openLoginActivity(Context ctx, boolean clearTask) {
        logDebug("Starting LoginActivity");
        Intent intent = new Intent(ctx, LoginActivity.class);
        if (clearTask) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        } else {
            markIntentAsNotRoot(intent);
        }
        ctx.startActivity(intent);
    }

    public static final void openViewImageActivity(Context ctx, String url) {
        logDebug("Starting ViewImageActivity");
        Intent intent = new Intent(ctx, ViewImageActivity.class);
        intent.putExtra(ViewImageActivity.INTENT_KEY_URL, url);
        markIntentAsNotRoot(intent);
        ctx.startActivity(intent);
    }

    // dialog & dialog fragment

    public static final void openNewMessageDialog(Context ctx, String receiverName, String title, String content) {
        logDebug("Starting NewMessageDialog(FragmentDialog), receiverName=" + receiverName);
        if (ctx instanceof Activity) {
            FragmentTransaction ft = ((Activity) ctx).getFragmentManager().beginTransaction();
            ft.addToBackStack(null);
            DialogFragment newFragment = NewMessageDialogFragment.newInstance(receiverName, title, content);
            newFragment.show(ft, "NewMessageDialogFragment");
        } else {
            HelperUtil.errorToast("openMessageDialog: Context" + ctx.toString() + " is not Activity");
        }
    }

    public static final void openUserInfoDialog(Context ctx, String userName) {
        logDebug("Starting UserInfoDialog(FragmentDialog), userName=" + userName);
        if (ctx instanceof Activity) {
            FragmentTransaction ft = ((Activity) ctx).getFragmentManager().beginTransaction();
            ft.addToBackStack(null);
            DialogFragment newFragment = UserInfoDialogFragment.newInstance(userName);
            newFragment.show(ft, "UserInfoDialogFragment");
        } else {
            HelperUtil.errorToast("openMessageDialog: Context" + ctx.toString() + " is not Activity");
        }
    }

    public static final void openUserInfoDialog(Context ctx, int userId) {
        logDebug("Starting UserInfoDialog(FragmentDialog), userId=" + userId);
        if (ctx instanceof Activity) {
            FragmentTransaction ft = ((Activity) ctx).getFragmentManager().beginTransaction();
            ft.addToBackStack(null);
            DialogFragment newFragment = UserInfoDialogFragment.newInstance(userId);
            newFragment.show(ft, "UserInfoDialogFragment");
        } else {
            HelperUtil.errorToast("openMessageDialog: Context" + ctx.toString() + " is not Activity");
        }
    }

    public static final void openMessageDialog(Context ctx, String senderName, String receiverName, String title, String content, boolean isDraft, boolean isRead, String sendTime) {
        logDebug("Starting MessageDialogFragment(FragmentDialog)");
        if (ctx instanceof Activity) {
            FragmentTransaction ft = ((Activity) ctx).getFragmentManager().beginTransaction();
            ft.addToBackStack(null);
            DialogFragment newFragment = MessageDialogFragment.newInstance(senderName, receiverName, title, content, isDraft, isRead, sendTime);
            newFragment.show(ft, "MessageDialogFragment");
        } else {
            HelperUtil.errorToast("openMessageDialog: Context" + ctx.toString() + " is not Activity");
        }
    }

    public static final void openNewPostDialog(Context ctx, int topicId, String replyTitle, String replyContent) {
        logDebug("Starting NewPostDialogFragment(FragmentDialog)");
        if (ctx instanceof Activity) {
            FragmentTransaction ft = ((Activity) ctx).getFragmentManager().beginTransaction();
            ft.addToBackStack(null);
            DialogFragment newFragment = NewPostDialogFragment.newInstance(topicId, replyTitle, replyContent);
            newFragment.show(ft, "NewPostDialogFragment");
        } else {
            HelperUtil.errorToast("openNewPostDialog: Context" + ctx.toString() + " is not Activity");
        }
    }

    public static final void openNewTopicDialog(Context ctx, int boardId, String title, String content) {
        logDebug("Starting NewTopicDialogFragment(FragmentDialog)");
        if (ctx instanceof Activity) {
            FragmentTransaction ft = ((Activity) ctx).getFragmentManager().beginTransaction();
            ft.addToBackStack(null);
            DialogFragment newFragment = NewTopicDialogFragment.newInstance(boardId, title, content);
            newFragment.show(ft, "NewTopicDialogFragment");
        } else {
            HelperUtil.errorToast("openNewTopicDialog: Context" + ctx.toString() + " is not Activity");
        }
    }

    // ... and goto that floor
    // maxPos is counted from 1(#1 floor, pos 0)
    public static final void openGotoFloorDialog(final Activity act, final int containerId, final int topicId, final int maxFloor) {
        logDebug("Opening 'goto floor' dialog, tID=" + topicId + ", maxFloor=" + maxFloor);
        AlertDialog.Builder alert = new AlertDialog.Builder(act);
        alert.setTitle(SuchApp.getStr(R.string.util_activity_goto_floor_dialog_title));
        final EditText input = new EditText(act);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setHint(SuchApp.getStr(R.string.util_activity_goto_floor_dialog_hint, maxFloor));
        alert.setView(input);
        alert.setPositiveButton(SuchApp.getStr(R.string.util_activity_goto_floor_dialog_button), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    int floor = Integer.parseInt(input.getText().toString());
                    if (floor > maxFloor || floor < 1) {
                        throw new NumberFormatException("floor=" + floor + " is invalid");
                    }
                    loadPostsFragment(act, containerId, topicId, floor - 1);
                } catch (NumberFormatException e) {
                    HelperUtil.errorToast(SuchApp.getStr(R.string.util_activity_goto_floor_dialog_wrong_floor));
                    e.printStackTrace();
                }
            }
        });
        alert.show();
    }

    public static final void openGotoTopicItemDialog(final Activity act, final int containerId, final int boardId, final int itemNum) {
        logDebug("Opening 'goto topic item' dialog, bID=" + boardId + ", itemNum=" + itemNum);
        AlertDialog.Builder alert = new AlertDialog.Builder(act);
        alert.setTitle(SuchApp.getStr(R.string.util_activity_goto_topic_dialog_title));
        final EditText input = new EditText(act);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setHint(SuchApp.getStr(R.string.util_activity_goto_topic_dialog_hint, itemNum));
        alert.setView(input);
        alert.setPositiveButton(SuchApp.getStr(R.string.util_activity_goto_topic_dialog_button), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    int floor = Integer.parseInt(input.getText().toString());
                    if (floor > itemNum || floor < 1) {
                        throw new NumberFormatException("floor=" + floor + " is invalid");
                    }
                    loadTopicsFragment(act, containerId, boardId, floor - 1);
                } catch (NumberFormatException e) {
                    HelperUtil.errorToast(SuchApp.getStr(R.string.util_activity_goto_topic_dialog_wrong_floor));
                    e.printStackTrace();
                }
            }
        });
        alert.show();
    }

    // fragment

    public static final void reloadFragment(Activity act, int containerId) {
        logDebug("Reload fragment, act=" + act.toString());
        FragmentTransaction transaction = act.getFragmentManager().beginTransaction();
        Fragment fragment = act.getFragmentManager().findFragmentById(containerId);
        transaction.detach(fragment);
        transaction.attach(fragment);
        transaction.commit();
    }

    public static final void loadPostsFragment(Activity act, int containerId, int topicId, int startPos) {
        logDebug("Loading PostsFragment, id=" + topicId + ", pos=" + startPos);
        FragmentTransaction transaction = act.getFragmentManager().beginTransaction();
        PostsFragment fragment = PostsFragment.newInstance(topicId, startPos);
        transaction.replace(containerId, fragment);
        transaction.commit();
    }

    public static final void loadTopicsFragment(Activity act, int containerId, int boardId, int startPos) {
        logDebug("Loading TopicsFragment, id=" + boardId + ", pos=" + startPos);
        FragmentTransaction transaction = act.getFragmentManager().beginTransaction();
        TopicsFragment fragment = TopicsFragment.newInstance(boardId, startPos);
        transaction.replace(containerId, fragment);
        transaction.commit();
    }

    public static final void loadBoardsFragment(Activity act, int containerId, int boardId, int startPos) {
        logDebug("Loading BoardsFragment, id=" + boardId + ", pos=" + startPos);
        FragmentTransaction transaction = act.getFragmentManager().beginTransaction();
        BoardsFragment fragment = BoardsFragment.newInstance(boardId, startPos);
        transaction.replace(containerId, fragment);
        transaction.commit();
    }


    public static void loadMessagesFragment(Activity act, int containerId, int filter, int startPos) {
        logDebug("Loading MessagesFragment, filter=" + filter + ", pos=" + startPos);
        FragmentTransaction transaction = act.getFragmentManager().beginTransaction();
        MessagesFragment fragment = MessagesFragment.newInstance(filter, startPos);
        transaction.replace(containerId, fragment);
        transaction.commit();
    }

    // - - - for callback

    public static void defaultOnApiFailure(Context ctx, int statCode, Header[] headers, byte[] body, Throwable error) {
        if (statCode == 401) {
            // "Unauthorized"
            HelperUtil.errorToast("登录信息失效，请重新登录(code=" + statCode + ", error=" + error.toString() + ")");
            Action.relogin(ctx, true);
        } else {
            HelperUtil.errorToast(SuchApp.getStr(R.string.general_on_api_failure_toast_text, statCode, error.toString()));
        }
    }


    // - - -

    private static final String ARG_INTENT_NOT_ROOT = "notRoot";

    private static final void markIntentAsNotRoot(Intent intent) {
        intent.putExtra(ARG_INTENT_NOT_ROOT, 0);
    }

    public static final boolean checkActivityIsRoot(Activity act) {
        return !(act.getIntent().hasExtra(ARG_INTENT_NOT_ROOT));
    }

    private static final void logDebug(String msg) {
        HelperUtil.generalDebug("ActivityUtil", msg);
    }


    private static final void logError(String msg) {
        HelperUtil.generalError("ActivityUtil", msg);
    }
}
