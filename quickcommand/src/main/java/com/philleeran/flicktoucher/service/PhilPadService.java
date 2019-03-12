

package com.philleeran.flicktoucher.service;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.Vibrator;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.philleeran.flicktoucher.view.pad.HotSpotView;
import com.philleeran.flicktoucher.IPhilPad;
import com.philleeran.flicktoucher.view.pad.PadBoardView;
import com.philleeran.flicktoucher.PadUtils;
import com.philleeran.flicktoucher.PhilPadApplication;
import com.philleeran.flicktoucher.view.pad.PrevBoardView;
import com.philleeran.flicktoucher.R;
import com.philleeran.flicktoucher.view.settings.SettingsActivity;
import com.philleeran.flicktoucher.db.LocalSettings;
import com.philleeran.flicktoucher.db.PhilPad;
import com.philleeran.flicktoucher.utils.D;
import com.philleeran.flicktoucher.utils.L;
import com.philleeran.flicktoucher.view.pad.PadContract;

public class PhilPadService extends Service implements OnKeyListener {
    static private int SPOT_SIZE_SQUARE = 15;

    static private int SPOT_SIZE_HEIGHT = 200;

    static private int SPOT_SIZE_WIDTH = 100;

    Context mContext;

    private Vibrator mVibrator;

    private WindowManager mWindowManager;

    private PadContract.View mBoardView;

    private PadContract.Presenter mPadPresenter;

    private PrevBoardView mPrevBoardView;

    private ImageView mLockImageViewCircle;

    private ImageView mLockImageViewFullCircle;

    private WindowManager.LayoutParams mLayoutParamsLockImageViewCircle;

    private HotSpotView mHotSpotView[] = new HotSpotView[11];

    private TouchListen mHotSpotTouchListen[] = new TouchListen[11];

    private WindowManager.LayoutParams mLayoutParamsPadBoardView;

    private WindowManager.LayoutParams mLayoutParamsPrevBoardView;

    private WindowManager.LayoutParams mLayoutParamsHotSpotView[] = new WindowManager.LayoutParams[11];

    public float DPSCALE;
    public int DISPLAY_WIDTH;

    public int DISPLAY_HEIGHT;

    private Notification mNotification;

    private NotificationManager mNotificationManager;

    private int mStartId;

    private Notification mNotificationHotspotDisable;

    private PadNoticeReceiver padNoticeReceiver;
    private Handler handler = new Handler();

    private final Runnable showClockByDelay = new Runnable() {
        public void run() {
            if (mPrevBoardView != null)
                mPrevBoardView.start();
        }
    };

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        mBoardView.setHideAndGroupIdInit();

        updateHotspotViews();
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        PhilPadApplication.tracker().setScreenName("created");

        mContext = this;
        boolean isPremium = PhilPad.Settings.getBoolean(mContext.getContentResolver(), PhilPad.Settings.SETTINGS_KEY_IS_PREMIUM, false);
        PhilPadApplication.tracker().send(new HitBuilders.EventBuilder().setCategory("PadService").setAction("onCreate").setLabel(isPremium ? "premium" : "lite").setNewSession().build());

        LocalSettings.mVibrationTime = PhilPad.Settings.getInt(mContext.getContentResolver(), PhilPad.Settings.SETTINGS_KEY_PAD_VIBRATION_LEVEL, 10);
        LocalSettings.mShowTitle = PhilPad.Settings.getBoolean(mContext.getContentResolver(), PhilPad.Settings.SETTINGS_KEY_PAD_SHOW_NAME_ENABLE, true);
        LocalSettings.mAnimationDurationTime = PhilPad.Settings.getInt(mContext.getContentResolver(), PhilPad.Settings.SETTINGS_KEY_PAD_ANIMATION_DURATION_LEVEL, D.ANIMATION_DURATION);
        boolean bTriggerVisible = PhilPad.Settings.getBoolean(mContext.getContentResolver(), PhilPad.Settings.SETTINGS_KEY_PAD_TRIGGER_VISIBLE, true);
        if (bTriggerVisible) {
            LocalSettings.mTrigerDefaultAlpha = D.TIRIGGER_DEFAULT_ALPHA;
        } else {
            LocalSettings.mTrigerDefaultAlpha = 0.0f;
        }

        mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        DPSCALE = getResources().getDisplayMetrics().density;
        DISPLAY_WIDTH = getResources().getDisplayMetrics().widthPixels;
        DISPLAY_HEIGHT = getResources().getDisplayMetrics().heightPixels;

        mBoardView = new PadBoardView(mContext, this);
        mPadPresenter = mBoardView.getPresenter();


        mPrevBoardView = new PrevBoardView(mContext, this);

        mLockImageViewCircle = new ImageView(mContext);

        Bitmap transparent = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        Bitmap bitmap = null;
        bitmap = Bitmap.createScaledBitmap(transparent, 500 * (int) DPSCALE, 500 * (int) DPSCALE, true);
        if (D.RECYCLE) {
            transparent.recycle();
        }
        transparent = null;
        Paint p = new Paint();
        p.setAntiAlias(true);
        p.setStyle(Paint.Style.STROKE);
        p.setColor(Color.WHITE);
        p.setStrokeWidth(10 * DPSCALE);

        Canvas c = new Canvas(bitmap);
        c.drawCircle(500 * (int) DPSCALE / 2, 500 * (int) DPSCALE / 2, 200 * (int) DPSCALE, p);

        // Drawable d = new BitmapDrawable(getResources(),bitmap);
        mLockImageViewCircle.setImageBitmap(bitmap);
        // mLockImageViewCircle.setBackground(d);

        mLockImageViewFullCircle = new ImageView(mContext);

        Bitmap transparent2 = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        Bitmap bitmap2 = null;
        bitmap2 = Bitmap.createScaledBitmap(transparent2, 500 * (int) DPSCALE, 500 * (int) DPSCALE, true);
        if (D.RECYCLE) {
            transparent2.recycle();
        }
        transparent2 = null;

        Paint p2 = new Paint();
        p2.setAntiAlias(true);
        p2.setColor(Color.WHITE);
        p2.setStrokeWidth(5 * DPSCALE); //
        Canvas c2 = new Canvas(bitmap2);
        c2.drawCircle(500 * (int) DPSCALE / 2, 500 * (int) DPSCALE / 2, 200 * (int) DPSCALE, p2);

        mLockImageViewFullCircle.setImageBitmap(bitmap2);

        mHotSpotView[0] = new HotSpotView(mContext, (SPOT_SIZE_SQUARE * 4), (int) (SPOT_SIZE_SQUARE * 1.5));/* mViewLeftTop */
        mHotSpotView[1] = new HotSpotView(mContext, (SPOT_SIZE_SQUARE * 4), (int) (SPOT_SIZE_SQUARE * 1.5));/* mViewRightTop */
        mHotSpotView[2] = new HotSpotView(mContext, (SPOT_SIZE_SQUARE), (SPOT_SIZE_HEIGHT));/* mViewLeftUp */
        mHotSpotView[3] = new HotSpotView(mContext, (SPOT_SIZE_SQUARE), (SPOT_SIZE_HEIGHT));/* mViewRightUp */
        mHotSpotView[4] = new HotSpotView(mContext, (SPOT_SIZE_SQUARE), (SPOT_SIZE_HEIGHT));/* mViewLeftDown */
        mHotSpotView[5] = new HotSpotView(mContext, (SPOT_SIZE_SQUARE), (SPOT_SIZE_HEIGHT));/* mViewRightDown */
        mHotSpotView[6] = new HotSpotView(mContext, (SPOT_SIZE_SQUARE), (SPOT_SIZE_SQUARE));/* mViewLeftBottom */
        mHotSpotView[7] = new HotSpotView(mContext, (SPOT_SIZE_SQUARE), (SPOT_SIZE_SQUARE));/* mViewRightBottom */
        mHotSpotView[8] = new HotSpotView(mContext, (SPOT_SIZE_SQUARE * 6), (SPOT_SIZE_SQUARE));/* mViewDownLeft */
        mHotSpotView[9] = new HotSpotView(mContext, (SPOT_SIZE_SQUARE * 6), (SPOT_SIZE_SQUARE));/* mViewDownRight */
        mHotSpotView[10] = new HotSpotView(mContext, (SPOT_SIZE_SQUARE * 6), (SPOT_SIZE_SQUARE));/* mViewDownCenter */
        mLayoutParamsPadBoardView = createPadViewLayoutParam();
        mLayoutParamsPrevBoardView = createPrevViewLayoutParam();

        mLayoutParamsHotSpotView[0] = createHotSpotLayoutParam(Gravity.LEFT | Gravity.TOP, 0, 0);/* mLayoutParamsLeftTop */
        mLayoutParamsHotSpotView[1] = createHotSpotLayoutParam(Gravity.LEFT | Gravity.TOP, (int) (DISPLAY_WIDTH / DPSCALE) - (SPOT_SIZE_SQUARE * 4), 0);/* mLayoutParamsRightTop */
        mLayoutParamsHotSpotView[2] = createHotSpotLayoutParam(Gravity.LEFT | Gravity.TOP, 0, (SPOT_SIZE_SQUARE * 8));/* mLayoutParamsLeftUp */
        mLayoutParamsHotSpotView[3] = createHotSpotLayoutParam(Gravity.LEFT | Gravity.TOP, (int) (DISPLAY_WIDTH / DPSCALE) - (SPOT_SIZE_SQUARE), (SPOT_SIZE_SQUARE * 8));/* mLayoutParamsRightUp */
        mLayoutParamsHotSpotView[4] = createHotSpotLayoutParam(Gravity.LEFT | Gravity.TOP, 0, (int) (DISPLAY_WIDTH / DPSCALE) - (SPOT_SIZE_SQUARE * 4));/* mLayoutParamsLeftDown */
        mLayoutParamsHotSpotView[5] = createHotSpotLayoutParam(Gravity.LEFT | Gravity.TOP, (int) (DISPLAY_WIDTH / DPSCALE) - (SPOT_SIZE_SQUARE), (int) (DISPLAY_WIDTH / DPSCALE) - (SPOT_SIZE_SQUARE * 4));/* mLayoutParamsRightDown */
        mLayoutParamsHotSpotView[6] = createHotSpotLayoutParam(Gravity.LEFT | Gravity.TOP, 0, (int) (DISPLAY_HEIGHT / DPSCALE) - (SPOT_SIZE_SQUARE));/* mLayoutParamsLeftBottom */
        mLayoutParamsHotSpotView[7] = createHotSpotLayoutParam(Gravity.LEFT | Gravity.TOP, (int) (DISPLAY_WIDTH / DPSCALE) - (SPOT_SIZE_SQUARE), (int) (DISPLAY_HEIGHT / DPSCALE) - (SPOT_SIZE_SQUARE));/* mLayoutParamsRightBottom */
        mLayoutParamsHotSpotView[8] = createHotSpotLayoutParam(Gravity.LEFT | Gravity.TOP, 25, (int) (DISPLAY_HEIGHT / DPSCALE) - (SPOT_SIZE_SQUARE));/* mLayoutParamsDownLeft */
        mLayoutParamsHotSpotView[9] = createHotSpotLayoutParam(Gravity.LEFT | Gravity.TOP, (int) (DISPLAY_WIDTH / DPSCALE) - (SPOT_SIZE_SQUARE * 6) - (25), (int) (DISPLAY_HEIGHT / DPSCALE) - (SPOT_SIZE_SQUARE));/* mLayoutParamsDownRight */
        mLayoutParamsHotSpotView[10] = createHotSpotLayoutParam(Gravity.LEFT | Gravity.TOP, (int) (DISPLAY_WIDTH / DPSCALE) / 2 - (SPOT_SIZE_SQUARE * 6) / 2, (int) (DISPLAY_HEIGHT / DPSCALE) - (SPOT_SIZE_SQUARE));/* mLayoutParamsDownCenter */

        mHotSpotTouchListen[0] = new TouchListen(Gravity.LEFT | Gravity.TOP, mHotSpotView[0], mLayoutParamsHotSpotView[0].width, mLayoutParamsHotSpotView[0].height, mLayoutParamsHotSpotView[0].x, mLayoutParamsHotSpotView[0].y);
        mHotSpotTouchListen[1] = new TouchListen(Gravity.RIGHT | Gravity.TOP, mHotSpotView[1], mLayoutParamsHotSpotView[1].width, mLayoutParamsHotSpotView[1].height, mLayoutParamsHotSpotView[1].x, mLayoutParamsHotSpotView[1].y);
        mHotSpotTouchListen[2] = new TouchListen(Gravity.LEFT | Gravity.TOP, mHotSpotView[2], mLayoutParamsHotSpotView[2].width, mLayoutParamsHotSpotView[2].height, mLayoutParamsHotSpotView[2].x, mLayoutParamsHotSpotView[2].y);
        mHotSpotTouchListen[3] = new TouchListen(Gravity.RIGHT | Gravity.TOP, mHotSpotView[3], mLayoutParamsHotSpotView[3].width, mLayoutParamsHotSpotView[3].height, mLayoutParamsHotSpotView[3].x, mLayoutParamsHotSpotView[3].y);
        mHotSpotTouchListen[4] = new TouchListen(Gravity.LEFT | Gravity.BOTTOM, mHotSpotView[4], mLayoutParamsHotSpotView[4].width, mLayoutParamsHotSpotView[4].height, mLayoutParamsHotSpotView[4].x, mLayoutParamsHotSpotView[4].y);
        mHotSpotTouchListen[5] = new TouchListen(Gravity.RIGHT | Gravity.BOTTOM, mHotSpotView[5], mLayoutParamsHotSpotView[5].width, mLayoutParamsHotSpotView[5].height, mLayoutParamsHotSpotView[5].x, mLayoutParamsHotSpotView[5].y);
        mHotSpotTouchListen[6] = new TouchListen(Gravity.LEFT | Gravity.BOTTOM, mHotSpotView[6], mLayoutParamsHotSpotView[6].width, mLayoutParamsHotSpotView[6].height, mLayoutParamsHotSpotView[6].x, mLayoutParamsHotSpotView[6].y);
        mHotSpotTouchListen[7] = new TouchListen(Gravity.RIGHT | Gravity.BOTTOM, mHotSpotView[7], mLayoutParamsHotSpotView[7].width, mLayoutParamsHotSpotView[7].height, mLayoutParamsHotSpotView[7].x, mLayoutParamsHotSpotView[7].y);
        mHotSpotTouchListen[8] = new TouchListen(Gravity.LEFT | Gravity.BOTTOM, mHotSpotView[8], mLayoutParamsHotSpotView[8].width, mLayoutParamsHotSpotView[8].height, mLayoutParamsHotSpotView[8].x, mLayoutParamsHotSpotView[8].y);
        mHotSpotTouchListen[9] = new TouchListen(Gravity.RIGHT | Gravity.BOTTOM, mHotSpotView[9], mLayoutParamsHotSpotView[9].width, mLayoutParamsHotSpotView[9].height, mLayoutParamsHotSpotView[9].x, mLayoutParamsHotSpotView[9].y);
        mHotSpotTouchListen[10] = new TouchListen(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM, mHotSpotView[10], mLayoutParamsHotSpotView[10].width, mLayoutParamsHotSpotView[10].height, mLayoutParamsHotSpotView[10].x, mLayoutParamsHotSpotView[10].y);

        for (int i = 0; i < mHotSpotView.length; i++) {
            mHotSpotView[i].setTouchListener(mHotSpotTouchListen[i]);
        }
        showHotspotViews();
        mWindowManager.addView(mBoardView.getView(), mLayoutParamsPadBoardView);
        mWindowManager.addView(mPrevBoardView, mLayoutParamsPrevBoardView);
        mLayoutParamsLockImageViewCircle = createLockImageCircleLayoutParam();
        mLockImageViewCircle.setVisibility(View.INVISIBLE);
        mLockImageViewFullCircle.setVisibility(View.INVISIBLE);
        mWindowManager.addView(mLockImageViewCircle, mLayoutParamsLockImageViewCircle);
        mWindowManager.addView(mLockImageViewFullCircle, mLayoutParamsLockImageViewCircle);
        padNoticeReceiver = new PadNoticeReceiver();
        IntentFilter filterHotSpotEnable = new IntentFilter();
        filterHotSpotEnable.addAction(D.ACTION_ENABLE_HOTSPOT_DETECT);
        filterHotSpotEnable.addAction(D.ACTION_DISABLE_HOTSPOT_DETECT);

        registerReceiver(padNoticeReceiver, filterHotSpotEnable);
        unregisterRestartAlarm();
    }

    private void updateLockImageViewCircleLayoutParam(int x, int y) {
        mLayoutParamsLockImageViewCircle.x = x - (mLockImageViewCircle.getMeasuredWidth() / 2);
        mLayoutParamsLockImageViewCircle.y = y - (mLockImageViewCircle.getMeasuredHeight() / 2);
        mWindowManager.updateViewLayout(mLockImageViewCircle, mLayoutParamsLockImageViewCircle);
        mWindowManager.updateViewLayout(mLockImageViewFullCircle, mLayoutParamsLockImageViewCircle);
    }

    private WindowManager.LayoutParams createLockImageCircleLayoutParam() {
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(WindowManager.LayoutParams.TYPE_SYSTEM_ERROR);
        lp.flags = WindowManager.LayoutParams.FLAG_TOUCHABLE_WHEN_WAKING//
                | WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED//
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;

        lp.format = PixelFormat.TRANSLUCENT;
        lp.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN;
        lp.x = 0;
        lp.y = 0;
        lp.width = (int) (250 * DPSCALE);
        lp.height = (int) (250 * DPSCALE);
        lp.gravity = Gravity.LEFT | Gravity.TOP;
        return lp;
    }

    public void showHotspotViews() {
        String[] hotspotsSharePref = mContext.getResources().getStringArray(R.array.hotspots_sharedpref);
        for (int i = 0; i < mHotSpotView.length; i++) {
            try {
                mWindowManager.removeView(mHotSpotView[i]);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        for (int i = 0; i < mHotSpotView.length; i++) {
            if (PhilPad.Settings.getBoolean(mContext.getContentResolver(), hotspotsSharePref[i], false)) {
                mWindowManager.addView(mHotSpotView[i], mLayoutParamsHotSpotView[i]);
            }
        }

    }

    public void reShowHotspotViews(String type, boolean isCheck) {
        String[] hotspotsSharePref = mContext.getResources().getStringArray(R.array.hotspots_sharedpref);
        if (isCheck) {
            for (int i = 0; i < mHotSpotView.length; i++) {
                if (hotspotsSharePref[i].equals(type)) {
                    mWindowManager.addView(mHotSpotView[i], mLayoutParamsHotSpotView[i]);
                }
            }
        } else {
            for (int i = 0; i < mHotSpotView.length; i++) {
                if (hotspotsSharePref[i].equals(type)) {
                    try {
                        mWindowManager.removeView(mHotSpotView[i]);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public void updateHotspotViews() {

        DPSCALE = getResources().getDisplayMetrics().density;
        DISPLAY_WIDTH = getResources().getDisplayMetrics().widthPixels;
        DISPLAY_HEIGHT = getResources().getDisplayMetrics().heightPixels;

        mLayoutParamsHotSpotView[0].x = 0;
        mLayoutParamsHotSpotView[0].y = 0;
        mLayoutParamsHotSpotView[1].x = (int) (((DISPLAY_WIDTH / DPSCALE) - (SPOT_SIZE_SQUARE * 4)) * DPSCALE);
        mLayoutParamsHotSpotView[1].y = 0;
        mLayoutParamsHotSpotView[2].x = 0;
        mLayoutParamsHotSpotView[2].y = (int) ((SPOT_SIZE_SQUARE * 8) * DPSCALE);
        mLayoutParamsHotSpotView[3].x = (int) (((DISPLAY_WIDTH / DPSCALE) - (SPOT_SIZE_SQUARE)) * DPSCALE);
        mLayoutParamsHotSpotView[3].y = (int) ((SPOT_SIZE_SQUARE * 8) * DPSCALE);
        mLayoutParamsHotSpotView[4].x = 0;
        mLayoutParamsHotSpotView[4].y = (int) (((DISPLAY_WIDTH / DPSCALE) - (SPOT_SIZE_SQUARE * 4)) * DPSCALE);
        mLayoutParamsHotSpotView[5].x = (int) (((DISPLAY_WIDTH / DPSCALE) - (SPOT_SIZE_SQUARE)) * DPSCALE);
        mLayoutParamsHotSpotView[5].y = (int) (((DISPLAY_WIDTH / DPSCALE) - (SPOT_SIZE_SQUARE * 4)) * DPSCALE);
        mLayoutParamsHotSpotView[6].x = 0;
        mLayoutParamsHotSpotView[6].y = (int) (((DISPLAY_HEIGHT / DPSCALE) - (SPOT_SIZE_SQUARE)) * DPSCALE);
        mLayoutParamsHotSpotView[7].x = (int) (((DISPLAY_WIDTH / DPSCALE) - (SPOT_SIZE_SQUARE)) * DPSCALE);
        mLayoutParamsHotSpotView[7].y = (int) (((DISPLAY_HEIGHT / DPSCALE) - (SPOT_SIZE_SQUARE)) * DPSCALE);
        mLayoutParamsHotSpotView[8].x = (int) (25 * DPSCALE);
        mLayoutParamsHotSpotView[8].y = (int) (((DISPLAY_HEIGHT / DPSCALE) - (SPOT_SIZE_SQUARE)) * DPSCALE);
        mLayoutParamsHotSpotView[9].x = (int) (((DISPLAY_WIDTH / DPSCALE) - (SPOT_SIZE_SQUARE * 6) - (25)) * DPSCALE);
        mLayoutParamsHotSpotView[9].y = (int) (((DISPLAY_HEIGHT / DPSCALE) - (SPOT_SIZE_SQUARE)) * DPSCALE);
        mLayoutParamsHotSpotView[10].x = (int) (((DISPLAY_WIDTH / DPSCALE) / 2 - (SPOT_SIZE_SQUARE * 6) / 2) * DPSCALE);
        mLayoutParamsHotSpotView[10].y = (int) (((DISPLAY_HEIGHT / DPSCALE) - (SPOT_SIZE_SQUARE)) * DPSCALE);

        for (int i = 0; i < mHotSpotTouchListen.length; i++) {
            mHotSpotTouchListen[i].setValue(mLayoutParamsHotSpotView[i].x, mLayoutParamsHotSpotView[i].y);
        }

        String[] hotspotsSharePref = mContext.getResources().getStringArray(R.array.hotspots_sharedpref);

        for (int i = 0; i < mHotSpotView.length - 1; i++) {
            if (PhilPad.Settings.getBoolean(mContext.getContentResolver(), hotspotsSharePref[i], false)) {
                mWindowManager.updateViewLayout(mHotSpotView[i], mLayoutParamsHotSpotView[i]);
            }
        }
        mPadPresenter.updateConfiguration(DPSCALE, DISPLAY_WIDTH, DISPLAY_HEIGHT);
    }

    public void setHotspotViewsDetectEnable(boolean enable) {
        if (enable == true) {
            for (int i = 0; i < mHotSpotView.length; i++) {
                mHotSpotView[i].setVisibility(View.VISIBLE);
            }
            Toast.makeText(mContext, mContext.getString(R.string.toast_trigger_area_detect_enabled), Toast.LENGTH_LONG).show();
        } else {
            for (int i = 0; i < mHotSpotView.length; i++) {
                mHotSpotView[i].setVisibility(View.GONE);
            }
            Toast.makeText(mContext, mContext.getString(R.string.toast_trigger_area_detect_disable), Toast.LENGTH_LONG).show();
        }
    }

    private WindowManager.LayoutParams createPadViewLayoutParam() {
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(WindowManager.LayoutParams.TYPE_PHONE);
        lp.flags = WindowManager.LayoutParams.FLAG_TOUCHABLE_WHEN_WAKING//d.j
                | WindowManager.LayoutParams.FLAG_FULLSCREEN//
                | WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED//
                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS//
//                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE //
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;

        lp.format = PixelFormat.TRANSLUCENT;
        lp.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN;
        lp.x = 0;
        lp.y = 0;
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        lp.gravity = Gravity.LEFT | Gravity.TOP;
        return lp;
    }


    private WindowManager.LayoutParams createPrevViewLayoutParam() {
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(WindowManager.LayoutParams.TYPE_SYSTEM_ERROR);
        lp.flags = WindowManager.LayoutParams.FLAG_TOUCHABLE_WHEN_WAKING//
                | WindowManager.LayoutParams.FLAG_FULLSCREEN//
                | WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED//
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN//
                | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL//
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        lp.format = PixelFormat.TRANSLUCENT;
        lp.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN;
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        return lp;
    }

    private WindowManager.LayoutParams createHotSpotLayoutParam(int gravity, int x, int y) {
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(WindowManager.LayoutParams.TYPE_SYSTEM_ERROR);
        lp.flags = WindowManager.LayoutParams.FLAG_TOUCHABLE_WHEN_WAKING//
                | WindowManager.LayoutParams.FLAG_FULLSCREEN//
                | WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED//
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN//
                | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL//
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        lp.format = PixelFormat.TRANSLUCENT;
        lp.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN;
        lp.x = (int) (x * DPSCALE);
        lp.y = (int) (y * DPSCALE);
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = gravity;
        return lp;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        PendingIntent pendingIntentSettingActivity = PendingIntent.getActivity(this, 0, new Intent(this, SettingsActivity.class), PendingIntent.FLAG_CANCEL_CURRENT);
        Intent hotspotIntentEnable = new Intent();
        hotspotIntentEnable.setAction(D.ACTION_ENABLE_HOTSPOT_DETECT);
        PendingIntent pendingIntentEnableHotspot = PendingIntent.getBroadcast(this, 0, hotspotIntentEnable, PendingIntent.FLAG_CANCEL_CURRENT);

        Intent hotspotIntentDisable = new Intent();
        hotspotIntentDisable.setAction(D.ACTION_DISABLE_HOTSPOT_DETECT);
        PendingIntent pendingIntentDisableHotspot = PendingIntent.getBroadcast(this, 0, hotspotIntentDisable, PendingIntent.FLAG_CANCEL_CURRENT);

        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotification = new Notification.Builder(getApplicationContext()).setPriority(Notification.PRIORITY_MIN).setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.nofification_text)).setAutoCancel(false).setSmallIcon(R.drawable.ic_launcher).build();
        mNotification.contentView = new RemoteViews(getPackageName(), R.layout.notification_content_layer);
        mNotification.contentView.setOnClickPendingIntent(R.id.notification_icon, pendingIntentSettingActivity);
        mNotification.contentView.setOnClickPendingIntent(R.id.notification_screen_overlay_on_off, pendingIntentDisableHotspot);


        mNotificationHotspotDisable = new Notification.Builder(getApplicationContext()).setPriority(Notification.PRIORITY_DEFAULT)
                .setContentTitle(getString(R.string.notification_title_trigger_area_enabled)).setContentText(getString(R.string.notification_text_trigger_area_disabled)).setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_launcher).setContentIntent(pendingIntentEnableHotspot).build();
        mStartId = startId;
        startForeground(startId, mNotification);
        mNotificationManager.notify(startId, mNotification);
        return START_STICKY;
    }

    public void setStartHotspotDisable() {
        mNotificationManager.cancel(mStartId);
        startForeground(mStartId, mNotificationHotspotDisable);
        setHotspotViewsDetectEnable(false);
    }

    public class PadNoticeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals(D.ACTION_DISABLE_HOTSPOT_DETECT)) {
                L.d("ACTION_DISABLE_HOTSPOT_DETECT");
                setStartHotspotDisable();
            } else if (intent.getAction().equals(D.ACTION_ENABLE_HOTSPOT_DETECT)) {
                L.d("ACTION_ENABLE_HOTSPOT_DETECT");
                mNotificationManager.cancel(mStartId);
                startForeground(mStartId, mNotification);
                setHotspotViewsDetectEnable(true);
            }
        }
    }

    @Override
    public void onDestroy() {
        L.d("onDestroy");

        PhilPadApplication.tracker().setScreenName("destroyed");
        PhilPadApplication.tracker().send(new HitBuilders.EventBuilder()
                .setCategory("PadService")
                .setAction("onDestroy")
                .build());
        PhilPadApplication.tracker().send(new HitBuilders.ScreenViewBuilder().build());


        for (int i = 0; i < mHotSpotView.length; i++) {
            try {
                mWindowManager.removeView(mHotSpotView[i]);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        mWindowManager.removeView(mLockImageViewCircle);
        mWindowManager.removeView(mLockImageViewFullCircle);
        unregisterReceiver(padNoticeReceiver);
        mPadPresenter.unregisterQuickSettingReceiver();
        mPadPresenter.detachView();
        registerRestartAlarm();
    }

    void registerRestartAlarm() {
        Intent intent = new Intent(PhilPadService.this, RestartServiceReceiver.class);
        intent.setAction(D.ACTION_RESTART_PERSISTENTSERVICE);
        PendingIntent sender = PendingIntent.getBroadcast(PhilPadService.this, 0, intent, 0);
        long firstTime = SystemClock.elapsedRealtime();
        firstTime += 3 * 1000;
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstTime, 3 * 1000, sender);
    }

    void unregisterRestartAlarm() {
        Intent intent = new Intent(PhilPadService.this, RestartServiceReceiver.class);
        intent.setAction(D.ACTION_RESTART_PERSISTENTSERVICE);
        PendingIntent sender = PendingIntent.getBroadcast(PhilPadService.this, 0, intent, 0);
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        am.cancel(sender);
    }


    class TouchListen implements View.OnTouchListener {
        private int mGravity;
        private boolean bStarted = false;
        float mStartX;
        float mStartY;
        private boolean mIsPreSwipeDragUp = false;
        private boolean mIsSwipeDragUp = false;
        float mX;
        float mY;
        private int mMarginX;
        private int mMarginY;
        private int mWidth;
        private int mHeight;
        Rect mRect;
        private HotSpotView mHotSpotView;

        public TouchListen(int gravity, HotSpotView hotSpotView, int width, int height, int marginX, int marginY) {
            mWidth = width;
            mHeight = height;
            mMarginX = marginX;
            mMarginY = marginY;
            mGravity = gravity;
            mHotSpotView = hotSpotView;
            mRect = new Rect();
        }

        public void setValue(int marginX, int marginY) {
            mMarginX = marginX;
            mMarginY = marginY;
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mPadPresenter.setShowPopupEnable(false);
                    vibrate(LocalSettings.mVibrationTime);
                    mStartX = event.getRawX();
                    mStartY = event.getRawY();

                    int a[] = new int[2];
                    mHotSpotView.getLocationOnScreen(a);
                    mHotSpotView.getLocalVisibleRect(mRect);
                    mRect.left = a[0];
                    mRect.top = a[1];
                    updateLockImageViewCircleLayoutParam((int) mStartX, (int) mStartY);
                    mLockImageViewCircle.setVisibility(View.VISIBLE);
                    mLockImageViewFullCircle.setVisibility(View.VISIBLE);
                    mLockImageViewFullCircle.setScaleX(0);
                    mLockImageViewFullCircle.setScaleY(0);
                    mLockImageViewCircle.setAlpha(0.0f);
                    mLockImageViewFullCircle.setAlpha(1.0f);

                    if (PhilPad.Settings.getBoolean(mContext.getContentResolver(), PhilPad.Settings.SETTINGS_KEY_PAD_STATUSINFO_ENABLE, true) == true) {
                        handler.postDelayed(showClockByDelay, 700);
                    }
                    mIsPreSwipeDragUp = false;
                    mIsSwipeDragUp = false;

                    if (PhilPad.Settings.getInt(mContext.getContentResolver(), PhilPad.Settings.SETTINGS_KEY_INTERACTION_OPTION, D.InteractionOption.INTERACTION_OPTION_SWIPE_AND_UP) == D.InteractionOption.INTERACTION_OPTION_SWIPE_AND_UP) {
                        mIsPreSwipeDragUp = true;
                    } else {
                        mIsPreSwipeDragUp = false;
                    }
                    mPadPresenter.setGravity(mGravity);
                    break;
                case MotionEvent.ACTION_MOVE: {

                    if (bStarted == false) {
                        mX = event.getRawX();
                        mY = event.getRawY();
                        int distance = PadUtils.getDistance((int) mStartX, (int) mStartY, (int) mX, (int) mY);
                        if (mIsPreSwipeDragUp ? distance > 25 * DPSCALE : distance > 100 * DPSCALE) {
                            mLockImageViewCircle.setVisibility(View.INVISIBLE);
                            mLockImageViewFullCircle.setVisibility(View.INVISIBLE);
                            vibrate(LocalSettings.mVibrationTime);
                            showPadView();
                            handler.removeCallbacks(showClockByDelay);
                            mPrevBoardView.stop(false);
                            bStarted = true;
                            if (mIsPreSwipeDragUp) {
                                mIsSwipeDragUp = true;
                                mPadPresenter.onHoverTouch(event, mWidth, mHeight, mMarginX, mMarginY);
                            }
                        } else if (!mIsPreSwipeDragUp) {
                            mLockImageViewFullCircle.setScaleX(distance / (100 * DPSCALE));
                            mLockImageViewFullCircle.setScaleY(distance / (100 * DPSCALE));
                            mLockImageViewCircle.setAlpha(distance / (100 * DPSCALE));
                            mLockImageViewFullCircle.setAlpha(((100 * DPSCALE) - distance) / (100 * DPSCALE));
                        }
                    } else if (mIsSwipeDragUp) {
                        mPadPresenter.onHoverTouch(event, mWidth, mHeight, mMarginX, mMarginY);
                    }
                }
                break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL: {


                    handler.removeCallbacks(showClockByDelay);
                    mPrevBoardView.stop(true);
                    if (bStarted && mIsSwipeDragUp) {
                        mPadPresenter.onHoverTouch(event, mWidth, mHeight, mMarginX, mMarginY);
                    }
                    mLockImageViewCircle.setVisibility(View.INVISIBLE);
                    mLockImageViewFullCircle.setVisibility(View.INVISIBLE);
                    bStarted = false;
                }
                break;
                default:
                    break;
            }
            return true;
        }

        private void vibrate(int time) {
            if (PhilPad.Settings.getBoolean(mContext.getContentResolver(), PhilPad.Settings.SETTINGS_KEY_PAD_HAPTIC_ENABLE, true)) {
                mVibrator.vibrate(time);
            }
        }
    }

    private void showPadView() {
        L.d("showPadView");

        updateHotspotViews();
        setHotspotVisible(false);
        mPadPresenter.registerQuickSettingReceiver();
        mPadPresenter.setEnable(true);
        mPadPresenter.updateCurrentApps(mContext);
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        return false;
    }

    @Override
    public IBinder onBind(Intent intent) {
        L.d("onBind");
        return mBinder;
    }

    IPhilPad.Stub mBinder = new IPhilPad.Stub() {

        @Override
        public int test(int a) throws RemoteException {
            return 0;
        }

        @Override
        public void notifyReDrawGridView() throws RemoteException {
            showPadView();
        }

        @Override
        public void reShowHotSpotViews(String type, boolean isCheck) throws RemoteException {
            reShowHotspotViews(type, isCheck);
        }

        @Override
        public void notiHotspotsSetting(boolean isStart) throws RemoteException {
            setHotspotTransparent(isStart);
        }

        @Override
        public void notiHotspotsVisible(boolean bVisible) throws RemoteException {
            setHotspotVisible(bVisible);
        }

        @Override
        public void notifyReDrawGridViewBackground() throws RemoteException {
            setGridViewBackground();
        }

        @Override
        public void setPremium(boolean enable) throws RemoteException {
            mPadPresenter.setPremium(enable);
        }

        @Override
        public void hotspotEnable(boolean enable) throws RemoteException {
            setHotspotViewsDetectEnable(enable);
        }

        public void setShowPadView(boolean enable) {
            if (enable == true) {

                final String[] hotspotsSharePref = mContext.getResources().getStringArray(R.array.hotspots_sharedpref);
                if (PhilPad.Settings.getBoolean(mContext.getContentResolver(), hotspotsSharePref[10], false) == false) {
                    mBoardView.setShowFromAssist(true);
                    PhilPad.Settings.putBoolean(mContext.getContentResolver(), hotspotsSharePref[10], true);
                    showHotspotViews();
                }
                mBoardView.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM);
                showPadView();
            }
        }
    };

    public void setHotspotTransparent(boolean isStart) {
        if (isStart) {
            for (int i = 0; i < mHotSpotView.length; i++) {
                mHotSpotView[i].setAlpha(0.5f);
                mHotSpotView[i].setBackground(R.drawable.no_bullet_sel_body);
            }
        } else {
            for (int i = 0; i < mHotSpotView.length; i++) {
                mHotSpotView[i].setBackground(R.drawable.no_bullet_normal);
                mHotSpotView[i].setAlpha(LocalSettings.mTrigerDefaultAlpha);
            }
        }
    }

    public void setHotspotVisible(boolean bVisible) {
        if (bVisible) {
            for (int i = 0; i < mHotSpotView.length; i++) {
                mHotSpotView[i].setAlpha(LocalSettings.mTrigerDefaultAlpha);
            }
        } else {
            for (int i = 0; i < mHotSpotView.length; i++) {
                mHotSpotView[i].setAlpha(0.0f);
            }
        }
    }


    public void setGridViewBackground() {

        mPadPresenter.setGridViewBackground(true);
    }


}