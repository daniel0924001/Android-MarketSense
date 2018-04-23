package com.idroi.marketsense.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;

import com.idroi.marketsense.data.Stock;

import java.lang.ref.WeakReference;
import java.util.WeakHashMap;

/**
 * Created by daniel.hsieh on 2018/4/23.
 */

public class StockListRenderer implements MarketSenseRenderer<Stock>{

    @Override
    public View createView(@NonNull Context context, @Nullable ViewGroup parent) {
        return null;
    }

    @Override
    public void renderView(@NonNull View view, @NonNull Stock content) {

    }
}
