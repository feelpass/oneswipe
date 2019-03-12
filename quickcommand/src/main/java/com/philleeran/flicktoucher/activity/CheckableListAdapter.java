package com.philleeran.flicktoucher.activity;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.BaseAdapter;

import com.philleeran.flicktoucher.db.ItemInfoBase;
import com.philleeran.flicktoucher.utils.L;

import java.util.HashSet;
import java.util.Set;

public abstract class CheckableListAdapter extends BaseAdapter {

    protected final Set<ItemInfoBase> mCheckedItems = new HashSet<>();

    public void checkItem(@NonNull ItemInfoBase item) {
        if (mCheckedItems.contains(item)) {
            L.e("[%s] already selected.", item.getKey());
        } else {
            mCheckedItems.add(item);
            notifyDataSetChanged();
        }
    }

    public void uncheckItem(@NonNull ItemInfoBase item) {
        boolean result = mCheckedItems.remove(item);
        if (result) {
            notifyDataSetChanged();
        }
    }

    public boolean isChecked(@NonNull ItemInfoBase item) {
        return mCheckedItems.contains(item);
    }

    @Nullable
    public ItemInfoBase getCheckedItem(@NonNull String key) {
        for (ItemInfoBase item : mCheckedItems) {
            if (item.getKey().equals(key)) {
                return item;
            }
        }
        return null;
    }

}
