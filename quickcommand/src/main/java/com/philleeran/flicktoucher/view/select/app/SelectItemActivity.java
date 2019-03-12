package com.philleeran.flicktoucher.view.select.app;

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
import com.philleeran.flicktoucher.view.settings.task.VersionChecker;

import java.util.ArrayList;
import java.util.List;

public class SelectItemActivity extends AppCompatActivity {

    public int mGroupId;
    public int mPositionId;
    public int mType;
    private AdView adView;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        L.d("enter");
        super.onCreate(savedInstanceState);

        mContext = this;
        mType = getIntent().getIntExtra(PadUtils.INTENT_REQUEST_TYPE, D.REQUEST_TYPE_NORMAL);
        mGroupId = getIntent().getIntExtra(PadUtils.INTENT_DATA_GROUPID, 0);
        mPositionId = getIntent().getIntExtra(PadUtils.INTENT_DATA_LISTID, 0);
        L.d("groupId " + mGroupId);
        setContentView(R.layout.activity_select_item);

        ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
        viewPager.setAdapter(new LaunchItemAdapter(getSupportFragmentManager(), getResources()));

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(viewPager);

        adView = (AdView) findViewById(R.id.adView);
        if (PhilPad.Settings.getBoolean(getContentResolver(), PhilPad.Settings.SETTINGS_KEY_IS_PREMIUM, false) == false) {
            initAdmob();
        } else {
            adView.setVisibility(View.GONE);
        }

        if (PhilPad.Settings.getBoolean(getContentResolver(), PhilPad.Settings.SETTINGS_KEY_COMPLETE_TOTURIAL, false)) {
            new VersionChecker(mContext).execute();
        }
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
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

    private static class LaunchItemAdapter extends FragmentPagerAdapter {

        private static final int PAGE_COUNT = 4;
        private final List<String> mTitleList;

        public LaunchItemAdapter(@NonNull FragmentManager fm, @NonNull Resources res) {
            super(fm);
            mTitleList = new ArrayList<>(PAGE_COUNT);
            mTitleList.add(res.getString(R.string.apps));
            mTitleList.add(res.getString(R.string.contacts));
            mTitleList.add(res.getString(R.string.tools));
            mTitleList.add(res.getString(R.string.add_link_list_shortcut));
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mTitleList.get(position);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new AppGridFragment();
                case 1:
                    return new ContactListFragment();
                case 2:
                    return new ToolListFragment();
                case 3:
                    return new ShortcutListFragment();
            }

            return null;
        }

        @Override
        public int getCount() {
            return PAGE_COUNT;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }
    }

}
