package com.idroi.marketsense.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by daniel.hsieh on 2018/4/19.
 */

public interface MarketSenseRenderer <T> {

    View createView(@NonNull Context context, @Nullable ViewGroup parent);

    void renderView(@NonNull View view, @NonNull T content);
}
