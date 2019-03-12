package com.philleeran.flicktoucher.view.pad;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.hardware.Camera;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.philleeran.flicktoucher.BuildConfig;
import com.philleeran.flicktoucher.PadUtils;
import com.philleeran.flicktoucher.PhilPadApplication;
import com.philleeran.flicktoucher.R;
import com.philleeran.flicktoucher.db.LocalSettings;
import com.philleeran.flicktoucher.db.PadItemInfo;
import com.philleeran.flicktoucher.db.PhilPad;
import com.philleeran.flicktoucher.service.PadNotificationListener;
import com.philleeran.flicktoucher.service.PhilPadService;
import com.philleeran.flicktoucher.utils.D;
import com.philleeran.flicktoucher.utils.L;
import com.philleeran.flicktoucher.utils.Utils;
import com.philleeran.flicktoucher.view.pad.adapter.BindListener;
import com.philleeran.flicktoucher.view.pad.adapter.PadGridViewAdapter;
import com.philleeran.flicktoucher.view.pad.adapter.PadGridViewAdapterContract;
import com.philleeran.flicktoucher.view.pad.command.AirplaneOnOff;
import com.philleeran.flicktoucher.view.pad.command.BackButton;
import com.philleeran.flicktoucher.view.pad.command.BluetoothOnOff;
import com.philleeran.flicktoucher.view.pad.command.FlashLightOnOff;
import com.philleeran.flicktoucher.view.pad.command.GoHome;
import com.philleeran.flicktoucher.view.pad.command.GoLastApp;
import com.philleeran.flicktoucher.view.pad.command.GoSettings;
import com.philleeran.flicktoucher.view.pad.command.HotspotDisable;
import com.philleeran.flicktoucher.view.pad.command.KillBackgroundProcess;
import com.philleeran.flicktoucher.view.pad.command.LaunchApplication;
import com.philleeran.flicktoucher.view.pad.command.MediaNext;
import com.philleeran.flicktoucher.view.pad.command.MediaPause;
import com.philleeran.flicktoucher.view.pad.command.MediaPlay;
import com.philleeran.flicktoucher.view.pad.command.MediaPrev;
import com.philleeran.flicktoucher.view.pad.command.MediaStop;
import com.philleeran.flicktoucher.view.pad.command.RecentAppInPad;
import com.philleeran.flicktoucher.view.pad.command.RotationOnOff;
import com.philleeran.flicktoucher.view.pad.command.ShowAppContext;
import com.philleeran.flicktoucher.view.pad.command.ShowIndicator;
import com.philleeran.flicktoucher.view.pad.command.ShowStatusBar;
import com.philleeran.flicktoucher.view.pad.command.VolumeDown;
import com.philleeran.flicktoucher.view.pad.command.VolumeUp;
import com.philleeran.flicktoucher.view.pad.command.WifiOnOff;
import com.philleeran.flicktoucher.view.pad.listener.FlashLightListener;
import com.philleeran.flicktoucher.view.pad.listener.TopPackageUpdateListener;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Stack;


public class PadPresenter implements PadContract.Presenter, TopPackageUpdateListener, BindListener {
    private final PhilPadService mPhilPadService;
    private final int mPadSize;
    private final String mDirectoryPath;

    public Stack<Integer> groupHistoryStack = new Stack<>();
    public boolean mFlashEnable = false;
    private boolean mWifiEnable = false;
    private boolean mRotateEnable = false;
    private boolean mBluetoothEnable = false;
    private boolean mAirplaneEnable = false;
    private Context mContext;
    private PadContract.View mView;
    private boolean mDragMode = false;

    private boolean mLongGestureMode = false;
    private boolean mIsPremium;

    private PadGridViewAdapter mPadGridPagerAdapter;

    final static private int HANDLER_REMOVE_ONLONG_CLICK = 0;

    private int mGestureCommand = D.Gesture.Command.GESTURE_NONE;

    final static private int HANDLER_REMOVE_ONLONG_PRESS_GESTURE = 1;

    final static private int HANDLER_ONLONG_CLICK = 2;

    final static private int HANDLER_ONCLICK = 3;

    final static private int HANDLER_ONCLICK_ONHOVER = 4;

    final static private int HANDLER_ONLONG_PRESS_GESTURE = 5;

    final static private int HANDLER_ON_GESTURE = 6;

    final static private int HANDLER_HOVER_ON_SELECTED = 7;


    private int mGroupIdOld;

    private int mGroupId;

    private int mStartPosition;

    private int mCurrentPosition;


    private GridView mGridView;

    private int mX;

    private int mY;

    private int mDownX;

    private int mDownY;

    private boolean bIsClick = false;

    private int mHoverCurrentPosition = -1;

    private int mHoverPrevPosition = -1;

    public float DPSCALE;

    public int DISPLAY_WIDTH;

    public int DISPLAY_HEIGHT;
    private int mGravity;
    private boolean mHoverEnable = false;
    private long mPrevTime;

    private String mTopPackageName;

    PadGridViewAdapterContract.Model mPadGridViewAdapterModel;
    PadGridViewAdapterContract.View mPadGridViewAdapterView;

    public PadPresenter(Context context, PhilPadService service) {

        mContext = context;
        mPhilPadService = service;

        DPSCALE = mContext.getResources().getDisplayMetrics().density;

        DISPLAY_WIDTH = mContext.getResources().getDisplayMetrics().widthPixels;

        DISPLAY_HEIGHT = mContext.getResources().getDisplayMetrics().heightPixels;
        mPadSize = PhilPad.Settings.getInt(mContext.getContentResolver(), PhilPad.Settings.SETTINGS_KEY_PAD_SIZE, 4);

        ContextWrapper cw = new ContextWrapper(mContext.getApplicationContext());
        mDirectoryPath = cw.getDir(D.DIR_PATH_DEFAULT, Context.MODE_PRIVATE).getPath();
    }


    private Camera mCamera;
    private Camera.Parameters mCameraParameters;


    @Override
    public void updateCurrentApps(Context context) {
        new Thread(new UpdateCurrentApps(context, mPadSize, this), "update current apps").start();
    }

    @Override
    public void registerQuickSettingReceiver() {
        L.d("registerQuickSettingReceiver");
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        mContext.registerReceiver(settingReceiver, filter);
        mRotateEnable = getRotateEnable();
        mBluetoothEnable = getBluetoothEnable();
        mAirplaneEnable = getAirplaneEnable();

    }

    private boolean getRotateEnable() {
        mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor(Settings.System.ACCELEROMETER_ROTATION), false, mRotationObserver);
        if (Settings.System.getInt(mContext.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0) == 1) {
            return true;
        } else {
            return false;
        }
    }

    private boolean getBluetoothEnable() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            // Device does not support Bluetooth
            return false;
        } else {
            if (bluetoothAdapter.isEnabled()) {
                return true;
            } else {
                return false;
            }
        }
    }

    private boolean getAirplaneEnable() {
        return Settings.System.getInt(mContext.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0) == 1;
    }

    @Override
    public void unregisterQuickSettingReceiver() {
        try {
            mContext.unregisterReceiver(settingReceiver);
            mContext.getContentResolver().unregisterContentObserver(mRotationObserver);
        } catch (IllegalArgumentException e) {
            L.e(e);
        }
    }

    @Override
    public void updateConfiguration(float dpscale, int width, int height) {
        DPSCALE = dpscale;
        DISPLAY_WIDTH = width;
        DISPLAY_HEIGHT = height;
    }

    @Override
    public void setShowPopupEnable(boolean b) {
        mView.setShowPopupEnable(b);
    }

    @Override
    public void setEnable(boolean b) {
        mView.updateLayoutParams(Gravity.RIGHT | Gravity.TOP);
        mView.setVisibility(View.VISIBLE);
    }

    @Override
    public void attachView(PadContract.View view) {
        mView = view;
    }

    @Override
    public void detachView() {
        mView = null;
    }

    @Override
    public void setGravity(int mGravity) {
        mView.setGravity(mGravity);
    }

    @Override
    public void setGridViewBackground(boolean b) {
        mView.setGridViewBackground(b);
    }

    @Override
    public void setPremium(boolean enable) {
        mIsPremium = enable;
    }

    @Override
    public void onHoverTouch(MotionEvent event, int width, int height, int marginX, int marginY) {

        switch (event.getAction()) {

            case MotionEvent.ACTION_MOVE:
                mHoverEnable = true;
                mGravity = mView.getGravity();
                if (mGravity == (Gravity.LEFT | Gravity.BOTTOM)) {
                    mX = (int) event.getX() - (int) (15 * DPSCALE) + marginX;
                    mY = (int) event.getY() + mGridView.getHeight() + (int) (15 * DPSCALE) - (DISPLAY_HEIGHT - marginY);
                } else if (mGravity == (Gravity.RIGHT | Gravity.BOTTOM)) {
                    mX = (int) event.getX() - DISPLAY_WIDTH + mGridView.getWidth() + (int) (15 * DPSCALE) + marginX;
                    mY = (int) event.getY() + mGridView.getHeight() + (int) (15 * DPSCALE) - (DISPLAY_HEIGHT - marginY);
                } else if (mGravity == (Gravity.LEFT | Gravity.TOP)) {
                    mX = (int) event.getX() - (int) (15 * DPSCALE) + marginX;
                    mY = (int) event.getY() - Utils.getStatusBarHeight(mContext) + marginY;
                } else if (mGravity == (Gravity.RIGHT | Gravity.TOP)) {
                    mX = (int) event.getX() - DISPLAY_WIDTH + mGridView.getWidth() + (int) (15 * DPSCALE) + marginX;
                    mY = (int) event.getY() - Utils.getStatusBarHeight(mContext) + marginY;
                } else if (mGravity == (Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM)) {
                    mX = (int) event.getX() + mGridView.getWidth() / 2 + marginX - DISPLAY_WIDTH / 2;
                    mY = (int) event.getY() + mGridView.getHeight() + (int) (15 * DPSCALE) - (DISPLAY_HEIGHT - marginY);
                } else if (mGravity == (Gravity.CENTER_VERTICAL | Gravity.LEFT)) {
                    mX = (int) event.getX() - (int) (15 * DPSCALE) + marginX;
                    mY = (int) event.getY() + mGridView.getHeight() / 2 + marginY - DISPLAY_HEIGHT / 2;
                } else if (mGravity == (Gravity.CENTER_VERTICAL | Gravity.RIGHT)) {
                    mX = (int) event.getX() - DISPLAY_WIDTH + mGridView.getWidth() + (int) (15 * DPSCALE) + marginX;
                    mY = (int) event.getY() + mGridView.getHeight() / 2 + marginY - DISPLAY_HEIGHT / 2;
                }
                mHoverCurrentPosition = mGridView.pointToPosition(mX, mY);
                if (mHoverCurrentPosition != mHoverPrevPosition) {
                    if (mHoverCurrentPosition != -1) {
                        mGridView.getChildAt(mHoverCurrentPosition).findViewById(R.id.item_image_background_forselect).setBackgroundResource(android.R.color.white);
                        mHandler.removeMessages(HANDLER_ONLONG_CLICK);
                        mHandler.removeMessages(HANDLER_HOVER_ON_SELECTED);
                        Message msg1 = new Message();
                        msg1.what = HANDLER_HOVER_ON_SELECTED;
                        mHandler.sendMessageDelayed(msg1, LocalSettings.mHoverSelectDelay);
                        Message msg2 = new Message();
                        msg2.what = HANDLER_ONLONG_CLICK;
                        mHandler.sendMessageDelayed(msg2, 1500);
                        mView.setShowPopupEnable(false);
                        mPrevTime = System.currentTimeMillis();
                    } else {
                        mHandler.removeMessages(HANDLER_ONLONG_CLICK);
                        mHandler.removeMessages(HANDLER_HOVER_ON_SELECTED);

                        if (mX < 0 || mY < 0 || mX > mGridView.getWidth() || mY > mGridView.getHeight()) {
                            L.d("out of Hover ");
                        }
                    }
                    if (mHoverPrevPosition != -1) {
                        try {
                            mGridView.getChildAt(mHoverPrevPosition).findViewById(R.id.item_image_background_forselect).setBackgroundColor(Color.TRANSPARENT);
                        } catch (NullPointerException e) {
                            L.e(e);
                        }
                    }
                    mHoverPrevPosition = mHoverCurrentPosition;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                try {
                    mGridView.getChildAt(mHoverPrevPosition).findViewById(R.id.item_image_background_forselect).setBackgroundColor(Color.TRANSPARENT);
                } catch (NullPointerException e) {
                    L.e(e);
                }

                if (mView.getShowPopupEnable() == true) {
                    if (mHoverEnable && mHoverCurrentPosition != -1) {
                        mStartPosition = mHoverCurrentPosition;
                        mHoverCurrentPosition = -1;
                        if (mGroupId == D.GROUPID_RECENT) {
                            mView.showOptionMenu(true);
                        } else {
                            mView.showOptionMenu(false);
                        }
                    }
                } else {
                    mHoverEnable = false;
                    mHandler.removeMessages(HANDLER_ONLONG_CLICK);
                    mHandler.removeMessages(HANDLER_HOVER_ON_SELECTED);

                    if (mHoverPrevPosition != -1) {
                        mStartPosition = mHoverPrevPosition;

                        mGravity = mView.getGravity();
                        if (mGravity == (Gravity.LEFT | Gravity.BOTTOM)) {
                            mX = (int) event.getX() - (int) (15 * DPSCALE) + marginX;
                            mY = (int) event.getY() + mGridView.getHeight() + (int) (15 * DPSCALE) - (DISPLAY_HEIGHT - marginY);
                        } else if (mGravity == (Gravity.RIGHT | Gravity.BOTTOM)) {
                            mX = (int) event.getX() - DISPLAY_WIDTH + mGridView.getWidth() + (int) (15 * DPSCALE) + marginX;
                            mY = (int) event.getY() + mGridView.getHeight() + (int) (15 * DPSCALE) - (DISPLAY_HEIGHT - marginY);
                        } else if (mGravity == (Gravity.LEFT | Gravity.TOP)) {
                            mX = (int) event.getX() - (int) (15 * DPSCALE) + marginX;
                            mY = (int) event.getY() - Utils.getStatusBarHeight(mContext) + marginY;
                        } else if (mGravity == (Gravity.RIGHT | Gravity.TOP)) {
                            mX = (int) event.getX() - DISPLAY_WIDTH + mGridView.getWidth() + (int) (15 * DPSCALE) + marginX;
                            mY = (int) event.getY() - Utils.getStatusBarHeight(mContext) + marginY;
                        } else if (mGravity == (Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM)) {
                            mX = (int) event.getX() + mGridView.getWidth() / 2 + marginX - DISPLAY_WIDTH / 2;
                            mY = (int) event.getY() + mGridView.getHeight() + (int) (15 * DPSCALE) - (DISPLAY_HEIGHT - marginY);
                        } else if (mGravity == (Gravity.CENTER_VERTICAL | Gravity.LEFT)) {
                            mX = (int) event.getX() - (int) (15 * DPSCALE) + marginX;
                            mY = (int) event.getY() + mGridView.getHeight() / 2 + marginY - DISPLAY_HEIGHT / 2;
                        } else if (mGravity == (Gravity.CENTER_VERTICAL | Gravity.RIGHT)) {
                            mX = (int) event.getX() - DISPLAY_WIDTH + mGridView.getWidth() + (int) (15 * DPSCALE) + marginX;
                            mY = (int) event.getY() + mGridView.getHeight() / 2 + marginY - DISPLAY_HEIGHT / 2;
                        }
                        if ((System.currentTimeMillis() - mPrevTime) > LocalSettings.mHoverSelectDelay) {
                            mHandler.sendEmptyMessage(HANDLER_ONCLICK_ONHOVER);
                        }
                    } else {
                        if (mHoverCurrentPosition != -1) {
//                                startLaunchAnimation(mGroupId, mStartPosition);
                            mView.setHideAndGroupIdInit();
                        } else {
                            mView.setHideAndGroupIdInit();
                        }
                    }
                }
        }
    }

    @Override
    public void setGridView(DragDropGridView gridView, int groupId, PadGridViewAdapter padGridPagerAdapter) {
        mGroupIdOld = mGroupId = groupId;
        mPadGridPagerAdapter = padGridPagerAdapter;
        mGridView = gridView;
    }

    @Override
    public void showToast(String message) {
        mView.showToast(message);
    }


    private ContentObserver mRotationObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            if (android.provider.Settings.System.getInt(mContext.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0) == 1) {
                mRotateEnable = true;
            } else {
                mRotateEnable = false;
            }
            mView.invalidateViews();

            L.d("mRotationObserver");
        }
    };


    private BroadcastReceiver settingReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            L.d("settingReceiver action : " + action);
            if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
                int extraWifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,
                        WifiManager.WIFI_STATE_UNKNOWN);
                switch (extraWifiState) {
                    case WifiManager.WIFI_STATE_DISABLED:
                        mWifiEnable = false;
                        mView.invalidateViews();
                        L.d("settingReceiver WIFI STATE DISABLED");
                        break;
                    case WifiManager.WIFI_STATE_DISABLING:
                        L.d("settingReceiver WIFI STATE DISABLING");
                        break;
                    case WifiManager.WIFI_STATE_ENABLED:
                        mWifiEnable = true;
                        mView.invalidateViews();
                        L.d("settingReceiver WIFI STATE ENABLED");
                        break;
                    case WifiManager.WIFI_STATE_ENABLING:
                        L.d("settingReceiver WIFI STATE ENABLING");
                        break;
                    case WifiManager.WIFI_STATE_UNKNOWN:
                        L.d("settingReceiver WIFI STATE UNKNOWN");
                        break;
                }
            }
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                if (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1) == BluetoothAdapter.STATE_OFF) {
                    mBluetoothEnable = false;
                } else {
                    mBluetoothEnable = true;
                }
                mView.invalidateViews();
            }
            if (Intent.ACTION_AIRPLANE_MODE_CHANGED.equals(action)) {
                L.d("ACTION_AIRPLANE_MODE_CHANGED");
                boolean state = intent.getBooleanExtra("state", false);
                if (state == true) {
                    mAirplaneEnable = true;
                } else {
                    mAirplaneEnable = false;
                }
            }
        }
    };

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            boolean isHover = false;
            isHover = false;
            switch (msg.what) {
                case HANDLER_HOVER_ON_SELECTED:
                    mView.vibrate(LocalSettings.mVibrationTime);
                    break;
                case HANDLER_REMOVE_ONLONG_CLICK:
                    removeMessages(HANDLER_ONLONG_CLICK);
                    break;
                case HANDLER_REMOVE_ONLONG_PRESS_GESTURE:
                    removeMessages(HANDLER_ONLONG_PRESS_GESTURE);
                    break;
                case HANDLER_ONLONG_CLICK: {
                    L.d("HANDLER_ONLONGCLICK");
                    mView.vibrate(LocalSettings.mVibrationTime);
                    bIsClick = false;
                    if (mHoverEnable) {
                        mView.setShowPopupEnable(true);
                        if (mHoverCurrentPosition == -1) {
                            if (mGroupId == D.GROUPID_RECENT) {
                                mView.showOptionMenu(true);
                            } else {
                                mView.showOptionMenu(false);
                            }
                        }
                    } else {
                        if (mGroupId == D.GROUPID_RECENT) {
                            mView.showOptionMenu(true);
                        } else {
                            mView.showOptionMenu(false);
                        }
                    }
                }
                break;
                case HANDLER_ONCLICK_ONHOVER:
                    //isHover = true;
                    L.d("HANDLER_ONCLICK_ONHOVER");
                case HANDLER_ONCLICK: {

                    if (mGroupId == D.GROUPID_NOTIFICATION) {
                        PadItemInfo info = PhilPad.Pads.getPadItemInfo(mContext.getContentResolver(), mGroupId, mStartPosition);
                        mContext.sendBroadcast(new Intent(PadNotificationListener.ACTION_LAUNCH).putExtra(PadNotificationListener.EXTRA_KEY, info.getExtraData()));
                        mView.setHideAndGroupIdInit();
                        return;
                    }

                    L.d("HANDLER_ONCLICK");
                    ContentResolver resolver = mContext.getContentResolver();
                    PadItemInfo info = PhilPad.Pads.getPadItemInfo(resolver, mGroupId, mStartPosition);
                    String packageName = null;
                    if (info != null) {
                        int type = info.getType();
                        if (PhilPad.Pads.PAD_TYPE_NULL == type) {
                            if (isHover) {
                                mHandler.sendEmptyMessage(HANDLER_ONLONG_CLICK);
                            } else if (mGroupIdOld == 0 || mGroupIdOld == 1) {
                                if (isHistoryStackEmpty() == false) {

                                    int preGroupId = popHistoryStack();
                                    mView.startAnimation(false, 0, preGroupId);
                                } else {
                                    mView.setHideAndGroupIdInit();
                                }
                            } else {
                                mView.setHideAndGroupIdInit();
                            }
                        } else if (PhilPad.Pads.PAD_TYPE_APPLICATION == type) {
                            new LaunchApplication(mContext, info.getPackageName(), mTopPackageName).execute();
                            mView.setHideAndGroupIdInit();
                            //TODO
                        } else if (PhilPad.Pads.PAD_TYPE_GROUP == type) {
                            mGridView.setScaleX(1.0f);
                            mGridView.setScaleY(1.0f);
                            pushHistoryStack(mGroupId);
                            int groupId = info.getToGroupId();
                            mView.startAnimation(true, mStartPosition, groupId);
                            // mPadGridPagerAdapter.notifyDataSetChanged();
                        } else if (PhilPad.Pads.PAD_TYPE_TOOLS == type) {
                            packageName = info.getExtraData();

                            Message msg1 = new Message();
                            msg1.what = Integer.parseInt(packageName);
                            msg1.obj = info.getPackageName();

                            mFunctionHandler.sendMessage(msg1);
                            // mView.setHideAndGroupIdInit();
                        } else if (PhilPad.Pads.PAD_TYPE_SHORTCUT == type) {
                            String intentDescription = info.getExtraData();
                            try {
                                Intent intent = Intent.parseUri(intentDescription, 0);
                                if (intent.getAction().equals("android.intent.action.CALL_PRIVILEGED")) {
                                    intent.setAction(Intent.ACTION_CALL);
                                }
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                mContext.startActivity(intent);

                            } catch (URISyntaxException e) {
                                e.printStackTrace();
                            }
                            mView.setHideAndGroupIdInit();
                            // startLaunchAnimation(mGroupId,
                            // mStartPosition);
                            // mView.setHideAndGroupIdInit();
                        } else if (PhilPad.Pads.PAD_TYPE_WIDGET == type) {
                            String extraData = info.getExtraData();
                            int appWidgetId = Integer.parseInt(extraData);
                            Intent intent = new Intent();
                            ComponentName component = new ComponentName("com.philleeran.flicktoucher", "com.philleeran.flicktoucher.view.widget.WidgetViewActivity");
                            intent.setComponent(component);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                            intent.putExtra("app.widget.id", appWidgetId);
                            mContext.startActivity(intent);
                            mView.setHideAndGroupIdInit();
                        } else if (PhilPad.Pads.PAD_TYPE_FILEOPEN == type) {

                            try {
                                String[] word = info.getExtraData().split("\\|");
                                String extraData = word[0];
                                String mimeTypeString = word[1];

                                if (TextUtils.isEmpty(extraData) == false && TextUtils.isEmpty(mimeTypeString) == false) {
                                    File file = new File(extraData);
                                    Intent i = new Intent();
                                    i.setAction(android.content.Intent.ACTION_VIEW);
                                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                        Uri photoURI = FileProvider.getUriForFile(mContext, BuildConfig.APPLICATION_ID + ".provider", file);
                                        i.setDataAndType(photoURI, mimeTypeString);
                                        i.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                    } else {
                                        i.setDataAndType(Uri.fromFile(file), mimeTypeString);
                                    }
                                    mContext.startActivity(i);
                                    mView.setHideAndGroupIdInit();
                                }
                            } catch (Exception e) {
                                L.e(e);
                                Toast.makeText(mContext, R.string.remakeplease, Toast.LENGTH_LONG).show();
                                mView.setHideAndGroupIdInit();
                            }
                        } else if (PhilPad.Pads.PAD_TYPE_CONTACT == type) {
                            try {
                                Intent intent = new Intent(Intent.ACTION_CALL);
                                intent.setData(Uri.parse(info.getExtraData()));
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                //TODO
                                mContext.startActivity(intent);
                                mView.setHideAndGroupIdInit();
                            } catch (SecurityException e) {
                                Toast.makeText(mContext, R.string.there_is_no_permission_to_read_contacts, Toast.LENGTH_LONG).show();
                            }
                        }
                    } else {
                        if (mGroupIdOld == 0 || mGroupIdOld == 1) {
                            if (isHistoryStackEmpty() == false) {
                                int preGroupId = popHistoryStack();
                                mView.startAnimation(false, 0, preGroupId);
                            } else {
                                mView.setHideAndGroupIdInit();
                            }
                        } else {
                            mView.setHideAndGroupIdInit();
                        }
                    }
                }
                break;
                case HANDLER_ONLONG_PRESS_GESTURE: {
                    // bIsClick = false;
                    mLongGestureMode = true;
                    // mGestureCommand = GESTURE_NONE;
                    mView.vibrate(LocalSettings.mVibrationTime);
                }
                break;

                case HANDLER_ON_GESTURE:
                    //TODO
                {
                    ContentResolver resolver = mContext.getContentResolver();
                    if (mGestureCommand != -1) {
                        PadItemInfo info = PhilPad.Pads.getPadItemInfo(resolver, D.GROUPID_GESTURE, mGestureCommand);
                        String packageName = null;
                        if (info != null) {
                            int type = info.getType();
                            if (PhilPad.Pads.PAD_TYPE_NULL == type) {

                                if (mGroupIdOld == D.GROUPID_GROUND) {
                                    if (groupHistoryStack.isEmpty() == false) {
                                        int preGroupId = groupHistoryStack.pop();
                                        setGroupId(preGroupId);
                                        mPadGridPagerAdapter.setGroupId(preGroupId, mPadSize);
                                    } else {
                                        //mView.setHideAndGroupIdInit();
                                    }
                                } else {
                                    mView.setHideAndGroupIdInit();
                                }

                            } else if (PhilPad.Pads.PAD_TYPE_APPLICATION == type) {
                                new LaunchApplication(mContext, info.getPackageName(), mTopPackageName).execute();
                                mView.setHideAndGroupIdInit();
                            } else if (PhilPad.Pads.PAD_TYPE_GROUP == type) {
                                mGridView.setScaleX(1.0f);
                                mGridView.setScaleY(1.0f);
                                pushHistoryStack(mGroupId);
                                int groupId = info.getToGroupId();
                                mPadGridPagerAdapter.setGroupId(groupId, mPadSize);
                                setGroupId(groupId);

                                mView.startAnimation(true, mStartPosition, groupId);
                                // mPadGridPagerAdapter.notifyDataSetChanged();
                            } else if (PhilPad.Pads.PAD_TYPE_TOOLS == type) {
                                packageName = info.getExtraData();
                                Message msg1 = new Message();
                                msg1.what = Integer.parseInt(packageName);
                                msg1.obj = info.getExtraData();
                                mFunctionHandler.sendMessage(msg1);

//                                    mView.setHideAndGroupIdInit(false);
                                // mView.setHideAndGroupIdInit();
                            } else if (PhilPad.Pads.PAD_TYPE_SHORTCUT == type) {
                                String intentDescription = info.getExtraData();
                                try {
                                    Intent intent = Intent.parseUri(intentDescription, 0);
                                    if (intent.getAction().equals("android.intent.action.CALL_PRIVILEGED")) {
                                        intent.setAction(Intent.ACTION_CALL);
                                    }
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    mContext.startActivity(intent);

                                } catch (URISyntaxException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }
                                mView.setHideAndGroupIdInit();
                                // startLaunchAnimation(mGroupId,
                                // mStartPosition);
                                // mView.setHideAndGroupIdInit();
                            } else if (PhilPad.Pads.PAD_TYPE_WIDGET == type) {
                                String extraData = info.getExtraData();
                                int appWidgetId = Integer.parseInt(extraData);
                                Intent intent = new Intent();
                                ComponentName component = new ComponentName("com.philleeran.flicktoucher", "com.philleeran.flicktoucher.view.widget.WidgetViewActivity");
                                intent.setComponent(component);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                                intent.putExtra("app.widget.id", appWidgetId);
                                mContext.startActivity(intent);
                                // startLaunchAnimation(mGroupId,
                                // mStartPosition);
                                mView.setHideAndGroupIdInit();
                            } else if (PhilPad.Pads.PAD_TYPE_FILEOPEN == type) {

                                try {
                                    String extraData = null;
                                    String mimetype = null;

                                    String[] word = info.getExtraData().split("\\|");
                                    extraData = word[0];
                                    mimetype = word[1];

                                    if (TextUtils.isEmpty(extraData) == false && TextUtils.isEmpty(mimetype) == false) {
                                        File file = new File(extraData);
                                        Intent i = new Intent();
                                        i.setAction(android.content.Intent.ACTION_VIEW);
                                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        i.setDataAndType(Uri.fromFile(file), mimetype);
                                        mContext.startActivity(i);
                                        mView.setHideAndGroupIdInit();
                                    }
                                } catch (Exception e) {
                                    L.e(e);
                                    Toast.makeText(mContext, R.string.remakeplease, Toast.LENGTH_LONG).show();
                                    mView.setHideAndGroupIdInit();
                                }
                                // startLaunchAnimation(mGroupId,
                                // mStartPosition);
                            }
                        } else {

                            if (mGroupIdOld == D.GROUPID_GROUND) {
                                if (groupHistoryStack.isEmpty() == false) {
                                    int preGroupId = groupHistoryStack.pop();
                                    setGroupId(preGroupId);
                                    mPadGridPagerAdapter.setGroupId(preGroupId, mPadSize);
                                    mView.startAnimation(false, mStartPosition, 0);
                                    // mPadGridPagerAdapter.notifyDataSetChanged();
                                } else {
                                    mView.setHideAndGroupIdInit();
                                }
                            } else {
                                mView.setHideAndGroupIdInit();
                            }

                        }
                    }
                }
                break;
            }
        }

    };

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDownX = (int) event.getX();
                mDownY = (int) event.getY();
                //TODO back
                mGestureCommand = D.Gesture.Command.GESTURE_NONE;
                mStartPosition = mGridView.pointToPosition(mDownX, mDownY);

                if (mDragMode == false) {
                    mGridView.getChildAt(mStartPosition).findViewById(R.id.item_image_background_forselect).setBackgroundResource(android.R.color.white);
                    mHandler.sendEmptyMessageDelayed(HANDLER_ONLONG_CLICK, 1000);
                } else {
                    ((DragDropGridView) mGridView).doDrag(mStartPosition);
                    return false;
                }
                bIsClick = true;

                break;
            case MotionEvent.ACTION_MOVE:
                mX = (int) event.getX();
                mY = (int) event.getY();
                if (bIsClick && mStartPosition != mGridView.pointToPosition(mX, mY)) {
                    mGridView.getChildAt(mStartPosition).findViewById(R.id.item_image_background_forselect).setBackgroundResource(android.R.color.white);

                    bIsClick = false;
                    mHandler.sendEmptyMessage(HANDLER_REMOVE_ONLONG_CLICK);
                } else if (PhilPad.Settings.getBoolean(mContext.getContentResolver(), PhilPad.Settings.SETTINGS_KEY_PAD_GESTURE_ENABLE, true) == true) {
                    mCurrentPosition = mGridView.pointToPosition(mX, mY);
                    int distance = PadUtils.getDistance((int) mDownX, (int) mDownY, (int) mX, (int) mY);

                    if (distance > 50 * DPSCALE && mDragMode == false) {
                        int diffX = mX - mDownX;
                        int diffY = mY - mDownY;
                        //TODO back
                        if (Math.abs(diffX) > Math.abs(diffY)) {
                            if (diffX > 0) {
                                // right
                                if (mGestureCommand != D.Gesture.Command.GESTURE_RIGHT) {
                                    mLongGestureMode = false;
                                    mView.vibrate(LocalSettings.mVibrationTime);
                                    mGestureCommand = D.Gesture.Command.GESTURE_RIGHT;
                                    mHandler.removeMessages(HANDLER_ONLONG_PRESS_GESTURE);
                                    mHandler.sendEmptyMessageDelayed(HANDLER_ONLONG_PRESS_GESTURE, 2000);
                                    L.d("gesture right");
                                    PadUtils.setSlideAnimation(mContext, mView.getGridViewBackground(), mGestureCommand, (int) (30 * DPSCALE));
                                }
                            } else {
                                // left
                                if (mGestureCommand != D.Gesture.Command.GESTURE_LEFT) {
                                    mLongGestureMode = false;
                                    mView.vibrate(LocalSettings.mVibrationTime);
                                    mGestureCommand = D.Gesture.Command.GESTURE_LEFT;
                                    mHandler.removeMessages(HANDLER_ONLONG_PRESS_GESTURE);
                                    mHandler.sendEmptyMessageDelayed(HANDLER_ONLONG_PRESS_GESTURE, 2000);
                                    L.d("gesture left");
                                    PadUtils.setSlideAnimation(mContext, mView.getGridViewBackground(), mGestureCommand, (int) (30 * DPSCALE));
                                }
                            }
                        } else {
                            if (diffY > 0) {
                                if (mGestureCommand != D.Gesture.Command.GESTURE_DOWN) {
                                    mLongGestureMode = false;
                                    mView.vibrate(LocalSettings.mVibrationTime);
                                    mGestureCommand = D.Gesture.Command.GESTURE_DOWN;
                                    mHandler.removeMessages(HANDLER_ONLONG_PRESS_GESTURE);
                                    mHandler.sendEmptyMessageDelayed(HANDLER_ONLONG_PRESS_GESTURE, 2000);
                                    L.d("gesture down");
                                    PadUtils.setSlideAnimation(mContext, mView.getGridViewBackground(), mGestureCommand, (int) (30 * DPSCALE));
                                    // }
                                }
                            } else {
                                // top
                                if (mGestureCommand != D.Gesture.Command.GESTURE_UP) {
                                    mLongGestureMode = false;
                                    mView.vibrate(LocalSettings.mVibrationTime);
                                    mHandler.removeMessages(HANDLER_ONLONG_PRESS_GESTURE);
                                    mHandler.sendEmptyMessageDelayed(HANDLER_ONLONG_PRESS_GESTURE, 2000);
                                    mGestureCommand = D.Gesture.Command.GESTURE_UP;
                                    L.d("gesture top");
                                    PadUtils.setSlideAnimation(mContext, mView.getGridViewBackground(), mGestureCommand, (int) (30 * DPSCALE));
                                }
                            }
                        }
                    } else {
                        mLongGestureMode = false;

                        //TODO back
                        if (mGestureCommand != D.Gesture.Command.GESTURE_NONE) {
                            mView.vibrate(LocalSettings.mVibrationTime);
                            mGestureCommand = D.Gesture.Command.GESTURE_NONE;
                            mHandler.removeMessages(HANDLER_ONLONG_PRESS_GESTURE);
                            mView.setGridViewBackgroundVisibility(View.INVISIBLE);
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:

                mGridView.getChildAt(mStartPosition).findViewById(R.id.item_image_background_forselect).setBackgroundColor(Color.TRANSPARENT);

                mView.setGridViewBackgroundVisibility(View.INVISIBLE);

                if (bIsClick) {
                    mLongGestureMode = false;
                    // onclick
                    mHandler.sendEmptyMessage(HANDLER_REMOVE_ONLONG_CLICK);

                    mX = (int) event.getX();
                    mY = (int) event.getY();

                    mHandler.sendEmptyMessage(HANDLER_ONCLICK);

                } else if (mLongGestureMode == true) {
                    mLongGestureMode = false;
                    mView.setGridViewBackgroundVisibility(View.INVISIBLE);

                    mView.showOptionMenuGesture(mGestureCommand);

                } else if (mLongGestureMode == false) {
                    mHandler.removeMessages(HANDLER_ONLONG_PRESS_GESTURE);
                    mLongGestureMode = false;
                    //TODO back
                    if (mGestureCommand != D.Gesture.Command.GESTURE_NONE) {
                        mHandler.sendEmptyMessage(HANDLER_ON_GESTURE);
                    }
                }
                mView.setGridViewBackgroundVisibility(View.INVISIBLE);


                break;
            default:
                break;
        }
        return false;
    }


    @Override
    public void setGroupId(int groupId) {
        mGroupId = groupId;
    }

    @Override
    public void setDragMode(boolean b) {
        mDragMode = b;
        setGridViewBackground(!b);
    }

    @Override
    public void removePadItem() {
        PhilPad.Pads.removePadItem(mContext, mGroupId, mStartPosition);

    }

    @Override
    public void setGroupIcon() {
        PhilPad.Pads.setGroupIcon(mContext, mGroupId, mPadSize);
    }

    @Override
    public int getPadSize() {
        return mPadSize;
    }

    @Override
    public float getDpScale() {
        return DPSCALE;
    }

    @Override
    public boolean getDragMode() {
        return mDragMode;
    }

    @Override
    public Integer getGroupId() {
        return mGroupId;
    }

    @Override
    public int getStartPosition() {
        return mStartPosition;
    }

    @Override
    public String getTopPackageName() {
        return mTopPackageName;
    }

    @Override
    public void setPadGridViewAdapterModel(PadGridViewAdapterContract.Model mPadGridPagerAdapter) {
        mPadGridViewAdapterModel = mPadGridPagerAdapter;
    }

    @Override
    public void setPadGridViewAdapterView(PadGridViewAdapterContract.View mPadGridPagerAdapter) {
        mPadGridViewAdapterView = mPadGridPagerAdapter;
        mPadGridViewAdapterView.setBindListener(this);
    }

    @Override
    public void pushHistoryStack(int groupId) {
        groupHistoryStack.push(groupId);
    }

    @Override
    public int popHistoryStack() {
        return groupHistoryStack.pop();
    }

    @Override
    public boolean isHistoryStackEmpty() {
        return groupHistoryStack.isEmpty();
    }

    @Override
    public void clearHistoryStack() {
        groupHistoryStack.clear();
    }


    private final Handler mFunctionHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case D.Tools.TOOLS_TYPE_HOME: {
                    L.d("TOOLS_TYPE_HOME");
                    new GoHome(mContext).execute();
                    mView.setHideAndGroupIdInit();
                }
                break;
                case D.Tools.TOOLS_TYPE_INDICATOR: {
                    new ShowIndicator().execute();
                    mView.setHideAndGroupIdInit();
                }
                break;
                case D.Tools.TOOLS_TYPE_RECENTAPPLICATION: {
                    new ShowStatusBar().execute();
                    mView.setHideAndGroupIdInit();
                }
                break;

                case D.Tools.TOOLS_TYPE_CONTEXT: {
                    new ShowAppContext(mContext).execute();
                    mView.setHideAndGroupIdInit();
                }
                break;

                case D.Tools.TOOLS_TYPE_CLOSE: {
                    mView.setHideAndGroupIdInit();
                }
                break;

                case D.Tools.TOOLS_TYPE_PADSETTINGS: {
                    new GoSettings(mContext).execute();
                    mView.setHideAndGroupIdInit();
                }
                break;
                case D.Tools.TOOLS_TYPE_HOTSPOT_DETECT_DISABLE: {
                    new HotspotDisable(mPhilPadService).execute();
                    mView.setHideAndGroupIdInit();
                }
                break;
                case D.Tools.TOOLS_TYPE_RECENTAPPLICATION_INPAD: {
                    new RecentAppInPad(mContext, mView, PadPresenter.this, mGroupId, mStartPosition).execute();

                }
                break;

                case D.Tools.TOOLS_TYPE_LASTAPP: {
                    String packageName = (String) msg.obj;
                    new GoLastApp(mContext, packageName, mTopPackageName).execute();
                    mView.setHideAndGroupIdInit();
                }
                break;

                case D.Tools.TOOLS_TYPE_FLASH_ON_OFF: {

                    if (mCamera == null) {
                        mCamera = Camera.open();
                        mCameraParameters = mCamera.getParameters();
                    }

                    new FlashLightOnOff(mContext, mCameraParameters, new FlashLightListener() {
                        @Override
                        public void onFlashLightChanged(boolean enable) {
                            if (enable) {
                                mCamera.setParameters(mCameraParameters);
                                mCamera.startPreview();
                            } else {
                                mCamera.setParameters(mCameraParameters);
                                mCamera.stopPreview();
                                mCamera.release();
                                mCamera = null;
                            }
                            mFlashEnable = enable;
                        }
                    }).execute();

                    mGridView.invalidateViews();
                }
                break;
                case D.Tools.TOOLS_TYPE_WIFI_ON_OFF: {
                    new WifiOnOff(mContext).execute();
                }
                break;
                case D.Tools.TOOLS_TYPE_VOLUME_UP: {
                    new VolumeUp(mContext, PadPresenter.this).execute();
                }
                break;
                case D.Tools.TOOLS_TYPE_VOLUME_DOWN: {
                    new VolumeDown(mContext, PadPresenter.this).execute();
                }
                break;

                case D.Tools.TOOLS_TYPE_ROTATION_ON_OFF: {
                    new RotationOnOff(mContext, mView).execute();
                }
                break;

                case D.Tools.TOOLS_TYPE_BLUETOOTH_ON_OFF: {
                    new BluetoothOnOff().execute();
                }
                break;

                case D.Tools.TOOLS_TYPE_AIRPLANE_ON_OFF: {
                    new AirplaneOnOff(mContext).execute();
                    mView.setHideAndGroupIdInit();
                }
                break;


                case D.Tools.TOOLS_TYPE_BACK_BUTTON: {
                    new BackButton(mContext).execute();
                    mView.setVisibility(View.INVISIBLE);
                    mView.setHideAndGroupIdInit();
                }
                break;

                case D.Tools.TOOLS_TYPE_MEDIA_PLAY: {
                    new MediaPlay(mContext).execute();
                }
                break;

                case D.Tools.TOOLS_TYPE_MEDIA_PAUSE: {
                    new MediaPause(mContext).execute();
                }
                break;

                case D.Tools.TOOLS_TYPE_MEDIA_PREV: {
                    new MediaPrev(mContext).execute();
                }
                break;

                case D.Tools.TOOLS_TYPE_MEDIA_NEXT: {
                    new MediaNext(mContext).execute();
                }
                break;

                case D.Tools.TOOLS_TYPE_MEDIA_STOP: {
                    new MediaStop(mContext).execute();
                }
                break;
                case D.Tools.TOOLS_TYPE_KILL_BACKGROUND_APP: {
                    new KillBackgroundProcess(mContext).execute();
                    mView.setHideAndGroupIdInit();
                }
                default:
                    break;
            }
            PhilPadApplication.tracker().send(new HitBuilders.EventBuilder().setCategory("launch").setAction("function").setLabel("" + msg.what).build());
        }

    };


    @Override
    public void onTopPackageUpdated(String packageName) {
        mTopPackageName = packageName;
    }

    private Bitmap mRecentAppPadBitmap;

    @Override
    public void onRecentAppBitmapChanged(Bitmap bitmap) {

        if (mRecentAppPadBitmap != null) {
            if (D.RECYCLE) {
                mRecentAppPadBitmap.recycle();
            }
            mRecentAppPadBitmap = null;
        }
        mRecentAppPadBitmap = bitmap;
    }

    @Override
    public void bindView(View v, Context context, Cursor cursor) {
        int type = cursor.getInt(0);//cursor.getColumnIndex(PhilPad.Pads.COLUMN_NAME_TYPE)
        ImageView imageView = (ImageView) v.findViewById(R.id.item_image);
        ImageView imageViewForSelect = (ImageView) v.findViewById(R.id.item_image_background_forselect);
        TextView itemName = (TextView) v.findViewById(R.id.item_name);
        imageViewForSelect.setBackgroundColor(Color.TRANSPARENT);
        if (type == PhilPad.Pads.PAD_TYPE_NULL) {
            imageView.setVisibility(View.INVISIBLE);
        } else if (type == PhilPad.Pads.PAD_TYPE_TOOLS) {
            String filePath = cursor.getString(2);//cursor.getColumnIndex(PhilPad.Pads.COLUMN_NAME_IMAGEFILE)
            String packageName = cursor.getString(3);//cursor.getColumnIndex(PhilPad.Pads.COLUMN_NAME_EXTRA_DATA)

            if (TextUtils.isEmpty(packageName))
                return;
            if (TextUtils.isDigitsOnly(packageName)) {
                if (Integer.parseInt(packageName) == D.Tools.TOOLS_TYPE_RECENTAPPLICATION_INPAD) {
                    imageView.setImageBitmap(mRecentAppPadBitmap);
                } else if (Integer.parseInt(packageName) == D.Tools.TOOLS_TYPE_NOFITICATIONPAD) {
                    String notificationPadPath = PhilPad.Settings.getString(mContext.getContentResolver(), PhilPad.Settings.SETTINGS_KEY_NOTIFICATION_PAD_FILE_PATH, "notificationpad");
                    Picasso.with(mContext).load("file://" + mDirectoryPath + "/" + notificationPadPath + ".png").into(imageView);
                } else if (Integer.parseInt(packageName) == D.Tools.TOOLS_TYPE_FLASH_ON_OFF) {
                    if (mFlashEnable)
                        Picasso.with(mContext).load(R.drawable.ic_flash_on_24dp_old).into(imageView);
                    else
                        Picasso.with(mContext).load(R.drawable.ic_flash_off_24dp_old).into(imageView);
                } else if (Integer.parseInt(packageName) == D.Tools.TOOLS_TYPE_WIFI_ON_OFF) {
                    if (mWifiEnable) {
                        Picasso.with(mContext).load(R.drawable.ic_signal_wifi_4_bar_24dp_old).into(imageView);
                    } else {
                        Picasso.with(mContext).load(R.drawable.ic_signal_wifi_off_24dp_old).into(imageView);
                    }
                } else if (Integer.parseInt(packageName) == D.Tools.TOOLS_TYPE_ROTATION_ON_OFF) {
                    if (mRotateEnable) {
                        Picasso.with(mContext).load(R.drawable.ic_screen_rotation_24dp_old).into(imageView);
                    } else {
                        Picasso.with(mContext).load(R.drawable.ic_screen_lock_rotation_24dp_old).into(imageView);
                    }
                } else if (Integer.parseInt(packageName) == D.Tools.TOOLS_TYPE_BLUETOOTH_ON_OFF) {
                    if (mBluetoothEnable) {
                        Picasso.with(mContext).load(R.drawable.ic_bluetooth_24dp_old).into(imageView);
                    } else {
                        Picasso.with(mContext).load(R.drawable.ic_bluetooth_disabled_24dp_old).into(imageView);
                    }
                } else if (Integer.parseInt(packageName) == D.Tools.TOOLS_TYPE_AIRPLANE_ON_OFF) {
                    if (mAirplaneEnable) {
                        Picasso.with(mContext).load(R.drawable.ic_airplanemode_on_24dp_old).into(imageView);
                    } else {
                        Picasso.with(mContext).load(R.drawable.ic_airplanemode_off_24dp_old).into(imageView);
                    }
                } else if (Integer.parseInt(packageName) == D.Tools.TOOLS_TYPE_LASTAPP) {
                    Picasso.with(context).load(filePath).into(imageView);
                } else {
                    Picasso.with(context).load(filePath).into(imageView);
                }
            }

            imageView.setVisibility(View.VISIBLE);
        } else {
            String filePath = cursor.getString(2);//cursor.getColumnIndex(PhilPad.Pads.COLUMN_NAME_IMAGEFILE)
            Picasso.with(context).load(filePath).into(imageView);
            imageView.setVisibility(View.VISIBLE);
        }

        if (LocalSettings.mShowTitle) {
            String applicationName = cursor.getString(1);//cursor.getColumnIndex(PhilPad.Pads.COLUMN_NAME_TITLE)
            itemName.setText(applicationName);
        } else {
            itemName.setText("");
        }
    }

}
