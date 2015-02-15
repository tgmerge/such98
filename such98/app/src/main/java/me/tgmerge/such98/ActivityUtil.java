package me.tgmerge.such98;

import android.content.Context;
import android.content.Intent;

/**
 * Created by tgmerge on 2/15.
 */
public class ActivityUtil {


    static final void openShowBoardsActivity(Context ctx, int id, int startPos, String title) {
        logDebug("Starting ShowBoardsActivity, id=" + id + ", startPos=" + startPos + ", title=" + title);
        Intent intent = new Intent(ctx, ShowBoardsActivity.class);
        intent.putExtra(ShowBoardsActivity.INTENT_KEY_ID, id);
        intent.putExtra(ShowBoardsActivity.INTENT_KEY_STARTPOS, startPos);
        intent.putExtra(ShowBoardsActivity.INTENT_KEY_TITLE, title);
        ctx.startActivity(intent);
    }


    static final void openShowTopicsActivity(Context ctx, int id, int startPos, String title) {
        logDebug("Starting ShowTopicsActivity, id=" + id + ", startPos=" + startPos + ", title=" + title);
        Intent intent = new Intent(ctx, ShowTopicsActivity.class);
        intent.putExtra(ShowTopicsActivity.INTENT_KEY_ID, id);
        intent.putExtra(ShowTopicsActivity.INTENT_KEY_STARTPOS, startPos);
        intent.putExtra(ShowTopicsActivity.INTENT_KEY_TITLE, title);
        ctx.startActivity(intent);
    }


    static final void openShowPostsActivity(Context ctx, int id, int startPos, String title) {
        logDebug("Starting ShowPostsActivity, id=" + id + ", startPos=" + startPos + ", title=" + title);
        Intent intent = new Intent(ctx, ShowPostsActivity.class);
        intent.putExtra(ShowPostsActivity.INTENT_KEY_ID, id);
        intent.putExtra(ShowPostsActivity.INTENT_KEY_STARTPOS, startPos);
        intent.putExtra(ShowPostsActivity.INTENT_KEY_TITLE, title);
        ctx.startActivity(intent);
    }

    private static final void logDebug(String msg) {
        HelperUtil.generalDebug("ActivityUtil", msg);
    }


    private static final void logError(String msg) {
        HelperUtil.generalError("ActivityUtil", msg);
    }
}
