package com.philleeran.flicktoucher.view.pad;


import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.text.TextUtils;

import com.philleeran.flicktoucher.PadUtils;
import com.philleeran.flicktoucher.R;
import com.philleeran.flicktoucher.db.PhilPad;
import com.philleeran.flicktoucher.utils.D;
import com.philleeran.flicktoucher.utils.L;
import com.philleeran.flicktoucher.utils.Utils;
import com.philleeran.flicktoucher.view.pad.listener.TopPackageUpdateListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class UpdateCurrentApps implements Runnable {

    private final Context mContext;
    private final int mPadSize;
    private final TopPackageUpdateListener mTopPackageUpdateListener;

    public UpdateCurrentApps(Context context, int padSize, TopPackageUpdateListener listener)
    {
        mTopPackageUpdateListener = listener;
        mContext = context;
        mPadSize = padSize;
    }
    public void run() {
        PackageManager pm = mContext.getPackageManager();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (!PadUtils.isUsageAccessEnable(mContext)) {
                return;
            }
            long currentTime = System.currentTimeMillis();
            Calendar endTime = Calendar.getInstance();
            endTime.setTimeInMillis(currentTime);
            Calendar beginTime = Calendar.getInstance();
            beginTime.set(Calendar.DATE, endTime.get(Calendar.DATE) - 1);
            beginTime.set(Calendar.MONTH, endTime.get(Calendar.MONTH));
            beginTime.set(Calendar.YEAR, endTime.get(Calendar.YEAR));

            UsageStatsManager mUsageStatsManager = (UsageStatsManager) mContext.getSystemService("usagestats");
            // We get usage stats for the last 10 seconds
            List<UsageStats> stats = mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, currentTime - 3 * 3600 * 1000, currentTime);
            // Sort the stats by the last time used

            Collections.sort(stats, new Comparator<UsageStats>() {
                @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                @Override
                public int compare(UsageStats lhs, UsageStats rhs) {
                    long lhsLong = lhs.getLastTimeUsed();
                    long rhsLong = rhs.getLastTimeUsed();
                    if (rhsLong > lhsLong)
                        return 1;
                    else if (rhsLong < lhsLong) {
                        return -1;
                    } else {
                        return 0;
                    }
                }
            });

            Bitmap transparent = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
            Bitmap bitmap = null;
            final int smallImageSize = D.Board.ICON_SIZE;
            final int smallImageInnerSize = D.Board.ICON_INNER_SIZE;
            final int smallImageMargin = D.Board.ICON_MARGIN_SIZE;
            bitmap = Bitmap.createScaledBitmap(transparent, smallImageSize * mPadSize, smallImageSize * mPadSize, true);
            if (transparent != null) {
                if (D.RECYCLE) {
                    transparent.recycle();
                }
                transparent = null;
            }

            Paint p = new Paint();
            p.setDither(true);
            p.setFlags(Paint.ANTI_ALIAS_FLAG);
            int folderColor = PhilPad.Settings.getInt(mContext.getContentResolver(), PhilPad.Settings.SETTINGS_KEY_PAD_SPECIAL_COLOR, Utils.getColor(mContext, R.color.color_special_background));
            p.setColor(folderColor);
            Paint pp = new Paint();
            pp.setDither(true);
            pp.setFlags(Paint.ANTI_ALIAS_FLAG);
            Canvas c = new Canvas(bitmap);

            c.drawRoundRect(new RectF(0, 0, smallImageSize * mPadSize, smallImageSize * mPadSize), D.GRID_RECT_ROUND_FACTOR, D.GRID_RECT_ROUND_FACTOR, p);

            ContextWrapper cw = new ContextWrapper(mContext.getApplicationContext());
            String fileName = Utils.getStringPref(mContext, D.FILE_PATH_RECENT_APPLICATIONS, null);
            if (TextUtils.isEmpty(fileName) == false) {
                File directory = cw.getDir(D.DIR_PATH_DEFAULT, Context.MODE_PRIVATE);
                File oldpath = new File(directory, fileName);
                oldpath.delete();
            }

            File directory = cw.getDir(D.DIR_PATH_DEFAULT, Context.MODE_PRIVATE);
            int i;
            int j;
            j = 0;
            boolean isLastAppChecked = false;

            ContentValues[] bulkToInsert;
            ArrayList<ContentValues> valueList = new ArrayList<>();

            if (stats != null && stats.size() > 0) {
                for (i = 0; i < stats.size(); i++) {

                    if (i > mPadSize * mPadSize)
                        break;

                    UsageStats recent = stats.get(i);
                    if (recent.getLastTimeUsed() == 0)
                        continue;
                    String packageName = recent.getPackageName();
                    if (i == 0) {
                        mTopPackageUpdateListener.onTopPackageUpdated(packageName);
                        continue;
                    }
                    File checkPath = new File(directory, packageName + ".png");
                    if (checkPath.exists()) {
                        FileInputStream fis = null;
                        Intent intent = pm.getLaunchIntentForPackage(packageName);
                        String labelName = null;
                        try {
                            ActivityInfo activityInfo = pm.getActivityInfo(intent.getComponent(), 0);
                            labelName = activityInfo.loadLabel(pm).toString();
                        } catch (Exception e) {
                            L.e(e);
                        }
                        String intentToUri = intent != null ? intent.toUri(0) : "";
                        if (isLastAppChecked == false && intent != null) {
                            PhilPad.Pads.setLastApp(mContext, intentToUri, "file://" + checkPath.getPath());
                            isLastAppChecked = true;
                        }
                        try {
                            fis = new FileInputStream(checkPath);
                            Bitmap bm = BitmapFactory.decodeStream(fis);
                            if (bm != null) {
                                Rect rect = new Rect(0, 0, bm.getWidth(), bm.getHeight());
                                c.drawBitmap(bm, rect, new Rect(smallImageSize * (j % mPadSize) + smallImageMargin, smallImageSize * (j / mPadSize) + smallImageMargin, smallImageSize * (j % mPadSize) + smallImageMargin
                                        + smallImageInnerSize, smallImageSize * (j / mPadSize) + smallImageMargin + smallImageInnerSize), pp);

                                ContentValues values = new ContentValues();
                                values.put(PhilPad.Pads.COLUMN_NAME_GROUPID, D.GROUPID_RECENT);
                                values.put(PhilPad.Pads.COLUMN_NAME_LISTID, j);
                                values.put(PhilPad.Pads.COLUMN_NAME_PACKAGE_NAME, intentToUri);
                                values.put(PhilPad.Pads.COLUMN_NAME_TYPE, PhilPad.Pads.PAD_TYPE_APPLICATION);
                                values.put(PhilPad.Pads.COLUMN_NAME_IMAGEFILE, "file://" + checkPath.getPath());
                                values.put(PhilPad.Pads.COLUMN_NAME_FUNCTION_MODE, 0);
                                values.put(PhilPad.Pads.COLUMN_NAME_EXTRA_DATA, 0);
                                values.put(PhilPad.Pads.COLUMN_NAME_TITLE, labelName);
                                valueList.add(values);

//                                        PhilPad.Pads.setPadItem(mContext, D.GROUPID_RECENT, j, PhilPad.Pads.PAD_TYPE_APPLICATION, packageName, packageName, "file://" + checkPath.getPath(), 0, null);
                                if (D.RECYCLE) {
                                    bm.recycle();
                                }
                                bm = null;
                            }
                            j++;
                        } catch (FileNotFoundException e) {
                            L.e(e);
                        } finally {
                            if (fis != null) {
                                try {
                                    fis.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }

                for (; j < D.ICON_MAX_COUNT; j++) {
                    ContentValues values = new ContentValues();
                    values.put(PhilPad.Pads.COLUMN_NAME_GROUPID, D.GROUPID_RECENT);
                    values.put(PhilPad.Pads.COLUMN_NAME_LISTID, j);
                    values.put(PhilPad.Pads.COLUMN_NAME_PACKAGE_NAME, "");
                    values.put(PhilPad.Pads.COLUMN_NAME_TYPE, PhilPad.Pads.PAD_TYPE_NULL);
                    values.put(PhilPad.Pads.COLUMN_NAME_IMAGEFILE, "");
                    values.put(PhilPad.Pads.COLUMN_NAME_FUNCTION_MODE, 0);
                    values.put(PhilPad.Pads.COLUMN_NAME_EXTRA_DATA, 0);
                    values.put(PhilPad.Pads.COLUMN_NAME_TITLE, "");
                    valueList.add(values);
                }

                bulkToInsert = new ContentValues[valueList.size()];
                valueList.toArray(bulkToInsert);
                PhilPad.Pads.setRecently(mContext, bulkToInsert);
            }

            mTopPackageUpdateListener.onRecentAppBitmapChanged(bitmap);
/*

            if (mRecentAppPadBitmap != null) {
                if (D.RECYCLE) {
                    mRecentAppPadBitmap.recycle();
                }
                mRecentAppPadBitmap = null;
            }
            mRecentAppPadBitmap = bitmap;
*/
        } else {
            ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RecentTaskInfo> list = am.getRecentTasks(16, android.app.ActivityManager.RECENT_WITH_EXCLUDED);
            Bitmap transparent = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
            Bitmap bitmap = null;
            final int smallImageSize = D.Board.ICON_SIZE;
            final int smallImageInnerSize = D.Board.ICON_INNER_SIZE;
            final int smallImageMargin = D.Board.ICON_MARGIN_SIZE;
            bitmap = Bitmap.createScaledBitmap(transparent, smallImageSize * mPadSize, smallImageSize * mPadSize, true);
            if (transparent != null) {
                if (D.RECYCLE) {
                    transparent.recycle();
                }
                transparent = null;
            }
            Paint p = new Paint();
            p.setDither(true);
            p.setFlags(Paint.ANTI_ALIAS_FLAG);
            p.setColor(0xFF41bdea);
            p.setAlpha(100);
            Paint pp = new Paint();
            pp.setDither(true);
            pp.setFlags(Paint.ANTI_ALIAS_FLAG);
            Canvas c = new Canvas(bitmap);
            c.drawRoundRect(new RectF(0, 0, smallImageSize * mPadSize, smallImageSize * mPadSize), D.GRID_RECT_ROUND_FACTOR, D.GRID_RECT_ROUND_FACTOR, p);

            ContextWrapper cw = new ContextWrapper(mContext.getApplicationContext());
            String fileName = Utils.getStringPref(mContext, D.FILE_PATH_RECENT_APPLICATIONS, null);
            if (TextUtils.isEmpty(fileName) == false) {
                File directory = cw.getDir(D.DIR_PATH_DEFAULT, Context.MODE_PRIVATE);
                File oldpath = new File(directory, fileName);
                oldpath.delete();
            }

            File directory = cw.getDir(D.DIR_PATH_DEFAULT, Context.MODE_PRIVATE);
            int i;
            int j;
            j = 0;
            boolean isLastAppChecked = false;

            ContentValues[] bulkToInsert;
            ArrayList<ContentValues> valueList = new ArrayList<>();

            if (list.size() > 0) {
                for (i = 1; i < list.size(); i++) {
                    ActivityManager.RecentTaskInfo recent = list.get(i);

                    String packageName = null;
                    try {
                        packageName = recent.baseIntent.getComponent().getPackageName();
                    } catch (Exception e) {
                        L.e(e);
                        continue;
                    }

                    if (TextUtils.isEmpty(packageName))
                        continue;

                    File checkPath = new File(directory, packageName + ".png");
                    if (checkPath.exists()) {
                        FileInputStream fis = null;
                        Intent intent = pm.getLaunchIntentForPackage(packageName);

                        String labelName = null;
                        try {
                            ActivityInfo activityInfo = pm.getActivityInfo(intent.getComponent(), 0);
                            labelName = activityInfo.loadLabel(pm).toString();
                        } catch (Exception e) {
                            L.e(e);
                            continue;
                        }

                        String intentToUri = intent != null ? intent.toUri(0) : "";
                        if (isLastAppChecked == false && intent != null) {
                            PhilPad.Pads.setLastApp(mContext, intentToUri, "file://" + checkPath.getPath());
                            isLastAppChecked = true;
                        }

                        try {
                            fis = new FileInputStream(checkPath);
                            Bitmap bm = BitmapFactory.decodeStream(fis);
                            if (bm != null) {
                                Rect rect = new Rect(0, 0, bm.getWidth(), bm.getHeight());
                                c.drawBitmap(bm, rect, new Rect(smallImageSize * (j % mPadSize) + smallImageMargin, smallImageSize * (j / mPadSize) + smallImageMargin, smallImageSize * (j % mPadSize) + smallImageMargin
                                        + smallImageInnerSize, smallImageSize * (j / mPadSize) + smallImageMargin + smallImageInnerSize), pp);
//                                        PhilPad.Pads.setPadItem(mContext, D.GROUPID_RECENT, j, PhilPad.Pads.PAD_TYPE_NULL, null, null, null, 0, null);
                                ContentValues values = new ContentValues();
                                values.put(PhilPad.Pads.COLUMN_NAME_GROUPID, D.GROUPID_RECENT);
                                values.put(PhilPad.Pads.COLUMN_NAME_LISTID, j);
                                values.put(PhilPad.Pads.COLUMN_NAME_PACKAGE_NAME, intentToUri);
                                values.put(PhilPad.Pads.COLUMN_NAME_TYPE, PhilPad.Pads.PAD_TYPE_APPLICATION);
                                values.put(PhilPad.Pads.COLUMN_NAME_IMAGEFILE, "file://" + checkPath.getPath());
                                values.put(PhilPad.Pads.COLUMN_NAME_FUNCTION_MODE, 0);
                                values.put(PhilPad.Pads.COLUMN_NAME_EXTRA_DATA, 0);
                                values.put(PhilPad.Pads.COLUMN_NAME_TITLE, labelName);
                                valueList.add(values);
                                if (D.RECYCLE) {
                                    bm.recycle();
                                }
                                bm = null;
                            }
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } finally {
                            if (fis != null) {
                                try {
                                    fis.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        j++;
                    }
                }

                for (; j < D.ICON_MAX_COUNT; j++) {
                    ContentValues values = new ContentValues();
                    values.put(PhilPad.Pads.COLUMN_NAME_GROUPID, D.GROUPID_RECENT);
                    values.put(PhilPad.Pads.COLUMN_NAME_LISTID, j);
                    values.put(PhilPad.Pads.COLUMN_NAME_PACKAGE_NAME, "");
                    values.put(PhilPad.Pads.COLUMN_NAME_TYPE, PhilPad.Pads.PAD_TYPE_NULL);
                    values.put(PhilPad.Pads.COLUMN_NAME_IMAGEFILE, "");
                    values.put(PhilPad.Pads.COLUMN_NAME_FUNCTION_MODE, 0);
                    values.put(PhilPad.Pads.COLUMN_NAME_EXTRA_DATA, 0);
                    values.put(PhilPad.Pads.COLUMN_NAME_TITLE, "");
                    valueList.add(values);
                }

                bulkToInsert = new ContentValues[valueList.size()];
                valueList.toArray(bulkToInsert);
                PhilPad.Pads.setRecently(mContext, bulkToInsert);
            }
            mTopPackageUpdateListener.onRecentAppBitmapChanged(bitmap);
        }

    }
}