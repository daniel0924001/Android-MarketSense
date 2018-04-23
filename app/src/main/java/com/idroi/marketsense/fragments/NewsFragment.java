package com.idroi.marketsense.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.R;
import com.idroi.marketsense.adapter.NewsRecyclerAdapter;

/**
 * Created by daniel.hsieh on 2018/4/18.
 */

public class NewsFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private NewsRecyclerAdapter mNewsRecyclerAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        final View view = inflater.inflate(R.layout.news_fragment, container, false);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.news_recycler_view);

        mNewsRecyclerAdapter = new NewsRecyclerAdapter(getActivity());
        mRecyclerView.setAdapter(mNewsRecyclerAdapter);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mNewsRecyclerAdapter.loadNews();
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int pastVisibleItems = layoutManager.findFirstVisibleItemPosition();
                if (pastVisibleItems + visibleItemCount >= totalItemCount) {
                    mNewsRecyclerAdapter.expand(7);
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        mNewsRecyclerAdapter.destroy();
        super.onDestroyView();
    }
}
