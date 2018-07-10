package com.idroi.marketsense.adapter;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.R;
import com.idroi.marketsense.common.ClientData;
import com.idroi.marketsense.data.PostEvent;
import com.idroi.marketsense.data.Stock;
import com.idroi.marketsense.data.UserProfile;
import com.idroi.marketsense.datasource.StockListPlacer;

import static com.idroi.marketsense.data.UserProfile.NOTIFY_ID_FAVORITE_LIST;
import static com.idroi.marketsense.fragments.StockListFragment.SELF_CHOICES_ID;

/**
 * Created by daniel.hsieh on 2018/4/23.
 */

public class StockListRecyclerAdapter extends RecyclerView.Adapter {

    public interface OnItemClickListener {
        void onItemClick(Stock stock);
    }

    public interface StockListAvailableListener {
        void onStockListAvailable();
        void onStockListEmpty();
    }

    private Activity mActivity;
    private StockListPlacer mStockListPlacer;
    private StockListRenderer mStockListRenderer;
    private OnItemClickListener mOnItemClickListener;
    private StockListAvailableListener mStockListAvailableListener;

    private Handler mHandler;
    private int mTaskId;
    private AlertDialog mDeleteCodeAlertDialog;

    public StockListRecyclerAdapter(final Activity activity, int taskId, int field, int direction) {
        mActivity = activity;
        mStockListPlacer = new StockListPlacer(activity, taskId, field, direction);
        mStockListRenderer = new StockListRenderer();
        mTaskId = taskId;
        mHandler = new Handler();
        mStockListPlacer.setStockListListener(new StockListPlacer.StockListListener() {
            @Override
            public void onStockListLoaded() {
                if(mStockListAvailableListener != null) {
                    if(mStockListPlacer.isEmpty()) {
                        mStockListAvailableListener.onStockListEmpty();
                    } else {
                        mStockListAvailableListener.onStockListAvailable();
                    }
                }
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        notifyDataSetChanged();
                    }
                });
            }
        });
    }

    public void sortByTask(int field, int direction) {
        mStockListPlacer.sortByTask(field, direction);
        notifyItemRangeChanged(0, getItemCount());
    }

    public void setStockListAvailableListener(StockListAvailableListener listener) {
        mStockListAvailableListener = listener;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    public void loadStockList(String networkUrl, String cacheUrl) {
        mStockListPlacer.loadStockList(networkUrl, cacheUrl);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MarketSenseViewHolder(mStockListRenderer.createView(mActivity, parent));
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        final Stock stock = mStockListPlacer.getStockData(position);
        if(stock != null) {
            mStockListRenderer.renderView(holder.itemView, stock);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(mStockListPlacer.getStockData(holder.getAdapterPosition()));
                }
            }
        });

        if(mTaskId == SELF_CHOICES_ID) {
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if(stock != null) {
                        showDeleteAlertDialog(stock.getName(), stock.getCode());
                    }
                    return true;
                }
            });
        }
    }

    private void showDeleteAlertDialog(final String name, final String code) {
        if(mDeleteCodeAlertDialog != null) {
            mDeleteCodeAlertDialog.dismiss();
            mDeleteCodeAlertDialog = null;
        }

        String deleteFormat = mActivity.getResources().getString(R.string.message_delete);
        mDeleteCodeAlertDialog = new AlertDialog.Builder(mActivity)
                .setTitle(R.string.title_delete)
                .setMessage(String.format(deleteFormat, name, code))
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        MSLog.d("delete: " + name + ", " + code);
                        PostEvent.sendFavoriteStocksDelete(mActivity, code);
                        UserProfile userProfile = ClientData.getInstance(mActivity).getUserProfile();
                        userProfile.deleteFavoriteStock(code);
                        userProfile.globalBroadcast(NOTIFY_ID_FAVORITE_LIST);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mDeleteCodeAlertDialog.dismiss();
                    }
                })
                .show();
    }

    @Override
    public int getItemCount() {
        return mStockListPlacer.getItemCount();
    }

    public void destroy() {
        mStockListRenderer.clear();
        mStockListPlacer.clear();
        if(mDeleteCodeAlertDialog != null) {
            mDeleteCodeAlertDialog.dismiss();
            mDeleteCodeAlertDialog = null;
        }
    }
}
