package com.philleeran.flicktoucher.view.select.app.fetcher;


import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import com.philleeran.flicktoucher.BuildConfig;
import com.philleeran.flicktoucher.db.PadItemInfo;
import com.philleeran.flicktoucher.db.PhilPad;
import com.philleeran.flicktoucher.utils.L;

import java.util.ArrayList;
import java.util.List;

public final class ShortcutListFetcher {
    private static final Uri EMPTY_PHOTO_URI = new Uri.Builder()
            .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
            .authority(BuildConfig.APPLICATION_ID)
            .appendPath("drawable")
            .appendPath("ic_flash_on_24dp_old")
            .build();

    public static void fetch(final PackageManager packageManager, @NonNull final LauncherItemFetcherCallback callback) {

        new AsyncTask<PackageManager, Void, List<PadItemInfo>>() {

            @Override
            protected List<PadItemInfo> doInBackground(PackageManager... params) {
                PackageManager pkgManager = params[0];

                List<PadItemInfo> infos = new ArrayList();

                Intent mainIntent = new Intent(Intent.ACTION_CREATE_SHORTCUT, null);

                List<ResolveInfo> mApps = pkgManager.queryIntentActivities(mainIntent, 0);

                L.d("mApps : " + mApps.size());
                for (ResolveInfo info :
                        mApps) {
                    try {
                        String pkgName = info.activityInfo.packageName;
                        CharSequence name = info.loadLabel(pkgManager);
                        L.d("name : " + name);
                        Resources res = pkgManager.getResourcesForApplication(pkgName);
                        int iconResId = info.getIconResource();
                        Uri iconUri = new Uri.Builder()
                                .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
                                .authority(pkgName)
                                .appendPath(res.getResourceTypeName(iconResId))
                                .appendPath(res.getResourceEntryName(iconResId))
                                .build();

                        PadItemInfo.Builder builder = new PadItemInfo.Builder();
                        builder.setType(PhilPad.Pads.PAD_TYPE_SHORTCUT).setTitle(name.toString()).setPackageName(info.activityInfo.packageName).setApplicationName(info.activityInfo.name).setImageFileName(iconUri.toString());
                        infos.add(builder.build());

                    } catch (IllegalArgumentException | PackageManager.NameNotFoundException | Resources.NotFoundException e) {
                        e.printStackTrace();
                    }
                }
                return infos;
            }

            @Override
            protected void onPostExecute(List<PadItemInfo> launchItemList) {
                callback.onResult(launchItemList);
            }

        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, packageManager);
    }
}
