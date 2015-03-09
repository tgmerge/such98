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
            HelperUtil.errorToast("未知类型的URI: " + uri.toString());
            return;
        }

        // Remove such98:// protocol in the beginning
        uriStr = uriStr.replaceFirst("such98://", "");

        if (uriStr.startsWith("url/")) {
            uriStr = uriStr.replaceFirst("^url/", "");
            HelperUtil.generalDebug("IntentFilterActivity", "type is url, " + uriStr);
            processUri_url(uriStr);
            return;
        }

        if (uriStr.startsWith("img/")) {
            uriStr = uriStr.replaceFirst("^img/", "");
            HelperUtil.generalDebug("IntentFilterActivity", "type is img, " + uriStr);
            processUri_img(uriStr);
            return;
        }

        HelperUtil.errorToast("解析错误的URI: " + uri.toString());
    }

    private void processUri_img(String url) {
        String urlStr = completeUrl(url);

        ActivityUtil.openViewImageActivity(this, urlStr);
    }

    private void processUri_url(String url) {
        String urlStr = completeUrl(url);

        // Case 1
        // URL point to a highlighted post in a topic:
        //      http://www.cc98.org/dispbbs.asp?boardid=537&id=4485771&star=252#7
        //      /dispbbs.asp?boardid=537&id=4485771&star=252#7
        //      dispbbs.asp?boardid=537&id=4485771&star=252#7
        // find id=xxx, star=xxx, #xxx
        // (at least 'id')
        // will be processed in app, opening new ShowPostsActivity
        if (urlStr.matches("(?i)http://www\\.cc98\\.org/dispbbs\\.asp\\?.*")) {
            Matcher idMatcher = Pattern.compile("(?i)\\Wid=(\\d+)").matcher(urlStr);
            Integer id = idMatcher.find() ? Integer.parseInt(idMatcher.group(1)) : null;
            Matcher starMatcher = Pattern.compile("(?i)\\Wstar=(\\d+)").matcher(urlStr);
            Integer star = starMatcher.find() ? Integer.parseInt(starMatcher.group(1)) : 1;
            Matcher hashMatcher = Pattern.compile("#(\\d+)").matcher(urlStr);
            Integer hash = hashMatcher.find() ? Integer.parseInt(hashMatcher.group(1)) : 1;

            if (id != null) {
                int startPos = (star - 1) * 10 + (hash - 1);
                HelperUtil.debugToast("URI为指定回复，正在跳转到对应页…");
                ActivityUtil.openShowPostsActivity(this, id, startPos, false);
                return;
            }
        }

        // Case 2:
        // Other http/https URL:
        //      http://www.baidu.com
        //      https://www.baidu.com
        // Start a new intent and throw it out
        if (urlStr.matches("(?i)(http|https)://.*")) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(urlStr));
            startActivity(intent);
            return;
        }

        // Case 3:
        // URI of other protocol:
        //      ftp://www.cc98.org
        //      3p://localhost
        //      ://://
        // Will be dropped, showing an error toast
        HelperUtil.debugToast("未知的URI: " + urlStr);
        return;
    }

    // * no domain&protocol?   www.cc98.org/
    // * no protocol?          http://
    private static final String completeUrl(String str) {

        if (str.matches("/.*")) {
            str = str.replaceFirst("/", "");
        }

        if (str.matches("(?![^.]+?://\\w+\\.).*")) {
            // no domain&protocol
            str = "www.cc98.org/" + str;
        }

        if (str.matches("(?![^.]+?://).*")) {
            // no protocol
            str = "http://" + str;
        }

        return str;
    }
}
