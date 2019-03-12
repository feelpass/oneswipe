package com.philleeran.flicktoucher.view.pad.command;

import android.content.Context;
import android.content.Intent;

public class MediaPrev implements Command {
    private Context mContext;

    public MediaPrev(Context context) {
        mContext = context;
    }

    @Override
    public void execute() {
        Intent i = new Intent("com.android.music.musicservicecommand");
        i.putExtra("command", "previous");
        mContext.sendBroadcast(i);

    }

}
