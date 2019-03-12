
package com.philleeran.flicktoucher.activity;

import android.app.Activity;
import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import com.philleeran.flicktoucher.IPhilPad;
import com.philleeran.flicktoucher.PadUtils;
import com.philleeran.flicktoucher.R;
import com.philleeran.flicktoucher.db.PhilPad;
import com.philleeran.flicktoucher.utils.D;
import com.philleeran.flicktoucher.utils.L;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

public class AppWidgetListActivity extends Activity {

    IPhilPad mPadBind;

    int index = 0;

    private Context mContext = null;

    static private int REQUEST_PICK_APPWIDGET = 5;;

    static private int REQUEST_CREATE_APPWIDGET = 6;;

    private int mGroupId;

    private int mPositionId;

    private float DPSCALE;

    private PackageManager mPackageManager;


    List<AppWidgetProviderInfo> mArrayList;

    private AppWidgetManager mAppWidgetManager;

    private AppWidgetHost mAppWidgetHost;
    private int mPadSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        mPadSize = Integer.parseInt(PhilPad.Settings.getString(mContext.getContentResolver(), PhilPad.Settings.SETTINGS_KEY_PAD_SIZE, "4"));
        DPSCALE = getResources().getDisplayMetrics().density;

        Intent intent = getIntent();

        mGroupId = intent.getIntExtra(PadUtils.INTENT_DATA_GROUPID, 0);
        mPositionId = intent.getIntExtra(PadUtils.INTENT_DATA_LISTID, 0);

        setContentView(R.layout.app_list_activity);
        mPackageManager = mContext.getPackageManager();
        mAppWidgetManager = AppWidgetManager.getInstance(mContext);
        mAppWidgetHost = new AppWidgetHost(mContext, D.WIDGET_HOST_ID);
        int appWidgetId = this.mAppWidgetHost.allocateAppWidgetId();
        Intent pickIntent = new Intent(AppWidgetManager.ACTION_APPWIDGET_PICK);
        pickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        startActivityForResult(pickIntent, REQUEST_PICK_APPWIDGET);

        Intent serviceIntent = new Intent(PadUtils.ACTION_PHILPAD_SERVICE);
        L.d("startPhilPadService");
        serviceIntent.setPackage(PadUtils.ACTION_PHILPAD_PACKAGE);
        serviceIntent.setAction(PadUtils.ACTION_PHILPAD_SERVICE);
        bindService(serviceIntent, conn, BIND_AUTO_CREATE);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    private void configureWidget(Intent data) {
        Bundle extras = data.getExtras();
        int appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
        AppWidgetProviderInfo appWidgetInfo = mAppWidgetManager.getAppWidgetInfo(appWidgetId);
        if (appWidgetInfo.configure != null) {
            Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE);
            intent.setComponent(appWidgetInfo.configure);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            startActivityForResult(intent, REQUEST_CREATE_APPWIDGET);
        } else {

            createWidget(data);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_CANCELED) {
            finish();
            return;
        }

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_PICK_APPWIDGET) {
                configureWidget(data);
            } else if (requestCode == REQUEST_CREATE_APPWIDGET) {
                createWidget(data);
            }
        }

    }

    private void createWidget(Intent data) {
        int appWidgetId = data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);

        AppWidgetProviderInfo appWidgetInfo = mAppWidgetManager.getAppWidgetInfo(appWidgetId);

        ApplicationInfo appInfo;
        Resources resources = null;
        try {
            appInfo = mPackageManager.getApplicationInfo(appWidgetInfo.provider.getPackageName(), 0);
            resources = mPackageManager.getResourcesForApplication(appInfo);
        } catch (NameNotFoundException e2) {
            e2.printStackTrace();
        }

        Drawable drawable = resources.getDrawable(appWidgetInfo.icon);
        if (drawable == null) {
            try {
                drawable = mContext.getPackageManager().getApplicationIcon(appWidgetInfo.provider.getPackageName());
            } catch (NameNotFoundException e1) {
                e1.printStackTrace();
            }
        }

        Bitmap bitmap = PadUtils.getBitmapFromDrawable(mContext, drawable, (int) (96 * DPSCALE), (int) (96 * DPSCALE));

        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        File directory = cw.getDir(D.DIR_PATH_DEFAULT, Context.MODE_PRIVATE);
        File mypath = new File(directory, "widget_" + mGroupId + "_" + mPositionId + "_" + appWidgetInfo.provider.getPackageName() + ".png");
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            ((Bitmap) bitmap).compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        PhilPad.Pads.setPadItem(mContext, mGroupId, mPositionId, PhilPad.Pads.PAD_TYPE_WIDGET, "widget", null, "file://" + mypath.getPath(), 0, String.valueOf(appWidgetId));
        PhilPad.Pads.setGroupIcon(mContext, mGroupId, mPadSize);

        try {
            mPadBind.notifyReDrawGridView();
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        finish();
    }

    ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mPadBind = IPhilPad.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mPadBind = null;
        }
    };

}
