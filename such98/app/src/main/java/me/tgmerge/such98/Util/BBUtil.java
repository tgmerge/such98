package me.tgmerge.such98.Util;

import android.content.Context;
import android.text.Html;
import android.text.Spanned;
import android.util.Pair;
import android.widget.TextView;
import java.util.Vector;

/**
 * Created by tgmerge on 2/17.
 * Provides methods to render BBCode(to html, on TextView, load images, etc)
 */
public class BBUtil {

    public static final void setBBcodeToTextView(Context ctx, TextView textView, String bb) {
        String html = bb2html(bb);
        //Spanned spanned = Html.fromHtml(html, new UILImageGetter(textView, ctx), null); todo  UILgetter not working
        Spanned spanned = Html.fromHtml(html, null, null);
        textView.setText(spanned);
    }

    private static final Vector<Pair<String, String>> bbMap = new Vector<>();

    static {
        bbMap.add(new Pair<>("(\r\n|\r|\n|\n\r)", "<br/>"));
        bbMap.add(new Pair<>("\\[b\\](.+?)\\[/b\\]", "<strong>$1</strong>"));
        bbMap.add(new Pair<>("\\[i\\](.+?)\\[/i\\]", "<span style='font-style:italic;'>$1</span>"));
        bbMap.add(new Pair<>("\\[u\\](.+?)\\[/u\\]", "<span style='text-decoration:underline;'>$1</span>"));
        bbMap.add(new Pair<>("\\[h1\\](.+?)\\[/h1\\]", "<h1>$1</h1>"));
        bbMap.add(new Pair<>("\\[h2\\](.+?)\\[/h2\\]", "<h2>$1</h2>"));
        bbMap.add(new Pair<>("\\[h3\\](.+?)\\[/h3\\]", "<h3>$1</h3>"));
        bbMap.add(new Pair<>("\\[h4\\](.+?)\\[/h4\\]", "<h4>$1</h4>"));
        bbMap.add(new Pair<>("\\[h5\\](.+?)\\[/h5\\]", "<h5>$1</h5>"));
        bbMap.add(new Pair<>("\\[h6\\](.+?)\\[/h6\\]", "<h6>$1</h6>"));
        bbMap.add(new Pair<>("\\[quote\\](.+?)\\[/quote\\]", "<blockquote>$1</blockquote>"));
        bbMap.add(new Pair<>("\\[p\\](.+?)\\[/p\\]", "<p>$1</p>"));
        bbMap.add(new Pair<>("\\[p=(.+?),(.+?)\\](.+?)\\[/p\\]", "<p style='text-indent:$1px;line-height:$2%;'>$3</p>"));
        bbMap.add(new Pair<>("\\[center\\](.+?)\\[/center\\]", "<div align='center'>$1"));
        bbMap.add(new Pair<>("\\[align=(.+?)\\](.+?)\\[/align\\]", "<div align='$1'>$2"));
        bbMap.add(new Pair<>("\\[size=(.+?)\\](.+?)\\[/size\\]", "<span style='font-size:$1;'>$2</span>"));
        //bbMap.add(new Pair<>("\\[img=(.+?),(.+?)\\](.+?)\\[/img\\]", "<img width='$1' height='$2' src='$3' />"));
        bbMap.add(new Pair<>("\\[email\\](.+?)\\[/email\\]", "<a href='mailto:$1'>$1</a>"));
        bbMap.add(new Pair<>("\\[email=(.+?)\\](.+?)\\[/email\\]", "<a href='mailto:$1'>$2</a>"));
        bbMap.add(new Pair<>("\\[url\\](.+?)\\[/url\\]", "<a href='$1'>$1</a>"));
        bbMap.add(new Pair<>("\\[url=(.+?)\\](.+?)\\[/url\\]", "<a href='$1'>$2</a>"));
        //bbMap.add(new Pair<>("\\[youtube\\](.+?)\\[/youtube\\]", "<object width='640' height='380'><param name='movie' value='http://www.youtube.com/v/$1'></param><embed src='http://www.youtube.com/v/$1' type='application/x-shockwave-flash' width='640' height='380'></embed></object>"));
        //bbMap.add(new Pair<>("\\[video\\](.+?)\\[/video\\]", "<video src='$1' />"));

        // todo image laoder is buggy
        bbMap.add(new Pair<>("\\[upload(=bmp|=png|=gif|=jpg)?(,\\d)\\](.+?)\\[/upload\\]", "<img src='$3' />"));
        bbMap.add(new Pair<>("\\[img(=\\d)?\\](.+?)\\[/img\\]", "<img src='$2' />"));

        bbMap.add(new Pair<>("\\[font=(.+?)\\](.+?)\\[/font\\]", "<font face='$1'>$2</font>"));
        bbMap.add(new Pair<>("\\[color=(.+?)\\](.+?)\\[/color\\]", "<font color=$1>$2</font>"));
        bbMap.add(new Pair<>("\\[quotex\\](.+?)\\[/quotex\\]", "<blockquote>$1</blockquote>"));
        bbMap.add(new Pair<>("\\[right\\](.+?)\\[/right\\]", "<div align='right'>$1"));
        bbMap.add(new Pair<>("\\[em\\d+\\]", "[喵]"));
    }

    private static final String bb2html(String text) {
        String html = text;

        for (Pair<String, String> entry : bbMap) {
            html = html.replaceAll(entry.first, entry.second);
        }

        return html;
    }
}
