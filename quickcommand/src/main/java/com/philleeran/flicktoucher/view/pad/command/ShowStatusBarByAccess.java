package com.philleeran.flicktoucher.view.pad.command;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;

import com.philleeran.flicktoucher.service.PhilPadAccessibilityService;
import com.philleeran.flicktoucher.utils.AccessibilityServiceUtil;

public class ShowStatusBarByAccess implements Command {
    private Context mContext;

    public ShowStatusBarByAccess(Context context) {
        mContext = context;
    }

    @Override
    public void execute() {
        boolean isSet = AccessibilityServiceUtil.isAccessibilityServiceOn(mContext, "com.philleeran.flicktoucher", "com.philleeran.flicktoucher.service.PhilPadAccessibilityService");
        if (isSet) {
            Intent intent = new Intent(mContext, PhilPadAccessibilityService.class);
            intent.putExtra("type", AccessibilityService.GLOBAL_ACTION_RECENTS);
            mContext.startService(intent);
        } else {
            Intent accessibilityServiceIntent
                    = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            accessibilityServiceIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(accessibilityServiceIntent);
        }

    }
}
