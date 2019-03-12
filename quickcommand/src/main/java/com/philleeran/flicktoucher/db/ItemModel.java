package com.philleeran.flicktoucher.db;


import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;

import com.philleeran.flicktoucher.utils.L;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public final class ItemModel {

    private static ItemModel sInstance;
    public static HashMap<String, PadItemInfo> mInfoHashMap = new HashMap<>();
    public static int mGroupId = 0;
    public static int mPosition = 0;


    private final Handler mHandler;
    private final List<EventListener> mListeners;

    private ItemModel() {
        mHandler = new Handler();
        mListeners = new LinkedList<>();
    }

    public static ItemModel getInstance() {
        synchronized (ItemModel.class) {
            if (sInstance == null) {
                sInstance = new ItemModel();
            }

            return sInstance;
        }
    }


    public static void setItemList(Context context, int groupId, int limitCount) {
        mGroupId = groupId;
        L.d("groupId : " + mGroupId);
        ArrayList<PadItemInfo> infos = PhilPad.Pads.getPadItemInfosInGroupPure(context, groupId, limitCount);
        mInfoHashMap.clear();
        for (int i = 0; i < infos.size(); i++) {
            PadItemInfo info = infos.get(i);
            L.d("info key : " + info.getKey());
            if (!mInfoHashMap.containsKey(info.getKey())) {

                mInfoHashMap.put(info.getKey(), info);
            } else {
                int lastKey = PhilPad.Settings.getInt(context.getContentResolver(), PhilPad.Settings.SETTINGS_KEY_LAST_KEY_ID, 0);
                PadItemInfo.Builder builder = new PadItemInfo.Builder();
                builder.setType(PhilPad.Pads.PAD_TYPE_NULL).setImageFileName("").setGroupId(info.getGroupId()).setPositionId(info.getPositionId()).setPackageName("key" + lastKey);
                PhilPad.Settings.putInt(context.getContentResolver(), PhilPad.Settings.SETTINGS_KEY_LAST_KEY_ID, lastKey + 1);
                PadItemInfo temInfo = builder.build();
                mInfoHashMap.put(temInfo.getKey(), temInfo);
            }
        }
    }

    public static List<PadItemInfo> getItemList() {
        List<PadItemInfo> list = new ArrayList<PadItemInfo>(mInfoHashMap.values());
        Collections.sort(list, new Comparator<ItemInfoBase>() {
            @Override
            public int compare(ItemInfoBase lhs, ItemInfoBase rhs) {
                return lhs.getPositionId() - rhs.getPositionId();
            }
        });

        return list;
    }

    public static void setNextPosition(int position) {
        L.dd("setNextPosition " + position);
        List<PadItemInfo> itemList = ItemModel.getItemList();
        int prevPosition = position == -1 ? 0 : position;
        int i = 0;
        for (i = prevPosition; i < itemList.size(); i++) {
            ItemInfoBase info = itemList.get(i);
            if (info.getType() == PhilPad.Pads.PAD_TYPE_NULL) {
                ItemModel.mPosition = i;
                L.d("mPosition : " + ItemModel.mPosition);
                return;
            }
        }
        for (i = 0; i < prevPosition; i++) {
            ItemInfoBase info = itemList.get(i);
            if (info.getType() == PhilPad.Pads.PAD_TYPE_NULL) {
                ItemModel.mPosition = i;
                L.d("mPosition : " + ItemModel.mPosition);
                return;
            }
        }
        ItemModel.mPosition = -1;
        L.d("mPosition : " + ItemModel.mPosition);
    }

    public static void setPrevPosition() {
        List<PadItemInfo> itemList = ItemModel.getItemList();
        int prevPosition = ItemModel.mPosition == -1 ? 0 : ItemModel.mPosition;
        int i = 0;
        for (i = prevPosition - 1; i >= 0; i--) {
            ItemInfoBase info = itemList.get(i);
            if (info.getType() == PhilPad.Pads.PAD_TYPE_NULL) {
                ItemModel.mPosition = i;
                return;
            }
        }
        for (i = itemList.size() - 1; i > prevPosition; i--) {
            ItemInfoBase info = itemList.get(i);
            if (info.getType() == PhilPad.Pads.PAD_TYPE_NULL) {
                ItemModel.mPosition = i;
                return;
            }
        }
        ItemModel.mPosition = -1;
    }

    public interface EventListener {
        void onAdded(@NonNull ItemInfoBase item);

        void onRemoved(@NonNull String id);
    }

    public boolean putItem(@NonNull Context context, @NonNull final PadItemInfo data) {

        if (mInfoHashMap.containsKey(data.getKey()) == false) {
            mInfoHashMap.put(data.getKey(), data);
        }

        synchronized (mListeners) {
            for (final EventListener listener : mListeners) {
                mHandler.post(new ItemAddRunner(listener, data));
            }
        }
        return true;
    }


    public boolean removeItem(@NonNull Context context, @NonNull final String key) {

        if (mInfoHashMap.containsKey(key)) {
            mInfoHashMap.remove(key);
        }

        synchronized (mListeners) {
            for (final EventListener listener : mListeners) {
                mHandler.post(new ItemRemoveRunner(listener, key));
            }
        }

        return true;
    }


    public boolean updateItem(@NonNull Context context, @NonNull final String key) {

        if (mInfoHashMap.containsKey(key)) {
            ItemInfoBase info = mInfoHashMap.get(key);
            int groupId = info.getGroupId();
            int positionId = info.getPositionId();
            int lastNull = PhilPad.Settings.getInt(context.getContentResolver(), PhilPad.Settings.SETTINGS_KEY_LAST_KEY_ID, 0);
            L.d("groupId : " + groupId + " positionId : " + positionId);
            PadItemInfo.Builder builder = new PadItemInfo.Builder();
            builder.setGroupId(groupId).setPositionId(positionId).setType(PhilPad.Pads.PAD_TYPE_NULL).setImageFileName(null).setPackageName("key" + lastNull);
            PhilPad.Settings.putInt(context.getContentResolver(), PhilPad.Settings.SETTINGS_KEY_LAST_KEY_ID, lastNull + 1);
            PadItemInfo padItemInfo = builder.build();
            ItemModel.mPosition = info.getPositionId();

            mInfoHashMap.remove(key);
            mInfoHashMap.put(padItemInfo.getKey(), padItemInfo);
        }

        synchronized (mListeners) {
            for (final EventListener listener : mListeners) {
                mHandler.post(new ItemRemoveRunner(listener, key));
            }
        }

        return true;
    }


    public void addEventListener(@NonNull EventListener listener) {
        L.d("");
        synchronized (mListeners) {
            if (!mListeners.contains(listener)) {
                mListeners.add(listener);
            }
        }
    }

    public void removeEventListener(@NonNull EventListener listener) {
        L.d("");
        synchronized (mListeners) {
            mListeners.remove(listener);
        }
    }
    public void clearEventListener() {
        L.d("");
        mInfoHashMap.clear();
        synchronized (mListeners) {
            mListeners.clear();
        }
    }



    private static class ItemAddRunner implements Runnable {

        private final EventListener listener;
        private final ItemInfoBase data;

        public ItemAddRunner(@NonNull EventListener listener, @NonNull ItemInfoBase data) {
            this.listener = listener;
            this.data = data;
        }

        @Override
        public void run() {
            listener.onAdded(data);
        }
    }

    private static class ItemRemoveRunner implements Runnable {

        private final EventListener listener;
        private final String key;

        public ItemRemoveRunner(@NonNull EventListener listener, @NonNull String key) {
            this.listener = listener;
            this.key = key;
        }

        @Override
        public void run() {
            listener.onRemoved(key);
        }
    }
}