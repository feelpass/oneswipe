package com.philleeran.flicktoucher.view.select.app;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.philleeran.flicktoucher.PadUtils;
import com.philleeran.flicktoucher.R;
import com.philleeran.flicktoucher.activity.BaseItemSelectFragment;
import com.philleeran.flicktoucher.db.ItemInfoBase;
import com.philleeran.flicktoucher.db.ItemModel;
import com.philleeran.flicktoucher.db.PadItemInfo;
import com.philleeran.flicktoucher.db.PhilPad;
import com.philleeran.flicktoucher.utils.L;
import com.philleeran.flicktoucher.view.select.activity.SelectActivityActivity;
import com.philleeran.flicktoucher.view.select.app.adapter.AppListAdapter;

import java.util.List;

public class AppGridFragment extends BaseItemSelectFragment implements AdapterView.OnItemClickListener, ItemModel.EventListener {

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

        for (ItemInfoBase item : infos) {
            if (ItemModel.mInfoHashMap.containsKey(item.getKey())) {
                mAppListAdapter.checkItem(item);
            } else {
                mAppListAdapter.uncheckItem(item);
            }
        }
        ItemModel.getInstance().addEventListener(this);
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
            ItemModel.getInstance().updateItem(getActivity(), data.getKey());
        } else {
            List<PadItemInfo> itemList = ItemModel.getItemList();
            if (ItemModel.mPosition != -1 && ItemModel.mPosition < itemList.size()) {
                PadItemInfo info = itemList.get(ItemModel.mPosition);
                if (info.getType() == PhilPad.Pads.PAD_TYPE_NULL) {
                    L.d("key : " + info.getKey() + " pos : " + info.getPositionId());

                    data.setGroupId(info.getGroupId());
                    data.setPositionId(info.getPositionId());
                    //TODO make groupid
                    ItemModel.getInstance().removeItem(getActivity(), info.getKey());
                    ItemModel.getInstance().putItem(getActivity(), data);
                }
            }
        }
    }

    @Override
    public void onAdded(@NonNull ItemInfoBase newItem) {
        L.d("[onAdded] " + newItem.getKey());
        if (mAppListAdapter != null && !mAppListAdapter.isChecked(newItem)) {
            int count = mAppListAdapter.getCount();
            for (int i = 0; i < count; ++i) {
                ItemInfoBase item = (ItemInfoBase) mAppListAdapter.getItem(i);
                if (item.equals(newItem)) {
                    mAppListAdapter.checkItem(item);
                    break;
                }
            }
        }
    }


    @Override
    public void onRemoved(@NonNull String id) {
        L.d("[onRemoved] " + id);
        if (mAppListAdapter != null) {
            ItemInfoBase item = mAppListAdapter.getCheckedItem(id);
            if (item != null) {
                mAppListAdapter.uncheckItem(item);
            }
        }
    }

    @Override
    public void newIntent() {
        List<PadItemInfo> infos = mAppListAdapter.getItems();
        for (ItemInfoBase item : infos) {
            if (ItemModel.mInfoHashMap.containsKey(item.getKey())) {
                mAppListAdapter.checkItem(item);
            } else {
                mAppListAdapter.uncheckItem(item);
            }
        }
    }


}