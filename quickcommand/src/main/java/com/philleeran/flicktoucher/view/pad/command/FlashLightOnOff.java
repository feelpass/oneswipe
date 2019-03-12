package com.philleeran.flicktoucher.view.pad.command;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.philleeran.flicktoucher.R;
import com.philleeran.flicktoucher.view.pad.listener.FlashLightListener;

public class FlashLightOnOff implements Command {
    private final FlashLightListener mFlashLightLitener;
    private final Camera.Parameters mCameraParameters;
    Context mContext;

    public FlashLightOnOff(Context context, Camera.Parameters parameters, FlashLightListener listener) {
        mContext = context;
        mFlashLightLitener = listener;
        mCameraParameters = parameters;
    }

    @Override
    public void execute() {


        if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA)) {
            Toast.makeText(mContext, R.string.there_is_no_permission_to_use_tools, Toast.LENGTH_LONG).show();
        } else {
            try {
                if (Camera.Parameters.FLASH_MODE_TORCH.equals(mCameraParameters.getFlashMode())) {
                    mCameraParameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);

                    mFlashLightLitener.onFlashLightChanged(false);

                } else {
                    mCameraParameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);

                    mFlashLightLitener.onFlashLightChanged(true);

                }
            } catch (RuntimeException e) {
            }
        }
    }

}
