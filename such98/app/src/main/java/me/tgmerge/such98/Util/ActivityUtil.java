package me.tgmerge.such98.Util;

import android.content.Context;
import android.content.Intent;

import me.tgmerge.such98.LoginActivity;
import me.tgmerge.such98.LoginPageActivity;
import me.tgmerge.such98.ShowBoardsActivity;
import me.tgmerge.such98.ShowPostsActivity;
import me.tgmerge.such98.ShowTopicsActivity;

/**
 * Created by tgmerge on 2/15.
 * Provides easy way for performing action(in Action subclass)
 * and methods for starting Activities.
 */
public final class ActivityUtil {

    public static final class Action {

        public static final void showHotTopics(Context ctx) { showHotTopics(ctx, false); }
        public static final void showHotTopics(Context ctx, boolean clearTask) {
            openShowTopicsActivity(ctx, ShowTopicsActivity.ID_HOT, 0, clearTask);
        }

        public static final void showNewTopics(Context ctx) { showNewTopics(ctx, false); }
        public static final void showNewTopics(Context ctx, boolean clearTask) {
            openShowTopicsActivity(ctx, ShowTopicsActivity.ID_NEW, 0, clearTask);
        }

        public static final void showRootBoard(Context ctx) { showRootBoard(ctx, false); }
        public static final void showRootBoard(Context ctx, boolean clearTask) {
            openShowBoardsActivity(ctx, ShowBoardsActivity.ID_ROOT, 0, clearTask);
        }

        public static final void showCustomBoards(Context ctx) { showCustomBoards(ctx, false); }
        public static final void showCustomBoards(Context ctx, boolean clearTask) {
            openShowBoardsActivity(ctx, ShowBoardsActivity.ID_CUSTOM, 0, clearTask);
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

    // - - -

    private static final void logDebug(String msg) {
        HelperUtil.generalDebug("ActivityUtil", msg);
    }


    private static final void logError(String msg) {
        HelperUtil.generalError("ActivityUtil", msg);
    }
}
