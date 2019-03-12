
package com.philleeran.flicktoucher.view.settings;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import com.philleeran.flicktoucher.view.pad.HotSpotView;
import com.philleeran.flicktoucher.IPhilPad;
import com.philleeran.flicktoucher.IPhilPad.Stub;
import com.philleeran.flicktoucher.PadUtils;
import com.philleeran.flicktoucher.utils.L;

public class SettingsHotspotsCustomizeActivity extends Activity implements View.OnTouchListener {

    IPhilPad mPad;

    Context mContext;
    private float DPSCALE;

    private int DISPLAY_WIDTH;

    private int DISPLAY_HEIGHT;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;

        DPSCALE = getResources().getDisplayMetrics().density;
        DISPLAY_WIDTH = getResources().getDisplayMetrics().widthPixels;
        DISPLAY_HEIGHT = getResources().getDisplayMetrics().heightPixels;

        RelativeLayout layout = new RelativeLayout(mContext);
        layout.setBackgroundColor(Color.GRAY);
        layout.setGravity(Gravity.BOTTOM | Gravity.RIGHT);
        layout.setOnTouchListener(this);
        setContentView(layout);

        HotSpotView hotspotView = new HotSpotView(mContext,10, 10);
        hotspotView.setAlpha(1.0f);
        hotspotView.updateLayoutParams(10,10, (int)(DISPLAY_WIDTH/DPSCALE) - 10, (int)(DISPLAY_HEIGHT/DPSCALE) - 10);
        layout.addView(hotspotView);

        Intent serviceIntent = new Intent(PadUtils.ACTION_PHILPAD_SERVICE);
        L.d("startPhilPadService");
        serviceIntent.setPackage(PadUtils.ACTION_PHILPAD_PACKAGE);
        serviceIntent.setAction(PadUtils.ACTION_PHILPAD_SERVICE);
        bindService(serviceIntent, conn, BIND_AUTO_CREATE);

    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            mPad.notiHotspotsSetting(false);
        } catch (RemoteException e) {
            L.e(e);
        } catch (NullPointerException e) {
            L.e(e);
        }
    }

    @Override
    protected void onDestroy() {
        unbindService(conn);
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            mPad.notiHotspotsSetting(false);
        } catch (RemoteException e) {
            L.e(e);
        } catch (NullPointerException e) {
            L.e(e);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;

            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }


    ServiceConnection conn = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mPad = Stub.asInterface(service);
            try {
                mPad.notiHotspotsSetting(true);
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mPad = null;
        }
    };

    @Override
    public boolean onTouch(View v, MotionEvent event) {




        return false;
    }
}
