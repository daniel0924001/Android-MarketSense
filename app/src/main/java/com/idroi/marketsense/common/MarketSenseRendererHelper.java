package com.idroi.marketsense.common;

import android.support.annotation.Nullable;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.view.View;
import android.widget.TextView;

import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.util.URLSpanNoUnderline;

/**
 * Created by daniel.hsieh on 2018/4/19.
 */

public class MarketSenseRendererHelper {

    public static void addTextView(@Nullable final TextView textView,
                                   @Nullable final String contents) {
        if (textView == null) {
            MSLog.v("Attempted to add text (" + contents + ") to null TextView.");
            return;
        }

        // Clear previous value
        textView.setText(null);

        if (contents == null) {
            textView.setVisibility(View.INVISIBLE); // 20161203 Ansel: to hide CTA button and text view without content
            MSLog.v("Attempted to set TextView contents to null.");
        } else {
            textView.setVisibility(View.VISIBLE); // 20161203 Ansel: to display CTA button and text view with content
            textView.setText(contents);
        }
    }

    public static void addHtmlToTextView(@Nullable final TextView textView,
                                         @Nullable final String html) {
        if (textView == null) {
            MSLog.v("Attempted to add text (" + html + ") to null TextView.");
            return;
        }

        textView.setText(null);

        if (html == null) {
            textView.setVisibility(View.INVISIBLE);
            MSLog.v("Attempted to set TextView contents to null.");
        } else {
            textView.setVisibility(View.VISIBLE);
            // https://stackoverflow.com/questions/37899856/html-fromhtml-is-deprecated-what-is-the-alternative
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                textView.setText(Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY));
            } else {
                textView.setText(Html.fromHtml(html));
            }
            try {
                removeUnderlines(textView);
            } catch (Exception exception) {
                MSLog.e("Exception in removeUnderlines: " + exception);
            }
            textView.setMovementMethod(LinkMovementMethod.getInstance());
        }
    }

    private static void removeUnderlines(TextView textView) {
        SpannableString s = new SpannableString(textView.getText());
        URLSpan[] spans = s.getSpans(0, s.length(), URLSpan.class);

        for(URLSpan span:spans) {
            int start = s.getSpanStart(span);
            int end = s.getSpanEnd(span);
            s.removeSpan(span);
            span = new URLSpanNoUnderline(span.getURL());
            s.setSpan(span, start, end, 0);
        }
        textView.setText(s);
    }

    public static void addNumberStringToTextView(@Nullable final TextView textView,
                                                 @Nullable final String contents,
                                                 final String defaultContents) {
        if (textView == null) {
            MSLog.v("Attempted to add text (" + contents + ") to null TextView.");
            return;
        }

        // Clear previous value
        textView.setText(null);

        if (contents == null) {
            textView.setVisibility(View.INVISIBLE); // 20161203 Ansel: to hide CTA button and text view without content
            MSLog.v("Attempted to set TextView contents to null.");
        } else {
            textView.setVisibility(View.VISIBLE); // 20161203 Ansel: to display CTA button and text view with content
            if(contents.equals("NaN")) {
                textView.setText(defaultContents);
            } else {
                textView.setText(contents);
            }
        }
    }
}
