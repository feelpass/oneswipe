
package com.philleeran.flicktoucher.view.widget;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;

import com.philleeran.flicktoucher.IPhilPad;
import com.philleeran.flicktoucher.PadUtils;
import com.philleeran.flicktoucher.view.pad.WidgetViewLayout;
import com.philleeran.flicktoucher.utils.L;

public class WidgetViewActivity extends Activity implements ServiceConnection {

    IPhilPad mPadBind;

    private Context mContext = null;
    private WidgetViewLayout mWidgetLayout;
    private int mAppWidgetId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;

        mAppWidgetId = getIntent().getIntExtra("app.widget.id", -1);
        mWidgetLayout = new WidgetViewLayout(mContext);
        mWidgetLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                finish();
                return true;
            }
        });

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(mWidgetLayout);
        mWidgetLayout.setWidgetByAppWidgetId(mAppWidgetId);

        Intent serviceIntent = new Intent(PadUtils.ACTION_PHILPAD_SERVICE);
        L.d("startPhilPadService");
        serviceIntent.setPackage(PadUtils.ACTION_PHILPAD_PACKAGE);
        serviceIntent.setAction(PadUtils.ACTION_PHILPAD_SERVICE);
        bindService(serviceIntent, this, BIND_AUTO_CREATE);
    }

    @Override
    public void finish() {
        super.finish();
        this.overridePendingTransition(0, 0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    @Override
    protected void onPause() {
        super.onPause();

        if (isFinishing() == false)
            finish();
    }

    public void onServiceConnected(ComponentName name, IBinder service) {
        mPadBind = IPhilPad.Stub.asInterface(service);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        mPadBind = null;
    }

}
