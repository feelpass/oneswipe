
package com.philleeran.flicktoucher.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.philleeran.flicktoucher.db.PhilPad;
import com.philleeran.flicktoucher.utils.D;
import com.philleeran.flicktoucher.utils.L;

public class RestartServiceReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(D.ACTION_RESTART_PERSISTENTSERVICE)) {
            L.d("ACTION_RESTART_PERSISTENTSERVICE");
            if (PhilPad.Settings.getBoolean(context.getContentResolver(), PhilPad.Settings.SETTINGS_KEY_IS_SERVICE_ENABLE, true)) {
                Intent i = new Intent(context, PhilPadService.class);
                context.startService(i);
            } else {
                Intent intent2 = new Intent(context, RestartServiceReceiver.class);
                intent2.setAction(D.ACTION_RESTART_PERSISTENTSERVICE);
                PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent2, 0);
                AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                am.cancel(sender);
            }
        }
    }
}
