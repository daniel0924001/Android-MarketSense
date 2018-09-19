package com.idroi.marketsense;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.idroi.marketsense.Logging.MSLog;

/**
 * Created by daniel.hsieh on 2018/9/18.
 */

public class StockKnowledgeListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_knowledge_list);

        setActionBar();
    }

    private void setActionBar() {
        final ActionBar actionBar = getSupportActionBar();

        if(actionBar != null) {
            actionBar.setElevation(0);
            actionBar.setBackgroundDrawable(
                    getDrawable(R.drawable.action_bar_background_with_border));
            View view = LayoutInflater.from(actionBar.getThemedContext())
                    .inflate(R.layout.action_bar_right_image, null);

            ImageView imageView = view.findViewById(R.id.action_bar_back);
            if(imageView != null) {
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        onBackPressed();
                    }
                });
            }

            TextView titleTextView = view.findViewById(R.id.action_bar_title);
            if(titleTextView != null) {
                titleTextView.setText(R.string.preference_knowledge);
            }

            ImageView searchImageView = view.findViewById(R.id.action_bar_action);
            if(searchImageView != null) {
                searchImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        MSLog.e("search~~~");
                    }
                });
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
}
