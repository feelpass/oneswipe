/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.philleeran.flicktoucher.db;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import com.philleeran.flicktoucher.utils.L;

import java.util.HashMap;

/**
 * Provides access to a database of notes. Each note has a title, the note
 * itself, a creation date and a modified data.
 */
public class PadProvider extends ContentProvider {

    private static final String DATABASE_NAME = "pad60.db";

    private static final int DATABASE_VERSION = 7;

    private static HashMap<String, String> sAppsProjectionMap;

    private static HashMap<String, String> sPadsProjectionMap;

    private static HashMap<String, String> sSettingsProjectionMap;

    private static HashMap<String, String> sNotificationsProjectionMap;

    private static HashMap<String, String> sBaseItemsProjectionMap;


    // The incoming URI matches the Notes URI pattern
    private static final int APPS = 1;

    private static final int APP_ID = 2;

    private static final int PADS = 3;

    private static final int PAD_GROUPID_LISTID = 4;

    private static final int PAD_ID = 5;

    private static final int SETTINGS = 6;

    private static final int SETTING_ID = 7;

    private static final int NOTIFICATIONS = 8;

    private static final int NOTIFICATION_ID = 9;

    private static final int BASEITEMS = 10;

    private static final int BASEITEM_ID = 11;

    private static final UriMatcher sUriMatcher;

    // Handle to a new DatabaseHelper.
    private DatabaseHelper mOpenHelper;

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(PhilPad.AUTHORITY, "apps", APPS);
        sUriMatcher.addURI(PhilPad.AUTHORITY, "apps/#", APP_ID);
        sUriMatcher.addURI(PhilPad.AUTHORITY, "pads", PADS);
        sUriMatcher.addURI(PhilPad.AUTHORITY, "pads/#", PAD_ID);
        sUriMatcher.addURI(PhilPad.AUTHORITY, "pads/#/#", PAD_GROUPID_LISTID);
        sUriMatcher.addURI(PhilPad.AUTHORITY, "settings", SETTINGS);
        sUriMatcher.addURI(PhilPad.AUTHORITY, "settings/#", SETTING_ID);
        sUriMatcher.addURI(PhilPad.AUTHORITY, "notifications", NOTIFICATIONS);
        sUriMatcher.addURI(PhilPad.AUTHORITY, "notifications/#", NOTIFICATION_ID);
        sUriMatcher.addURI(PhilPad.AUTHORITY, "baseitems", BASEITEMS);
        sUriMatcher.addURI(PhilPad.AUTHORITY, "baseitems/#", BASEITEM_ID);

        sAppsProjectionMap = new HashMap<>();
        sAppsProjectionMap.put(PhilPad.Apps._ID, PhilPad.Apps._ID);
        sAppsProjectionMap.put(PhilPad.Apps.COLUMN_NAME_COMPONENT_NAME, PhilPad.Apps.COLUMN_NAME_COMPONENT_NAME);
        sAppsProjectionMap.put(PhilPad.Apps.COLUMN_NAME_TITLE, PhilPad.Apps.COLUMN_NAME_TITLE);
        sAppsProjectionMap.put(PhilPad.Apps.COLUMN_NAME_IMAGEFILE, PhilPad.Apps.COLUMN_NAME_IMAGEFILE);
        sAppsProjectionMap.put(PhilPad.Apps.COLUMN_NAME_LAUNCH_COUNT, PhilPad.Apps.COLUMN_NAME_LAUNCH_COUNT);

        sPadsProjectionMap = new HashMap<>();
        sPadsProjectionMap.put(PhilPad.Pads._ID, PhilPad.Pads._ID);
        sPadsProjectionMap.put(PhilPad.Pads.COLUMN_NAME_GROUPID, PhilPad.Pads.COLUMN_NAME_GROUPID);
        sPadsProjectionMap.put(PhilPad.Pads.COLUMN_NAME_LISTID, PhilPad.Pads.COLUMN_NAME_LISTID);
        sPadsProjectionMap.put(PhilPad.Pads.COLUMN_NAME_IMAGEFILE, PhilPad.Pads.COLUMN_NAME_IMAGEFILE);
        sPadsProjectionMap.put(PhilPad.Pads.COLUMN_NAME_TYPE, PhilPad.Pads.COLUMN_NAME_TYPE);
        sPadsProjectionMap.put(PhilPad.Pads.COLUMN_NAME_TITLE, PhilPad.Pads.COLUMN_NAME_TITLE);
        sPadsProjectionMap.put(PhilPad.Pads.COLUMN_NAME_PACKAGE_NAME, PhilPad.Pads.COLUMN_NAME_PACKAGE_NAME);
        sPadsProjectionMap.put(PhilPad.Pads.COLUMN_NAME_TOGROUP, PhilPad.Pads.COLUMN_NAME_TOGROUP);
        sPadsProjectionMap.put(PhilPad.Pads.COLUMN_NAME_LAUNCH_COUNT, PhilPad.Pads.COLUMN_NAME_LAUNCH_COUNT);
        sPadsProjectionMap.put(PhilPad.Pads.COLUMN_NAME_ISRUNNING, PhilPad.Pads.COLUMN_NAME_ISRUNNING);
        sPadsProjectionMap.put(PhilPad.Pads.COLUMN_NAME_ISNOTI, PhilPad.Pads.COLUMN_NAME_ISNOTI);
        sPadsProjectionMap.put(PhilPad.Pads.COLUMN_NAME_FUNCTION_MODE, PhilPad.Pads.COLUMN_NAME_FUNCTION_MODE);
        sPadsProjectionMap.put(PhilPad.Pads.COLUMN_NAME_EXTRA_DATA, PhilPad.Pads.COLUMN_NAME_EXTRA_DATA);
        sPadsProjectionMap.put(PhilPad.Pads.COLUMN_NAME_CREATE_DATE, PhilPad.Pads.COLUMN_NAME_CREATE_DATE);
        sPadsProjectionMap.put(PhilPad.Pads.COLUMN_NAME_MODIFICATION_DATE, PhilPad.Pads.COLUMN_NAME_MODIFICATION_DATE);

        sSettingsProjectionMap = new HashMap<>();
        sSettingsProjectionMap.put(PhilPad.Settings._ID, PhilPad.Settings._ID);
        sSettingsProjectionMap.put(PhilPad.Settings.COLUMN_NAME_KEY, PhilPad.Settings.COLUMN_NAME_KEY);
        sSettingsProjectionMap.put(PhilPad.Settings.COLUMN_NAME_VALUE, PhilPad.Settings.COLUMN_NAME_VALUE);
        sSettingsProjectionMap.put(PhilPad.Settings.COLUMN_NAME_CREATED_DATE, PhilPad.Settings.COLUMN_NAME_CREATED_DATE);
        sSettingsProjectionMap.put(PhilPad.Settings.COLUMN_NAME_LAST_UPDATED, PhilPad.Settings.COLUMN_NAME_LAST_UPDATED);


        sNotificationsProjectionMap = new HashMap<>();
        sNotificationsProjectionMap.put(PhilPad.Notifications._ID, PhilPad.Notifications._ID);
        sNotificationsProjectionMap.put(PhilPad.Notifications.COLUMN_NAME_PACKAGE_NAME, PhilPad.Notifications.COLUMN_NAME_PACKAGE_NAME);
        sNotificationsProjectionMap.put(PhilPad.Notifications.COLUMN_NAME_APPLICATION_NAME, PhilPad.Notifications.COLUMN_NAME_APPLICATION_NAME);
        sNotificationsProjectionMap.put(PhilPad.Notifications.COLUMN_NAME_CONTENT_INTENT, PhilPad.Notifications.COLUMN_NAME_CONTENT_INTENT);
        sNotificationsProjectionMap.put(PhilPad.Notifications.COLUMN_NAME_DELETE_INTENT, PhilPad.Notifications.COLUMN_NAME_DELETE_INTENT);
        sNotificationsProjectionMap.put(PhilPad.Notifications.COLUMN_NAME_LARGE_ICON, PhilPad.Notifications.COLUMN_NAME_LARGE_ICON);
        sNotificationsProjectionMap.put(PhilPad.Notifications.COLUMN_NAME_TICKER_TEXT, PhilPad.Notifications.COLUMN_NAME_TICKER_TEXT);
        sNotificationsProjectionMap.put(PhilPad.Notifications.COLUMN_NAME_EXTRA_DATA, PhilPad.Notifications.COLUMN_NAME_EXTRA_DATA);
        sNotificationsProjectionMap.put(PhilPad.Notifications.COLUMN_NAME_CREATED_DATE, PhilPad.Notifications.COLUMN_NAME_CREATED_DATE);
        sNotificationsProjectionMap.put(PhilPad.Notifications.COLUMN_NAME_LAST_UPDATED, PhilPad.Notifications.COLUMN_NAME_LAST_UPDATED);

        sBaseItemsProjectionMap = new HashMap<>();
        sBaseItemsProjectionMap.put(PhilPad.BaseItems._ID, PhilPad.BaseItems._ID);
        sBaseItemsProjectionMap.put(PhilPad.BaseItems.COLUMN_NAME_KEY, PhilPad.BaseItems.COLUMN_NAME_KEY);
        sBaseItemsProjectionMap.put(PhilPad.BaseItems.COLUMN_NAME_TITLE_NAME, PhilPad.BaseItems.COLUMN_NAME_TITLE_NAME);
        sBaseItemsProjectionMap.put(PhilPad.BaseItems.COLUMN_NAME_SUBTITLE_NAME, PhilPad.BaseItems.COLUMN_NAME_SUBTITLE_NAME);
        sBaseItemsProjectionMap.put(PhilPad.BaseItems.COLUMN_NAME_IMAGE_PATH, PhilPad.BaseItems.COLUMN_NAME_IMAGE_PATH);
        sBaseItemsProjectionMap.put(PhilPad.BaseItems.COLUMN_NAME_GROUPID, PhilPad.BaseItems.COLUMN_NAME_GROUPID);
        sBaseItemsProjectionMap.put(PhilPad.BaseItems.COLUMN_NAME_LISTID, PhilPad.BaseItems.COLUMN_NAME_LISTID);
        sBaseItemsProjectionMap.put(PhilPad.BaseItems.COLUMN_NAME_EXTRA_DATA, PhilPad.BaseItems.COLUMN_NAME_EXTRA_DATA);
        sBaseItemsProjectionMap.put(PhilPad.BaseItems.COLUMN_NAME_CREATED_DATE, PhilPad.BaseItems.COLUMN_NAME_CREATED_DATE);
        sBaseItemsProjectionMap.put(PhilPad.BaseItems.COLUMN_NAME_LAST_UPDATED, PhilPad.BaseItems.COLUMN_NAME_LAST_UPDATED);



    }

    static class DatabaseHelper extends SQLiteOpenHelper {
        Context mContext;
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            mContext = context;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            createAppsTable(db);
            createPadsTable(db);
            createSettingsTable(db);
            createNotificationsTable(db);
            createBaseItemTable(db);
        }

        private void createAppsTable(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE IF NOT EXISTS  " + PhilPad.Apps.TABLE_NAME + " (" + PhilPad.Apps._ID + " INTEGER PRIMARY KEY," + PhilPad.Apps.COLUMN_NAME_COMPONENT_NAME + " TEXT,"
                    + PhilPad.Apps.COLUMN_NAME_TITLE + " TEXT," + PhilPad.Apps.COLUMN_NAME_IMAGEFILE + " TEXT," + PhilPad.Apps.COLUMN_NAME_LAUNCH_COUNT + " INTEGER,"
                    + PhilPad.Apps.COLUMN_NAME_CREATE_DATE + " INTEGER," + PhilPad.Apps.COLUMN_NAME_MODIFICATION_DATE + " INTEGER," + PhilPad.Apps.COLUMN_NAME_ACCESS_DATE + " INTEGER" + ");");
        }

        private void createPadsTable(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE IF NOT EXISTS  " + PhilPad.Pads.TABLE_NAME + " (" + PhilPad.Pads._ID + " INTEGER PRIMARY KEY," + PhilPad.Pads.COLUMN_NAME_GROUPID + " INTEGER,"
                    + PhilPad.Pads.COLUMN_NAME_LISTID + " INTEGER," + PhilPad.Pads.COLUMN_NAME_IMAGEFILE + " TEXT," + PhilPad.Pads.COLUMN_NAME_TYPE + " INTEGER," + PhilPad.Pads.COLUMN_NAME_TITLE
                    + " TEXT," + PhilPad.Pads.COLUMN_NAME_PACKAGE_NAME + " TEXT," + PhilPad.Pads.COLUMN_NAME_TOGROUP + " INTEGER," + PhilPad.Pads.COLUMN_NAME_LAUNCH_COUNT + " INTEGER,"
                    + PhilPad.Pads.COLUMN_NAME_ISRUNNING + " INTEGER," + PhilPad.Pads.COLUMN_NAME_ISNOTI + " INTEGER," + PhilPad.Pads.COLUMN_NAME_FUNCTION_MODE + " INTEGER,"
                    + PhilPad.Pads.COLUMN_NAME_EXTRA_DATA + " TEXT," + PhilPad.Pads.COLUMN_NAME_CREATE_DATE + " INTEGER," + PhilPad.Pads.COLUMN_NAME_MODIFICATION_DATE + " INTEGER" + ");");
        }

        private void createSettingsTable(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE IF NOT EXISTS " + PhilPad.Settings.TABLE_NAME + " ("//
                    + PhilPad.Settings._ID + " INTEGER PRIMARY KEY,"//
                    + PhilPad.Settings.COLUMN_NAME_KEY + " TEXT unique,"//
                    + PhilPad.Settings.COLUMN_NAME_VALUE + " TEXT,"//
                    + PhilPad.Settings.COLUMN_NAME_CREATED_DATE + " INTEGER,"//
                    + PhilPad.Settings.COLUMN_NAME_LAST_UPDATED + " INTEGER"//
                    + ");");
        }

        private void createNotificationsTable(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE IF NOT EXISTS " + PhilPad.Notifications.TABLE_NAME + " ("//
                    + PhilPad.Notifications._ID + " INTEGER PRIMARY KEY,"//
                    + PhilPad.Notifications.COLUMN_NAME_PACKAGE_NAME + " TEXT,"//
                    + PhilPad.Notifications.COLUMN_NAME_APPLICATION_NAME + " TEXT,"//
                    + PhilPad.Notifications.COLUMN_NAME_CONTENT_INTENT + " BLOB,"//
                    + PhilPad.Notifications.COLUMN_NAME_DELETE_INTENT + " BLOB,"//
                    + PhilPad.Notifications.COLUMN_NAME_LARGE_ICON + " BLOB,"//
                    + PhilPad.Notifications.COLUMN_NAME_TICKER_TEXT + " TEXT,"//
                    + PhilPad.Notifications.COLUMN_NAME_EXTRA_DATA + " TEXT,"//
                    + PhilPad.Notifications.COLUMN_NAME_CREATED_DATE + " INTEGER,"//
                    + PhilPad.Notifications.COLUMN_NAME_LAST_UPDATED + " INTEGER"//
                    + ");");
        }

        private void createBaseItemTable(SQLiteDatabase db)
        {
            L.d("");
            db.execSQL("CREATE TABLE IF NOT EXISTS " + PhilPad.BaseItems.TABLE_NAME + " ("//
                    + PhilPad.BaseItems._ID + " INTEGER PRIMARY KEY,"//
                    + PhilPad.BaseItems.COLUMN_NAME_KEY + " TEXT,"//
                    + PhilPad.BaseItems.COLUMN_NAME_TITLE_NAME + " TEXT,"//
                    + PhilPad.BaseItems.COLUMN_NAME_SUBTITLE_NAME + " TEXT,"//
                    + PhilPad.BaseItems.COLUMN_NAME_IMAGE_PATH + " TEXT,"//
                    + PhilPad.BaseItems.COLUMN_NAME_GROUPID + " INTEGER,"//
                    + PhilPad.BaseItems.COLUMN_NAME_LISTID + " INTEGER,"//
                    + PhilPad.BaseItems.COLUMN_NAME_EXTRA_DATA + " TEXT,"//
                    + PhilPad.BaseItems.COLUMN_NAME_CREATED_DATE + " INTEGER,"//
                    + PhilPad.BaseItems.COLUMN_NAME_LAST_UPDATED + " INTEGER"//
                    + ");");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
/*
            db.execSQL("DROP TABLE IF EXISTS apps");
            db.execSQL("DROP TABLE IF EXISTS pads");
            db.execSQL("DROP TABLE IF EXISTS settings");
            onCreate(db);
*/
            if (oldVersion <= 5) {
                createNotificationsTable(db);
            }
            if (oldVersion <= 6) {
                createBaseItemTable(db);
/*
                updateDbForMakeGroupKey(mContext);
                PhilPad.Settings.putBoolean(mContext.getContentResolver(), PhilPad.Settings.SETTINGS_KEY_COMPLETE_FIRST_ADD_APP, true);
*/
            }
        }
    }

    @Override
    public boolean onCreate() {
        Context context = getContext();
        mOpenHelper = new DatabaseHelper(context);
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        switch (sUriMatcher.match(uri)) {
            case APPS:
                qb.setTables(PhilPad.Apps.TABLE_NAME);
                qb.setProjectionMap(sAppsProjectionMap);
                break;
            case APP_ID:
                qb.setTables(PhilPad.Apps.TABLE_NAME);
                qb.setProjectionMap(sAppsProjectionMap);
                qb.appendWhere(PhilPad.Apps._ID + // the name of the ID column
                        "=" + uri.getPathSegments().get(PhilPad.Apps.NOTE_ID_PATH_POSITION));
                break;
            case PADS:
                qb.setTables(PhilPad.Pads.TABLE_NAME);
                qb.setProjectionMap(sPadsProjectionMap);
                break;
            case PAD_ID:
                qb.setTables(PhilPad.Pads.TABLE_NAME);
                qb.setProjectionMap(sPadsProjectionMap);
                // qb.appendWhere(PhilPad.Pads._ID + // the name of the ID
                // column
                // "=" +
                // uri.getPathSegments().get(PhilPad.Pads.NOTE_ID_PATH_POSITION));
                break;
            case PAD_GROUPID_LISTID:
                qb.setTables(PhilPad.Pads.TABLE_NAME);
                qb.setProjectionMap(sPadsProjectionMap);
                qb.appendWhere(PhilPad.Pads.COLUMN_NAME_GROUPID + "=" + uri.getPathSegments().get(PhilPad.Pads.PAD_GROUPID_PATH_POSITION) + " AND " + PhilPad.Pads.COLUMN_NAME_LISTID + "="
                        + uri.getPathSegments().get(PhilPad.Pads.PAD_LISTID_PATH_POSITION));

                // column
                // "=" +
                // uri.getPathSegments().get(PhilPad.Pads.NOTE_ID_PATH_POSITION));
                break;
            case SETTINGS:
                qb.setTables(PhilPad.Settings.TABLE_NAME);
                qb.setProjectionMap(sSettingsProjectionMap);
                break;
            case SETTING_ID:
                qb.setTables(PhilPad.Settings.TABLE_NAME);
                qb.setProjectionMap(sSettingsProjectionMap);
                qb.appendWhere(PhilPad.Settings._ID + "=" + uri.getPathSegments().get(PhilPad.Settings.SETTINGS_ID_PATH_POSITION));
                break;

            case BASEITEMS:
                qb.setTables(PhilPad.BaseItems.TABLE_NAME);
                qb.setProjectionMap(sBaseItemsProjectionMap);
                break;
            case BASEITEM_ID:
                qb.setTables(PhilPad.BaseItems.TABLE_NAME);
                qb.setProjectionMap(sBaseItemsProjectionMap);
                qb.appendWhere(PhilPad.BaseItems._ID + "=" + uri.getPathSegments().get(PhilPad.BaseItems.BASEITEMS_ID_PATH_POSITION));
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        String orderBy;
        if (TextUtils.isEmpty(sortOrder)) {
            orderBy = PhilPad.Apps.DEFAULT_SORT_ORDER;
        } else {
            orderBy = sortOrder;
        }

        switch (sUriMatcher.match(uri)) {
            case APPS:
            case APP_ID:
                if (TextUtils.isEmpty(sortOrder)) {
                    orderBy = PhilPad.Apps.DEFAULT_SORT_ORDER;
                } else {
                    orderBy = sortOrder;
                }
                break;
            case PADS:
            case PAD_ID:
            case PAD_GROUPID_LISTID:
                if (TextUtils.isEmpty(sortOrder)) {
                    orderBy = PhilPad.Pads.DEFAULT_SORT_ORDER;
                } else {
                    orderBy = sortOrder;
                }
                break;
            case SETTINGS:
            case SETTING_ID:
                if (TextUtils.isEmpty(sortOrder)) {
                    orderBy = PhilPad.Settings.DEFAULT_SORT_ORDER;
                } else {
                    orderBy = sortOrder;
                }
                break;
            case BASEITEMS:
            case BASEITEM_ID:
                if (TextUtils.isEmpty(sortOrder)) {
                    orderBy = PhilPad.BaseItems.DEFAULT_SORT_ORDER;
                } else {
                    orderBy = sortOrder;
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        SQLiteDatabase db = mOpenHelper.getReadableDatabase();

        Cursor c = qb.query(db, // The database to query
                projection, // The columns to return from the query
                selection, // The columns for the where clause
                selectionArgs, // The values for the where clause
                null, // don't group the rows
                null, // don't filter by row groups
                orderBy // The sort order
                );

        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public String getType(Uri uri) {

        /**
         * Chooses the MIME type based on the incoming URI pattern
         */
        switch (sUriMatcher.match(uri)) {

            case APPS:
                return PhilPad.Apps.CONTENT_TYPE;
            case APP_ID:
                return PhilPad.Apps.CONTENT_ITEM_TYPE;
            case PADS:
                return PhilPad.Pads.CONTENT_TYPE;
            case PAD_ID:
                return PhilPad.Pads.CONTENT_ITEM_TYPE;
            case SETTINGS:
                return PhilPad.Settings.CONTENT_TYPE;
            case SETTING_ID:
                return PhilPad.Settings.CONTENT_ITEM_TYPE;
            case BASEITEMS:
                return PhilPad.BaseItems.CONTENT_TYPE;
            case BASEITEM_ID:
                return PhilPad.BaseItems.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
        if (sUriMatcher.match(uri) != APPS && sUriMatcher.match(uri) != PADS && sUriMatcher.match(uri) != SETTINGS && sUriMatcher.match(uri) != BASEITEMS) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        ContentValues values;

        if (initialValues != null) {
            values = new ContentValues(initialValues);
        } else {
            values = new ContentValues();
        }
        Long now = Long.valueOf(System.currentTimeMillis());
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        long rowId;
        switch (sUriMatcher.match(uri)) {
            case APPS:
                if (values.containsKey(PhilPad.Apps.COLUMN_NAME_LAUNCH_COUNT) == false) {
                    values.put(PhilPad.Apps.COLUMN_NAME_LAUNCH_COUNT, 0);
                }
                if (values.containsKey(PhilPad.Apps.COLUMN_NAME_CREATE_DATE) == false) {
                    values.put(PhilPad.Apps.COLUMN_NAME_CREATE_DATE, now);
                }

                if (values.containsKey(PhilPad.Apps.COLUMN_NAME_MODIFICATION_DATE) == false) {
                    values.put(PhilPad.Apps.COLUMN_NAME_MODIFICATION_DATE, now);
                }

                if (values.containsKey(PhilPad.Apps.COLUMN_NAME_TITLE) == false) {
                    Resources r = Resources.getSystem();
                    values.put(PhilPad.Apps.COLUMN_NAME_TITLE, r.getString(android.R.string.untitled));
                }

                rowId = db.insert(PhilPad.Apps.TABLE_NAME, // The table to
                                                           // insert
                        PhilPad.Apps.COLUMN_NAME_COMPONENT_NAME, // A hack, SQLite
                                                               // sets this
                        values // A map of column names, and the values to
                               // insert
                        );
                if (rowId > 0) {
                    Uri appUri = ContentUris.withAppendedId(PhilPad.Apps.CONTENT_ID_URI_BASE, rowId);
                    getContext().getContentResolver().notifyChange(appUri, null);
                    return appUri;
                }
                break;
            case PADS:
                if (values.containsKey(PhilPad.Pads.COLUMN_NAME_GROUPID) == false) {
                    values.put(PhilPad.Pads.COLUMN_NAME_GROUPID, -1);
                }
                if (values.containsKey(PhilPad.Pads.COLUMN_NAME_LISTID) == false) {
                    values.put(PhilPad.Pads.COLUMN_NAME_LISTID, -1);
                }
                if (values.containsKey(PhilPad.Pads.COLUMN_NAME_TYPE) == false) {
                    values.put(PhilPad.Pads.COLUMN_NAME_TYPE, -1);
                }
                if (values.containsKey(PhilPad.Pads.COLUMN_NAME_LISTID) == false) {
                    values.put(PhilPad.Pads.COLUMN_NAME_LISTID, -1);
                }
                if (values.containsKey(PhilPad.Pads.COLUMN_NAME_LAUNCH_COUNT) == false) {
                    values.put(PhilPad.Pads.COLUMN_NAME_LAUNCH_COUNT, 0);
                }

                if (values.containsKey(PhilPad.Pads.COLUMN_NAME_FUNCTION_MODE) == false) {
                    values.put(PhilPad.Pads.COLUMN_NAME_FUNCTION_MODE, 0);
                }

                if (values.containsKey(PhilPad.Pads.COLUMN_NAME_CREATE_DATE) == false) {
                    values.put(PhilPad.Pads.COLUMN_NAME_CREATE_DATE, now);
                }

                if (values.containsKey(PhilPad.Pads.COLUMN_NAME_MODIFICATION_DATE) == false) {
                    values.put(PhilPad.Pads.COLUMN_NAME_MODIFICATION_DATE, now);
                }

                if (values.containsKey(PhilPad.Pads.COLUMN_NAME_TITLE) == false) {
                    Resources r = Resources.getSystem();
                    values.put(PhilPad.Pads.COLUMN_NAME_TITLE, r.getString(android.R.string.untitled));
                }

                rowId = db.insert(PhilPad.Pads.TABLE_NAME, // The table to
                                                           // insert
                        null, // A hack, SQLite sets this
                        values // A map of column names, and the values to
                               // insert
                        );
                if (rowId > 0) {
                    Uri appUri = ContentUris.withAppendedId(PhilPad.Pads.CONTENT_ID_URI_BASE, rowId);
                    getContext().getContentResolver().notifyChange(appUri, null);
                    return appUri;
                }
                break;

            case SETTINGS:
                if (values.containsKey(PhilPad.Settings.COLUMN_NAME_KEY) == false) {
                    values.put(PhilPad.Settings.COLUMN_NAME_KEY, "");
                }
                if (values.containsKey(PhilPad.Settings.COLUMN_NAME_VALUE) == false) {
                    values.put(PhilPad.Settings.COLUMN_NAME_VALUE, "");
                }
                if (values.containsKey(PhilPad.Settings.COLUMN_NAME_CREATED_DATE) == false) {
                    values.put(PhilPad.Settings.COLUMN_NAME_CREATED_DATE, now);
                }
                if (values.containsKey(PhilPad.Settings.COLUMN_NAME_LAST_UPDATED) == false) {
                    values.put(PhilPad.Settings.COLUMN_NAME_LAST_UPDATED, now);
                }
                rowId = db.insert(PhilPad.Settings.TABLE_NAME, null, values);

                if (rowId > 0) {
                    Uri settingsUri = ContentUris.withAppendedId(PhilPad.Settings.CONTENT_ID_URI_BASE, rowId);
                    getContext().getContentResolver().notifyChange(settingsUri, null);
                    return settingsUri;
                }
                break;

            case BASEITEMS:
                if (values.containsKey(PhilPad.BaseItems.COLUMN_NAME_KEY) == false) {
                    values.put(PhilPad.BaseItems.COLUMN_NAME_KEY, "");
                }
                if (values.containsKey(PhilPad.BaseItems.COLUMN_NAME_KEY) == false) {
                    values.put(PhilPad.BaseItems.COLUMN_NAME_KEY, "");
                }
                if (values.containsKey(PhilPad.BaseItems.COLUMN_NAME_CREATED_DATE) == false) {
                    values.put(PhilPad.BaseItems.COLUMN_NAME_CREATED_DATE, now);
                }
                if (values.containsKey(PhilPad.BaseItems.COLUMN_NAME_LAST_UPDATED) == false) {
                    values.put(PhilPad.BaseItems.COLUMN_NAME_LAST_UPDATED, now);
                }
                rowId = db.insert(PhilPad.BaseItems.TABLE_NAME, null, values);

                if (rowId > 0) {
                    Uri baseItemsUri = ContentUris.withAppendedId(PhilPad.BaseItems.CONTENT_ID_URI_BASE, rowId);
                    getContext().getContentResolver().notifyChange(baseItemsUri, null);
                    return baseItemsUri;
                }
                break;


            default:
                break;
        }

        throw new SQLException("Failed to insert row into " + uri);
    }


    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        int numInserted = 0;
        String table = null;
        int uriType = sUriMatcher.match(uri);
        switch (uriType) {
            case PADS:
                table = PhilPad.Pads.TABLE_NAME;
                break;
            default:
                break;
        }
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            for (ContentValues cv : values) {
                long newID = db.insertOrThrow(table, null, cv);
                if (newID <= 0) {
                    throw new SQLException("Failed to insert row into " + uri);
                }
            }
            db.setTransactionSuccessful();
            getContext().getContentResolver().notifyChange(uri, null);
            numInserted = values.length;
        } finally {
            db.endTransaction();
        }
        return numInserted;
    }




    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        String finalWhere;
        int count;
        switch (sUriMatcher.match(uri)) {
            case APPS:
                count = db.delete(PhilPad.Apps.TABLE_NAME, // The database table
                                                           // name
                        where, // The incoming where clause column names
                        whereArgs // The incoming where clause values
                        );
                break;
            case APP_ID:
                finalWhere = PhilPad.Apps._ID + // The ID column name
                        " = " + // test for equality
                        uri.getPathSegments(). // the incoming note ID
                                get(PhilPad.Apps.NOTE_ID_PATH_POSITION);

                if (where != null) {
                    finalWhere = finalWhere + " AND " + where;
                }

                // Performs the delete.
                count = db.delete(PhilPad.Apps.TABLE_NAME, // The database table
                        finalWhere, // The final WHERE clause
                        whereArgs // The incoming where clause values.
                        );
                break;
            case PADS:
                count = db.delete(PhilPad.Pads.TABLE_NAME, // The database table
                                                           // name
                        where, // The incoming where clause column names
                        whereArgs // The incoming where clause values
                        );
                break;
            case PAD_ID:
                finalWhere = PhilPad.Pads._ID + // The ID column name
                        " = " + // test for equality
                        uri.getPathSegments(). // the incoming note ID
                                get(PhilPad.Pads.PAD_ID_PATH_POSITION);

                if (where != null) {
                    finalWhere = finalWhere + " AND " + where;
                }

                // Performs the delete.
                count = db.delete(PhilPad.Pads.TABLE_NAME, // The database table
                        finalWhere, // The final WHERE clause
                        whereArgs // The incoming where clause values.
                        );
                break;
            case SETTINGS:
                count = db.delete(PhilPad.Settings.TABLE_NAME, // The database
                                                               // table
                        // name
                        where, // The incoming where clause column names
                        whereArgs // The incoming where clause values
                        );
                break;
            case SETTING_ID:
                finalWhere = PhilPad.Settings._ID + // The ID column name
                        " = " + // test for equality
                        uri.getPathSegments(). // the incoming note ID
                                get(PhilPad.Settings.SETTINGS_ID_PATH_POSITION);

                if (where != null) {
                    finalWhere = finalWhere + " AND " + where;
                }

                // Performs the delete.
                count = db.delete(PhilPad.Settings.TABLE_NAME, // The database
                                                               // table
                        finalWhere, // The final WHERE clause
                        whereArgs // The incoming where clause values.
                        );
                break;
            case BASEITEMS:
                count = db.delete(PhilPad.BaseItems.TABLE_NAME, // The database
                        // table
                        // name
                        where, // The incoming where clause column names
                        whereArgs // The incoming where clause values
                );
                break;

            case BASEITEM_ID:
                finalWhere = PhilPad.BaseItems._ID + // The ID column name
                        " = " + // test for equality
                        uri.getPathSegments(). // the incoming note ID
                                get(PhilPad.BaseItems.BASEITEMS_ID_PATH_POSITION);

                if (where != null) {
                    finalWhere = finalWhere + " AND " + where;
                }

                // Performs the delete.
                count = db.delete(PhilPad.BaseItems.TABLE_NAME, // The database
                        // table
                        finalWhere, // The final WHERE clause
                        whereArgs // The incoming where clause values.
                );
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count;
        String finalWhere;
        Long now = Long.valueOf(System.currentTimeMillis());
        switch (sUriMatcher.match(uri)) {
            case APPS:

                // Does the update and returns the number of rows updated.
                count = db.update(PhilPad.Apps.TABLE_NAME, // The database table
                                                           // name.
                        values, // A map of column names and new values to use.
                        where, // The where clause column names.
                        whereArgs // The where clause column values to select
                                  // on.
                        );
                break;

            case APP_ID:
                String appId = uri.getPathSegments().get(PhilPad.Apps.NOTE_ID_PATH_POSITION);
                finalWhere = PhilPad.Apps._ID + // The ID column name
                        " = " + // test for equality
                        appId;

                if (where != null) {
                    finalWhere = finalWhere + " AND " + where;
                }

                count = db.update(PhilPad.Apps.TABLE_NAME, // The database table
                                                           // name.
                        values, // A map of column names and new values to use.
                        finalWhere, // The final WHERE clause to use
                        whereArgs // The where clause column values to select
                        );
                break;

            case PADS:
                if (values != null) {
                    values = new ContentValues(values);
                } else {
                    values = new ContentValues();
                }

                if (values.containsKey(PhilPad.Pads.COLUMN_NAME_MODIFICATION_DATE) == false) {
                    values.put(PhilPad.Pads.COLUMN_NAME_MODIFICATION_DATE, now);
                }

                // Does the update and returns the number of rows updated.
                count = db.update(PhilPad.Pads.TABLE_NAME, // The database table
                                                           // name.
                        values, // A map of column names and new values to use.
                        where, // The where clause column names.
                        whereArgs // The where clause column values to select
                                  // on.
                        );
                break;

            case PAD_ID:
                String padId = uri.getPathSegments().get(PhilPad.Pads.PAD_ID_PATH_POSITION);
                finalWhere = PhilPad.Pads._ID + // The ID column name
                        " = " + // test for equality
                        padId;

                if (where != null) {
                    finalWhere = finalWhere + " AND " + where;
                }

                count = db.update(PhilPad.Pads.TABLE_NAME, // The database table
                                                           // name.
                        values, // A map of column names and new values to use.
                        finalWhere, // The final WHERE clause to use
                        whereArgs // The where clause column values to select
                        );
                break;
            case SETTINGS:
                count = db.update(PhilPad.Settings.TABLE_NAME,//
                        values, // A map of column names and new values to use.
                        where, // The where clause column names.
                        whereArgs // The where clause column values to select
                        // on.
                        );
                break;
            case SETTING_ID:
                @SuppressWarnings("unused")
                String settingsId = uri.getPathSegments().get(PhilPad.Settings.SETTINGS_ID_PATH_POSITION);

                finalWhere = PhilPad.Settings._ID + // The ID column name
                        " = " + // test for equality
                        uri.getPathSegments(). // the incoming settings ID
                                get(PhilPad.Settings.SETTINGS_ID_PATH_POSITION);

                if (where != null) {
                    finalWhere = finalWhere + " AND " + where;
                }

                count = db.update(PhilPad.Settings.TABLE_NAME, values, finalWhere, whereArgs);
                break;

            case BASEITEMS:
                count = db.update(PhilPad.BaseItems.TABLE_NAME,//
                        values, // A map of column names and new values to use.
                        where, // The where clause column names.
                        whereArgs // The where clause column values to select
                        // on.
                );

                break;
            case BASEITEM_ID:
                @SuppressWarnings("unused")
                String itemsId = uri.getPathSegments().get(PhilPad.BaseItems.BASEITEMS_ID_PATH_POSITION);

                finalWhere = PhilPad.BaseItems._ID + // The ID column name
                        " = " + // test for equality
                        uri.getPathSegments(). // the incoming settings ID
                                get(PhilPad.BaseItems.BASEITEMS_ID_PATH_POSITION);

                if (where != null) {
                    finalWhere = finalWhere + " AND " + where;
                }

                count = db.update(PhilPad.BaseItems.TABLE_NAME, values, finalWhere, whereArgs);
                break;
            
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return count;
    }

    DatabaseHelper getOpenHelperForTest() {
        return mOpenHelper;
    }
}
