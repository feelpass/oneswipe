package com.philleeran.flicktoucher.view.pad.command;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.philleeran.flicktoucher.R;
import com.philleeran.flicktoucher.utils.L;
import com.philleeran.flicktoucher.utils.Utils;

/**
 * Created by young on 2017-03-05.
 */

public class KillBackgroundProcess implements Command, Runnable {

    private Context mContext;

    public KillBackgroundProcess(Context context) {
        mContext = context;
    }

    @Override
    public void execute() {
        new Thread(this, "kill background app").start();
    }

    @Override
    public void run() {
        long a = System.currentTimeMillis();
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        ActivityManager activityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        activityManager.getMemoryInfo(mi);
        long availableMegs = mi.availMem / 1048576L;
        Utils.killAllBackgroundApp(mContext);

        activityManager.getMemoryInfo(mi);
        L.v("elapsed=" + (System.currentTimeMillis() - a) + "retain : " + ((mi.availMem / 1048576L) - availableMegs));

        if (((mi.availMem / 1048576L) - availableMegs) > 0) {
            final String message = String.format(mContext.getString(R.string.freed_memoey), ((mi.availMem / 1048576L) - availableMegs));
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
                }
            });
        } else {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mContext, mContext.getString(R.string.freed_memoey_already), Toast.LENGTH_LONG).show();
                }
            });

        }
    }
}
