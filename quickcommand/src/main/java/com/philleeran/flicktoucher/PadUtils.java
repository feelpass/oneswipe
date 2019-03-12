
package com.philleeran.flicktoucher;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.Toast;

import com.philleeran.flicktoucher.db.PadItemInfo;
import com.philleeran.flicktoucher.db.PhilPad;
import com.philleeran.flicktoucher.utils.D;
import com.philleeran.flicktoucher.utils.L;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.Random;

public class PadUtils {

    public final static String ACTION_PHILPAD_SERVICE = "com.philleeran.flicktoucher.service.PhilPadService";
    public final static String ACTION_PHILPAD_PACKAGE= "com.philleeran.flicktoucher";

    public final static String INTENT_DATA_APPLISTREQUEST_TYPE = "allapplicationrequesttype";

    public final static int APPLISTREQUEST_TYPE_SELECT_APPLICATION = 0;

    public final static int APPLISTREQUEST_TYPE_SELECT_GESTURE_LAUNCH = 1;

    public final static String INTENT_DATA_GESTURE_TYPE = "intent_gesture_type";

    public final static String INTENT_DATA_MIMETYPE = "mime";

    public final static String INTENT_DATA_GROUPID = "groupid";

    public final static String INTENT_DATA_LISTID = "listid";

    public final static String INTENT_DATA_PACKAGENAME = "packagename";

    public final static String INTENT_REQUEST_TYPE = "request_type";

    public final static String SHARED_PREF_ISFIRST = "pref.isfirst";

    public final static String SHARED_PREF_LASTGROUPID = "lastest.group.id";

    final static String DO_ACTION_HOME_KEY = "do.action.home.key";

    final static String DO_ACTION_INDICATOR = "do_action_indicator";

    final static String DO_ACTION_RECENTAPP = "do_action_recentapp";

    
    static public boolean killTask(String packagename) {
        try {
            Runtime.getRuntime().exec(new String[] {
                    "su", "-c", "am force-stop " + packagename
            });
            return true;
        } catch (Exception e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
            return false;
        }
    }

    static public boolean rootDetected() {

        try {
            Runtime.getRuntime().exec("su");
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public static Bitmap getBitmapFromDrawable(Context context, Drawable drawable, int w, int h) {
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);

        Paint p = new Paint();
        p.setDither(true);
        p.setFlags(Paint.ANTI_ALIAS_FLAG);
        int height = drawable.getIntrinsicHeight();
        int width = drawable.getIntrinsicWidth();
        if (width > height) {
            drawable.setBounds(0, 0, w, (int) (h * ((float) height / width)));
        } else {
            drawable.setBounds(0, 0, (int) (w * ((float) height / width)), h);
        }
        drawable.draw(canvas);

        return bitmap;
    }

    public static int randomColor() {
        Random random = new Random();
        return Color.rgb(random.nextInt(255), random.nextInt(255), random.nextInt(255));
    }

    public static int getDistance(int x1, int y1, int x2, int y2) {
        return (int) Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
    }

    public static void setBlickAnimation(View view, int color, float startAlpha, float endAlpha, int duration) {
        Animation anim = new AlphaAnimation(startAlpha, endAlpha);
        anim.setDuration(duration);
        anim.setStartOffset(20);
        anim.setRepeatMode(Animation.REVERSE);
        anim.setRepeatCount(Animation.INFINITE);
        view.setBackgroundColor(color);
        view.startAnimation(anim);
    }

    public static void setClickAnimation(final View imageView, int color, float startAlpha, float endAlpha, int duration) {
        Animation anim = new AlphaAnimation(startAlpha, endAlpha);
        anim.setDuration(duration);
        anim.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                imageView.setBackgroundColor(Color.TRANSPARENT);
            }
        });
        imageView.setBackgroundColor(color);
        imageView.startAnimation(anim);
    }

    public static void setFunctionItem(Context context, int functionType, int groupId, int positionId, String mypath, int padSize) {
        PhilPad.Pads.setPadItem(context, groupId, positionId, PhilPad.Pads.PAD_TYPE_TOOLS, "function"+ String.valueOf(functionType), null, mypath, 0, String.valueOf(functionType));
        PhilPad.Pads.setGroupIcon(context, groupId, padSize);
    }

    public static String makeImageIcon(Context context, Bitmap bitmap, float DPSCALE, String packageName, int groupId, int positionId) {
        ContextWrapper cw = new ContextWrapper(context);
        File directory = cw.getDir(D.DIR_PATH_DEFAULT, Context.MODE_PRIVATE);
        File mypath = new File(directory, "function_" + packageName + "_" + groupId + "_" + positionId + "_" + ".png");
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        L.d("makeImageIcon : " + mypath.getPath());
        return "file://" + mypath.getPath();
    }

    public static void setSlideAnimation(Context context, final ImageView backgroundImage, int gestureCommand, int padding) {
        PadItemInfo info = PhilPad.Pads.getPadItemInfo(context.getContentResolver(), D.GROUPID_GESTURE, gestureCommand);
        if (info != null && info.getType() != -1) {
            Picasso.with(context).load(info.getImageFileName()).into(backgroundImage, new Callback() {
                @Override
                public void onSuccess() {
                    backgroundImage.setVisibility(View.VISIBLE);
                }

                @Override
                public void onError() {

                }
            });
        } else {
            PadUtils.ToastShort(context, R.string.pref_gesture_summary);
            backgroundImage.setVisibility(View.INVISIBLE);
        }
    }

    public static String getDeviceInfo(Context context) {
        final PackageManager pm = context.getPackageManager();
        PackageInfo pi = null;
        try {
            pi = pm.getPackageInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        StringBuilder builder = new StringBuilder();
        builder.append("ANDROID_VERSION=")//
                .append(android.os.Build.VERSION.RELEASE)//
                .append("\n")//
                .append("APP_VERSION_NAME=")//
                .append(pi.versionName)//
                .append("\n")//
                .append("BRAND=")//
                .append(android.os.Build.BRAND)//
                .append("\n")//
                .append("PHONE_MODEL=")//
                .append(android.os.Build.MODEL)//
                .append("\n");
        return builder.toString();
    }

    public static void Toast(Context context, int stringId) {
        Toast toast = android.widget.Toast.makeText(context, stringId, Toast.LENGTH_LONG);
        toast.show();
    }

    public static void ToastShort(Context context, int stringId) {
        Toast toast = android.widget.Toast.makeText(context, stringId, Toast.LENGTH_SHORT);
        toast.show();
    }
    public static void ToastShort(Context context, String message) {
        Toast toast = android.widget.Toast.makeText(context, message, Toast.LENGTH_SHORT);
        toast.show();
    }

    public static void ToastLong(Context context, int stringId) {
        Toast toast = android.widget.Toast.makeText(context, stringId, Toast.LENGTH_LONG);
        toast.show();
    }

    public static void ToastLong(Context context, String message) {
        Toast toast = android.widget.Toast.makeText(context, message, Toast.LENGTH_LONG);
        toast.show();
    }


    public static void Toast(Context context, int stringId, int gravity, int xOffset, int yOffset) {
        Toast toast = android.widget.Toast.makeText(context, stringId, Toast.LENGTH_LONG);
        toast.setGravity(gravity, xOffset, yOffset);
        toast.show();
    }

    public static int getPadType(int type) {
        switch (type) {
            case PhilPad.Pads.PAD_TYPE_APPLICATION:
                return R.string.add_link_list_application;
            case PhilPad.Pads.PAD_TYPE_GROUP:
                return R.string.add_link_list_group;
            case PhilPad.Pads.PAD_TYPE_TOOLS:
                return R.string.add_link_list_tools;
            case PhilPad.Pads.PAD_TYPE_SHORTCUT:
                return R.string.add_link_list_shortcut;
            case PhilPad.Pads.PAD_TYPE_WIDGET:
                return R.string.add_link_list_widget;
            default:
                return -1;
        }
    }

    private static class TIME_MAXIMUM {
        public static final int SEC = 60;

        public static final int MIN = 60;

        public static final int HOUR = 24;

        public static final int DAY = 30;

        public static final int MONTH = 12;
    }

    public static String formatTimeString(Context context, Date tempDate) {

        long curTime = System.currentTimeMillis();
        long regTime = tempDate.getTime();
        long diffTime = (curTime - regTime) / 1000;

        String msg = null;
        if (diffTime < TIME_MAXIMUM.SEC) {
            // sec
            msg = context.getString(R.string.format_since_justnow);
        } else if ((diffTime /= TIME_MAXIMUM.SEC) < TIME_MAXIMUM.MIN) {
            // min
            msg = diffTime + context.getString(diffTime == 1 ? R.string.format_since_minute : R.string.format_since_minutes);
        } else if ((diffTime /= TIME_MAXIMUM.MIN) < TIME_MAXIMUM.HOUR) {
            // hour
            msg = diffTime + context.getString(diffTime == 1 ? R.string.format_since_hour : R.string.format_since_hours);
        } else if ((diffTime /= TIME_MAXIMUM.HOUR) < TIME_MAXIMUM.DAY) {
            // day
            msg = diffTime + context.getString(diffTime == 1 ? R.string.format_since_day : R.string.format_since_days);
        } else if ((diffTime /= TIME_MAXIMUM.DAY) < TIME_MAXIMUM.MONTH) {
            // day
            msg = diffTime + context.getString(diffTime == 1 ? R.string.format_since_month : R.string.format_since_months);
        } else {
            msg = diffTime + context.getString(diffTime == 1 ? R.string.format_since_year : R.string.format_since_years);
        }

        return msg;
    }

    public static int getBitmapOfWidth(String fileName) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(fileName, options);
            return options.outWidth;
        } catch (Exception e) {
            return 0;
        }
    }

    /** Get Bitmap's height **/
    public static int getBitmapOfHeight(String fileName) {

        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(fileName, options);

            return options.outHeight;
        } catch (Exception e) {
            return 0;
        }
    }

    public static Bitmap getBitmapByDownSampling(String fileName) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(fileName, options);
            int scale = 1;
            if (options.outHeight > 1024 || options.outWidth > 1024) {
                scale = (int) Math.pow(2, (int) Math.round(Math.log(1024 / (double) Math.max(options.outHeight, options.outWidth)) / Math.log(0.5)));
            }
            options.inJustDecodeBounds = false;
            options.inSampleSize = scale;

            return BitmapFactory.decodeFile(fileName, options);

        } catch (Exception e) {
            return null;
        }

    }

    public static Intent getOpenFacebookIntent(Context context) {
       try {
            context.getPackageManager().getPackageInfo("com.facebook.katana", 0);
            return new Intent(Intent.ACTION_VIEW, Uri.parse("fb://page/827601337256582"));
        } catch (Exception e) {
            return new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/sidepadlaundher"));
        }
    }


    public static boolean isUsageAccessEnable(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(context.getPackageName(), 0);
            AppOpsManager appOpsManager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
            int mode = appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, applicationInfo.uid, applicationInfo.packageName);
            return (mode == AppOpsManager.MODE_ALLOWED);
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

}
