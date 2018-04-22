package com.idroi.marketsense.common;

import com.facebook.datasource.DataSource;

/**
 * Created by daniel.hsieh on 2018/4/22.
 */

public interface FrescoImageListener {
    void onResponse(String imageUrl, DataSource<Void> dataSource, boolean isImmediate);
}
