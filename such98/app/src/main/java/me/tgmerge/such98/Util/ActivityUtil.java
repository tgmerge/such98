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
        public static final void showHotTopics(Context ctx) {
            openShowTopicsActivity(ctx, ShowTopicsActivity.ID_HOT, 0);
        }

        public static final void showNewTopics(Context ctx) {
            openShowTopicsActivity(ctx, ShowTopicsActivity.ID_NEW, 0);
        }

        public static final void showRootBoard(Context ctx) {
            openShowBoardsActivity(ctx, ShowBoardsActivity.ID_ROOT, 0);
        }

        public static final void relogin(Context ctx) {
            OAuthUtil.clearToken(ctx);
            openLoginPageActivity(ctx);
        }

        public static final void logout(Context ctx) {
            OAuthUtil.clearToken(ctx);
            openLoginActivity(ctx);
        }

        public static final void myInfo(Context ctx) {
            // todo
        }

        public static final void showMessages(Context ctx) {
            // todo
        }

        public static final void setting(Context ctx) {
            // todo
        }
    }

    // - - -

    public static final void openShowBoardsActivity(Context ctx, int id, int startPos) {
        logDebug("Starting ShowBoardsActivity, id=" + id + ", startPos=" + startPos);
        Intent intent = new Intent(ctx, ShowBoardsActivity.class);
        intent.putExtra(ShowBoardsActivity.INTENT_KEY_ID, id);
        intent.putExtra(ShowBoardsActivity.INTENT_KEY_START_POS, startPos);
        ctx.startActivity(intent);
    }

    public static final void openShowTopicsActivity(Context ctx, int id, int startPos) {
        logDebug("Starting ShowTopicsActivity, id=" + id + ", startPos=" + startPos);
        Intent intent = new Intent(ctx, ShowTopicsActivity.class);
        intent.putExtra(ShowTopicsActivity.INTENT_KEY_ID, id);
        intent.putExtra(ShowTopicsActivity.INTENT_KEY_START_POS, startPos);
        ctx.startActivity(intent);
    }

    public static final void openShowPostsActivity(Context ctx, int id, int startPos) {
        logDebug("Starting ShowPostsActivity, id=" + id + ", startPos=" + startPos);
        Intent intent = new Intent(ctx, ShowPostsActivity.class);
        intent.putExtra(ShowPostsActivity.INTENT_KEY_ID, id);
        intent.putExtra(ShowPostsActivity.INTENT_KEY_START_POS, startPos);
        ctx.startActivity(intent);
    }

    public static final void openLoginPageActivity(Context ctx) {
        logDebug("Starting LoginPageActivity");
        ctx.startActivity(new Intent(ctx, LoginPageActivity.class));
    }

    public static final void openLoginActivity(Context ctx) {
        logDebug("Starting LoginActivity");
        ctx.startActivity(new Intent(ctx, LoginActivity.class));
    }

    // - - -

    private static final void logDebug(String msg) {
        HelperUtil.generalDebug("ActivityUtil", msg);
    }


    private static final void logError(String msg) {
        HelperUtil.generalError("ActivityUtil", msg);
    }
}
