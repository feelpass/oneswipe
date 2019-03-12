package com.philleeran.flicktoucher.utils;

public class LogConfig {

    public boolean enabled;

    public final String tag;
    public final String logFormat;
    public final String logFormatForStack;
    public final int stackDepth;
    public final boolean lineFeed;
    public final boolean android;

    private LogConfig(Builder build) {
        this.tag = build.tag;
        this.enabled = build.enabled;
        this.logFormat = build.logFormat;
        this.logFormatForStack = build.logFormatForStack;
        this.stackDepth = build.stackDepth;
        this.lineFeed = build.lineFeed;
        this.android = build.android;
    }

    public static class Builder {

        public static final String DEFAULT_LOG_FORMAT =  "(%s:%d) %s [%s]";
        public static final String DEFAULT_LOG_FORMAT_FOR_STACK =  "    (%s:%d) %s [%s]";
        public static final String DEFAULT_TAG = "OneSwipe";

        private String tag = DEFAULT_TAG;
        private boolean enabled = true;
        private String logFormat = DEFAULT_LOG_FORMAT;
        private String logFormatForStack = DEFAULT_LOG_FORMAT_FOR_STACK;
        private int stackDepth = 4;
        private boolean lineFeed = true;
        private boolean android = true;

        private Builder() {
        }

        public Builder setTag(String tag) {
            this.tag = tag;
            return this;
        }

        public Builder setEnabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public Builder setLogFormat(String logFormat) {
            this.logFormat = logFormat;
            return this;
        }

        public Builder setLogFormatForStack(String logFormat)
        {
            this.logFormatForStack = logFormat;
            return this;
        }

        public Builder setStackDepth(int stackDepth) {
            this.stackDepth = stackDepth;
            return this;
        }

        public Builder setLineFeed(boolean lineFeed) {
            this.lineFeed = lineFeed;
            return this;
        }

        public void setAndroid(boolean android) {
            this.android = android;
        }

        public LogConfig build() {
            return new LogConfig(this);
        }
    }

    public static LogConfig createDefaultConfig() {
        return new LogConfig.Builder().build();
    }

    public static LogConfig createDisableConfig() {
        return new LogConfig.Builder().setEnabled(false).build();
    }

}

