package com.philleeran.flicktoucher.utils.compat;

import android.content.Intent;

import java.lang.reflect.Field;

/**
 * Created by youngpillee on 7/13/15.
 */
public class IntentCompat {

    private static final Class<?> CLASS_Intent = Intent.class;
    private static final Field FIELD_EXTRA_WINDOW_MODE = CompatUtils.getField(CLASS_Intent, "EXTRA_WINDOW_MODE");
    public static final String EXTRA_WINDOW_MODE = CompatUtils.getFieldValue(null, null, FIELD_EXTRA_WINDOW_MODE);

}
