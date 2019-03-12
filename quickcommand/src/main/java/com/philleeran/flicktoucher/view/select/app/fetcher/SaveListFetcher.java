package com.philleeran.flicktoucher.view.select.app.fetcher;


import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import com.philleeran.flicktoucher.db.ItemInfoBase;
import com.philleeran.flicktoucher.db.ItemModel;
import com.philleeran.flicktoucher.db.PadItemInfo;
import com.philleeran.flicktoucher.db.PhilPad;
import com.philleeran.flicktoucher.utils.D;

import java.util.List;

public final class SaveListFetcher {

    public static void fetch(final Context context, @NonNull final SaveItemFetcherCallback callback, final int groupId) {

        new AsyncTask<Void, Void, Boolean>() {

            @Override
            protected Boolean doInBackground(Void... params) {
                List<PadItemInfo> infos = ItemModel.getItemList();
                boolean isSuccessNewAdd = false;
                for (ItemInfoBase info : infos) {
                    if (info.getType() != PhilPad.Pads.PAD_TYPE_NULL) {
                        isSuccessNewAdd = true;
                        break;
                    }
                }
                if (groupId == D.GROUPID_GROUND) {
                    if (isSuccessNewAdd) {
                        for (ItemInfoBase info : infos) {
                            PhilPad.Pads.setPadItem(context, groupId, info.getPositionId(), info.getType(), info.getKey(), info.getTitle(), info.getImage(), info.getToGroupId(), info.getExtraData());
                        }

                        PhilPad.Settings.putBoolean(context.getContentResolver(), PhilPad.Settings.SETTINGS_KEY_COMPLETE_FIRST_ADD_APP, true);

                        return true;
                    } else {
                        return false;
                    }
                }

                for (ItemInfoBase info : infos) {
                    PhilPad.Pads.setPadItem(context, groupId, info.getPositionId(), info.getType(), info.getKey(), info.getTitle(), info.getImage(), info.getToGroupId(), info.getExtraData());
                }
                int padSize = PhilPad.Settings.getInt(context.getContentResolver(), PhilPad.Settings.SETTINGS_KEY_PAD_SIZE, D.PAD_SIZE_4_4);
                PhilPad.Pads.setGroupIcon(context, groupId, padSize);
                return true;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                callback.onResult(result);
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
}
