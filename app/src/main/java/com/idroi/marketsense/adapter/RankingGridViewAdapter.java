package com.idroi.marketsense.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.R;
import com.idroi.marketsense.data.Stock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by daniel.hsieh on 2018/6/25.
 */

public class RankingGridViewAdapter extends BaseAdapter {

    public interface OnItemClickListener {
        void onItemClick(Stock stock);
    }

    public static final int RANKING_BY_TECH = 1;
    public static final int RANKING_BY_NEWS = 2;

    private Context mContext;
    private ArrayList<Stock> mStockList;
    private int mRankType;

    private OnItemClickListener mOnItemClickListener;

    public RankingGridViewAdapter(Context context, ArrayList<Stock> stockList, int rankType) {
        mContext = context;
        mStockList = new ArrayList<>(stockList);
        mRankType = rankType;

        Collections.sort(mStockList, genComparator());
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    @Override
    public int getCount() {
        if(mStockList == null) {
            return 0;
        } else {
            return Math.min(mStockList.size(), 6);
        }
    }

    @Override
    public Object getItem(int position) {
        return mStockList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        RankingGridViewHolder rankingGridViewHolder = null;
        if(convertView == null) {
            convertView = LayoutInflater.from(mContext)
                    .inflate(R.layout.ranking_grid_item, parent, false);
            rankingGridViewHolder = RankingGridViewHolder.convertToViewHolder(convertView);
            convertView.setTag(rankingGridViewHolder);
        }

        rankingGridViewHolder = (RankingGridViewHolder) convertView.getTag();
        final Stock stock = (Stock) getItem(position);
        if(stock != null) {
            rankingGridViewHolder.nameView.setText(stock.getName());
            rankingGridViewHolder.priceView.setText(stock.getPrice());
            setRankingIcon(rankingGridViewHolder.iconView, stock);
        }
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(stock);
                }
            }
        });

        return convertView;
    }

    private void setRankingIcon(ImageView imageView, Stock stock) {
        int direction = Stock.TREND_UP;
        switch (mRankType) {
            case RANKING_BY_TECH:
                direction = stock.getPredictTechDirection();
                break;
            case RANKING_BY_NEWS:
                direction = stock.getConfidenceDirection();
                break;
        }

        if(direction == Stock.TREND_DOWN) {
            imageView.setImageResource(R.mipmap.ic_direction_down);
        } else {
            imageView.setImageResource(R.mipmap.ic_direction_up);
        }
    }

    private Comparator<Stock> genComparator() {
        return new Comparator<Stock>() {
            @Override
            public int compare(Stock stock1, Stock stock2) {
                switch (mRankType) {
                    case RANKING_BY_TECH:
                        return compareValue(Math.abs(stock2.getPredictionTechScore()), Math.abs(stock1.getPredictionTechScore()));
                    case RANKING_BY_NEWS:
                        return compareValue(Math.abs(stock2.getPredictNewsScore()), Math.abs(stock1.getPredictNewsScore()));
                    default:
                        return 0;
                }
            }
        };
    }

    private int compareValue(float double1, float double2) {
        if(double1 > double2) {
            return 1;
        } else if(double1 == double2) {
            return 0;
        } else {
            return -1;
        }
    }
}
