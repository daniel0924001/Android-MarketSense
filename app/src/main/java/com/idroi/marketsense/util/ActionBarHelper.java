package com.idroi.marketsense.util;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.idroi.marketsense.MainActivity;
import com.idroi.marketsense.R;
import com.idroi.marketsense.RichEditorActivity;
import com.idroi.marketsense.SearchAndResponseActivity;
import com.idroi.marketsense.common.FBHelper;

import java.lang.ref.WeakReference;

import static com.idroi.marketsense.MainActivity.sSearchAndOpenRequestCode;

/**
 * Created by daniel.hsieh on 2018/8/22.
 */

public class ActionBarHelper {

    public interface ActionBarEventNotificationListener {
        void onEventNotification(int eventId);
    }

    public final static int ACTION_BAR_INITIAL = 0;
    public final static int ACTION_BAR_TYPE_MAIN = 1;
    public final static int ACTION_BAR_TYPE_TREND = 2;
    public final static int ACTION_BAR_TYPE_FAVORITE = 3;
    public final static int ACTION_BAR_TYPE_POST = 4;
    public final static int ACTION_BAR_TYPE_PROFILE = 5;

    public final static int ACTION_HAS_SELECTOR = 100;
    public final static int ACTION_NO_SELECTOR = 0;

    private static volatile ActionBarHelper sInstance;

    private int mCurrentActionBarType;

    public static ActionBarHelper getInstance() {
        // Use a local variable so we can reduce accesses of the volatile field.
        ActionBarHelper result = sInstance;
        if (result == null) {
            synchronized (ActionBarHelper.class) {
                result = sInstance;
                if(result == null) {
                    result = new ActionBarHelper();
                    sInstance = result;
                }
            }
        }
        return result;
    }

    private ActionBarHelper() {
        mCurrentActionBarType = ACTION_BAR_INITIAL;
    }

    private boolean checkSameTypeAndChangeType(int type) {
        boolean equal = mCurrentActionBarType == type;
        mCurrentActionBarType = type;
        return equal;
    }

    private void internalSetCurrentActionBarType(int type) {
        mCurrentActionBarType = type;
    }

    private static boolean isSameTypeAndChangeType(int type) {
        ActionBarHelper actionBarHelper = ActionBarHelper.getInstance();
        return actionBarHelper.checkSameTypeAndChangeType(type);
    }

    private static void setCurrentActionBarType(int type) {
        ActionBarHelper actionBarHelper = ActionBarHelper.getInstance();
        actionBarHelper.internalSetCurrentActionBarType(type);
    }

    public static void setActionBarForMain(AppCompatActivity activity, boolean forceRefresh) {

        if(!forceRefresh && isSameTypeAndChangeType(ACTION_BAR_TYPE_MAIN)) {
            return;
        }
        setCurrentActionBarType(ACTION_BAR_TYPE_MAIN);

        final ActionBar actionBar = activity.getSupportActionBar();

        final WeakReference<AppCompatActivity> activityWeakReference
                = new WeakReference<AppCompatActivity>(activity);

        if(actionBar != null) {
            actionBar.setElevation(0);
            actionBar.setBackgroundDrawable(
                    activity.getDrawable(R.drawable.action_bar_background_with_border));
            View view = LayoutInflater.from(actionBar.getThemedContext())
                    .inflate(R.layout.action_bar_home, null);
            actionBar.setDisplayShowHomeEnabled(false);
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setCustomView(view,
                    new ActionBar.LayoutParams(
                            ActionBar.LayoutParams.MATCH_PARENT,
                            ActionBar.LayoutParams.MATCH_PARENT));
            actionBar.setDisplayShowCustomEnabled(true);

            ImageButton searchImageButton = view.findViewById(R.id.action_bar_search);
            if(searchImageButton != null) {
                searchImageButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        AppCompatActivity appCompatActivity = activityWeakReference.get();
                        if(appCompatActivity != null) {
                            Intent intent = new Intent(appCompatActivity, SearchAndResponseActivity.class);
                            appCompatActivity.startActivityForResult(intent, sSearchAndOpenRequestCode);
                            appCompatActivity.overridePendingTransition(0, 0);
                        }
                    }
                });
            }
        }
    }

    public static void setActionBarForTrend(AppCompatActivity activity,
                                            int typeId, boolean forceRefresh,
                                            final ActionBarEventNotificationListener eventNotificationListener) {

        if(!forceRefresh && isSameTypeAndChangeType(typeId)) {
            return;
        }
        setCurrentActionBarType(typeId);

        final ActionBar actionBar = activity.getSupportActionBar();

        final WeakReference<AppCompatActivity> activityWeakReference
                = new WeakReference<AppCompatActivity>(activity);

        if(actionBar != null) {
            actionBar.setElevation(0);
            actionBar.setBackgroundDrawable(
                    activity.getDrawable(R.drawable.action_bar_background_with_border));

            View view = LayoutInflater.from(actionBar.getThemedContext())
                    .inflate(R.layout.action_bar_trend, null);

            actionBar.setDisplayShowHomeEnabled(false);
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setCustomView(view,
                    new ActionBar.LayoutParams(
                            ActionBar.LayoutParams.MATCH_PARENT,
                            ActionBar.LayoutParams.MATCH_PARENT));
            actionBar.setDisplayShowCustomEnabled(true);

            ImageButton searchImageButton = view.findViewById(R.id.action_bar_search);
            if(searchImageButton != null) {
                searchImageButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        AppCompatActivity appCompatActivity = activityWeakReference.get();
                        if(appCompatActivity != null) {
                            Intent intent = new Intent(appCompatActivity, SearchAndResponseActivity.class);
                            appCompatActivity.startActivityForResult(intent, sSearchAndOpenRequestCode);
                            appCompatActivity.overridePendingTransition(0, 0);
                        }
                    }
                });
            }

            final Button trendButton = view.findViewById(R.id.btn_trend);
            final Button newsButton = view.findViewById(R.id.btn_news);

            trendButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AppCompatActivity appCompatActivity = activityWeakReference.get();
                    if(appCompatActivity != null) {
                        newsButton.setTextColor(appCompatActivity.getResources().getColor(R.color.text_black));
                        newsButton.setBackground(appCompatActivity.getDrawable(R.drawable.btn_oval_right_not_selected));
                        newsButton.setEnabled(true);
                        trendButton.setTextColor(appCompatActivity.getResources().getColor(R.color.text_white));
                        trendButton.setBackground(appCompatActivity.getDrawable(R.drawable.btn_oval_left_selected));
                        trendButton.setEnabled(false);

                        if(eventNotificationListener != null) {
                            eventNotificationListener.onEventNotification(R.id.btn_trend);
                        }
                    }
                }
            });

            newsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AppCompatActivity appCompatActivity = activityWeakReference.get();
                    if(appCompatActivity != null) {
                        trendButton.setTextColor(appCompatActivity.getResources().getColor(R.color.text_black));
                        trendButton.setBackground(appCompatActivity.getDrawable(R.drawable.btn_oval_left_not_selected));
                        trendButton.setEnabled(true);
                        newsButton.setTextColor(appCompatActivity.getResources().getColor(R.color.text_white));
                        newsButton.setBackground(appCompatActivity.getDrawable(R.drawable.btn_oval_right_selected));
                        newsButton.setEnabled(false);

                        if(eventNotificationListener != null) {
                            eventNotificationListener.onEventNotification(R.id.btn_news);
                        }
                    }
                }
            });
        }
    }

    public static void setActionBarForDiscussion(AppCompatActivity activity,
                                                 boolean forceRefresh,
                                                 final ActionBarEventNotificationListener eventNotificationListener) {

        if(!forceRefresh && isSameTypeAndChangeType(ACTION_BAR_TYPE_POST)) {
            return;
        }
        setCurrentActionBarType(ACTION_BAR_TYPE_POST);

        final ActionBar actionBar = activity.getSupportActionBar();

        final WeakReference<AppCompatActivity> activityWeakReference
                = new WeakReference<AppCompatActivity>(activity);

        if(actionBar != null) {
            actionBar.setElevation(0);
            actionBar.setBackgroundDrawable(
                    activity.getDrawable(R.drawable.action_bar_background_with_border));
            View view = LayoutInflater.from(actionBar.getThemedContext())
                    .inflate(R.layout.action_bar_discussion, null);
            actionBar.setDisplayShowHomeEnabled(false);
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setCustomView(view,
                    new ActionBar.LayoutParams(
                            ActionBar.LayoutParams.MATCH_PARENT,
                            ActionBar.LayoutParams.MATCH_PARENT));
            actionBar.setDisplayShowCustomEnabled(true);

            ImageButton searchImageButton = view.findViewById(R.id.action_bar_search);
            if(searchImageButton != null) {
                searchImageButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        AppCompatActivity appCompatActivity = activityWeakReference.get();
                        if(appCompatActivity != null) {
                            Intent intent = new Intent(appCompatActivity, SearchAndResponseActivity.class);
                            appCompatActivity.startActivityForResult(intent, sSearchAndOpenRequestCode);
                            appCompatActivity.overridePendingTransition(0, 0);
                        }
                    }
                });
            }

            ImageButton postImageButton = view.findViewById(R.id.action_bar_post);
            if(postImageButton != null) {
                postImageButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        AppCompatActivity appCompatActivity = activityWeakReference.get();
                        if(appCompatActivity != null) {
                            if(FBHelper.checkFBLogin()) {
                                appCompatActivity.startActivityForResult(RichEditorActivity.generateRichEditorActivityIntent(
                                        appCompatActivity, RichEditorActivity.TYPE.NO_CONTENT, null),
                                        MainActivity.sEditorRequestCode);
                                appCompatActivity.overridePendingTransition(R.anim.enter, R.anim.stop);
                            } else {
                                if(eventNotificationListener != null) {
                                    eventNotificationListener.onEventNotification(R.id.action_bar_post);
                                }
                            }
                        }
                    }
                });
            }
        }
    }

    public static void setActionBarForProfile(AppCompatActivity activity, boolean forceRefresh) {

        if(!forceRefresh && isSameTypeAndChangeType(ACTION_BAR_TYPE_PROFILE)) {
            return;
        }
        setCurrentActionBarType(ACTION_BAR_TYPE_PROFILE);

        final ActionBar actionBar = activity.getSupportActionBar();

        if(actionBar != null) {
            actionBar.setElevation(0);
            actionBar.setBackgroundDrawable(
                    activity.getDrawable(R.drawable.action_bar_background_with_border));
            View view = LayoutInflater.from(actionBar.getThemedContext())
                    .inflate(R.layout.action_bar_profile, null);
            actionBar.setDisplayShowHomeEnabled(false);
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setCustomView(view,
                    new ActionBar.LayoutParams(
                            ActionBar.LayoutParams.MATCH_PARENT,
                            ActionBar.LayoutParams.MATCH_PARENT));
            actionBar.setDisplayShowCustomEnabled(true);
        }
    }

    public static void setActionBarForSimpleTitleAndBack(AppCompatActivity activity,
                                                         String actionBarTitle) {

        final ActionBar actionBar = activity.getSupportActionBar();

        final WeakReference<AppCompatActivity> activityWeakReference
                = new WeakReference<AppCompatActivity>(activity);

        if(actionBar != null) {
            actionBar.setElevation(0);
            actionBar.setBackgroundDrawable(
                    activity.getDrawable(R.drawable.action_bar_background_with_border));
            View view = LayoutInflater.from(actionBar.getThemedContext())
                    .inflate(R.layout.action_bar_simple_title_and_back, null);

            ImageView imageView = view.findViewById(R.id.action_bar_back);
            if(imageView != null) {
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        AppCompatActivity appCompatActivity = activityWeakReference.get();
                        if(appCompatActivity != null) {
                            appCompatActivity.onBackPressed();
                        }
                    }
                });
            }

            TextView textView = view.findViewById(R.id.action_bar_title);
            if(textView != null) {
                textView.setText(actionBarTitle);
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

    public static void setActionBarForRightImage(AppCompatActivity activity) {
        final ActionBar actionBar = activity.getSupportActionBar();

        final WeakReference<AppCompatActivity> activityWeakReference
                = new WeakReference<AppCompatActivity>(activity);

        if(actionBar != null) {
            actionBar.setElevation(0);
            actionBar.setBackgroundDrawable(
                    activity.getDrawable(R.drawable.action_bar_background_with_border));
            View view = LayoutInflater.from(actionBar.getThemedContext())
                    .inflate(R.layout.action_bar_right_image, null);

            ImageView imageView = view.findViewById(R.id.action_bar_back);
            if(imageView != null) {
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        AppCompatActivity appCompatActivity = activityWeakReference.get();
                        if(appCompatActivity != null) {
                            appCompatActivity.onBackPressed();
                        }
                    }
                });
            }

            TextView titleTextView = view.findViewById(R.id.action_bar_title);
            if(titleTextView != null) {
                titleTextView.setText(R.string.preference_knowledge);
            }

            ImageView searchImageView = view.findViewById(R.id.action_bar_action);
            if(searchImageView != null) {
                searchImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // TODO: search
                    }
                });
                searchImageView.setVisibility(View.GONE);
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
}
