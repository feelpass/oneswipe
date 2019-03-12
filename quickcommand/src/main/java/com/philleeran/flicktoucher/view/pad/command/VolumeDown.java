package com.philleeran.flicktoucher.view.pad.command;

import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;

import com.philleeran.flicktoucher.R;
import com.philleeran.flicktoucher.view.pad.PadContract;

public class VolumeDown implements Command {

    private Context mContext;

    private PadContract.Presenter mPresenter;

    public VolumeDown(Context context, PadContract.Presenter presenter) {
        mContext = context;
        mPresenter = presenter;
    }

    @Override
    public void execute() {
        AudioManager audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, 0);
        int volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        mPresenter.showToast(mContext.getString(R.string.toast_media_volume_set, new Object[]{volume}));
    }

}
