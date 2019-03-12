
package com.philleeran.flicktoucher.service;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.TargetApi;
import android.app.Notification;
import android.content.Intent;
import android.os.Build;
import android.os.Parcelable;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;

import com.philleeran.flicktoucher.utils.L;

import java.util.List;

public class PhilPadAccessibilityService extends AccessibilityService {

    public static final String ACTION_CAPTURE_NOTIFICATION = "action_capture_notification";
    public static final String EXTRA_NOTIFICATION_TYPE = "extra_notification_type";
    public static final String EXTRA_PACKAGE_NAME = "extra_package_name";
    public static final String EXTRA_MESSAGE = "extra_message";

    public static final int EXTRA_TYPE_NOTIFICATION = 0x19;
    public static final int EXTRA_TYPE_OTHERS = EXTRA_TYPE_NOTIFICATION + 1;

    public static String TAG = "AccessibilityEventCaptureService";

    private int statusBarHeight;
    private View view;
    private boolean viewAdded = false;
    private WindowManager windowManager;
    private WindowManager.LayoutParams layoutParams;

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();


    }

    private String getEventType(AccessibilityEvent event) {
        switch (event.getEventType()) {
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
                L.d("TYPE_NOTIFICATION_STATE_CHANGED");
                return "TYPE_NOTIFICATION_STATE_CHANGED";
            case AccessibilityEvent.TYPE_VIEW_CLICKED:
                L.d("TYPE_VIEW_CLICKED");
                return "TYPE_VIEW_CLICKED";
            case AccessibilityEvent.TYPE_VIEW_FOCUSED:
                L.d("TYPE_VIEW_FOCUSED");
                return "TYPE_VIEW_FOCUSED";
            case AccessibilityEvent.TYPE_VIEW_LONG_CLICKED:
                L.d("TYPE_VIEW_LONG_CLICKED");
                return "TYPE_VIEW_LONG_CLICKED";
            case AccessibilityEvent.TYPE_VIEW_SELECTED:
                L.d("TYPE_VIEW_SELECTED");
                return "TYPE_VIEW_SELECTED";
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                L.d("TYPE_WINDOW_STATE_CHANGED");
                return "TYPE_WINDOW_STATE_CHANGED";
            case AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED:
                L.d("TYPE_VIEW_TEXT_CHANGED");
                return "TYPE_VIEW_TEXT_CHANGED";
        }
        return "default";
    }

    private String getEventText(AccessibilityEvent event) {
        StringBuilder sb = new StringBuilder();
        for (CharSequence s : event.getText()) {
            sb.append(s);
        }
        return sb.toString();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

        L.d("AccessibilityEvent");
  /*      if (hasMessage(event) == false) {
            L.d("AccessibilityEvent false");
            return;
        }*/

/*

        switch (event.getEventType()) {
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
            {
                L.d("TYPE_NOTIFICATION_STATE_CHANGED");
                Parcelable parcelable = event.getParcelableData();
                if (parcelable instanceof Notification) {
                    Notification notification = (Notification) parcelable;
                    L.d("Notification Text : " + notification.extras.getString(notification.EXTRA_TEXT));
                }
            }
            break;
            case AccessibilityEvent.TYPE_VIEW_CLICKED:
                L.d("TYPE_VIEW_CLICKED");
                break;
            case AccessibilityEvent.TYPE_VIEW_FOCUSED:
                L.d("TYPE_VIEW_FOCUSED");
                break;
            case AccessibilityEvent.TYPE_VIEW_LONG_CLICKED:
                L.d("TYPE_VIEW_LONG_CLICKED");
                break;
            case AccessibilityEvent.TYPE_VIEW_SELECTED:
                L.d("TYPE_VIEW_SELECTED");
                break;
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                L.d("TYPE_WINDOW_STATE_CHANGED");
                break;
            case AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED:
                L.d("TYPE_VIEW_TEXT_CHANGED");
                break;
            default:
                break;
        }
*/



        /*final int eventType = event.getEventType();
        final String sourcePackageName = (String) event.getPackageName();
        final List<CharSequence> messages = event.getText();
        final CharSequence message = messages.get(0);
        L.d("AccessibilityEvent sourcePackageName : " + sourcePackageName);






        if (eventType == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
            Intent sendingIntent = new Intent(ACTION_CAPTURE_NOTIFICATION);
            Parcelable parcelable = event.getParcelableData();
            if (parcelable instanceof Notification) {
                sendingIntent.putExtra(EXTRA_NOTIFICATION_TYPE, EXTRA_TYPE_NOTIFICATION);
            } else {
                sendingIntent.putExtra(EXTRA_NOTIFICATION_TYPE, EXTRA_TYPE_OTHERS);
            }
            sendingIntent.putExtra(EXTRA_PACKAGE_NAME, sourcePackageName);
            sendingIntent.putExtra(EXTRA_MESSAGE, message);
            getApplicationContext().sendBroadcast(sendingIntent);
        }*/
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        L.d("onServiceConnected");
/*        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.flags = AccessibilityServiceInfo.DEFAULT;a
        info.notificationTimeout = 100;
        info.eventTypes = AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_ALL_MASK;
        info.packageNames = null;
        setServiceInfo(info);*/
    }


    @Override
    public void onInterrupt() {
    }

    private boolean hasMessage(AccessibilityEvent event) {
        return event != null && (event.getText().size() > 0);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        L.d("onStartCommand");
        if (intent != null) {
            int type = intent.getIntExtra("type", GLOBAL_ACTION_BACK);
            L.d("PhilPadAccessibilityService type : " + type);
            performGlobalAction(type);
        }
        return super.onStartCommand(intent, flags, startId);
    }
}
