package com.idroi.marketsense;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.adapter.SettingAdapter;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by daniel.hsieh on 2018/4/27.
 */

public class SettingActivity extends AppCompatActivity {

    private int[] mStringIds = {
            R.string.preference_notification, // fake
            R.string.preference_notification,
            R.string.preference_category,
            R.string.preference_share,
            R.string.preference_line,
            R.string.preference_feedback,
            R.string.preference_star,
            R.string.preference_copyright,
            R.string.preference_about
    };

    private Integer[] mDrawableIds = {
            R.drawable.setting_notification, // fake
            R.drawable.setting_notification,
            R.drawable.setting_category,
            R.drawable.setting_share,
            R.mipmap.line_logo,
            R.drawable.setting_feedback,
            R.drawable.setting_star,
            R.drawable.setting_copyright,
            R.drawable.setting_about,
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        setListView();
        setActionBar();
    }

    private void setListView() {
        ListView listView = (ListView) findViewById(R.id.setting_listview);

        final ArrayList<String> list = new ArrayList<>();
        for (int stringId : mStringIds) {
            list.add(getResources().getString(stringId));
        }

        final ArrayList<Integer> list2 = new ArrayList<>();
        list2.addAll(Arrays.asList(mDrawableIds));

        SettingAdapter settingAdapter = new SettingAdapter(this, list, list2);
        listView.setAdapter(settingAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                handleListClick(position);
            }
        });
    }

    private void handleListClick(int position) {
        if(position <= 0 || position >= mStringIds.length) {
            return;
        }
        int id = mStringIds[position];
        switch (id) {
            case R.string.preference_about:
                MSLog.e("this is about");
                break;
            default:
                MSLog.e("this is not about");
        }
    }

    private void setActionBar() {
        final ActionBar actionBar = getSupportActionBar();

        if(actionBar != null) {
            View view = LayoutInflater.from(actionBar.getThemedContext())
                    .inflate(R.layout.main_action_bar, null);

            ImageView imageView = view.findViewById(R.id.action_bar_avatar);
            if(imageView != null) {
                imageView.setImageResource(R.drawable.ic_keyboard_backspace_white_24px);
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        onBackPressed();
                    }
                });
            }

            TextView textView = view.findViewById(R.id.action_bar_name);
            if(textView != null) {
                textView.setText(getResources().getText(R.string.activity_news_setting));
            }

            actionBar.setDisplayShowHomeEnabled(false);
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setCustomView(view,
                    new ActionBar.LayoutParams(
                            ActionBar.LayoutParams.MATCH_PARENT,
                            ActionBar.LayoutParams.MATCH_PARENT));
            actionBar.setDisplayShowCustomEnabled(true);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.stop, R.anim.right_to_left);
    }
}
