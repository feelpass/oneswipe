package com.philleeran.flicktoucher.view.pad.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.View;

/**
 * Created by young on 2017-03-10.
 */

public interface BindListener {
    void bindView(View v, Context context, Cursor cursor);
}
