
package com.philleeran.flicktoucher.view.settings;

import android.app.ActionBar;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.view.MenuItem;

import com.philleeran.flicktoucher.IPhilPad;
import com.philleeran.flicktoucher.PadUtils;
import com.philleeran.flicktoucher.R;
import com.philleeran.flicktoucher.db.PhilPad;
import com.philleeran.flicktoucher.utils.L;

import org.json.JSONException;
import org.json.JSONObject;

public class SettingsDeveloperOptionsActivity extends PreferenceActivity {

    IPhilPad mPad;

    Context mContext;

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        initActionBar();
        setPreferenceScreen(createPreferenceHierarchy());
        if (PhilPad.Settings.getBoolean(getContentResolver(), PhilPad.Settings.SETTINGS_KEY_IS_SERVICE_ENABLE, true)) {
            Intent serviceIntent = new Intent(PadUtils.ACTION_PHILPAD_SERVICE);
            L.d("startPhilPadService");
            serviceIntent.setPackage(PadUtils.ACTION_PHILPAD_PACKAGE);
            serviceIntent.setAction(PadUtils.ACTION_PHILPAD_SERVICE);
            bindService(serviceIntent, conn, BIND_AUTO_CREATE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void initActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setTitle(R.string.activity_title_settings);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayShowTitleEnabled(true);

    }

    @Override
    protected void onDestroy() {
        try {
            unbindService(conn);
        } catch (Exception e) {
            e.printStackTrace();
        }

        super.onDestroy();
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

    @SuppressWarnings("deprecation")
    private PreferenceScreen createPreferenceHierarchy() {
        // Root
        PreferenceScreen root = getPreferenceManager().createPreferenceScreen(this);
        PreferenceScreen intentPref;
        PreferenceCategory inlinePrefCat;
        // // Inline preferences

        intentPref = getPreferenceManager().createPreferenceScreen(this);
        intentPref.setTitle("Reset DeveloperMode & PremiumMode");
        intentPref.setSummary("Reset DeveloperMode & PremiumMode");
        intentPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                PhilPad.Settings.putBoolean(getContentResolver(), PhilPad.Settings.SETTINGS_KEY_DEVELOPER_MODE, false);
                PhilPad.Settings.putBoolean(getContentResolver(), PhilPad.Settings.SETTINGS_KEY_IS_PREMIUM, false);
                return true;
            }
        });
        root.addPreference(intentPref);


/*
        inlinePrefCat = new PreferenceCategory(this);
        inlinePrefCat.setTitle(R.string.pref_category_feature);
        root.addPreference(inlinePrefCat);

        intentPref = getPreferenceManager().createPreferenceScreen(this);
        intentPref.setTitle(R.string.pref_title_buy);
        intentPref.setSummary(R.string.pref_title_buy_summary);
        intentPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                Intent intent = new Intent();
                ComponentName componentName = new ComponentName(mContext, BillingActivity.class);
                intent.setComponent(componentName);
                startActivity(intent);

                
                return true;
            }
        });
        root.addPreference(intentPref);
*/

        return root;
    }

    ServiceConnection conn = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mPad = IPhilPad.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mPad = null;
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1001) {
            int responseCode = data.getIntExtra("RESPONSE_CODE", 0);
            String purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA");
            String dataSignature = data.getStringExtra("INAPP_DATA_SIGNATURE");

            if (resultCode == RESULT_OK) {
                try {
                    JSONObject jo = new JSONObject(purchaseData);
                    String sku = jo.getString("productId");
                    L.d("You have bought the " + sku + ". Excellent choice,adventurer!");
                } catch (JSONException e) {
                    L.d("Failed to parse purchase data.");
                    e.printStackTrace();
                }
            } else {
                L.d("resultCode : " + resultCode);
            }
        }
    }

}
