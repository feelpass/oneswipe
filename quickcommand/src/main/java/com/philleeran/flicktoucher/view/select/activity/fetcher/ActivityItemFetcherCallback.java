package com.philleeran.flicktoucher.view.select.activity.fetcher;

import android.content.pm.ActivityInfo;
import android.support.annotation.NonNull;

import java.util.List;

public interface ActivityItemFetcherCallback {
    void onResult(@NonNull List<ActivityInfo> activityInfos);
}
