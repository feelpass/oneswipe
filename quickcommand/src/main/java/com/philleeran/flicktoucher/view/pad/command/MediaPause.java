package com.philleeran.flicktoucher.view.pad.command;

import android.content.Context;
import android.content.Intent;

public class MediaPause implements Command {
    private Context mContext;

    public MediaPause(Context context) {
        mContext = context;
    }

    @Override
    public void execute() {
        Intent i = new Intent("com.android.music.musicservicecommand");
        i.putExtra("command", "pause");
        mContext.sendBroadcast(i);

    }

}
