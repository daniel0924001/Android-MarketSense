package com.idroi.marketsense;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.idroi.marketsense.adapter.DiscussionScreenSlidePagerAdapter;
import com.idroi.marketsense.adapter.MainPageScreenSlidePagerAdapter;
import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.adapter.BaseScreenSlidePagerAdapter;
import com.idroi.marketsense.adapter.ChoiceScreenSlidePagerAdapter;
import com.idroi.marketsense.adapter.ProfileScreenSlidePagerAdapter;
import com.idroi.marketsense.adapter.TrendScreenSlidePagerAdapter;
import com.idroi.marketsense.adapter.NewsScreenSlidePagerAdapter;
import com.idroi.marketsense.common.ClientData;
import com.idroi.marketsense.common.FBHelper;
import com.idroi.marketsense.common.FrescoHelper;
import com.idroi.marketsense.common.MarketSenseCommonNavigator;
import com.idroi.marketsense.data.Comment;
import com.idroi.marketsense.data.PostEvent;
import com.idroi.marketsense.data.Stock;
import com.idroi.marketsense.data.UserProfile;
import com.idroi.marketsense.fragments.MainFragment;
import com.idroi.marketsense.util.ActionBarHelper;
import com.idroi.marketsense.util.MarketSenseUtils;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import net.lucode.hackware.magicindicator.MagicIndicator;
import net.lucode.hackware.magicindicator.ViewPagerHelper;

import org.json.JSONObject;

import static com.idroi.marketsense.RichEditorActivity.EXTRA_RES_EVENT_ID;
import static com.idroi.marketsense.RichEditorActivity.EXTRA_RES_HTML;
import static com.idroi.marketsense.SearchAndResponseActivity.EXTRA_SEARCH_TYPE;
import static com.idroi.marketsense.SearchAndResponseActivity.EXTRA_SELECTED_COMPANY_CODE_KEY;
import static com.idroi.marketsense.SearchAndResponseActivity.EXTRA_SELECTED_COMPANY_NAME_KEY;
import static com.idroi.marketsense.SearchAndResponseActivity.SEARCH_CODE_ONLY;
import static com.idroi.marketsense.common.Constants.FACEBOOK_CONSTANTS;
import static com.idroi.marketsense.data.UserProfile.NOTIFY_ID_FAVORITE_LIST;
import static com.idroi.marketsense.data.UserProfile.NOTIFY_ID_FUNCTION_INSERT_COMMENT;
import static com.idroi.marketsense.data.UserProfile.NOTIFY_ID_FUNCTION_SEARCH_COMMENT;
import static com.idroi.marketsense.data.UserProfile.NOTIFY_ID_MAIN_ACTIVITY_FUNCTION_CLICK;
import static com.idroi.marketsense.data.UserProfile.NOTIFY_ID_NEED_TO_APK_UPDATED;
import static com.idroi.marketsense.data.UserProfile.NOTIFY_ID_NEED_TO_REOPEN;
import static com.idroi.marketsense.data.UserProfile.NOTIFY_USER_HAS_LOGIN;
import static com.idroi.marketsense.data.UserProfile.NOTIFY_USER_LOGIN_FAILED;
import static com.idroi.marketsense.notification.NotificationHelper.NEWS_GENERAL_ALL;
import static com.idroi.marketsense.notification.NotificationHelper.USER_REGISTRATION_TOKEN_PREFIX;
import static com.idroi.marketsense.notification.NotificationHelper.VOTING_GENERAL_ALL;

public class MainActivity extends AppCompatActivity {

    private SwipeableViewPager mViewPager;
    private MagicIndicator mMagicIndicator;
    private FloatingActionButton mFab;
    private BottomNavigationViewEx mBottomNavigationView;

    private boolean mForceChangeBottomNavigation = false;
    private int mLastSelectedItemId = -1;

    public final static int sSearchAndAddRequestCode = 1;
    public final static int sSearchAndOpenRequestCode = 3;
    public final static int sSearchAndQueryCommentRequestCode = 4;
    public final static int sEditorRequestCode = 5;

    // fb login part when the user click fab
    private AlertDialog mLoginAlertDialog, mStarAlertDialog;
    private LoginButton mFBLoginBtn;
    private CallbackManager mFBCallbackManager;

    UserProfile.GlobalBroadcastListener mGlobalBroadcastListener;

    public final static int LAST_CLICK_IS_NONE = 0;
    public final static int LAST_CLICK_IS_WRITE_COMMENT = 1;
    public final static int LAST_CLICK_IS_ADD_FAVORITE = 2;
    private int mLastClick;

    private boolean mCanReturn = false;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            if(mForceChangeBottomNavigation || mLastSelectedItemId != item.getItemId()) {
                mLastSelectedItemId = item.getItemId();
                // we have to overcome when the user profile is not initialized
                // since FB is not ready.
                UserProfile userProfile = ClientData.getInstance().getUserProfile();
                userProfile.tryToLoginAndInitUserData(MainActivity.this);

                switch (item.getItemId()) {
                    case R.id.navigation_main_page:
                        setViewPager(R.id.navigation_main_page, mForceChangeBottomNavigation);
                        return true;
                    case R.id.navigation_trend:
                        setViewPager(R.id.navigation_trend, mForceChangeBottomNavigation);
                        return true;
                    case R.id.navigation_post:
                        setViewPager(R.id.navigation_post, mForceChangeBottomNavigation);
                        return true;
                    case R.id.navigation_profile:
                        setViewPager(R.id.navigation_profile, mForceChangeBottomNavigation);
                        return true;
                    case R.id.navigation_favorites:
                        setViewPager(R.id.navigation_favorites, mForceChangeBottomNavigation);
                        return true;
                }
            }
            return false;
        }
    };

    private FloatingActionButton.OnClickListener mOnFabClickListener
            = new FloatingActionButton.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(FBHelper.checkFBLogin()) {
                Intent intent = new Intent(MainActivity.this, SearchAndResponseActivity.class);
                intent.putExtra(EXTRA_SEARCH_TYPE, SEARCH_CODE_ONLY);
                startActivityForResult(intent, sSearchAndAddRequestCode);
                overridePendingTransition(0, 0);
            } else {
                showLoginAlertDialog(LAST_CLICK_IS_ADD_FAVORITE);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MSLog.i("Enter MainActivity");

        FirebaseMessaging.getInstance().subscribeToTopic(NEWS_GENERAL_ALL);
        FirebaseMessaging.getInstance().subscribeToTopic(VOTING_GENERAL_ALL);
        String userRegistrationTokenTopic = USER_REGISTRATION_TOKEN_PREFIX+FirebaseInstanceId.getInstance().getId();
        FirebaseMessaging.getInstance().subscribeToTopic(userRegistrationTokenTopic);

        MSLog.i("Initialize ClientData: " + userRegistrationTokenTopic);
        ClientData clientData = ClientData.getInstance(this);
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int width = (int)Math.ceil((double)metrics.widthPixels/metrics.density);
        int height = (int)Math.ceil((double)metrics.heightPixels/metrics.density);
        clientData.setScreenSizeInPixels(metrics.widthPixels, metrics.heightPixels);
        clientData.setScreenSize(width, height, metrics.density);

        mGlobalBroadcastListener = new UserProfile.GlobalBroadcastListener() {
            @Override
            public void onGlobalBroadcast(int notifyId, Object payload) {
                if(notifyId == NOTIFY_USER_HAS_LOGIN) {
                    MSLog.i("[user login]: notify user login success");
                } else if(notifyId == NOTIFY_USER_LOGIN_FAILED) {
                    Toast.makeText(MainActivity.this, R.string.login_failed_description, Toast.LENGTH_SHORT).show();
                    LoginManager.getInstance().logOut();
                } else if(notifyId == NOTIFY_ID_MAIN_ACTIVITY_FUNCTION_CLICK) {
                    switch (mLastClick) {
                        case LAST_CLICK_IS_WRITE_COMMENT:
                            startActivityForResult(RichEditorActivity.generateRichEditorActivityIntent(
                                    MainActivity.this, RichEditorActivity.TYPE.NO_CONTENT, null),
                                    sEditorRequestCode);
                            overridePendingTransition(R.anim.enter, R.anim.stop);
                            break;
                        case LAST_CLICK_IS_ADD_FAVORITE:
                            Intent intent = new Intent(MainActivity.this, SearchAndResponseActivity.class);
                            intent.putExtra(EXTRA_SEARCH_TYPE, SEARCH_CODE_ONLY);
                            startActivityForResult(intent, sSearchAndAddRequestCode);
                            overridePendingTransition(0, 0);
                            break;
                    }
                    mLastClick = LAST_CLICK_IS_NONE;
                } else if(notifyId == NOTIFY_ID_NEED_TO_APK_UPDATED) {
                    showStarAlertDialog(R.string.need_to_update_title,
                            R.string.need_to_update_description,
                            R.string.need_to_update_positive,
                            R.string.need_to_update_negative);
                } else if(notifyId == NOTIFY_ID_NEED_TO_REOPEN) {
                    if(mBottomNavigationView != null) {
                        mForceChangeBottomNavigation = true;
                        mBottomNavigationView.setSelectedItemId(mLastSelectedItemId);
                    }
                }
            }
        };

        clientData.getUserProfile().addGlobalBroadcastListener(mGlobalBroadcastListener);
        MarketSenseUtils.isNeedToUpdated(this);

        FrescoHelper.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);

        setBottomNavigationView();

        mFab = findViewById(R.id.fab_add);

        initFBLogin();
        mBottomNavigationView.setSelectedItemId(R.id.navigation_main_page);
    }

    @Override
    protected void onPause() {
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
    protected void onDestroy() {
        UserProfile userProfile = ClientData.getInstance(this).getUserProfile();
        userProfile.deleteGlobalBroadcastListener(mGlobalBroadcastListener);
        MSLog.i("Exit MainActivity");
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(mCanReturn) {
            mCanReturn = false;
            ActionBarHelper.setActionBarForMain(this, true);
        }
    }

    private void setFab(boolean show) {
        if(mFab != null) {
            if (show) {
                mFab.setVisibility(View.VISIBLE);
                mFab.setOnClickListener(mOnFabClickListener);
            } else {
                mFab.setVisibility(View.GONE);
                mFab.setOnClickListener(null);
            }
        }
    }

    private void setBottomNavigationView() {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        mBottomNavigationView = findViewById(R.id.navigation);
        mBottomNavigationView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        mBottomNavigationView.enableAnimation(false);
        mBottomNavigationView.enableShiftingMode(false);
        mBottomNavigationView.enableItemShiftingMode(false);
        mBottomNavigationView.setIconSize(30, 30);
        mBottomNavigationView.setTextSize(12);
        mBottomNavigationView.setIconsMarginTop((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, metrics));
    }

    private static final int SELF_NEWS_SLIDE_PAGER = 1;

    private void setViewPager(int itemId, boolean force) {

        clearViewPager();

        // Initialize the ViewPager and set an adapter
        mViewPager = findViewById(R.id.pager);
        mMagicIndicator = findViewById(R.id.tabs);

        BaseScreenSlidePagerAdapter baseScreenSlidePagerAdapter;
        switch (itemId) {
            case R.id.navigation_main_page:
                baseScreenSlidePagerAdapter =
                        new MainPageScreenSlidePagerAdapter(this, getSupportFragmentManager(), new MainFragment.OnActionBarChangeListener() {
                            @Override
                            public void onActionBarChange(String title, boolean canReturn) {
                                if(canReturn) {
                                    ActionBarHelper.setActionBarForSimpleTitleAndBack(MainActivity.this, title);
                                }
                                mCanReturn = canReturn;
                            }
                        });
                setFab(false);
                mViewPager.setSwipeable(false);
                mMagicIndicator.setVisibility(View.GONE);
                ActionBarHelper.setActionBarForMain(this, force);
                break;
            case R.id.navigation_trend:
                baseScreenSlidePagerAdapter =
                        new TrendScreenSlidePagerAdapter(this, getSupportFragmentManager());
                setFab(false);
                mViewPager.setSwipeable(false);
                mMagicIndicator.setVisibility(View.GONE);
                ActionBarHelper.setActionBarForTrend(this,
                        ActionBarHelper.ACTION_BAR_TYPE_TREND, force,
                        new ActionBarHelper.ActionBarEventNotificationListener() {
                    @Override
                    public void onEventNotification(int eventId) {
                        final ActionBar actionBar = getSupportActionBar();
                        if(eventId == R.id.btn_trend) {
                            setViewPager(R.id.navigation_trend, false);
                            if(actionBar != null) {
                                actionBar.setBackgroundDrawable(getDrawable(R.drawable.action_bar_background_with_border));
                                actionBar.getCustomView().setBackground(getDrawable(R.drawable.action_bar_background_with_border));
                            }
                        } else if(eventId == R.id.btn_news) {
                            setViewPager(SELF_NEWS_SLIDE_PAGER, false);
                            if(actionBar != null) {
                                actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.white_eight)));
                                actionBar.getCustomView().setBackground(new ColorDrawable(getResources().getColor(R.color.white_eight)));
                            }
                        }
                    }
                });
                break;
            case SELF_NEWS_SLIDE_PAGER:
                baseScreenSlidePagerAdapter =
                        new NewsScreenSlidePagerAdapter(this, getSupportFragmentManager());
                mViewPager.setSwipeable(true);
                mMagicIndicator.setVisibility(View.VISIBLE);
                ActionBarHelper.setActionBarForTrend(this,
                        ActionBarHelper.ACTION_BAR_TYPE_TREND, force,
                        new ActionBarHelper.ActionBarEventNotificationListener() {
                    @Override
                    public void onEventNotification(int eventId) {
                        final ActionBar actionBar = getSupportActionBar();
                        if(eventId == R.id.btn_trend) {
                            setViewPager(R.id.navigation_trend, false);
                            if(actionBar != null) {
                                actionBar.setBackgroundDrawable(getDrawable(R.drawable.action_bar_background_with_border));
                                actionBar.getCustomView().setBackground(getDrawable(R.drawable.action_bar_background_with_border));
                            }
                        } else if(eventId == R.id.btn_news) {
                            setViewPager(SELF_NEWS_SLIDE_PAGER, false);
                            if(actionBar != null) {
                                actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.white_eight)));
                                actionBar.getCustomView().setBackground(new ColorDrawable(getResources().getColor(R.color.white_eight)));
                            }
                        }
                    }
                });
                break;
            case R.id.navigation_favorites:
                baseScreenSlidePagerAdapter =
                        new ChoiceScreenSlidePagerAdapter(this, getSupportFragmentManager());
                setFab(true);
                mViewPager.setSwipeable(false);
                mMagicIndicator.setVisibility(View.GONE);
                ActionBarHelper.setActionBarForTrend(this,
                        ActionBarHelper.ACTION_BAR_TYPE_FAVORITE, force,
                        new ActionBarHelper.ActionBarEventNotificationListener() {
                            @Override
                            public void onEventNotification(int eventId) {
                                if(eventId == R.id.btn_trend) {
                                    mViewPager.setCurrentItem(0, false);
                                } else if(eventId == R.id.btn_news) {
                                    mViewPager.setCurrentItem(1, false);
                                }
                            }
                        });
                break;
            case R.id.navigation_post:
                baseScreenSlidePagerAdapter =
                        new DiscussionScreenSlidePagerAdapter(this, getSupportFragmentManager());
                setFab(false);
                mViewPager.setSwipeable(false);
                mMagicIndicator.setVisibility(View.GONE);
                ActionBarHelper.setActionBarForDiscussion(this, force, new ActionBarHelper.ActionBarEventNotificationListener() {
                    @Override
                    public void onEventNotification(int eventId) {
                        if(eventId == R.id.action_bar_post) {
                            showLoginAlertDialog(LAST_CLICK_IS_WRITE_COMMENT);
                        }
                    }
                });
                break;
            case R.id.navigation_profile:
                baseScreenSlidePagerAdapter =
                        new ProfileScreenSlidePagerAdapter(this, getSupportFragmentManager());
                setFab(false);
                mViewPager.setSwipeable(false);
                mMagicIndicator.setVisibility(View.GONE);
                ActionBarHelper.setActionBarForProfile(this, force);
                break;
            default:
                // invalid category
                return;
        }
        mViewPager.setAdapter(baseScreenSlidePagerAdapter);

        MarketSenseCommonNavigator commonNavigator =
                new MarketSenseCommonNavigator(this, mViewPager,
                        baseScreenSlidePagerAdapter.getTitles());
        mMagicIndicator.setBackgroundColor(getResources().getColor(R.color.white_eight));

        mMagicIndicator.setNavigator(commonNavigator);
        ViewPagerHelper.bind(mMagicIndicator, mViewPager);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == sSearchAndAddRequestCode) {
            if(resultCode == RESULT_OK) {
                String name = data.getStringExtra(EXTRA_SELECTED_COMPANY_NAME_KEY);
                String code = data.getStringExtra(EXTRA_SELECTED_COMPANY_CODE_KEY);
                MSLog.d("select favorite stock name: " + name + ", code: " + code);
                addFavoriteStock(code);
            }
        } else if (requestCode == sSearchAndOpenRequestCode) {
            if(resultCode == RESULT_OK) {
                String name = data.getStringExtra(EXTRA_SELECTED_COMPANY_NAME_KEY);
                String code = data.getStringExtra(EXTRA_SELECTED_COMPANY_CODE_KEY);
                Stock stock = ClientData.getInstance(this).getPriceFromCode(code);

                if(stock == null || !MarketSenseUtils.isNetworkAvailable(this)) {
                    String format = getResources().getString(R.string.title_can_not_open_stock_page);
                    Toast.makeText(this, String.format(format, name), Toast.LENGTH_SHORT).show();
                    return;
                }

                MSLog.d("try to open name: " + stock.getName() + ", code: " + stock.getCode());
                startActivity(StockActivity.generateStockActivityIntent(
                        this, stock.getName(), stock.getCode(), stock.getRaiseNum(), stock.getFallNum(),
                        stock.getPrice(), stock.getDiffNumber(), stock.getDiffPercentage()));
            }
        } else if(requestCode == sSearchAndQueryCommentRequestCode) {
            if(resultCode == RESULT_OK) {
                String code = data.getStringExtra(EXTRA_SELECTED_COMPANY_CODE_KEY);
                String name = data.getStringExtra(EXTRA_SELECTED_COMPANY_NAME_KEY);

                MSLog.d("try to search name: " + name + ", code: " + code);
                UserProfile userProfile = ClientData.getInstance(this).getUserProfile();
                userProfile.globalBroadcast(NOTIFY_ID_FUNCTION_SEARCH_COMMENT, code);
            }
        } else if(requestCode == sEditorRequestCode) {
            if(resultCode == RESULT_OK) {
                String html = data.getStringExtra(EXTRA_RES_HTML);
                String eventId = data.getStringExtra(EXTRA_RES_EVENT_ID);

                Comment newComment = new Comment();
                newComment.setCommentId(eventId);
                newComment.setCommentHtml(html);
                UserProfile userProfile = ClientData.getInstance(this).getUserProfile();
                userProfile.globalBroadcast(NOTIFY_ID_FUNCTION_INSERT_COMMENT, newComment);
            }
        }

        if(mFBCallbackManager != null) {
            mFBCallbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void clearViewPager() {
        mForceChangeBottomNavigation = false;
        if(mViewPager != null) {
            mViewPager.clearOnPageChangeListeners();
            mViewPager.setAdapter(null);
            mViewPager.removeAllViewsInLayout();
            mViewPager = null;
        }
        if(mMagicIndicator != null) {
            mMagicIndicator.setNavigator(null);
            mMagicIndicator = null;
        }
    }

    private void addFavoriteStock(String code) {
        PostEvent.sendFavoriteStocksAdd(this, code);
        UserProfile userProfile = ClientData.getInstance(this).getUserProfile();
        boolean isSuccessful = userProfile.addFavoriteStock(code);
        String name = ClientData.getInstance(this).getNameFromCode(code);
        if(isSuccessful) {
            userProfile.globalBroadcast(NOTIFY_ID_FAVORITE_LIST);
            String format = getResources().getString(R.string.title_add_complete);
            Toast.makeText(this, String.format(format, name, code), Toast.LENGTH_SHORT).show();

            if(userProfile.canShowStarDialog(this)) {
                showStarAlertDialog(R.string.star_title,
                        R.string.star_description_simple,
                        R.string.star_positive,
                        R.string.star_negative_cancel);
            }
        } else {
            String format = getResources().getString(R.string.title_add_duplicated);
            Toast.makeText(this, String.format(format, name, code), Toast.LENGTH_SHORT).show();
        }
    }

    // fb login part when the user click fab
    private void initFBLogin() {

        MSLog.d("The user has logged in Facebook: " + FBHelper.checkFBLogin());

        mFBCallbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(mFBCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                MSLog.d("facebook registerCallback onSuccess in MainActivity");
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
                PostEvent.sendRegister(MainActivity.this, userId, userName, FACEBOOK_CONSTANTS,
                        UserProfile.generatePassword(userId, FACEBOOK_CONSTANTS), userEmail, avatarLink, new PostEvent.PostEventListener() {
                            @Override
                            public void onResponse(boolean isSuccessful, Object data) {
                                if(!isSuccessful) {
                                    MSLog.e("[user login]: notify user login failed");
                                    Toast.makeText(MainActivity.this, R.string.login_failed_description, Toast.LENGTH_SHORT).show();
                                    LoginManager.getInstance().logOut();
                                } else {
                                    UserProfile userProfile = ClientData.getInstance(MainActivity.this).getUserProfile();
                                    userProfile.globalBroadcast(NOTIFY_ID_MAIN_ACTIVITY_FUNCTION_CLICK);
                                }
                            }
                        });
            }
        });
    }

    private void showLoginAlertDialog(int lastClick) {
        mLastClick = lastClick;
        if(mLoginAlertDialog != null) {
            mLoginAlertDialog.dismiss();
            mLoginAlertDialog = null;
        }

        final View alertView = LayoutInflater.from(MainActivity.this)
                .inflate(R.layout.alertdialog_login, null);
        mLoginAlertDialog = new AlertDialog.Builder(MainActivity.this)
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
    // end of fb login

    private void showStarAlertDialog(int titleId, int descriptionId, int positiveStringId, int negativeStringId) {
        if(mStarAlertDialog != null) {
            mStarAlertDialog.dismiss();
            mStarAlertDialog = null;
        }

        mStarAlertDialog = new AlertDialog.Builder(this)
                .setTitle(titleId)
                .setMessage(descriptionId)
                .setPositiveButton(positiveStringId, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
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
                .setNegativeButton(negativeStringId, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mStarAlertDialog.dismiss();
                    }
                })
                .show();
    }
}
