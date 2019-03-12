
package com.philleeran.flicktoucher.service;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;

import com.philleeran.flicktoucher.db.NotificationItemInfo;
import com.philleeran.flicktoucher.utils.L;
import com.philleeran.flicktoucher.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class PadNotificationListener extends NotificationListenerService {
    // Message tags
    private static final int MSG_NOTIFY = 1;
    private static final int MSG_CANCEL = 2;
    private static final int MSG_STARTUP = 3;
    private static final int MSG_ORDER = 4;
    private static final int MSG_DISMISS = 5;
    private static final int MSG_LAUNCH = 6;
    private static final int PAGE = 10;

    static final String ACTION_DISMISS = "com.philleeran.flicktoucher.notificationlistener.DISMISS";
    public static final String ACTION_LAUNCH = "com.philleeran.flicktoucher.notificationlistener.LAUNCH";
    static final String ACTION_REFRESH = "com.philleeran.flicktoucher.notificationlistener.REFRESH";
    public static final String EXTRA_KEY = "key";

    public static ArrayList<StatusBarNotification> sNotifications;

    public static ArrayList<NotificationItemInfo> sNotificataionItems;

    private Context mContext;

    public static List<StatusBarNotification> getNotifications() {
        return sNotifications;
    }

    public static List<NotificationItemInfo> getNotificatinosInfo() {
        return sNotificataionItems;
    }

    private final Ranking mTempRanking = new Ranking();


    private class Delta {
        final StatusBarNotification mSbn;
        final RankingMap mRankingMap;

        public Delta(StatusBarNotification sbn, RankingMap rankingMap) {
            mSbn = sbn;
            mRankingMap = rankingMap;
        }
    }


    private final Comparator<StatusBarNotification> mRankingComparator =
            new Comparator<StatusBarNotification>() {
                private final Ranking mLhsRanking = new Ranking();
                private final Ranking mRhsRanking = new Ranking();

                @Override
                public int compare(StatusBarNotification lhs, StatusBarNotification rhs) {
                    mRankingMap.getRanking(lhs.getKey(), mLhsRanking);
                    mRankingMap.getRanking(rhs.getKey(), mRhsRanking);
                    return Integer.compare(mLhsRanking.getRank(), mRhsRanking.getRank());
                }
            };

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Delta delta = null;
            if (msg.obj instanceof Delta) {
                delta = (Delta) msg.obj;
            }

            switch (msg.what) {
                case MSG_NOTIFY:
                    L.i("notify: " + delta.mSbn.getKey());
                    synchronized (sNotifications) {
                        boolean exists = mRankingMap.getRanking(delta.mSbn.getKey(), mTempRanking);
                        if (!exists) {
                            sNotifications.add(delta.mSbn);
                        } else {
                            int position = mTempRanking.getRank();
                            sNotifications.set(position, delta.mSbn);
                        }
                        if (delta.mSbn.isClearable()) {
                            NotificationItemInfo.Builder builder = new NotificationItemInfo.Builder();

                            builder.setPackageName(delta.mSbn.getPackageName());
                            builder.setApplicationName(Utils.getLabelByPackageName(mContext, delta.mSbn.getPackageName()));
                            builder.setLargeIcon(delta.mSbn.getNotification().largeIcon);
                            builder.setContentIntent(delta.mSbn.getNotification().contentIntent);
                            builder.setDeleteIntent(delta.mSbn.getNotification().deleteIntent);
                            if (!TextUtils.isEmpty(delta.mSbn.getNotification().tickerText)) {
                                builder.setTickerText(delta.mSbn.getNotification().tickerText.toString());
                            }
                            builder.setMissedCount(delta.mSbn.getNotification().number);
                            sNotificataionItems.add(builder.build());


//                            PhilPad.Notifications.setNotificationItem(mContext, builder.build());
                        }
                        mRankingMap = delta.mRankingMap;
                        Collections.sort(sNotifications, mRankingComparator);
                        L.i("finish with: " + sNotifications.size());
                    }
/*
                    LocalBroadcastManager.getInstance(PadNotificationListener.this)
                            .sendBroadcast(new Intent(ACTION_REFRESH)
                                    .putExtra(EXTRA_KEY, delta.mSbn.getKey()));
*/
                    break;

                case MSG_CANCEL:
                    L.i("remove: " + delta.mSbn.getKey());
                    synchronized (sNotifications) {
                        boolean exists = mRankingMap.getRanking(delta.mSbn.getKey(), mTempRanking);
                        if (exists) {
                            sNotifications.remove(mTempRanking.getRank());
                        }
                        mRankingMap = delta.mRankingMap;
                        Collections.sort(sNotifications, mRankingComparator);
                    }

/*
                    LocalBroadcastManager.getInstance(PadNotificationListener.this)
                            .sendBroadcast(new Intent(ACTION_REFRESH));
*/
                    break;

                case MSG_ORDER:
                    L.i("reorder");
                    synchronized (sNotifications) {
                        mRankingMap = delta.mRankingMap;
                        Collections.sort(sNotifications, mRankingComparator);
                    }
/*
                    LocalBroadcastManager.getInstance(PadNotificationListener.this)
                            .sendBroadcast(new Intent(ACTION_REFRESH));
*/
                    break;

                case MSG_STARTUP:
                    L.d("MSG_STARTUP");
                    fetchActive();

                    break;

                case MSG_DISMISS:
                    L.d("MSG_DISMISS");
                    if (msg.obj instanceof String) {
                        final String key = (String) msg.obj;
                        mRankingMap.getRanking(key, mTempRanking);
                        StatusBarNotification sbn = sNotifications.get(mTempRanking.getRank());
                        if ((sbn.getNotification().flags & Notification.FLAG_AUTO_CANCEL) != 0 &&
                                sbn.getNotification().contentIntent != null) {
                            try {

                                sbn.getNotification().contentIntent.send();
                            } catch (PendingIntent.CanceledException e) {
                                L.d("failed to send intent for " + sbn.getKey(), e);
                            }
                        }
                        cancelNotification(key);
                    }

                    break;

                case MSG_LAUNCH:
                    if (msg.obj instanceof String) {
                        final String key = (String) msg.obj;
                        mRankingMap.getRanking(key, mTempRanking);
                        StatusBarNotification sbn = sNotifications.get(mTempRanking.getRank());
                        if (sbn.getNotification().contentIntent != null) {
                            try {
                                sbn.getNotification().contentIntent.send();
                            } catch (PendingIntent.CanceledException e) {
                                L.d("failed to send intent for " + sbn.getKey(), e);
                            }
                        }
                        if ((sbn.getNotification().flags & Notification.FLAG_AUTO_CANCEL) != 0) {
                            cancelNotification(key);
                        }
                    }
                    break;
            }
        }
    };

    private RankingMap mRankingMap;


    private NotificationServiceReceiver notificationServiceReceiver;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        notificationServiceReceiver = new NotificationServiceReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_DISMISS);
        filter.addAction(ACTION_LAUNCH);
        registerReceiver(notificationServiceReceiver, filter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(notificationServiceReceiver);
    }

    @Override
    public void onListenerConnected() {
        Message.obtain(mHandler, MSG_STARTUP).sendToTarget();
    }

    @Override
    public void onNotificationRankingUpdate(RankingMap rankingMap) {
        Message.obtain(mHandler, MSG_ORDER,
                new Delta(null, rankingMap)).sendToTarget();
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn, RankingMap rankingMap) {
        L.i("**********  onNotificationPosted");
        L.i("ID :" + sbn.getId() + " ticker : " + sbn.getNotification().tickerText + "packageName : " + sbn.getPackageName());
        L.d("onNotificationPosted contentIntent : " + sbn.getNotification().contentIntent);
        //if(sbn.isClearable())
        //{
        Message.obtain(mHandler, MSG_NOTIFY, new Delta(sbn, rankingMap)).sendToTarget();
        //}
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn, RankingMap rankingMap) {
        L.i("**********  onNotificationRemoved");
        L.i("ID :" + sbn.getId() + " ticker : " + sbn.getNotification().tickerText + "packageName : " + sbn.getPackageName());
        Message.obtain(mHandler, MSG_CANCEL,
                new Delta(sbn, rankingMap)).sendToTarget();
    }

    private void fetchActive() {
        L.d("fetchActive");
        mRankingMap = getCurrentRanking();
        sNotifications = new ArrayList<>();
        sNotificataionItems = new ArrayList<>();
        for (StatusBarNotification sbn : getActiveNotifications()) {
            sNotifications.add(sbn);
        }

        Collections.sort(sNotifications, mRankingComparator);
    }

    class NotificationServiceReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String key = intent.getStringExtra(EXTRA_KEY);
            int what = MSG_DISMISS;
            if (ACTION_LAUNCH.equals(intent.getAction())) {
                what = MSG_LAUNCH;
            }
            L.d("received an action broadcast " + intent.getAction());
            if (!TextUtils.isEmpty(key)) {
                L.d("  on " + key);
                Message.obtain(mHandler, what, key).sendToTarget();
            }
        }
    }
}
