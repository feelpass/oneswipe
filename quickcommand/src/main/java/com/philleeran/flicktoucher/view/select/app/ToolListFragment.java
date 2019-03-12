package com.philleeran.flicktoucher.view.select.app;


import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;

import com.philleeran.flicktoucher.PadUtils;
import com.philleeran.flicktoucher.R;
import com.philleeran.flicktoucher.activity.BaseItemSelectFragment;
import com.philleeran.flicktoucher.view.select.app.fetcher.LauncherItemFetcherCallback;
import com.philleeran.flicktoucher.db.ItemInfoBase;
import com.philleeran.flicktoucher.db.ItemModel;
import com.philleeran.flicktoucher.db.PadItemInfo;
import com.philleeran.flicktoucher.db.PhilPad;
import com.philleeran.flicktoucher.utils.D;
import com.philleeran.flicktoucher.utils.L;
import com.philleeran.flicktoucher.view.select.app.adapter.ToolListAdapter;
import com.philleeran.flicktoucher.view.select.app.fetcher.ToolListFetcher;

import java.util.List;

public class ToolListFragment extends BaseItemSelectFragment implements LauncherItemFetcherCallback, AdapterView.OnItemClickListener, View.OnClickListener, ItemModel.EventListener {

    private static final int REQUEST_CODE_READ_TOOLS = 1;

    private ListView mListView;
    private View mProgressBar;
    private View mEmptyView;
    private View mPermissionLayout;
    private ToolListAdapter mAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        L.d("onCreateView");

        View root = inflater.inflate(R.layout.fragment_tool_list, container, false);
        mListView = (ListView) root.findViewById(R.id.list_view);
        mEmptyView = root.findViewById(R.id.empty_view);
        mProgressBar = root.findViewById(R.id.progress_bar);
        mPermissionLayout = root.findViewById(R.id.permission_layout);


        if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(inflater.getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                || PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(inflater.getContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                || PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(inflater.getContext(), Manifest.permission.CAMERA)
                ) {
            View requestBtn = mPermissionLayout.findViewById(R.id.permission_request_button);
            requestBtn.setOnClickListener(this);

            mPermissionLayout.setVisibility(View.VISIBLE);
            mProgressBar.setVisibility(View.GONE);
            mListView.setVisibility(View.GONE);
            mEmptyView.setVisibility(View.GONE);
        } else {
            ToolListFetcher.fetch(getActivity(), this);
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
            case REQUEST_CODE_READ_TOOLS:
                try {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        L.d("Permission allow");
                        mPermissionLayout.setVisibility(View.GONE);
                        mProgressBar.setVisibility(View.VISIBLE);
                        mListView.setVisibility(View.VISIBLE);
                        mEmptyView.setVisibility(View.GONE);
                        ToolListFetcher.fetch(getActivity(), this);
                    }
                }
                catch(ArrayIndexOutOfBoundsException e)
                {

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
        mAdapter = new ToolListAdapter(inflater, launchItemList);


        for (ItemInfoBase item : launchItemList) {
            if (ItemModel.mInfoHashMap.containsKey(item.getKey())) {
                mAdapter.checkItem(item);
            } else {
                mAdapter.uncheckItem(item);
            }
        }

        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        PadItemInfo data = (PadItemInfo) mAdapter.getItem(position);

        if (data == null) {
            L.e("[%d] launch item is null.");
            return;
        }

        if ((data.getPackageName().equals("function" + D.Icon.PadPopupMenuTools.LastApp) ||
                data.getPackageName().equals("function" + D.Icon.PadPopupMenuTools.RecentApplicationInPad) ||
                data.getPackageName().equals("function" + D.Icon.PadPopupMenuTools.Context)) && !mAdapter.isChecked(data)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (!PadUtils.isUsageAccessEnable(getActivity())) {
                    try {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setTitle(R.string.dialog_howtouse_title).setMessage(R.string.dialog_check_usage_access_message).setPositiveButton(R.string.dialog_yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                getActivity().startActivity(intent);
                            }
                        }).create();
                        AlertDialog alert = builder.create();
                        alert.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                        alert.show();

                    } catch (Exception e) {

                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setTitle(R.string.dialog_howtouse_title).setMessage(R.string.dialog_check_usage_access_message).setPositiveButton(R.string.dialog_yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(Settings.ACTION_SETTINGS);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                getActivity().startActivity(intent);

                            }
                        }).create();
                        AlertDialog alert = builder.create();
                        alert.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                        alert.show();
                        //Settings > Security > Usage access > Check SidePad
                    }
                    return;
                }
            }

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
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, REQUEST_CODE_READ_TOOLS);
                    //Settings.System.canWrite(getActivity());

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

        List<PadItemInfo> infos = mAdapter.getItems();
        for (ItemInfoBase item : infos) {
            if (ItemModel.mInfoHashMap.containsKey(item.getKey())) {
                mAdapter.checkItem(item);
            } else {
                mAdapter.uncheckItem(item);
            }
        }
    }

}
