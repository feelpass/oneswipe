package com.philleeran.flicktoucher.view.select.activity;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.philleeran.flicktoucher.PadUtils;
import com.philleeran.flicktoucher.R;
import com.philleeran.flicktoucher.activity.BaseItemSelectFragment;
import com.philleeran.flicktoucher.db.PhilPad;
import com.philleeran.flicktoucher.utils.D;
import com.philleeran.flicktoucher.utils.L;

public class SelectActivityActivity extends   AppCompatActivity {

        public int mGroupId;
        public int mPositionId;
        public int mType;
        private AdView adView;
        private Context mContext;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            L.d("onCreate");
            super.onCreate(savedInstanceState);

            mContext = this;
            mType = getIntent().getIntExtra(PadUtils.INTENT_REQUEST_TYPE, D.REQUEST_TYPE_NORMAL);
            mGroupId = getIntent().getIntExtra(PadUtils.INTENT_DATA_GROUPID, 0);
            mPositionId = getIntent().getIntExtra(PadUtils.INTENT_DATA_LISTID, 0);
            L.d("groupId " + mGroupId);
            setContentView(R.layout.activity_select_activity);

            adView = (AdView) findViewById(R.id.adView);
            if (PhilPad.Settings.getBoolean(getContentResolver(), PhilPad.Settings.SETTINGS_KEY_IS_PREMIUM, false) == false) {
                initAdmob();
            } else {
                adView.setVisibility(View.GONE);
            }

        }


        @Override
        protected void onNewIntent(Intent intent) {
            super.onNewIntent(intent);
            L.d("onNewIntent");
            mType = intent.getIntExtra(PadUtils.INTENT_REQUEST_TYPE, D.REQUEST_TYPE_NORMAL);
            mGroupId = intent.getIntExtra(PadUtils.INTENT_DATA_GROUPID, 0);
            mPositionId = intent.getIntExtra(PadUtils.INTENT_DATA_LISTID, 0);

            for (Fragment fragment : getSupportFragmentManager().getFragments()) {
                if (fragment instanceof BaseItemSelectFragment) {
                    BaseItemSelectFragment itemSelectedListFragment = (BaseItemSelectFragment) fragment;
                    itemSelectedListFragment.newIntent();
                }
            }
        }

        @Override
        protected void onPause() {
            super.onPause();
        }

        @Override
        protected void onDestroy() {
            super.onDestroy();
            if (adView != null) {
                adView.destroy();
            }

        }

        private void initAdmob() {
            AdRequest adRequest = new AdRequest.Builder().addTestDevice("616A16DCB454C509D21DEA6AE61FE192")// .addTestDevice("6D53B98E9799F63E94F8E1653B37E63D")
                    .build();
            // Start loading the ad in the background.
            adView.loadAd(adRequest);
            adView.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            for (Fragment fragment : getSupportFragmentManager().getFragments()) {
                fragment.onActivityResult(requestCode, resultCode, data);
            }
        }


}
