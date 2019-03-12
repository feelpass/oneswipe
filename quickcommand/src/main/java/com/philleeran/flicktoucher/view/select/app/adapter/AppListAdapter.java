package com.philleeran.flicktoucher.view.select.app.adapter;

import android.content.Context;
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


public class AppListAdapter extends CheckableListAdapter {


    private final LayoutInflater mInflater;
    private final List<PadItemInfo> mAppList;
    private final Context mContext;

    public AppListAdapter(Context context, @NonNull LayoutInflater inflater, @NonNull List<PadItemInfo> list) {
        mContext = context;
        mInflater = inflater;
        mAppList = list;
    }


    public List<PadItemInfo> getItems() {
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
            convertView = mInflater.inflate(R.layout.app_grid_item_layout, parent, false);
            convertView.setTag(R.id.app_icon_image_view, convertView.findViewById(R.id.app_icon_image_view));
            convertView.setTag(R.id.check_image_view, convertView.findViewById(R.id.check_image_view));
            convertView.setTag(R.id.app_name_text_view, convertView.findViewById(R.id.app_name_text_view));
        }

        ItemInfoBase launchItem = mAppList.get(position);
        TextView nameView = (TextView) convertView.getTag(R.id.app_name_text_view);
        nameView.setText(launchItem.getTitle());

        ImageView iconView = (ImageView) convertView.getTag(R.id.app_icon_image_view);
        Picasso.with(mContext).load(launchItem.getImage()).into(iconView);

        View checkView = (View) convertView.getTag(R.id.check_image_view);
        checkView.setVisibility(mCheckedItems.contains(launchItem) ? View.VISIBLE : View.INVISIBLE);

        return convertView;
    }

}
