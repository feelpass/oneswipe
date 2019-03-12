package com.philleeran.flicktoucher.view.pad.command;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.WindowManager;

import com.philleeran.flicktoucher.PadUtils;
import com.philleeran.flicktoucher.R;
import com.philleeran.flicktoucher.utils.L;
import com.philleeran.flicktoucher.utils.Utils;

public class GoLastApp implements Command {


    private final Context mContext;
    private String mTopPackageName;
    private String mPackageName;

    public GoLastApp(Context context, String packageName, String topPackageName) {

        mContext = context;
        mPackageName = packageName;
        mTopPackageName = topPackageName;
    }

    @Override
    public void execute() {
        L.d("execute");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (!PadUtils.isUsageAccessEnable(mContext)) {
                try {
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setTitle(R.string.dialog_howtouse_title).setMessage(R.string.dialog_check_usage_access_message).setPositiveButton(R.string.dialog_yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            mContext.startActivity(intent);
                        }
                    }).create();
                    AlertDialog alert = builder.create();
                    alert.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                    alert.show();

                } catch (Exception e) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setTitle(R.string.dialog_howtouse_title).setMessage(R.string.dialog_check_usage_access_message).setPositiveButton(R.string.dialog_yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(Settings.ACTION_SETTINGS);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            mContext.startActivity(intent);

                        }
                    }).create();
                    AlertDialog alert = builder.create();
                    alert.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                    alert.show();
                    //Settings > Security > Usage access > Check SidePad
                }
            } else {
                try {
                    Utils.launchActivity(mContext, mPackageName, mTopPackageName);
                } catch (Exception e) {
                    PadUtils.Toast(mContext, R.string.toast_not_found);
                    Intent intentt = new Intent(Intent.ACTION_VIEW);
                    intentt.setData(Uri.parse("market://details?id=" + mPackageName));
                    intentt.setFlags(Intent.FLAG_RECEIVER_FOREGROUND);
                    mContext.startActivity(intentt);
                }
            }
        } else {
            try {
                Utils.launchActivity(mContext, mPackageName, mTopPackageName);
            } catch (Exception e) {
                PadUtils.Toast(mContext, R.string.toast_not_found);
                Intent intentt = new Intent(Intent.ACTION_VIEW);
                intentt.setData(Uri.parse("market://details?id=" + mPackageName));
                intentt.setFlags(Intent.FLAG_RECEIVER_FOREGROUND);
                mContext.startActivity(intentt);
            }
        }
    }
}
