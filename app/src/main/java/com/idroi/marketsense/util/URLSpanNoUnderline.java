package com.idroi.marketsense.util;

import android.text.TextPaint;
import android.text.style.URLSpan;

import com.idroi.marketsense.Logging.MSLog;

/**
 * Created by daniel.hsieh on 2018/8/8.
 */

public class URLSpanNoUnderline extends URLSpan {

    public URLSpanNoUnderline(String s) {
        super(s);
    }

    @Override
    public void updateDrawState(TextPaint textPaint) {
        super.updateDrawState(textPaint);
        textPaint.setUnderlineText(false);
    }
}
