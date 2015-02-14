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
        intent.putExtra(ShowBoardsActivity.INTENT_ID, id);
        intent.putExtra(ShowBoardsActivity.INTENT_STARTPOS, startPos);
        intent.putExtra(ShowBoardsActivity.INTENT_TITLE, title);
        ctx.startActivity(intent);
    }


    static final void openShowTopicsActivity(Context ctx, int id, int startPos, String title) {
        logDebug("Starting ShowTopicsActivity, id=" + id + ", startPos=" + startPos + ", title=" + title);
        Intent intent = new Intent(ctx, ShowTopicsActivity.class);
        intent.putExtra(ShowTopicsActivity.INTENT_ID, id);
        intent.putExtra(ShowTopicsActivity.INTENT_STARTPOS, startPos);
        intent.putExtra(ShowTopicsActivity.INTENT_TITLE, title);
        ctx.startActivity(intent);
    }


    private static final void logDebug(String msg) {
        HelperUtil.generalDebug("ActivityUtil", msg);
    }


    private static final void logError(String msg) {
        HelperUtil.generalError("ActivityUtil", msg);
    }
}
