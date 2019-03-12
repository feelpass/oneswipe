package com.philleeran.flicktoucher.view.pad.command;

import android.content.Context;
import android.content.Intent;

public class MediaNext implements Command {
    private Context mContext;

    public MediaNext(Context context) {
        mContext = context;
    }

    @Override
    public void execute() {

        Intent i = new Intent("com.android.music.musicservicecommand");
        i.putExtra("command", "next");
        mContext.sendBroadcast(i);

    }

}
