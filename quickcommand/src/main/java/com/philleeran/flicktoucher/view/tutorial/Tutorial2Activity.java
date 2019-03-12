
package com.philleeran.flicktoucher.view.tutorial;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.philleeran.flicktoucher.PadUtils;
import com.philleeran.flicktoucher.PhilPadApplication;
import com.philleeran.flicktoucher.R;
import com.philleeran.flicktoucher.db.PhilPad;
import com.philleeran.flicktoucher.utils.L;
import com.philleeran.flicktoucher.view.settings.SettingsActivity;

public class Tutorial2Activity extends AppCompatActivity {

    static final int NUM_PAGES = 5;

    LinearLayout circles;
    /*
        Button skip;
    */
    public static int OVERLAY_PERMISSION_REQ_CODE = 1234;

    public float DPSCALE;

    private int mDownX;
    private int mDownY;
    private int mX;
    private int mY;
    private boolean bStarted = false;
    private int mStep = 1;
    private boolean bStarted2 = false;

    private Button startButton;

    private Context mContext;
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
                    ((TextView) text_head).setText(R.string.welcom_content_06_4);
                    mShowPopup = true;
                    break;
            }
        }

    };
    private View text_head;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        DPSCALE = getResources().getDisplayMetrics().density;
        setContentView(R.layout.activity_tutorial2);

        mContext = this;

        mVibrator = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);

        final View backgroundView = findViewById(R.id.welcome_fragment);
        text_head = findViewById(R.id.heading);
       /*     View object1 = page.findViewById(R.id.a000);
            View object9 = page.findViewById(R.id.a010);
            View object11 = page.findViewById(R.id.a007);*/

        final ImageView imageRound = (ImageView) findViewById(R.id.image_round);
        final ImageView imageArrow = (ImageView) findViewById(R.id.image_arrow);
        final ImageView imageMain = (ImageView) findViewById(R.id.welcome_tutorial_main);
        final ImageView imageMainNext = (ImageView) findViewById(R.id.welcome_tutorial_main_next);
        final ImageView imageMenu = (ImageView) findViewById(R.id.welcome_tutorial_menu);
        final TextView tutorialTextView = (TextView) findViewById(R.id.turorial);
        startButton = (Button) findViewById(R.id.button_start);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (!Settings.canDrawOverlays(mContext)) {
                        Toast.makeText(Tutorial2Activity.this, R.string.need_to_draw_overlay, Toast.LENGTH_LONG).show();
                        ComponentName component = new ComponentName("com.philleeran.flicktoucher", "com.philleeran.flicktoucher.service.PhilPadService");
                        Intent i = new Intent().setComponent(component);
                        stopService(i);

                        PhilPad.Settings.putBoolean(getContentResolver(), PhilPad.Settings.SETTINGS_KEY_IS_SERVICE_ENABLE, false);

                        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:" + getPackageName()));
                        startActivityForResult(intent, OVERLAY_PERMISSION_REQ_CODE);


                        return;
                    }
                }

                PhilPad.Settings.putString(mContext.getContentResolver(), PhilPad.Settings.SETTINGS_KEY_PAD_SIZE, "5");
                PhilPad.Settings.putBoolean(getContentResolver(), PhilPad.Settings.SETTINGS_KEY_COMPLETE_TOTURIAL, true);
                PhilPad.Settings.putBoolean(getContentResolver(), PhilPad.Settings.SETTINGS_KEY_IS_SERVICE_ENABLE, true);

                PhilPadApplication.tracker().send(new HitBuilders.EventBuilder().setCategory("tutorial").setAction("tutorial end").build());

                startActivity(new Intent(mContext, SettingsActivity.class));

                finish();
                overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out);
            }
        });
        if (mStep == 3) {
            if (imageRound != null)
                imageRound.setVisibility(View.INVISIBLE);
            if (imageArrow != null) {
                imageArrow.clearAnimation();
                imageArrow.setVisibility(View.INVISIBLE);
            }
            if (text_head != null) {
                ((TextView) text_head).setText(R.string.welcom_content_06_2);
            }
        } else if (mStep == 2) {
            if (text_head != null) {
                ((TextView) text_head).setText(R.string.welcom_content_06);
            }
            if (imageRound != null) {
                Animation myFadeInAnimation = AnimationUtils.loadAnimation(mContext, R.anim.fade_in);
                Animation myFadeOutAnimation = AnimationUtils.loadAnimation(mContext, R.anim.fade_out);
                imageRound.startAnimation(myFadeInAnimation);
                imageRound.startAnimation(myFadeOutAnimation);
            }
            if (imageArrow != null) {
                Animation myFadeInAnimation = AnimationUtils.loadAnimation(mContext, R.anim.fade_in);
                Animation myFadeOutAnimation = AnimationUtils.loadAnimation(mContext, R.anim.fade_out);
                imageArrow.startAnimation(myFadeInAnimation);
                imageArrow.startAnimation(myFadeOutAnimation);
            }
        } else if (mStep == 1) {
            if (text_head != null) {
                ((TextView) text_head).setText(R.string.welcom_content_05);
            }
            if (imageRound != null) {
                imageRound.setVisibility(View.VISIBLE);
                Animation myFadeInAnimation = AnimationUtils.loadAnimation(mContext, R.anim.fade_in);
                Animation myFadeOutAnimation = AnimationUtils.loadAnimation(mContext, R.anim.fade_out);
                imageRound.startAnimation(myFadeInAnimation);
                imageRound.startAnimation(myFadeOutAnimation);
            }
            if (imageArrow != null) {
                Animation myFadeInAnimation = AnimationUtils.loadAnimation(mContext, R.anim.fade_in);
                Animation myFadeOutAnimation = AnimationUtils.loadAnimation(mContext, R.anim.fade_out);
                imageArrow.startAnimation(myFadeInAnimation);
                imageArrow.startAnimation(myFadeOutAnimation);
            }
        }

        if (imageRound != null) {
             /*   Animation myFadeInAnimation = AnimationUtils.loadAnimation(mContext, R.anim.fade_in);
                Animation myFadeOutAnimation = AnimationUtils.loadAnimation(mContext, R.anim.fade_out);
                myImageView.startAnimation(myFadeInAnimation);
                myImageView.startAnimation(myFadeOutAnimation);*/
            imageRound.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {

                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            mDownX = (int) event.getX();
                            mDownY = (int) event.getY();
                            mVibrator.vibrate(20);

                            break;
                        case MotionEvent.ACTION_MOVE:
                            mX = (int) event.getX();
                            mY = (int) event.getY();
                            int distance = PadUtils.getDistance((int) mDownX, (int) mDownY, (int) mX, (int) mY);
                            if (distance > 30 * DPSCALE) {
                                int diffX = mX - mDownX;
                                int diffY = mY - mDownY;
                                if (bStarted == false) {

                                    if (imageMain != null) {
                                        imageMain.setImageResource(R.drawable.tutorial_2_1);
                                    }
                                    if (imageRound != null) {
//                                        imageRound.clearAnimation();
                                        imageRound.setAlpha(0.3f);
                                    }
                                    if (imageArrow != null) {
//                                        imageArrow.clearAnimation();
                                        imageArrow.setAlpha(0.3f);
                                    }
                                    if (imageMainNext != null) {
                                        imageMainNext.setVisibility(View.VISIBLE);
                                        Animation myFadeInAnimation = AnimationUtils.loadAnimation(mContext, R.anim.fade_in);
                                        Animation myFadeOutAnimation = AnimationUtils.loadAnimation(mContext, R.anim.fade_out);
                                        imageMainNext.startAnimation(myFadeInAnimation);
                                        imageMainNext.startAnimation(myFadeOutAnimation);
                                    }
                                    if (text_head != null) {
                                        if (mStep == 1) {
                                            ((TextView) text_head).setText(R.string.welcom_content_05_1);
                                        } else if (mStep == 2) {
                                            ((TextView) text_head).setText(R.string.welcom_content_06_1);
                                        }
                                    }

                                    mVibrator.vibrate(20);
                                    bStarted = true;

                                } else {
                                    if (mStep == 2) {
                                        if (event.getX() > -190 * DPSCALE && event.getX() < -130 * DPSCALE && event.getY() > 65 * DPSCALE && event.getY() < 125 * DPSCALE) {
                                            if (bStarted2 == false) {
                                                bStarted2 = true;
                                                mVibrator.vibrate(20);
                                                Message msg1 = new Message();
                                                msg1.what = HANDLER_HOVER_ONSELECTED;
                                                ((TextView) text_head).setText(R.string.welcom_content_06_3);
                                                ((TextView) text_head).setTextColor(Color.RED);

                                                mHandler.sendMessageDelayed(msg1, 1500);
                                            }
                                        } else {
                                            if (bStarted2) {
                                                ((TextView) text_head).setText(R.string.welcom_content_06_1);
                                                ((TextView) text_head).setTextColor(Color.WHITE);
                                                mHandler.removeMessages(HANDLER_HOVER_ONSELECTED);
                                                bStarted2 = false;
                                                mShowPopup = false;
                                            }
                                        }
                                    } else {
                                        if (event.getX() > -190 * DPSCALE && event.getX() < -130 * DPSCALE && event.getY() > 65 * DPSCALE && event.getY() < 125 * DPSCALE) {
                                            if (bStarted2 == false) {
                                                bStarted2 = true;
                                                mVibrator.vibrate(20);
                                            }
                                        } else {
                                            if (bStarted2) {
                                                bStarted2 = false;
                                            }
                                        }
                                    }

                                }
                            }
                            break;
                        case MotionEvent.ACTION_UP:
                        case MotionEvent.ACTION_CANCEL:
                            boolean bSuccess = false;
                            bSuccess = false;
                            if (!mShowPopup) {
                                if (imageArrow != null) {
                                    imageMain.setImageResource(R.drawable.tutorial_1_1);
                                }
                                if (imageRound != null) {
                                    imageRound.setVisibility(View.VISIBLE);
                                    imageRound.setAlpha(1.0f);
                                    Animation myFadeInAnimation = AnimationUtils.loadAnimation(mContext, R.anim.fade_in);
                                    Animation myFadeOutAnimation = AnimationUtils.loadAnimation(mContext, R.anim.fade_out);
                                    imageRound.startAnimation(myFadeInAnimation);
                                    imageRound.startAnimation(myFadeOutAnimation);
                                }
                                if (imageArrow != null) {
                                    imageArrow.setVisibility(View.VISIBLE);
                                    imageArrow.setAlpha(1.0f);
                                    Animation myFadeInAnimation = AnimationUtils.loadAnimation(mContext, R.anim.fade_in);
                                    Animation myFadeOutAnimation = AnimationUtils.loadAnimation(mContext, R.anim.fade_out);
                                    imageArrow.startAnimation(myFadeInAnimation);
                                    imageArrow.startAnimation(myFadeOutAnimation);
                                }
                            }
                            if (imageMainNext != null) {
                                imageMainNext.clearAnimation();
                                imageMainNext.setVisibility(View.INVISIBLE);
                            }
                            if (bStarted) {
                                bStarted = false;
                                bStarted2 = false;
                                ((TextView) text_head).setText(R.string.welcom_content_05);
                                L.d("feelpass x : " + event.getX() / DPSCALE);
                                L.d("feelpass y : " + event.getY() / DPSCALE);
                                if (event.getX() > -190 * DPSCALE && event.getX() < -130 * DPSCALE && event.getY() > 65 * DPSCALE && event.getY() < 125 * DPSCALE) {

                                    if (mStep == 1) {
                                        bSuccess = true;
                                        PhilPadApplication.tracker().send(new HitBuilders.EventBuilder().setCategory("tutorial").setAction("tutorial 1").build());

                                        backgroundView.setBackgroundColor(0xff00bcd4);

                                        mStep = 2;
                                        tutorialTextView.setText(R.string.welcom_head_06);

                                        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                                        builder.setMessage(R.string.dialog_tutorial_2).setCancelable(false).setPositiveButton(R.string.dialog_yes, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {

                                            }
                                        }).create().show();


                                    }

                                    if (mShowPopup) {
                                        PhilPadApplication.tracker().send(new HitBuilders.EventBuilder().setCategory("tutorial").setAction("tutorial 2").build());
                                        imageMenu.setVisibility(View.VISIBLE);
                                        ((TextView) text_head).setText(R.string.welcom_content_06_2);
                                        ((TextView) text_head).setTextColor(Color.WHITE);
                                        tutorialTextView.setText(R.string.welcom_head_01);
                                        mStep = 3;
                                        mShowPopup = false;
                                        bSuccess = true;
                                        startButton.setVisibility(View.VISIBLE);
                                        imageRound.clearAnimation();
                                        imageArrow.clearAnimation();
                                        imageRound.setVisibility(View.INVISIBLE);
                                        imageArrow.setVisibility(View.INVISIBLE);


                                        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                                        builder.setMessage(R.string.dialog_tutorial_3).setCancelable(false).setPositiveButton(R.string.dialog_yes, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {

                                            }
                                        }).create().show();
                                    }
                                }

                                if (bSuccess == false) {
                                    mVibrator.vibrate(500);
                                } else {
                                }
                                mShowPopup = false;
                                mHandler.removeMessages(HANDLER_HOVER_ONSELECTED);
                            }
                            break;
                        default:
                            break;
                    }

                    return true;
                }
            });
        }

        ((Button) findViewById(R.id.button_skip)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (!Settings.canDrawOverlays(mContext)) {
                        Toast.makeText(Tutorial2Activity.this, R.string.need_to_draw_overlay, Toast.LENGTH_LONG).show();
                        ComponentName component = new ComponentName("com.philleeran.flicktoucher", "com.philleeran.flicktoucher.service.PhilPadService");
                        Intent i = new Intent().setComponent(component);
                        stopService(i);

                        PhilPad.Settings.putBoolean(getContentResolver(), PhilPad.Settings.SETTINGS_KEY_IS_SERVICE_ENABLE, false);

                        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:" + getPackageName()));
                        startActivityForResult(intent, OVERLAY_PERMISSION_REQ_CODE);


                        return;
                    }
                }

                PhilPad.Settings.putString(mContext.getContentResolver(), PhilPad.Settings.SETTINGS_KEY_PAD_SIZE, "5");
                PhilPad.Settings.putBoolean(getContentResolver(), PhilPad.Settings.SETTINGS_KEY_COMPLETE_TOTURIAL, true);
                PhilPad.Settings.putBoolean(getContentResolver(), PhilPad.Settings.SETTINGS_KEY_IS_SERVICE_ENABLE, true);

                PhilPadApplication.tracker().send(new HitBuilders.EventBuilder().setCategory("tutorial").setAction("tutorial end").build());

                startActivity(new Intent(mContext, SettingsActivity.class));

                finish();
                overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out);
            }
        });



        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage(R.string.dialog_tutorial_1).setCancelable(false).setPositiveButton(R.string.dialog_yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        }).create().show();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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

    @Override
    public void onBackPressed() {
/*
        if (pager.getCurrentItem() == 0) {

        } else {
            pager.setCurrentItem(pager.getCurrentItem() - 1);
        }
*/
    }
}
