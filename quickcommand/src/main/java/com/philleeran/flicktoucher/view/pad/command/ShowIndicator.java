package com.philleeran.flicktoucher.view.pad.command;

import android.os.IBinder;
import android.os.RemoteException;

import com.philleeran.flicktoucher.utils.L;
import com.philleeran.flicktoucher.utils.compat.CompatUtils;
import com.philleeran.flicktoucher.utils.compat.ServiceManagerCompat;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ShowIndicator implements Command {

    @Override
    public void execute() {
        try {
            IBinder binder = ServiceManagerCompat.getService("statusbar");
            Class<?> statusBarClasz;
            statusBarClasz = CompatUtils.getClass(binder.getInterfaceDescriptor());
            Class<?> stubClasz = CompatUtils.getClass(binder.getInterfaceDescriptor() + "$Stub");
            Method asInterface = CompatUtils.getMethod(stubClasz, "asInterface", IBinder.class);
            Object statusBarObject = asInterface.invoke(stubClasz, binder);
            Method toggleRecentApps = CompatUtils.getMethod(statusBarClasz, "toggleNotificationPanel");
            toggleRecentApps.setAccessible(true);
            toggleRecentApps.invoke(statusBarObject);
        } catch (RemoteException e) {
            L.e(e);
        } catch (InvocationTargetException e) {
            L.e(e);
        } catch (IllegalAccessException e) {
            L.e(e);
        }

    }

}
