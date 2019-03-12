package com.philleeran.flicktoucher.view.settings;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

import com.philleeran.flicktoucher.IPhilPad;
import com.philleeran.flicktoucher.PadUtils;
import com.philleeran.flicktoucher.R;
import com.philleeran.flicktoucher.db.PhilPad;
import com.philleeran.flicktoucher.utils.L;

/**
 * Created by young on 2016-01-10.
 */
public class TriggerSettingPreferenceFragment extends PreferenceFragment{

    IPhilPad mPad;

    private Context mContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getActivity();
        
        setPreferenceScreen(createPreferenceHierarchy(mContext));

        Intent serviceIntent = new Intent(PadUtils.ACTION_PHILPAD_SERVICE);
        L.d("startPhilPadService");
        serviceIntent.setPackage(PadUtils.ACTION_PHILPAD_PACKAGE);
        serviceIntent.setAction(PadUtils.ACTION_PHILPAD_SERVICE);
        getActivity().bindService(serviceIntent, conn, Activity.BIND_AUTO_CREATE);

    }

    @Override
    public void onResume() {
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
    public void onDestroy() {
        super.onDestroy();
        getActivity().unbindService(conn);
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            mPad.notiHotspotsSetting(false);
        } catch (RemoteException e) {
            L.e(e);
        } catch (NullPointerException e) {
            L.e(e);
        }
    }

    private PreferenceScreen createPreferenceHierarchy(Context context) {
        // Root
        PreferenceScreen root = getPreferenceManager().createPreferenceScreen(context);
        CheckBoxPreference checkBoxPref;

        PreferenceCategory inlinePref = new PreferenceCategory(context);
        inlinePref.setTitle(R.string.activity_title_settings_trigger_area);
        root.addPreference(inlinePref);

        String[] hotspotsTitle = mContext.getResources().getStringArray(R.array.hotspots_title);
        String[] hotspotsSharePref = mContext.getResources().getStringArray(R.array.hotspots_sharedpref);

        for (int i = 0; i < hotspotsTitle.length; i++) {
            checkBoxPref = new CheckBoxPreference(context);
            checkBoxPref.setTitle(hotspotsTitle[i]);
            checkBoxPref.setDefaultValue(PhilPad.Settings.getBoolean(mContext.getContentResolver(), hotspotsSharePref[i], false));
            final String prefString = hotspotsSharePref[i];
            checkBoxPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    PhilPad.Settings.putBoolean(mContext.getContentResolver(), prefString, (Boolean) newValue);
                    try {
                        mPad.reShowHotSpotViews(prefString, (Boolean) newValue);
                    } catch (RemoteException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    return true;
                }
            });
            root.addPreference(checkBoxPref);
        }

        return root;
    }

    ServiceConnection conn = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mPad = IPhilPad.Stub.asInterface(service);
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
}
