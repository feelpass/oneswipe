package com.philleeran.flicktoucher.view.select.activity;


import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.philleeran.flicktoucher.R;
import com.philleeran.flicktoucher.utils.L;
import com.philleeran.flicktoucher.view.select.activity.adapter.ActivityListAdapter;
import com.philleeran.flicktoucher.view.select.activity.fetcher.ActivityItemFetcherCallback;
import com.philleeran.flicktoucher.view.select.activity.fetcher.ActivityListFetcher;

import java.util.List;

public class ActivityListFragment extends Fragment implements ActivityItemFetcherCallback {
    private ListView mListView;
    private View mProgressBar;
    private View mEmptyView;
    private View mPermissionLayout;
    private ActivityListAdapter mAdapter;
    private Context mContext;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        L.d("onCreateView");

        mContext = getActivity();
        View root = inflater.inflate(R.layout.fragment_activity_list, container, false);
        mListView = (ListView) root.findViewById(R.id.list_view);
        mEmptyView = root.findViewById(R.id.empty_view);
        mProgressBar = root.findViewById(R.id.progress_bar);
        mPermissionLayout = root.findViewById(R.id.permission_layout);

        ActivityListFetcher.fetch(getActivity(), "com.kakao.talk", this);
        //ItemModel.getInstance().addEventListener(this);


        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }


    @Override
    public void onResult(@NonNull List<ActivityInfo> activityInfos) {
        Activity activity = getActivity();
        if (activity == null) {
            L.e("activity is null.");
            return;
        }
        LayoutInflater inflater = activity.getLayoutInflater();
        mAdapter = new ActivityListAdapter(mContext, inflater, activityInfos);
        mListView.setAdapter(mAdapter);
    }
}
