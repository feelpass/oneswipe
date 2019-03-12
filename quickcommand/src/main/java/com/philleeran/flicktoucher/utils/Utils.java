
package com.philleeran.flicktoucher.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceManager;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;

import com.google.android.gms.analytics.HitBuilders;
import com.philleeran.flicktoucher.PadUtils;
import com.philleeran.flicktoucher.PhilPadApplication;
import com.philleeran.flicktoucher.R;
import com.philleeran.flicktoucher.db.PadItemInfo;
import com.philleeran.flicktoucher.db.PhilPad;
import com.philleeran.flicktoucher.utils.compat.IntentCompat;
import com.philleeran.flicktoucher.utils.compat.WindowManagerPolicyCompat;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class Utils {

    private static WeakReference<Resources> resources;
    private static List<ResolveInfo> mApps;

    public static String getStringPref(Context context, String key, String defualtValue) {
        SharedPreferences defaultSharedPref = PreferenceManager
                .getDefaultSharedPreferences(context);
        return defaultSharedPref.getString(key, defualtValue);
    }

    public static void setStringPref(Context context, String key, String value) {
        SharedPreferences defaultSharedPref = PreferenceManager
                .getDefaultSharedPreferences(context);
        defaultSharedPref.edit().putString(key, value).commit();
    }

    public static int getIntPref(Context context, String key, int defualtValue) {
        SharedPreferences defaultSharedPref = PreferenceManager
                .getDefaultSharedPreferences(context);
        return defaultSharedPref.getInt(key, defualtValue);
    }

    public static void setIntPref(Context context, String key, int value) {
        SharedPreferences defaultSharedPref = PreferenceManager
                .getDefaultSharedPreferences(context);
        defaultSharedPref.edit().putInt(key, value).commit();
    }

    public static long getLongPref(Context context, String key, long defualtValue) {
        SharedPreferences defaultSharedPref = PreferenceManager
                .getDefaultSharedPreferences(context);
        return defaultSharedPref.getLong(key, defualtValue);
    }

    public static void setLongPref(Context context, String key, long value) {
        SharedPreferences defaultSharedPref = PreferenceManager
                .getDefaultSharedPreferences(context);
        defaultSharedPref.edit().putLong(key, value).commit();
    }

    public static Boolean getBooleanPref(Context context, String key, boolean defualtValue) {
        SharedPreferences defaultSharedPref = PreferenceManager
                .getDefaultSharedPreferences(context);
        return defaultSharedPref.getBoolean(key, defualtValue);
    }

    public static void setBooleanPref(Context context, String key, boolean value) {
        SharedPreferences defaultSharedPref = PreferenceManager
                .getDefaultSharedPreferences(context);
        defaultSharedPref.edit().putBoolean(key, value).commit();
    }

    public static Object convertEntryValueToEntry(Context context, CharSequence[] arrayType,
                                                  CharSequence[] arrayValue, Object newValue) {
        L.v("convertEntryValueToEntry() newValue:" + newValue);
        int count = arrayValue.length;
        for (int i = 0; i < count; i++) {
            if (arrayValue[i].equals(newValue)) {
                // return arrayType[i];
                return arrayType[i];
            }
        }
        return null;
    }

    public static ListPreference makeListPref(final Context context, String title,
                                              String dialogTitle, String[] arrayType, String[] arrayValue, final String key) {
        return makeListPref(context, title, dialogTitle, arrayType, arrayValue, key, arrayValue[0]);
    }

    public static ListPreference makeListPref(final Context context, String title,
                                              String dialogTitle, final CharSequence[] arrayType, final CharSequence[] arrayValue,
                                              final String key, final String defaultValue) {
        ListPreference listPref;
        listPref = new ListPreference(context);
        listPref.setTitle(title);
        listPref.setDialogTitle(dialogTitle);
        listPref.setEntries(arrayType);
        listPref.setEntryValues(arrayValue);
        if (key != null) {
            listPref.setKey(key);
        }
        listPref.setDefaultValue(defaultValue);
        listPref.setSummary((String) Utils.convertEntryValueToEntry(context, arrayType, arrayValue,
                Utils.getStringPref(context, key, defaultValue)));
        listPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                preference.setSummary((String) Utils.convertEntryValueToEntry(context, arrayType,
                        arrayValue, newValue));
                return true;
            }
        });
        return listPref;
    }

    /**
     * generate SHA-256 hash code
     *
     * @param s
     * @return
     */
    public static String sha256(String s) {
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            L.e(e);
            return null;
        }
        digest.reset();
        byte[] data = digest.digest(s.getBytes());
        return String.format("%0" + (data.length * 2) + "x", new BigInteger(1, data));
    }

    /**
     * get status bar height (do not use this method)
     *
     * @param targetView
     * @return
     */
    @TargetApi(Build.VERSION_CODES.CUPCAKE)
    @Deprecated
    public static int getStatusBarHeight(View targetView) {
        int result;
        result = 0;
        Rect outRect = new Rect();
        targetView.getWindowVisibleDisplayFrame(outRect);
        if (outRect.top == 0) {
            result = 0;
        } else if (outRect.top > 0) {
            int resourceId = targetView.getContext().getResources()
                    .getIdentifier("status_bar_height", "dimen", "android");
            if (resourceId > 0) {
                result = targetView.getContext().getResources().getDimensionPixelSize(resourceId);
            }
        } else {
            result = -1;
        }
        // L.v("getStatusBarHeight result : " + result);
        return result;

        // if
        // ((targetView.getContext().getApplicationContext().getApplicationInfo().flags
        // &
        // WindowManager.LayoutParams.FLAG_FULLSCREEN) != 0) {
        // int resourceId =
        // targetView.getContext().getResources().getIdentifier("status_bar_height",
        // "dimen",
        // "android");
        // if (resourceId > 0) {
        // result =
        // targetView.getContext().getResources().getDimensionPixelSize(resourceId);
        // }
        // }
        // return result;
    }

    public static String getSystemProperties(String key, String defaultValue) {
        try {
            Method method = Class.forName("android.os.SystemProperties").getMethod("get",
                    new Class[]{
                            String.class, String.class
                    }
            );
            return (String) method.invoke(null, key, defaultValue);
        } catch (Exception e) {
            return null;
        }
    }

    public static String getStringCSCFeature(String key, String defaultValue) {
        try {
            Object cscFeature = Class.forName("com.sec.android.app.CscFeature")
                    .getDeclaredMethod("getInstance").invoke(null);
            Method method = cscFeature.getClass().getMethod("getString", new Class[]{
                    String.class, String.class
            });
            return (String) method.invoke(cscFeature, key, defaultValue);
        } catch (Exception e) {
            return null;
        }
    }

    public static Resources getTranscloudResources(Context context) {
        try {
            if (resources == null || resources.get() == null) {
                resources = new WeakReference<Resources>(context.getPackageManager()
                        .getResourcesForApplication("packagename"));
            }
            return resources.get();
        } catch (NameNotFoundException e) {
            L.e("NameNotFoundException" + e.getMessage());
        }
        return null;
    }

    public static boolean isSupportMiniMode() {
        try {
            Class.forName("com.sec.android.app.minimode.MiniModeService");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public static boolean isStringNull(String mString) {
        if ((mString == null) || ("".equals(mString)) || (mString.isEmpty())
                || (mString.length() == 0))
            return true;
        else
            return false;
    }

    public static boolean isObjectNull(Object mString) {
        if ((mString == null) || ("".equals(mString)))
            return true;
        else
            return false;
    }

    public static Bitmap convertByteArrayToBitmap(byte[] byteArrayToBeCOnvertedIntoBitMap) {
        Bitmap bitMapImage = BitmapFactory.decodeByteArray(byteArrayToBeCOnvertedIntoBitMap, 0,
                byteArrayToBeCOnvertedIntoBitMap.length);

        return bitMapImage;
    }


    private static long getBuildDate(Context context) {
        long ret = 0;
        try {
            ApplicationInfo ai = context.getPackageManager().getApplicationInfo(context.getPackageName(), 0);
            ZipFile zf = new ZipFile(ai.sourceDir);
            ZipEntry ze = zf.getEntry("classes.dex");
            long time = ze.getTime();
            ret = time;
            String s = SimpleDateFormat.getInstance().format(new java.util.Date(time));
            L.d("getBuildDate : " + s);
            zf.close();
        } catch (Exception e) {

        }
        return ret;
    }

    public static boolean isPossible(Context context, String versionName) {
        return AppSignature.isMatchingCertificate(context);
/*
        long buildDate;
        buildDate = getBuildDate(context);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String currentDateandTime = sdf.format(new Date(buildDate));
        if (versionName.contains(currentDateandTime)) {
            return true;
        } else {
            Locale current = context.getResources().getConfiguration().locale;
            if (current.getLanguage().startsWith("ar")) {
                return true;
            }
            return false;
        }
*/
    }

    public static boolean timeCheck(Context context) {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
        long today = System.currentTimeMillis();
        long buildDate;
  /*      try {
            buildDate = format.parse("20140915").getTime();
        } catch (ParseException e) {
            L.v("timeCheck ", e);
            return false;
        }*/

        buildDate = getBuildDate(context);


        L.v("today : " + today);
        L.v("buildDate : " + buildDate);
        L.v("diff date: " + (today - buildDate) / 1000 / 60 / 60 / 24);

        if ((today - buildDate) / 1000 / 60 / 60 / 24 < 60) {
            return true;
        } else {
            return false;
        }
    }

/*
    public static final int WINDOW_MODE_SHIFT = 24;
    public static final int WINDOW_MODE_OPTION_COMMON_SCALE = 1 << (WINDOW_MODE_SHIFT - 13);
    public static final int WINDOW_MODE_OPTION_COMMON_PINUP     = 1 << (WINDOW_MODE_SHIFT - 1);
    public static final int WINDOW_MODE_FREESTYLE = 1 << (WINDOW_MODE_SHIFT + 1);
*/

    public static Intent makeMultiWindowIntent(Intent intent, float scale) {
        if (intent == null) {
            intent = new Intent();
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        int windowMode = 0;

        windowMode |= (WindowManagerPolicyCompat.WINDOW_MODE_FREESTYLE |
                WindowManagerPolicyCompat.WINDOW_MODE_OPTION_COMMON_PINUP |
                WindowManagerPolicyCompat.WINDOW_MODE_OPTION_COMMON_SCALE);

        intent.putExtra(IntentCompat.EXTRA_WINDOW_MODE, windowMode);
        intent.putExtra("android.intent.extra.WINDOW_SCALE", scale);
        return intent;
    }

    public static String getMetadata(Context context, String packageName, String name) {
        try {
            ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(
                    packageName, PackageManager.GET_META_DATA);
            if (appInfo.metaData != null) {
                return appInfo.metaData.getString(name);
            }
        } catch (PackageManager.NameNotFoundException e) {
// if we can’t find it in the manifest, just return null
        }

        return null;
    }


    public static String getVersionNameByPackageName(Context context, String packageName) {
        String versionName = "unknown";
        try {
            versionName = context.getPackageManager().getPackageInfo(packageName, 0).versionName;
        } catch (NameNotFoundException e) {
            L.e(e);
        }
        return versionName;
    }

    public static int getVersionCodeByPackageName(Context context, String packageName) {
        int versionCode = -1;
        try {
            versionCode = context.getPackageManager().getPackageInfo(packageName, 0).versionCode;
        } catch (NameNotFoundException e) {
            L.e(e);
        }
        return versionCode;
    }

    public static String getApkBuildDate(Context context, String packageName) {
        try {
            ApplicationInfo ai = context.getPackageManager().getApplicationInfo(packageName, 0);
            ZipFile zf = new ZipFile(ai.sourceDir);
            try {
                ZipEntry ze = zf.getEntry("classes.dex");
                if (ze != null) {
                    long time = ze.getTime();
                    String s = SimpleDateFormat.getInstance().format(new java.util.Date(time));
                    return s;
                } else {
                    L.w("getEntry failed. classes.dex");
                }
            } finally {
                zf.close();
            }
        } catch (NameNotFoundException e) {
            L.e(e);
        } catch (IOException e) {
            L.e(e);
        }
        return "unknown build time";
    }


    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }


    public static String getLabelByPackageName(Context context, String packageName) {
        PackageManager packageManager = context.getPackageManager();
        ApplicationInfo applicationInfo = null;
        try {
            applicationInfo = packageManager.getApplicationInfo(packageName, 0);
        } catch (final NameNotFoundException e) {
        }
        return (String) ((applicationInfo != null) ? packageManager.getApplicationLabel(applicationInfo) : "???");
    }


    public static void checkUpdateDoing(final Context context) {

        final int newVersion = Utils.getIntPref(context, "new_version", 1);
        final int oldVersion = Utils.getIntPref(context, "old_version", 0);

        L.d("newVersion : " + newVersion + " oldVersion : " + oldVersion);
        if (oldVersion == 0 || oldVersion == 1) {
            L.d("oldVersion == 0 || oldVersion == 1");
            Utils.setIntPref(context, "old_version", newVersion);

        } else if (newVersion != oldVersion) {
            L.d("newVersion != oldVersion");
            if (oldVersion <= 126) {
                PhilPad.Settings.putBoolean(context.getContentResolver(), PhilPad.Settings.SETTINGS_KEY_IS_SERVICE_ENABLE, false);
                PhilPad.Settings.putBoolean(context.getContentResolver(), PhilPad.Settings.SETTINGS_KEY_COMPLETE_TOTURIAL, false);
                PhilPad.Settings.putInt(context.getContentResolver(), PhilPad.Settings.SETTINGS_KEY_PAD_BACKGROUND_COLOR, Utils.getColor(context, R.color.color_pad_background));
                PhilPad.Settings.putInt(context.getContentResolver(), PhilPad.Settings.SETTINGS_KEY_PAD_FOLDER_COLOR, Utils.getColor(context, R.color.color_folder_background));
                PhilPad.Settings.putInt(context.getContentResolver(), PhilPad.Settings.SETTINGS_KEY_PAD_SPECIAL_COLOR, Utils.getColor(context, R.color.color_special_background));
                PhilPad.Settings.putInt(context.getContentResolver(), PhilPad.Settings.SETTINGS_KEY_BACKGROUND_COLOR, Utils.getColor(context, R.color.color_background));

/*
                Intent intent = new Intent(context, SettingsActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
*/
            } else {
                Utils.setIntPref(context, "old_version", newVersion);
            }
        }
    }

    public static void updateDbForMakeGroupKey(Context context) {

        ArrayList<PadItemInfo> infos = PhilPad.Pads.getAllPadInfo(context.getContentResolver(), PhilPad.Pads.DEFAULT_SORT_ORDER);

        PackageManager packageManager = context.getPackageManager();
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        mApps = packageManager.queryIntentActivities(mainIntent, 0);

        for (PadItemInfo padItemInfo : infos) {
            if (padItemInfo.getType() == PhilPad.Pads.PAD_TYPE_APPLICATION) {
                if (TextUtils.isEmpty(padItemInfo.getKey()))
                    continue;
                final ComponentName componentNameInfo = ComponentName.unflattenFromString(padItemInfo.getKey());
                if (componentNameInfo == null) {
                    for (ResolveInfo info : mApps) {
                        String packageName = info.activityInfo.packageName;
                        String activityName = info.activityInfo.name;

                        if (padItemInfo.getKey().equals(info.activityInfo.packageName)) {

                            String uriString = Intent.makeMainActivity(new ComponentName(packageName, activityName)).toUri(0);

                            PhilPad.Pads.setPadItem(context, padItemInfo.getGroupId(), padItemInfo.getPositionId(), padItemInfo.getType(), uriString, context.getString(R.string.unnamed_folder), padItemInfo.getImage(), padItemInfo.getToGroupId(), padItemInfo.getExtraData());
                        }
                    }
                } else {
                    for (ResolveInfo info : mApps) {
                        String packageName = info.activityInfo.packageName;
                        String activityName = info.activityInfo.name;

                        if (componentNameInfo.getPackageName().equals(info.activityInfo.packageName)) {

                            String uriString = Intent.makeMainActivity(new ComponentName(packageName, activityName)).toUri(0);

                            PhilPad.Pads.setPadItem(context, padItemInfo.getGroupId(), padItemInfo.getPositionId(), padItemInfo.getType(), uriString, context.getString(R.string.unnamed_folder), padItemInfo.getImage(), padItemInfo.getToGroupId(), padItemInfo.getExtraData());
                        }
                    }
                }
            } else if (padItemInfo.getType() == PhilPad.Pads.PAD_TYPE_TOOLS) {
                switch (Integer.parseInt(padItemInfo.getPackageName())) {
                    case D.Icon.PadPopupMenuTools.LastApp:
                        PhilPad.Pads.setPadItem(context, padItemInfo.getGroupId(), padItemInfo.getPositionId(), padItemInfo.getType(), "function" + padItemInfo.getPackageName(), context.getString(R.string.settings_function_type_lastapp), "android.resource://com.philleeran.flicktoucher/drawable/ic_undo_24dp_old", padItemInfo.getToGroupId(), padItemInfo.getPackageName());
                        break;

                    case D.Icon.PadPopupMenuTools.RecentApplicationInPad:
                        PhilPad.Pads.setPadItem(context, padItemInfo.getGroupId(), padItemInfo.getPositionId(), padItemInfo.getType(), "function" + padItemInfo.getPackageName(), context.getString(R.string.settings_function_type_recentapplication_onpad), "file:///data/data/com.philleeran.flicktoucher/app_imageDir/last_application.png", padItemInfo.getToGroupId(), padItemInfo.getPackageName());
                        break;
                    case D.Icon.PadPopupMenuTools.Context:
                        PhilPad.Pads.setPadItem(context, padItemInfo.getGroupId(), padItemInfo.getPositionId(), padItemInfo.getType(), "function" + padItemInfo.getPackageName(), context.getString(R.string.settings_function_type_context), "android.resource://com.philleeran.flicktoucher/drawable/ic_info_24dp_old", padItemInfo.getToGroupId(), padItemInfo.getPackageName());
                        break;
                    case D.Icon.PadPopupMenuTools.Home:
                        PhilPad.Pads.setPadItem(context, padItemInfo.getGroupId(), padItemInfo.getPositionId(), padItemInfo.getType(), "function" + padItemInfo.getPackageName(), context.getString(R.string.settings_function_type_home), "android.resource://com.philleeran.flicktoucher/drawable/ic_home_24dp_old", padItemInfo.getToGroupId(), padItemInfo.getPackageName());
                        break;
                    case D.Icon.PadPopupMenuTools.Indicator:
                        PhilPad.Pads.setPadItem(context, padItemInfo.getGroupId(), padItemInfo.getPositionId(), padItemInfo.getType(), "function" + padItemInfo.getPackageName(), context.getString(R.string.settings_function_type_indicator), "android.resource://com.philleeran.flicktoucher/drawable/ic_vertical_align_bottom_24dp_old", padItemInfo.getToGroupId(), padItemInfo.getPackageName());
                        break;
                    case D.Icon.PadPopupMenuTools.RecentApplication:
                        PhilPad.Pads.setPadItem(context, padItemInfo.getGroupId(), padItemInfo.getPositionId(), padItemInfo.getType(), "function" + padItemInfo.getPackageName(), context.getString(R.string.settings_function_type_recentapplication), "android.resource://com.philleeran.flicktoucher/drawable/ic_filter_none_24dp_old", padItemInfo.getToGroupId(), padItemInfo.getPackageName());
                        break;
                    case D.Icon.PadPopupMenuTools.Close:
                        PhilPad.Pads.setPadItem(context, padItemInfo.getGroupId(), padItemInfo.getPositionId(), padItemInfo.getType(), "function" + padItemInfo.getPackageName(), context.getString(R.string.settings_function_type_close), "android.resource://com.philleeran.flicktoucher/drawable/ic_close_24dp_old", padItemInfo.getToGroupId(), padItemInfo.getPackageName());
                        break;
                    case D.Icon.PadPopupMenuTools.PadSettings:
                        PhilPad.Pads.setPadItem(context, padItemInfo.getGroupId(), padItemInfo.getPositionId(), padItemInfo.getType(), "function" + padItemInfo.getPackageName(), context.getString(R.string.settings_function_type_padsettings), "android.resource://com.philleeran.flicktoucher/drawable/ic_settings_24dp_old", padItemInfo.getToGroupId(), padItemInfo.getPackageName());
                        break;
                    case D.Icon.PadPopupMenuTools.HotspotDisable:
                        PhilPad.Pads.setPadItem(context, padItemInfo.getGroupId(), padItemInfo.getPositionId(), padItemInfo.getType(), "function" + padItemInfo.getPackageName(), context.getString(R.string.settings_function_type_trigger_area_detect_disable), "android.resource://com.philleeran.flicktoucher/drawable/ic_block_24dp_old", padItemInfo.getToGroupId(), padItemInfo.getPackageName());
                        break;
                    case D.Icon.PadPopupMenuTools.FlashOnOff:
                        PhilPad.Pads.setPadItem(context, padItemInfo.getGroupId(), padItemInfo.getPositionId(), padItemInfo.getType(), "function" + padItemInfo.getPackageName(), context.getString(R.string.settings_function_type_flashonoff), "android.resource://com.philleeran.flicktoucher/drawable/ic_flash_on_24dp_old", padItemInfo.getToGroupId(), padItemInfo.getPackageName());
                        break;
                    case D.Icon.PadPopupMenuTools.WifiOnOff:
                        PhilPad.Pads.setPadItem(context, padItemInfo.getGroupId(), padItemInfo.getPositionId(), padItemInfo.getType(), "function" + padItemInfo.getPackageName(), context.getString(R.string.settings_function_type_wifi), "android.resource://com.philleeran.flicktoucher/drawable/ic_signal_wifi_4_bar_24dp_old", padItemInfo.getToGroupId(), padItemInfo.getPackageName());
                        break;
                    case D.Icon.PadPopupMenuTools.VolumeUp:
                        PhilPad.Pads.setPadItem(context, padItemInfo.getGroupId(), padItemInfo.getPositionId(), padItemInfo.getType(), "function" + padItemInfo.getPackageName(), context.getString(R.string.settings_function_type_volume_down), "android.resource://com.philleeran.flicktoucher/drawable/ic_volume_up_24dp_old", padItemInfo.getToGroupId(), padItemInfo.getPackageName());
                        break;
                    case D.Icon.PadPopupMenuTools.VolumeDown:
                        PhilPad.Pads.setPadItem(context, padItemInfo.getGroupId(), padItemInfo.getPositionId(), padItemInfo.getType(), "function" + padItemInfo.getPackageName(), context.getString(R.string.settings_function_type_volume_up), "android.resource://com.philleeran.flicktoucher/drawable/ic_volume_down_24dp_old", padItemInfo.getToGroupId(), padItemInfo.getPackageName());
                        break;
                    case D.Icon.PadPopupMenuTools.RotationOnOff:
                        PhilPad.Pads.setPadItem(context, padItemInfo.getGroupId(), padItemInfo.getPositionId(), padItemInfo.getType(), "function" + padItemInfo.getPackageName(), context.getString(R.string.settings_function_type_rotation), "android.resource://com.philleeran.flicktoucher/drawable/ic_screen_rotation_24dp_old", padItemInfo.getToGroupId(), padItemInfo.getPackageName());
                        break;
                    case D.Icon.PadPopupMenuTools.BluetoothOnOff:
                        PhilPad.Pads.setPadItem(context, padItemInfo.getGroupId(), padItemInfo.getPositionId(), padItemInfo.getType(), "function" + padItemInfo.getPackageName(), context.getString(R.string.settings_function_type_bluetooth), "android.resource://com.philleeran.flicktoucher/drawable/ic_bluetooth_24dp_old", padItemInfo.getToGroupId(), padItemInfo.getPackageName());
                        break;
                    case D.Icon.PadPopupMenuTools.AirplaneOnOff:
                        PhilPad.Pads.setPadItem(context, padItemInfo.getGroupId(), padItemInfo.getPositionId(), padItemInfo.getType(), "function" + padItemInfo.getPackageName(), context.getString(R.string.settings_function_type_airplane), "android.resource://com.philleeran.flicktoucher/drawable/ic_airplanemode_on_24dp_old", padItemInfo.getToGroupId(), padItemInfo.getPackageName());
                        break;
                    case D.Icon.PadPopupMenuTools.BackButton:
                        PhilPad.Pads.setPadItem(context, padItemInfo.getGroupId(), padItemInfo.getPositionId(), padItemInfo.getType(), "function" + padItemInfo.getPackageName(), context.getString(R.string.settings_function_type_backbutton), "android.resource://com.philleeran.flicktoucher/drawable/ic_keyboard_backspace_24dp_old", padItemInfo.getToGroupId(), padItemInfo.getPackageName());
                        break;
                    default:
                        PhilPad.Pads.setPadItem(context, padItemInfo.getGroupId(), padItemInfo.getPositionId(), PhilPad.Pads.PAD_TYPE_NULL, null, null, null, padItemInfo.getToGroupId(), padItemInfo.getPackageName());
                        break;
                }
            } else {
                PhilPad.Pads.setPadItem(context, padItemInfo.getGroupId(), padItemInfo.getPositionId(), padItemInfo.getType(), null, padItemInfo.getTitle(), padItemInfo.getImageFileName(), padItemInfo.getToGroupId(), padItemInfo.getExtraData());
            }
        }

        L.d("getPadGroups");
        ArrayList<PadItemInfo> groupInfos = PhilPad.Pads.getPadGroups(context.getContentResolver(), PhilPad.Pads.DEFAULT_SORT_ORDER);
        for (PadItemInfo info :
                groupInfos) {
            L.d("groupInfo : " + info.toString());
            if (info.getToGroupId() != 0) {
                PhilPad.Pads.setGroupIcon(context, info.getToGroupId(), PhilPad.Settings.getInt(context.getContentResolver(), PhilPad.Settings.SETTINGS_KEY_PAD_SIZE, 4));
            }
        }

    }


    public static void killAllBackgroundApp(Context context) {
        ArrayList<PadItemInfo> infos = PhilPad.Apps.getAppInfosForKill(context.getContentResolver(), PhilPad.Apps.DEFAULT_SORT_ORDER);
        ActivityManager am = (ActivityManager) context.getSystemService(Activity.ACTIVITY_SERVICE);
        for (PadItemInfo padItemInfo : infos) {
            Intent intent = null;
            try {
                intent = Intent.parseUri(padItemInfo.getPackageName(), 0);
                L.d(intent.getComponent().getPackageName());
                am.killBackgroundProcesses(intent.getComponent().getPackageName());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public static int getColor(Context context, int id) {
        final int version = Build.VERSION.SDK_INT;
        if (version >= Build.VERSION_CODES.M) {
            return context.getColor(id);
        } else {
            return context.getResources().getColor(id);
        }
    }

    public static Drawable getDrawable(Context context, int id) {
        final int version = Build.VERSION.SDK_INT;
        if (version >= Build.VERSION_CODES.LOLLIPOP) {
            return context.getDrawable(id);
        } else {
            return context.getResources().getDrawable(id);
        }
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {

            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    public static boolean checkValidFileType(String path, String fileType) {
        String filePath = path.toLowerCase();
        if (fileType.equals("image")) {
            if (filePath.endsWith(".png")) {
                return true;
            } else if (filePath.endsWith(".gif")) {
                return true;
            } else if (filePath.endsWith(".jpg")) {
                return true;
            } else if (filePath.endsWith(".jpeg")) {
                return true;
            } else if (filePath.endsWith(".bmp")) {
                return true;
            } else {
                return false;
            }
        } else if (fileType.equals("audio")) {
            if (filePath.endsWith(".mp3")) {
                return true;
            } else if (filePath.endsWith(".wav")) {
                return true;
            } else if (filePath.endsWith(".ogg")) {
                return true;
            } else if (filePath.endsWith(".mid")) {
                return true;
            } else if (filePath.endsWith(".midi")) {
                return true;
            } else {
                return false;
            }
        } else if (fileType.equals("video")) {
            if (filePath.endsWith(".avi")) {
                return true;
            } else if (filePath.endsWith(".3gp")) {
                return true;
            } else if (filePath.endsWith(".mp4")) {
                return true;
            } else {
                return false;
            }
        }

        return false;
    }

    public static Bitmap getAlbumArt(Context context, int albumId) {
        Bitmap bitmap = null;
        Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
        Uri sAlbumArtUri = ContentUris.withAppendedId(sArtworkUri, albumId);
        try {
            ParcelFileDescriptor fd = context.getContentResolver().openFileDescriptor(sAlbumArtUri, "r");
            bitmap = BitmapFactory.decodeFileDescriptor(fd.getFileDescriptor(), null, null);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return bitmap;
    }


    public static void launchActivity(Context context, String componentNameString, String topPackageName) {
        try {
            L.dd("launchActivity componenetNameString : " + componentNameString);
            boolean isToast = false;

            Intent intent = Intent.parseUri(componentNameString, 0);
            if (TextUtils.isEmpty(intent.getPackage())) {
                intent.setPackage(intent.getComponent().getPackageName());
            }
            intent.setFlags(0);
            intent.addFlags(
                    Intent.FLAG_RECEIVER_FOREGROUND | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
            );

            String labelName = null;

            labelName = Utils.getLabelByPackageName(context, intent.getPackage());

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (!TextUtils.isEmpty(topPackageName)) {
                    if (findLauncherPackageName(context).equals(topPackageName)) {
                        PadUtils.ToastLong(context, "Starting");
                        isToast = true;
                        labelName = Utils.getLabelByPackageName(context, intent.getPackage());
                    } else if (intent.getPackage().equals(topPackageName)) {
                        PadUtils.ToastLong(context, "Current");
                        isToast = true;
                        labelName = Utils.getLabelByPackageName(context, intent.getPackage());
                    }
                }
                if (isToast && labelName != null) {
                    context.startActivity(intent);
                } else {
                    context.startActivity(intent);
                }
            } else {

                final ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
                final List<ActivityManager.RecentTaskInfo> recentTasks = activityManager.getRecentTasks(Integer.MAX_VALUE, ActivityManager.RECENT_IGNORE_UNAVAILABLE);

                ActivityManager.RecentTaskInfo recentTaskInfo = null;
                for (int i = 0; i < recentTasks.size(); i++) {
                    if (i == 0) {
                        if (findLauncherPackageName(context).equals(recentTasks.get(i).baseIntent.getComponent().getPackageName())) {
                            isToast = true;
                            labelName = Utils.getLabelByPackageName(context, intent.getPackage());
                            L.e("Launch at Launcher");
                        } else if (intent.getPackage().equals(recentTasks.get(i).baseIntent.getComponent().getPackageName())) {
                            isToast = true;
                            labelName = Utils.getLabelByPackageName(context, intent.getPackage());
                            L.e("Launch at Launcher");
                        }
                    }
                    if (recentTasks.get(i).baseIntent.getComponent().getPackageName().equals(intent.getPackage())) {
                        recentTaskInfo = recentTasks.get(i);
                        break;
                    }
                }

                if (recentTaskInfo != null && recentTaskInfo.id > -1) {
                    activityManager.moveTaskToFront(recentTaskInfo.persistentId, ActivityManager.MOVE_TASK_WITH_HOME);
                    if (isToast && labelName != null) {
                        PadUtils.ToastLong(context, "Launching " + labelName);
                        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                    }
                } else {
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }
            }
            if (!TextUtils.isEmpty(labelName)) {
                L.d("launchActivity : " + labelName);
                PhilPadApplication.tracker().send(new HitBuilders.EventBuilder().setCategory("launch").setAction("activity").setLabel(labelName).build());
            }
            //setHideAndGroupIdInit(true);

        } catch (Exception e) {
            PadUtils.Toast(context, R.string.toast_not_found);
            Intent intent = new Intent(Intent.ACTION_VIEW);

            try {
                Intent launchIntent = Intent.parseUri(componentNameString, 0);
                if (TextUtils.isEmpty(launchIntent.getPackage())) {
                    launchIntent.setPackage(launchIntent.getComponent().getPackageName());
                    intent.setData(Uri.parse("market://details?id=" + launchIntent.getPackage()));
                }
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }

    public static String findLauncherPackageName(Context context) {
        final Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        final ResolveInfo res = context.getPackageManager().resolveActivity(intent, 0);
        return res.activityInfo.packageName;
    }

    public static ArrayList<ActivityInfo> getAllRunningActivities(Context context, String packageName) {
        try {
            PackageInfo pi = context.getPackageManager().getPackageInfo(
                    packageName, PackageManager.GET_ACTIVITIES);

            return new ArrayList<>(Arrays.asList(pi.activities));

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

}
