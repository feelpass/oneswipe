package com.philleeran.flicktoucher.view.pad.command;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class GoHome implements Command {

    private Context mContext;

    public GoHome(Context context) {
        mContext = context;
    }

    @Override
    public void execute() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
    }
}
