package com.philleeran.flicktoucher.view.pad.command;

import android.content.Context;
import android.content.Intent;

public class MediaPlay implements Command {
    private Context mContext;

    public MediaPlay(Context context) {
        mContext = context;
    }

    @Override
    public void execute() {

        Intent i = new Intent("com.android.music.musicservicecommand");
        i.putExtra("command", "play");
        mContext.sendBroadcast(i);

    }

}
