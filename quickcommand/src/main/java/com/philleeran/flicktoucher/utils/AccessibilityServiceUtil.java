package com.philleeran.flicktoucher.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.WindowManager;

import com.philleeran.flicktoucher.PadUtils;
import com.philleeran.flicktoucher.R;

import java.util.ArrayList;

public class AccessibilityServiceUtil {

    public static ArrayList<String> getAllAccessibilityServices(Context context) {
        TextUtils.SimpleStringSplitter colonSplitter = new TextUtils.SimpleStringSplitter(':');
        ArrayList<String> allAccessibilityServices = new ArrayList<String>();

        String settingValue = Settings.Secure.getString(
                context.getApplicationContext().getContentResolver(),
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);

        if (settingValue != null) {
            colonSplitter.setString(settingValue);
            while (colonSplitter.hasNext()) {
                String accessabilityService = colonSplitter.next();
                allAccessibilityServices.add(accessabilityService);
            }
        }
        return allAccessibilityServices;
    }

    public static boolean isAccessibilityServiceOn(Context context, String packageName, String className) {
        ArrayList<String> allAccessibilityServices = getAllAccessibilityServices(context);
        StringBuffer concat = new StringBuffer();
        concat.append(packageName);
        concat.append('/');
        concat.append(className);
        boolean ret = allAccessibilityServices.contains(concat.toString());
        if (ret == false) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(R.string.dialog_howtouse_title).setMessage(R.string.toast_accessibility_message).setPositiveButton(R.string.dialog_yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            }).create();
            AlertDialog alert = builder.create();
            alert.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
            alert.show();



        }
        return ret;
    }
}
