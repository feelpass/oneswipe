
package com.philleeran.flicktoucher.view.tutorial;

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.google.android.gms.analytics.HitBuilders;
import com.nineoldandroids.view.ViewHelper;
import com.philleeran.flicktoucher.PhilPadApplication;
import com.philleeran.flicktoucher.R;
import com.philleeran.flicktoucher.activity.ProductTourFragment;
import com.philleeran.flicktoucher.db.PhilPad;
import com.philleeran.flicktoucher.utils.D;
import com.philleeran.flicktoucher.utils.L;
import com.philleeran.flicktoucher.utils.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

public class TutorialActivity extends AppCompatActivity {

    static final int NUM_PAGES = 4;

    ViewPager pager;
    PagerAdapter pagerAdapter;
    LinearLayout circles;
    /*
        Button skip;
    */
    Button done;
    ImageButton next;
    Button startButton;
    Button startButtonTutorial;
    boolean isOpaque = true;

    public float DPSCALE;


    private ProgressBar mProgressBar;

    private PackageManager mPackageManager;

    private List<ResolveInfo> mApps;

    private Context mContext;
    private View mTransparentView;
    private Vibrator mVibrator;
    final public static int HANDLER_HOVER_ONSELECTED = 1;
    private boolean mShowPopup = false;
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            boolean isHover = false;
            isHover = false;
            switch (msg.what) {
                case HANDLER_HOVER_ONSELECTED:
                    mVibrator.vibrate(20);
                    mShowPopup = true;
                    break;
            }
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        DPSCALE = getResources().getDisplayMetrics().density;
        setContentView(R.layout.activity_tutorial);

        mContext = this;
        mVibrator = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);

        mPackageManager = getPackageManager();

        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);

        mTransparentView = findViewById(R.id.transparent_view);
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);


        mProgressBar.setMax(mPackageManager.queryIntentActivities(mainIntent, 0).size());






      /*  skip = Button.class.cast(findViewById(R.id.skip));
        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                endTutorial();
            }
        });
        skip.setEnabled(false);*/

        next = ImageButton.class.cast(findViewById(R.id.next));
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pager.setCurrentItem(pager.getCurrentItem() + 1, true);
            }
        });

        done = Button.class.cast(findViewById(R.id.done));
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                endTutorial();
            }
        });
        done.setEnabled(false);



        startButton = Button.class.cast(findViewById(R.id.button_start));
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new AppListUpdateTask().execute();
                startButton.setVisibility(View.GONE);
            }
        });

        startButtonTutorial = Button.class.cast(findViewById(R.id.button_start_tutorial));
        startButtonTutorial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                endTutorial();
            }
        });




        pager = (ViewPager) findViewById(R.id.pager);
        pagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        pager.setAdapter(pagerAdapter);
        pager.setPageTransformer(true, new CrossfadePageTransformer());
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                if (position == NUM_PAGES - 2 && positionOffset > 0) {
                    if (isOpaque) {
                        pager.setBackgroundColor(Color.TRANSPARENT);
                        isOpaque = false;
                    }
                } else {
                    if (!isOpaque) {
                        pager.setBackgroundColor(getResources().getColor(R.color.primary_material_light));
                        isOpaque = true;
                    }
                }
            }

            @Override
            public void onPageSelected(int position) {
                setIndicator(position);
                if (position == NUM_PAGES - 2) {
/*
                    skip.setVisibility(View.GONE);
*/
                    next.setVisibility(View.VISIBLE);
                    done.setVisibility(View.GONE);

                } else if (position < NUM_PAGES - 2) {
                    if(startButton.getVisibility() == View.VISIBLE)
                    {
                        pager.setCurrentItem(0);
                    }
/*
                    skip.setVisibility(View.VISIBLE);
*/
                    next.setVisibility(View.VISIBLE);
                    done.setVisibility(View.GONE);
                } else if (position == NUM_PAGES - 1) {
                    next.setVisibility(View.GONE);
//                    done.setVisibility(View.VISIBLE);
                    // endTutorial();
                    //mTransparentView.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        buildCircles();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (pager != null) {
            pager.clearOnPageChangeListeners();
        }
    }

    private void buildCircles() {
        circles = LinearLayout.class.cast(findViewById(R.id.circles));

        float scale = getResources().getDisplayMetrics().density;
        int padding = (int) (5 * scale + 0.5f);

        for (int i = 0; i < NUM_PAGES; i++) {
            ImageView circle = new ImageView(this);
            circle.setImageResource(R.drawable.ic_swipe_indicator_white_18dp);
            circle.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            circle.setAdjustViewBounds(true);
            circle.setPadding(padding, 0, padding, 0);
            circles.addView(circle);
        }

        setIndicator(0);
    }

    private void setIndicator(int index) {
        if (index < NUM_PAGES) {
            for (int i = 0; i < NUM_PAGES; i++) {
                ImageView circle = (ImageView) circles.getChildAt(i);
                if (i == index) {
                    circle.setColorFilter(getResources().getColor(R.color.text_selected));
                } else {
                    circle.setColorFilter(getResources().getColor(android.R.color.transparent));
                }
            }
        }
    }

    private void endTutorial() {
/*
        PhilPad.Settings.putBoolean(getContentResolver(), PhilPad.Settings.SETTINGS_KEY_COMPLETE_TOTURIAL, true);
*/
        startActivity(new Intent(mContext, Tutorial2Activity.class));
        finish();
        overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out);





        PhilPadApplication.tracker().send(new HitBuilders.EventBuilder().setCategory("tutorial").setAction("tutorial start").build());




    }

    @Override
    public void onBackPressed() {
/*
        if (pager.getCurrentItem() == 0) {

        } else {
            pager.setCurrentItem(pager.getCurrentItem() - 1);
        }
*/
    }

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {

        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public android.support.v4.app.Fragment getItem(int position) {
            ProductTourFragment tp = null;
            switch (position) {
                case 0:
                    tp = ProductTourFragment.newInstance(R.layout.welcome_fragment1);
                    break;
                case 1:
                    tp = ProductTourFragment.newInstance(R.layout.welcome_fragment2);
                    break;
                case 2:
                    tp = ProductTourFragment.newInstance(R.layout.welcome_fragment3);
                    break;
                case 3:
                    tp = ProductTourFragment.newInstance(R.layout.welcome_fragment4);
                    break;
                case 4:
                    tp = ProductTourFragment.newInstance(R.layout.welcome_fragment5);
                    break;
            }

            return tp;
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }

    public class CrossfadePageTransformer implements ViewPager.PageTransformer {

        private int mDownX;
        private int mDownY;
        private int mX;
        private int mY;
        private boolean bStarted = false;
        private int mStep = 1;
        private boolean bStarted2 = false;

        @Override
        public void transformPage(View page, float position) {
            int pageWidth = page.getWidth();

            View backgroundView = page.findViewById(R.id.welcome_fragment);
            final View text_head = page.findViewById(R.id.heading);
            final View text_content = page.findViewById(R.id.content);
       /*     View object1 = page.findViewById(R.id.a000);
            View object9 = page.findViewById(R.id.a010);
            View object11 = page.findViewById(R.id.a007);*/


            if (0 <= position && position < 1) {
                ViewHelper.setTranslationX(page, pageWidth * -position);
            }
            if (-1 < position && position < 0) {
                ViewHelper.setTranslationX(page, pageWidth * -position);
            }

            if (position <= -1.0f || position >= 1.0f) {
            } else if (position == 0.0f) {
            } else {
                if (backgroundView != null) {
                    ViewHelper.setAlpha(backgroundView, 1.0f - Math.abs(position));
                }

                if (text_head != null) {
                    ViewHelper.setTranslationX(text_head, pageWidth * position);
                    ViewHelper.setAlpha(text_head, 1.0f - Math.abs(position));
                }

                if (text_content != null) {
                    ViewHelper.setTranslationX(text_content, pageWidth * position);
                    ViewHelper.setAlpha(text_content, 1.0f - Math.abs(position));
                }

            }
        }
    }

    public class AppListUpdateTask extends AsyncTask<Void, Integer, Cursor> {


        protected Cursor doInBackground(Void... args) {
            Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

            mContext.getContentResolver().delete(PhilPad.Apps.CONTENT_URI, null, null);
//            mContext.getContentResolver().delete(PhilPad.Pads.CONTENT_URI, null, null);

            mApps = mPackageManager.queryIntentActivities(mainIntent, 0);
            Cursor c = null;


            for (int i = 0; i < mApps.size(); i++) {
                publishProgress(i);
                ResolveInfo info = mApps.get(i);

                String packageName = info.activityInfo.packageName;
                String activityName = info.activityInfo.name;
                Resources res = null;
                try {
                    res = mPackageManager.getResourcesForApplication(packageName);
                } catch (PackageManager.NameNotFoundException e) {
                    L.e(e);
                }
/*
                int iconResId = info.getIconResource();
                Uri iconUri = new Uri.Builder()
                        .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
                        .authority(packageName)
                        .appendPath(res.getResourceTypeName(iconResId))
                        .appendPath(res.getResourceEntryName(iconResId))
                        .build();
*/

                String uriString = Intent.makeMainActivity(new ComponentName(packageName, activityName)).toUri(0);

                c = getContentResolver().query(PhilPad.Apps.CONTENT_URI, new String[]{
                        PhilPad.Apps.COLUMN_NAME_COMPONENT_NAME
                }, PhilPad.Apps.COLUMN_NAME_COMPONENT_NAME + "=?", new String[]{
                        uriString
                }, null);
                if (c == null || c.getCount() > 0) {
                    continue;
                }

                ContentValues cv = new ContentValues();

                cv.put(PhilPad.Apps.COLUMN_NAME_COMPONENT_NAME, uriString);
                cv.put(PhilPad.Apps.COLUMN_NAME_TITLE, (String) info.loadLabel(mPackageManager));

                // convert drawable to bitmap
                Drawable d = info.loadIcon(mPackageManager);
                int h = 96;
                int w = 96;
                Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                d.setBounds(0, 0, w, h);
                d.draw(canvas);

                ContextWrapper cw = new ContextWrapper(getApplicationContext());
                File directory = cw.getDir(D.DIR_PATH_DEFAULT, Context.MODE_PRIVATE);
                File mypath = new File(directory, activityName + ".png");
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(mypath);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                    fos.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                File mypath2 = new File(directory, packageName + ".png");
                FileOutputStream fos2 = null;
                try {
                    fos2 = new FileOutputStream(mypath2);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos2);
                    fos2.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

//                cv.put(PhilPad.Apps.COLUMN_NAME_IMAGEFILE, iconUri.toString());
                cv.put(PhilPad.Apps.COLUMN_NAME_IMAGEFILE, "file://" + mypath.getPath());
                Uri uri = getContentResolver().insert(PhilPad.Apps.CONTENT_URI, cv);
                if (bitmap != null) {
                    bitmap.recycle();
                    bitmap = null;
                }

            }


            final int newVersion = Utils.getIntPref(TutorialActivity.this, "new_version", 1);
            final int oldVersion = Utils.getIntPref(TutorialActivity.this, "old_version", 0);
            if (oldVersion == 0 || oldVersion == 1) {
                L.d("oldVersion == 0 || oldVersion == 1");
                Utils.setIntPref(TutorialActivity.this, "old_version", newVersion);
            } else if (newVersion != oldVersion) {
                L.d("newVersion != oldVersion");
                if (oldVersion <= 126) {
                    Utils.updateDbForMakeGroupKey(TutorialActivity.this);
                    Utils.setIntPref(TutorialActivity.this, "old_version", newVersion);
                }
            }
            return c;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            mProgressBar.setProgress(values[0]);
            /*pager.setCurrentItem((int) (values[0] / ((mProgressBar.getMax() / 4.0f))), true);*/
        }

        protected void onPostExecute(Cursor result) {
            PhilPad.Pads.getPadItemInfosCursorInGroup(mContext, D.GROUPID_GROUND, 4 * 4);
/*
            pager.setCurrentItem(NUM_PAGES - 1, true);
*/
            mProgressBar.setVisibility(View.INVISIBLE);
/*
            skip.setEnabled(true);
*/
            done.setEnabled(true);
            startButtonTutorial.setVisibility(View.VISIBLE);
            done.setVisibility(View.GONE);

            next.setEnabled(true);
           /* if (progressDialog != null) {
                try {
                    Intent bi = new Intent(mContext, SelectItemActivity.class);
                    bi.putExtra(PadUtils.INTENT_DATA_GROUPID, 0);
                    bi.putExtra(PadUtils.INTENT_REQUEST_TYPE, D.REQUEST_TYPE_TUTORIAL);
                    mContext.startActivity(bi);
                    finish();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                progressDialog = null;
            }*/
        }
    }
}
