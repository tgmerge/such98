package me.tgmerge.such98.Util;

import android.content.Context;
import android.content.Intent;

import me.tgmerge.such98.ShowBoardsActivity;
import me.tgmerge.such98.ShowPostsActivity;
import me.tgmerge.such98.ShowTopicsActivity;

/**
 * Created by tgmerge on 2/15.
 */
public final class ActivityUtil {


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

    private static final void logDebug(String msg) {
        HelperUtil.generalDebug("ActivityUtil", msg);
    }


    private static final void logError(String msg) {
        HelperUtil.generalError("ActivityUtil", msg);
    }
}
