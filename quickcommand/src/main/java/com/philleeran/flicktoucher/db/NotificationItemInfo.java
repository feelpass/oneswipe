
package com.philleeran.flicktoucher.db;

import android.app.PendingIntent;
import android.graphics.Bitmap;

public class NotificationItemInfo {
    private final String mPackageName;

    private final String mApplicationName;

    private final PendingIntent mContentIntent;

    private final PendingIntent mDeleteIntent;

    private final Bitmap mLargeIcon;

    private final String mTickerText;

    private final long mCreateTime;

    private final long mLastUpdateTime;

    private final String mExtraData;

    private final int mMissedCount;

    private NotificationItemInfo(Builder builder) {
        mPackageName = builder.mPackageName;
        mApplicationName = builder.mApplicationName;
        mContentIntent = builder.mContentIntent;
        mDeleteIntent = builder.mDeleteIntent;
        mLargeIcon = builder.mLargeIcon;
        mTickerText = builder.mTickerText;
        mCreateTime = builder.mCreateTime;
        mLastUpdateTime = builder.mLastUpdateTime;
        mExtraData = builder.mExtraData;
        mMissedCount = builder.mMissedCount;
    }

    public String getPackageName() { return mPackageName; }

    public String getApplicationName() {
        return mApplicationName;
    }

    public PendingIntent getContentIntent() {
        return mContentIntent;
    }

    public PendingIntent getDeleteIntent()
    {
        return mDeleteIntent;
    }

    public Bitmap getLargeIcon()
    {
        return mLargeIcon;
    }

    public String getTickerText()
    {
        return mTickerText;
    }

    public long getCreateTime()
    {
        return mCreateTime;
    }
    public long getLastUpdateTime()
    {
        return mLastUpdateTime;
    }

    public String getExtraData()
    {
        return mExtraData;
    }

    public static class Builder {

        private String mPackageName;

        private String mApplicationName;

        private PendingIntent mContentIntent;

        private PendingIntent mDeleteIntent;

        private Bitmap mLargeIcon;

        private String mTickerText;

        private long mCreateTime;

        private long mLastUpdateTime;

        private String mExtraData;

        private int mMissedCount;

        public Builder() {
        }

        public Builder setPackageName(String packageName) {
            mPackageName = packageName;
            return this;
        }

        public Builder setApplicationName(String applicationName) {
            mApplicationName = applicationName;
            return this;
        }

        public Builder setContentIntent(PendingIntent contentIntent)
        {
            mContentIntent = contentIntent;
            return this;
        }

        public Builder setDeleteIntent(PendingIntent deleteIntent)
        {
            mDeleteIntent = deleteIntent;
            return this;
        }

        public Builder setLargeIcon(Bitmap bitmap)
        {
            mLargeIcon = bitmap;
            return this;
        }

        public Builder setTickerText(String text)
        {
            mTickerText = text;
            return this;
        }

        public Builder setCreateTime(long createTime) {
            mCreateTime = createTime;
            return this;
        }

        public Builder setLastUpdateTime(long updateTime)
        {
            mLastUpdateTime = updateTime;
            return this;
        }

        public Builder setExtraData(String extraData) {
            mExtraData = extraData;
            return this;
        }

        public Builder setMissedCount(int count)
        {
            mMissedCount = count;

            return this;

        }


        public NotificationItemInfo build() {
            return new NotificationItemInfo(this);
        }

    }

}
