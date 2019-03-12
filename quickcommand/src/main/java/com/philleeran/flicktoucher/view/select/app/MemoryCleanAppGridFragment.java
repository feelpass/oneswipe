package com.philleeran.flicktoucher.view.select.app;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.philleeran.flicktoucher.R;
import com.philleeran.flicktoucher.activity.BaseItemSelectFragment;
import com.philleeran.flicktoucher.db.PadItemInfo;
import com.philleeran.flicktoucher.db.PhilPad;
import com.philleeran.flicktoucher.utils.L;
import com.philleeran.flicktoucher.view.select.app.adapter.AppListAdapter;

import java.util.List;

public class MemoryCleanAppGridFragment extends BaseItemSelectFragment implements AdapterView.OnItemClickListener{

    private GridView mGridView;
    private View mProgressBar;
    private AppListAdapter mAppListAdapter;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        L.d("onCreateView");

        View root = inflater.inflate(R.layout.fragment_app_grid, container, false);
        mGridView = (GridView) root.findViewById(R.id.grid_view);
        mProgressBar = root.findViewById(R.id.progress_bar);
        mProgressBar.setVisibility(View.GONE);

        List<PadItemInfo> infos = PhilPad.Apps.getAppInfos(getActivity().getContentResolver(), PhilPad.Apps.DEFAULT_SORT_ORDER_TITLE_ASC);

        mAppListAdapter = new AppListAdapter(getActivity(), inflater, infos);

        for (PadItemInfo item : infos) {
            if(item.getLaunchCount() == 1)
            {
                mAppListAdapter.checkItem(item);
            }
            else
            {
                mAppListAdapter.uncheckItem(item);
            }
        }
        mGridView.setAdapter(mAppListAdapter);
        mGridView.setOnItemClickListener(this);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        L.d("");
        PadItemInfo data = (PadItemInfo) mAppListAdapter.getItem(position);
        if (data == null) {
            L.e("[%d] launch item is null.");
            return;
        }

        if (mAppListAdapter.isChecked(data)) {
            mAppListAdapter.uncheckItem(data);
            try {
                Intent intent = Intent.parseUri(data.getPackageName(), 0);
                PhilPad.Apps.setExcludeBoosting(getActivity().getContentResolver(), intent.getComponent().getPackageName(), 0);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            mAppListAdapter.checkItem(data);
            try {
                Intent intent = Intent.parseUri(data.getPackageName(), 0);
                PhilPad.Apps.setExcludeBoosting(getActivity().getContentResolver(), intent.getComponent().getPackageName(), 1);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void newIntent() {

    }

}