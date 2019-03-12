package com.philleeran.flicktoucher.view.select.app.fetcher;


import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ContactListFetcher {
    private static final Uri EMPTY_PHOTO_URI = new Uri.Builder()
            .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
            .authority(BuildConfig.APPLICATION_ID)
            .appendPath("drawable")
            .appendPath("ic_person_24dp_old")
            .build();

    public static void fetch(final Context context, @NonNull final LauncherItemFetcherCallback callback) {

        new AsyncTask<Void, Void, List<PadItemInfo>>() {

            @Override
            protected List<PadItemInfo> doInBackground(Void... params) {

                String[] proj = {
                        ContactsContract.CommonDataKinds.Phone._ID,
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                        ContactsContract.CommonDataKinds.Phone.NUMBER,
                        ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI
                };
                Cursor cursor = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, proj, null, null, null);
                if (cursor == null) {
                    return Collections.emptyList();
                }

                try {
                    List<PadItemInfo> items = new ArrayList<>();
                    Map<String, PadItemInfo> itemMap = new HashMap<>();

                    while (cursor.moveToNext()) {
                        long id = cursor.getLong(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone._ID));
                        String displayName = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                        String number = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        String photoThumbUri = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI));

                        if (TextUtils.isEmpty(displayName)) {
                            L.e("[%d] display is empty.", id);
                            continue;
                        }

                        if (TextUtils.isEmpty(number)) {
                            L.e("[%d] number is empty.", id);
                            continue;
                        }

                        Uri thumbnailUri = EMPTY_PHOTO_URI;
                        if (!TextUtils.isEmpty(photoThumbUri)) {
                            thumbnailUri = Uri.parse(photoThumbUri);
                        }

                        Intent intent = new Intent(Intent.ACTION_DIAL);
                        intent.setData(Uri.parse("tel:" + number));

                        try {
                            PadItemInfo.Builder builder = new PadItemInfo.Builder();
                            builder.setType(PhilPad.Pads.PAD_TYPE_CONTACT).setPackageName("contact"+String.valueOf(id)).setTitle(displayName).setApplicationName(number).setImageFileName(thumbnailUri.toString()).setExtraData(intent.toUri(0));
                            if (!itemMap.containsKey(number)) {
                                itemMap.put(number, builder.build());
                                items.add(builder.build());
                            }
                        } catch (IllegalArgumentException e) {
                            e.printStackTrace();
                        }
                    }

                    Collections.sort(items, new Comparator<PadItemInfo>() {
                        @Override
                        public int compare(PadItemInfo lhs, PadItemInfo rhs) {
                            return lhs.getTitle().compareTo(rhs.getTitle());
                        }
                    });

                    return items;

                } finally {
                    cursor.close();
                }
            }

            @Override
            protected void onPostExecute(List<PadItemInfo> launchItemList) {
                L.d("feelpass");
                callback.onResult(launchItemList);
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
}
