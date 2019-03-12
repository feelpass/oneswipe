
package com.philleeran.flicktoucher;


import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.philleeran.flicktoucher.utils.L;
import com.philleeran.flicktoucher.utils.Utils;

public class PhilPadApplication extends Application {

    public static final String PROPERTY_ID = "UA-59980839-1";

    public static GoogleAnalytics analytics;
    public static Tracker tracker;
    public static Tracker tracker() {
        return tracker;
    }
    @Override
    public void onCreate() {
        
        super.onCreate();

        final PackageManager pm = getApplicationContext().getPackageManager();
        PackageInfo pi = null;
        try {
            pi = pm.getPackageInfo(getApplicationContext().getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        Utils.setIntPref(getApplicationContext(), "new_version", pi.versionCode);

        Utils.checkUpdateDoing(this);


        L.v("onCreate() USE_ANALYTICS=true");
        analytics = GoogleAnalytics.getInstance(this);
        analytics.setLocalDispatchPeriod(1800);

        tracker = analytics.newTracker(PROPERTY_ID); // Replace with actual tracker/property Id
        tracker.enableExceptionReporting(true);
        tracker.enableAdvertisingIdCollection(false);
        tracker.enableAutoActivityTracking(true);
    }

}
