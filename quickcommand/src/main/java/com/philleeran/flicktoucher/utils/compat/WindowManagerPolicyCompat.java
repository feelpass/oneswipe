package com.philleeran.flicktoucher.utils.compat;

import java.lang.reflect.Field;

/**
 * Created by youngpillee on 7/13/15.
 */
public class WindowManagerPolicyCompat {

    private static final Class<?> CLASS_WindowManagerPolicy = CompatUtils.getClass("android.view.WindowManagerPolicy");
    private static final Field FIELD_WINDOW_MODE_FREESTYLE = CompatUtils.getField(CLASS_WindowManagerPolicy, "WINDOW_MODE_FREESTYLE");
    private static final Field FIELD_WINDOW_MODE_OPTION_COMMON_PINUP = CompatUtils.getField(CLASS_WindowManagerPolicy, "WINDOW_MODE_OPTION_COMMON_PINUP");
    private static final Field FIELD_WINDOW_MODE_OPTION_COMMON_SCALE = CompatUtils.getField(CLASS_WindowManagerPolicy, "WINDOW_MODE_OPTION_COMMON_SCALE");
    public static int WINDOW_MODE_FREESTYLE = CompatUtils.getFieldValue(null, null, FIELD_WINDOW_MODE_FREESTYLE);
    public static int WINDOW_MODE_OPTION_COMMON_PINUP = CompatUtils.getFieldValue(null, null, FIELD_WINDOW_MODE_OPTION_COMMON_PINUP);
    public static int WINDOW_MODE_OPTION_COMMON_SCALE = CompatUtils.getFieldValue(null, null, FIELD_WINDOW_MODE_OPTION_COMMON_SCALE);
}
