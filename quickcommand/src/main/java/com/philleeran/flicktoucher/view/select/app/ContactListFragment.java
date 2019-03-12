package com.philleeran.flicktoucher.view.select.app;


import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.philleeran.flicktoucher.R;
import com.philleeran.flicktoucher.activity.BaseItemSelectFragment;
import com.philleeran.flicktoucher.view.select.app.fetcher.LauncherItemFetcherCallback;
import com.philleeran.flicktoucher.db.ItemInfoBase;
import com.philleeran.flicktoucher.db.ItemModel;
import com.philleeran.flicktoucher.db.PadItemInfo;
import com.philleeran.flicktoucher.db.PhilPad;
import com.philleeran.flicktoucher.utils.L;
import com.philleeran.flicktoucher.view.select.app.adapter.ContactListAdapter;
import com.philleeran.flicktoucher.view.select.app.fetcher.ContactListFetcher;

import java.util.List;

public class ContactListFragment extends BaseItemSelectFragment implements LauncherItemFetcherCallback, AdapterView.OnItemClickListener, View.OnClickListener, ItemModel.EventListener {

    private static final int REQUEST_CODE_READ_CONTACTS = 1;

    private ListView mListView;
    private View mProgressBar;
    private View mEmptyView;
    private View mPermissionLayout;
    private ContactListAdapter mAdapter;

    private Toast mUpToFiveToast;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        L.d("onCreateView");

        View root = inflater.inflate(R.layout.fragment_contact_list, container, false);
        mListView = (ListView) root.findViewById(R.id.list_view);
        mEmptyView = root.findViewById(R.id.empty_view);
        mProgressBar = root.findViewById(R.id.progress_bar);
        mPermissionLayout = root.findViewById(R.id.permission_layout);

        mUpToFiveToast = Toast.makeText(getActivity(), R.string.it_supports_up_to_five_applications, Toast.LENGTH_SHORT);

        if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(inflater.getContext(), Manifest.permission.READ_CONTACTS)
                || PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(inflater.getContext(), Manifest.permission.CALL_PHONE)
                ) {
            View requestBtn = mPermissionLayout.findViewById(R.id.permission_request_button);
            requestBtn.setOnClickListener(this);

            mPermissionLayout.setVisibility(View.VISIBLE);
            mProgressBar.setVisibility(View.GONE);
            mListView.setVisibility(View.GONE);
            mEmptyView.setVisibility(View.GONE);
        } else {
            ContactListFetcher.fetch(getActivity(), this);
        }
        ItemModel.getInstance().addEventListener(this);


        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (permissions.length <= 0 || grantResults.length <= 0) return;

        switch (requestCode) {
            case REQUEST_CODE_READ_CONTACTS:
                if (TextUtils.equals(permissions[0], Manifest.permission.READ_CONTACTS) && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mPermissionLayout.setVisibility(View.GONE);
                    mProgressBar.setVisibility(View.VISIBLE);
                    mListView.setVisibility(View.VISIBLE);
                    mEmptyView.setVisibility(View.GONE);
                    ContactListFetcher.fetch(getActivity(), this);
                }
                break;
        }
    }

    @Override
    public void onResult(@NonNull List<PadItemInfo> launchItemList) {
        Activity activity = getActivity();
        if (activity == null) {
            L.e("activity is null.");
            return;
        }

        mProgressBar.setVisibility(View.GONE);
        mListView.setEmptyView(mEmptyView);

        LayoutInflater inflater = activity.getLayoutInflater();
        mAdapter = new ContactListAdapter(inflater, launchItemList);


        for (ItemInfoBase item : launchItemList) {
            if (ItemModel.mInfoHashMap.containsKey(item.getKey())) {
                mAdapter.checkItem(item);
            } else {
                mAdapter.uncheckItem(item);
            }
        }

        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        PadItemInfo data = (PadItemInfo) mAdapter.getItem(position);
        if (data == null) {
            L.e("[%d] launch item is null.");
            return;
        }

        if (mAdapter.isChecked(data)) {

            ItemModel.getInstance().updateItem(getActivity(), data.getKey());

        } else {

            if (mAdapter.isChecked(data)) {
                ItemModel.getInstance().updateItem(getActivity(), data.getKey());
            } else {
                List<PadItemInfo> itemList = ItemModel.getItemList();
                if (ItemModel.mPosition != -1 && ItemModel.mPosition < itemList.size()) {
                    ItemInfoBase info = itemList.get(ItemModel.mPosition);
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
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.permission_request_button:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{Manifest.permission.READ_CONTACTS, Manifest.permission.CALL_PHONE}, REQUEST_CODE_READ_CONTACTS);
                }
                break;
        }
    }

    @Override
    public void onAdded(@NonNull ItemInfoBase newItem) {
        L.d("[onAdded] " + newItem.getKey());
        if (mAdapter != null && !mAdapter.isChecked(newItem)) {
            int count = mAdapter.getCount();
            for (int i = 0; i < count; ++i) {
                ItemInfoBase item = (ItemInfoBase) mAdapter.getItem(i);
                if (item.equals(newItem)) {
                    mAdapter.checkItem(item);
                    break;
                }
            }
        }
    }

    @Override
    public void onRemoved(@NonNull String id) {
        L.d("[onRemoved] " + id);
        if (mAdapter != null) {
            ItemInfoBase item = mAdapter.getCheckedItem(id);
            if (item != null) {
                mAdapter.uncheckItem(item);
            }
        }
    }

    @Override
    public void newIntent() {
        List<PadItemInfo> launchItemList = mAdapter.getItems();
        for (ItemInfoBase item : launchItemList) {
            if (ItemModel.mInfoHashMap.containsKey(item.getKey())) {
                mAdapter.checkItem(item);
            } else {
                mAdapter.uncheckItem(item);
            }

        }
    }



}
