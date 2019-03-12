
package com.philleeran.flicktoucher.view.settings;

import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.philleeran.flicktoucher.BuildConfig;
import com.philleeran.flicktoucher.IPhilPad;
import com.philleeran.flicktoucher.PadUtils;
import com.philleeran.flicktoucher.PhilPadApplication;
import com.philleeran.flicktoucher.R;
import com.philleeran.flicktoucher.db.PhilPad;
import com.philleeran.flicktoucher.utils.D;
import com.philleeran.flicktoucher.utils.L;
import com.philleeran.flicktoucher.utils.Utils;
import com.philleeran.flicktoucher.view.select.app.SelectItemActivity;
import com.philleeran.flicktoucher.view.settings.task.VersionChecker;
import com.philleeran.flicktoucher.view.tutorial.TutorialActivity;

import java.util.HashMap;
import java.util.Map;

public class SettingsActivity extends AppCompatActivity implements ServiceConnection {

    public IPhilPad mPad;

    Context mContext;

    public SwitchCompat mSwitch;

    // Does the user have the premium upgrade?
    boolean mIsPremium = false;

    /**
     * The view to show the ad.
     */
    private AdView adView;

    private long mBackKeyPressedTime = 0;
    private Toast mToast;
    private Fragment mSettingFragment;
    public static int OVERLAY_PERMISSION_REQ_CODE = 1234;


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == OVERLAY_PERMISSION_REQ_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(this)) {
                    // SYSTEM_ALERT_WINDOW permission not granted...
                    L.d("SYSTEM_ALERT_WINDOW permission not granted...");
                }
            }
        }
    }


    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = this;

        String packageName = mContext.getPackageName();
        String versionName = Utils.getVersionNameByPackageName(mContext, packageName);

        if (!BuildConfig.DEBUG && !Utils.isPossible(mContext, versionName)) {
            PadUtils.Toast(mContext, R.string.apk_is_broken);
            PhilPadApplication.tracker().send(new HitBuilders.EventBuilder().setCategory("error").setAction("apk_is_broken").build());
            finish();
        }
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        // inflate the view
        final View view = inflater.inflate(R.layout.switch_layout, null);
        mSwitch = (SwitchCompat) view.findViewById(R.id.switchAB);
        toolbar.addView(view);
        mSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    L.d("onCheckedChanged true");

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (!Settings.canDrawOverlays(mContext)) {
                            Toast.makeText(SettingsActivity.this, R.string.need_to_draw_overlay, Toast.LENGTH_LONG).show();
                            mSwitch.setChecked(false);
                            try {
                                unbindService(SettingsActivity.this);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            ComponentName component = new ComponentName(D.PACKAGE_NAME, D.PAD_SERVICE_NAME);
                            Intent i = new Intent().setComponent(component);
                            stopService(i);

                            PhilPad.Settings.putBoolean(getContentResolver(), PhilPad.Settings.SETTINGS_KEY_IS_SERVICE_ENABLE, false);

                            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                            startActivityForResult(intent, OVERLAY_PERMISSION_REQ_CODE);
                            return;
                        }
                    }

                    ComponentName component = new ComponentName(D.PACKAGE_NAME, D.PAD_SERVICE_NAME);
                    Intent i = new Intent().setComponent(component);
                    startService(i);
                    PhilPad.Settings.putBoolean(getContentResolver(), PhilPad.Settings.SETTINGS_KEY_IS_SERVICE_ENABLE, true);

                    Intent serviceIntent = new Intent(PadUtils.ACTION_PHILPAD_SERVICE);
                    L.d("startPhilPadService");
                    serviceIntent.setPackage(PadUtils.ACTION_PHILPAD_PACKAGE);
                    serviceIntent.setAction(PadUtils.ACTION_PHILPAD_SERVICE);
                    bindService(serviceIntent, SettingsActivity.this, BIND_AUTO_CREATE);


                    if (PhilPad.Settings.getBoolean(getContentResolver(), PhilPad.Settings.SETTINGS_KEY_COMPLETE_TOTURIAL, false) == true) {
                        if (PhilPad.Settings.getBoolean(getContentResolver(), PhilPad.Settings.SETTINGS_KEY_COMPLETE_FIRST_ADD_APP, false) == false) {
                            Intent bi = new Intent(mContext, SelectItemActivity.class);
                            bi.putExtra(PadUtils.INTENT_DATA_GROUPID, 0);
                            mContext.startActivity(bi);
                        }
                    }
                } else {
                    L.d("onCheckedChanged false");

                    try {
                        unbindService(SettingsActivity.this);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    ComponentName component = new ComponentName(D.PACKAGE_NAME, D.PAD_SERVICE_NAME);
                    Intent i = new Intent().setComponent(component);
                    stopService(i);

                    mPad = null;
                    PhilPad.Settings.putBoolean(getContentResolver(), PhilPad.Settings.SETTINGS_KEY_IS_SERVICE_ENABLE, false);
                }
            }
        });

        boolean philpadEnabled = isEnabled();
        if (mSwitch.isChecked() != philpadEnabled) {
            mSwitch.setChecked(philpadEnabled);
        }

        mSettingFragment = new SettingFragment();

        getFragmentManager().beginTransaction().replace(R.id.fragment_container, mSettingFragment).commit();

        if (PhilPad.Settings.getBoolean(getContentResolver(), PhilPad.Settings.SETTINGS_KEY_IS_SERVICE_ENABLE, false)) {
            Intent serviceIntent = new Intent(PadUtils.ACTION_PHILPAD_SERVICE);
            L.d("startPhilPadService");
            serviceIntent.setPackage(PadUtils.ACTION_PHILPAD_PACKAGE);
            serviceIntent.setAction(PadUtils.ACTION_PHILPAD_SERVICE);
            bindService(serviceIntent, SettingsActivity.this, BIND_AUTO_CREATE);
        }
        if (PhilPad.Settings.getBoolean(mContext.getContentResolver(), PhilPad.Settings.SETTINGS_KEY_IS_PREMIUM, false) == true) {
            mIsPremium = true;
        }

        adView = (AdView) findViewById(R.id.adView);
        if (!mIsPremium) {
            initAdmob();
        } else {
            adView.setVisibility(View.GONE);
        }

        if (PhilPad.Settings.getBoolean(getContentResolver(), PhilPad.Settings.SETTINGS_KEY_COMPLETE_TOTURIAL, false) == false) {
            PhilPad.Settings.putInt(mContext.getContentResolver(), PhilPad.Settings.SETTINGS_KEY_INTERACTION_OPTION, D.InteractionOption.INTERACTION_OPTION_SWIPE_AND_UP);
            PhilPad.Settings.putBoolean(getContentResolver(), getResources().getString(R.string.pref_trigger_area_sharedpref_top_right_corner), true);
            PhilPad.Settings.putBoolean(getContentResolver(), getResources().getString(R.string.pref_trigger_area_sharedpref_right_edge_upper_half), true);
            startActivity(new Intent(mContext, TutorialActivity.class));
            finish();
        } else {
            new VersionChecker(mContext).execute();
        }

        // Capture author info & user status
        Map<String, String> articleParams = new HashMap<>();
//param keys and values have to be of String type
        articleParams.put("premium status", String.valueOf(mIsPremium));
        articleParams.put("interaction type", PhilPad.Settings.getInt(mContext.getContentResolver(), PhilPad.Settings.SETTINGS_KEY_INTERACTION_OPTION, D.InteractionOption.INTERACTION_OPTION_SWIPE_AND_UP) == D.InteractionOption.INTERACTION_OPTION_SWIPE_AND_TOUCH ? "touch" : "up");
//up to 10 params can be logged with each event

    }

    private void initAdmob() {
        AdRequest adRequest = new AdRequest.Builder().addTestDevice("616A16DCB454C509D21DEA6AE61FE192")// .addTestDevice("6D53B98E9799F63E94F8E1653B37E63D")
                .build();
        // Start loading the ad in the background.
        adView.loadAd(adRequest);
        adView.setVisibility(View.VISIBLE);
    }

    private boolean isEnabled() {
        return PhilPad.Settings.getBoolean(getContentResolver(), PhilPad.Settings.SETTINGS_KEY_IS_SERVICE_ENABLE, false);
    }


    // We're being destroyed. It's important to dispose of the helper here!
    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            unbindService(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (adView != null) {
            adView.destroy();
        }
    }

    @Override
    public void onBackPressed() {
        if (System.currentTimeMillis() > mBackKeyPressedTime + 1000) {
            L.d("");
            mBackKeyPressedTime = System.currentTimeMillis();
            mToast = Toast.makeText(mContext, getString(R.string.toast_message_back_again), Toast.LENGTH_SHORT);
            mToast.show();
        } else if (System.currentTimeMillis() <= mBackKeyPressedTime + 1000) {
            mToast.cancel();
            finish();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        GoogleAnalytics.getInstance(SettingsActivity.this).reportActivityStart(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        GoogleAnalytics.getInstance(SettingsActivity.this).reportActivityStop(this);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        mPad = IPhilPad.Stub.asInterface(service);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        mPad = null;
    }
}
