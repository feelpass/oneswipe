
package com.philleeran.flicktoucher.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.philleeran.flicktoucher.R;
import com.philleeran.flicktoucher.view.settings.TriggerSettingPreferenceFragment;

@SuppressWarnings("deprecation")
public class TriggerSettingActivity extends AppCompatActivity {

    Context mContext;
    private TriggerSettingPreferenceFragment mTriggerSettingPreferenceFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_trigger);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(true);
        }
        mTriggerSettingPreferenceFragment = new TriggerSettingPreferenceFragment();
        getFragmentManager().beginTransaction().replace(R.id.fragment_container, mTriggerSettingPreferenceFragment).commit();
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onDestroy() {

        super.onDestroy();

    }

    @Override
    protected void onPause() {
        super.onPause();

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;

            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }




}
