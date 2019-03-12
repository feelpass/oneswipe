
package com.philleeran.flicktoucher.db;

public class PadItemInfo extends ItemInfoBase {
    private final long mId;

    private final String mTitle;

    private String mComponentName;

    private final String mImageFileName;

    private final int mLaunchCount;

    private final int mType;

    private final long mCreateTime;

    private final int mIsRunning;

    private final int mFunctionMode;

    private int mToGroupId;

    private final String mExtraData;

    private int mGroupId;

    private int mPositionId;

    private final String mApplicationName;

    private PadItemInfo(Builder builder) {
        mId = builder.mId;
        mTitle = builder.mTitle;
        mComponentName = builder.mComponentName;
        mApplicationName = builder.mApplicationName;
        mImageFileName = builder.mImageFileName;
        mLaunchCount = builder.mLaunchCount;
        mType = builder.mType;
        mCreateTime = builder.mCreateTime;
        mIsRunning = builder.mIsRunning;
        mFunctionMode = builder.mFunctionMode;
        mToGroupId = builder.mToGroupId;
        mExtraData = builder.mExtraData;
        mPositionId = builder.mPositionId;
        mGroupId = builder.mGroupId;
    }

    public long getId()
    {
        return mId;
    }

    public String getTitle() {
        return mTitle;
    }

    @Override
    public String getSubtitle() {
        return mTitle;
    }

    @Override
    public String getImage() {
        return mImageFileName;
    }

    @Override
    public String getKey() {
        return mComponentName;
    }

    @Override
    public void setKey(String key) {
        mComponentName = key;
    }


    public String getPackageName() {
        return mComponentName;
    }

    public String getApplicationName() {
        return mApplicationName;
    }

    public String getImageFileName() {
        return mImageFileName;
    }

    public int getLaunchCount() {
        return mLaunchCount;
    }

    public int getType() {
        return mType;
    }

    @Override
    public void setGroupId(int id) {
        mGroupId = id;
    }

    @Override
    public void setPositionId(int id) {
        mPositionId = id;
    }

    @Override
    public void setToGroupId(int id) {
        mToGroupId = id;
    }

    public long getCreateTime() {
        return mCreateTime;
    }

    public int getIsRunning() {
        return mIsRunning;
    }

    public int getFunctionMode() {
        return mFunctionMode;
    }

    public int getToGroupId() {
        return mToGroupId;
    }

    public String getExtraData() {
        return mExtraData;
    }

    public int getPositionId() {
        return mPositionId;
    }


    public int getGroupId()
    {
        return mGroupId;
    }

    public static class Builder {

        private long mId;

        private String mTitle;

        private String mComponentName;

        private String mApplicationName;

        private String mImageFileName;

        private int mLaunchCount;

        private int mType;

        private long mCreateTime;

        private int mIsRunning;

        private int mFunctionMode;

        private int mToGroupId;

        private int mPositionId;

        public int mGroupId;

        private String mExtraData;

        public Builder() {
        }

        public Builder setId(long id)
        {
            this.mId = id;
            return this;
        }

        public Builder setTitle(String title) {
            this.mTitle = title;
            return this;
        }

        public Builder setPackageName(String packageName) {
            mComponentName = packageName;
            return this;
        }

        public Builder setApplicationName(String applicationName) {
            mApplicationName = applicationName;
            return this;
        }

        public Builder setImageFileName(String imageFileName) {
            mImageFileName = imageFileName;
            return this;
        }

        public Builder setLaunchCount(int launchCount) {
            mLaunchCount = launchCount;
            return this;
        }

        public Builder setType(int type) {
            mType = type;
            return this;
        }

        public Builder setCreateTime(long createTime) {
            mCreateTime = createTime;
            return this;
        }

        public Builder setIsRunning(int isRunning) {
            mIsRunning = isRunning;
            return this;
        }

        public Builder setFunctionMode(int functionMode) {
            mFunctionMode = functionMode;
            return this;
        }

        public Builder setToGroupId(int toGroupId) {
            mToGroupId = toGroupId;
            return this;
        }

        public Builder setExtraData(String extraData) {
            mExtraData = extraData;
            return this;
        }

        public Builder setPositionId(int position) {
            mPositionId = position;
            return this;
        }

        public Builder setGroupId(int groupid) {
            mGroupId = groupid;
            return this;
        }

        public PadItemInfo build() {
            return new PadItemInfo(this);
        }

    }

    @Override
    public String toString() {
        return "PadItemInfo{" +
                "mTitle='" + mTitle + '\'' +
                ", mComponentName='" + mComponentName + '\'' +
                ", mImageFileName='" + mImageFileName + '\'' +
                ", mType=" + mType +
                ", mApplicationName='" + mApplicationName + '\'' +
                ", mGroupId=" + mGroupId +
                ", mPositionId=" + mPositionId +
                ", mToGroupId=" + mToGroupId +
                '}';
    }
}
