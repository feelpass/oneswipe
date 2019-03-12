package com.philleeran.flicktoucher.view.pad.command;

import android.bluetooth.BluetoothAdapter;

public class BluetoothOnOff implements Command {

    @Override
    public void execute() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.disable();
        } else {
            mBluetoothAdapter.enable();
        }
    }

}
