package com.philleeran.flicktoucher.view.pad.command;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import com.philleeran.flicktoucher.view.settings.SettingsActivity;

public class GoSettings implements Command {
    Context mContext;

    public GoSettings(Context context)
    {
        mContext = context;
    }

    @Override
    public void execute() {

        ComponentName component = new ComponentName(mContext, SettingsActivity.class);
        Intent bi = new Intent().setComponent(component);
        bi.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(bi);


    }
}
