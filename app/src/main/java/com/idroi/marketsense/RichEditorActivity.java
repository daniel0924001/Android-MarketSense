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
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.common.ClientData;
import com.idroi.marketsense.common.FrescoHelper;
import com.idroi.marketsense.data.PostEvent;
import com.idroi.marketsense.data.Stock;

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
    View mLastPressView;

    public static final String EXTRA_REQ_TYPE = "extra_type";
    public static final String EXTRA_REQ_ID = "extra_id";
    public static final String EXTRA_REQ_STOCK_KEYWORDS = "extra_stock_keywords";
    public static final String EXTRA_REQ_NEWS_TITLE = "extra_news_title";
    public static final String EXTRA_REQ_NEWS_DATE = "extra_news_date";
    public static final String EXTRA_REQ_NEWS_PREDICTION_IMAGE_ID = "extra_news_prediction_image_id";
    public static final String EXTRA_REQ_NEWS_PREDICTION_TEXT_ID = "extra_news_prediction_text";
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
    private String mNewsSourceDate;
    private int mNewsPredictionStringResourceId;
    private int mNewsPredictionBackgroundDrawableResourceId;

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
        mNewsSourceDate = intent.getStringExtra(EXTRA_REQ_NEWS_DATE);
        mNewsPredictionStringResourceId = intent.getIntExtra(EXTRA_REQ_NEWS_PREDICTION_TEXT_ID, 0);
        mNewsPredictionBackgroundDrawableResourceId = intent.getIntExtra(EXTRA_REQ_NEWS_PREDICTION_IMAGE_ID, 0);
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
        mEditor.setEditorFontColor(getResources().getColor(R.color.marketsense_text_black));
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

            ConstraintLayout newsBlock = findViewById(R.id.comment_news_block);
            newsBlock.setVisibility(View.VISIBLE);
            TextView titleTextView = findViewById(R.id.comment_news_title_tv);
            titleTextView.setText(mNewsTitle);
            TextView dateTextView = findViewById(R.id.comment_news_date_tv);
            dateTextView.setText(mNewsSourceDate);
            TextView predictionTextView = findViewById(R.id.comment_news_prediction);
            try {
                predictionTextView.setText(mNewsPredictionStringResourceId);
                predictionTextView.setBackground(getResources().getDrawable(mNewsPredictionBackgroundDrawableResourceId));
            } catch (Exception e) {
                predictionTextView.setVisibility(View.GONE);
                predictionTextView.setVisibility(View.GONE);
            }

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
        return generateRichEditorActivityIntent(context, type, id, null, null, 0, null);
    }

    public static Intent generateRichEditorActivityIntent(Context context, TYPE type, String id, String[] stockKeywords, String title, int level, String date) {
        Intent intent = new Intent(context, RichEditorActivity.class);
        intent.putExtra(EXTRA_REQ_TYPE, type.getType());
        intent.putExtra(EXTRA_REQ_ID, id);
        intent.putExtra(EXTRA_REQ_STOCK_KEYWORDS, stockKeywords);
        intent.putExtra(EXTRA_REQ_NEWS_TITLE, title);
        intent.putExtra(EXTRA_REQ_NEWS_DATE, date);

        switch (level) {
            case 3:
                intent.putExtra(EXTRA_REQ_NEWS_PREDICTION_IMAGE_ID, R.drawable.btn_oval_small_corner_red);
                intent.putExtra(EXTRA_REQ_NEWS_PREDICTION_TEXT_ID, R.string.title_news_good3);
                break;
            case 2:
                intent.putExtra(EXTRA_REQ_NEWS_PREDICTION_IMAGE_ID, R.drawable.btn_oval_small_corner_red);
                intent.putExtra(EXTRA_REQ_NEWS_PREDICTION_TEXT_ID, R.string.title_news_good2);
                break;
            case 1:
                intent.putExtra(EXTRA_REQ_NEWS_PREDICTION_IMAGE_ID, R.drawable.btn_oval_small_corner_red);
                intent.putExtra(EXTRA_REQ_NEWS_PREDICTION_TEXT_ID, R.string.title_news_good1);
                break;
            case -1:
                intent.putExtra(EXTRA_REQ_NEWS_PREDICTION_IMAGE_ID, R.drawable.btn_oval_small_corner_green);
                intent.putExtra(EXTRA_REQ_NEWS_PREDICTION_TEXT_ID, R.string.title_news_bad1);
                break;
            case -2:
                intent.putExtra(EXTRA_REQ_NEWS_PREDICTION_IMAGE_ID, R.drawable.btn_oval_small_corner_green);
                intent.putExtra(EXTRA_REQ_NEWS_PREDICTION_TEXT_ID, R.string.title_news_bad2);
                break;
            case -3:
                intent.putExtra(EXTRA_REQ_NEWS_PREDICTION_IMAGE_ID, R.drawable.btn_oval_small_corner_green);
                intent.putExtra(EXTRA_REQ_NEWS_PREDICTION_TEXT_ID, R.string.title_news_bad3);
                break;
            default:
                intent.putExtra(EXTRA_REQ_NEWS_PREDICTION_IMAGE_ID, 0);
                intent.putExtra(EXTRA_REQ_NEWS_PREDICTION_TEXT_ID, 0);
        }

        return intent;
    }

    // Useless
    private void changeBtnBackgroundColor(View view) {
        if(mLastPressView != view) {
            if(mLastPressView != null) {
                mLastPressView.setBackgroundColor(
                        getResources().getColor(R.color.marketsense_rich_edit_black_background));
            }
            view.setBackgroundColor(
                    getResources().getColor(R.color.trend_red));
            mLastPressView = view;
        } else {
            view.setBackgroundColor(
                    getResources().getColor(R.color.marketsense_rich_edit_black_background));
            mLastPressView = null;
        }
    }

    private boolean changeBtnBackgroundColorImmediately(View view, MotionEvent motionEvent) {
        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
            // Pressed
            if(mLastPressView != null) {
                mLastPressView.setBackgroundColor(
                        getResources().getColor(R.color.marketsense_rich_edit_black_background));
            }
            view.setBackgroundColor(
                    getResources().getColor(R.color.trend_red));
            mLastPressView = view;
            view.performClick();
        } else if (motionEvent.getAction() == MotionEvent.ACTION_UP ||
                motionEvent.getAction() == MotionEvent.ACTION_CANCEL) {
            // Released
            view.setBackgroundColor(
                    getResources().getColor(R.color.marketsense_rich_edit_black_background));
        }

        return false;
    }

    private void initEditorPanel() {
        // TODO: maybe one day we will open these functions
//        findViewById(R.id.action_undo).setOnClickListener(new View.OnClickListener() {
//            @Override public void onClick(View v) {
//                mEditor.undo();
//            }
//        });
//        findViewById(R.id.action_undo).setOnTouchListener(new View.OnTouchListener() {
//            @SuppressLint("ClickableViewAccessibility")
//            @Override
//            public boolean onTouch(View view, MotionEvent motionEvent) {
//                return changeBtnBackgroundColorImmediately(view, motionEvent);
//            }
//        });
//
//        findViewById(R.id.action_redo).setOnClickListener(new View.OnClickListener() {
//            @Override public void onClick(View v) {
//                mEditor.redo();
//            }
//        });
//        findViewById(R.id.action_redo).setOnTouchListener(new View.OnTouchListener() {
//            @SuppressLint("ClickableViewAccessibility")
//            @Override
//            public boolean onTouch(View view, MotionEvent motionEvent) {
//                return changeBtnBackgroundColorImmediately(view, motionEvent);
//            }
//        });
//
//        findViewById(R.id.action_bold).setOnClickListener(new View.OnClickListener() {
//            @Override public void onClick(View v) {
//                mEditor.setBold();
//                changeBtnBackgroundColor(v);
//            }
//        });
//
//        findViewById(R.id.action_italic).setOnClickListener(new View.OnClickListener() {
//            @Override public void onClick(View v) {
//                mEditor.setItalic();
//                changeBtnBackgroundColor(v);
//            }
//        });
//
//        findViewById(R.id.action_subscript).setOnClickListener(new View.OnClickListener() {
//            @Override public void onClick(View v) {
//                mEditor.setSubscript();
//                changeBtnBackgroundColor(v);
//            }
//        });
//
//        findViewById(R.id.action_superscript).setOnClickListener(new View.OnClickListener() {
//            @Override public void onClick(View v) {
//                mEditor.setSuperscript();
//                changeBtnBackgroundColor(v);
//            }
//        });
//
//        findViewById(R.id.action_strikethrough).setOnClickListener(new View.OnClickListener() {
//            @Override public void onClick(View v) {
//                mEditor.setStrikeThrough();
//                changeBtnBackgroundColor(v);
//            }
//        });
//
//        findViewById(R.id.action_underline).setOnClickListener(new View.OnClickListener() {
//            @Override public void onClick(View v) {
//                mEditor.setUnderline();
//                changeBtnBackgroundColor(v);
//            }
//        });
//
//        findViewById(R.id.action_heading1).setOnClickListener(new View.OnClickListener() {
//            @Override public void onClick(View v) {
//                mEditor.setHeading(1);
//                changeBtnBackgroundColor(v);
//            }
//        });
//
//        findViewById(R.id.action_heading2).setOnClickListener(new View.OnClickListener() {
//            @Override public void onClick(View v) {
//                mEditor.setHeading(2);
//                changeBtnBackgroundColor(v);
//            }
//        });
//
//        findViewById(R.id.action_heading3).setOnClickListener(new View.OnClickListener() {
//            @Override public void onClick(View v) {
//                mEditor.setHeading(3);
//                changeBtnBackgroundColor(v);
//            }
//        });
//
//        findViewById(R.id.action_heading4).setOnClickListener(new View.OnClickListener() {
//            @Override public void onClick(View v) {
//                mEditor.setHeading(4);
//                changeBtnBackgroundColor(v);
//            }
//        });
//
//        findViewById(R.id.action_heading5).setOnClickListener(new View.OnClickListener() {
//            @Override public void onClick(View v) {
//                mEditor.setHeading(5);
//                changeBtnBackgroundColor(v);
//            }
//        });
//
//        findViewById(R.id.action_heading6).setOnClickListener(new View.OnClickListener() {
//            @Override public void onClick(View v) {
//                mEditor.setHeading(6);
//                changeBtnBackgroundColor(v);
//            }
//        });
//
//        findViewById(R.id.action_txt_color).setOnClickListener(new View.OnClickListener() {
//            private boolean isChanged;
//
//            @Override public void onClick(View view) {
//                mEditor.setTextColor(isChanged ?
//                        getResources().getColor(R.color.marketsense_text_black) :
//                        getResources().getColor(R.color.marketsense_text_red));
//                view.setBackgroundColor(isChanged ?
//                        getResources().getColor(R.color.marketsense_rich_edit_black_background) :
//                        getResources().getColor(R.color.trend_red));
//                isChanged = !isChanged;
//            }
//        });
//
//        findViewById(R.id.action_bg_color).setOnClickListener(new View.OnClickListener() {
//            private boolean isChanged;
//
//            @Override public void onClick(View view) {
//                mEditor.setTextBackgroundColor(isChanged ?
//                        getResources().getColor(R.color.marketsense_trans) :
//                        Color.YELLOW);
//                view.setBackgroundColor(isChanged ?
//                        getResources().getColor(R.color.marketsense_rich_edit_black_background) :
//                        getResources().getColor(R.color.trend_red));
//                isChanged = !isChanged;
//            }
//        });
//
//        findViewById(R.id.action_indent).setOnClickListener(new View.OnClickListener() {
//            @Override public void onClick(View v) {
//                mEditor.setIndent();
//                changeBtnBackgroundColor(v);
//            }
//        });
//
//        findViewById(R.id.action_outdent).setOnClickListener(new View.OnClickListener() {
//            @Override public void onClick(View v) {
//                mEditor.setOutdent();
//                changeBtnBackgroundColor(v);
//            }
//        });
//
//        findViewById(R.id.action_align_left).setOnClickListener(new View.OnClickListener() {
//            @Override public void onClick(View v) {
//                mEditor.setAlignLeft();
//                changeBtnBackgroundColor(v);
//            }
//        });
//
//        findViewById(R.id.action_align_center).setOnClickListener(new View.OnClickListener() {
//            @Override public void onClick(View v) {
//                mEditor.setAlignCenter();
//                changeBtnBackgroundColor(v);
//            }
//        });
//
//        findViewById(R.id.action_align_right).setOnClickListener(new View.OnClickListener() {
//            @Override public void onClick(View v) {
//                mEditor.setAlignRight();
//                changeBtnBackgroundColor(v);
//            }
//        });
//
//        findViewById(R.id.action_blockquote).setOnClickListener(new View.OnClickListener() {
//            @Override public void onClick(View v) {
//                mEditor.setBlockquote();
//                changeBtnBackgroundColor(v);
//            }
//        });
//
//        findViewById(R.id.action_insert_image).setOnTouchListener(new View.OnTouchListener() {
//            @SuppressLint("ClickableViewAccessibility")
//            @Override
//            public boolean onTouch(View view, MotionEvent motionEvent) {
//                if(motionEvent.getAction() == MotionEvent.ACTION_UP) {
//                    mEditor.focusEditor();
//                    final View alertView = LayoutInflater.from(RichEditorActivity.this)
//                            .inflate(R.layout.alertdialog_single_input, null);
//                    if (mImageAlertDialog != null) {
//                        mImageAlertDialog.dismiss();
//                        mImageAlertDialog = null;
//                    }
//                    final EditText editText = alertView.findViewById(R.id.alert_dialog_input);
//                    editText.setText(HTTP);
//                    mImageAlertDialog = new AlertDialog.Builder(RichEditorActivity.this)
//                            .setTitle(R.string.insert_image_url)
//                            .setView(alertView)
//                            .setPositiveButton(R.string.insert_ok, new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialogInterface, int i) {
//                                    mEditor.insertImage(editText.getText().toString(),
//                                            getResources().getString(R.string.image_alt));
//                                    mImageAlertDialog.dismiss();
//                                }
//                            }).setNegativeButton(R.string.insert_cancel, new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialogInterface, int i) {
//                                    mImageAlertDialog.dismiss();
//                                }
//                            }).show();
//                }
//                return changeBtnBackgroundColorImmediately(view, motionEvent);
//            }
//        });
//
//        findViewById(R.id.action_insert_link).setOnTouchListener(new View.OnTouchListener() {
//            @SuppressLint("ClickableViewAccessibility")
//            @Override
//            public boolean onTouch(View view, MotionEvent motionEvent) {
//                if(motionEvent.getAction() == MotionEvent.ACTION_UP) {
//                    mEditor.focusEditor();
//                    final View alertView = LayoutInflater.from(RichEditorActivity.this)
//                            .inflate(R.layout.alertdialog_two_inputs, null);
//                    if (mUrlAlertDialog != null) {
//                        mUrlAlertDialog.dismiss();
//                        mUrlAlertDialog = null;
//                    }
//                    final EditText editTextUrl = alertView.findViewById(R.id.alert_dialog_input_2);
//                    editTextUrl.setText(HTTP);
//                    mUrlAlertDialog = new AlertDialog.Builder(RichEditorActivity.this)
//                            .setTitle(R.string.insert_hyperlink)
//                            .setView(alertView)
//                            .setPositiveButton(R.string.insert_ok, new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialogInterface, int i) {
//                                    EditText editTextTitle = alertView.findViewById(R.id.alert_dialog_input_1);
//                                    mEditor.insertLink(
//                                            editTextUrl.getText().toString(),
//                                            editTextTitle.getText().toString());
//                                    mUrlAlertDialog.dismiss();
//                                }
//                            }).setNegativeButton(R.string.insert_cancel, new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialogInterface, int i) {
//                                    mUrlAlertDialog.dismiss();
//                                }
//                            }).show();
//                }
//                return changeBtnBackgroundColorImmediately(view, motionEvent);
//            }
//        });
//
//        findViewById(R.id.action_insert_checkbox).setOnClickListener(new View.OnClickListener() {
//            @Override public void onClick(View v) {
//                mEditor.insertTodo();
//                changeBtnBackgroundColor(v);
//            }
//        });
    }
}
