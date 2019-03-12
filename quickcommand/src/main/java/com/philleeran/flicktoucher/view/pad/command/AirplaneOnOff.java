package com.philleeran.flicktoucher.view.pad.command;

import android.content.Context;
import android.content.Intent;
import android.provider.Settings;

public class AirplaneOnOff implements Command {
    private Context mContext;

    public AirplaneOnOff(Context context) {
        mContext = context;
    }

    @Override
    public void execute() {

        Intent intent = new Intent(Settings.ACTION_SETTINGS);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);

    }

}
