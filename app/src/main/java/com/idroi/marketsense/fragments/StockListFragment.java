package com.idroi.marketsense.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.idroi.marketsense.R;
import com.idroi.marketsense.adapter.StockListRecyclerAdapter;
import com.idroi.marketsense.data.Stock;

/**
 * Created by daniel.hsieh on 2018/4/23.
 */

public class StockListFragment extends Fragment{

    private RecyclerView mRecyclerView;
    private StockListRecyclerAdapter mStockListRecyclerAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        final View view = inflater.inflate(R.layout.stock_fragment, container, false);
        mRecyclerView = view.findViewById(R.id.stock_recycler_view);

        mStockListRecyclerAdapter = new StockListRecyclerAdapter(getActivity());
        mRecyclerView.setAdapter(mStockListRecyclerAdapter);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mStockListRecyclerAdapter.loadStockList();
        mStockListRecyclerAdapter.setOnItemClickListener(new StockListRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Stock stock) {
                Toast.makeText(getContext(), "stock name: " + stock.getName(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        mStockListRecyclerAdapter.destroy();
        super.onDestroyView();
    }
}
