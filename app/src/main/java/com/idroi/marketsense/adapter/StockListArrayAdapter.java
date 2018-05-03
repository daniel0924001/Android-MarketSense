package com.idroi.marketsense.adapter;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.R;
import com.idroi.marketsense.data.Stock;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by daniel.hsieh on 2018/5/3.
 */

public class StockListArrayAdapter extends ArrayAdapter<Stock> {

    private final Context mContext;
    private final List<Stock> mStockList;
    private final List<Stock> mStockListAll;
    private final int mLayoutResourceId;

    public StockListArrayAdapter(Context context, int resource, List<Stock> stockList) {
        super(context, resource, stockList);
        mContext = context;
        for(Stock stock : stockList) {
            MSLog.d("StockListArrayAdapter list: " + stock.getName() + ", " + stock.getCode());
        }
        mStockList = new ArrayList<>(stockList);
        mStockListAll = new ArrayList<>(stockList);
        mLayoutResourceId = resource;
    }

    @Override
    public int getCount() {
        return mStockList.size();
    }

    @Nullable
    @Override
    public Stock getItem(int position) {
        return mStockList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        try {
            if(convertView == null) {
                LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
                convertView = inflater.inflate(mLayoutResourceId, parent, false);
            }
            Stock stock = getItem(position);
            if(stock != null) {
                TextView name = (TextView) convertView.findViewById(R.id.marketsense_stock_simple_name_tv);
                TextView code = (TextView) convertView.findViewById(R.id.marketsense_stock_simple_code_tv);
                name.setText(stock.getName());
                code.setText(stock.getCode());
            }
        } catch (Exception e) {
            MSLog.e("Exception in StockListArrayAdapter: " + e);
        }

        return convertView;
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            public CharSequence convertResultToString(Object resultValue) {
                return ((Stock) resultValue).getName();
            }

            @Override
            protected FilterResults performFiltering(CharSequence constrain) {
                FilterResults filterResults = new FilterResults();
                List<Stock> stocksSuggestion = new ArrayList<>();
                if(constrain != null) {
                    for (Stock stock : mStockListAll) {
                        if(stock.getName().contains(constrain.toString()) ||
                                stock.getCode().contains(constrain.toString())) {
                            stocksSuggestion.add(stock);
                        }
                    }
                    filterResults.values = stocksSuggestion;
                    filterResults.count = stocksSuggestion.size();
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                mStockList.clear();
                if(results != null && results.count > 0) {
                    // avoids unchecked cast warning when using mDepartments.addAll((ArrayList<Department>) results.values);
                    for (Object object : (List<?>) results.values) {
                        if (object instanceof Stock) {
                            mStockList.add((Stock) object);
                        }
                    }
                    notifyDataSetChanged();
                } else if (constraint == null) {
                    // no filter, add entire original list back in
                    mStockList.addAll(mStockListAll);
                    notifyDataSetInvalidated();
                }
            }
        };
    }
}
