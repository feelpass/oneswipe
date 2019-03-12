package com.philleeran.flicktoucher.db;

/**
 * Created by young on 2016-01-14.
 */

public abstract class ItemInfoBase {
    public abstract long getId();
    public abstract String getTitle();
    public abstract String getSubtitle();
    public abstract String getImage();
    public abstract int getGroupId();
    public abstract int getPositionId();
    public abstract String getKey();
    public abstract void setKey(String key);
    public abstract String getApplicationName();
    public abstract int getType();
    public abstract void setGroupId(int id);
    public abstract void setPositionId(int id);
    public abstract void setToGroupId(int id);
    public abstract int getToGroupId();
    public abstract String getExtraData();
}

