package com.idroi.marketsense.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.idroi.marketsense.BuildConfig;
import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.R;
import com.idroi.marketsense.WebViewActivity;
import com.idroi.marketsense.adapter.SettingAdapter;
import com.idroi.marketsense.common.ClientData;
import com.idroi.marketsense.common.FBHelper;
import com.idroi.marketsense.common.MarketSenseRendererHelper;
import com.idroi.marketsense.common.SharedPreferencesCompat;
import com.idroi.marketsense.data.PostEvent;
import com.idroi.marketsense.data.UserProfile;
import com.idroi.marketsense.datasource.SettingSource;

import org.json.JSONObject;

import static com.idroi.marketsense.common.Constants.FACEBOOK_CONSTANTS;
import static com.idroi.marketsense.common.Constants.SHARED_PREFERENCE_USER_SETTING;
import static com.idroi.marketsense.common.Constants.SHARE_APP_INSTALL_LINK;
import static com.idroi.marketsense.common.Constants.USER_SETTING_NOTIFICATION_KEY;
import static com.idroi.marketsense.data.UserProfile.NOTIFY_ID_FAVORITE_LIST;

/**
 * Created by daniel.hsieh on 2018/8/23.
 */

public class ProfileFragment extends Fragment {

    private AlertDialog mLoginAlertDialog, mStarAlertDialog;
    private CallbackManager mFBCallbackManager;
    private LoginButton mFBLoginBtn;
    private ListView mListView;
    private SettingAdapter mSettingAdapter;
    private SettingSource mSettingSource;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setListView(view);
        initFBLogin();
    }

    private void setListView(View view) {
        mListView = view.findViewById(R.id.setting_listview);
        mSettingSource = new SettingSource(getActivity());

        mSettingAdapter = new SettingAdapter(getActivity(), mSettingSource);
        mSettingAdapter.setSettingOnClickListener(new SettingAdapter.SettingOnClickListener() {
            @Override
            public void onLoginBtnClick() {
                if(getActivity() != null) {
                    if (FBHelper.checkFBLogin()) {
                        MSLog.d("perform logout");
                        LoginManager.getInstance().logOut();
                        internalRefreshFBUi(null, null);

                        UserProfile userProfile = ClientData.getInstance(getActivity()).getUserProfile();
                        userProfile.saveFavoriteStocksAndEvents(getActivity());
                        userProfile.clearUserProfile();
                        userProfile.globalBroadcast(NOTIFY_ID_FAVORITE_LIST);
                    } else {
                        MSLog.d("perform login");
                        showLoginAlertDialog();
                    }
                }
            }

            @Override
            public void onSwitchClick(boolean isChecked) {
                if(getActivity() != null) {
                    MSLog.d("user set notification to: " + isChecked);
                    SharedPreferences.Editor editor =
                            getActivity().getSharedPreferences(SHARED_PREFERENCE_USER_SETTING, Context.MODE_PRIVATE).edit();
                    editor.putBoolean(USER_SETTING_NOTIFICATION_KEY, isChecked);
                    SharedPreferencesCompat.apply(editor);
                }
            }
        });
        mListView.setAdapter(mSettingAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                if(mSettingSource.getItem(position).isClickable()) {
                    handleListClick(position);
                }
            }
        });
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

    private void getFBUserProfile(final boolean shouldRegister) {
        Activity activity = getActivity();
        if(activity == null) {
            return;
        }

        FBHelper.getFBUserProfile(activity, new FBHelper.FBHelperListener() {
            @Override
            public void onTaskCompleted(JSONObject data, final String avatarLink) {
                final String userName = FBHelper.fetchFbData(data, UserProfile.FB_USER_NAME_KEY);

                final Activity activity = getActivity();
                if(activity == null) {
                    return;
                }

                if(shouldRegister) {
                    String userId = FBHelper.fetchFbData(data, UserProfile.FB_USER_ID_KEY);
                    String userEmail = FBHelper.fetchFbData(data, UserProfile.FB_USER_EMAIL_KEY);
                    PostEvent.sendRegister(activity, userId, userName, FACEBOOK_CONSTANTS,
                            UserProfile.generatePassword(userId, FACEBOOK_CONSTANTS), userEmail, avatarLink, new PostEvent.PostEventListener() {
                                @Override
                                public void onResponse(boolean isSuccessful, Object data) {
                                    if(!isSuccessful) {
                                        Toast.makeText(activity, R.string.login_failed_description, Toast.LENGTH_SHORT).show();
                                        LoginManager.getInstance().logOut();
                                    } else {
                                        refreshFBUi(userName, avatarLink);
                                    }
                                }
                            });
                } else {
                    refreshFBUi(userName, avatarLink);
                }
            }
        });
    }

    private void refreshFBUi(@Nullable String userName, String avatarUrl) {
        if(userName != null && avatarUrl != null) {
            internalRefreshFBUi(userName, avatarUrl);
        } else {
            ClientData clientData = ClientData.getInstance();
            UserProfile userProfile = clientData.getUserProfile();
            internalRefreshFBUi(userProfile.getUserName(), userProfile.getUserAvatarLink());
        }
    }

    private void handleListClick(int position) {
        Activity activity = getActivity();
        if(position <= 0 || position >= mSettingSource.getSize() || activity == null) {
            return;
        }

        int id = mSettingSource.getId(position);
        String title = getResources().getString(id);
        switch (id) {
            // https://play.google.com/store/apps/details?id=com.idroi.marketsense&referrer=utm_source%3Dandroid_app%26utm_medium%3Dinapp%26utm_campaign%3Dshare%26
            case R.string.preference_share:
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.share_description) + SHARE_APP_INSTALL_LINK);
                sendIntent.setType("text/plain");
                startActivity(sendIntent);
                break;
            case R.string.preference_star:
                showStarAlertDialog();
                break;
            case R.string.preference_feedback:
                startActivity(WebViewActivity.generateWebViewActivityIntent(
                        activity, id, title, "https://docs.google.com/forms/d/e/1FAIpQLSfT0nDlt-Ra052pzXeG7nSjwkStnChRhyTOD5M5flRkukLWoQ/viewform"));
                break;
            case R.string.preference_privacy_statement:
                startActivity(WebViewActivity.generateWebViewActivityIntent(
                        activity, id, title, "http://www.infohubapp.com/marketsense/documents/privacy-statement.html"));
                break;
            case R.string.preference_term_of_service:
                startActivity(WebViewActivity.generateWebViewActivityIntent(
                        activity, id, title, "http://www.infohubapp.com/marketsense/documents/term-of-service.html"));
                break;
            case R.string.preference_disclaimer:
                startActivity(WebViewActivity.generateWebViewActivityIntent(
                        activity, id, title, "http://www.infohubapp.com/marketsense/documents/disclaimer.html"));
                break;
            case R.string.preference_email:
                sendEmail();
                break;
            default:
                Toast.makeText(activity, R.string.preference_sorry, Toast.LENGTH_SHORT).show();
        }

        activity.overridePendingTransition(R.anim.enter, R.anim.stop);
    }

    private void sendEmail() {

        String title = getResources().getString(R.string.preference_feedback);
        String format = "MANUFACTURER: %s\nMODEL: %s\nPRODUCT: %s\nVERSION: %s\nSDK_VERSION: %s\n================\n意見回饋：";

        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setType("message/rfc822");
        intent.setData(Uri.parse("mailto:"));
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"bigbirdgeeks@gmail.com"});
        intent.putExtra(Intent.EXTRA_SUBJECT, title);
        intent.putExtra(Intent.EXTRA_TEXT, String.format(format, Build.MANUFACTURER, Build.MODEL, Build.PRODUCT, BuildConfig.VERSION_NAME, Build.VERSION.SDK_INT));
        try {
            startActivity(Intent.createChooser(intent, "寄信給開發者"));
        } catch (android.content.ActivityNotFoundException ex) {
            // pass
        }
    }

    private void showLoginAlertDialog() {
        if(mLoginAlertDialog != null) {
            mLoginAlertDialog.dismiss();
            mLoginAlertDialog = null;
        }

        Activity activity = getActivity();
        if(activity == null) {
            return;
        }

        final View alertView = LayoutInflater.from(activity)
                .inflate(R.layout.alertdialog_login, null);
        mLoginAlertDialog = new AlertDialog.Builder(activity)
                .setView(alertView).create();
        mLoginAlertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                setFBLoginBtn(alertView);
            }
        });
        mLoginAlertDialog.show();
    }

    private void setFBLoginBtn(View view) {
        mFBLoginBtn = view.findViewById(R.id.login_button);
        mFBLoginBtn.setReadPermissions("email");
        mFBLoginBtn.setReadPermissions("public_profile");
    }

    private void showStarAlertDialog() {
        if(mStarAlertDialog != null) {
            mStarAlertDialog.dismiss();
            mStarAlertDialog = null;
        }

        Activity activity = getActivity();
        if(activity == null) {
            return;
        }

        mStarAlertDialog = new AlertDialog.Builder(activity)
                .setTitle(R.string.star_title)
                .setMessage(R.string.star_description)
                .setPositiveButton(R.string.star_positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Activity activity = getActivity();
                        if(activity == null) {
                            return;
                        }

                        final String appPackageName = activity.getPackageName(); // getPackageName() from Context or Activity object
                        try {
                            MSLog.d("go to: " + Uri.parse("market://details?id=" + appPackageName));
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                        } catch (android.content.ActivityNotFoundException e) {
                            MSLog.d("go to: " + Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName));
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                        }
                        mStarAlertDialog.dismiss();
                    }
                })
                .setNegativeButton(R.string.star_negative, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mStarAlertDialog.dismiss();

                        Activity activity = getActivity();
                        if(activity == null) {
                            return;
                        }

                        startActivity(WebViewActivity.generateWebViewActivityIntent(
                                activity,
                                R.string.preference_feedback,
                                getResources().getString(R.string.preference_feedback),
                                "https://docs.google.com/forms/d/e/1FAIpQLSfT0nDlt-Ra052pzXeG7nSjwkStnChRhyTOD5M5flRkukLWoQ/viewform"));
                    }
                })
                .show();
    }

    @Override
    public void onPause() {
        super.onPause();

        if(mLoginAlertDialog != null) {
            mLoginAlertDialog.dismiss();
            mLoginAlertDialog = null;
        }

        if(mStarAlertDialog != null) {
            mStarAlertDialog.dismiss();
            mStarAlertDialog = null;
        }
    }

    @Override
    public void onDestroyView() {
        mSettingSource.destroy();
        super.onDestroyView();
    }
}
