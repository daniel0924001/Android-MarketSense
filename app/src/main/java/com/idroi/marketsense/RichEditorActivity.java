package com.idroi.marketsense;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.common.ClientData;
import com.idroi.marketsense.common.FrescoHelper;
import com.idroi.marketsense.data.News;
import com.idroi.marketsense.data.PostEvent;
import com.idroi.marketsense.data.Stock;
import com.idroi.marketsense.viewholders.NewsReferencedByCommentViewHolder;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.wasabeef.richeditor.RichEditor;

import static com.idroi.marketsense.SearchAndResponseActivity.EXTRA_SEARCH_TYPE;
import static com.idroi.marketsense.SearchAndResponseActivity.EXTRA_SELECTED_COMPANY_CODE_KEY;
import static com.idroi.marketsense.SearchAndResponseActivity.EXTRA_SELECTED_COMPANY_NAME_KEY;
import static com.idroi.marketsense.SearchAndResponseActivity.SEARCH_CODE_ONLY;
import static com.idroi.marketsense.common.Constants.STOCK_CODE_DEEP_LINK;

/**
 * Created by daniel.hsieh on 2018/5/8.
 */

public class RichEditorActivity extends AppCompatActivity {

    public final static int sSearchAndTagRequestCode = 1;

    RichEditor mEditor;
//    String mEditorString;

    public static final String EXTRA_REQ_TYPE = "extra_type";
    public static final String EXTRA_REQ_ID = "extra_id";
    public static final String EXTRA_REQ_STOCK_KEYWORDS = "extra_stock_keywords";
    public static final String EXTRA_REQ_NEWS_TITLE = "extra_news_title";
    public static final String EXTRA_REQ_NEWS_LEVEL = "extra_news_level";
    public static final String EXTRA_RES_HTML = "extra_response_html";
    public static final String EXTRA_RES_TYPE = EXTRA_REQ_TYPE;
    public static final String EXTRA_RES_ID = EXTRA_REQ_ID;
    public static final String EXTRA_RES_EVENT_ID = "extra_event_id";
    public final static int sEditorRequestCode = 2;
    public final static int sReplyEditorRequestCode = 3;

    private String mType;
    private String mId;
    private String[] mStockKeywords;
    private boolean mDoubleClickBack = false;
    private AlertDialog mImageAlertDialog;
    private AlertDialog mUrlAlertDialog;

    private TextView mCompletedTextView;

    private String mNewsTitle;
    private int mNewsLevel;

    public enum TYPE {
        NEWS("news"),
        STOCK("stock"),
        REPLY("reply"),
        NO_CONTENT("no_content");

        private String mType;

        TYPE(String type) {
            mType = type;
        }

        public String getType() {
            return mType;
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrescoHelper.initialize(getApplicationContext());
        setContentView(R.layout.activity_rich_editor);

        setInformation();
        setActionBar();
        setRichEditor();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if(mImageAlertDialog != null) {
            mImageAlertDialog.dismiss();
            mImageAlertDialog = null;
        }

        if(mUrlAlertDialog != null) {
            mUrlAlertDialog.dismiss();
            mUrlAlertDialog = null;
        }
    }

    private void setInformation() {
        Intent intent = getIntent();
        mId = intent.getStringExtra(EXTRA_REQ_ID);
        mType = intent.getStringExtra(EXTRA_REQ_TYPE);
        mStockKeywords = intent.getStringArrayExtra(EXTRA_REQ_STOCK_KEYWORDS);

        mNewsTitle = intent.getStringExtra(EXTRA_REQ_NEWS_TITLE);
        mNewsLevel = intent.getIntExtra(EXTRA_REQ_NEWS_LEVEL, 0);
    }

    private void leaveRichEditorActivity(boolean isSuccessful, String html, String eventId) {
        if(isSuccessful) {
            Intent intent = new Intent();
            intent.putExtra(EXTRA_RES_HTML, html);
            intent.putExtra(EXTRA_RES_TYPE, mType);
            intent.putExtra(EXTRA_RES_ID, mId);
            if(eventId != null) {
                intent.putExtra(EXTRA_RES_EVENT_ID, eventId);
            }
            setResult(RESULT_OK, intent);
            finish();
            overridePendingTransition(R.anim.stop, R.anim.right_to_left);
        } else {
            mCompletedTextView.setClickable(true);
            Toast.makeText(this, R.string.send_comment_fail, Toast.LENGTH_SHORT).show();
        }
    }

    private void setRichEditor() {
        mEditor = (RichEditor) findViewById(R.id.rich_editor);
        mEditor.loadCSS("file:///android_asset/img.css");
        mEditor.setEditorHeight(200);
        mEditor.setEditorFontSize(22);
        mEditor.setEditorFontColor(getResources().getColor(R.color.text_first));
        //mEditor.setEditorBackgroundColor(Color.BLUE);
        //mEditor.setBackgroundColor(Color.BLUE);
        //mEditor.setBackgroundResource(R.drawable.bg);
        mEditor.setPadding(16, 16, 16, 16);
        //    mEditor.setBackground("https://raw.githubusercontent.com/wasabeef/art/master/chip.jpg");
        mEditor.setPlaceholder(getResources().getString(R.string.comment_hint));

        TextView tagStockTextView = findViewById(R.id.edit_tag_stock);
        tagStockTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RichEditorActivity.this, SearchAndResponseActivity.class);
                intent.putExtra(EXTRA_SEARCH_TYPE, SEARCH_CODE_ONLY);
                startActivityForResult(intent, sSearchAndTagRequestCode);
                overridePendingTransition(0, 0);
            }
        });

        if(mType.equals(TYPE.STOCK.getType())) {
            Stock stock = ClientData.getInstance(this).getPriceFromCode(mId);
            if(stock != null) {
                insertLink(stock.getCode(), stock.getName());
            }
        } else if(mType.equals(TYPE.NEWS.getType())) {
            for (String name: mStockKeywords) {
                String code = ClientData.getInstance(this).getCodeFromName(name);
                if(code != null) {
                    insertLink(code, name);
                }
            }

            NewsReferencedByCommentViewHolder newsReferencedByCommentViewHolder =
                    NewsReferencedByCommentViewHolder.convertToViewHolder(findViewById(R.id.comment_news_block));
            newsReferencedByCommentViewHolder.update(this, mNewsTitle, mStockKeywords, mNewsLevel);

            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) mEditor.getLayoutParams();
            params.bottomToTop = R.id.comment_news_block;
            params.bottomToBottom = 0;
        }
    }

    private void setActionBar() {
        final ActionBar actionBar = getSupportActionBar();

        if(actionBar != null) {
            actionBar.setElevation(0);
            actionBar.setBackgroundDrawable(
                    getDrawable(R.drawable.action_bar_background_with_border));
            View view = LayoutInflater.from(actionBar.getThemedContext())
                    .inflate(R.layout.action_bar_right_text, null);

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
                if(mType.equals(TYPE.REPLY.getType())) {
                    titleTextView.setText(getResources().getText(R.string.reply_comment));
                } else {
                    titleTextView.setText(getResources().getText(R.string.publish_comment));
                }
            }

            mCompletedTextView = view.findViewById(R.id.action_bar_complete);
            if(mCompletedTextView != null) {
                mCompletedTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        completeEditComment();
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

    private void completeEditComment() {
        final String html = mEditor.getHtml();
        if(html == null) {
            Toast.makeText(RichEditorActivity.this,
                    R.string.title_comment_create_null, Toast.LENGTH_SHORT).show();
            return;
        }
        mCompletedTextView.setClickable(false);
        ArrayList<String> tags = parseStockCodeTags(html);
        if(mType.equals(TYPE.NEWS.getType())) {
            MSLog.i("send news comment (" + mId + "): " + html);
            PostEvent.sendNewsComment(RichEditorActivity.this, mId, html, tags, new PostEvent.PostEventListener() {
                @Override
                public void onResponse(boolean isSuccessful, Object data) {
                    if(data instanceof String) {
                        // we have to get event id
                        leaveRichEditorActivity(isSuccessful, html, (String) data);
                    } else {
                        leaveRichEditorActivity(false, html, null);
                    }
                }
            });
        } else if(mType.equals(TYPE.STOCK.getType())) {
            MSLog.i("send stock comment (" + mId + "): " + html);
            PostEvent.sendStockComment(RichEditorActivity.this, mId, html, tags, new PostEvent.PostEventListener() {
                @Override
                public void onResponse(boolean isSuccessful, Object data) {
                    if(data instanceof String) {
                        leaveRichEditorActivity(isSuccessful, html, (String) data);
                    } else {
                        leaveRichEditorActivity(false, html, null);
                    }
                }
            });
        } else if(mType.equals(TYPE.REPLY.getType())) {
            MSLog.i("send reply comment (" + mId + "): " + html);
            PostEvent.sendReplyComment(RichEditorActivity.this, mId, html, tags);
            leaveRichEditorActivity(true, html, null);
        } else if(mType.equals(TYPE.NO_CONTENT.getType())) {
            MSLog.i("send no content comment: " + html);
            PostEvent.sendNoContentComment(RichEditorActivity.this, html, tags, new PostEvent.PostEventListener() {
                @Override
                public void onResponse(boolean isSuccessful, Object data) {
                    if(data instanceof String) {
                        leaveRichEditorActivity(isSuccessful, html, (String) data);
                    } else {
                        leaveRichEditorActivity(false, html, null);
                    }
                }
            });
        }
    }

    private ArrayList<String> parseStockCodeTags(String html) {
        ArrayList<String> tags = new ArrayList<>();
        Pattern pattern = Pattern.compile("marketsense:\\/\\/open.marketsense.app\\/stock\\/code\\/(\\d+)");
        Matcher matcher = pattern.matcher(html);
        while (matcher.find()) {
            String code = matcher.group(1);
            tags.add(code);
            MSLog.d("find tag: " + code);
        }
        return tags;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == sSearchAndTagRequestCode) {
            if (resultCode == RESULT_OK) {
                String name = data.getStringExtra(EXTRA_SELECTED_COMPANY_NAME_KEY);
                String code = data.getStringExtra(EXTRA_SELECTED_COMPANY_CODE_KEY);
                MSLog.d("tag stock name: " + name + ", code: " + code);
                insertLink(code, name);
            }
        }
    }

    @Override
    public void onBackPressed() {
        if(mDoubleClickBack || mEditor.getHtml() == null || mEditor.getHtml().isEmpty()) {
            super.onBackPressed();
            overridePendingTransition(R.anim.stop, R.anim.right_to_left);
            return;
        }

        mDoubleClickBack = true;
        Toast.makeText(this,
                getResources().getString(R.string.title_leave_rich_editor), Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mDoubleClickBack = false;
            }
        }, 2000);
    }

    @Override
    protected void onDestroy() {
        mEditor.destroy();
        mEditor = null;
        super.onDestroy();
    }

    private void insertLink(String code, String name) {
        if(mEditor != null) {
            String href = String.format(STOCK_CODE_DEEP_LINK, code);
            mEditor.focusEditor();
            mEditor.insertLink(href, "$" + name);
        }
    }

    public static Intent generateRichEditorActivityIntent(Context context, TYPE type, String id) {
        return generateRichEditorActivityIntent(context, type, id, null, null, 0);
    }

    public static Intent generateRichEditorActivityIntent(Context context, TYPE type, String id, String[] stockKeywords, String title, int level) {
        Intent intent = new Intent(context, RichEditorActivity.class);
        intent.putExtra(EXTRA_REQ_TYPE, type.getType());
        intent.putExtra(EXTRA_REQ_ID, id);
        intent.putExtra(EXTRA_REQ_STOCK_KEYWORDS, stockKeywords);
        intent.putExtra(EXTRA_REQ_NEWS_TITLE, title);
        intent.putExtra(EXTRA_REQ_NEWS_LEVEL, level);

        return intent;
    }
}
