package com.idroi.marketsense;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.adapter.CommentsRecyclerViewAdapter;
import com.idroi.marketsense.common.ClientData;
import com.idroi.marketsense.common.FBHelper;
import com.idroi.marketsense.common.FrescoHelper;
import com.idroi.marketsense.common.FrescoImageHelper;
import com.idroi.marketsense.common.MarketSenseRendererHelper;
import com.idroi.marketsense.data.Comment;
import com.idroi.marketsense.data.News;
import com.idroi.marketsense.data.PostEvent;
import com.idroi.marketsense.data.UserProfile;

import org.json.JSONObject;

import java.util.ArrayList;

import static com.idroi.marketsense.RichEditorActivity.EXTRA_RES_HTML;
import static com.idroi.marketsense.RichEditorActivity.EXTRA_RES_ID;
import static com.idroi.marketsense.RichEditorActivity.EXTRA_RES_TYPE;
import static com.idroi.marketsense.RichEditorActivity.sReplyEditorRequestCode;
import static com.idroi.marketsense.common.Constants.FACEBOOK_CONSTANTS;
import static com.idroi.marketsense.data.UserProfile.NOTIFY_ID_REPLY_COMMENT_CLICK;

/**
 * Created by daniel.hsieh on 2018/7/3.
 */

public class CommentActivity extends AppCompatActivity {

    public static final String EXTRA_COMMENT = "com.idroi.marketsense.CommentActivity.extra_comment";
    public static final String EXTRA_NEED_TO_CHANGE = "com.idroi.marketsense.CommentActivity.extra_need_to_change";
    public static final String EXTRA_POSITION = "com.idroi.marketsense.CommentActivity.extra_position";
    public final static int CLICK_LIKE_BEFORE_LOGIN = 1;
    public final static int CLICK_COMMENT_BEFORE_LOGIN = 2;

    private Comment mComment;
    private int mPosition;

    private RecyclerView mCommentRecyclerView;
    private CommentsRecyclerViewAdapter mCommentsRecyclerViewAdapter;

    private AlertDialog mLoginAlertDialog;
    private LoginButton mFBLoginBtn;
    private UserProfile.GlobalBroadcastListener mGlobalBroadcastListener;
    private CallbackManager mFBCallbackManager;

    private ConstraintLayout mSayLikeBlock;
    private ImageView mSayLikeImageView;
    private TextView mSayLikeTextView;

    private int mLastClickedButton;
    private boolean mIsNeedToChange = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrescoHelper.initialize(getApplicationContext());
        setContentView(R.layout.activity_comment);

        initFBLogin();
        setInformation();
        setUpperBlock();
        setReplyBlock();
        setSocialFunctionBlock();
        setActionBar();
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

            TextView textview = view.findViewById(R.id.action_bar_name);
            if(textview != null) {
                textview.setText(getString(R.string.activity_comment));
            }

            ImageView additionFunction = view.findViewById(R.id.action_bar_notification);
            additionFunction.setVisibility(View.GONE);

            actionBar.setDisplayShowHomeEnabled(false);
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setCustomView(view,
                    new ActionBar.LayoutParams(
                            ActionBar.LayoutParams.MATCH_PARENT,
                            ActionBar.LayoutParams.MATCH_PARENT));
            actionBar.setDisplayShowCustomEnabled(true);
        }
    }

    private void setUpperBlock() {
        MarketSenseRendererHelper.addTextView((TextView) findViewById(R.id.comment_user_name), mComment.getUserName());
        MarketSenseRendererHelper.addTextView((TextView) findViewById(R.id.comment_create_time), mComment.getDateString());
        if(mComment.getAvatarUrl() != null && !mComment.getAvatarUrl().isEmpty()) {
            FrescoImageHelper.loadImageView(mComment.getAvatarUrl(),
                    (SimpleDraweeView) findViewById(R.id.comment_avatar_image_iv), FrescoImageHelper.ICON_IMAGE_RATIO);
        } else {
            FrescoImageHelper.loadImageView("http://monster.infohubapp.com/monsters/ic_mon_01_y.png",
                    (SimpleDraweeView) findViewById(R.id.comment_avatar_image_iv), FrescoImageHelper.ICON_IMAGE_RATIO);
        }
        NewsWebView commentBody = findViewById(R.id.comment_body);
        commentBody.getSettings().setLoadWithOverviewMode(false);
        commentBody.getSettings().setUseWideViewPort(false);
        commentBody.getSettings().setJavaScriptEnabled(false);
        String htmlData = "<link rel=\"stylesheet\" type=\"text/css\" href=\"img.css\" />" + mComment.getCommentHtml();
        commentBody.loadDataWithBaseURL("file:///android_asset/", htmlData, "text/html", "UTF-8", null);

        News news = mComment.getNews();
        if(news != null) {
            setNewsBlock(news);
        }
    }

    private void setNewsBlock(final News news) {
        ConstraintLayout newsBlock = findViewById(R.id.comment_news_block);
        TextView newsTitleView = findViewById(R.id.comment_news_title_tv);
        TextView dateTitleView = findViewById(R.id.comment_news_date_tv);
        TextView fireTextView = findViewById(R.id.comment_news_fire_tv);
        ImageView fireImageView = findViewById(R.id.comment_news_fire_iv);

        MarketSenseRendererHelper.addTextView(newsTitleView, news.getTitle());
        MarketSenseRendererHelper.addTextView(dateTitleView, news.getDate());

        // fire text
        if(fireTextView != null) {
            if (news.isOptimistic()) {
                fireTextView.setTextColor(
                        fireTextView.getContext().getResources().getColor(R.color.colorTrendUp));
                fireTextView.setVisibility(View.VISIBLE);
            } else if (news.isPessimistic()) {
                fireTextView.setTextColor(
                        fireTextView.getContext().getResources().getColor(R.color.colorTrendDown));
                fireTextView.setVisibility(View.VISIBLE);
            } else {
                fireTextView.setVisibility(View.GONE);
            }
        }

        // fire image
        if(fireImageView != null && fireTextView != null) {
            fireImageView.setVisibility(View.VISIBLE);
            switch (news.getLevel()) {
                case 3:
                    fireImageView.setImageResource(R.mipmap.ic_news_up3);
                    fireTextView.setText(R.string.title_news_good3);
                    break;
                case 2:
                    fireImageView.setImageResource(R.mipmap.ic_news_up2);
                    fireTextView.setText(R.string.title_news_good2);
                    break;
                case 1:
                    fireImageView.setImageResource(R.mipmap.ic_news_up1);
                    fireTextView.setText(R.string.title_news_good1);
                    break;
                case -1:
                    fireImageView.setImageResource(R.mipmap.ic_news_down1);
                    fireTextView.setText(R.string.title_news_bad1);
                    break;
                case -2:
                    fireImageView.setImageResource(R.mipmap.ic_news_down2);
                    fireTextView.setText(R.string.title_news_bad2);
                    break;
                case -3:
                    fireImageView.setImageResource(R.mipmap.ic_news_down3);
                    fireTextView.setText(R.string.title_news_bad3);
                    break;
                default:
                    fireImageView.setVisibility(View.GONE);
            }
        }

        View horizontalLineView = findViewById(R.id.social_horizontal_line);
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) horizontalLineView.getLayoutParams();
        if(newsBlock != null) {
            params.topToBottom = newsBlock.getId();
            newsBlock.setVisibility(View.VISIBLE);
            newsBlock.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(NewsWebViewActivity.generateNewsWebViewActivityIntent(
                            CommentActivity.this, news));
                    overridePendingTransition(R.anim.enter, R.anim.stop);
                }
            });
        }
        horizontalLineView.setLayoutParams(params);
    }

    private void setReplyBlock() {
        mCommentRecyclerView = findViewById(R.id.marketsense_webview_comment_rv);

        mCommentsRecyclerViewAdapter = new CommentsRecyclerViewAdapter(this);
        mCommentRecyclerView.setAdapter(mCommentsRecyclerViewAdapter);
        mCommentRecyclerView.setNestedScrollingEnabled(false);
        mCommentRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mCommentsRecyclerViewAdapter.setCommentArrayList(mComment.getReplyArrayList());
        updateCommentTitle();
    }

    private void updateCommentTitle() {
        ArrayList<Comment> replyArrayList = mComment.getReplyArrayList();
        if(replyArrayList != null) {
            TextView commentTitle = findViewById(R.id.comment_title);
            String format = getString(R.string.title_comment_format);
            commentTitle.setText(String.format(format, replyArrayList.size()));
        }
    }

    private void setLikeBlock() {
        mSayLikeBlock = findViewById(R.id.social_say_like);
        mSayLikeImageView = findViewById(R.id.social_say_like_iv);
        mSayLikeTextView = findViewById(R.id.social_say_like_tv);
        mSayLikeTextView.setText(String.valueOf(mComment.getLikeNumber()));
        if(!mComment.isLiked()) {
            mSayLikeImageView.setImageResource(R.mipmap.ic_like_off);
            mSayLikeBlock.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(FBHelper.checkFBLogin()) {
                        sayLike();
                    } else {
                        showLoginAlertDialog(CLICK_LIKE_BEFORE_LOGIN);
                    }
                }
            });
        } else {
            mSayLikeImageView.setImageResource(R.mipmap.ic_like_on);
            mSayLikeBlock.setOnClickListener(null);
        }
    }

    private void setSocialFunctionBlock() {
        setLikeBlock();
        TextView writeReply = findViewById(R.id.social_write_comment);
        writeReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(FBHelper.checkFBLogin()) {
                    startActivityForResult(RichEditorActivity.generateRichEditorActivityIntent(
                            CommentActivity.this, RichEditorActivity.TYPE.REPLY, mComment.getCommentId()),
                            sReplyEditorRequestCode);
                    overridePendingTransition(R.anim.enter, R.anim.stop);
                } else {
                    showLoginAlertDialog(CLICK_COMMENT_BEFORE_LOGIN);
                }
            }
        });

        UserProfile userProfile = ClientData.getInstance(this).getUserProfile();
        mGlobalBroadcastListener = new UserProfile.GlobalBroadcastListener() {
            @Override
            public void onGlobalBroadcast(int notifyId, Object payload) {
                if(notifyId == NOTIFY_ID_REPLY_COMMENT_CLICK && FBHelper.checkFBLogin()) {
                    mComment.updateLikeUserProfile();
                    setLikeBlock();
                    switch (mLastClickedButton) {
                        case CLICK_COMMENT_BEFORE_LOGIN:
                            startActivityForResult(RichEditorActivity.generateRichEditorActivityIntent(
                                    CommentActivity.this, RichEditorActivity.TYPE.REPLY, mComment.getCommentId()),
                                    sReplyEditorRequestCode);
                            overridePendingTransition(R.anim.enter, R.anim.stop);
                            return;
                        case CLICK_LIKE_BEFORE_LOGIN:
                            if(!mComment.isLiked()) {
                                sayLike();
                            }
                    }
                }
            }
        };
        userProfile.addGlobalBroadcastListener(mGlobalBroadcastListener);
    }

    private void sayLike() {
        mIsNeedToChange = true;
        mComment.setLike(true);
        mComment.increaseLike();
        mSayLikeTextView.setText(String.valueOf(mComment.getLikeNumber()));
        mSayLikeImageView.setImageResource(R.mipmap.ic_like_on);
        mSayLikeBlock.setOnClickListener(null);
        PostEvent.sendLike(this, mComment.getCommentId());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == sReplyEditorRequestCode) {
            if(resultCode == RESULT_OK) {
                mIsNeedToChange = true;

                String html = data.getStringExtra(EXTRA_RES_HTML);
                String type = data.getStringExtra(EXTRA_RES_TYPE);
                String id = data.getStringExtra(EXTRA_RES_ID);

                Comment newComment = new Comment();
                newComment.setCommentId(id);
                newComment.setCommentHtml(html);
                newComment.setViewType(Comment.VIEW_TYPE_REPLY);
                if(type.equals(RichEditorActivity.TYPE.REPLY.getType())) {
                    mCommentsRecyclerViewAdapter.addOneComment(newComment);
                    mComment.addReply(newComment);
                    updateCommentTitle();
                }

                MSLog.d(String.format("user send a reply on (%s, %s): %s", type, id, html));
            }
        }
        mFBCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    // fb login part when the user click fab
    private void initFBLogin() {

        MSLog.d("The user has logged in Facebook: " + FBHelper.checkFBLogin());

        mFBCallbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(mFBCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                MSLog.d("facebook registerCallback onSuccess in StockActivity");
                getFBUserProfile();
            }

            @Override
            public void onCancel() {
                MSLog.d("facebook registerCallback onCancel");
            }

            @Override
            public void onError(FacebookException exception) {
                MSLog.d("facebook registerCallback onError: " + exception.toString());
            }
        });
    }

    private void getFBUserProfile() {
        FBHelper.getFBUserProfile(this, new FBHelper.FBHelperListener() {
            @Override
            public void onTaskCompleted(JSONObject data, String avatarLink) {
                String userName = FBHelper.fetchFbData(data, UserProfile.FB_USER_NAME_KEY);
                String userId = FBHelper.fetchFbData(data, UserProfile.FB_USER_ID_KEY);
                String userEmail = FBHelper.fetchFbData(data, UserProfile.FB_USER_EMAIL_KEY);
                PostEvent.sendRegister(CommentActivity.this, userId, userName, FACEBOOK_CONSTANTS,
                        UserProfile.generatePassword(userId, FACEBOOK_CONSTANTS), userEmail, avatarLink, new PostEvent.PostEventListener() {
                            @Override
                            public void onResponse(boolean isSuccessful, Object data) {
                                if(!isSuccessful) {
                                    Toast.makeText(CommentActivity.this, R.string.login_failed_description, Toast.LENGTH_SHORT).show();
                                    LoginManager.getInstance().logOut();
                                } else {
                                    UserProfile userProfile = ClientData.getInstance(CommentActivity.this).getUserProfile();
                                    userProfile.globalBroadcast(NOTIFY_ID_REPLY_COMMENT_CLICK);
                                }
                            }
                        });
            }
        });
    }

    private void showLoginAlertDialog(int lastButton) {
        mLastClickedButton = lastButton;
        if(mLoginAlertDialog != null) {
            mLoginAlertDialog.dismiss();
            mLoginAlertDialog = null;
        }

        final View alertView = LayoutInflater.from(this)
                .inflate(R.layout.alertdialog_login, null);
        mLoginAlertDialog = new AlertDialog.Builder(this)
                .setView(alertView).create();
        mLoginAlertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                mFBLoginBtn = alertView.findViewById(R.id.login_button);
                mFBLoginBtn.setReadPermissions("email");
                mFBLoginBtn.setReadPermissions("public_profile");
            }
        });
        mLoginAlertDialog.show();
    }

    @Override
    protected void onDestroy() {
        UserProfile userProfile = ClientData.getInstance(this).getUserProfile();
        userProfile.deleteGlobalBroadcastListener(mGlobalBroadcastListener);
        mCommentsRecyclerViewAdapter.destroy();
        super.onDestroy();
    }

    @Override
    public void onPause() {
        super.onPause();

        if(mLoginAlertDialog != null) {
            mLoginAlertDialog.dismiss();
            mLoginAlertDialog = null;
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_COMMENT, mComment);
        intent.putExtra(EXTRA_NEED_TO_CHANGE, mIsNeedToChange);
        intent.putExtra(EXTRA_POSITION, mPosition);
        setResult(RESULT_OK, intent);
        finish();
        overridePendingTransition(R.anim.stop, R.anim.right_to_left);
        super.onBackPressed();
    }

    private void setInformation() {
        Intent intent = getIntent();
        mComment = (Comment) intent.getSerializableExtra(EXTRA_COMMENT);
        mPosition = intent.getIntExtra(EXTRA_POSITION, -1);
    }

    public static Intent generateCommentActivityIntent(Context context, Comment comment, int position) {
        Intent intent = new Intent(context, CommentActivity.class);
        intent.putExtra(EXTRA_COMMENT, comment);
        intent.putExtra(EXTRA_POSITION, position);
        return intent;
    }
}
