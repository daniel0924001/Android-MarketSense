package com.idroi.marketsense.common;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v4.view.ViewPager;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import com.idroi.marketsense.R;

import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.indicators.LinePagerIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.ColorTransitionPagerTitleView;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.CommonPagerTitleView;

/**
 * Created by daniel.hsieh on 2018/4/23.
 */

public class MarketSenseCommonNavigator extends CommonNavigator{

    private final String[] mTitles;
    private final CommonNavigatorAdapter mCommonNavigatorAdapter;
    private final ViewPager mViewPager;

    public MarketSenseCommonNavigator(Context context, ViewPager viewPager, String[] titles) {
        super(context);
        mTitles = titles;
        mViewPager = viewPager;
        mCommonNavigatorAdapter = new CommonNavigatorAdapter() {
            @Override
            public int getCount() {
                return mTitles.length;
            }

            @Override
            public IPagerTitleView getTitleView(Context context, final int index) {
                CommonPagerTitleView commonPagerTitleView = new CommonPagerTitleView(context);
                commonPagerTitleView.setContentView(R.layout.simple_pager_title_layout);

                final TextView textview = (TextView) commonPagerTitleView.findViewById(R.id.title_text);
                textview.setText(mTitles[index]);
                textview.setTextColor(Color.parseColor("#4a4a4a"));

                commonPagerTitleView.setOnPagerTitleChangeListener(new CommonPagerTitleView.OnPagerTitleChangeListener() {
                    @Override
                    public void onSelected(int i, int i1) {
                        textview.setTypeface(Typeface.DEFAULT_BOLD, Typeface.BOLD);
                    }

                    @Override
                    public void onDeselected(int i, int i1) {
                        textview.setTypeface(Typeface.DEFAULT, Typeface.NORMAL);
                    }

                    @Override
                    public void onLeave(int i, int i1, float v, boolean b) {

                    }

                    @Override
                    public void onEnter(int i, int i1, float v, boolean b) {

                    }
                });
                commonPagerTitleView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mViewPager.setCurrentItem(index);
                    }
                });
                return commonPagerTitleView;
            }

            @Override
            public IPagerIndicator getIndicator(Context context) {
                LinePagerIndicator indicator = new LinePagerIndicator(context);
                indicator.setMode(LinePagerIndicator.MODE_WRAP_CONTENT);
                indicator.setColors(Color.parseColor("#F74343"));
                return indicator;
            }
        };
        this.setAdapter(mCommonNavigatorAdapter);
    }
}
