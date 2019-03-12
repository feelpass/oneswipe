package com.philleeran.flicktoucher.view.pad.command;

import android.content.Context;
import android.content.Intent;

public class MediaStop implements Command {
    private Context mContext;

    public MediaStop(Context context) {
        mContext = context;
    }

    @Override
    public void execute() {
        Intent intent = new Intent("com.android.music.musicservicecommand");
        intent.putExtra("command", "stop");
        mContext.sendBroadcast(intent);

    }

}
