package com.philleeran.flicktoucher.view.pad.command;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.view.WindowManager;

import com.philleeran.flicktoucher.PadUtils;
import com.philleeran.flicktoucher.R;
import com.philleeran.flicktoucher.view.context.ContextActivity;
import com.philleeran.flicktoucher.utils.L;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ShowAppContext implements Command {
    private Context mContext;

    public ShowAppContext(Context context) {
        mContext = context;
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
                UsageStatsManager mUsageStatsManager = (UsageStatsManager) mContext.getSystemService("usagestats");
                long time = System.currentTimeMillis();
                // We get usage stats for the last 10 seconds
                List<UsageStats> stats = mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000 * 60 * 60 * 3, time);
                // Sort the stats by the last time used

                Collections.sort(stats, new Comparator<UsageStats>() {
                    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public int compare(UsageStats lhs, UsageStats rhs) {
                        if (rhs.getLastTimeUsed() > lhs.getLastTimeUsed())
                            return 1;
                        else {
                            return -1;
                        }
                    }
                });
                if (stats != null && stats.size() > 0) {
                    String topPackageName = stats.get(0).getPackageName();
                    ComponentName component = new ComponentName(mContext, ContextActivity.class);
                    Intent bi = new Intent().setComponent(component);
                    bi.putExtra(PadUtils.INTENT_DATA_PACKAGENAME, topPackageName);
                    bi.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(bi);
                }
            }
        } else {
            ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
            // get the info from the currently running task
            List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
            L.d("topActivity CURRENT Activity ::" + taskInfo.get(0).topActivity.getClassName());
            ComponentName componentInfo = taskInfo.get(0).topActivity;

            ComponentName component = new ComponentName(mContext, ContextActivity.class);
            Intent bi = new Intent().setComponent(component);
            bi.putExtra(PadUtils.INTENT_DATA_PACKAGENAME, componentInfo.getPackageName());
            bi.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(bi);

        }

    }

}
