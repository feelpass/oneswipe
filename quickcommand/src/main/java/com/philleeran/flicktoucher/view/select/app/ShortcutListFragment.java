package com.philleeran.flicktoucher.view.select.app;


import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import com.philleeran.flicktoucher.utils.D;
import com.philleeran.flicktoucher.utils.L;
import com.philleeran.flicktoucher.view.select.app.adapter.ToolListAdapter;
import com.philleeran.flicktoucher.view.select.app.fetcher.ContactListFetcher;
import com.philleeran.flicktoucher.view.select.app.fetcher.ShortcutListFetcher;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

public class ShortcutListFragment extends BaseItemSelectFragment implements LauncherItemFetcherCallback, AdapterView.OnItemClickListener, View.OnClickListener, ItemModel.EventListener {

    private static final int REQUEST_CODE_READ_TOOLS = 1;
    private static final int REQUEST_CREATE_SHORTCUT = 131;

    private ListView mListView;
    private View mProgressBar;
    private View mEmptyView;
    private View mPermissionLayout;
    private ToolListAdapter mAdapter;

    private Toast mUpToFiveToast;
    private PackageManager mPackageManager;

    private List<ResolveInfo> mApps;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        L.d("onCreateView");

        View root = inflater.inflate(R.layout.fragment_tool_list, container, false);
        mListView = (ListView) root.findViewById(R.id.list_view);
        mEmptyView = root.findViewById(R.id.empty_view);
        mProgressBar = root.findViewById(R.id.progress_bar);
        mPermissionLayout = root.findViewById(R.id.permission_layout);

        mPackageManager = getActivity().getPackageManager();

        mUpToFiveToast = Toast.makeText(getActivity(), R.string.it_supports_up_to_five_applications, Toast.LENGTH_SHORT);


        Intent mainIntent = new Intent(Intent.ACTION_CREATE_SHORTCUT, null);
        mApps = mPackageManager.queryIntentActivities(mainIntent, 0);

        ShortcutListFetcher.fetch(getActivity().getPackageManager(), this);

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

        mAdapter = new ToolListAdapter(inflater, launchItemList);

        for (ItemInfoBase item : launchItemList) {
            if (ItemModel.mInfoHashMap.containsKey(item.getKey())) {
                mAdapter.checkItem(item);
            } else {
                mAdapter.uncheckItem(item);
            }

        }

        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);
        ItemModel.getInstance().addEventListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        PadItemInfo data = (PadItemInfo) mAdapter.getItem(position);

        if (data == null) {
            L.e("[%d] launch item is null.");
            return;
        }
        if (ItemModel.mPosition != -1) {
            Intent createShortcutIntent = new Intent(Intent.ACTION_CREATE_SHORTCUT);
            createShortcutIntent.setComponent(new ComponentName(data.getPackageName(), data.getApplicationName()));
            getActivity().startActivityForResult(createShortcutIntent, REQUEST_CREATE_SHORTCUT);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != REQUEST_CREATE_SHORTCUT)
            return;

        if (resultCode == Activity.RESULT_CANCELED) {
            return;
        }
        Intent intent = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT);
        String name = data.getStringExtra(Intent.EXTRA_SHORTCUT_NAME);
        Bitmap bitmap = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON);

        Intent.ShortcutIconResource iconResource = null;
//            int lastKey = PhilPad.Settings.getInt(getActivity().getContentResolver(), PhilPad.Settings.SETTINGS_KEY_LAST_KEY_ID, 0);
//            PhilPad.Settings.putInt(getActivity().getContentResolver(), PhilPad.Settings.SETTINGS_KEY_LAST_KEY_ID, lastKey + 1);


        PadItemInfo.Builder builder = new PadItemInfo.Builder();

        if (bitmap != null && bitmap instanceof Bitmap) {
            ContextWrapper cw = new ContextWrapper(getActivity());
            File directory = cw.getDir(D.DIR_PATH_DEFAULT, Context.MODE_PRIVATE);

            int lastKey = PhilPad.Settings.getInt(getActivity().getContentResolver(), PhilPad.Settings.SETTINGS_KEY_LAST_KEY_ID, 0);

            File mypath = new File(directory, "shortcut_" + lastKey + ".png");
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(mypath);
                ((Bitmap) bitmap).compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.close();
            } catch (Exception e) {
                L.e(e);
            }

            builder.setType(PhilPad.Pads.PAD_TYPE_SHORTCUT).setPackageName("key" + lastKey).setExtraData(intent.toUri(0)).setImageFileName("file://" + mypath.getPath());
            if (!TextUtils.isEmpty(name)) {
                builder.setApplicationName(name);
            }
            PadItemInfo dataInfo = builder.build();

            List<PadItemInfo> itemList = ItemModel.getItemList();
            if (ItemModel.mPosition != -1 && ItemModel.mPosition < itemList.size()) {
                ItemInfoBase info = itemList.get(ItemModel.mPosition);
                if (info.getType() == PhilPad.Pads.PAD_TYPE_NULL) {
                    L.d("key : " + dataInfo.getKey() + " pos : " + info.getPositionId());
                    dataInfo.setGroupId(info.getGroupId());
                    dataInfo.setPositionId(info.getPositionId());
                    //TODO make groupid
                    ItemModel.getInstance().removeItem(getActivity(), info.getKey());
                    ItemModel.getInstance().putItem(getActivity(), dataInfo);
                }
            }

            PhilPad.Settings.putInt(getActivity().getContentResolver(), PhilPad.Settings.SETTINGS_KEY_LAST_KEY_ID, lastKey + 1);

        } else {
            Parcelable extra = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE);
            if (extra != null && extra instanceof Intent.ShortcutIconResource) {
                iconResource = (Intent.ShortcutIconResource) extra;
                try {
                    final PackageManager packageManager = getActivity().getPackageManager();
                    Resources resources;
                    resources = packageManager.getResourcesForApplication(iconResource.packageName);
                    final int id = resources.getIdentifier(iconResource.resourceName, null, null);
                    Bitmap bm = BitmapFactory.decodeResource(resources, id);
                    if (bm != null) {
                        ContextWrapper cw = new ContextWrapper(getActivity());
                        File directory = cw.getDir(D.DIR_PATH_DEFAULT, Context.MODE_PRIVATE);
                        int lastKey = PhilPad.Settings.getInt(getActivity().getContentResolver(), PhilPad.Settings.SETTINGS_KEY_LAST_KEY_ID, 0);
                        File mypath = new File(directory, "shortcut_" + lastKey + ".png");
                        FileOutputStream fos = null;
                        try {
                            fos = new FileOutputStream(mypath);
                            bm.compress(Bitmap.CompressFormat.PNG, 100, fos);
                            fos.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        builder.setType(PhilPad.Pads.PAD_TYPE_SHORTCUT).setPackageName("key" + lastKey).setExtraData(intent.toUri(0)).setImageFileName("file://" + mypath.getPath());
                        if (!TextUtils.isEmpty(name)) {
                            builder.setApplicationName(name);
                        }
                        PadItemInfo dataInfo = builder.build();
                        List<PadItemInfo> itemList = ItemModel.getItemList();
                        if (ItemModel.mPosition != -1 && ItemModel.mPosition < itemList.size()) {
                            ItemInfoBase info = itemList.get(ItemModel.mPosition);
                            if (info.getType() == PhilPad.Pads.PAD_TYPE_NULL) {
                                L.d("key : " + info.getKey() + " pos : " + info.getPositionId());

                                dataInfo.setGroupId(info.getGroupId());
                                dataInfo.setPositionId(info.getPositionId());
                                //TODO make groupid
                                ItemModel.getInstance().removeItem(getActivity(), info.getKey());
                                ItemModel.getInstance().putItem(getActivity(), dataInfo);
                            }
                        }
                        PhilPad.Settings.putInt(getActivity().getContentResolver(), PhilPad.Settings.SETTINGS_KEY_LAST_KEY_ID, lastKey + 1);
                    }
                } catch (PackageManager.NameNotFoundException e1) {
                    L.e(e1);
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

    }



}
