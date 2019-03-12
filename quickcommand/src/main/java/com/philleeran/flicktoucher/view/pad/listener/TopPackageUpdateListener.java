package com.philleeran.flicktoucher.view.pad.listener;

import android.graphics.Bitmap;

/**
 * Created by young on 2017-03-10.
 */

public interface TopPackageUpdateListener {
    void onTopPackageUpdated(String packageName);
    void onRecentAppBitmapChanged(Bitmap bitmap);
}
