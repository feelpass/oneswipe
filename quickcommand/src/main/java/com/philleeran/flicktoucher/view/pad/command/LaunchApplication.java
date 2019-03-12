package com.philleeran.flicktoucher.view.pad.command;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.IntentCompat;
import android.text.TextUtils;

import com.google.android.gms.analytics.HitBuilders;
import com.philleeran.flicktoucher.PadUtils;
import com.philleeran.flicktoucher.PhilPadApplication;
import com.philleeran.flicktoucher.R;
import com.philleeran.flicktoucher.utils.L;
import com.philleeran.flicktoucher.utils.Utils;

import java.util.List;

public class LaunchApplication implements Command {
    private Context mContext;

    private String mComponentName;
    private String mTopPackageName;

    public LaunchApplication(Context context, String componentName, String topPackageName) {

        mContext = context;
        mComponentName = componentName;
        mTopPackageName = topPackageName;
    }

    @Override
    public void execute() {
        try {
            L.dd("launchActivity componentName : " + mComponentName);
            boolean isToast = false;

            Intent intent = Intent.parseUri(mComponentName, 0);
            if (TextUtils.isEmpty(intent.getPackage())) {
                intent.setPackage(intent.getComponent().getPackageName());
            }
            intent.setFlags(0);
            intent.addFlags(
                    Intent.FLAG_RECEIVER_FOREGROUND | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
            );

            String labelName = Utils.getLabelByPackageName(mContext, intent.getPackage());

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

                if (!TextUtils.isEmpty(mTopPackageName)) {
                    if (findLauncherPackageName().equals(mTopPackageName)) {
                        PadUtils.ToastLong(mContext, "Starting");
                        labelName = Utils.getLabelByPackageName(mContext, intent.getPackage());
                    } else if (intent.getPackage().equals(mTopPackageName)) {
                        PadUtils.ToastLong(mContext, "Current");
                        labelName = Utils.getLabelByPackageName(mContext, intent.getPackage());
                    }
                }


                mContext.startActivity(intent);
            } else {

                final ActivityManager activityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
                final List<ActivityManager.RecentTaskInfo> recentTasks = activityManager.getRecentTasks(Integer.MAX_VALUE, ActivityManager.RECENT_IGNORE_UNAVAILABLE);

                ActivityManager.RecentTaskInfo recentTaskInfo = null;
                for (int i = 0; i < recentTasks.size(); i++) {
                    if (i == 0) {
                        if (findLauncherPackageName().equals(recentTasks.get(i).baseIntent.getComponent().getPackageName())) {
                            isToast = true;
                            labelName = Utils.getLabelByPackageName(mContext, intent.getPackage());
                            L.e("Launch at Launcher");
                        } else if (intent.getPackage().equals(recentTasks.get(i).baseIntent.getComponent().getPackageName())) {
                            isToast = true;
                            labelName = Utils.getLabelByPackageName(mContext, intent.getPackage());
                            L.e("Launch at Launcher");
                        }
                    }
                    if (recentTasks.get(i).baseIntent.getComponent().getPackageName().equals(intent.getPackage())) {
                        recentTaskInfo = recentTasks.get(i);
                        break;
                    }
                }

                if (recentTaskInfo != null && recentTaskInfo.id > -1) {
                    activityManager.moveTaskToFront(recentTaskInfo.persistentId, ActivityManager.MOVE_TASK_WITH_HOME);
                    if (isToast && labelName != null) {
                        PadUtils.ToastLong(mContext, "Launching " + labelName);
                        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
                        mContext.startActivity(intent);
                    }
                } else {
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(intent);
                }
            }
            if (!TextUtils.isEmpty(labelName)) {
                L.d("launchActivity : " + labelName);
                PhilPadApplication.tracker().send(new HitBuilders.EventBuilder().setCategory("launch").setAction("activity").setLabel(labelName).build());
            }

        } catch (Exception e) {
            PadUtils.Toast(mContext, R.string.toast_not_found);
            Intent intent = new Intent(Intent.ACTION_VIEW);

            try {
                Intent launchIntent = Intent.parseUri(mComponentName, 0);
                if (TextUtils.isEmpty(launchIntent.getPackage())) {
                    launchIntent.setPackage(launchIntent.getComponent().getPackageName());
                    intent.setData(Uri.parse("market://details?id=" + launchIntent.getPackage()));
                }
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
            } catch (Exception e1) {
                L.e(e1);
            }
        }
    }

    private String findLauncherPackageName() {
        final Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        final ResolveInfo res = mContext.getPackageManager().resolveActivity(intent, 0);
        return res.activityInfo.packageName;
    }
}
