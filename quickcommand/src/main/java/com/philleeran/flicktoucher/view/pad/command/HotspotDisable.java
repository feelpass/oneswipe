package com.philleeran.flicktoucher.view.pad.command;

import android.os.Bundle;

import com.philleeran.flicktoucher.service.PhilPadService;

public class HotspotDisable implements Command {
    PhilPadService mService;

    public HotspotDisable(PhilPadService service) {
        mService = service;
    }

    @Override
    public void execute() {
        if (mService!= null)
            mService.setStartHotspotDisable();
    }

}
