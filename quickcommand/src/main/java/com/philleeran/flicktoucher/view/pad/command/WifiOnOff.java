package com.philleeran.flicktoucher.view.pad.command;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Bundle;

public class WifiOnOff implements Command {
    private Context mContext;

    public WifiOnOff(Context context) {
        mContext = context;
    }

    @Override
    public void execute() {
        WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(!wifiManager.isWifiEnabled());
    }

}
