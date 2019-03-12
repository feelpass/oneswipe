package com.philleeran.flicktoucher.view.select.activity.fetcher;


import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.philleeran.flicktoucher.BuildConfig;
import com.philleeran.flicktoucher.db.PadItemInfo;
import com.philleeran.flicktoucher.db.PhilPad;
import com.philleeran.flicktoucher.utils.L;
import com.philleeran.flicktoucher.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ActivityListFetcher {
    private static final Uri EMPTY_PHOTO_URI = new Uri.Builder()
            .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
            .authority(BuildConfig.APPLICATION_ID)
            .appendPath("drawable")
            .appendPath("ic_person_24dp_old")
            .build();

    public static void fetch(final Context context, final String packageName, @NonNull final ActivityItemFetcherCallback callback) {

        new AsyncTask<Void, Void, List<ActivityInfo>>() {

            @Override
            protected List<ActivityInfo> doInBackground(Void... params) {


                return Utils.getAllRunningActivities(context, packageName);

            }

            @Override
            protected void onPostExecute(List<ActivityInfo> launchItemList) {
                L.d("feelpass");
                callback.onResult(launchItemList);
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
}
