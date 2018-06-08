package com.idroi.marketsense;

import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.adapter.SettingAdapter;
import com.idroi.marketsense.common.ClientData;
import com.idroi.marketsense.common.Constants;
import com.idroi.marketsense.common.FBHelper;
import com.idroi.marketsense.common.FrescoHelper;
import com.idroi.marketsense.common.MarketSenseRendererHelper;
import com.idroi.marketsense.common.SharedPreferencesCompat;
import com.idroi.marketsense.data.PostEvent;
import com.idroi.marketsense.data.UserProfile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import static com.idroi.marketsense.common.Constants.FACEBOOK_CONSTANTS;
import static com.idroi.marketsense.common.Constants.SHARED_PREFERENCE_REQUEST_NAME;
import static com.idroi.marketsense.common.Constants.SHARED_PREFERENCE_USER_SETTING;
import static com.idroi.marketsense.common.Constants.USER_SETTING_NOTIFICATION_KEY;
import static com.idroi.marketsense.data.UserProfile.NOTIFY_ID_FAVORITE_LIST;

/**
 * Created by daniel.hsieh on 2018/4/27.
 */

public class SettingActivity extends AppCompatActivity {

    private int[] mStringIds = {
            R.string.preference_notification, // fake
            R.string.preference_notification,
            R.string.preference_share,
            R.string.preference_feedback,
            R.string.preference_star,
            R.string.preference_privacy_statement,
            R.string.preference_term_of_service,
            R.string.preference_disclaimer
    };

    private Integer[] mDrawableIds = {
            R.drawable.setting_notification, // fake
            R.drawable.setting_notification,
            R.drawable.setting_share,
            R.drawable.setting_feedback,
            R.drawable.setting_star,
            R.drawable.setting_about,
            R.drawable.setting_about,
            R.drawable.setting_about,
    };

    private AlertDialog mLoginAlertDialog;
    private CallbackManager mFBCallbackManager;
    private LoginButton mFBLoginBtn;
    private ListView mListView;
    private SettingAdapter mSettingAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrescoHelper.initialize(getApplicationContext());
        setContentView(R.layout.activity_setting);

        setListView();
        setActionBar();
        initFBLogin();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if(mLoginAlertDialog != null) {
            mLoginAlertDialog.dismiss();
            mLoginAlertDialog = null;
        }
    }

    private void initFBLogin() {

        MSLog.d("The user has logged in Facebook: " + FBHelper.checkFBLogin());

        mFBCallbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(mFBCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                MSLog.d("facebook registerCallback onSuccess in SettingActivity");
                getFBUserProfile(true);
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

        if(FBHelper.checkFBLogin()) {
            getFBUserProfile(false);
            refreshFBUi();
        }
    }

    private void setFBLoginBtn(View view) {
        mFBLoginBtn = view.findViewById(R.id.login_button);
        mFBLoginBtn.setReadPermissions("email");
        mFBLoginBtn.setReadPermissions("public_profile");
    }

    private void getFBUserProfile(final boolean shouldRegister) {
        FBHelper.getFBUserProfile(this, new FBHelper.FBHelperListener() {
            @Override
            public void onTaskCompleted(JSONObject data, String avatarLink) {
                String userName = FBHelper.fetchFbData(data, UserProfile.FB_USER_NAME_KEY);
                if(shouldRegister) {
                    String userId = FBHelper.fetchFbData(data, UserProfile.FB_USER_ID_KEY);
                    String userEmail = FBHelper.fetchFbData(data, UserProfile.FB_USER_EMAIL_KEY);
                    PostEvent.sendRegister(SettingActivity.this, userId, userName, FACEBOOK_CONSTANTS,
                            UserProfile.generatePassword(userId, FACEBOOK_CONSTANTS), userEmail, avatarLink);
                }
                refreshFBUi(userName, avatarLink);
            }
        }, true);
    }

    private void refreshFBUi() {
        refreshFBUi(null, null);
    }

    private void refreshFBUi(@Nullable String userName, String avatarUrl) {
        if(userName != null && avatarUrl != null) {
            internalRefreshFBUi(userName, avatarUrl);
        } else {
            ClientData clientData = ClientData.getInstance(this);
            UserProfile userProfile = clientData.getUserProfile();
            internalRefreshFBUi(userProfile.getUserName(), userProfile.getUserAvatarLink());
        }
    }

    private void internalRefreshFBUi(String userName, String avatarLink) {

        boolean isLogin = FBHelper.checkFBLogin();
        MSLog.d("internalRefreshFBUi isLogin: " + isLogin);

        View view = getViewByPosition(0, mListView);
        SimpleDraweeView imageView = view.findViewById(R.id.user_name_block_avatar);
        TextView textView = view.findViewById(R.id.user_name_tv);
        Button button = view.findViewById(R.id.setting_login_btn);
        if(isLogin) {
            if(avatarLink != null) {
                imageView.setImageURI(Uri.parse(avatarLink));
            }
            MarketSenseRendererHelper.addTextView(textView, userName);
            button.setText(R.string.logout);
        } else {
            imageView.setImageResource(R.drawable.ic_account_circle_gray_24px);
            textView.setText(getResources().getString(R.string.hello));
            button.setText(R.string.login);
        }
    }

    private View getViewByPosition(int pos, ListView listView) {
        final int firstListItemPosition = listView.getFirstVisiblePosition();
        final int lastListItemPosition = firstListItemPosition + listView.getChildCount() - 1;

        if (pos < firstListItemPosition || pos > lastListItemPosition ) {
            return listView.getAdapter().getView(pos, null, listView);
        } else {
            final int childIndex = pos - firstListItemPosition;
            return listView.getChildAt(childIndex);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mFBCallbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void setListView() {
        mListView = (ListView) findViewById(R.id.setting_listview);

        final ArrayList<String> list = new ArrayList<>();
        for (int stringId : mStringIds) {
            list.add(getResources().getString(stringId));
        }

        final ArrayList<Integer> list2 = new ArrayList<>();
        list2.addAll(Arrays.asList(mDrawableIds));

        mSettingAdapter = new SettingAdapter(this, list, list2);
        mSettingAdapter.setSettingOnClickListener(new SettingAdapter.SettingOnClickListener() {
            @Override
            public void onLoginBtnClick() {
                if(FBHelper.checkFBLogin()) {
                    MSLog.d("perform logout");
                    LoginManager.getInstance().logOut();
                    internalRefreshFBUi(null, null);

                    UserProfile userProfile = ClientData.getInstance(SettingActivity.this).getUserProfile();
                    userProfile.clearFavoriteStock();
                    userProfile.clearEvents();
                    userProfile.notifyUserProfile(NOTIFY_ID_FAVORITE_LIST);
                } else {
                    MSLog.d("perform login");
                    showLoginAlertDialog();
                }
            }

            @Override
            public void onSwitchClick(boolean isChecked) {
                MSLog.d("user set notification to: " + isChecked);
                SharedPreferences.Editor editor =
                        SettingActivity.this.getSharedPreferences(SHARED_PREFERENCE_USER_SETTING, Context.MODE_PRIVATE).edit();
                editor.putBoolean(USER_SETTING_NOTIFICATION_KEY, isChecked);
                SharedPreferencesCompat.apply(editor);
            }
        });
        mListView.setAdapter(mSettingAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                handleListClick(position);
            }
        });
    }

    private void showLoginAlertDialog() {
        if(mLoginAlertDialog != null) {
            mLoginAlertDialog.dismiss();
            mLoginAlertDialog = null;
        }

        final View alertView = LayoutInflater.from(SettingActivity.this)
                .inflate(R.layout.alertdialog_login, null);
        mLoginAlertDialog = new AlertDialog.Builder(SettingActivity.this)
                .setView(alertView).create();
        mLoginAlertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                setFBLoginBtn(alertView);
            }
        });
        mLoginAlertDialog.show();
    }

    private void handleListClick(int position) {
        if(position <= 0 || position >= mStringIds.length) {
            return;
        }
        int id = mStringIds[position];
        String title = getResources().getString(id);
        switch (id) {
            case R.string.preference_feedback:
                startActivity(WebViewActivity.generateWebViewActivityIntent(
                        this, id, title, "https://docs.google.com/forms/d/e/1FAIpQLSfT0nDlt-Ra052pzXeG7nSjwkStnChRhyTOD5M5flRkukLWoQ/viewform"));
                break;
            case R.string.preference_privacy_statement:
                startActivity(WebViewActivity.generateWebViewActivityIntent(
                        this, id, title, "http://www.infohubapp.com/marketsense/documents/privacy-statement.html"));
                break;
            case R.string.preference_term_of_service:
                startActivity(WebViewActivity.generateWebViewActivityIntent(
                        this, id, title, "http://www.infohubapp.com/marketsense/documents/term-of-service.html"));
                break;
            case R.string.preference_disclaimer:
                startActivity(WebViewActivity.generateWebViewActivityIntent(
                        this, id, title, "http://www.infohubapp.com/marketsense/documents/disclaimer.html"));
                break;
            default:
                Toast.makeText(this, R.string.preference_sorry, Toast.LENGTH_SHORT).show();
        }
        overridePendingTransition(R.anim.enter, R.anim.stop);
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
