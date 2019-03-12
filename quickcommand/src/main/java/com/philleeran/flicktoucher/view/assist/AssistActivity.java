
package com.philleeran.flicktoucher.view.assist;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;

import com.philleeran.flicktoucher.IPhilPad;
import com.philleeran.flicktoucher.PadUtils;
import com.philleeran.flicktoucher.utils.L;

import java.util.concurrent.CountDownLatch;

public class AssistActivity extends Activity implements ServiceConnection {

    IPhilPad mPadBind;


    private CountDownLatch mSignalBind;

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSignalBind = new CountDownLatch(1);
        Intent serviceIntent = new Intent(PadUtils.ACTION_PHILPAD_SERVICE);
        L.d("startPhilPadService");
        serviceIntent.setPackage(PadUtils.ACTION_PHILPAD_PACKAGE);
        serviceIntent.setAction(PadUtils.ACTION_PHILPAD_SERVICE);
        bindService(serviceIntent, this, BIND_AUTO_CREATE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing() == false)
            finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        unbindService(this);
        super.onDestroy();
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        mPadBind = IPhilPad.Stub.asInterface(service);
        mSignalBind.countDown();
        try {
            L.d("setShowPadView");
            mPadBind.setShowPadView(true);
            finish();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        mPadBind = null;
    }

}
