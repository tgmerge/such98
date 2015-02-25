package me.tgmerge.such98.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.tgmerge.such98.R;
import me.tgmerge.such98.util.ActivityUtil;
import me.tgmerge.such98.util.HelperUtil;

public class IntentFilterActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Uri data = getIntent().getData();
        processUri(data);
        finish();
    }

    private void processUri(Uri uri) {

        HelperUtil.generalDebug("IntentFilterActivity", "Get uri=" + uri);

        // Return if not a such98:// URI
        String uriStr = uri.toString();
        if (!uriStr.startsWith("such98://")) {
            HelperUtil.errorToast("未知格式的URI: " + uriStr);
            return;
        }

        // Remove such98:// protocol in the beginning
        uriStr = uriStr.replaceFirst("such98://", "");

        // URI without protocol? Add "http://www.cc98.org/" to the beginning
        uriStr = uriStr.replaceFirst("^(?!(.+?)://)/?", "http://www.cc98.org/");

        // Case 1
        // URI, point to a highlighted post in a topic:
        //      http://www.cc98.org/dispbbs.asp?boardid=537&id=4485771&star=252#7
        //      /dispbbs.asp?boardid=537&id=4485771&star=252#7
        //      dispbbs.asp?boardid=537&id=4485771&star=252#7
        // find id=xxx, star=xxx, #xxx
        // (at least 'id')
        // will be processed in app, opening new ShowPostsActivity
        if (uriStr.matches("http://www\\.cc98\\.org/dispbbs\\.asp\\?.*")) {
            Matcher idMatcher = Pattern.compile("\\Wid=(\\d+)").matcher(uriStr);
            Integer id = idMatcher.find() ? Integer.parseInt(idMatcher.group(1)) : null;
            Matcher starMatcher = Pattern.compile("\\Wstar=(\\d+)").matcher(uriStr);
            Integer star = starMatcher.find() ? Integer.parseInt(starMatcher.group(1)) : 1;
            Matcher hashMatcher = Pattern.compile("#(\\d+)").matcher(uriStr);
            Integer hash = hashMatcher.find() ? Integer.parseInt(hashMatcher.group(1)) : 1;

            if (id != null) {
                int startPos = (star - 1) * 10 + (hash - 1);
                HelperUtil.debugToast("URI为指定回复，正在跳转到对应页…");
                ActivityUtil.openShowPostsActivity(this, id, startPos, false);
                return;
            }
        }

        // Case 2:
        // Other http/https URI:
        //      http://www.baidu.com
        //      https://www.baidu.com
        // Start a new intent and throw it out
        if (uriStr.matches("(http|https)://.*")) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(uriStr));
            startActivity(intent);
            return;
        }

        // Case 3:
        // URI of other protocol:
        //      ftp://www.cc98.org
        //      3p://localhost
        //      ://://
        // Will be dropped, showing an error toast
        HelperUtil.debugToast("未知的URI: " + uriStr);
        return;
    }
}
