package me.tgmerge.such98.util;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;

import me.tgmerge.such98.activity.LoginActivity;
import me.tgmerge.such98.activity.LoginPageActivity;
import me.tgmerge.such98.activity.ShowBoardsActivity;
import me.tgmerge.such98.activity.ShowPostsActivity;
import me.tgmerge.such98.activity.ShowTopicsActivity;
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

        public static final void showHotTopics(Context ctx) { showHotTopics(ctx, false); }
        public static final void showHotTopics(Context ctx, boolean clearTask) {
            openShowTopicsActivity(ctx, TopicsFragment.ID_HOT, 0, clearTask);
        }

        public static final void showNewTopics(Context ctx) { showNewTopics(ctx, false); }
        public static final void showNewTopics(Context ctx, boolean clearTask) {
            openShowTopicsActivity(ctx, TopicsFragment.ID_NEW, 0, clearTask);
        }

        public static final void showRootBoard(Context ctx) { showRootBoard(ctx, false); }
        public static final void showRootBoard(Context ctx, boolean clearTask) {
            openShowBoardsActivity(ctx, BoardsFragment.ID_ROOT, 0, clearTask);
        }

        public static final void showCustomBoards(Context ctx) { showCustomBoards(ctx, false); }
        public static final void showCustomBoards(Context ctx, boolean clearTask) {
            openShowBoardsActivity(ctx, BoardsFragment.ID_CUSTOM, 0, clearTask);
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

        public static final void postGotoFirstPage(Activity act, int containerId, int topicId) {
            FragmentTransaction transaction = act.getFragmentManager().beginTransaction();
            transaction.replace(containerId, PostsFragment.newInstance(topicId,PostsFragment.PARAM_POS_BEGINNING));
            transaction.commit();
        }

        public static final void postGotoLastPage(Activity act, int containerId, int topicId) {
            FragmentTransaction transaction = act.getFragmentManager().beginTransaction();
            transaction.replace(containerId, PostsFragment.newInstance(topicId,PostsFragment.PARAM_POS_END));
            transaction.commit();
        }

        public static final void addPostFragment(Activity act, int containerId, int topicId, int startPos) {
            FragmentTransaction transaction = act.getFragmentManager().beginTransaction();
            transaction.add(containerId, PostsFragment.newInstance(topicId, startPos));
            transaction.commit();
        }

        public static final void reloadFragment(Activity act, int containerId) {
            Fragment fg = act.getFragmentManager().findFragmentById(containerId);
            FragmentTransaction transaction = act.getFragmentManager().beginTransaction();
            transaction.detach(fg);
            transaction.attach(fg);
            transaction.commit();
        }
    }

    // - - -

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

    public static final void openNewPostDialog(Context ctx, int topicId, String replyTitle, String replyContent) {
        logDebug("Starting NewPostFragment(FragmentDialog)");
        if (ctx instanceof Activity) {
            FragmentTransaction ft = ((Activity) ctx).getFragmentManager().beginTransaction();
            ft.addToBackStack(null);
            DialogFragment newFragment = NewPostFragment.newInstance(topicId, replyTitle, replyContent);
            newFragment.show(ft, "NewPostFragment");
        } else {
            HelperUtil.errorToast(ctx, "openNewPostDialog: Context" + ctx.toString() + " is not Activity");
        }
    }

    // - - -

    private static final void logDebug(String msg) {
        HelperUtil.generalDebug("ActivityUtil", msg);
    }


    private static final void logError(String msg) {
        HelperUtil.generalError("ActivityUtil", msg);
    }
}
