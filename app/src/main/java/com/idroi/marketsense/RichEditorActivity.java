package com.idroi.marketsense;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;
import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.common.FrescoHelper;
import com.idroi.marketsense.data.PostEvent;

import jp.wasabeef.richeditor.RichEditor;

import static com.idroi.marketsense.common.Constants.HTTP;

/**
 * Created by daniel.hsieh on 2018/5/8.
 */

public class RichEditorActivity extends AppCompatActivity {

    RichEditor mEditor;
//    String mEditorString;
    View mLastPressView;

    public static final String EXTRA_REQ_TYPE = "extra_type";
    public static final String EXTRA_REQ_ID = "extra_id";
    public static final String EXTRA_RES_HTML = "extra_response_html";
    public static final String EXTRA_RES_TYPE = EXTRA_REQ_TYPE;
    public static final String EXTRA_RES_ID = EXTRA_REQ_ID;
    public final static int sEditorRequestCode = 2;
    public final static int sReplyEditorRequestCode = 3;

    private String mType;
    private String mId;
    private boolean mDoubleClickBack = false;
    private AlertDialog mImageAlertDialog;
    private AlertDialog mUrlAlertDialog;

    public enum TYPE {
        NEWS("news"),
        STOCK("stock"),
        REPLY("reply");

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

        setActionBar();
        setRichEditor();
        setPostBtn();
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

    private void setPostBtn() {

        mId = getIntent().getStringExtra(EXTRA_REQ_ID);
        mType = getIntent().getStringExtra(EXTRA_REQ_TYPE);

        final Button sendBtn = findViewById(R.id.btn_say_comment_send);
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String html = mEditor.getHtml();
                if(html == null) {
                    Toast.makeText(RichEditorActivity.this,
                            R.string.title_comment_create_null, Toast.LENGTH_SHORT).show();
                    return;
                }
                if(mType.equals(TYPE.NEWS.getType())) {
                    MSLog.i("send news comment (" + mId + "): " + html);
                    PostEvent.sendNewsComment(RichEditorActivity.this, mId, html);
                } else if(mType.equals(TYPE.STOCK.getType())) {
                    MSLog.i("send stock comment (" + mId + "): " + html);
                    PostEvent.sendStockComment(RichEditorActivity.this, mId, html);
                } else if(mType.equals(TYPE.REPLY.getType())) {
                    MSLog.i("send reply comment (" + mId + "): " + html);
                    PostEvent.sendReplyComment(RichEditorActivity.this, mId, html);
                }
                Intent intent = new Intent();
                intent.putExtra(EXTRA_RES_HTML, html);
                intent.putExtra(EXTRA_RES_TYPE, mType);
                intent.putExtra(EXTRA_RES_ID, mId);
                setResult(RESULT_OK, intent);
                finish();
                overridePendingTransition(R.anim.stop, R.anim.right_to_left);
            }
        });
    }

    private void changeBtnBackgroundColor(View view) {
        if(mLastPressView != view) {
            if(mLastPressView != null) {
                mLastPressView.setBackgroundColor(
                        getResources().getColor(R.color.marketsense_rich_edit_black_background));
            }
            view.setBackgroundColor(
                    getResources().getColor(R.color.colorTrendUp));
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
                    getResources().getColor(R.color.colorTrendUp));
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

    private void setRichEditor() {
        mEditor = (RichEditor) findViewById(R.id.rich_editor);
        mEditor.loadCSS("file:///android_asset/img.css");
        mEditor.setEditorHeight(200);
        mEditor.setEditorFontSize(22);
        mEditor.setEditorFontColor(getResources().getColor(R.color.marketsense_text_black));
        //mEditor.setEditorBackgroundColor(Color.BLUE);
        //mEditor.setBackgroundColor(Color.BLUE);
        //mEditor.setBackgroundResource(R.drawable.bg);
        mEditor.setPadding(15, 15, 15, 15);
        //    mEditor.setBackground("https://raw.githubusercontent.com/wasabeef/art/master/chip.jpg");
        mEditor.setPlaceholder(getResources().getString(R.string.comment_warning));

        findViewById(R.id.action_undo).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mEditor.undo();
            }
        });
        findViewById(R.id.action_undo).setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return changeBtnBackgroundColorImmediately(view, motionEvent);
            }
        });

        findViewById(R.id.action_redo).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mEditor.redo();
            }
        });
        findViewById(R.id.action_redo).setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return changeBtnBackgroundColorImmediately(view, motionEvent);
            }
        });

        findViewById(R.id.action_bold).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mEditor.setBold();
                changeBtnBackgroundColor(v);
            }
        });

        findViewById(R.id.action_italic).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mEditor.setItalic();
                changeBtnBackgroundColor(v);
            }
        });

        findViewById(R.id.action_subscript).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mEditor.setSubscript();
                changeBtnBackgroundColor(v);
            }
        });

        findViewById(R.id.action_superscript).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mEditor.setSuperscript();
                changeBtnBackgroundColor(v);
            }
        });

        findViewById(R.id.action_strikethrough).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mEditor.setStrikeThrough();
                changeBtnBackgroundColor(v);
            }
        });

        findViewById(R.id.action_underline).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mEditor.setUnderline();
                changeBtnBackgroundColor(v);
            }
        });

        findViewById(R.id.action_heading1).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mEditor.setHeading(1);
                changeBtnBackgroundColor(v);
            }
        });

        findViewById(R.id.action_heading2).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mEditor.setHeading(2);
                changeBtnBackgroundColor(v);
            }
        });

        findViewById(R.id.action_heading3).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mEditor.setHeading(3);
                changeBtnBackgroundColor(v);
            }
        });

        findViewById(R.id.action_heading4).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mEditor.setHeading(4);
                changeBtnBackgroundColor(v);
            }
        });

        findViewById(R.id.action_heading5).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mEditor.setHeading(5);
                changeBtnBackgroundColor(v);
            }
        });

        findViewById(R.id.action_heading6).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mEditor.setHeading(6);
                changeBtnBackgroundColor(v);
            }
        });

        findViewById(R.id.action_txt_color).setOnClickListener(new View.OnClickListener() {
            private boolean isChanged;

            @Override public void onClick(View view) {
                mEditor.setTextColor(isChanged ?
                        getResources().getColor(R.color.marketsense_text_black) :
                        getResources().getColor(R.color.marketsense_text_red));
                view.setBackgroundColor(isChanged ?
                        getResources().getColor(R.color.marketsense_rich_edit_black_background) :
                        getResources().getColor(R.color.colorTrendUp));
                isChanged = !isChanged;
            }
        });

        findViewById(R.id.action_bg_color).setOnClickListener(new View.OnClickListener() {
            private boolean isChanged;

            @Override public void onClick(View view) {
                mEditor.setTextBackgroundColor(isChanged ?
                        getResources().getColor(R.color.marketsense_trans) :
                        Color.YELLOW);
                view.setBackgroundColor(isChanged ?
                        getResources().getColor(R.color.marketsense_rich_edit_black_background) :
                        getResources().getColor(R.color.colorTrendUp));
                isChanged = !isChanged;
            }
        });

        findViewById(R.id.action_indent).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mEditor.setIndent();
                changeBtnBackgroundColor(v);
            }
        });

        findViewById(R.id.action_outdent).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mEditor.setOutdent();
                changeBtnBackgroundColor(v);
            }
        });

        findViewById(R.id.action_align_left).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mEditor.setAlignLeft();
                changeBtnBackgroundColor(v);
            }
        });

        findViewById(R.id.action_align_center).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mEditor.setAlignCenter();
                changeBtnBackgroundColor(v);
            }
        });

        findViewById(R.id.action_align_right).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mEditor.setAlignRight();
                changeBtnBackgroundColor(v);
            }
        });

        findViewById(R.id.action_blockquote).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mEditor.setBlockquote();
                changeBtnBackgroundColor(v);
            }
        });

        findViewById(R.id.action_insert_image).setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    mEditor.focusEditor();
                    final View alertView = LayoutInflater.from(RichEditorActivity.this)
                            .inflate(R.layout.alertdialog_single_input, null);
                    if (mImageAlertDialog != null) {
                        mImageAlertDialog.dismiss();
                        mImageAlertDialog = null;
                    }
                    final EditText editText = alertView.findViewById(R.id.alert_dialog_input);
                    editText.setText(HTTP);
                    mImageAlertDialog = new AlertDialog.Builder(RichEditorActivity.this)
                            .setTitle(R.string.insert_image_url)
                            .setView(alertView)
                            .setPositiveButton(R.string.insert_ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    mEditor.insertImage(editText.getText().toString(),
                                            getResources().getString(R.string.image_alt));
                                    mImageAlertDialog.dismiss();
                                }
                            }).setNegativeButton(R.string.insert_cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    mImageAlertDialog.dismiss();
                                }
                            }).show();
                }
                return changeBtnBackgroundColorImmediately(view, motionEvent);
            }
        });

        findViewById(R.id.action_insert_link).setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    mEditor.focusEditor();
                    final View alertView = LayoutInflater.from(RichEditorActivity.this)
                            .inflate(R.layout.alertdialog_two_inputs, null);
                    if (mUrlAlertDialog != null) {
                        mUrlAlertDialog.dismiss();
                        mUrlAlertDialog = null;
                    }
                    final EditText editTextUrl = alertView.findViewById(R.id.alert_dialog_input_2);
                    editTextUrl.setText(HTTP);
                    mUrlAlertDialog = new AlertDialog.Builder(RichEditorActivity.this)
                            .setTitle(R.string.insert_hyperlink)
                            .setView(alertView)
                            .setPositiveButton(R.string.insert_ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    EditText editTextTitle = alertView.findViewById(R.id.alert_dialog_input_1);
                                    mEditor.insertLink(
                                            editTextUrl.getText().toString(),
                                            editTextTitle.getText().toString());
                                    mUrlAlertDialog.dismiss();
                                }
                            }).setNegativeButton(R.string.insert_cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    mUrlAlertDialog.dismiss();
                                }
                            }).show();
                }
                return changeBtnBackgroundColorImmediately(view, motionEvent);
            }
        });

        findViewById(R.id.action_insert_checkbox).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mEditor.insertTodo();
                changeBtnBackgroundColor(v);
            }
        });
    }

    private void setActionBar() {
        final ActionBar actionBar = getSupportActionBar();

        if(actionBar != null) {
            actionBar.setElevation(0);
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
                textView.setText(getResources().getText(R.string.activity_rich_editor));
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

    public static Intent generateRichEditorActivityIntent(Context context, TYPE type, String id) {
        Intent intent = new Intent(context, RichEditorActivity.class);
        intent.putExtra(EXTRA_REQ_TYPE, type.getType());
        intent.putExtra(EXTRA_REQ_ID, id);
        return intent;
    }
}
