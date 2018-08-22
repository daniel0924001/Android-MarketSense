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
import com.idroi.marketsense.R;
import com.idroi.marketsense.util.URLSpanNoUnderline;

/**
 * Created by daniel.hsieh on 2018/4/19.
 */

public class MarketSenseRendererHelper {

    public static void setBackgroundColor(@Nullable final View view,
                                          final int backgroundColor) {
        if (view == null) {
            MSLog.w("Attempted to add color to null view.");
            return;
        }

        view.setBackgroundColor(view.getContext().getResources().getColor(backgroundColor));
    }

    public static void addTextViewWithColorAndIcon(@Nullable final TextView textView,
                                                   @Nullable final String contents,
                                                   final int textColor,
                                                   final int iconId) {
        if (textView == null) {
            MSLog.w("Attempted to add text (" + contents + ") to null TextView.");
            return;
        }

        textView.setCompoundDrawablesWithIntrinsicBounds(iconId, 0, 0, 0);
        addTextViewWithColor(textView, contents, textColor);
    }

    public static void addTextViewWithIcon(@Nullable final TextView textView,
                                                   @Nullable final String contents,
                                                   final int iconId) {
        if (textView == null) {
            MSLog.w("Attempted to add text (" + contents + ") to null TextView.");
            return;
        }

        textView.setCompoundDrawablesWithIntrinsicBounds(iconId, 0, 0, 0);
        addTextView(textView, contents);
    }

    public static void addTextViewWithColor(@Nullable final TextView textView,
                                            @Nullable final String contents,
                                            final int textColor) {
        if (textView == null) {
            MSLog.w("Attempted to add text (" + contents + ") to null TextView.");
            return;
        }

        textView.setTextColor(textView.getContext().getResources().getColor(textColor));
        addTextView(textView, contents);
    }

    public static void addTextViewWithAutoColor(@Nullable final TextView textView,
                                                @Nullable final String contents,
                                                float value, float baseline) {
        if(value > baseline) {
            addTextViewWithColor(textView, contents, R.color.trend_red);
        } else if(value < baseline) {
            addTextViewWithColor(textView, contents, R.color.trend_green);
        } else {
            addTextViewWithColor(textView, contents, R.color.draw_grey);
        }
    }

    public static void addTextView(@Nullable final TextView textView,
                                   @Nullable final String contents) {
        if (textView == null) {
            MSLog.w("Attempted to add text (" + contents + ") to null TextView.");
            return;
        }

        // Clear previous value
        textView.setText(null);

        if (contents == null) {
            textView.setVisibility(View.INVISIBLE); // 20161203 Ansel: to hide CTA button and text view without content
            MSLog.w("Attempted to set TextView contents to null.");
        } else {
            textView.setVisibility(View.VISIBLE); // 20161203 Ansel: to display CTA button and text view with content
            textView.setText(contents);
        }
    }

    public static void addHtmlToTextView(@Nullable final TextView textView,
                                         @Nullable final String html) {
        if (textView == null) {
            MSLog.w("Attempted to add text (" + html + ") to null TextView.");
            return;
        }

        textView.setText(null);

        if (html == null) {
            textView.setVisibility(View.INVISIBLE);
            MSLog.w("Attempted to set TextView contents to null.");
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
        addNumberStringToTextView(textView, contents, defaultContents, 0, 0);
    }

    public static void addNumberStringToTextView(@Nullable final TextView textView,
                                                 @Nullable final String contents,
                                                 final String defaultContents,
                                                 float value, float baseline) {
        if (textView == null) {
            MSLog.w("Attempted to add text (" + contents + ") to null TextView.");
            return;
        }

        // Clear previous value
        textView.setText(null);

        if (contents == null) {
            textView.setVisibility(View.INVISIBLE); // 20161203 Ansel: to hide CTA button and text view without content
            MSLog.w("Attempted to set TextView contents to null.");
        } else {
            textView.setVisibility(View.VISIBLE); // 20161203 Ansel: to display CTA button and text view with content
            if(contents.equals("NaN")) {
                addTextViewWithColor(textView, defaultContents, R.color.text_black);
            } else {
                if(value > baseline) {
                    addTextViewWithColor(textView, contents, R.color.trend_red);
                } else if(value < baseline) {
                    addTextViewWithColor(textView, contents, R.color.trend_green);
                } else {
                    addTextViewWithColor(textView, contents, R.color.draw_grey);
                }
            }
        }
    }
}
