package com.idroi.marketsense;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.firebase.messaging.FirebaseMessaging;
import com.idroi.marketsense.adapter.MainPageScreenSlidePagerAdapter;
import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.adapter.BaseScreenSlidePagerAdapter;
import com.idroi.marketsense.adapter.ChoiceScreenSlidePagerAdapter;
import com.idroi.marketsense.adapter.ChoicesNewsScreenSlidePagerAdapter;
import com.idroi.marketsense.adapter.PredictScreenSlidePagerAdapter;
import com.idroi.marketsense.adapter.NewsScreenSlidePagerAdapter;
import com.idroi.marketsense.common.BottomNavigationViewHelper;
import com.idroi.marketsense.common.ClientData;
import com.idroi.marketsense.common.FBHelper;
import com.idroi.marketsense.common.FrescoHelper;
import com.idroi.marketsense.common.MarketSenseCommonNavigator;
import com.idroi.marketsense.data.PostEvent;
import com.idroi.marketsense.data.Stock;
import com.idroi.marketsense.data.UserProfile;
import com.idroi.marketsense.util.MarketSenseUtils;

import net.lucode.hackware.magicindicator.MagicIndicator;
import net.lucode.hackware.magicindicator.ViewPagerHelper;

import org.json.JSONObject;

import static com.idroi.marketsense.SearchAndResponseActivity.EXTRA_SELECTED_COMPANY_CODE_KEY;
import static com.idroi.marketsense.SearchAndResponseActivity.EXTRA_SELECTED_COMPANY_NAME_KEY;
import static com.idroi.marketsense.common.Constants.FACEBOOK_CONSTANTS;
import static com.idroi.marketsense.data.UserProfile.NOTIFY_ID_FAVORITE_LIST;
import static com.idroi.marketsense.data.UserProfile.NOTIFY_USER_HAS_LOGIN;
import static com.idroi.marketsense.data.UserProfile.NOTIFY_USER_LOGIN_FAILED;
import static com.idroi.marketsense.notification.NotificationHelper.NEWS_GENERAL_ALL;
import static com.idroi.marketsense.notification.NotificationHelper.VOTING_GENERAL_ALL;

public class MainActivity extends AppCompatActivity {

    private SwipeableViewPager mViewPager;
    private MagicIndicator mMagicIndicator;
    private FloatingActionButton mFab;
    private Button mLeftButton, mRightButton;
    private TextView mActionTitleBar;
    private int mLastSelectedItemId = -1;

    public final static int sSearchAndAddRequestCode = 1;
    public final static int sSettingRequestCode = 2;
    public final static int sSearchAndOpenRequestCode = 3;
    private SimpleDraweeView mAvatarImageView;

    // fb login part when the user click fab
    private AlertDialog mLoginAlertDialog;
    private LoginButton mFBLoginBtn;
    private CallbackManager mFBCallbackManager;

    UserProfile.UserProfileChangeListener mUserProfileChangeListener;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            if(mLastSelectedItemId != item.getItemId()) {
                mLastSelectedItemId = item.getItemId();
                switch (item.getItemId()) {
                    case R.id.navigation_main_page:
                        setViewPager(R.id.navigation_main_page);
                        return true;
                    case R.id.navigation_predict:
                        setViewPager(R.id.navigation_predict);
                        return true;
                    case R.id.navigation_news:
                        setViewPager(R.id.navigation_news);
                        return true;
                    case R.id.navigation_choices:
                        setViewPager(R.id.navigation_choices);
                        return true;
                }
            }
            return false;
        }
    };

    private ViewPager.OnPageChangeListener mOnPageChangeListener
            = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        @Override
        public void onPageSelected(int position) {
            if(position == 0) {
                setFab(false);
            } else if(position == 1) {
                setFab(true);
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {
        }
    };

    private FloatingActionButton.OnClickListener mOnFabClickListener
            = new FloatingActionButton.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(FBHelper.checkFBLogin()) {
                Intent intent = new Intent(MainActivity.this, SearchAndResponseActivity.class);
                startActivityForResult(intent, sSearchAndAddRequestCode);
                overridePendingTransition(0, 0);
            } else {
                initFBLogin();
                showLoginAlertDialog();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MSLog.i("Enter MainActivity");

        FirebaseMessaging.getInstance().subscribeToTopic(NEWS_GENERAL_ALL);
        FirebaseMessaging.getInstance().subscribeToTopic(VOTING_GENERAL_ALL);

        MSLog.i("Initialize ClientData");
        ClientData clientData = ClientData.getInstance(this);
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int width = (int)Math.ceil((double)metrics.widthPixels/metrics.density);
        int height = (int)Math.ceil((double)metrics.heightPixels/metrics.density);
        clientData.setScreenSizeInPixels(metrics.widthPixels, metrics.heightPixels);
        clientData.setScreenSize(width, height);

        mUserProfileChangeListener = new UserProfile.UserProfileChangeListener() {
            @Override
            public void onUserProfileChange(int notifyId) {
                if(notifyId == NOTIFY_USER_HAS_LOGIN) {
                    MSLog.i("[user login]: notify user login success");
                    internalSetAvatarImage(true);
                } else if(notifyId == NOTIFY_USER_LOGIN_FAILED) {
                    MSLog.e("[user login]: notify user login failed");
                    internalSetAvatarImage(false);
                    Toast.makeText(MainActivity.this, R.string.login_failed, Toast.LENGTH_SHORT).show();
                }
            }
        };

        clientData.getUserProfile().addUserProfileChangeListener(mUserProfileChangeListener);

        FrescoHelper.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        BottomNavigationViewHelper.disableShiftMode(navigation);

        mFab = findViewById(R.id.fab_add);

        setActionBar();
        setViewPager();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setAvatarImage();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if(mLoginAlertDialog != null) {
            mLoginAlertDialog.dismiss();
            mLoginAlertDialog = null;
        }
    }

    @Override
    protected void onDestroy() {
        ClientData.getInstance(this).getUserProfile().deleteUserProfileChangeListener(mUserProfileChangeListener);
        MSLog.i("Exit MainActivity");
        super.onDestroy();
        finish();
    }

    private void setAvatarImage() {
        if(mAvatarImageView != null) {
            if (FBHelper.checkFBLogin()) {
                UserProfile userProfile = ClientData.getInstance(this).getUserProfile();
                if(userProfile != null) {
                    MSLog.i("[user login]: fb is login so we try to login. it will notify NOTIFY_USER_HAS_LOGIN event when it successes");
                    userProfile.tryToLoginAndInitUserData(this);
                } else {
                    MSLog.e("User profile is null in setAvatarImage.");
                    internalSetAvatarImage(false);
                }
            } else {
                MSLog.i("[user login]: fb is not login so set avatar image to false");
                internalSetAvatarImage(false);
            }
        }
    }

    private void internalSetAvatarImage(boolean isLogin) {
        if(isLogin) {
            mAvatarImageView.setImageURI(
                    ClientData.getInstance(this).getUserProfile().getUserAvatarLink());
            RoundingParams roundingParams = RoundingParams.fromCornersRadius(5f);
            roundingParams.setRoundAsCircle(true);
            mAvatarImageView.getHierarchy().setRoundingParams(roundingParams);
        } else {
            mAvatarImageView.setImageResource(R.drawable.ic_account_circle_white_24px);
            RoundingParams roundingParams = RoundingParams.fromCornersRadius(0);
            roundingParams.setRoundAsCircle(false);
            mAvatarImageView.getHierarchy().setRoundingParams(roundingParams);
        }
    }

    private void setActionBar() {
        final ActionBar actionBar = getSupportActionBar();

        if(actionBar != null) {
            actionBar.setElevation(0);
            View view = LayoutInflater.from(actionBar.getThemedContext())
                    .inflate(R.layout.action_bar_middle_button, null);
            actionBar.setDisplayShowHomeEnabled(false);
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setCustomView(view,
                    new ActionBar.LayoutParams(
                            ActionBar.LayoutParams.MATCH_PARENT,
                            ActionBar.LayoutParams.MATCH_PARENT));
            actionBar.setDisplayShowCustomEnabled(true);

            mAvatarImageView = view.findViewById(R.id.action_bar_avatar);
            if(mAvatarImageView != null) {
                mAvatarImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(MainActivity.this, SettingActivity.class);
                        startActivityForResult(intent, sSettingRequestCode);
                        overridePendingTransition(R.anim.left_to_right, R.anim.stop);
                    }
                });
                setAvatarImage();
            }

            ImageView searchAndOpenView = view.findViewById(R.id.action_bar_search);
            if(searchAndOpenView != null) {
                searchAndOpenView.setVisibility(View.VISIBLE);
                searchAndOpenView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(MainActivity.this, SearchAndResponseActivity.class);
                        startActivityForResult(intent, sSearchAndOpenRequestCode);
                        overridePendingTransition(0, 0);
                    }
                });
            }

            mActionTitleBar = view.findViewById(R.id.action_bar_name);
            mLeftButton = view.findViewById(R.id.btn_left);
            mRightButton = view.findViewById(R.id.btn_right);
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

    private void setActionBarTwoButton(boolean clickable, final boolean switchable) {

        if(switchable && !clickable) {
            throw new IllegalStateException();
        }

        if(mLeftButton != null && mRightButton != null) {
            if(clickable) {
                mLeftButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mRightButton.setTextColor(getResources().getColor(R.color.text_white));
                        mRightButton.setBackground(getDrawable(R.drawable.btn_oval_right_black));
                        mLeftButton.setTextColor(getResources().getColor(R.color.text_black));
                        mLeftButton.setBackground(getDrawable(R.drawable.btn_oval_left_white));
                        if(switchable) {
                            mViewPager.setCurrentItem(0, false);
                        } else {
                            setViewPager(R.id.navigation_news);
                        }
                    }
                });

                mRightButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mRightButton.setTextColor(getResources().getColor(R.color.text_black));
                        mRightButton.setBackground(getDrawable(R.drawable.btn_oval_right_white));
                        mLeftButton.setTextColor(getResources().getColor(R.color.text_white));
                        mLeftButton.setBackground(getDrawable(R.drawable.btn_oval_left_black));
                        if(switchable) {
                            mViewPager.setCurrentItem(1, false);
                        } else {
                            setViewPager(SELF_CHOICE_NEWS_SLIDE_PAGER);
                        }
                    }
                });
                mLeftButton.setVisibility(View.VISIBLE);
                mRightButton.setVisibility(View.VISIBLE);
                mActionTitleBar.setVisibility(View.GONE);
            } else {
                mLeftButton.setOnClickListener(null);
                mRightButton.setOnClickListener(null);
                mLeftButton.setVisibility(View.GONE);
                mRightButton.setVisibility(View.GONE);
                mActionTitleBar.setVisibility(View.VISIBLE);
            }
        }
    }

    private void initActionBarTwoButton() {
        if(mLeftButton != null && mRightButton != null) {
            mRightButton.setTextColor(getResources().getColor(R.color.text_white));
            mRightButton.setBackground(getDrawable(R.drawable.btn_oval_right_black));
            mLeftButton.setTextColor(getResources().getColor(R.color.text_black));
            mLeftButton.setBackground(getDrawable(R.drawable.btn_oval_left_white));
        }
    }

    private void setViewPager() {
        setViewPager(R.id.navigation_main_page);
    }

    private static final int SELF_CHOICE_NEWS_SLIDE_PAGER = 1;

    private void setViewPager(int itemId) {

        clearViewPager();

        // Initialize the ViewPager and set an adapter
        mViewPager = findViewById(R.id.pager);
        mMagicIndicator = findViewById(R.id.tabs);

        BaseScreenSlidePagerAdapter baseScreenSlidePagerAdapter = null;
        switch (itemId) {
            case R.id.navigation_main_page:
                baseScreenSlidePagerAdapter =
                        new MainPageScreenSlidePagerAdapter(this, getSupportFragmentManager());
                setFab(false);
                mViewPager.setSwipeable(false);
                mMagicIndicator.setVisibility(View.GONE);
                setActionBarTwoButton(false, false);
                break;
            case R.id.navigation_predict:
                baseScreenSlidePagerAdapter =
                        new PredictScreenSlidePagerAdapter(this, getSupportFragmentManager());
                setFab(false);
                mViewPager.setSwipeable(false);
                mMagicIndicator.setVisibility(View.GONE);
                mViewPager.addOnPageChangeListener(mOnPageChangeListener);
                setActionBarTwoButton(true, true);
                initActionBarTwoButton();
                break;
            case R.id.navigation_news:
                baseScreenSlidePagerAdapter =
                        new NewsScreenSlidePagerAdapter(this, getSupportFragmentManager());
                setFab(false);
                mViewPager.setSwipeable(true);
                mMagicIndicator.setVisibility(View.VISIBLE);
                setActionBarTwoButton(true, false);
                initActionBarTwoButton();
                break;
            case SELF_CHOICE_NEWS_SLIDE_PAGER:
                baseScreenSlidePagerAdapter =
                        new ChoicesNewsScreenSlidePagerAdapter(this, getSupportFragmentManager());
                setFab(false);
                mViewPager.setSwipeable(false);
                mMagicIndicator.setVisibility(View.GONE);
                setActionBarTwoButton(true, false);
                break;
            case R.id.navigation_choices:
                baseScreenSlidePagerAdapter =
                        new ChoiceScreenSlidePagerAdapter(this, getSupportFragmentManager());
                setFab(true);
                mViewPager.setSwipeable(true);
                mMagicIndicator.setVisibility(View.VISIBLE);
                setActionBarTwoButton(false, false);
                break;
            default:
                // invalid category
                return;
        }
        mViewPager.setAdapter(baseScreenSlidePagerAdapter);

        MarketSenseCommonNavigator commonNavigator =
                new MarketSenseCommonNavigator(this, mViewPager,
                        baseScreenSlidePagerAdapter.getTitles());
        mMagicIndicator.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));

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
//                String name = data.getStringExtra(EXTRA_SELECTED_COMPANY_NAME_KEY);
                String code = data.getStringExtra(EXTRA_SELECTED_COMPANY_CODE_KEY);
                Stock stock = ClientData.getInstance(this).getPriceFromCode(code);

                if(stock == null || !MarketSenseUtils.isNetworkAvailable(this)) {
                    Toast.makeText(this, R.string.title_can_not_open_stock_page, Toast.LENGTH_SHORT).show();
                    return;
                }

                MSLog.d("try to open name: " + stock.getName() + ", code: " + stock.getCode());
                startActivity(StockActivity.generateStockActivityIntent(
                        this, stock.getName(), stock.getCode(), stock.getRaiseNum(), stock.getFallNum(),
                        stock.getPrice(), stock.getDiffNumber(), stock.getDiffPercentage()));
            }
        } else if(requestCode == sSettingRequestCode) {
            setAvatarImage();
        }
        if(mFBCallbackManager != null) {
            mFBCallbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void clearViewPager() {
        if(mViewPager != null) {
            mViewPager.clearOnPageChangeListeners();
            mViewPager.setAdapter(null);
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
        userProfile.addFavoriteStock(code);
        userProfile.notifyUserProfile(NOTIFY_ID_FAVORITE_LIST);

        String format = getResources().getString(R.string.title_add_complete);
        String name = ClientData.getInstance(this).getNameFromCode(code);
        Toast.makeText(this, String.format(format, name, code), Toast.LENGTH_SHORT).show();
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
                        UserProfile.generatePassword(userId, FACEBOOK_CONSTANTS), userEmail, avatarLink);
                setAvatarImage();
            }
        }, true);
    }

    private void showLoginAlertDialog() {
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
}
