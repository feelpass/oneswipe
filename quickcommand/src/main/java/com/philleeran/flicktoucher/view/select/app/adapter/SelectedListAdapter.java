package com.philleeran.flicktoucher.view.select.app.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.philleeran.flicktoucher.R;
import com.philleeran.flicktoucher.db.ItemInfoBase;
import com.philleeran.flicktoucher.db.ItemModel;
import com.philleeran.flicktoucher.db.PadItemInfo;
import com.philleeran.flicktoucher.utils.L;
import com.squareup.picasso.Picasso;

import java.util.List;

public class SelectedListAdapter extends BaseAdapter {

    private final LayoutInflater mInflater;
    private List<PadItemInfo> mAppList;
    private final Context mContext;

    public SelectedListAdapter(Context context, @NonNull LayoutInflater inflater, @NonNull List<PadItemInfo> list) {
        mContext = context;
        mInflater = inflater;
        mAppList = list;
    }


    public void setItemList(List<PadItemInfo> list) {
        mAppList.clear();
        mAppList.addAll(list);
        for (ItemInfoBase info :
                mAppList) {
            L.d("info : " + info.getPositionId() + " path : " + info.getImage() + " key " + info.getKey());

        }
        notifyDataSetChanged();
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
            convertView = mInflater.inflate(R.layout.grid_icon_item_small, parent, false);
            convertView.setTag(R.id.item_image, convertView.findViewById(R.id.item_image));
            convertView.setTag(R.id.item_cancel, convertView.findViewById(R.id.item_cancel));
        }

        ItemInfoBase launchItem = mAppList.get(position);
        ImageView iconView = (ImageView) convertView.getTag(R.id.item_image);
        ImageView cancelView = (ImageView) convertView.getTag(R.id.item_cancel);
        if (!TextUtils.isEmpty(launchItem.getImage())) {
            Picasso.with(mContext).load(launchItem.getImage()).error(R.drawable.ic_border_clear_24dp).into(iconView);
            cancelView.setVisibility(View.VISIBLE);
        } else {
            cancelView.setVisibility(View.GONE);
            if (position == ItemModel.mPosition) {
                Picasso.with(mContext).load(R.drawable.ic_add_24dp_old).into(iconView);
            } else {
                Picasso.with(mContext).load(R.drawable.icon_hidden).error(R.drawable.icon_hidden).into(iconView);
            }
        }
        return convertView;
    }
}
