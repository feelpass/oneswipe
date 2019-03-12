package com.philleeran.flicktoucher.view.pad.command;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.provider.Settings;

import com.philleeran.flicktoucher.service.PhilPadAccessibilityService;
import com.philleeran.flicktoucher.utils.AccessibilityServiceUtil;

public class BackButton implements Command {
    private Context mContext;

    public BackButton(Context context) {
        mContext = context;
    }

    @Override
    public void execute() {

        boolean isSet = AccessibilityServiceUtil.isAccessibilityServiceOn(mContext, "com.philleeran.flicktoucher", "com.philleeran.flicktoucher.service.PhilPadAccessibilityService");
        if (isSet) {

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(mContext, PhilPadAccessibilityService.class);
                    intent.putExtra("type", AccessibilityService.GLOBAL_ACTION_BACK);
                    mContext.startService(intent);
                }
            }, 400);
        } else {
            Intent accessibilityServiceIntent
                    = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            accessibilityServiceIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(accessibilityServiceIntent);
        }

    }

}
