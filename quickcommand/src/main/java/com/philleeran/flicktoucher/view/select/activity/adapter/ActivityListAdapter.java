package com.philleeran.flicktoucher.view.select.activity.adapter;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.philleeran.flicktoucher.R;
import com.philleeran.flicktoucher.activity.CheckableListAdapter;
import com.philleeran.flicktoucher.db.ItemInfoBase;
import com.philleeran.flicktoucher.db.PadItemInfo;
import com.squareup.picasso.Picasso;

import java.util.List;


public class ActivityListAdapter extends CheckableListAdapter {


    private final LayoutInflater mInflater;
    private final List<ActivityInfo> mAppList;
    private final Context mContext;

    public ActivityListAdapter(Context context, @NonNull LayoutInflater inflater, @NonNull List<ActivityInfo> list) {
        mContext = context;
        mInflater = inflater;
        mAppList = list;
    }


    public List<ActivityInfo> getItems() {
        return mAppList;
    }

    @Override
    public int getCount() {
        return mAppList.size();
    }

    @Override
    public Object getItem(int position) {
        return mAppList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.activity_list_item_layout, parent, false);
            convertView.setTag(R.id.photo_view, convertView.findViewById(R.id.photo_view));
            convertView.setTag(R.id.display_name_view, convertView.findViewById(R.id.display_name_view));
            convertView.setTag(R.id.activity_name_view, convertView.findViewById(R.id.activity_name_view));
        }


        ActivityInfo launchItem = mAppList.get(position);
        TextView nameView = (TextView) convertView.getTag(R.id.activity_name_view);
        nameView.setText(launchItem.name);

        /*ImageView iconView = (ImageView) convertView.getTag(R.id.app_icon_image_view);
        Picasso.with(mContext).load(launchItem.icon).into(iconView);
*/

        return convertView;
    }

}
