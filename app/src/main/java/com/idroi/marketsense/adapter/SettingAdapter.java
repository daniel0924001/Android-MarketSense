package com.idroi.marketsense.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.R;
import com.idroi.marketsense.common.ClientData;
import com.idroi.marketsense.common.FBHelper;
import com.idroi.marketsense.common.MarketSenseRendererHelper;
import com.idroi.marketsense.data.UserProfile;
import com.idroi.marketsense.datasource.SettingSource;

import java.lang.ref.WeakReference;

import static com.idroi.marketsense.common.Constants.SHARED_PREFERENCE_USER_SETTING;
import static com.idroi.marketsense.common.Constants.USER_SETTING_NOTIFICATION_KEY;

/**
 * Created by daniel.hsieh on 2018/4/30.
 */

public class SettingAdapter extends RecyclerView.Adapter {

    public interface SettingOnClickListener {
        void onLoginBtnClick();
        void onSwitchClick(boolean isChecked);
        void onItemClick(int position);
    }

    private WeakReference<Activity> mActivity;

    public static final int TYPE_USER = 0;
    public static final int TYPE_OTHER = 1;
    public static final int TYPE_SWITCH = 2;
    public static final int TYPE_NO_DRAWABLE = 3;
    private static final int TYPE_MAX_COUNT = TYPE_NO_DRAWABLE + 1;

    private SettingOnClickListener mSettingOnClickListener;
    private SettingSource mSettingSource;

    private View mLogoutItemView;
    private boolean mIsLogin = false;
    private String mUserName;
    private String mAvatarLink;

    public SettingAdapter(Activity activity, SettingSource settingSource, View logoutItem) {
        mActivity = new WeakReference<>(activity);
        mSettingSource = settingSource;
        mLogoutItemView = logoutItem;

        mIsLogin = FBHelper.checkFBLogin();
        UserProfile userProfile = ClientData.getInstance(activity).getUserProfile();
        if(userProfile != null) {
            mUserName = userProfile.getUserName();
            mAvatarLink = userProfile.getUserAvatarLink();
        }
        initialLogoutView();
    }

    private void initialLogoutView() {
        Activity activity = mActivity.get();
        if(activity == null) {
            return;
        }

        mLogoutItemView.setBackground(activity.getDrawable(R.drawable.border_on_top_and_bottom_layout));
        mLogoutItemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mSettingOnClickListener != null) {
                    mSettingOnClickListener.onLoginBtnClick();
                }
            }
        });
        TextView loginStatusTextView = mLogoutItemView.findViewById(R.id.setting_title_tv);
        loginStatusTextView.setText(R.string.logout);
    }

    public void setSettingOnClickListener(SettingOnClickListener listener) {
        mSettingOnClickListener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        return mSettingSource.getItem(position).getType();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        switch (viewType) {
            case TYPE_USER:
                return new MarketSenseViewHolder(LayoutInflater.from(mActivity.get())
                        .inflate(R.layout.setting_list_item_user, viewGroup, false));
            case TYPE_OTHER:
                return new MarketSenseViewHolder(LayoutInflater.from(mActivity.get())
                        .inflate(R.layout.setting_list_item, viewGroup, false));
            case TYPE_SWITCH:
                return new MarketSenseViewHolder(LayoutInflater.from(mActivity.get())
                        .inflate(R.layout.setting_list_item_switch, viewGroup, false));
            case TYPE_NO_DRAWABLE:
                return new MarketSenseViewHolder(LayoutInflater.from(mActivity.get())
                        .inflate(R.layout.setting_list_item_no_drawable, viewGroup, false));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        int viewType = getItemViewType(position);
        SettingViewHolder settingViewHolder = null;
        switch (viewType) {
            case TYPE_USER:
                Button LoginBtn = holder.itemView.findViewById(R.id.setting_login_btn);
                LoginBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(mSettingOnClickListener != null) {
                            mSettingOnClickListener.onLoginBtnClick();
                        }
                    }
                });
                internalRefreshFBUi(holder.itemView);
                break;
            case TYPE_OTHER:
                settingViewHolder = SettingViewHolder.convertToViewHolder(holder.itemView);
                holder.itemView.setTag(settingViewHolder);
                break;
            case TYPE_SWITCH:
                settingViewHolder = SettingViewHolder.convertToViewHolder(holder.itemView);
                holder.itemView.setTag(settingViewHolder);
                break;
            case TYPE_NO_DRAWABLE:
                settingViewHolder = SettingViewHolder.convertToViewHolder(holder.itemView);
                holder.itemView.setTag(settingViewHolder);
                break;
        }

        if(holder.itemView.getTag() != null) {
            settingViewHolder = (SettingViewHolder) holder.itemView.getTag();

            if(settingViewHolder.titleView != null) {
                settingViewHolder.titleView.setText(getTitle(position));
            }

            if(settingViewHolder.iconView != null) {
                settingViewHolder.iconView.setImageDrawable(getDrawable(position));
            }

            if(settingViewHolder.switchView != null) {
                boolean isNotificationSetting = true;
                Activity activity = mActivity.get();
                if(activity != null) {
                    SharedPreferences sharedPreferences =
                            activity.getSharedPreferences(SHARED_PREFERENCE_USER_SETTING, Context.MODE_PRIVATE);
                    isNotificationSetting = sharedPreferences.getBoolean(USER_SETTING_NOTIFICATION_KEY, true);
                }
                settingViewHolder.switchView.setChecked(isNotificationSetting);
                settingViewHolder.switchView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                        if(mSettingOnClickListener != null) {
                            mSettingOnClickListener.onSwitchClick(isChecked);
                        }
                    }
                });
            }

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mSettingOnClickListener.onItemClick(holder.getAdapterPosition());
                }
            });

            if(isLargeMargin(position)) {
                float density = ClientData.getInstance().getScreenDensity();
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) holder.itemView.getLayoutParams();
                params.setMargins(0, (int) (density * 24), 0, 0);
                holder.itemView.setLayoutParams(params);

                if(settingViewHolder.divider != null) {
                    settingViewHolder.divider.setVisibility(View.GONE);
                }
            }

            if(hideArrow(position)) {
                if(settingViewHolder.gotoView != null) {
                    settingViewHolder.gotoView.setVisibility(View.GONE);
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return mSettingSource.getSize();
    }

    private String getTitle(int position) {
        return mSettingSource.getTitle(position);
    }

    private Drawable getDrawable(int position) {
        return mSettingSource.getDrawable(position);
    }

    private boolean isLargeMargin(int position) {
        return mSettingSource.isLargeMargin(position);
    }

    private boolean hideArrow(int position) {
        return mSettingSource.hideArrow(position);
    }

    public void notifyUserLogin(String userName, String avatarLink) {
        boolean currentIsLogin = FBHelper.checkFBLogin();
        mUserName = userName;
        mAvatarLink = avatarLink;
        MSLog.d("notifyUserLogin currentIsLogin: " + currentIsLogin);
        if(mIsLogin != currentIsLogin) {
            mIsLogin = currentIsLogin;
            notifyItemChanged(0);
        }
    }

    private void internalRefreshFBUi(View view) {
        SimpleDraweeView imageView = view.findViewById(R.id.user_name_block_avatar);
        TextView userNameTextView = view.findViewById(R.id.user_name_tv);
        TextView secondLineTextView = view.findViewById(R.id.user_second_line_tv);

        Button button = view.findViewById(R.id.setting_login_btn);
        if(mIsLogin) {
            if(mAvatarLink != null) {
                imageView.setImageURI(Uri.parse(mAvatarLink));
            }
            MarketSenseRendererHelper.addTextView(userNameTextView, mUserName);
            MarketSenseRendererHelper.addTextView(secondLineTextView,
                    view.getResources().getString(R.string.second_line_state_login));
            button.setVisibility(View.GONE);

            mLogoutItemView.setVisibility(View.VISIBLE);
        } else {
            imageView.setImageResource(R.drawable.ic_account_circle_gray_24px);
            MarketSenseRendererHelper.addTextView(userNameTextView,
                    view.getResources().getString(R.string.first_line_state_not_login));
            MarketSenseRendererHelper.addTextView(secondLineTextView,
                    view.getResources().getString(R.string.second_line_state_not_login));
            button.setVisibility(View.VISIBLE);
            button.setText(R.string.login);

            mLogoutItemView.setVisibility(View.GONE);
        }
    }
}
