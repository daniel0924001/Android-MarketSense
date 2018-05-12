package com.idroi.marketsense;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;
import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.adapter.StockListArrayAdapter;
import com.idroi.marketsense.common.ClientData;

/**
 * Created by daniel.hsieh on 2018/4/27.
 */

public class SearchAndResponseActivity extends AppCompatActivity {

    public static final String EXTRA_SELECTED_COMPANY_NAME_KEY = "extra_selected_company_name";
    public static final String EXTRA_SELECTED_COMPANY_CODE_KEY = "extra_selected_company_code";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        ClientData.getInstance(this);
        StockListArrayAdapter adapter = new StockListArrayAdapter(this,
                R.layout.stock_list_simple_item,
                ClientData.getInstance(this).getAllStocksListInfo());

        final AutoCompleteTextView autoCompleteTextView = findViewById(R.id.stock_search_ctv);
        autoCompleteTextView.setThreshold(1);
        autoCompleteTextView.setAdapter(adapter);

        Button button = findViewById(R.id.search_btn);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String name = autoCompleteTextView.getText().toString();
                String code = ClientData.getInstance().getCodeFromName(name);

                if(!ClientData.getInstance().isNameAndCodeAreValid(name, code)) {
                    code = autoCompleteTextView.getText().toString();
                    name = ClientData.getInstance().getNameFromCode(code);

                    if(!ClientData.getInstance().isNameAndCodeAreValid(name, code)) {
                        Toast.makeText(SearchAndResponseActivity.this,
                                R.string.choice_name_or_code_are_invalid, Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                Intent intent = new Intent();
                intent.putExtra(EXTRA_SELECTED_COMPANY_NAME_KEY, name);
                intent.putExtra(EXTRA_SELECTED_COMPANY_CODE_KEY, code);
                setResult(RESULT_OK, intent);
                finish();
                overridePendingTransition(R.anim.stop, R.anim.right_to_left);
            }
        });

        setActionBar();
    }

    private void setActionBar() {
        final ActionBar actionBar = getSupportActionBar();

        if(actionBar != null) {
            View view = LayoutInflater.from(actionBar.getThemedContext())
                    .inflate(R.layout.main_action_bar, null);

            SimpleDraweeView imageView = view.findViewById(R.id.action_bar_avatar);
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
                textView.setText(getResources().getText(R.string.activity_news_search));
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
        setResult(RESULT_CANCELED);
        overridePendingTransition(R.anim.stop, R.anim.right_to_left);
    }
}
