package com.idroi.marketsense.common;

import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import com.idroi.marketsense.Logging.MSLog;

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
}
