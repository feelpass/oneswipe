
package com.philleeran.flicktoucher.utils.compat;


import android.os.IBinder;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ServiceManagerCompat {
    private static final Class<?> CLASS_ServiceManager = CompatUtils.getClass("android.os.ServiceManager");

    private static final Method METHOD_getService = CompatUtils.getMethod(CLASS_ServiceManager, "getService", String.class);

    public static IBinder getService(String name) {
        return (IBinder)CompatUtils.invoke(null, null, METHOD_getService, name);
    }

}
