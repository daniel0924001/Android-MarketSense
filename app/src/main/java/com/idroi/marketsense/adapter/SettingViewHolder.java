package com.idroi.marketsense.adapter;

import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.R;

/**
 * Created by daniel.hsieh on 2018/4/30.
 */

public class SettingViewHolder {

    View mainView;
    TextView titleView;
    @Nullable ImageView iconView;
    @Nullable Switch switchView;
    @Nullable ImageView gotoView;
    @Nullable View divider;

    static final SettingViewHolder EMPTY_VIEW_HOLDER = new SettingViewHolder();

    private SettingViewHolder() {}

    static SettingViewHolder convertToViewHolder(final View view) {
        final SettingViewHolder settingViewHolder = new SettingViewHolder();
        settingViewHolder.mainView = view.findViewById(R.id.setting_item);
        try {
            settingViewHolder.titleView = view.findViewById(R.id.setting_title_tv);
            settingViewHolder.iconView = view.findViewById(R.id.setting_icon_iv);
            settingViewHolder.switchView = view.findViewById(R.id.setting_action_switch);
            settingViewHolder.gotoView = view.findViewById(R.id.setting_action_goto);
            settingViewHolder.divider = view.findViewById(R.id.divider);
            return settingViewHolder;
        } catch (ClassCastException exception) {
            MSLog.e(exception.toString());
            return EMPTY_VIEW_HOLDER;
        }
    }
}
