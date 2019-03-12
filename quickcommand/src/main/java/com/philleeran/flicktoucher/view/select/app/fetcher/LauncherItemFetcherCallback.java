package com.philleeran.flicktoucher.view.select.app.fetcher;

import android.support.annotation.NonNull;

import com.philleeran.flicktoucher.db.PadItemInfo;

import java.util.List;

public interface LauncherItemFetcherCallback {
    void onResult(@NonNull List<PadItemInfo> launchItemList);
}
