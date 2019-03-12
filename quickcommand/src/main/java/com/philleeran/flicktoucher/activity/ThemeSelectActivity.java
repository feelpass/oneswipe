
package com.philleeran.flicktoucher.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.philleeran.flicktoucher.IPhilPad;
import com.philleeran.flicktoucher.PadUtils;
import com.philleeran.flicktoucher.R;
import com.philleeran.flicktoucher.db.PadItemInfo;
import com.philleeran.flicktoucher.db.PhilPad;
import com.philleeran.flicktoucher.utils.D;
import com.philleeran.flicktoucher.utils.L;

import java.util.ArrayList;
import java.util.Locale;

public class ThemeSelectActivity extends AppCompatActivity{

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
     * will keep every loaded fragment in memory. If this becomes too memory
     * intensive, it may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    /**
     * Create the activity. Sets up an {@link android.app.ActionBar} with tabs, and then configures the
     * {@link ViewPager} contained inside R.layout.activity_main.
     * <p/>
     * <p>A {@link SectionsPagerAdapter} will be instantiated to hold the different pages of
     * fragments that are to be displayed. A
     * {@link android.support.v4.view.ViewPager.SimpleOnPageChangeListener} will also be configured
     * to receive callbacks when the user swipes between pages in the ViewPager.
     *
     * @param savedInstanceState
     */


    IPhilPad mPad;
    private Button mButton;

    private Context mContext;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
//        initActionBar();
        if (PhilPad.Settings.getBoolean(getContentResolver(), PhilPad.Settings.SETTINGS_KEY_IS_SERVICE_ENABLE, true)) {
            Intent serviceIntent = new Intent(PadUtils.ACTION_PHILPAD_SERVICE);
            L.d("startPhilPadService");
            serviceIntent.setPackage(PadUtils.ACTION_PHILPAD_PACKAGE);
            serviceIntent.setAction(PadUtils.ACTION_PHILPAD_SERVICE);
            bindService(serviceIntent, conn, BIND_AUTO_CREATE);
        }
        setContentView(R.layout.activity_theme);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(true);
        }
        // Set up the action bar. The navigation mode is set to NAVIGATION_MODE_TABS, which will
        // cause the ActionBar to render a set of tabs. Note that these tabs are *not* rendered
        // by the ViewPager; additional logic is lower in this file to synchronize the ViewPager
        // state with the tab state. (See mViewPager.setOnPageChangeListener() and onTabSelected().)
        // BEGIN_INCLUDE (set_navigation_mode)
        // END_INCLUDE (set_navigation_mode)

        // BEGIN_INCLUDE (setup_view_pager)
        // Create the adapter that will return a fragment for each of the three primary sections
        // of the app.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {

                if (mButton != null) {
                    mButton.setBackgroundColor(getPadBackgroundColor(position));
                    mButton.setTextColor(getFolderColor(position));
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        mButton = (Button) findViewById(R.id.ok);
        mButton.setBackgroundColor(getPadBackgroundColor(0));
        mButton.setTextColor(getFolderColor(0));
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                L.d("set theme mViewPager.getCurrentItem() : " + mViewPager.getCurrentItem());
                int backgroundColor = 0xFF0C274C;
                int folderColor = 0xAA19BDC4;
                int specialColor = 0xAAEF4089;


                switch (mViewPager.getCurrentItem()) {
                    case 0:
                        backgroundColor = 0xFF0C274C;
                        folderColor = 0xAA18709C;
                        specialColor = 0xAA19BDC4;
                        break;
                    case 1:
                        backgroundColor = 0xFF364659;
                        folderColor = 0xAAF2F2F2;
                        specialColor = 0xAAF2D2AE;
                        break;
                    case 2:
                        backgroundColor = 0xFF345391;
                        folderColor = 0xAA9B9EA3;
                        specialColor = 0xAAD3D6DB;
                        break;

                    case 3:
                        backgroundColor = 0xFF02A6EC;
                        folderColor = 0xAAEFF0EF;
                        specialColor = 0xAAB6CCC7;
                        break;

                    case 4:
                        backgroundColor = 0xFFB6D41A;
                        folderColor = 0xAAFFEA1A;
                        specialColor = 0xAA2E9396;
                        break;


                    case 5:
                        backgroundColor = 0xFFF4FBFF;
                        folderColor = 0xAAB0D5EB;
                        specialColor = 0xAADDE3E7;
                        break;

                    case 6:
                        backgroundColor = 0xFF386300;
                        folderColor = 0xAAFFAC00;
                        specialColor = 0xAA1C0A02;
                        break;


                    case 7:
                        backgroundColor = 0xFF000000;
                        folderColor = 0xAAEFAA00;
                        specialColor = 0xAA900000;
                        break;


                    case 8:
                        backgroundColor = 0xFFF35D5F;
                        folderColor = 0xAAF2F2F2;
                        specialColor = 0xAA049DD9;
                        break;


                    case 9:
                        backgroundColor = 0xFFF0D20C;
                        folderColor = 0xAAF47402;
                        specialColor = 0xAAF25A02;
                        break;
                }

                try {
                    PhilPad.Settings.putInt(mContext.getContentResolver(), PhilPad.Settings.SETTINGS_KEY_PAD_BACKGROUND_COLOR, backgroundColor);
                    PhilPad.Settings.putInt(mContext.getContentResolver(), PhilPad.Settings.SETTINGS_KEY_PAD_FOLDER_COLOR, folderColor);
                    PhilPad.Settings.putInt(mContext.getContentResolver(), PhilPad.Settings.SETTINGS_KEY_PAD_SPECIAL_COLOR, specialColor);

                    if(mPad != null)
                    {
                        mPad.notifyReDrawGridViewBackground();
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }


                ArrayList<PadItemInfo> infos = PhilPad.Pads.getPadGroups(mContext.getContentResolver(), PhilPad.Pads.DEFAULT_SORT_ORDER);
                int padSize = PhilPad.Settings.getInt(mContext.getContentResolver(), PhilPad.Settings.SETTINGS_KEY_PAD_SIZE, 4);
                PhilPad.Pads.setGroupIcon(mContext, D.GROUPID_GROUND, padSize);
                for (PadItemInfo info : infos) {
                    PhilPad.Pads.setGroupIcon(mContext, info.getToGroupId(), padSize);
                    L.d("group Id : " + info.getType() + " , " + info.getToGroupId());
                }
                finish();
            }
        });

        // END_INCLUDE (setup_view_pager)

        // When swiping between different sections, select the corresponding tab. We can also use
        // ActionBar.Tab#select() to do this if we have a reference to the Tab.
        // BEGIN_INCLUDE (page_change_listener)
   /*     mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });*/
        // END_INCLUDE (page_change_listener)

        // BEGIN_INCLUDE (add_tabs)
        // For each of the sections in the app, add a tab to the action bar.
   /*     for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by the adapter. Also
            // specify this Activity object, which implements the TabListener interface, as the
            // callback (listener) for when this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }*/
        // END_INCLUDE (add_tabs)

    }

    private int getPadBackgroundColor(int currentItem) {
        switch (currentItem) {
            case 0:
                return 0xFF0C274C;
//                return 0xAA18709C;
//                return 0xAA19BDC4;
            case 1:
                return 0xFF364659;
//                return 0xAAF2F2F2;
//                return 0xAAF2D2AE;
            case 2:
                return 0xFF345391;
//                return 0xAA9B9EA3;
//                return 0xAAD3D6DB;

            case 3:
                return 0xFF02A6EC;
//                return 0xAAEFF0EF;
//                return 0xAAB6CCC7;

            case 4:
                return 0xFFB6D41A;
//                return  0xAAFFEA1A;
//                return 0xAA2E9396;


            case 5:
                return 0xFFF4FBFF;
//                return 0xAAB0D5EB;
//                return 0xAADDE3E7;

            case 6:
                return 0xFF386300;
//                return 0xAAFFAC00;
//                return 0xAA1C0A02;


            case 7:
                return 0xFF000000;
//                return 0xAAEFAA00;
//                return 0xAA900000;


            case 8:
                return 0xFFF35D5F;
//                return 0xAAF2F2F2;
//                return 0xAA049DD9;


            case 9:
                return 0xFFF0D20C;
//                return 0xAAF47402;
//                return 0xAAF25A02;
        }
        return 0xFFF0D20C;

    }

    private int getFolderColor(int currentItem) {
        switch (currentItem) {
            case 0:
//                return 0xFF0C274C;
                return 0xAA18709C;
//            return 0xAA19BDC4;
            case 1:
//                return 0xFF364659;
                return 0xAAF2F2F2;
//            return 0xAAF2D2AE;
            case 2:
//                return 0xFF345391;
                return 0xAA9B9EA3;
//            return 0xAAD3D6DB;

            case 3:
//                return 0xFF02A6EC;
                return 0xAAEFF0EF;
//            return 0xAAB6CCC7;

            case 4:
//                return 0xFFB6D41A;
                return 0xAAFFEA1A;
//            return 0xAA2E9396;


            case 5:
//                return 0xFFF4FBFF;
                return 0xAAB0D5EB;
//            return 0xAADDE3E7;

            case 6:
//                return 0xFF386300;
                return 0xAAFFAC00;
//            return 0xAA1C0A02;


            case 7:
//                return 0xFF000000;
                return 0xAAEFAA00;
//            return 0xAA900000;


            case 8:
//                return 0xFFF35D5F;
                return 0xAAF2F2F2;
//            return 0xAA049DD9;


            case 9:
//                return 0xFFF0D20C;
                return 0xAAF47402;
//            return 0xAAF25A02;
        }
        return 0xFFF0D20C;

    }

    private int getSpacialColor(int currentItem) {
        switch (currentItem) {
            case 0:
//                return 0xFF0C274C;
//            return 0xAA18709C;
                return 0xAA19BDC4;
            case 1:
//                return 0xFF364659;
//            return 0xAAF2F2F2;
                return 0xAAF2D2AE;
            case 2:
//                return 0xFF345391;
//            return 0xAA9B9EA3;
                return 0xAAD3D6DB;

            case 3:
//                return 0xFF02A6EC;
//            return 0xAAEFF0EF;
                return 0xAAB6CCC7;

            case 4:
//                return 0xFFB6D41A;
//            return  0xAAFFEA1A;
                return 0xAA2E9396;


            case 5:
//                return 0xFFF4FBFF;
//            return 0xAAB0D5EB;
                return 0xAADDE3E7;

            case 6:
//                return 0xFF386300;
//            return 0xAAFFAC00;
                return 0xAA1C0A02;


            case 7:
//                return 0xFF000000;
//            return 0xAAEFAA00;
                return 0xAA900000;


            case 8:
//                return 0xFFF35D5F;
//            return 0xAAF2F2F2;
                return 0xAA049DD9;


            case 9:
//                return 0xFFF0D20C;
//            return 0xAAF47402;
                return 0xAAF25A02;
        }
        return 0xFFF0D20C;

    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            unbindService(conn);
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    // BEGIN_INCLUDE (fragment_pager_adapter)

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages. This provides the data for the {@link ViewPager}.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        // END_INCLUDE (fragment_pager_adapter)


        public SectionsPagerAdapter(android.support.v4.app.FragmentManager fm) {
            super(fm);
        }

        // BEGIN_INCLUDE (fragment_pager_adapter_getitem)

        /**
         * Get fragment corresponding to a specific position. This will be used to populate the
         * contents of the {@link ViewPager}.
         *
         * @param position Position to fetch fragment for.
         * @return Fragment for specified position.
         */
        @Override
        public android.support.v4.app.Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a DummySectionFragment (defined as a static inner class
            // below) with the page number as its lone argument.
            android.support.v4.app.Fragment fragment = new DummySectionFragment();
            Bundle args = new Bundle();
            args.putInt(DummySectionFragment.ARG_SECTION_NUMBER, position + 1);
            args.putInt(DummySectionFragment.ARG_SECTION_IMAGE, position);
            fragment.setArguments(args);
            return fragment;
        }
        // END_INCLUDE (fragment_pager_adapter_getitem)

        // BEGIN_INCLUDE (fragment_pager_adapter_getcount)

        /**
         * Get number of pages the {@link ViewPager} should render.
         *
         * @return Number of fragments to be rendered as pages.
         */
        @Override
        public int getCount() {
            // Show 3 total pages.
            return 10;
        }
        // END_INCLUDE (fragment_pager_adapter_getcount)

        // BEGIN_INCLUDE (fragment_pager_adapter_getpagetitle)

        /**
         * Get title for each of the pages. This will be displayed on each of the tabs.
         *
         * @param position Page to fetch title for.
         * @return Title for specified page.
         */
        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return "111";
                case 1:
                    return "211";
                case 2:
                    return "113";
            }
            return null;
        }
        // END_INCLUDE (fragment_pager_adapter_getpagetitle)
    }

    /**
     * A dummy fragment representing a section of the app, but that simply displays dummy text.
     * This would be replaced with your application's content.
     */
    public static class DummySectionFragment extends android.support.v4.app.Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        public static final String ARG_SECTION_NUMBER = "section_number";
        public static final String ARG_SECTION_IMAGE = "section_image";

        public DummySectionFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_select_theme, container, false);
            ImageView dummyImageView = (ImageView) rootView.findViewById(R.id.theme_image);
            int number = getArguments().getInt(ARG_SECTION_IMAGE);
            switch (number) {
                case 0:
                    dummyImageView.setImageResource(R.drawable.theme_0);
                    break;
                case 1:
                    dummyImageView.setImageResource(R.drawable.theme_1);
                    break;
                case 2:
                    dummyImageView.setImageResource(R.drawable.theme_2);
                    break;
                case 3:
                    dummyImageView.setImageResource(R.drawable.theme_3);
                    break;
                case 4:
                    dummyImageView.setImageResource(R.drawable.theme_4);
                    break;
                case 5:
                    dummyImageView.setImageResource(R.drawable.theme_5);
                    break;
                case 6:
                    dummyImageView.setImageResource(R.drawable.theme_6);
                    break;
                case 7:
                    dummyImageView.setImageResource(R.drawable.theme_7);
                    break;
                case 8:
                    dummyImageView.setImageResource(R.drawable.theme_8);
                    break;
                case 9:
                    dummyImageView.setImageResource(R.drawable.theme_9);
                    break;
                default:
                    dummyImageView.setImageResource(R.drawable.theme_9);
                    break;

            }
            return rootView;
        }
    }
}
