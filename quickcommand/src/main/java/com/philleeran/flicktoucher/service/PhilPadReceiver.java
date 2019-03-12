
package com.philleeran.flicktoucher.service;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import com.philleeran.flicktoucher.db.PhilPad;
import com.philleeran.flicktoucher.utils.D;
import com.philleeran.flicktoucher.utils.L;

import java.io.File;
import java.io.FileOutputStream;

public class PhilPadReceiver extends BroadcastReceiver {
    private final String BOOT_ACTION = "android.intent.action.BOOT_COMPLETED";

    private final String FIRST_ACTION = "android.intent.action.PACKAGE_FIRST_LAUNCH";

    private final String REPLACED_ACTION = "android.intent.action.PACKAGE_REPLACED";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(BOOT_ACTION)) {

            if (PhilPad.Settings.getBoolean(context.getContentResolver(), PhilPad.Settings.SETTINGS_KEY_IS_SERVICE_ENABLE, true)) {
                ComponentName component = new ComponentName("com.philleeran.flicktoucher", "com.philleeran.flicktoucher.service.PhilPadService");
                Intent i = new Intent().setComponent(component);
                context.startService(i);
                L.d("PhilPadServiceStart");
            }
        } else if (PhilPad.Settings.getBoolean(context.getContentResolver(), PhilPad.Settings.SETTINGS_KEY_COMPLETE_TOTURIAL, false) == true) {
            final String action = intent.getAction();
            final String packageName = intent.getData().getSchemeSpecificPart();
            if (Intent.ACTION_PACKAGE_ADDED.equals(action)) {
                PackageManager packageManager = context.getPackageManager();
                Intent intentToResolve = packageManager.getLaunchIntentForPackage(packageName);
                if (intentToResolve == null)
                    return;
                ResolveInfo info = packageManager.resolveActivity(intentToResolve, 0);

                ContentValues cv = new ContentValues();
                String uriString = Intent.makeMainActivity(new ComponentName(info.activityInfo.packageName, info.activityInfo.name)).toUri(0);
                cv.put(PhilPad.Apps.COLUMN_NAME_COMPONENT_NAME, uriString);
                L.d("ACTION_PACKAGE_ADDED : " + uriString);
                cv.put(PhilPad.Apps.COLUMN_NAME_TITLE, (String) info.loadLabel(packageManager));

                // convert drawable to bitmap
                Drawable d = info.loadIcon(packageManager);
                int h = d.getIntrinsicHeight();
                int w = d.getIntrinsicWidth();
                Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                d.setBounds(0, 0, w, h);
                d.draw(canvas);

                ContextWrapper cw = new ContextWrapper(context.getApplicationContext());
                File directory = cw.getDir(D.DIR_PATH_DEFAULT, Context.MODE_PRIVATE);
                File mypath = new File(directory, info.activityInfo.packageName + ".png");
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(mypath);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                    fos.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                cv.put(PhilPad.Apps.COLUMN_NAME_IMAGEFILE, "file://" + mypath.getPath());
                int row = context.getContentResolver().update(PhilPad.Apps.CONTENT_URI, cv, PhilPad.Apps.COLUMN_NAME_COMPONENT_NAME + "=?", new String[]{
                        uriString
                });
                // context.getContentResolver().insert(PhilPad.Apps.CONTENT_URI,
                // cv);
                if (row <= 0) {
                    context.getContentResolver().insert(PhilPad.Apps.CONTENT_URI, cv);
                }
            } else if (Intent.ACTION_PACKAGE_REMOVED.equals(action)) {
                L.d("ACTION_PACKAGE_REMOVED : " + packageName);

                int row = context.getContentResolver().delete(PhilPad.Apps.CONTENT_URI, PhilPad.Apps.COLUMN_NAME_COMPONENT_NAME + " like ?", new String[]{
                        "%" + packageName + "%"
                });
                if (row <= 0) {
                    L.e("ACTION_PACKAGE_REMOVED : " + packageName);
                }

            } else if (Intent.ACTION_PACKAGE_CHANGED.equals(action)) {
                PackageManager packageManager = context.getPackageManager();

                Intent intentToResolve = packageManager.getLaunchIntentForPackage(packageName);
                if (intentToResolve == null)
                    return;
                ResolveInfo info = packageManager.resolveActivity(intentToResolve, 0);
                ContentValues cv = new ContentValues();
                String uriString = Intent.makeMainActivity(new ComponentName(info.activityInfo.packageName, info.activityInfo.name)).toUri(0);
                L.d("ACTION_PACKAGE_CHANGED : " + uriString);
                cv.put(PhilPad.Apps.COLUMN_NAME_COMPONENT_NAME, uriString);
                cv.put(PhilPad.Apps.COLUMN_NAME_TITLE, (String) info.loadLabel(packageManager));

                // convert drawable to bitmap
                Drawable d = info.loadIcon(packageManager);
                int h = d.getIntrinsicHeight();
                int w = d.getIntrinsicWidth();
                Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                d.setBounds(0, 0, w, h);
                d.draw(canvas);

                ContextWrapper cw = new ContextWrapper(context.getApplicationContext());
                File directory = cw.getDir(D.DIR_PATH_DEFAULT, Context.MODE_PRIVATE);
                File mypath = new File(directory, info.activityInfo.packageName + ".png");
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(mypath);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                    fos.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                cv.put(PhilPad.Apps.COLUMN_NAME_IMAGEFILE, "file://" + mypath.getPath());
                int row = context.getContentResolver().update(PhilPad.Apps.CONTENT_URI, cv, PhilPad.Apps.COLUMN_NAME_COMPONENT_NAME + "=?", new String[]{
                        uriString
                });
                if (row <= 0) {
                    context.getContentResolver().insert(PhilPad.Apps.CONTENT_URI, cv);
                }
                if (packageName.equals(D.PACKAGE_NAME)) {
                    if (PhilPad.Settings.getBoolean(context.getContentResolver(), PhilPad.Settings.SETTINGS_KEY_IS_SERVICE_ENABLE, true)) {
                        ComponentName component = new ComponentName("com.philleeran.flicktoucher", "com.philleeran.flicktoucher.service.PhilPadService");
                        Intent i = new Intent().setComponent(component);
                        context.startService(i);
                        L.d("PhilPadServiceStart");
                    }
                }

            } else if (Intent.ACTION_PACKAGE_REPLACED.equals(action)) {
                L.d("ACTION_PACKAGE_REPLACED : " + packageName);
                PackageManager packageManager = context.getPackageManager();

                Intent intentToResolve = packageManager.getLaunchIntentForPackage(packageName);
                if (intentToResolve == null)
                    return;
                ResolveInfo info = packageManager.resolveActivity(intentToResolve, 0);
                ContentValues cv = new ContentValues();
                String uriString = Intent.makeMainActivity(new ComponentName(info.activityInfo.packageName, info.activityInfo.name)).toUri(0);
                cv.put(PhilPad.Apps.COLUMN_NAME_COMPONENT_NAME, uriString);
                cv.put(PhilPad.Apps.COLUMN_NAME_TITLE, (String) info.loadLabel(packageManager));

                // convert drawable to bitmap
                Drawable d = info.loadIcon(packageManager);
                int h = d.getIntrinsicHeight();
                int w = d.getIntrinsicWidth();
                Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                d.setBounds(0, 0, w, h);
                d.draw(canvas);

                ContextWrapper cw = new ContextWrapper(context.getApplicationContext());
                File directory = cw.getDir(D.DIR_PATH_DEFAULT, Context.MODE_PRIVATE);
                File mypath = new File(directory, info.activityInfo.packageName + ".png");
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(mypath);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                    fos.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                cv.put(PhilPad.Apps.COLUMN_NAME_IMAGEFILE, "file://" + mypath.getPath());
                int row = context.getContentResolver().update(PhilPad.Apps.CONTENT_URI, cv, PhilPad.Apps.COLUMN_NAME_COMPONENT_NAME + "=?", new String[]{
                        uriString
                });
                if (row <= 0) {
                    context.getContentResolver().insert(PhilPad.Apps.CONTENT_URI, cv);
                }
                if (packageName.equals(D.PACKAGE_NAME)) {
                    if (PhilPad.Settings.getBoolean(context.getContentResolver(), PhilPad.Settings.SETTINGS_KEY_IS_SERVICE_ENABLE, true)) {
                        ComponentName component = new ComponentName("com.philleeran.flicktoucher", "com.philleeran.flicktoucher.service.PhilPadService");
                        Intent i = new Intent().setComponent(component);
                        context.startService(i);
                        L.d("PhilPadServiceStart");
                    }
                }
            }
        } else {
            final String action = intent.getAction();
            final String packageName = intent.getData().getSchemeSpecificPart();
            if (Intent.ACTION_PACKAGE_ADDED.equals(action)) {
                L.d("ACTION_PACKAGE_ADDED : " + packageName);
                if (packageName.equals(D.PACKAGE_NAME)) {
                    if (PhilPad.Settings.getBoolean(context.getContentResolver(), PhilPad.Settings.SETTINGS_KEY_IS_SERVICE_ENABLE, true)) {
                        ComponentName component = new ComponentName("com.philleeran.flicktoucher", "com.philleeran.flicktoucher.service.PhilPadService");
                        Intent i = new Intent().setComponent(component);
                        context.startService(i);
                        L.d("PhilPadServiceStart");
                    }
                }
            } else if (Intent.ACTION_PACKAGE_CHANGED.equals(action)) {
                if (packageName.equals(D.PACKAGE_NAME)) {
                    if (PhilPad.Settings.getBoolean(context.getContentResolver(), PhilPad.Settings.SETTINGS_KEY_IS_SERVICE_ENABLE, true)) {
                        ComponentName component = new ComponentName("com.philleeran.flicktoucher", "com.philleeran.flicktoucher.service.PhilPadService");
                        Intent i = new Intent().setComponent(component);
                        context.startService(i);
                        L.d("PhilPadServiceStart");
                    }
                }
            } else if (Intent.ACTION_PACKAGE_REPLACED.equals(action)) {
                L.d("ACTION_PACKAGE_REPLACED : " + packageName);
                if (packageName.equals(D.PACKAGE_NAME)) {
                    if (PhilPad.Settings.getBoolean(context.getContentResolver(), PhilPad.Settings.SETTINGS_KEY_IS_SERVICE_ENABLE, false)) {
                        ComponentName component = new ComponentName("com.philleeran.flicktoucher", "com.philleeran.flicktoucher.service.PhilPadService");
                        Intent i = new Intent().setComponent(component);
                        context.startService(i);
                        L.d("PhilPadServiceStart");
                    }
                }
            }
        }
    }
}
