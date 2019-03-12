package com.philleeran.flicktoucher.view.select.app.adapter;

import android.os.Build;
import android.telephony.PhoneNumberUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.philleeran.flicktoucher.R;
import com.philleeran.flicktoucher.activity.CheckableListAdapter;
import com.philleeran.flicktoucher.db.PadItemInfo;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by young on 2017-03-12.
 */

public class ContactListAdapter extends CheckableListAdapter {

    private final List<PadItemInfo> mItems;
    private final LayoutInflater mInflater;

    public ContactListAdapter(LayoutInflater inflater, List<PadItemInfo> items) {
        mInflater = inflater;
        mItems = items;
    }

    public List<PadItemInfo> getItems() {
        return mItems;
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public Object getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.contact_list_item_layout, parent, false);
            convertView.setTag(R.id.photo_view, convertView.findViewById(R.id.photo_view));
            convertView.setTag(R.id.display_name_view, convertView.findViewById(R.id.display_name_view));
            convertView.setTag(R.id.number_view, convertView.findViewById(R.id.number_view));
            convertView.setTag(R.id.check_image_view, convertView.findViewById(R.id.check_image_view));
        }

        PadItemInfo item = (PadItemInfo) getItem(position);

        TextView textView = (TextView) convertView.getTag(R.id.display_name_view);
        textView.setText(item.getTitle());

        textView = (TextView) convertView.getTag(R.id.number_view);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            textView.setText(PhoneNumberUtils.formatNumber(item.getApplicationName(), "US"));
        } else {
            textView.setText(PhoneNumberUtils.formatNumber(item.getApplicationName()));
        }

        Picasso.with(mInflater.getContext()).load(item.getImageFileName()).into((ImageView) convertView.getTag(R.id.photo_view));

        View checkView = (View) convertView.getTag(R.id.check_image_view);
        checkView.setVisibility(mCheckedItems.contains(item) ? View.VISIBLE : View.INVISIBLE);

        return convertView;
    }
}