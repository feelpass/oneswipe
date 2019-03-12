package com.philleeran.flicktoucher.db;


import com.philleeran.flicktoucher.utils.D;

import java.util.HashMap;

public final class LocalSettings {
    public static HashMap<String, PadItemInfo> mInfoHashMap = new HashMap<>();
    public static int mVibrationTime = 0;
    public static int mAnimationDurationTime = D.ANIMATION_DURATION;

    public static float mTrigerDefaultAlpha = D.TIRIGGER_DEFAULT_ALPHA;

    public static long mHoverSelectDelay = 50;
    public static boolean mShowTitle = true;
}