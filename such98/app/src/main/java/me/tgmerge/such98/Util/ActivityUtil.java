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

import me.tgmerge.such98.R;
import me.tgmerge.such98.activity.LoginActivity;
import me.tgmerge.such98.activity.LoginPageActivity;
import me.tgmerge.such98.activity.ShowBoardsActivity;
import me.tgmerge.such98.activity.ShowPostsActivity;
import me.tgmerge.such98.activity.ShowTopicsActivity;
import me.tgmerge.such98.activity.ViewImageActivity;
import me.tgmerge.such98.custom.SuchApp;
import me.tgmerge.such98.fragment.BoardsFragment;
import me.tgmerge.such98.fragment.NewPostFragment;
import me.tgmerge.such98.fragment.PostsFragment;
import me.tgmerge.such98.fragment.TopicsFragment;

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

        public static final void showMessages(Context ctx) { showMessages(ctx, false); }
        public static final void showMessages(Context ctx, boolean clearTask) {
            // todo
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

    public static final void openShowBoardsActivity(Context ctx, int id, int startPos, boolean clearTask) {
        logDebug("Starting ShowBoardsActivity, id=" + id + ", startPos=" + startPos);
        Intent intent = new Intent(ctx, ShowBoardsActivity.class);
        intent.putExtra(ShowBoardsActivity.INTENT_KEY_ID, id);
        intent.putExtra(ShowBoardsActivity.INTENT_KEY_START_POS, startPos);
        if (clearTask) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
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
        }
        ctx.startActivity(intent);
    }

    public static final void openLoginPageActivity(Context ctx, boolean clearTask) {
        logDebug("Starting LoginPageActivity");
        Intent intent = new Intent(ctx, LoginPageActivity.class);
        if (clearTask) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        }
        ctx.startActivity(intent);
    }

    public static final void openLoginActivity(Context ctx, boolean clearTask) {
        logDebug("Starting LoginActivity");
        Intent intent = new Intent(ctx, LoginActivity.class);
        if (clearTask) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        }
        ctx.startActivity(intent);
    }

    public static final void openViewImageActivity(Context ctx, String url) {
        logDebug("Starting ViewImageActivity");
        Intent intent = new Intent(ctx, ViewImageActivity.class);
        intent.putExtra(ViewImageActivity.INTENT_KEY_URL, url);
        ctx.startActivity(intent);
    }

    // dialog & dialog fragment

    public static final void openNewPostDialog(Context ctx, int topicId, String replyTitle, String replyContent) {
        logDebug("Starting NewPostFragment(FragmentDialog)");
        if (ctx instanceof Activity) {
            FragmentTransaction ft = ((Activity) ctx).getFragmentManager().beginTransaction();
            ft.addToBackStack(null);
            DialogFragment newFragment = NewPostFragment.newInstance(topicId, replyTitle, replyContent);
            newFragment.show(ft, "NewPostFragment");
        } else {
            HelperUtil.errorToast("openNewPostDialog: Context" + ctx.toString() + " is not Activity");
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

    // - - -

    private static final void logDebug(String msg) {
        HelperUtil.generalDebug("ActivityUtil", msg);
    }


    private static final void logError(String msg) {
        HelperUtil.generalError("ActivityUtil", msg);
    }
}
