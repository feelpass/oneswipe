package com.philleeran.flicktoucher.view.settings.task;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.philleeran.flicktoucher.R;
import com.philleeran.flicktoucher.db.PhilPad;
import com.philleeran.flicktoucher.utils.L;
import com.philleeran.flicktoucher.utils.MarketVersionChecker;
import com.philleeran.flicktoucher.utils.Utils;
import com.philleeran.flicktoucher.view.settings.SettingsActivity;

/**
 * Created by young on 2017-03-12.
 */

public class VersionChecker extends AsyncTask<Void, Void, String> {

    private Context mContext;

    public VersionChecker(Context context) {
        mContext = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(Void... params) {
        L.d("version doInBackground");
        return MarketVersionChecker.getMarketVersionFast("com.philleeran.flicktoucher");
    }

    @Override
    protected void onPostExecute(String result) {

        // Version check the execution application.

        L.d("version : " + result);
        if (!TextUtils.isEmpty(result) && !Utils.getVersionNameByPackageName(mContext, "com.philleeran.flicktoucher").equals(result)) {
            String[] marketVersionString;
            marketVersionString = result.split("\\.");
            String[] localVersionString = Utils.getVersionNameByPackageName(mContext, "com.philleeran.flicktoucher").split("\\.");

            int marketVersion = (Integer.parseInt(marketVersionString[0]) * 1000000000) + (Integer.parseInt(marketVersionString[1]) * 100000000) + Integer.parseInt(marketVersionString[2]);
            L.d("marketVersion : " + marketVersion);
            int localVersion = (Integer.parseInt(localVersionString[0]) * 1000000000) + (Integer.parseInt(localVersionString[1]) * 100000000) + Integer.parseInt(localVersionString[2]);
            L.d("localVersion : " + localVersion);
            if (marketVersion > localVersion) {
                if (!PhilPad.Settings.getBoolean(mContext.getContentResolver(), PhilPad.Settings.SETTINGS_KEY_RATE, false)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setMessage(R.string.dialog_update_message).setCancelable(false)
                            .setPositiveButton(R.string.dialog_update_rate, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent(Intent.ACTION_VIEW);
                                    intent.setData(Uri.parse("market://details?id=" + mContext.getPackageName()));
                                    mContext.startActivity(intent);
                                }
                            }).setNeutralButton(R.string.dialog_later, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    }).create().show();
                }
            }
        }
        super.onPostExecute(result);
    }
}
