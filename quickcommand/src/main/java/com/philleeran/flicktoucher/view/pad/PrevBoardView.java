
package com.philleeran.flicktoucher.view.pad;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.BatteryManager;
import android.os.Handler;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.philleeran.flicktoucher.R;
import com.philleeran.flicktoucher.service.PhilPadService;
import com.philleeran.flicktoucher.utils.L;

public class PrevBoardView extends RelativeLayout {

    private Context mContext;

    public final float DPSCALE = getResources().getDisplayMetrics().density;

    public final int DISPLAY_WIDTH = getResources().getDisplayMetrics().widthPixels;

    public final int DISPLAY_HEIGHT = getResources().getDisplayMetrics().heightPixels;

    private RelativeLayout mMainPadLayout;

    private LayoutParams mMainboardLayoutParams;

    private PhilPadService mPhilPadService;

    private static char digitcharset[] = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'
    };

    private static char lettercharset[] = {
            'A', 'P'
    };

    private Typeface[] fonts;

    private Handler handler = new Handler();

    private final BroadcastReceiver timeIntentReceiver;

    private final Runnable runUpdateTime = new Runnable() {
        public void run() {
            L.d("feepass updateTime");
            updateTime();
        }
    };

    private final Runnable runMoveDisplay = new Runnable() {
        public void run() {
            moveClock();
        }
    };

    private LinearLayout clockDisplay;

    private DisplayView clockDisplayPP;

    private DisplayView clockDisplayTime;

    private DisplayView clockDisplayDay;

    private boolean isRunning;

    private long prefsScreenSaverSpeed = 500;

    private int prefsFont = 0;

    private View backgroundView;

    private AnimatorSet mOpenAnimation;

    private AnimatorSet mCloseAnimation;

    private DisplayView clockDisplayInfo;

    public PrevBoardView(Context context, PhilPadService philPadService) {
        super(context);
        mPhilPadService = philPadService;
        mContext = context;
        setVisibility(View.INVISIBLE);
        initLayout();

        timeIntentReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();

                if (Intent.ACTION_TIME_TICK.equals(action)) {
                    updateTime();
                }
            }
        };
        fonts = new Typeface[1];
        fonts[0] = Typeface.createFromAsset(mContext.getAssets(), "fonts/MouseMemoirs-Regular.ttf");
        loadPrefs();
        resizeClock();

    }

    private void initLayout() {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        mMainPadLayout = (RelativeLayout) inflater.inflate(R.layout.prevboard, null);
        clockDisplay = (LinearLayout) mMainPadLayout.findViewById(R.id.clock_view);
        clockDisplayPP = (DisplayView) mMainPadLayout.findViewById(R.id.clock_view_pp);
        clockDisplayTime = (DisplayView) mMainPadLayout.findViewById(R.id.clock_view_time);
        clockDisplayDay = (DisplayView) mMainPadLayout.findViewById(R.id.clock_view_day);
        clockDisplayInfo = (DisplayView) mMainPadLayout.findViewById(R.id.clock_view_info);

        backgroundView = mMainPadLayout.findViewById(R.id.background_view);
        backgroundView.setAlpha(0.0f);
        mMainboardLayoutParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        mMainboardLayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        addView(mMainPadLayout, mMainboardLayoutParams);
    }

    private void loadPrefs() {

        clockDisplayPP.setColor(Color.WHITE);
        clockDisplayTime.setColor(Color.WHITE);
        clockDisplayDay.setColor(Color.WHITE);
        clockDisplayInfo.setColor(Color.WHITE);
        // layout.setBackgroundColor(prefsBackgroundColor);
        // clockDisplayTime.setBackgroundColor(Color.TRANSPARENT);

    }

    private void resizeClock() {

        char bdigit = digitcharset[0];
        Rect bb = getBoundingBox(String.valueOf(bdigit), fonts[prefsFont], 10);
        int w = bb.width();
        for (int i = 1; i < digitcharset.length; i++) {
            bb = getBoundingBox(String.valueOf(digitcharset[i]), fonts[prefsFont], 10);
            if (bb.width() > w) {
                bdigit = digitcharset[i];
                w = bb.width();
            }
        }

        // determine largest letter
        char bletter = lettercharset[0];
        bb = getBoundingBox(String.valueOf(bletter), fonts[prefsFont], 10);
        w = bb.width();
        for (int i = 1; i < lettercharset.length; i++) {
            bb = getBoundingBox(String.valueOf(lettercharset[i]), fonts[prefsFont], 10);
            if (bb.width() > w) {
                bletter = lettercharset[i];
                w = bb.width();
            }
        }

        String str = String.format("%c%c:%c%c", bdigit, bdigit, bdigit, bdigit);

        if (prefsShowSeconds)
            str = String.format("%s:%c%c", str, bdigit, bdigit);
        if (prefsShowMeridiem)
            str = String.format("%s %cM", str, bletter);
        Rect boundingBoxPP = new Rect(0, 0, (int) (30 * DPSCALE), (int) (20 * DPSCALE));
        float fontSizePP = fitTextToRect(fonts[prefsFont], "AM", boundingBoxPP);

        Rect boundingBoxTime = new Rect(0, 0, (int) (300 * DPSCALE), (int) (50 * DPSCALE));
        float fontSizeTime = fitTextToRect(fonts[prefsFont], str, boundingBoxTime);

        Rect boundingBoxDay = new Rect(0, 0, (int) (300 * DPSCALE), (int) (30 * DPSCALE));
        float fontSizeDay = fitTextToRect(fonts[prefsFont], "88, 88 8888", boundingBoxDay);

        Rect boundingBoxInfo = new Rect(0, 0, (int) (300 * DPSCALE), (int) (20 * DPSCALE));
        float fontSizeInfo = fitTextToRect(fonts[prefsFont], "88, 88 8888", boundingBoxInfo);

        int leftPaddingTime = 0;
        Rect digitBoundsTime = getBoundingBox("8", fonts[prefsFont], fontSizeTime);
        int widthTime = digitBoundsTime.width();
        leftPaddingTime = widthTime * -4;

        int leftPaddingDay = 0;
        Rect digitBoundsDay = getBoundingBox("8", fonts[prefsFont], fontSizeDay);
        int widthDay = digitBoundsDay.width();
        leftPaddingDay = widthDay * -4;

        int leftPaddingPP = 0;
        Rect digitBoundsPP = getBoundingBox("8", fonts[prefsFont], fontSizePP);
        int widthPP = digitBoundsPP.width();
        leftPaddingPP = widthPP * -4;

        int leftPaddingInfo = 0;
        Rect digitBoundsInfo = getBoundingBox("8", fonts[prefsFont], fontSizeInfo);
        int widthInfo = digitBoundsInfo.width();
        leftPaddingInfo = widthInfo * -4;

        clockDisplayPP.setWideTime(str);
        clockDisplayPP.setFont(fonts[prefsFont]);
        clockDisplayPP.setPadding(leftPaddingPP, 0, 0, 0);
        clockDisplayPP.setSize(fontSizePP);

        clockDisplayTime.setWideTime(str);
        clockDisplayTime.setFont(fonts[prefsFont]);
        clockDisplayTime.setPadding(leftPaddingTime, 0, 0, 0);
        clockDisplayTime.setSize(fontSizeTime);

        clockDisplayDay.setWideTime(str);
        clockDisplayDay.setFont(fonts[prefsFont]);
        clockDisplayDay.setPadding(leftPaddingDay, 0, 0, 0);
        clockDisplayDay.setSize(fontSizeDay);

        clockDisplayInfo.setWideTime(str);
        clockDisplayInfo.setFont(fonts[prefsFont]);
        clockDisplayInfo.setPadding(leftPaddingInfo, 0, 0, 0);
        clockDisplayInfo.setSize(fontSizeInfo);

    }

    public void start() {
        setVisibility(View.VISIBLE);

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_DOCK_EVENT);
        filter.addAction(Intent.ACTION_TIME_TICK);
        mContext.registerReceiver(timeIntentReceiver, filter);
        isRunning = true;
        updateTime();
        handler.postDelayed(runMoveDisplay, prefsScreenSaverSpeed);
        startOpenAnimation();

        getBatteryPercentage();

    }

    private void getBatteryPercentage() {
        IntentFilter batteryLevelFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent intent = mContext.registerReceiver(null, batteryLevelFilter);
        int currentLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        int level = -1;
        if (currentLevel >= 0 && scale > 0) {
            level = (currentLevel * 100) / scale;
        }
        MemoryInfo mi = new MemoryInfo();
        ActivityManager activityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        activityManager.getMemoryInfo(mi);
        long availableMegs = mi.availMem / 1048576L;

        clockDisplayInfo.setTime("BATTERY : " + level + "% FREE MEMORY : " + availableMegs + "MB");
        L.d("Battery : " + level + "%");
    }

    public void stop(boolean isAnimation) {
        if (isRunning == true) {
            if (isAnimation) {
                startCloseAnimation();
            } else {
                setVisibility(View.INVISIBLE);
                clockDisplay.setScaleX(1.0f);
                clockDisplay.setScaleY(1.0f);
            }

            mContext.unregisterReceiver(timeIntentReceiver);
            isRunning = false;
            handler.removeCallbacks(runMoveDisplay);
            handler.removeCallbacks(runUpdateTime);
        }

    }

    public void setCustomAlpha(float alpha) {
        backgroundView.setAlpha(alpha);
    }

    private void moveClock() {
        clockDisplayTime.move();
        handler.removeCallbacks(runMoveDisplay);
        handler.postDelayed(runMoveDisplay, prefsScreenSaverSpeed);
    }

    private boolean prefsMilitaryTime = false;

    private boolean prefsLeadingZero = true;

    private boolean prefsShowMeridiem = false;

    private boolean prefsBlinkColon = false;

    private boolean prefsShowSeconds = false;

    private void updateTime() {

        Calendar cal = Calendar.getInstance();
        StringBuffer format = new StringBuffer();

        if (prefsMilitaryTime)
            format.append("kk");
        else if (prefsLeadingZero)
            format.append("hh");
        else
            format.append("h");

        if (prefsBlinkColon && cal.get(Calendar.SECOND) % 2 == 0)
            format.append(" ");
        else
            format.append(":");

        format.append("mm");

        if (prefsShowSeconds) {
            if (prefsBlinkColon && cal.get(Calendar.SECOND) % 2 == 0)
                format.append(" ");
            else
                format.append(":");
            format.append("ss");
        }
        String DATE_FORMAT = "EEE, dd MMM yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.US);
        long time = System.currentTimeMillis();
        Date date = new Date(time);

        clockDisplayPP.setTime(cal.get(Calendar.HOUR_OF_DAY) >= 12 ? "PM" : "AM");

        clockDisplayTime.setTime(DateFormat.format(format.toString(), cal));

        clockDisplayDay.setTime(sdf.format(date));

        if (isRunning) {
            handler.removeCallbacks(runUpdateTime);
            handler.postDelayed(runUpdateTime, 1000);
        }

    }

    private Rect getBoundingBox(String text, Typeface font, float size) {
        Rect r = new Rect(0, 0, 0, 0);
        float widths[] = new float[text.length()];
        float width = 0;
        Paint paint = new Paint(0);
        paint.setTypeface(font);
        paint.setTextSize(size);
        paint.getTextBounds(text, 0, text.length(), r);
        paint.getTextWidths(text, widths);
        for (float w : widths)
            width += w;
        r.right = (int) width;
        return r;
    }

    private float fitTextToRect(Typeface font, String text, Rect fitRect) {

        int width = fitRect.width();
        int height = fitRect.height();

        int minGuess = 0;
        int maxGuess = 640;
        int guess = 320;

        Rect r;
        boolean lastGuessTooSmall = true;

        for (int i = 0; i < 32; i++) {

            if (minGuess + 1 == maxGuess) {
                r = getBoundingBox(text, font, guess);
                return minGuess;
            }

            r = getBoundingBox(text, font, guess);
            if (r.width() > width || r.height() > height) {
                maxGuess = guess;
                lastGuessTooSmall = false;

            } else {
                minGuess = guess;
                lastGuessTooSmall = true;
            }
            guess = (minGuess + maxGuess) / 2;
        }

        if (lastGuessTooSmall)
            return maxGuess;
        else
            return minGuess;

    }

    public void startOpenAnimation() {
        if (mOpenAnimation == null) {
            createOpenAnimation();
        }
        if (mOpenAnimation.isStarted()) {
            return;
        }

        mOpenAnimation.start();
    }

    public void startCloseAnimation() {
        if (mCloseAnimation == null) {
            createCloseAnimation();
        }
        if (mCloseAnimation.isStarted()) {
            return;
        }

        mCloseAnimation.start();
    }

    private void createOpenAnimation() {
        ObjectAnimator translationXAnim = ObjectAnimator.ofFloat(clockDisplay, "translationX", -50 * DPSCALE, 0).setDuration(300);
        translationXAnim.setInterpolator(new AccelerateInterpolator());

        ObjectAnimator translationYAnim = ObjectAnimator.ofFloat(clockDisplayInfo, "translationY", 50 * DPSCALE, 0).setDuration(400);
        translationYAnim.setInterpolator(new AccelerateInterpolator());

        ObjectAnimator alphaAnim = ObjectAnimator.ofFloat(backgroundView, "alpha", 0.0f, 0.7f).setDuration(300);
        alphaAnim.setInterpolator(new AccelerateInterpolator());
        mOpenAnimation = new AnimatorSet();
        mOpenAnimation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }
        });
        ((AnimatorSet) mOpenAnimation).playTogether(translationXAnim, translationYAnim, alphaAnim);
    }

    private void createCloseAnimation() {
        ObjectAnimator scaleYAnim = ObjectAnimator.ofFloat(clockDisplay, "scaleY", 1.0f, 0.1f).setDuration(300);
        scaleYAnim.setInterpolator(new AccelerateInterpolator());
        mCloseAnimation = new AnimatorSet();
        mCloseAnimation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                setVisibility(View.INVISIBLE);
                clockDisplay.setScaleX(1.0f);
                clockDisplay.setScaleY(1.0f);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationStart(Animator animation) {

                backgroundView.setAlpha(0.0f);
                super.onAnimationStart(animation);
            }
        });
        ((AnimatorSet) mCloseAnimation).playTogether(scaleYAnim);
    }

}
