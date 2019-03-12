
package com.philleeran.flicktoucher.db;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Parcel;
import android.provider.BaseColumns;
import android.text.TextUtils;

import com.philleeran.flicktoucher.PadUtils;
import com.philleeran.flicktoucher.R;
import com.philleeran.flicktoucher.utils.D;
import com.philleeran.flicktoucher.utils.L;
import com.philleeran.flicktoucher.utils.Utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class PhilPad {
    public static final String AUTHORITY = "com.philleeran.flicktoucher.db.PhilPad";

    private PhilPad() {
    }

    public static final class Apps implements BaseColumns {

        private Apps() {
        }

        public static final String TABLE_NAME = "apps";

        private static final String SCHEME = "content://";

        private static final String PATH_APPS = "/apps";

        private static final String PATH_APP_ID = "/apps/";

        public static final int NOTE_ID_PATH_POSITION = 1;

        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_APPS);

        public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PATH_APP_ID);

        public static final Uri CONTENT_ID_URI_PATTERN = Uri.parse(SCHEME + AUTHORITY + PATH_APP_ID + "/#");

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.feelpass.apps";

        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.feelpass.apps";

        public static final String DEFAULT_SORT_ORDER = "modified DESC";

        public static final String DEFAULT_SORT_ORDER_TITLE_DESC = "title DESC";

        public static final String DEFAULT_SORT_ORDER_TITLE_ASC = "title ASC";

        public static final String COLUMN_NAME_COMPONENT_NAME = "packagename"; // Type:
        // TEXT

        public static final String COLUMN_NAME_TITLE = "title"; // Type: TEXT

        public static final String COLUMN_NAME_IMAGEFILE = "image"; // Type:
        // TEXT

        public static final String COLUMN_NAME_LAUNCH_COUNT = "launchcount"; // Type:
        // INTEGER

        public static final String COLUMN_NAME_CREATE_DATE = "created"; // Type:
        // INTEGER

        public static final String COLUMN_NAME_MODIFICATION_DATE = "modified"; // Type:
        // INTEGER

        public static final String COLUMN_NAME_ACCESS_DATE = "accessed"; // Type:
        // INTEGER

        public static Uri getUriFor(String name) {
            return getUriFor(CONTENT_ID_URI_BASE, name);
        }

        public static Uri getUriFor(Uri uri, String name) {
            return Uri.withAppendedPath(uri, name);
        }


        public static ArrayList<PadItemInfo> getAppInfos(ContentResolver resolver, String sortOrder) {
            ArrayList<PadItemInfo> itemList = new ArrayList<PadItemInfo>();
            Cursor cursor = null;
            try {
                cursor = resolver.query(Apps.CONTENT_URI, new String[]{
                        BaseColumns._ID,
                        Apps.COLUMN_NAME_COMPONENT_NAME,
                        Apps.COLUMN_NAME_TITLE, Apps.COLUMN_NAME_IMAGEFILE
                        , Apps.COLUMN_NAME_LAUNCH_COUNT
                }, null, null, sortOrder);

                if (cursor != null && cursor.moveToFirst()) {
                    do {

                        PadItemInfo.Builder builder = new PadItemInfo.Builder();
                        builder.setType(Pads.PAD_TYPE_APPLICATION)
                                .setPackageName(cursor.getString(cursor.getColumnIndex(Apps.COLUMN_NAME_COMPONENT_NAME)))
                                .setTitle(cursor.getString(cursor.getColumnIndex(Apps.COLUMN_NAME_TITLE)))
                                .setImageFileName(cursor.getString(cursor.getColumnIndex(Apps.COLUMN_NAME_IMAGEFILE)))
                                .setLaunchCount(cursor.getInt(cursor.getColumnIndex(Apps.COLUMN_NAME_LAUNCH_COUNT)));
                        itemList.add(builder.build());
                    } while (cursor.moveToNext());
                }
                return itemList;
            } finally {
                if (cursor != null && cursor.isClosed() == false) {
                    cursor.close();
                }
            }
        }

        public static ArrayList<PadItemInfo> getAppInfosForKill(ContentResolver resolver, String sortOrder) {
            ArrayList<PadItemInfo> itemList = new ArrayList<PadItemInfo>();
            Cursor cursor = null;
            try {
                cursor = resolver.query(Apps.CONTENT_URI, new String[]{
                        BaseColumns._ID,
                        Apps.COLUMN_NAME_COMPONENT_NAME,
                        Apps.COLUMN_NAME_TITLE, Apps.COLUMN_NAME_IMAGEFILE
                        , Apps.COLUMN_NAME_LAUNCH_COUNT
                }, Apps.COLUMN_NAME_LAUNCH_COUNT + "=?", new String[]{
                        String.valueOf(0)
                }, sortOrder);

                if (cursor != null && cursor.moveToFirst()) {
                    do {

                        PadItemInfo.Builder builder = new PadItemInfo.Builder();
                        builder.setType(Pads.PAD_TYPE_APPLICATION)
                                .setPackageName(cursor.getString(cursor.getColumnIndex(Apps.COLUMN_NAME_COMPONENT_NAME)))
                                .setTitle(cursor.getString(cursor.getColumnIndex(Apps.COLUMN_NAME_TITLE)))
                                .setImageFileName(cursor.getString(cursor.getColumnIndex(Apps.COLUMN_NAME_IMAGEFILE)))
                                .setLaunchCount(cursor.getInt(cursor.getColumnIndex(Apps.COLUMN_NAME_LAUNCH_COUNT)));
                        itemList.add(builder.build());
                    } while (cursor.moveToNext());
                }
                return itemList;
            } finally {
                if (cursor != null && cursor.isClosed() == false) {
                    cursor.close();
                }
            }
        }


        public static void setExcludeBoosting(ContentResolver resolver, String packageName, int data) {
            L.d("packageName : " + packageName);
            ContentValues values = new ContentValues();
            values.put(Apps.COLUMN_NAME_LAUNCH_COUNT, String.valueOf(data));
            int row = resolver.update(Apps.CONTENT_URI, values, Apps.COLUMN_NAME_COMPONENT_NAME + " like ?", new String[]{"%" + packageName + "%"});
            L.e("update complete : " + row);
            if (row <= 0) {
                L.e("update error");
            }
        }
    }

    public static final class Pads implements BaseColumns {

        private Pads() {
        }

        public static final String TABLE_NAME = "pads";

        private static final String SCHEME = "content://";

        private static final String PATH_PADS = "/pads";

        private static final String PATH_PAD_ID = "/pads/";

        public static final int PAD_TYPE_NULL = -1;

        public static final int PAD_TYPE_APPLICATION = 0;

        public static final int PAD_TYPE_GROUP = 1;

        @Deprecated
        public static final int PAD_TYPE_GROUP_SECRET = 2;

        public static final int PAD_TYPE_TOOLS = 3;

        public static final int PAD_TYPE_SHORTCUT = 4;

        public static final int PAD_TYPE_WIDGET = 5;

        public static final int PAD_TYPE_FILEOPEN = 6;

        public static final int PAD_TYPE_CONTACT = 7;


        public static final int PAD_ID_PATH_POSITION = 1;

        public static final int PAD_GROUPID_PATH_POSITION = 1;

        public static final int PAD_LISTID_PATH_POSITION = 2;

        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_PADS);

        public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PATH_PAD_ID);

        public static final Uri CONTENT_ID_URI_PATTERN = Uri.parse(SCHEME + AUTHORITY + PATH_PAD_ID + "/#");

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.feelpass.pads";

        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.feelpass.pads";

        public static final String DEFAULT_SORT_ORDER = "modified DESC";

        public static final String MODIFIED_SORT_ORDER = "modified DESC";

        public static final String MODIFIED_SORT_ORDER_ASC = "modified ASC";

        public static final String CREATED_SORT_ORDER = "created DESC";

        public static final String CREATED_SORT_ORDER_ASC = "created ASC";

        public static final String LAUNCHCOUNT_SORT_ORDER = "launchcount DESC";

        public static final String LAUNCHCOUNT_SORT_ORDER_ASC = "launchcount ASC";

        public static final String TYPE_SORT_ORDER = "type DESC";

        public static final String TYPE_SORT_ORDER_ASC = "type ASC";

        public static final String TITLE_SORT_ORDER = "title DESC";

        public static final String TITLE_SORT_ORDER_ASC = "title ASC";

        public static final String DEFAULT_SORT_ORDER_ASC = "modified ASC";

        public static final String POSITIONID_SORT_ORDER_DESC = "listid DESC";

        public static final String POSITIONID_SORT_ORDER_ASC = "listid ASC";

        public static final String GROUPID_SORT_ORDER_DESC = "groupid DESC";

        public static final String GROUPID_SORT_ORDER_ASC = "groupid ASC";

        public static final String TOGROUP_SORT_ORDER_DESC = "togroup DESC";

        public static final String TOGROUP_SORT_ORDER_ASC = "togroup ASC";

        public static final String COLUMN_NAME_GROUPID = "groupid"; // Type:

        public static final String COLUMN_NAME_LISTID = "listid"; // Type: TEXT

        public static final String COLUMN_NAME_IMAGEFILE = "image"; // Type:
        // TEXT

        public static final String COLUMN_NAME_TYPE = "type"; // Type: TEXT

        public static final String COLUMN_NAME_TITLE = "title"; // Type: TEXT

        public static final String COLUMN_NAME_PACKAGE_NAME = "packagename";

        public static final String COLUMN_NAME_TOGROUP = "togroup"; // Type:
        // INTEGER

        public static final String COLUMN_NAME_LAUNCH_COUNT = "launchcount"; // Type:
        // INTEGER

        public static final String COLUMN_NAME_ISRUNNING = "isrunning"; // Type:
        // INTEGER

        public static final String COLUMN_NAME_ISNOTI = "isnoti"; // Type:

        public static final String COLUMN_NAME_FUNCTION_MODE = "functionmode"; // Type:

        public static final String COLUMN_NAME_EXTRA_DATA = "extradata"; // Type:

        public static final String COLUMN_NAME_CREATE_DATE = "created"; // Type:

        public static final String COLUMN_NAME_MODIFICATION_DATE = "modified"; // Type:

        public static Uri getUriFor(String groupId, String positionId) {
            return getUriFor(CONTENT_ID_URI_BASE, groupId, positionId);
        }

        public static Uri getUriFor(Uri uri, String groupId, String positionId) {
            return Uri.withAppendedPath(Uri.withAppendedPath(uri, groupId), positionId);
        }

        public static boolean isGroupListExist(ContentResolver resolver, int groupId, int listId) {
            Cursor cursor = null;
            try {
                cursor = resolver.query(Pads.CONTENT_URI, new String[]{
                        Pads.COLUMN_NAME_PACKAGE_NAME
                }, Pads.COLUMN_NAME_GROUPID + "=? AND " + Pads.COLUMN_NAME_LISTID + "=?", new String[]{
                        String.valueOf(groupId), String.valueOf(listId)
                }, null);
                if (cursor != null && cursor.getCount() > 0) {
                    return true;
                } else {
                    return false;
                }
            } finally {
                if (cursor != null && cursor.isClosed() == false) {
                    cursor.close();
                }
            }
        }

        public static PadItemInfo getPadItemInfo(ContentResolver resolver, int groupId, int positionId) {
            Cursor cursor = null;
            try {
                cursor = resolver.query(getUriFor(String.valueOf(groupId), String.valueOf(positionId)), null, null, null, null);
                if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
                    PadItemInfo.Builder builder = new PadItemInfo.Builder();
                    builder.setId(cursor.getInt(cursor.getColumnIndex(BaseColumns._ID)))
                            .setTitle(cursor.getString(cursor.getColumnIndex(Pads.COLUMN_NAME_TITLE)))
                            .setPackageName(cursor.getString(cursor.getColumnIndex(Pads.COLUMN_NAME_PACKAGE_NAME)))
                            .setImageFileName(cursor.getString(cursor.getColumnIndex(Pads.COLUMN_NAME_IMAGEFILE)))
                            .setApplicationName(cursor.getString(cursor.getColumnIndex(Pads.COLUMN_NAME_TITLE)))
                            .setLaunchCount(cursor.getInt(cursor.getColumnIndex(Pads.COLUMN_NAME_LAUNCH_COUNT)))
                            .setPositionId(cursor.getInt(cursor.getColumnIndex(Pads.COLUMN_NAME_LISTID)))
                            .setType(cursor.getInt(cursor.getColumnIndex(Pads.COLUMN_NAME_TYPE)))
                            .setGroupId(cursor.getInt(cursor.getColumnIndex(Pads.COLUMN_NAME_GROUPID)))
                            .setToGroupId(cursor.getInt(cursor.getColumnIndex(Pads.COLUMN_NAME_TOGROUP)))
                            .setExtraData(cursor.getString(cursor.getColumnIndex(Pads.COLUMN_NAME_EXTRA_DATA)))
                            .setCreateTime(cursor.getLong(cursor.getColumnIndex(Pads.COLUMN_NAME_MODIFICATION_DATE)));
                    return builder.build();
                } else {
                    return null;
                }
            } finally {
                if (cursor != null && cursor.isClosed() == false) {
                    cursor.close();
                }
            }
        }

        public static PadItemInfo getPadItemInfoByPackageName(ContentResolver resolver, String packageName) {
            Cursor cursor = null;
            try {

                cursor = resolver.query(Pads.CONTENT_URI, new String[]{
                        Pads.COLUMN_NAME_TITLE, Pads.COLUMN_NAME_PACKAGE_NAME, Pads.COLUMN_NAME_IMAGEFILE, Pads.COLUMN_NAME_LAUNCH_COUNT, Pads.COLUMN_NAME_GROUPID, Pads.COLUMN_NAME_LISTID,
                        Pads.COLUMN_NAME_TYPE, Pads.COLUMN_NAME_MODIFICATION_DATE, Pads.COLUMN_NAME_TOGROUP, Pads.COLUMN_NAME_EXTRA_DATA
                }, Pads.COLUMN_NAME_PACKAGE_NAME + "==? AND " + Pads.COLUMN_NAME_TYPE + "==?", new String[]{
                        packageName,
                        String.valueOf(Pads.PAD_TYPE_APPLICATION)
                }, Pads.MODIFIED_SORT_ORDER + " LIMIT " + 1);
                if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
                    PadItemInfo.Builder builder = new PadItemInfo.Builder();
                    builder.setId(cursor.getInt(cursor.getColumnIndex(BaseColumns._ID)))
                            .setTitle(cursor.getString(cursor.getColumnIndex(Pads.COLUMN_NAME_TITLE))).setType(cursor.getInt(cursor.getColumnIndex(Pads.COLUMN_NAME_TYPE)))
                            .setPackageName(cursor.getString(cursor.getColumnIndex(Pads.COLUMN_NAME_PACKAGE_NAME)))
                            .setImageFileName(cursor.getString(cursor.getColumnIndex(Pads.COLUMN_NAME_IMAGEFILE)))
                            .setApplicationName(cursor.getString(cursor.getColumnIndex(Pads.COLUMN_NAME_TITLE)))
                            .setLaunchCount(cursor.getInt(cursor.getColumnIndex(Pads.COLUMN_NAME_LAUNCH_COUNT)))
                            .setCreateTime(cursor.getLong(cursor.getColumnIndex(Pads.COLUMN_NAME_MODIFICATION_DATE)))
                            .setToGroupId(cursor.getInt(cursor.getColumnIndex(Pads.COLUMN_NAME_TOGROUP)))
                            .setGroupId(cursor.getInt(cursor.getColumnIndex(Pads.COLUMN_NAME_GROUPID)))
                            .setPositionId(cursor.getInt(cursor.getColumnIndex(Pads.COLUMN_NAME_LISTID)))
                            .setExtraData(cursor.getString(cursor.getColumnIndex(Pads.COLUMN_NAME_EXTRA_DATA)));

                    return builder.build();
                } else {
                    return null;
                }
            } finally {
                if (cursor != null && cursor.isClosed() == false) {
                    cursor.close();
                }
            }
        }


        public static ArrayList<PadItemInfo> getPadItemInfos(ContentResolver resolver, String sortOrder, int limitCount) {
            ArrayList<PadItemInfo> itemList = new ArrayList<PadItemInfo>();
            Cursor cursor = null;
            try {
                cursor = resolver.query(Pads.CONTENT_URI, new String[]{
                        Pads.COLUMN_NAME_TITLE, Pads.COLUMN_NAME_PACKAGE_NAME, Pads.COLUMN_NAME_IMAGEFILE, Pads.COLUMN_NAME_LAUNCH_COUNT,
                        Pads.COLUMN_NAME_TYPE, Pads.COLUMN_NAME_MODIFICATION_DATE, Pads.COLUMN_NAME_TOGROUP, Pads.COLUMN_NAME_EXTRA_DATA
                }, Pads.COLUMN_NAME_TYPE + "!=? AND " + Pads.COLUMN_NAME_TYPE + "!=?", new String[]{
                        String.valueOf(Pads.PAD_TYPE_GROUP), String.valueOf(Pads.PAD_TYPE_NULL)
                }, sortOrder + " LIMIT " + limitCount);

                if (cursor != null && cursor.moveToFirst()) {
                    do {
                        PadItemInfo.Builder builder = new PadItemInfo.Builder();
                        builder.setId(cursor.getInt(cursor.getColumnIndex(BaseColumns._ID)))
                                .setTitle(cursor.getString(cursor.getColumnIndex(Pads.COLUMN_NAME_TITLE)))
                                .setPackageName(cursor.getString(cursor.getColumnIndex(Pads.COLUMN_NAME_PACKAGE_NAME)))
                                .setImageFileName(cursor.getString(cursor.getColumnIndex(Pads.COLUMN_NAME_IMAGEFILE)))
                                .setApplicationName(cursor.getString(cursor.getColumnIndex(Pads.COLUMN_NAME_TITLE)))
                                .setLaunchCount(cursor.getInt(cursor.getColumnIndex(Pads.COLUMN_NAME_LAUNCH_COUNT)))
                                .setToGroupId(cursor.getInt(cursor.getColumnIndex(Pads.COLUMN_NAME_TOGROUP)))
                                .setExtraData(cursor.getString(cursor.getColumnIndex(Pads.COLUMN_NAME_EXTRA_DATA)))
                                .setCreateTime(cursor.getLong(cursor.getColumnIndex(Pads.COLUMN_NAME_MODIFICATION_DATE)));
                        itemList.add(builder.build());
                    } while (cursor.moveToNext());
                }
                return itemList;
            } finally {
                if (cursor != null && cursor.isClosed() == false) {
                    cursor.close();
                }
            }
        }

        public static ArrayList<PadItemInfo> getPadItemInfos(ContentResolver resolver, String sortOrder) {
            ArrayList<PadItemInfo> itemList = new ArrayList<PadItemInfo>();
            Cursor cursor = null;
            try {
                cursor = resolver.query(Pads.CONTENT_URI, new String[]{
                        BaseColumns._ID,
                        Pads.COLUMN_NAME_TITLE, Pads.COLUMN_NAME_PACKAGE_NAME, Pads.COLUMN_NAME_IMAGEFILE, Pads.COLUMN_NAME_LAUNCH_COUNT,
                        Pads.COLUMN_NAME_TYPE, Pads.COLUMN_NAME_MODIFICATION_DATE, Pads.COLUMN_NAME_TOGROUP, Pads.COLUMN_NAME_EXTRA_DATA
                }, Pads.COLUMN_NAME_TYPE + "!=? AND " + Pads.COLUMN_NAME_TYPE + "!=?", new String[]{
                        String.valueOf(Pads.PAD_TYPE_GROUP), String.valueOf(Pads.PAD_TYPE_NULL)
                }, sortOrder);

                if (cursor != null && cursor.moveToFirst()) {
                    do {
                        PadItemInfo.Builder builder = new PadItemInfo.Builder();
                        builder.setId(cursor.getInt(cursor.getColumnIndex(BaseColumns._ID)))
                                .setTitle(cursor.getString(cursor.getColumnIndex(Pads.COLUMN_NAME_TITLE)))
                                .setPackageName(cursor.getString(cursor.getColumnIndex(Pads.COLUMN_NAME_PACKAGE_NAME)))
                                .setImageFileName(cursor.getString(cursor.getColumnIndex(Pads.COLUMN_NAME_IMAGEFILE)))
                                .setApplicationName(cursor.getString(cursor.getColumnIndex(Pads.COLUMN_NAME_TITLE)))
                                .setLaunchCount(cursor.getInt(cursor.getColumnIndex(Pads.COLUMN_NAME_LAUNCH_COUNT)))
                                .setToGroupId(cursor.getInt(cursor.getColumnIndex(Pads.COLUMN_NAME_TOGROUP)))
                                .setExtraData(cursor.getString(cursor.getColumnIndex(Pads.COLUMN_NAME_EXTRA_DATA)))
                                .setCreateTime(cursor.getLong(cursor.getColumnIndex(Pads.COLUMN_NAME_MODIFICATION_DATE)));
                        itemList.add(builder.build());
                    } while (cursor.moveToNext());
                }
                return itemList;
            } finally {
                if (cursor != null && cursor.isClosed() == false) {
                    cursor.close();
                }
            }
        }

        public static int getLastGroupId(ContentResolver resolver) {
            int lastGroupId;
            Cursor cursor = null;
            try {
                cursor = resolver.query(Pads.CONTENT_URI, null, Pads.COLUMN_NAME_TYPE + "=? OR " + Pads.COLUMN_NAME_TYPE + "=?", new String[]{
                        String.valueOf(Pads.PAD_TYPE_GROUP), String.valueOf(Pads.PAD_TYPE_GROUP_SECRET)
                }, Pads.TOGROUP_SORT_ORDER_DESC + " LIMIT 1");
                if (cursor != null && cursor.moveToFirst()) {
                    lastGroupId = cursor.getInt(cursor.getColumnIndex(Pads.COLUMN_NAME_TOGROUP));
                    if (lastGroupId <= 0) {
                        return 0;
                    } else {
                        return lastGroupId;
                    }
                }
                return 1;
            } finally {
                if (cursor != null && cursor.isClosed() == false) {
                    cursor.close();
                }
            }
        }

        public static ArrayList<PadItemInfo> getPadGroups(ContentResolver resolver, String sortOrder) {
            ArrayList<PadItemInfo> itemList = new ArrayList<PadItemInfo>();
            Cursor cursor = null;
            try {
                cursor = resolver.query(Pads.CONTENT_URI, null, Pads.COLUMN_NAME_TYPE + "==?", new String[]{
                        String.valueOf(Pads.PAD_TYPE_GROUP)
                }, sortOrder);

                if (cursor != null && cursor.moveToFirst()) {
                    do {
                        PadItemInfo.Builder builder = new PadItemInfo.Builder();
                        builder.setId(cursor.getInt(cursor.getColumnIndex(BaseColumns._ID)))
                                .setTitle(cursor.getString(cursor.getColumnIndex(Pads.COLUMN_NAME_TITLE)))
                                .setPackageName(cursor.getString(cursor.getColumnIndex(Pads.COLUMN_NAME_PACKAGE_NAME)))
                                .setImageFileName(cursor.getString(cursor.getColumnIndex(Pads.COLUMN_NAME_IMAGEFILE)))
                                .setApplicationName(cursor.getString(cursor.getColumnIndex(Pads.COLUMN_NAME_TITLE)))
                                .setLaunchCount(cursor.getInt(cursor.getColumnIndex(Pads.COLUMN_NAME_LAUNCH_COUNT)))
                                .setPositionId(cursor.getInt(cursor.getColumnIndex(Pads.COLUMN_NAME_LISTID)))
                                .setType(cursor.getInt(cursor.getColumnIndex(Pads.COLUMN_NAME_TYPE)))
                                .setGroupId(cursor.getInt(cursor.getColumnIndex(Pads.COLUMN_NAME_GROUPID)))
                                .setToGroupId(cursor.getInt(cursor.getColumnIndex(Pads.COLUMN_NAME_TOGROUP)))
                                .setExtraData(cursor.getString(cursor.getColumnIndex(Pads.COLUMN_NAME_EXTRA_DATA)))
                                .setCreateTime(cursor.getLong(cursor.getColumnIndex(Pads.COLUMN_NAME_MODIFICATION_DATE)));
                        itemList.add(builder.build());
                    } while (cursor.moveToNext());
                }
                return itemList;
            } finally {
                if (cursor != null && cursor.isClosed() == false) {
                    cursor.close();
                }
            }

        }

        public static ArrayList<PadItemInfo> getPadNulls(ContentResolver resolver, String sortOrder) {
            ArrayList<PadItemInfo> itemList = new ArrayList<PadItemInfo>();
            Cursor cursor = null;
            try {
                cursor = resolver.query(Pads.CONTENT_URI, new String[]{
                        Pads.COLUMN_NAME_TITLE, Pads.COLUMN_NAME_PACKAGE_NAME, Pads.COLUMN_NAME_IMAGEFILE, Pads.COLUMN_NAME_LAUNCH_COUNT,
                        Pads.COLUMN_NAME_TYPE, Pads.COLUMN_NAME_MODIFICATION_DATE, Pads.COLUMN_NAME_TOGROUP, Pads.COLUMN_NAME_EXTRA_DATA
                }, Pads.COLUMN_NAME_TYPE + "==?", new String[]{
                        String.valueOf(Pads.PAD_TYPE_NULL)
                }, sortOrder);

                if (cursor != null && cursor.moveToFirst()) {
                    do {
                        PadItemInfo.Builder builder = new PadItemInfo.Builder();
                        builder.setTitle(cursor.getString(cursor.getColumnIndex(Pads.COLUMN_NAME_TITLE)))
                                .setToGroupId(cursor.getInt(cursor.getColumnIndex(Pads.COLUMN_NAME_TOGROUP)))
                                .setPackageName(cursor.getString(cursor.getColumnIndex(Pads.COLUMN_NAME_PACKAGE_NAME)))
                                .setImageFileName(cursor.getString(cursor.getColumnIndex(Pads.COLUMN_NAME_IMAGEFILE)))
                                .setApplicationName(cursor.getString(cursor.getColumnIndex(Pads.COLUMN_NAME_TITLE)))
                                .setLaunchCount(cursor.getInt(cursor.getColumnIndex(Pads.COLUMN_NAME_LAUNCH_COUNT)))
                                .setToGroupId(cursor.getInt(cursor.getColumnIndex(Pads.COLUMN_NAME_TOGROUP)))
                                .setExtraData(cursor.getString(cursor.getColumnIndex(Pads.COLUMN_NAME_EXTRA_DATA)))
                                .setCreateTime(cursor.getLong(cursor.getColumnIndex(Pads.COLUMN_NAME_MODIFICATION_DATE)));
                        itemList.add(builder.build());
                    } while (cursor.moveToNext());
                }
                return itemList;
            } finally {
                if (cursor != null && cursor.isClosed() == false) {
                    cursor.close();
                }
            }

        }


        public static void setLastApp(Context context, String packageName, String imagePath) {
            ContentResolver resolver = context.getContentResolver();

            ContentValues values = new ContentValues();
            values.put(Pads.COLUMN_NAME_IMAGEFILE, imagePath);
            values.put(Pads.COLUMN_NAME_PACKAGE_NAME, packageName);
            int row = resolver.update(Pads.CONTENT_URI, values, Pads.COLUMN_NAME_TYPE + "=? AND " + Pads.COLUMN_NAME_EXTRA_DATA + "=?", new String[]{
                    String.valueOf(Pads.PAD_TYPE_TOOLS), String.valueOf(D.Icon.PadPopupMenuTools.LastApp)
            });
            if (row <= 0) {
                L.e("setLastApp fail");
            }
        }

        public static void setPadItems(Context context, ContentValues[] bulkToInsert) {
            ContentResolver resolver = context.getContentResolver();


            resolver.bulkInsert(PhilPad.Pads.CONTENT_URI, bulkToInsert);
        }


        public static void setPadItem(Context context, int groupId, int positionId, int type, String packageName, String applicationName, String imagePath, int toGroupId, String extraData) {
            ContentResolver resolver = context.getContentResolver();
            // removePadItem(context, groupId, positionId);
            L.dd("setPadItem groupId : " + groupId + " posititonId : " + positionId + "toGroupId : " + toGroupId + " type : " + type + "packageName : " + packageName + " applicationName : " + applicationName + " imagePath : " + imagePath + " extraData : " + extraData);
            try {
                ContentValues value = new ContentValues();
                value.put(Pads.COLUMN_NAME_GROUPID, groupId);
                value.put(Pads.COLUMN_NAME_LISTID, positionId);
                value.put(Pads.COLUMN_NAME_TYPE, type);
                value.put(Pads.COLUMN_NAME_IMAGEFILE, imagePath);
                value.put(Pads.COLUMN_NAME_FUNCTION_MODE, 0);
                value.put(Pads.COLUMN_NAME_EXTRA_DATA, extraData);

                if (!(type == Pads.PAD_TYPE_APPLICATION || type == Pads.PAD_TYPE_CONTACT || type == Pads.PAD_TYPE_TOOLS) && (TextUtils.isEmpty(packageName) || !packageName.startsWith("key"))) {
                    int lastKey = Settings.getInt(resolver, Settings.SETTINGS_KEY_LAST_KEY_ID, 0);
                    value.put(Pads.COLUMN_NAME_PACKAGE_NAME, "key" + lastKey);
                    Settings.putInt(resolver, Settings.SETTINGS_KEY_LAST_KEY_ID, lastKey + 1);
                } else {
                    if (TextUtils.isEmpty(packageName)) {
                        int lastKey = Settings.getInt(resolver, Settings.SETTINGS_KEY_LAST_KEY_ID, 0);
                        value.put(Pads.COLUMN_NAME_PACKAGE_NAME, "key" + lastKey);
                        Settings.putInt(resolver, Settings.SETTINGS_KEY_LAST_KEY_ID, lastKey + 1);
                    } else {
                        value.put(Pads.COLUMN_NAME_PACKAGE_NAME, packageName);
                    }
                }

                if (type == Pads.PAD_TYPE_GROUP || type == Pads.PAD_TYPE_GROUP_SECRET) {
                    if (TextUtils.isEmpty(packageName) || !packageName.startsWith("key")) {
                        if (toGroupId == 0) {
                            ContentValues[] bulkToInsert;
                            ArrayList<ContentValues> valueList = new ArrayList<>();
                            int lastGroupId = Settings.getInt(resolver, Settings.SETTINGS_KEY_LAST_GROUPID, 5);
                            value.put(Pads.COLUMN_NAME_TOGROUP, lastGroupId + 1);
                            toGroupId = lastGroupId + 1;
                            Settings.putInt(resolver, Settings.SETTINGS_KEY_LAST_GROUPID, lastGroupId + 1);

                            for (int i = 0; i < D.ICON_MAX_COUNT; i++) {
                                ContentValues values = new ContentValues();
                                values.put(PhilPad.Pads.COLUMN_NAME_GROUPID, toGroupId);
                                values.put(PhilPad.Pads.COLUMN_NAME_LISTID, i);
                                int lastKey = Settings.getInt(resolver, Settings.SETTINGS_KEY_LAST_KEY_ID, 0);
                                values.put(Pads.COLUMN_NAME_PACKAGE_NAME, "key" + lastKey);
                                Settings.putInt(resolver, Settings.SETTINGS_KEY_LAST_KEY_ID, lastKey + 1);
                                values.put(PhilPad.Pads.COLUMN_NAME_TYPE, PhilPad.Pads.PAD_TYPE_NULL);
                                values.put(PhilPad.Pads.COLUMN_NAME_IMAGEFILE, "");
                                values.put(PhilPad.Pads.COLUMN_NAME_FUNCTION_MODE, 0);
                                values.put(PhilPad.Pads.COLUMN_NAME_EXTRA_DATA, 0);
                                values.put(PhilPad.Pads.COLUMN_NAME_TITLE, "");
                                valueList.add(values);
                            }
                            bulkToInsert = new ContentValues[valueList.size()];
                            valueList.toArray(bulkToInsert);
                            setPadItems(context, bulkToInsert);
                        }
                    } else {
                        if (toGroupId == 0) {
                            int lastGroupId = Settings.getInt(resolver, Settings.SETTINGS_KEY_LAST_GROUPID, 5);
                            value.put(Pads.COLUMN_NAME_TOGROUP, lastGroupId + 1);
                            toGroupId = lastGroupId + 1;
                            value.put(Pads.COLUMN_NAME_PACKAGE_NAME, toGroupId);
                        } else {
                            value.put(Pads.COLUMN_NAME_TOGROUP, toGroupId);
                        }

                    }
                }

                value.put(Pads.COLUMN_NAME_TITLE, applicationName);
                int row = resolver.update(Pads.CONTENT_URI, value, Pads.COLUMN_NAME_GROUPID + "=? AND " + Pads.COLUMN_NAME_LISTID + "=?", new String[]{
                        String.valueOf(groupId), String.valueOf(positionId)
                });
                if (row <= 0) {
                    resolver.insert(Pads.CONTENT_URI, value);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public static ArrayList<PadItemInfo> getPadItemInfosInGroupPure(Context context, int groupId, int limitCount) {
            ContentResolver resolver = context.getContentResolver();
            ArrayList<PadItemInfo> itemList = new ArrayList<PadItemInfo>();

            Cursor cursor = null;
            try {
                cursor = resolver.query(Pads.CONTENT_URI, null, Pads.COLUMN_NAME_GROUPID + "=?", new String[]{
                        String.valueOf(groupId)
                }, Pads.POSITIONID_SORT_ORDER_ASC + " LIMIT " + limitCount);
                if (cursor != null && cursor.moveToFirst()) {
                    do {
                        PadItemInfo.Builder builder = new PadItemInfo.Builder();

                        builder.setId(cursor.getInt(cursor.getColumnIndex(BaseColumns._ID)))
                                .setTitle(cursor.getString(cursor.getColumnIndex(Pads.COLUMN_NAME_TITLE)))
                                .setPackageName(cursor.getString(cursor.getColumnIndex(Pads.COLUMN_NAME_PACKAGE_NAME)))
                                .setImageFileName(cursor.getString(cursor.getColumnIndex(Pads.COLUMN_NAME_IMAGEFILE)))
                                .setApplicationName(cursor.getString(cursor.getColumnIndex(Pads.COLUMN_NAME_TITLE)))
                                .setLaunchCount(cursor.getInt(cursor.getColumnIndex(Pads.COLUMN_NAME_LAUNCH_COUNT)))
                                .setPositionId(cursor.getInt(cursor.getColumnIndex(Pads.COLUMN_NAME_LISTID)))
                                .setType(cursor.getInt(cursor.getColumnIndex(Pads.COLUMN_NAME_TYPE)))
                                .setGroupId(cursor.getInt(cursor.getColumnIndex(Pads.COLUMN_NAME_GROUPID)))
                                .setToGroupId(cursor.getInt(cursor.getColumnIndex(Pads.COLUMN_NAME_TOGROUP)))
                                .setExtraData(cursor.getString(cursor.getColumnIndex(Pads.COLUMN_NAME_EXTRA_DATA)))
                                .setCreateTime(cursor.getLong(cursor.getColumnIndex(Pads.COLUMN_NAME_MODIFICATION_DATE)));
                        itemList.add(builder.build());
                    } while (cursor.moveToNext());
                } else {
                    for (int i = 0; i < D.ICON_MAX_COUNT; i++) {
                        Pads.setPadItem(context, groupId, i, Pads.PAD_TYPE_NULL, null, null, null, 0, null);
                        PadItemInfo.Builder builder = new PadItemInfo.Builder();
                        builder.setType(Pads.PAD_TYPE_NULL);
                        itemList.add(builder.build());
                    }
                }
                return itemList;
            } finally {
                if (cursor != null && cursor.isClosed() == false) {
                    cursor.close();
                }
            }
        }

        public static ArrayList<ItemInfoBase> getPadItemInfosInGroup(Context context, int groupId) {
            ContentResolver resolver = context.getContentResolver();
            ArrayList<ItemInfoBase> itemList = new ArrayList<ItemInfoBase>();

            Cursor cursor = null;
            try {
                cursor = resolver.query(Pads.CONTENT_URI, null, Pads.COLUMN_NAME_GROUPID + "=?", new String[]{
                        String.valueOf(groupId)
                }, Pads.POSITIONID_SORT_ORDER_ASC);
                if (cursor != null && cursor.moveToFirst()) {
                    do {
                        PadItemInfo.Builder builder = new PadItemInfo.Builder();
                        builder.setId(cursor.getInt(cursor.getColumnIndex(BaseColumns._ID)))
                                .setTitle(cursor.getString(cursor.getColumnIndex(Pads.COLUMN_NAME_TITLE)))
                                .setPackageName(cursor.getString(cursor.getColumnIndex(Pads.COLUMN_NAME_PACKAGE_NAME)))
                                .setImageFileName(cursor.getString(cursor.getColumnIndex(Pads.COLUMN_NAME_IMAGEFILE)))
                                .setApplicationName(cursor.getString(cursor.getColumnIndex(Pads.COLUMN_NAME_TITLE)))
                                .setLaunchCount(cursor.getInt(cursor.getColumnIndex(Pads.COLUMN_NAME_LAUNCH_COUNT)))
                                .setPositionId(cursor.getInt(cursor.getColumnIndex(Pads.COLUMN_NAME_LISTID))).setType(cursor.getInt(cursor.getColumnIndex(Pads.COLUMN_NAME_TYPE)))
                                .setToGroupId(cursor.getInt(cursor.getColumnIndex(Pads.COLUMN_NAME_TOGROUP)))
                                .setExtraData(cursor.getString(cursor.getColumnIndex(Pads.COLUMN_NAME_EXTRA_DATA)))
                                .setCreateTime(cursor.getLong(cursor.getColumnIndex(Pads.COLUMN_NAME_MODIFICATION_DATE)));
                        itemList.add(builder.build());
                    } while (cursor.moveToNext());
                } else {
                    for (int i = 0; i < D.ICON_MAX_COUNT; i++) {
                        Pads.setPadItem(context, groupId, i, Pads.PAD_TYPE_NULL, null, null, null, 0, null);
                        PadItemInfo.Builder builder = new PadItemInfo.Builder();
                        builder.setType(Pads.PAD_TYPE_NULL);
                        itemList.add(builder.build());
                    }
                }
                return itemList;
            } finally {
                if (cursor != null && cursor.isClosed() == false) {
                    cursor.close();
                }
            }
        }


        public static Cursor getPadItemInfosCursorInGroup(Context context, int groupId, int limitCount) {
            ContentResolver resolver = context.getContentResolver();
            Cursor cursor = null;
            cursor = resolver.query(Pads.CONTENT_URI, new String[]{
                    Pads.COLUMN_NAME_TYPE, Pads.COLUMN_NAME_TITLE, Pads.COLUMN_NAME_IMAGEFILE, Pads.COLUMN_NAME_EXTRA_DATA, BaseColumns._ID
            }, Pads.COLUMN_NAME_GROUPID + "=?", new String[]{
                    String.valueOf(groupId)
            }, Pads.POSITIONID_SORT_ORDER_ASC + " LIMIT " + limitCount);
            if (cursor != null && cursor.moveToFirst()) {
                return cursor;
            } else {
                cursor.close();
                for (int i = 0; i < D.ICON_MAX_COUNT; i++) {
                    Pads.setPadItem(context, groupId, i, Pads.PAD_TYPE_NULL, null, null, null, 0, null);
                }
            }
            cursor = resolver.query(Pads.CONTENT_URI, new String[]{
                    Pads.COLUMN_NAME_TYPE, Pads.COLUMN_NAME_TITLE, Pads.COLUMN_NAME_IMAGEFILE, Pads.COLUMN_NAME_EXTRA_DATA, BaseColumns._ID
            }, Pads.COLUMN_NAME_GROUPID + "=?", new String[]{
                    String.valueOf(groupId)
            }, Pads.POSITIONID_SORT_ORDER_ASC + " LIMIT " + limitCount);

            return cursor;
        }

        public static void setTempPadItem(Context context, int drawableId, int groupId, int positionId, int padType, String packageName, String title) {
            Bitmap bitmap = PadUtils.getBitmapFromDrawable(context, context.getResources().getDrawable(drawableId), 96 * (int) 3, 96 * (int) 3);
            String mypath = PadUtils.makeImageIcon(context, bitmap, 3, packageName, groupId, 0);
            Pads.setPadItem(context, groupId, positionId, padType, packageName, title, mypath, 0, null);
            if (bitmap != null) {
                bitmap.recycle();
                bitmap = null;
            }

        }

        public static void setTempPadItem(Context context, Drawable drawable, int groupId, int positionId, int padType, String packageName, String title) {
            Bitmap bitmap = PadUtils.getBitmapFromDrawable(context, drawable, 96 * (int) 3, 96 * (int) 3);
            String mypath = PadUtils.makeImageIcon(context, bitmap, 3, packageName, groupId, 0);
            Pads.setPadItem(context, groupId, positionId, padType, packageName, title, mypath, 0, null);
            if (bitmap != null) {
                bitmap.recycle();
                bitmap = null;
            }

        }

        public static ArrayList<PadItemInfo> getPadItemInfosInRunning(ContentResolver resolver, int groupId) {
            ArrayList<PadItemInfo> itemList = new ArrayList<PadItemInfo>();
            Cursor cursor = null;
            try {
                cursor = resolver.query(Pads.CONTENT_URI, new String[]{
                        Pads.COLUMN_NAME_PACKAGE_NAME, Pads.COLUMN_NAME_TYPE, Pads.COLUMN_NAME_IMAGEFILE, Pads.COLUMN_NAME_GROUPID, Pads.COLUMN_NAME_LISTID
                }, Pads.COLUMN_NAME_GROUPID + "=? AND " + Pads.COLUMN_NAME_ISRUNNING + "=?", new String[]{
                        String.valueOf(groupId), String.valueOf(1)
                }, Pads.POSITIONID_SORT_ORDER_ASC);
                if (cursor != null && cursor.moveToFirst()) {
                    do {
                        PadItemInfo.Builder builder = new PadItemInfo.Builder();
                        builder.setId(cursor.getInt(cursor.getColumnIndex(BaseColumns._ID)))
                                .setTitle(cursor.getString(cursor.getColumnIndex(Pads.COLUMN_NAME_TITLE)))
                                .setPackageName(cursor.getString(cursor.getColumnIndex(Pads.COLUMN_NAME_PACKAGE_NAME)))
                                .setImageFileName(cursor.getString(cursor.getColumnIndex(Pads.COLUMN_NAME_IMAGEFILE)))
                                .setApplicationName(cursor.getString(cursor.getColumnIndex(Pads.COLUMN_NAME_TITLE)))
                                .setLaunchCount(cursor.getInt(cursor.getColumnIndex(Pads.COLUMN_NAME_LAUNCH_COUNT)))
                                .setPositionId(cursor.getInt(cursor.getColumnIndex(Pads.COLUMN_NAME_LISTID)))
                                .setToGroupId(cursor.getInt(cursor.getColumnIndex(Pads.COLUMN_NAME_TOGROUP)))
                                .setExtraData(cursor.getString(cursor.getColumnIndex(Pads.COLUMN_NAME_EXTRA_DATA)))
                                .setCreateTime(cursor.getLong(cursor.getColumnIndex(Pads.COLUMN_NAME_MODIFICATION_DATE)));
                        itemList.add(builder.build());
                    } while (cursor.moveToNext());
                }
                return itemList;
            } finally {
                if (cursor != null && cursor.isClosed() == false) {
                    cursor.close();
                }
            }
        }

        public static void setGroupIcon(Context context, int groupId, int padSize) {
            L.d("groupId : " + groupId + " padSize : " + padSize);
            final int smallImageSize = D.Board.ICON_SIZE;
            final int smallImageInnerSize = D.Board.ICON_INNER_SIZE;
            final int smallImageMargin = D.Board.ICON_MARGIN_SIZE;
            int folderColor = PhilPad.Settings.getInt(context.getContentResolver(), PhilPad.Settings.SETTINGS_KEY_PAD_FOLDER_COLOR, Utils.getColor(context, R.color.color_pad_background));

            ContentResolver resolver = context.getContentResolver();

            ArrayList<ItemInfoBase> infos = Pads.getPadItemInfosInGroup(context, groupId);

            Bitmap transparent = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
            Bitmap bitmap = null;
            bitmap = Bitmap.createScaledBitmap(transparent, smallImageSize * padSize, smallImageSize * padSize, true);

            Paint p = new Paint();
            p.setDither(true);
            p.setFlags(Paint.ANTI_ALIAS_FLAG);
            p.setColor(folderColor);
            //p.setAlpha(100);

            Paint pp = new Paint();
            pp.setDither(true);
            pp.setFlags(Paint.ANTI_ALIAS_FLAG);
            Canvas c = new Canvas(bitmap);
            String imageFile;
            int position;
            c.drawRoundRect(new RectF(0, 0, smallImageSize * padSize, smallImageSize * padSize), D.GRID_RECT_ROUND_FACTOR, D.GRID_RECT_ROUND_FACTOR, p);

            int i = 0;
            for (; i < infos.size(); i++) {
                PadItemInfo padItemInfo = (PadItemInfo) infos.get(i);
                imageFile = padItemInfo.getImageFileName();
                int type = padItemInfo.getType();
                String filepath;
                if (type == Pads.PAD_TYPE_NULL) {
                    continue;
                } else {
                    filepath = imageFile;
                }

                position = padItemInfo.getPositionId();

                File file = new File(filepath.replace("file://", ""));
                if (file.exists()) {
                    FileInputStream fis = null;
                    try {
                        fis = new FileInputStream(file);

                        Bitmap bm = BitmapFactory.decodeStream(fis);
                        Rect rect = new Rect(0, 0, bm.getWidth(), bm.getHeight());
                        c.drawBitmap(bm, rect, new Rect(smallImageSize * (position % padSize) + smallImageMargin, smallImageSize * (position / padSize) + smallImageMargin, smallImageSize * (position % padSize)
                                + smallImageMargin + smallImageInnerSize, smallImageSize * (position / padSize) + smallImageMargin + smallImageInnerSize), pp);
                        if (bm != null) {
                            bm.recycle();
                            bm = null;
                        }

                    } catch (FileNotFoundException e) {
                        L.e(e);
                    }
                } else {
                    try {
                        InputStream inputStream = context.getContentResolver().openInputStream(Uri.parse(padItemInfo.getImageFileName()));
                        Drawable yourDrawable = Drawable.createFromStream(inputStream, padItemInfo.getImageFileName());
                        Bitmap bm = Utils.drawableToBitmap(yourDrawable);
                        Rect rect = new Rect(0, 0, bm.getWidth(), bm.getHeight());
                        c.drawBitmap(bm, rect, new Rect(smallImageSize * (position % padSize) + smallImageMargin, smallImageSize * (position / padSize) + smallImageMargin, smallImageSize * (position % padSize)
                                + smallImageMargin + smallImageInnerSize, smallImageSize * (position / padSize) + smallImageMargin + smallImageInnerSize), pp);
                        if (bm != null) {
                            bm.recycle();
                            bm = null;
                        }

                    } catch (FileNotFoundException e) {
                        L.e(e);
                    }
                }
            }
            for (int j = i; j < D.ICON_MAX_COUNT; j++) {
                Pads.setPadItem(context, groupId, j, Pads.PAD_TYPE_NULL, null, null, null, 0, null);
            }

            Cursor cursor = null;
            try {
                cursor = resolver.query(Pads.CONTENT_URI, new String[]{
                        Pads.COLUMN_NAME_IMAGEFILE
                }, Pads.COLUMN_NAME_TYPE + "=? AND " + Pads.COLUMN_NAME_TOGROUP + "=?", new String[]{
                        String.valueOf(Pads.PAD_TYPE_GROUP), String.valueOf(groupId)
                }, Pads.DEFAULT_SORT_ORDER);

                if (cursor != null && cursor.moveToFirst()) {
                    String filePath = cursor.getString(cursor.getColumnIndex(Pads.COLUMN_NAME_IMAGEFILE));
                    if (TextUtils.isEmpty(filePath) == false) {
                        File oldpath = new File(filePath);
                        oldpath.delete();
                    }
                }
            } finally {
                if (cursor != null && cursor.isClosed() == false) {
                    cursor.close();
                }
            }

            ContextWrapper cw = new ContextWrapper(context.getApplicationContext());
            File directory = cw.getDir(D.DIR_PATH_DEFAULT, Context.MODE_PRIVATE);
            File mypath = new File(directory, "group" + groupId + System.currentTimeMillis() + ".png");
            FileOutputStream fos = null;

            ContentValues values = new ContentValues();
            values.put(Pads.COLUMN_NAME_IMAGEFILE, "file://" + mypath.getPath());

            int row = resolver.update(Pads.CONTENT_URI, values, Pads.COLUMN_NAME_TYPE + "=? AND " + Pads.COLUMN_NAME_TOGROUP + "=?", new String[]{
                    String.valueOf(Pads.PAD_TYPE_GROUP), String.valueOf(groupId)
            });
            if (row > 0) {
                try {
                    fos = new FileOutputStream(mypath);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                    fos.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (bitmap != null) {
                bitmap.recycle();
                bitmap = null;
            }

            int parentGroupId = getParentGroup(resolver, groupId);
            while (parentGroupId != 0) {
                setGroupIcon(context, parentGroupId, padSize);
                parentGroupId = getParentGroup(resolver, parentGroupId);
            }
        }

        private static int getParentGroup(ContentResolver resolver , int groupId) {
            Cursor cursor = null;
            try {
                cursor = resolver.query(Pads.CONTENT_URI, new String[]{
                        Pads.COLUMN_NAME_TOGROUP, Pads.COLUMN_NAME_GROUPID
                }, Pads.COLUMN_NAME_TOGROUP + "=?", new String[]{
                        String.valueOf(groupId)
                }, Pads.POSITIONID_SORT_ORDER_ASC);
                if (cursor != null && cursor.moveToFirst()) {
                    int ret = cursor.getInt(cursor.getColumnIndex(Pads.COLUMN_NAME_GROUPID));
                    return ret;
                } else {
                    return 0;
                }
            } finally {
                if (cursor != null && cursor.isClosed() == false) {
                    cursor.close();
                }
            }
        }

        public static void removePadItem(Context context, int groupId, int positionId) {
            Pads.setPadItem(context, groupId, positionId, Pads.PAD_TYPE_NULL, null, null, null, 0, null);
        }

        public static void updatePadItemRunning(ContentResolver resolver, int groupId, int positionId, int isRunning) {
            PadItemInfo info = getPadItemInfo(resolver, groupId, positionId);
            if (info != null) {
                String packageString = info.getPackageName();
                ContentValues values = new ContentValues();
                values.put(Pads.COLUMN_NAME_ISRUNNING, isRunning);
                resolver.update(Pads.CONTENT_URI, values, Pads.COLUMN_NAME_PACKAGE_NAME + "=?", new String[]{
                        packageString
                });
            }
        }

        public static void setFunctionMode(Context context, int functionType, int mode) {
            ContentResolver resolver = context.getContentResolver();
            ContentValues values = new ContentValues();
            values.put(Pads.COLUMN_NAME_FUNCTION_MODE, mode);
            resolver.update(Pads.CONTENT_URI, values, Pads.COLUMN_NAME_PACKAGE_NAME + "=?", new String[]{
                    String.valueOf(functionType)
            });
        }

        public static int getCountOfShortcut(Context context) {
            ContentResolver resolver = context.getContentResolver();
            Cursor cursor = null;
            try {
                cursor = resolver.query(Pads.CONTENT_URI, new String[]{
                        Pads.COLUMN_NAME_TYPE
                }, Pads.COLUMN_NAME_TYPE + "=?", new String[]{
                        String.valueOf(Pads.PAD_TYPE_SHORTCUT)
                }, Pads.POSITIONID_SORT_ORDER_ASC);
                if (cursor != null) {
                    int ret = cursor.getCount();
                    return ret;
                } else {
                    return 0;
                }
            } finally {
                if (cursor != null && cursor.isClosed() == false) {
                    cursor.close();
                }
            }
        }

        public static int getCountOfWidget(Context context) {
            ContentResolver resolver = context.getContentResolver();
            Cursor cursor = null;
            try {
                cursor = resolver.query(Pads.CONTENT_URI, new String[]{
                        Pads.COLUMN_NAME_TYPE
                }, Pads.COLUMN_NAME_TYPE + "=?", new String[]{
                        String.valueOf(Pads.PAD_TYPE_WIDGET)
                }, Pads.POSITIONID_SORT_ORDER_ASC);
                if (cursor != null) {
                    int ret = cursor.getCount();
                    return ret;
                } else {
                    return 0;
                }
            } finally {
                if (cursor != null && cursor.isClosed() == false) {
                    cursor.close();
                }
            }
        }

        public static void setRecently(Context context, ContentValues[] bulkToInsert) {
            ContentResolver resolver = context.getContentResolver();
            resolver.delete(Pads.CONTENT_URI, Pads.COLUMN_NAME_GROUPID + "=?", new String[]{
                    String.valueOf(D.GROUPID_RECENT)
            });
            resolver.bulkInsert(PhilPad.Pads.CONTENT_URI, bulkToInsert);
        }

        public static void setNotificationPad(Context context, ContentValues[] bulkToInsert) {
            ContentResolver resolver = context.getContentResolver();
            resolver.delete(Pads.CONTENT_URI, Pads.COLUMN_NAME_GROUPID + "=?", new String[]{
                    String.valueOf(D.GROUPID_NOTIFICATION)
            });
            resolver.bulkInsert(PhilPad.Pads.CONTENT_URI, bulkToInsert);
        }

        public static ArrayList<PadItemInfo> getAllPadInfo(ContentResolver resolver, String defaultSortOrder) {
            ArrayList<PadItemInfo> itemList = new ArrayList<PadItemInfo>();
            Cursor cursor = null;
            try {
                cursor = resolver.query(Pads.CONTENT_URI, null, null, null, defaultSortOrder);

                if (cursor != null && cursor.moveToFirst()) {
                    do {
                        PadItemInfo.Builder builder = new PadItemInfo.Builder();
                        builder.setId(cursor.getInt(cursor.getColumnIndex(BaseColumns._ID)))
                                .setTitle(cursor.getString(cursor.getColumnIndex(Pads.COLUMN_NAME_TITLE)))
                                .setPackageName(cursor.getString(cursor.getColumnIndex(Pads.COLUMN_NAME_PACKAGE_NAME)))
                                .setImageFileName(cursor.getString(cursor.getColumnIndex(Pads.COLUMN_NAME_IMAGEFILE)))
                                .setApplicationName(cursor.getString(cursor.getColumnIndex(Pads.COLUMN_NAME_TITLE)))
                                .setLaunchCount(cursor.getInt(cursor.getColumnIndex(Pads.COLUMN_NAME_LAUNCH_COUNT)))
                                .setPositionId(cursor.getInt(cursor.getColumnIndex(Pads.COLUMN_NAME_LISTID)))
                                .setType(cursor.getInt(cursor.getColumnIndex(Pads.COLUMN_NAME_TYPE)))
                                .setGroupId(cursor.getInt(cursor.getColumnIndex(Pads.COLUMN_NAME_GROUPID)))
                                .setToGroupId(cursor.getInt(cursor.getColumnIndex(Pads.COLUMN_NAME_TOGROUP)))
                                .setExtraData(cursor.getString(cursor.getColumnIndex(Pads.COLUMN_NAME_EXTRA_DATA)))
                                .setCreateTime(cursor.getLong(cursor.getColumnIndex(Pads.COLUMN_NAME_MODIFICATION_DATE)));
                        itemList.add(builder.build());
                    } while (cursor.moveToNext());
                }
                return itemList;
            } finally {
                if (cursor != null && cursor.isClosed() == false) {
                    cursor.close();
                }
            }
        }
    }

    public static final class Settings implements BaseColumns {

        private Settings() {
        }

        public static final int SETTINGS_ID_PATH_POSITION = 1;

        public static final String TABLE_NAME = "settings";

        private static final String SCHEME = "content://";

        private static final String PATH_SETTINGS = "/settings";

        private static final String PATH_SETTING_ID = "/settings/";

        public static final String SETTINGS_KEY_IS_PREMIUM = "is_premium";

        public static final String SETTINGS_KEY_TOTAL_LAUNCH_COUNT = "total_launch_count";

        public static final String SETTINGS_KEY_RATE = "is.rate";

        public static final String SETTINGS_KEY_COMPLETE_TOTURIAL = "is.build.applist.first";

        public static final String SETTINGS_KEY_COMPLETE_FIRST_ADD_APP = "is.add.applist.first";

        public static final String SETTINGS_KEY_NOTIFICATION_PAD_FILE_PATH = "notification.file.path";


        public static final String SETTINGS_KEY_IS_APP_INSTALL_FIRST = "is.app.install.first";


        public static final String SETTINGS_KEY_LAST_GROUPID = "last.group.id";

        public static final String SETTINGS_KEY_LAST_KEY_ID = "last.key.id";

        public static final String SETTINGS_KEY_IS_SERVICE_ENABLE = "philpad.service.enabled";

        public static final String SETTINGS_KEY_GESTURE_TYPE_UP = "gesture.type.up";

        public static final String SETTINGS_KEY_GESTURE_TYPE_DOWN = "gesture.type.down";

        public static final String SETTINGS_KEY_GESTURE_TYPE_LEFT = "gesture.type.left";

        public static final String SETTINGS_KEY_GESTURE_TYPE_RIGHT = "gesture.type.right";

        public static final String SETTINGS_KEY_GESTURE_TYPE_UP_LEFT = "gesture.type.up.left";

        public static final String SETTINGS_KEY_GESTURE_TYPE_UP_RIGHT = "gesture.type.up.right";

        public static final String SETTINGS_KEY_GESTURE_TYPE_DOWN_LEFT = "gesture.type.down.left";

        public static final String SETTINGS_KEY_GESTURE_TYPE_DOWN_RIGHT = "gesture.type.down.right";

        public static final String SETTINGS_KEY_GESTURE_TYPE_LEFT_UP = "gesture.type.left.up";

        public static final String SETTINGS_KEY_GESTURE_TYPE_LEFT_DOWN = "gesture.type.left.down";

        public static final String SETTINGS_KEY_GESTURE_TYPE_RIGHT_UP = "gesture.type.right.up";

        public static final String SETTINGS_KEY_GESTURE_TYPE_RIGHT_DOWN = "gesture.type.right.down";

        public static final String SETTINGS_KEY_BACKGROUND_COLOR = "background.color";

        public static final String SETTINGS_KEY_PAD_BACKGROUND_COLOR = "pad.background.color";

        public static final String SETTINGS_KEY_PAD_FOLDER_COLOR = "pad.folder.color";

        public static final String SETTINGS_KEY_PAD_SPECIAL_COLOR = "pad.special.color";

        public static final String SETTINGS_KEY_PAD_ANIMATION_ENABLE = "pad.animation.enable";

        public static final String SETTINGS_KEY_PAD_GESTURE_ENABLE = "pad.gesture.enable";

        public static final String SETTINGS_KEY_PAD_STATUSINFO_ENABLE = "pad.statusinfo.enable";

        public static final String SETTINGS_KEY_PAD_SHOW_NAME_ENABLE = "pad.showtitle.enable";

        public static final String SETTINGS_KEY_PAD_HAPTIC_ENABLE = "pad.haptic.enable";

        public static final String SETTINGS_KEY_PAD_TRIGGER_VISIBLE= "pad.trigger.visible";

        public static final String SETTINGS_KEY_PAD_VIBRATION_LEVEL = "pad.vibration.level";

        public static final String SETTINGS_KEY_PAD_ANIMATION_DURATION_LEVEL = "pad.animation.duration.level";

        public static final String SETTINGS_KEY_DEVELOPER_MODE = "pad.developermode.manage.enable";

        public static final String SETTINGS_KEY_BACKGROUNDIMAGE_PATH = "background.image.path";

        public static final String SETTINGS_KEY_PAD_SIZE = "pad.size";

        public static final String SETTINGS_KEY_DYNAMIC_OPTION = "dynamic.option";

        public static final String SETTINGS_KEY_INTERACTION_OPTION = "interaction.option";

        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_SETTINGS);

        public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PATH_SETTING_ID);

        public static final Uri CONTENT_ID_URI_PATTERN = Uri.parse(SCHEME + AUTHORITY + PATH_SETTING_ID + "/#");

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.feelpass.settings";

        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.feelpass.settings";

        public static final String COLUMN_NAME_KEY = "key";

        public static final String COLUMN_NAME_VALUE = "value";

        public static final String COLUMN_NAME_CREATED_DATE = "created_date";

        public static final String COLUMN_NAME_LAST_UPDATED = "last_updated";

        public static final String DEFAULT_SORT_ORDER = COLUMN_NAME_LAST_UPDATED + " DESC";

        public static Uri getUriFor(String name) {
            return getUriFor(CONTENT_ID_URI_BASE, name);
        }

        public static Uri getUriFor(Uri uri, String name) {
            return Uri.withAppendedPath(uri, name);
        }

        public static void putBoolean(ContentResolver contentResolver, String key, boolean value) {
            putString(contentResolver, key, Boolean.toString(value));
        }

        public static boolean getBoolean(ContentResolver contentResolver, String key, boolean defaultValue) {
            String result = getString(contentResolver, key, Boolean.toString(defaultValue));
            if (!TextUtils.isEmpty(result)) {
                if (result.equals("true") || result.equals("false")) {
                    return Boolean.parseBoolean(result);
                } else {
                    return defaultValue;
                }
            } else {
                return defaultValue;
            }
        }

        public static void putInt(ContentResolver contentResolver, String key, int value) {
            putString(contentResolver, key, Integer.toString(value));
        }

        public static int getInt(ContentResolver contentResolver, String key, int defaultValue) {
            String result = getString(contentResolver, key, Integer.toString(defaultValue));
            if (!TextUtils.isEmpty(result)) {
                try {
                    return Integer.parseInt(result);
                } catch (NumberFormatException e) {
                    L.e("key:" + key + " defaultValue:" + defaultValue + " message:" + e.getMessage());
                    return defaultValue;
                }
            } else {
                return defaultValue;
            }
        }

        public static void putLong(ContentResolver contentResolver, String key, long value) {
            putString(contentResolver, key, Long.toString(value));
        }

        public static long getLong(ContentResolver contentResolver, String key, long defaultValue) {
            String result = getString(contentResolver, key, Long.toString(defaultValue));
            if (!TextUtils.isEmpty(result)) {
                try {
                    return Long.parseLong(result);
                } catch (NumberFormatException e) {
                    return defaultValue;
                }
            } else {
                return defaultValue;
            }
        }

        public static void putString(ContentResolver contentResolver, String key, String value) {
            Cursor cursor = null;
            try {
                cursor = contentResolver.query(Settings.CONTENT_URI, new String[]{
                        Settings.COLUMN_NAME_KEY, Settings.COLUMN_NAME_VALUE
                }, Settings.COLUMN_NAME_KEY + "=?", new String[]{
                        key
                }, Settings.DEFAULT_SORT_ORDER + " LIMIT 1");

                ContentValues values = new ContentValues();
                values.put(Settings.COLUMN_NAME_KEY, key);
                values.put(Settings.COLUMN_NAME_VALUE, value);

                if (cursor != null && cursor.moveToFirst()) {
                    contentResolver.update(Settings.CONTENT_URI, values, Settings.COLUMN_NAME_KEY + "=? ", new String[]{
                            key
                    });
                } else {
                    contentResolver.insert(Settings.CONTENT_URI, values);
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }

        }

        public static String getString(ContentResolver contentResolver, String key, String defaultValue) {
            Cursor cursor = null;
            try {
                cursor = contentResolver.query(Settings.CONTENT_URI, new String[]{
                        Settings.COLUMN_NAME_KEY, Settings.COLUMN_NAME_VALUE
                }, Settings.COLUMN_NAME_KEY + "=?", new String[]{
                        key
                }, Settings.DEFAULT_SORT_ORDER + " LIMIT 1");
                if (cursor != null && cursor.moveToFirst()) {
                    return cursor.getString(cursor.getColumnIndex(Settings.COLUMN_NAME_VALUE));
                } else {
                    return defaultValue;
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
    }


    public static final class Notifications implements BaseColumns {
        private Notifications() {
        }

        public static final int NOTIFICATIONS_ID_PATH_POSITION = 1;

        public static final String TABLE_NAME = "notifications";

        private static final String SCHEME = "content://";

        private static final String PATH_NOTIFICATIONS = "/notifications";

        private static final String PATH_NOTIFICATION_ID = "/notifications/";


        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_NOTIFICATIONS);

        public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PATH_NOTIFICATION_ID);

        public static final Uri CONTENT_ID_URI_PATTERN = Uri.parse(SCHEME + AUTHORITY + PATH_NOTIFICATION_ID + "/#");

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.feelpass.notifications";

        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.feelpass.notifications";

        public static final String COLUMN_NAME_PACKAGE_NAME = "package_name";

        public static final String COLUMN_NAME_APPLICATION_NAME = "application_name";

        public static final String COLUMN_NAME_CONTENT_INTENT = "content_intent";

        public static final String COLUMN_NAME_DELETE_INTENT = "delete_intent";

        public static final String COLUMN_NAME_LARGE_ICON = "large_icon";

        public static final String COLUMN_NAME_TICKER_TEXT = "ticker_text";

        public static final String COLUMN_NAME_EXTRA_DATA = "extra_data";

        public static final String COLUMN_NAME_CREATED_DATE = "created_date";

        public static final String COLUMN_NAME_LAST_UPDATED = "last_updated";

        public static final String DEFAULT_SORT_ORDER = COLUMN_NAME_LAST_UPDATED + " DESC";

        public static Uri getUriFor(String name) {
            return getUriFor(CONTENT_ID_URI_BASE, name);
        }

        public static Uri getUriFor(Uri uri, String name) {
            return Uri.withAppendedPath(uri, name);
        }


        public static void setNotificationItem(Context context, NotificationItemInfo info) {
            ContentResolver resolver = context.getContentResolver();
            try {
                ContentValues values = new ContentValues();
                values.put(Notifications.COLUMN_NAME_PACKAGE_NAME, info.getPackageName());
                values.put(Notifications.COLUMN_NAME_APPLICATION_NAME, info.getApplicationName());

                /*
                Bundle bundle = // get a bundle from somewhere
                final Parcel parcel = Parcel.obtain();
                bundle.writeToParcel(parcel, 0);
                byte[] bundleBytes = parcel.marshall();
                parcel.recycle();

                ContentValues values = new ContentValues();
                values.put("bundle", bundleBytes);
                SQLiteDatabase db = // create a db object
                        db.insert("table", null, values);

                Cursor cur = // get a cursor of your db
                byte[] bundleBytes = cur.getBlob(cur.getColumnIndex("bundle"));
                final Parcel parcel = Parcel.obtain();
                parcel.unmarshall(bundleBytes, 0, bundleBytes.length);
                parcel.setDataPosition(0);
                Bundle bundle = (Bundle) parcel.readBundle();
                parcel.recycle();
http://stackoverflow.com/questions/11790104/how-to-storebitmap-image-and-retrieve-image-from-sqlite-database-in-android
                */


                Parcel parcel = Parcel.obtain();
                info.getContentIntent().writeToParcel(parcel, 0);
                byte[] bundleBytes = parcel.marshall();
                parcel.recycle();

                values.put(Notifications.COLUMN_NAME_CONTENT_INTENT, bundleBytes);
                parcel = Parcel.obtain();
                info.getDeleteIntent().writeToParcel(parcel, 0);
                bundleBytes = parcel.marshall();
                parcel.recycle();
                values.put(Notifications.COLUMN_NAME_DELETE_INTENT, bundleBytes);

                /*
public class DbBitmapUtility {

    // convert from bitmap to byte array
    public static byte[] getBytes(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(CompressFormat.PNG, 0, stream);
        return stream.toByteArray();
    }

    // convert from byte array to bitmap
    public static Bitmap getImage(byte[] image) {
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }
}
                 */
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                info.getLargeIcon().compress(Bitmap.CompressFormat.PNG, 0, stream);
                values.put(Notifications.COLUMN_NAME_LARGE_ICON, stream.toByteArray());
                values.put(Notifications.COLUMN_NAME_TICKER_TEXT, info.getTickerText());
                values.put(Notifications.COLUMN_NAME_EXTRA_DATA, info.getExtraData());

                resolver.insert(Pads.CONTENT_URI, values);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }


    }


    public static final class BaseItems implements BaseColumns {
        private BaseItems() {
        }

        public static final int BASEITEMS_ID_PATH_POSITION = 1;

        public static final String TABLE_NAME = "baseitems";

        private static final String SCHEME = "content://";

        private static final String PATH_BASEITEMS = "/baseitems";

        private static final String PATH_BASEITEM_ID = "/baseitems/";


        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_BASEITEMS);

        public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PATH_BASEITEM_ID);

        public static final Uri CONTENT_ID_URI_PATTERN = Uri.parse(SCHEME + AUTHORITY + PATH_BASEITEM_ID + "/#");

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.feelpass.baseitems";

        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.baseitem/vnd.feelpass.baseitems";

        public static final String COLUMN_NAME_KEY = "key_name";

        public static final String COLUMN_NAME_TITLE_NAME = "title_name";

        public static final String COLUMN_NAME_SUBTITLE_NAME = "sub_title_name";

        public static final String COLUMN_NAME_IMAGE_PATH = "image_path";

        public static final String COLUMN_NAME_GROUPID = "groupid"; // Type:

        public static final String COLUMN_NAME_LISTID = "listid"; // Type: TEXT

        public static final String COLUMN_NAME_EXTRA_DATA = "extra_data";

        public static final String COLUMN_NAME_CREATED_DATE = "created_date";

        public static final String COLUMN_NAME_LAST_UPDATED = "last_updated";

        public static final String POSITIONID_SORT_ORDER_ASC = "listid ASC";


        public static final String DEFAULT_SORT_ORDER = POSITIONID_SORT_ORDER_ASC;

        public static Uri getUriFor(String name) {
            return getUriFor(CONTENT_ID_URI_BASE, name);
        }

        public static Uri getUriFor(Uri uri, String name) {
            return Uri.withAppendedPath(uri, name);
        }


        //TODO need change to bulk insert
        public static void putItems(Context context, ArrayList<ItemInfoBase> infos) {
            ContentResolver resolver = context.getContentResolver();
            for (int i = 0; i < infos.size(); i++) {
                ItemInfoBase info = infos.get(i);
                ContentValues values = new ContentValues();
                values.put(BaseItems.COLUMN_NAME_KEY, info.getKey());
                values.put(BaseItems.COLUMN_NAME_TITLE_NAME, info.getTitle());
                values.put(BaseItems.COLUMN_NAME_SUBTITLE_NAME, info.getSubtitle());
                values.put(BaseItems.COLUMN_NAME_IMAGE_PATH, info.getImage());
                values.put(BaseItems.COLUMN_NAME_GROUPID, info.getGroupId());
                values.put(BaseItems.COLUMN_NAME_LISTID, info.getPositionId());

                int row = resolver.update(BaseItems.CONTENT_URI, values, BaseItems.COLUMN_NAME_KEY + "=?", new String[]{
                        info.getKey()
                });
                if (row <= 0) {
                    L.d("");
                    resolver.insert(BaseItems.CONTENT_URI, values);
                }
            }
        }

        public static void deleteItems(Context context) {
            ContentResolver resolver = context.getContentResolver();
            resolver.delete(BaseItems.CONTENT_URI, null, null);
        }

        public static List<ItemInfoBase> getItems(Context context, int groupId) {
            ContentResolver resolver = context.getContentResolver();
            ArrayList<ItemInfoBase> itemList = new ArrayList<>();
            Cursor cursor = null;
            try {
                cursor = resolver.query(BaseItems.CONTENT_URI, new String[]{
                        BaseColumns._ID,
                        BaseItems.COLUMN_NAME_KEY, BaseItems.COLUMN_NAME_TITLE_NAME, BaseItems.COLUMN_NAME_SUBTITLE_NAME,
                        BaseItems.COLUMN_NAME_IMAGE_PATH, BaseItems.COLUMN_NAME_GROUPID, BaseItems.COLUMN_NAME_LISTID
                }, BaseItems.COLUMN_NAME_GROUPID + "=?", new String[]{
                        String.valueOf(groupId)}, BaseItems.DEFAULT_SORT_ORDER);
            } finally {
                if (cursor != null && cursor.isClosed() == false) {
                    cursor.close();
                }
            }
            return itemList;
        }


        public static void putItem(Context context, ItemInfoBase info) {

            ContentResolver resolver = context.getContentResolver();
            ContentValues values = new ContentValues();
            values.put(BaseItems.COLUMN_NAME_KEY, info.getKey());
            values.put(BaseItems.COLUMN_NAME_TITLE_NAME, info.getTitle());
            values.put(BaseItems.COLUMN_NAME_SUBTITLE_NAME, info.getSubtitle());
            values.put(BaseItems.COLUMN_NAME_IMAGE_PATH, info.getImage());
            values.put(BaseItems.COLUMN_NAME_GROUPID, info.getGroupId());
            values.put(BaseItems.COLUMN_NAME_LISTID, info.getPositionId());

            int row = resolver.update(BaseItems.CONTENT_URI, values, BaseItems.COLUMN_NAME_KEY + "=? ", new String[]{
                    info.getKey()
            });
            if (row <= 0) {
                resolver.insert(BaseItems.CONTENT_URI, values);
            }
        }

        public static boolean deleteItem(Context context, String key) {
            ContentResolver resolver = context.getContentResolver();
            int row = resolver.delete(BaseItems.CONTENT_URI, BaseItems.COLUMN_NAME_KEY + "=?", new String[]{
                    key
            });
            if (row <= 0) {
                return false;
            }
            return true;
        }
    }
}
