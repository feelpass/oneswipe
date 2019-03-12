package com.philleeran.flicktoucher.view.pad.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.philleeran.flicktoucher.R;
import com.philleeran.flicktoucher.db.PhilPad;


public class PadGridViewAdapter extends SimpleCursorAdapter implements PadGridViewAdapterContract.Model, PadGridViewAdapterContract.View{

    private BindListener mBindListener;
    private Context mContext;

    private int mGroupId;
    LayoutInflater mInflater;

    public PadGridViewAdapter(Context context, int layout, Cursor c, String[] from, int[] to) {
        super(context, layout, c, from, to);
        mInflater = LayoutInflater.from(context);
        mContext = context;
    }




    public int getGroupId() {
        return mGroupId;
    }

    public void setGroupId(int groupId, int padSize) {
        mGroupId = groupId;
        Cursor cursor = PhilPad.Pads.getPadItemInfosCursorInGroup(mContext, groupId, padSize * padSize);

        // TODO
        changeCursor(cursor);
        // notifyDataSetChanged();
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View v = mInflater.inflate(R.layout.grid_icon_item, parent, false);
        return v;
    }

    @Override
    public void bindView(View v, Context context, Cursor cursor) {


        mBindListener.bindView(v, context, cursor);


    }

    public final long getItemId(int position) {
        return position;
    }

    @Override
    public void setBindListener(BindListener padPresenter) {
        mBindListener = padPresenter;
    }
}