package com.philleeran.flicktoucher.view.select.app;

import android.Manifest;
import android.app.Activity;
import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.PopupMenu;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.philleeran.flicktoucher.PadUtils;
import com.philleeran.flicktoucher.R;
import com.philleeran.flicktoucher.activity.BaseItemSelectFragment;
import com.philleeran.flicktoucher.view.select.app.fetcher.SaveItemFetcherCallback;
import com.philleeran.flicktoucher.view.select.app.fetcher.SaveListFetcher;
import com.philleeran.flicktoucher.db.ItemInfoBase;
import com.philleeran.flicktoucher.db.ItemModel;
import com.philleeran.flicktoucher.db.PadItemInfo;
import com.philleeran.flicktoucher.db.PhilPad;
import com.philleeran.flicktoucher.utils.D;
import com.philleeran.flicktoucher.utils.L;
import com.philleeran.flicktoucher.utils.Utils;
import com.philleeran.flicktoucher.view.select.app.adapter.SelectedListAdapter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public final class ItemSelectedListFragment extends BaseItemSelectFragment implements ItemModel.EventListener, View.OnClickListener, AdapterView.OnItemClickListener {

    private GridView mGridView;
    private View mProgressBar;
    private SelectedListAdapter mAppListAdapter;
    public float DPSCALE = 0.0f;

    static private final int GALLERY_INTENT_CALLED = 1;
    static private final int GALLERY_KITKAT_INTENT_CALLED = 2;
    static private final int REQUEST_PICK_APPWIDGET = 5;

    static private final int REQUEST_CREATE_APPWIDGET = 6;
    private static final int REQUEST_CODE_READ_FILE = 7;


    private String mMimeType;
    private AppWidgetManager mAppWidgetManager;
    private AppWidgetHost mAppWidgetHost;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        int padSize = PhilPad.Settings.getInt(getActivity().getContentResolver(), PhilPad.Settings.SETTINGS_KEY_PAD_SIZE, D.PAD_SIZE_4_4);
        DPSCALE = getActivity().getResources().getDisplayMetrics().density;
        int resource = 0;

        switch (padSize) {
            case 5:
                resource = R.layout.fragment_launch_item_list_5_5;
                break;
            case 4:
                resource = R.layout.fragment_launch_item_list_4_4;
                break;
            case 3:
                resource = R.layout.fragment_launch_item_list_3_3;
                break;
            case 2:
                resource = R.layout.fragment_launch_item_list_2_2;
                break;
        }
        View root = inflater.inflate(resource, container, false);
        mGridView = (GridView) root.findViewById(R.id.grid_view);
        mGridView.setOnItemClickListener(this);
        View okButton = root.findViewById(R.id.button_ok);
        okButton.setOnClickListener(this);
        View cancelButton = root.findViewById(R.id.button_cancel);
        cancelButton.setOnClickListener(this);
        View folderButton = root.findViewById(R.id.button_folder);
        folderButton.setOnClickListener(this);
        View fileButton = root.findViewById(R.id.button_file);
        fileButton.setOnClickListener(this);
        View widgetButton = root.findViewById(R.id.button_widget);
        widgetButton.setOnClickListener(this);


        mProgressBar = root.findViewById(R.id.progress_bar);
        mProgressBar.setVisibility(View.GONE);


        ItemModel.setItemList(getActivity(), ((SelectItemActivity) getActivity()).mGroupId, padSize * padSize);
        List<PadItemInfo> items = ItemModel.getItemList();

        mAppListAdapter = new SelectedListAdapter(getActivity(), inflater, items);

        ItemModel.mPosition = ((SelectItemActivity) getActivity()).mPositionId;
        ItemModel.setNextPosition(ItemModel.mPosition);

        mGridView.setAdapter(mAppListAdapter);

        ItemModel.getInstance().addEventListener(this);

        return root;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        L.d("");
        ItemInfoBase data = (ItemInfoBase) mAppListAdapter.getItem(position);
        if (data == null) {
            L.e("[%d] launch item is null.");
            return;
        }
        if (data.getType() == PhilPad.Pads.PAD_TYPE_NULL) {
            ItemModel.setNextPosition(position);
            mAppListAdapter.notifyDataSetChanged();
            return;
        }
        ItemModel.getInstance().updateItem(view.getContext(), data.getKey());

        mAppListAdapter.setItemList(ItemModel.getItemList());

    }

    @Override
    public void newIntent() {
        L.dd();
        int padSize = PhilPad.Settings.getInt(getActivity().getContentResolver(), PhilPad.Settings.SETTINGS_KEY_PAD_SIZE, D.PAD_SIZE_4_4);
        ItemModel.setItemList(getActivity(), ((SelectItemActivity) getActivity()).mGroupId, padSize * padSize);
        List<PadItemInfo> items = ItemModel.getItemList();
        mAppListAdapter.setItemList(items);
        ItemModel.mPosition = ((SelectItemActivity) getActivity()).mPositionId;
        ItemModel.setNextPosition(ItemModel.mPosition);

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ItemModel.getInstance().clearEventListener();


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (permissions.length <= 0 || grantResults.length <= 0) return;

        switch (requestCode) {
            case REQUEST_CODE_READ_FILE:
                try {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        L.d("Permission allow");
                    }
                } catch (ArrayIndexOutOfBoundsException e) {

                }

                break;
        }
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.button_ok) {
            mProgressBar.setVisibility(View.VISIBLE);
            SaveListFetcher.fetch(getActivity(), new SaveItemFetcherCallback() {
                @Override
                public void onResult(boolean result) {
                    mProgressBar.setVisibility(View.GONE);
                    if (result) {
                        getActivity().finish();
                    } else {
                        Toast.makeText(getActivity(), R.string.need_to_at_least_one, Toast.LENGTH_LONG).show();
                    }
                }
            }, ((SelectItemActivity) getActivity()).mGroupId);
/*
            List<PadItemInfo> infos = ItemModel.getItemList();
            boolean isSuccessNewAdd = false;
            for (ItemInfoBase info : infos) {
                if (info.getType() != PhilPad.Pads.PAD_TYPE_NULL) {
                    isSuccessNewAdd = true;
                }
            }
            if (((SelectItemActivity) getActivity()).mGroupId == D.GROUPID_GROUND) {
                if (isSuccessNewAdd) {
                    for (ItemInfoBase info : infos) {
                        PhilPad.Pads.setPadItem(getActivity(), ((SelectItemActivity) getActivity()).mGroupId, info.getPositionId(), info.getType(), info.getKey(), info.getApplicationName(), info.getImage(), info.getToGroupId(), info.getExtraData());
                    }

                    PhilPad.Settings.putBoolean(getActivity().getContentResolver(), PhilPad.Settings.SETTINGS_KEY_COMPLETE_FIRST_ADD_APP, true);
                    getActivity().finish();
                    return;
                } else {
                    Toast.makeText(getActivity(), R.string.need_to_at_least_one, Toast.LENGTH_LONG).show();
                    return;
                }
            }

            for (ItemInfoBase info : infos) {
                PhilPad.Pads.setPadItem(getActivity(), ((SelectItemActivity) getActivity()).mGroupId, info.getPositionId(), info.getType(), info.getKey(), info.getApplicationName(), info.getImage(), info.getToGroupId(), info.getExtraData());
            }
            int padSize = PhilPad.Settings.getInt(getActivity().getContentResolver(), PhilPad.Settings.SETTINGS_KEY_PAD_SIZE, D.PAD_SIZE_4_4);
            PhilPad.Pads.setGroupIcon(getActivity(), ((SelectItemActivity) getActivity()).mGroupId, padSize);
            getActivity().finish();*/
        } else if (id == R.id.button_cancel) {
            if (PhilPad.Settings.getBoolean(getActivity().getContentResolver(), PhilPad.Settings.SETTINGS_KEY_COMPLETE_FIRST_ADD_APP, false) == false) {
                Toast.makeText(getActivity(), R.string.need_to_at_least_one, Toast.LENGTH_LONG).show();
            } else {
                getActivity().finish();
            }
        } else if (id == R.id.button_folder) {
            PopupMenu popup = new PopupMenu(getActivity(), v);

            MenuInflater inflater = popup.getMenuInflater();
            inflater.inflate(R.menu.folder_add_menu, popup.getMenu());
            popup.show();
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {

                    if (menuItem.getItemId() == R.id.action_folder) {
                        List<PadItemInfo> itemList = ItemModel.getItemList();
                        if (ItemModel.mPosition != -1 && ItemModel.mPosition < itemList.size()) {
                            PadItemInfo info = itemList.get(ItemModel.mPosition);
                            if (info.getType() == PhilPad.Pads.PAD_TYPE_NULL) {
                                L.d("key : " + info.getKey() + " pos : " + info.getPositionId());

                                int lastKey = PhilPad.Settings.getInt(getActivity().getContentResolver(), PhilPad.Settings.SETTINGS_KEY_LAST_KEY_ID, 0);

                                Bitmap transparent = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
                                Bitmap bitmap = null;
                                bitmap = Bitmap.createScaledBitmap(transparent, (int) (96 * DPSCALE), (int) (96 * DPSCALE), true);
                                L.d("recycle");
                                if (transparent != null) {
                                    if (D.RECYCLE) {
                                        transparent.recycle();
                                    }
                                    transparent = null;
                                }

                                Paint p = new Paint();
                                p.setDither(true);
                                p.setFlags(Paint.ANTI_ALIAS_FLAG);
                                int folderColor = PhilPad.Settings.getInt(getActivity().getContentResolver(), PhilPad.Settings.SETTINGS_KEY_PAD_FOLDER_COLOR, Utils.getColor(getActivity(), R.color.color_folder_background));

                                p.setColor(folderColor);
//                                        p.setAlpha(100);
                                Paint pp = new Paint();
                                pp.setDither(true);
                                pp.setFlags(Paint.ANTI_ALIAS_FLAG);
                                Canvas c = new Canvas(bitmap);
                                c.drawRoundRect(new RectF(0, 0, (int) (96 * DPSCALE), (int) (96 * DPSCALE)), D.GRID_RECT_ROUND_FACTOR, D.GRID_RECT_ROUND_FACTOR, p);

                                ContextWrapper cw = new ContextWrapper(getActivity());
                                File directory = cw.getDir(D.DIR_PATH_DEFAULT, Context.MODE_PRIVATE);
                                File mypath = new File(directory, "group_" + info.getGroupId() + "_" + info.getPositionId() + ".png");
                                FileOutputStream fos = null;
                                try {
                                    fos = new FileOutputStream(mypath);
                                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                } finally {
                                    if (fos != null) {
                                        try {
                                            fos.close();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }

                                PadItemInfo data = new PadItemInfo.Builder().setGroupId(info.getGroupId()).setPositionId(info.getPositionId()).setType(PhilPad.Pads.PAD_TYPE_GROUP)
                                        .setTitle(getString(R.string.add_link_list_group))
                                        .setPackageName("temp" + lastKey).setImageFileName("file://" + mypath.getPath()).build();

                                //TODO make groupid
                                PhilPad.Settings.putInt(getActivity().getContentResolver(), PhilPad.Settings.SETTINGS_KEY_LAST_KEY_ID, lastKey + 1);


                                //TODO make groupid
                                ItemModel.getInstance().removeItem(getActivity(), info.getKey());
                                ItemModel.getInstance().putItem(getActivity(), data);
                            }
                        }
                    }
                    return false;
                }
            });


        } else if (id == R.id.button_file) {
/*
            infos.add(new PadItemInfo.Builder().setType(PhilPad.Pads.PAD_TYPE_GROUP_SECRET).setTitle(getString(R.string.add_link_list_group_secret)).setPackageName("folder_secret").setImageFileName("android.resource://com.philleeran.flicktoucher/drawable/ic_lock_outline_24dp_old").build());
            infos.add(new PadItemInfo.Builder().setType(PhilPad.Pads.PAD_TYPE_FILEOPEN).setTitle(getString(R.string.add_link_list_fileopen)).setPackageName("fileopen").setImageFileName("android.resource://com.philleeran.flicktoucher/drawable/ic_attachment_24dp_old").build());
*/


            PopupMenu popup = new PopupMenu(getActivity(), v);

            MenuInflater inflater = popup.getMenuInflater();
            inflater.inflate(R.menu.file_open_menu, popup.getMenu());
            popup.show();
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {

                    if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            || PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)
                            ) {

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_READ_FILE);
                            //Settings.System.canWrite(getActivity());

                        }

                    } else {
                        mMimeType = null;

                        if (menuItem.getItemId() == R.id.action_image) {
                            mMimeType = "image/*";
                        } else if (menuItem.getItemId() == R.id.action_audio) {
                            mMimeType = "audio/*";
                        } else if (menuItem.getItemId() == R.id.action_video) {
                            mMimeType = "video/*";
                        }
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                            Intent chooseFile;
                            chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
                            chooseFile.setType(mMimeType);
                            getActivity().startActivityForResult(Intent.createChooser(chooseFile, "Select Application"), GALLERY_INTENT_CALLED);

                        } else {
                            Intent chooseFile = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                            chooseFile.addCategory(Intent.CATEGORY_OPENABLE);
                            chooseFile.setType(mMimeType);
                            getActivity().startActivityForResult(chooseFile, GALLERY_KITKAT_INTENT_CALLED);
                        }
                    }


                    return false;
                }
            });


        } else if (id == R.id.button_widget) {
            PopupMenu popup = new PopupMenu(getActivity(), v);
            MenuInflater inflater = popup.getMenuInflater();
            inflater.inflate(R.menu.widget_open_menu, popup.getMenu());
            popup.show();
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    if (menuItem.getItemId() == R.id.action_widget) {

                        mAppWidgetManager = AppWidgetManager.getInstance(getActivity());
                        mAppWidgetHost = new AppWidgetHost(getActivity(), D.WIDGET_HOST_ID);
                        int appWidgetId = mAppWidgetHost.allocateAppWidgetId();
                        Intent pickIntent = new Intent(AppWidgetManager.ACTION_APPWIDGET_PICK);
                        pickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                        getActivity().startActivityForResult(pickIntent, REQUEST_PICK_APPWIDGET);

                    }
                    return false;
                }
            });
        }


    }

    @Override
    public void onAdded(@NonNull ItemInfoBase newItem) {
        L.d("[onAdded] " + newItem.getKey());

        mAppListAdapter.setItemList(ItemModel.getItemList());
        ItemModel.setNextPosition(ItemModel.mPosition);
    }

    @Override
    public void onRemoved(@NonNull String key) {
        L.d("[onRemoved] " + key);
        mAppListAdapter.setItemList(ItemModel.getItemList());
        ItemModel.setNextPosition(ItemModel.mPosition);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        L.dd("requestCode : " + requestCode + " resultCode : " + resultCode);
        switch (requestCode) {
            case GALLERY_INTENT_CALLED: {
                if (resultCode == Activity.RESULT_OK) {
                    List<PadItemInfo> itemList = ItemModel.getItemList();
                    if (ItemModel.mPosition != -1 && ItemModel.mPosition < itemList.size()) {
                        PadItemInfo info = itemList.get(ItemModel.mPosition);
                        if (info.getType() == PhilPad.Pads.PAD_TYPE_NULL) {

                            Uri uri = data.getData();
                            String uriString = uri.toString();
                            L.d("uriString : " + uriString);
                            String path = null;
                            if (uriString.startsWith("content:")) {
                                path = Utils.getPath(getActivity(), uri);
                            } else if (uriString.startsWith("file:")) {
                                path = uri.toString().substring("file:/".length());
                            }
                            final int THUMBSIZE = 94;
                            BitmapFactory.Options options = new BitmapFactory.Options();
                            options.inSampleSize = 1;

                            Bitmap curThumb = null;
                            if (mMimeType.startsWith("image")) {
                                L.d("path : " + path);
                                if (Utils.checkValidFileType(path, "image")) {
                                    curThumb = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(path), THUMBSIZE * (int) DPSCALE, THUMBSIZE * (int) DPSCALE);
                                } else {
                                    PadUtils.Toast(getActivity(), R.string.toast_invalid_file);
                                }

                            } else if (mMimeType.startsWith("audio")) {
                                if (Utils.checkValidFileType(path, "audio")) {
                                    Cursor cursor = getActivity().getContentResolver().query(uri, new String[]{
                                            MediaStore.Audio.AlbumColumns.ALBUM_ID
                                    }, null, null, null);

                                    if (cursor.moveToFirst()) {
                                        int albumId = cursor.getInt(0);
                                        curThumb = Utils.getAlbumArt(getActivity(), albumId);
                                        if (curThumb == null) {
                                            curThumb = PadUtils.getBitmapFromDrawable(getActivity(), getActivity().getResources().getDrawable(R.drawable.ic_music_video_black_24px), 96 * (int) DPSCALE, 96 * (int) DPSCALE);
                                        }
                                    }
                                    cursor.close();
                                } else {
                                    PadUtils.Toast(getActivity(), R.string.toast_invalid_file);
                                }
                            } else if (mMimeType.startsWith("video")) {
                                if (Utils.checkValidFileType(path, "video")) {
                                    curThumb = ThumbnailUtils.createVideoThumbnail(path, MediaStore.Images.Thumbnails.MINI_KIND);
                                } else {
                                    PadUtils.Toast(getActivity(), R.string.toast_invalid_file);
                                }
                            }
                            if (curThumb == null) {
                                return;
                            }
                            ContextWrapper cw = new ContextWrapper(getActivity());
                            File directory = cw.getDir(D.DIR_PATH_DEFAULT, Context.MODE_PRIVATE);
                            File mypath = new File(directory, "group_" + info.getGroupId() + "_" + info.getPositionId() + "_" + System.currentTimeMillis() + ".png");
                            FileOutputStream fos = null;
                            try {
                                fos = new FileOutputStream(mypath);
                                curThumb.compress(Bitmap.CompressFormat.PNG, 100, fos);
                                fos.close();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            int lastKey = PhilPad.Settings.getInt(getActivity().getContentResolver(), PhilPad.Settings.SETTINGS_KEY_LAST_KEY_ID, 0);

                            PadItemInfo dataInfo = new PadItemInfo.Builder().setGroupId(info.getGroupId()).setPositionId(info.getPositionId()).setType(PhilPad.Pads.PAD_TYPE_FILEOPEN)
                                    .setTitle(getString(R.string.add_link_list_fileopen)).setExtraData(path + "|" + mMimeType)
                                    .setPackageName("temp" + lastKey).setImageFileName("file://" + mypath.getPath()).build();
                            PhilPad.Settings.putInt(getActivity().getContentResolver(), PhilPad.Settings.SETTINGS_KEY_LAST_KEY_ID, lastKey + 1);

                            //TODO make groupid
                            ItemModel.getInstance().removeItem(getActivity(), info.getKey());
                            ItemModel.getInstance().putItem(getActivity(), dataInfo);
                        }
                    }

                }
            }
            break;
            case GALLERY_KITKAT_INTENT_CALLED: {
                if (resultCode == Activity.RESULT_OK) {
                    List<PadItemInfo> itemList = ItemModel.getItemList();
                    if (ItemModel.mPosition != -1 && ItemModel.mPosition < itemList.size()) {
                        PadItemInfo info = itemList.get(ItemModel.mPosition);
                        if (info.getType() == PhilPad.Pads.PAD_TYPE_NULL) {


                            Uri uri = data.getData();
                            String uriString = uri.toString();
                            L.d("uriString : " + uriString);
                            String path = null;
                            if (uriString.startsWith("content:")) {
                                final int takeFlags = data.getFlags()
                                        & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                                        | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                                // Check for the freshest data.
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                    getActivity().getContentResolver().takePersistableUriPermission(uri, takeFlags);
                                }

                                path = Utils.getPath(getActivity(), uri);
                            } else if (uriString.startsWith("file:")) {
                                path = uri.toString().substring("file:/".length());
                            }
                            final int THUMBSIZE = 94;
                            BitmapFactory.Options options = new BitmapFactory.Options();
                            options.inSampleSize = 1;

                            Bitmap curThumb = null;
                            if (mMimeType.startsWith("image")) {
                                if (!TextUtils.isEmpty(path) && Utils.checkValidFileType(path, "image")) {
                                    curThumb = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(path), THUMBSIZE * (int) DPSCALE, THUMBSIZE * (int) DPSCALE);
                                } else {
                                    PadUtils.Toast(getActivity(), R.string.toast_invalid_file);
                                }

                            } else if (mMimeType.startsWith("audio")) {
                                if (Utils.checkValidFileType(path, "audio")) {
                                    Cursor cursor = getActivity().getContentResolver().query(uri, new String[]{
                                            MediaStore.Audio.AlbumColumns.ALBUM_ID
                                    }, null, null, null);

                                    if (cursor.moveToFirst()) {
                                        int albumId = cursor.getInt(0);
                                        curThumb = Utils.getAlbumArt(getActivity(), albumId);

                                        //TODO curThumb == null at M
                                        if (curThumb == null) {
                                            curThumb = PadUtils.getBitmapFromDrawable(getActivity(), getActivity().getResources().getDrawable(R.drawable.ic_music_video_black_24px), 96 * (int) DPSCALE, 96 * (int) DPSCALE);
                                        }
                                    }
                                    cursor.close();
                                } else {
                                    PadUtils.Toast(getActivity(), R.string.toast_invalid_file);
                                }

                            } else if (mMimeType.startsWith("video")) {
                                if (Utils.checkValidFileType(path, "video")) {
                                    curThumb = ThumbnailUtils.createVideoThumbnail(path, MediaStore.Images.Thumbnails.MINI_KIND);
                                } else {
                                    PadUtils.Toast(getActivity(), R.string.toast_invalid_file);
                                }
                            }
                            if (curThumb == null) {
                                return;
                            }
                            ContextWrapper cw = new ContextWrapper(getActivity());
                            File directory = cw.getDir(D.DIR_PATH_DEFAULT, Context.MODE_PRIVATE);
                            File mypath = new File(directory, "group_" + info.getGroupId() + "_" + info.getPositionId() + "_" + System.currentTimeMillis() + ".png");
                            FileOutputStream fos = null;
                            try {
                                fos = new FileOutputStream(mypath);
                                curThumb.compress(Bitmap.CompressFormat.PNG, 100, fos);
                                fos.close();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            int lastKey = PhilPad.Settings.getInt(getActivity().getContentResolver(), PhilPad.Settings.SETTINGS_KEY_LAST_KEY_ID, 0);

                            PadItemInfo dataInfo = new PadItemInfo.Builder().setGroupId(info.getGroupId()).setPositionId(info.getPositionId()).setType(PhilPad.Pads.PAD_TYPE_FILEOPEN)
                                    .setTitle(getString(R.string.file)).setExtraData(path + "|" + mMimeType)
                                    .setPackageName("temp" + lastKey).setImageFileName("file://" + mypath.getPath()).build();
                            PhilPad.Settings.putInt(getActivity().getContentResolver(), PhilPad.Settings.SETTINGS_KEY_LAST_KEY_ID, lastKey + 1);

                            //TODO make groupid
                            ItemModel.getInstance().removeItem(getActivity(), info.getKey());
                            ItemModel.getInstance().putItem(getActivity(), dataInfo);


                        }
                    }
                }
            }
            break;
            case REQUEST_PICK_APPWIDGET: {
                if (resultCode == Activity.RESULT_OK) {
                    Bundle extras = data.getExtras();
                    int appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
                    AppWidgetProviderInfo appWidgetInfo = mAppWidgetManager.getAppWidgetInfo(appWidgetId);
                    if (appWidgetInfo.configure != null) {
                        Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE);
                        intent.setComponent(appWidgetInfo.configure);
                        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                        getActivity().startActivityForResult(intent, REQUEST_CREATE_APPWIDGET);
                    } else {
                        List<PadItemInfo> itemList = ItemModel.getItemList();
                        if (ItemModel.mPosition != -1 && ItemModel.mPosition < itemList.size()) {
                            PadItemInfo info = itemList.get(ItemModel.mPosition);
                            if (info.getType() == PhilPad.Pads.PAD_TYPE_NULL) {
                                PackageManager packageManager = getActivity().getPackageManager();
                                ApplicationInfo appInfo;
                                Resources resources = null;
                                try {
                                    appInfo = packageManager.getApplicationInfo(appWidgetInfo.provider.getPackageName(), 0);
                                    resources = packageManager.getResourcesForApplication(appInfo);
                                } catch (PackageManager.NameNotFoundException e2) {
                                    e2.printStackTrace();
                                }

                                Drawable drawable = resources.getDrawable(appWidgetInfo.icon);
                                if (drawable == null) {
                                    try {
                                        drawable = getActivity().getPackageManager().getApplicationIcon(appWidgetInfo.provider.getPackageName());
                                    } catch (PackageManager.NameNotFoundException e1) {
                                        e1.printStackTrace();
                                    }
                                }

                                Bitmap bitmap = PadUtils.getBitmapFromDrawable(getActivity(), drawable, (int) (96 * DPSCALE), (int) (96 * DPSCALE));

                                ContextWrapper cw = new ContextWrapper(getActivity());
                                File directory = cw.getDir(D.DIR_PATH_DEFAULT, Context.MODE_PRIVATE);
                                File mypath = new File(directory, "widget_" + info.getGroupId() + "_" + info.getPositionId() + "_" + appWidgetInfo.provider.getPackageName() + ".png");
                                FileOutputStream fos = null;
                                try {
                                    fos = new FileOutputStream(mypath);
                                    ((Bitmap) bitmap).compress(Bitmap.CompressFormat.PNG, 100, fos);
                                    fos.close();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }


                                int lastKey = PhilPad.Settings.getInt(getActivity().getContentResolver(), PhilPad.Settings.SETTINGS_KEY_LAST_KEY_ID, 0);

                                PadItemInfo dataInfo = new PadItemInfo.Builder().setGroupId(info.getGroupId()).setPositionId(info.getPositionId()).setType(PhilPad.Pads.PAD_TYPE_WIDGET)
                                        .setTitle(getString(R.string.add_link_list_widget)).setExtraData(String.valueOf(appWidgetId)).setApplicationName(mMimeType)
                                        .setPackageName("temp" + lastKey).setImageFileName("file://" + mypath.getPath()).build();
                                PhilPad.Settings.putInt(getActivity().getContentResolver(), PhilPad.Settings.SETTINGS_KEY_LAST_KEY_ID, lastKey + 1);

                                //TODO make groupid
                                ItemModel.getInstance().removeItem(getActivity(), info.getKey());
                                ItemModel.getInstance().putItem(getActivity(), dataInfo);
                            }
                        }

                    }
                }
            }
            break;
            case REQUEST_CREATE_APPWIDGET: {
                if (resultCode == Activity.RESULT_OK) {
                    List<PadItemInfo> itemList = ItemModel.getItemList();
                    if (ItemModel.mPosition != -1 && ItemModel.mPosition < itemList.size()) {
                        PadItemInfo info = itemList.get(ItemModel.mPosition);
                        if (info.getType() == PhilPad.Pads.PAD_TYPE_NULL) {


                            int appWidgetId = data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
                            PackageManager packageManager = getActivity().getPackageManager();
                            AppWidgetProviderInfo appWidgetInfo = mAppWidgetManager.getAppWidgetInfo(appWidgetId);

                            ApplicationInfo appInfo;
                            Resources resources = null;
                            try {
                                appInfo = packageManager.getApplicationInfo(appWidgetInfo.provider.getPackageName(), 0);
                                resources = packageManager.getResourcesForApplication(appInfo);
                            } catch (PackageManager.NameNotFoundException e2) {
                                e2.printStackTrace();
                            }

                            Drawable drawable = resources.getDrawable(appWidgetInfo.icon);
                            if (drawable == null) {
                                try {
                                    drawable = getActivity().getPackageManager().getApplicationIcon(appWidgetInfo.provider.getPackageName());
                                } catch (PackageManager.NameNotFoundException e1) {
                                    e1.printStackTrace();
                                }
                            }

                            Bitmap bitmap = PadUtils.getBitmapFromDrawable(getActivity(), drawable, (int) (96 * DPSCALE), (int) (96 * DPSCALE));

                            ContextWrapper cw = new ContextWrapper(getActivity());
                            File directory = cw.getDir(D.DIR_PATH_DEFAULT, Context.MODE_PRIVATE);
                            File mypath = new File(directory, "widget_" + info.getGroupId() + "_" + info.getPositionId() + "_" + appWidgetInfo.provider.getPackageName() + ".png");
                            FileOutputStream fos = null;
                            try {
                                fos = new FileOutputStream(mypath);
                                ((Bitmap) bitmap).compress(Bitmap.CompressFormat.PNG, 100, fos);
                                fos.close();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }


                            int lastKey = PhilPad.Settings.getInt(getActivity().getContentResolver(), PhilPad.Settings.SETTINGS_KEY_LAST_KEY_ID, 0);

                            PadItemInfo dataInfo = new PadItemInfo.Builder().setGroupId(info.getGroupId()).setPositionId(info.getPositionId()).setType(PhilPad.Pads.PAD_TYPE_WIDGET)
                                    .setTitle(getString(R.string.add_link_list_widget)).setExtraData(String.valueOf(appWidgetId)).setApplicationName(mMimeType)
                                    .setPackageName("temp" + lastKey).setImageFileName("file://" + mypath.getPath()).build();
                            PhilPad.Settings.putInt(getActivity().getContentResolver(), PhilPad.Settings.SETTINGS_KEY_LAST_KEY_ID, lastKey + 1);

                            //TODO make groupid
                            ItemModel.getInstance().removeItem(getActivity(), info.getKey());
                            ItemModel.getInstance().putItem(getActivity(), dataInfo);
                        }
                    }
                }
            }
            break;
        }
    }

}
