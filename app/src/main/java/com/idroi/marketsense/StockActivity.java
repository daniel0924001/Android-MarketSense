package com.idroi.marketsense;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
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
import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.adapter.StockScreenSlidePagerAdapter;
import com.idroi.marketsense.common.ClientData;
import com.idroi.marketsense.common.FBHelper;
import com.idroi.marketsense.common.MarketSenseCommonNavigator;
import com.idroi.marketsense.data.PostEvent;
import com.idroi.marketsense.data.Stock;
import com.idroi.marketsense.data.UserProfile;

import net.lucode.hackware.magicindicator.MagicIndicator;
import net.lucode.hackware.magicindicator.ViewPagerHelper;

import org.json.JSONObject;

import static com.idroi.marketsense.common.Constants.FACEBOOK_CONSTANTS;
import static com.idroi.marketsense.data.UserProfile.NOTIFY_ID_FAVORITE_LIST;
import static com.idroi.marketsense.data.UserProfile.NOTIFY_ID_STOCK_COMMENT_CLICK;

/**
 * Created by daniel.hsieh on 2018/4/24.
 */

public class StockActivity extends AppCompatActivity {

    public final static String EXTRA_CODE = "com.idroi.marketsense.StockActivity.extra_code";
    public final static String EXTRA_RAISE_NUM = "com.idroi.marketsense.StockActivity.extra_raise_number";
    public final static String EXTRA_FALL_NUM = "com.idroi.marketsense.StockActivity.extra_fall_number";

    private ViewPager mViewPager;
    private MagicIndicator mMagicIndicator;

    private String mStockName;
    private String mCode;
    private int mRaiseNum, mFallNum;

    private CallbackManager mFBCallbackManager;
    private UserProfile mUserProfile;
    private boolean mIsFavorite;

    private ViewPager.OnPageChangeListener mOnPageChangeListener
            = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            if(mMagicIndicator != null) {
                mMagicIndicator.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }
        }

        @Override
        public void onPageSelected(int position) {
            if(mMagicIndicator != null) {
                mMagicIndicator.onPageSelected(position);
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            if(mMagicIndicator != null) {
                mMagicIndicator.onPageScrollStateChanged(state);
            }
        }
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock);

        mUserProfile = ClientData.getInstance(this).getUserProfile();

        initFBLogin();
        setInformation();
        setActionBar();
        setViewPager();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.stop, R.anim.right_to_left);
    }

    @Override
    protected void onDestroy() {
        clearViewPager();
        super.onDestroy();
    }

    private void setInformation() {
        mStockName = getIntent().getStringExtra(Intent.EXTRA_TITLE);
        mCode = getIntent().getStringExtra(EXTRA_CODE);
        mRaiseNum = getIntent().getIntExtra(EXTRA_RAISE_NUM, 0);
        mFallNum = getIntent().getIntExtra(EXTRA_FALL_NUM, 0);

        mIsFavorite = mUserProfile.isFavoriteStock(mCode);
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
                String title = mStockName + " " + mCode;
                textView.setText(title);
            }

            final ImageView addFavorite = view.findViewById(R.id.action_bar_notification);
            if(addFavorite != null) {
                addFavorite.setVisibility(View.VISIBLE);
                if(mIsFavorite) {
                    addFavorite.setImageResource(R.drawable.ic_star_yellow_24px);
                } else {
                    addFavorite.setImageResource(R.drawable.ic_star_border_white_24px);
                }
                addFavorite.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        changeFavorite(addFavorite);
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

    private void changeFavorite(final ImageView imageView) {
        if(mIsFavorite) {
            mUserProfile.deleteFavoriteStock(mCode);
            String format = getResources().getString(R.string.title_delete_complete);
            Toast.makeText(this, String.format(format, mStockName, mCode), Toast.LENGTH_SHORT).show();
            imageView.setImageResource(R.drawable.ic_star_border_white_24px);
        } else {
            mUserProfile.addFavoriteStock(mCode);
            String format = getResources().getString(R.string.title_add_complete);
            Toast.makeText(this, String.format(format, mStockName, mCode), Toast.LENGTH_SHORT).show();
            imageView.setImageResource(R.drawable.ic_star_yellow_24px);
        }
        mIsFavorite = mUserProfile.isFavoriteStock(mCode);
        mUserProfile.notifyUserProfile(NOTIFY_ID_FAVORITE_LIST);
    }

    private void setViewPager() {
        mViewPager = findViewById(R.id.pager);
        StockScreenSlidePagerAdapter stockScreenSlidePagerAdapter =
                new StockScreenSlidePagerAdapter(
                        this, getSupportFragmentManager(),
                        mStockName, mCode, mRaiseNum, mFallNum);
        mViewPager.setAdapter(stockScreenSlidePagerAdapter);

        mMagicIndicator = (MagicIndicator) findViewById(R.id.tabs);
        MarketSenseCommonNavigator commonNavigator =
                new MarketSenseCommonNavigator(this, mViewPager,
                        stockScreenSlidePagerAdapter.getTitles());

        mViewPager.addOnPageChangeListener(mOnPageChangeListener);

        mMagicIndicator.setNavigator(commonNavigator);
        ViewPagerHelper.bind(mMagicIndicator, mViewPager);
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

    public static Intent generateStockActivityIntent(Context context,
                                                     String title, String code,
                                                     int raiseNum, int fallNum) {
        Intent intent = new Intent(context, StockActivity.class);
        intent.putExtra(Intent.EXTRA_TITLE, title);
        intent.putExtra(EXTRA_CODE, code);
        intent.putExtra(EXTRA_RAISE_NUM, raiseNum);
        intent.putExtra(EXTRA_FALL_NUM, fallNum);
        return intent;
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
                PostEvent.sendRegister(StockActivity.this, userId, userName, FACEBOOK_CONSTANTS,
                        UserProfile.generatePassword(userId, FACEBOOK_CONSTANTS), userEmail, avatarLink);
                mUserProfile.notifyUserProfile(NOTIFY_ID_STOCK_COMMENT_CLICK);
            }
        }, true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mFBCallbackManager.onActivityResult(requestCode, resultCode, data);
    }
}
