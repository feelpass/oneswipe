
package com.philleeran.flicktoucher.view.pad;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Vibrator;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.analytics.HitBuilders;
import com.philleeran.flicktoucher.view.pad.DragDropGridView.OnDropListener;
import com.philleeran.flicktoucher.PadUtils;
import com.philleeran.flicktoucher.PhilPadApplication;
import com.philleeran.flicktoucher.R;
import com.philleeran.flicktoucher.view.select.file.AddFileOpenActivity;
import com.philleeran.flicktoucher.activity.AppListActivity;
import com.philleeran.flicktoucher.activity.AppShortCutListActivity;
import com.philleeran.flicktoucher.activity.AppWidgetListActivity;
import com.philleeran.flicktoucher.view.select.app.SelectItemActivity;
import com.philleeran.flicktoucher.db.LocalSettings;
import com.philleeran.flicktoucher.db.PadItemInfo;
import com.philleeran.flicktoucher.db.PhilPad;
import com.philleeran.flicktoucher.service.PhilPadService;
import com.philleeran.flicktoucher.utils.D;
import com.philleeran.flicktoucher.utils.L;
import com.philleeran.flicktoucher.utils.Utils;
import com.philleeran.flicktoucher.utils.compat.IntentCompat;
import com.philleeran.flicktoucher.view.pad.adapter.PadGridViewAdapter;
import com.squareup.picasso.Picasso;
import com.u1aryz.android.lib.newpopupmenu.MenuItem;
import com.u1aryz.android.lib.newpopupmenu.PopupMenu;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Stack;

public class PadBoardView extends RelativeLayout implements OnTouchListener, android.view.View.OnKeyListener, PadContract.View {

    private final PadContract.Presenter mPadPresenter;

    private Context mContext;


    public RelativeLayout mMainPadLayout;

    public DragDropGridView mGridView;

    private Vibrator mVibrator;

    private ImageView mGridViewBackground;

    private PadGridViewAdapter mPadGridPagerAdapter;

    private LayoutParams mMainboardLayoutParams;

    private LayoutParams mBackgroundImageViewLayoutParams;

    private LayoutParams mAdmobViewLayoutParams;

    private PhilPadService mPhilPadService;

    private LinearLayout mGridViewLayout;

    public int mGravity;


    private ImageView mBackgroundImageView;

    private Stack<Integer> positionHistoryStack = new Stack<>();

    /**
     * The view to show the ad.
     */
    private AdView adView;
    private AdRequest adRequest;
    private boolean mAdmobEnable = false;

    private boolean mIsPremium;
    private ObjectAnimator mOpenScaleXAnim;
    private Interpolator mInterpolator = new AccelerateDecelerateInterpolator();

    private boolean mShowPopup = false;


    private Toast mToast;
    public boolean mShowFromAssist = false;

    public PadBoardView(Context context, PhilPadService philPadService) {
        super(context);
        mPhilPadService = philPadService;
        mContext = context;

        setVisibility(View.INVISIBLE);

        setOnTouchListener(this);

        mPadPresenter = new PadPresenter(mContext, mPhilPadService);
        mPadPresenter.attachView(this);

        initLayout();


    }


    private void initLayout() {
        L.d("initLayout");

        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));


        PhilPadApplication.tracker().send(new HitBuilders.EventBuilder().setCategory("settings").setAction("padSize").setLabel("" + mPadPresenter.getPadSize()).build());

        if (mPadPresenter.getPadSize() == D.PAD_SIZE_5_5) {
            mMainPadLayout = (RelativeLayout) inflater.inflate(R.layout.mainboard_5_5, null);
        } else if (mPadPresenter.getPadSize() == D.PAD_SIZE_4_4) {
            mMainPadLayout = (RelativeLayout) inflater.inflate(R.layout.mainboard_4_4, null);
        } else if (mPadPresenter.getPadSize() == D.PAD_SIZE_3_3) {
            mMainPadLayout = (RelativeLayout) inflater.inflate(R.layout.mainboard_3_3, null);
        } else if (mPadPresenter.getPadSize() == D.PAD_SIZE_2_2) {
            mMainPadLayout = (RelativeLayout) inflater.inflate(R.layout.mainboard_2_2, null);
        }

        mGridViewLayout = (LinearLayout) mMainPadLayout.findViewById(R.id.gridview_layout);

        mGridView = (DragDropGridView) mMainPadLayout.findViewById(R.id.gridview);
        mGridViewBackground = (ImageView) mMainPadLayout.findViewById(R.id.gridview_background);
        Cursor cursor = PhilPad.Pads.getPadItemInfosCursorInGroup(mContext, D.GROUPID_GROUND, mPadPresenter.getPadSize() * mPadPresenter.getPadSize());

        String[] fromColumns = {
                PhilPad.Pads.COLUMN_NAME_IMAGEFILE
        };
        int[] toViews = {
                R.id.item_image
        };
        mPadGridPagerAdapter = new PadGridViewAdapter(mContext, R.layout.grid_icon_item, cursor, fromColumns, toViews);

        mPadPresenter.setPadGridViewAdapterView(mPadGridPagerAdapter);
        mPadPresenter.setPadGridViewAdapterModel(mPadGridPagerAdapter);

        mGridView.setOnDropListener(onDropListener);
        mGridView.setAdapter(mPadGridPagerAdapter);
        mGridView.setLongClickable(false);


        mPadPresenter.setGridView(mGridView, D.GROUPID_GROUND, mPadGridPagerAdapter);
        mGridView.setOnTouchListener(mPadPresenter);
        mGridView.setOnKeyListener(PadBoardView.this);

        mMainboardLayoutParams = new RelativeLayout.LayoutParams((int) (D.Board.ICON_SIZE * mPadPresenter.getPadSize() * mPadPresenter.getDpScale()), (int) (D.Board.ICON_SIZE * mPadPresenter.getPadSize() * mPadPresenter.getDpScale()));
        mMainboardLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        mMainboardLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

        mBackgroundImageViewLayoutParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        mAdmobViewLayoutParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        mAdmobViewLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        mAdmobViewLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

        mBackgroundImageView = new ImageView(mContext);
        mBackgroundImageView.setCropToPadding(true);
        String filepath = PhilPad.Settings.getString(mContext.getContentResolver(), PhilPad.Settings.SETTINGS_KEY_BACKGROUNDIMAGE_PATH, null);

        if (!TextUtils.isEmpty(filepath)) {
            Picasso.with(mContext).load(filepath).into(mBackgroundImageView);
        }

        addView(mBackgroundImageView, mBackgroundImageViewLayoutParams);
        addView(mMainPadLayout, mMainboardLayoutParams);

        mIsPremium = PhilPad.Settings.getBoolean(mContext.getContentResolver(), PhilPad.Settings.SETTINGS_KEY_IS_PREMIUM, false);
        if (Utils.getIntPref(mContext, "launch.count", 0) > 10) {
            if (mIsPremium == false)
                mAdmobEnable = true;
        }

        mAdmobEnable = false;

        if (mAdmobEnable) {
            adView = new AdView(mContext);
            adView.setAdSize(AdSize.BANNER);
            adView.setAdUnitId(D.Admob.AD_UNIT_ID_PADBOARD);
            addView(adView, mAdmobViewLayoutParams);
            adRequest = new AdRequest.Builder().addTestDevice(D.Admob.AD_DEVELOPER_DEVICE_ID).build();
        }

        mVibrator = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
        setGridViewBackground(true);
        updateCurrentApps(mContext);
    }


    private OnDropListener onDropListener = new OnDropListener() {
        @Override
        public void drop(int from, int to) {

            PadItemInfo fromInfo = PhilPad.Pads.getPadItemInfo(mContext.getContentResolver(), mPadGridPagerAdapter.getGroupId(), from);
            PadItemInfo toInfo = PhilPad.Pads.getPadItemInfo(mContext.getContentResolver(), mPadGridPagerAdapter.getGroupId(), to);

            PhilPad.Pads.setPadItem(mContext, mPadGridPagerAdapter.getGroupId(), to, fromInfo.getType(), fromInfo.getPackageName(), fromInfo.getApplicationName(), fromInfo.getImageFileName(),
                    fromInfo.getToGroupId(), fromInfo.getExtraData());
            PhilPad.Pads.setPadItem(mContext, mPadGridPagerAdapter.getGroupId(), from, toInfo.getType(), toInfo.getPackageName(), toInfo.getApplicationName(), toInfo.getImageFileName(),
                    toInfo.getToGroupId(), toInfo.getExtraData());

            if (mPadGridPagerAdapter.getGroupId() != 0) {
                PhilPad.Pads.setGroupIcon(mContext, mPadGridPagerAdapter.getGroupId(), mPadPresenter.getPadSize());
            }
        }
    };


    @Override
    public void setHideAndGroupIdInit() {
        L.dd();
        if (mShowFromAssist) {
            final String[] hotspotsSharePref = mContext.getResources().getStringArray(R.array.hotspots_sharedpref);
            PhilPad.Settings.putBoolean(mContext.getContentResolver(), hotspotsSharePref[10], false);
            if (mPhilPadService != null)
                mPhilPadService.showHotspotViews();
            mShowFromAssist = false;
        }

        mPhilPadService.setHotspotVisible(true);

        mGridViewBackground.setVisibility(View.INVISIBLE);
        mGridView.setScaleX(1.0f);
        mGridView.setScaleY(1.0f);
        if (mPadPresenter.getDragMode() == true) {
//            mGridView.setLongClickable(false);
            mPadPresenter.setDragMode(false);
            setGridViewBackground(true);
        }
        setVisibility(View.INVISIBLE);

        mPadPresenter.clearHistoryStack();

        positionHistoryStack.clear();
        mPadPresenter.setGroupId(D.GROUPID_GROUND);
        mPadGridPagerAdapter.setGroupId(D.GROUPID_GROUND, mPadPresenter.getPadSize());

        mGridView.setVisibility(View.VISIBLE);

        setAdViewPause();
    }

    @Override
    public PadContract.Presenter getPresenter() {
        return mPadPresenter;
    }

    @Override
    public View getView() {
        return this;
    }

    @Override
    public void updateLayoutParams(int gravity) {
        if (mGridViewBackground != null) {
            mGridViewBackground.setVisibility(View.INVISIBLE);
        }

        mGravity = gravity;
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams((int) (D.Board.ICON_SIZE * mPadPresenter.getPadSize() * mPadPresenter.getDpScale()), (int) (D.Board.ICON_SIZE * mPadPresenter.getPadSize() * mPadPresenter.getDpScale())); // 160
        if (gravity == (Gravity.LEFT | Gravity.BOTTOM)) {
            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        } else if (gravity == (Gravity.RIGHT | Gravity.BOTTOM)) {
            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        } else if (gravity == (Gravity.LEFT | Gravity.TOP)) {
            params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        } else if (gravity == (Gravity.RIGHT | Gravity.TOP)) {
            params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        } else if (gravity == (Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM)) {
            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        }

        if (Configuration.ORIENTATION_PORTRAIT == getResources().getConfiguration().orientation) {
            params.rightMargin = (int) (15 * mPadPresenter.getDpScale());
            params.leftMargin = (int) (15 * mPadPresenter.getDpScale());
            if (mIsPremium || mAdmobEnable == false) {
                params.topMargin = Utils.getStatusBarHeight(mContext);
                params.bottomMargin = (int) (15 * mPadPresenter.getDpScale());
            } else {
                params.topMargin = (int) (50 * mPadPresenter.getDpScale()) + Utils.getStatusBarHeight(mContext);
                params.bottomMargin = (int) (50 * mPadPresenter.getDpScale());
            }
        } else if (Configuration.ORIENTATION_LANDSCAPE == getResources().getConfiguration().orientation) {
            params.rightMargin = (int) (15 * mPadPresenter.getDpScale());
            params.leftMargin = (int) (15 * mPadPresenter.getDpScale());
            if (mIsPremium || mAdmobEnable == false) {
                params.topMargin = Utils.getStatusBarHeight(mContext);
                params.bottomMargin = (int) (15 * mPadPresenter.getDpScale());
            } else {
                params.topMargin = (int) (50 * mPadPresenter.getDpScale()) + Utils.getStatusBarHeight(mContext);
                params.bottomMargin = (int) (50 * mPadPresenter.getDpScale());
            }
        }
        updateViewLayout(mMainPadLayout, params);
    }

    public void updateCurrentApps(final Context context) {

        mPadPresenter.updateCurrentApps(context);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (mPadPresenter.getDragMode() == true) {
                    setHideAndGroupIdInit();
                } else if (mPadPresenter.isHistoryStackEmpty() == false) {
                    int preGroupId = mPadPresenter.popHistoryStack();
                    startAnimation(false, 0, preGroupId);
                } else {
                    setHideAndGroupIdInit();
                }
                break;
            default:
                break;
        }
        return false;
    }

    private AnimatorSet mOpenAnimation;

    private AnimatorSet mCloseAnimation;

    @Override
    public void startAnimation(boolean open, int position, final int groupId) {
        if (PhilPad.Settings.getBoolean(mContext.getContentResolver(), PhilPad.Settings.SETTINGS_KEY_PAD_ANIMATION_ENABLE, true) == false) {
            mPadGridPagerAdapter.setGroupId(groupId, mPadPresenter.getPadSize());
            mPadPresenter.setGroupId(groupId);
            return;
        }


        int expectedPosition = position;

        if (open == false && positionHistoryStack.isEmpty() == false) {
            expectedPosition = positionHistoryStack.pop();
        }

        if (expectedPosition == -1) {
            mGridView.setPivotX(mGridView.getWidth() / 2);
            mGridView.setPivotY(mGridView.getHeight() / 2);
        } else {
            float x = (64 * mPadPresenter.getDpScale() * ((expectedPosition) % mPadPresenter.getPadSize()));
            float y = (64 * mPadPresenter.getDpScale() * ((expectedPosition / mPadPresenter.getPadSize())));
            switch (expectedPosition % mPadPresenter.getPadSize()) {
                case 0:
                    if (mPadPresenter.getPadSize() == D.PAD_SIZE_5_5) {
                        x = (float) (9.4 * mPadPresenter.getDpScale());
                    } else if (mPadPresenter.getPadSize() == D.PAD_SIZE_4_4) {
                        x = (float) (9.8 * mPadPresenter.getDpScale());
                    } else if (mPadPresenter.getPadSize() == D.PAD_SIZE_3_3) {
                        x = (float) (10.5 * mPadPresenter.getDpScale());
                    } else {
                        x = (float) (12.5 * mPadPresenter.getDpScale());
                    }

                    break;
                case 1:
                    if (mPadPresenter.getPadSize() == D.PAD_SIZE_5_5) {
                        x += 20.7 * mPadPresenter.getDpScale();
                    } else if (mPadPresenter.getPadSize() == D.PAD_SIZE_4_4) {
                        x += 24.5 * mPadPresenter.getDpScale();
                    } else if (mPadPresenter.getPadSize() == D.PAD_SIZE_3_3) {
                        x += 32 * mPadPresenter.getDpScale();
                    } else {
                        x += 51 * mPadPresenter.getDpScale();

                    }
                    break;
                case 2:
                    if (mPadPresenter.getPadSize() == D.PAD_SIZE_5_5) {
                        x += (float) 32 * mPadPresenter.getDpScale();
                    } else if (mPadPresenter.getPadSize() == D.PAD_SIZE_4_4) {
                        x += 39.3 * mPadPresenter.getDpScale();
                    } else if (mPadPresenter.getPadSize() == D.PAD_SIZE_3_3) {
                        x += (float) 53.2 * mPadPresenter.getDpScale();

                    } else {

                    }
                    break;
                case 3:
                    if (mPadPresenter.getPadSize() == D.PAD_SIZE_5_5) {
                        x += (43.3) * mPadPresenter.getDpScale();
                    } else if (mPadPresenter.getPadSize() == D.PAD_SIZE_4_4) {
                        x += (64 - 10) * mPadPresenter.getDpScale();
                    } else if (mPadPresenter.getPadSize() == D.PAD_SIZE_3_3) {
                        x += (64 - 10) * mPadPresenter.getDpScale();
                    }
                    break;
                case 4:
                    if (mPadPresenter.getPadSize() == D.PAD_SIZE_5_5) {
                        x += 54.6 * mPadPresenter.getDpScale();
                    }
                    break;

                default:
                    break;
            }
            switch ((int) (expectedPosition / mPadPresenter.getPadSize())) {
                case 0:
                    if (mPadPresenter.getPadSize() == D.PAD_SIZE_5_5) {
                        y = (float) (9.4 * mPadPresenter.getDpScale());
                    } else if (mPadPresenter.getPadSize() == D.PAD_SIZE_4_4) {
                        y = (float) (9.8 * mPadPresenter.getDpScale());
                    } else if (mPadPresenter.getPadSize() == D.PAD_SIZE_3_3) {
                        y = (float) (10.5 * mPadPresenter.getDpScale());
                    } else {
                        y = (float) (12.5 * mPadPresenter.getDpScale());
                    }
                    break;
                case 1:
                    if (mPadPresenter.getPadSize() == D.PAD_SIZE_5_5) {
                        y += (float) 20.7 * mPadPresenter.getDpScale();
                    } else if (mPadPresenter.getPadSize() == D.PAD_SIZE_4_4) {
                        y += (float) 24.5 * mPadPresenter.getDpScale();
                    } else if (mPadPresenter.getPadSize() == D.PAD_SIZE_3_3) {
                        y += 32 * mPadPresenter.getDpScale();
                    } else {
                        y += 51 * mPadPresenter.getDpScale();
                    }
                    break;
                case 2:
                    if (mPadPresenter.getPadSize() == D.PAD_SIZE_5_5) {
                        y += (float) 32 * mPadPresenter.getDpScale();
                    } else if (mPadPresenter.getPadSize() == D.PAD_SIZE_4_4) {
                        y += (float) 39.3 * mPadPresenter.getDpScale();
                    } else if (mPadPresenter.getPadSize() == D.PAD_SIZE_3_3) {
                        y += (float) 53.2 * mPadPresenter.getDpScale();
                    } else {
                        y += (float) 53.2 * mPadPresenter.getDpScale();
                    }
                    break;
                case 3:
                    if (mPadPresenter.getPadSize() == D.PAD_SIZE_5_5) {
                        y += (43.3) * mPadPresenter.getDpScale();
                    } else if (mPadPresenter.getPadSize() == D.PAD_SIZE_4_4) {

                        y += (64 - 10) * mPadPresenter.getDpScale();
                    } else if (mPadPresenter.getPadSize() == D.PAD_SIZE_3_3) {
                        y += (64 - 10) * mPadPresenter.getDpScale();
                    }
                    break;
                case 4:
                    if (mPadPresenter.getPadSize() == D.PAD_SIZE_5_5) {
                        y += 54.6 * mPadPresenter.getDpScale();
                    }
                    break;

                default:
                    break;
            }
            mGridView.setPivotX(x);
            mGridView.setPivotY(y);
        }
        if (open) {
            positionHistoryStack.push(expectedPosition);
//            if (mOpenAnimation == null) {
            createOpenAnimation();
//            }
            mOpenAnimation.removeAllListeners();
            if (!mOpenAnimation.isStarted()) {
                mOpenAnimation.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        super.onAnimationStart(animation);
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (mCloseAnimation == null || (mCloseAnimation != null && !mCloseAnimation.isRunning())) {
                            mPadGridPagerAdapter.setGroupId(groupId, mPadPresenter.getPadSize());
                            mPadPresenter.setGroupId(groupId);
                            mGridView.setScaleX(1.0f);
                            mGridView.setScaleY(1.0f);
                        }
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                    }
                });
                mOpenAnimation.start();
            }
        } else {
            mPadGridPagerAdapter.setGroupId(groupId, mPadPresenter.getPadSize());
            mPadPresenter.setGroupId(groupId);
            createCloseAnimation();
            if (!mCloseAnimation.isStarted()) {
                mCloseAnimation.start();
            }
        }
    }

    private void createOpenAnimation() {
        //0.1875
        mOpenScaleXAnim = ObjectAnimator.ofFloat(mGridView, "scaleX", 1.0f, ((float) D.Board.ICON_SIZE * mPadPresenter.getPadSize()) / D.Board.ICON_INNER_SIZE).setDuration(LocalSettings.mAnimationDurationTime);
        mOpenScaleXAnim.setInterpolator(mInterpolator);
        ObjectAnimator openScaleYAnim = ObjectAnimator.ofFloat(mGridView, "scaleY", 1.0f, ((float) D.Board.ICON_SIZE * mPadPresenter.getPadSize()) / D.Board.ICON_INNER_SIZE).setDuration(LocalSettings.mAnimationDurationTime);
        openScaleYAnim.setInterpolator(mInterpolator);
        mOpenAnimation = new AnimatorSet();
        mOpenAnimation.playTogether(mOpenScaleXAnim, openScaleYAnim);
    }


    private void createCloseAnimation() {
        float value = 1.0f;

        if (mOpenScaleXAnim != null && mOpenScaleXAnim.isRunning())
            value = (Float) mOpenScaleXAnim.getAnimatedValue() / (((float) D.Board.ICON_SIZE * mPadPresenter.getPadSize()) / D.Board.ICON_INNER_SIZE);

        // 5.33 = 256 / 48
        ObjectAnimator closeScaleXAnim = ObjectAnimator.ofFloat(mGridView, "scaleX", ((float) D.Board.ICON_SIZE * mPadPresenter.getPadSize()) / D.Board.ICON_INNER_SIZE * value, 1.0f).setDuration((int) (LocalSettings.mAnimationDurationTime));
        closeScaleXAnim.setInterpolator(mInterpolator);
        ObjectAnimator closeScaleYAnim = ObjectAnimator.ofFloat(mGridView, "scaleY", ((float) D.Board.ICON_SIZE * mPadPresenter.getPadSize()) / D.Board.ICON_INNER_SIZE * value, 1.0f).setDuration((int) (LocalSettings.mAnimationDurationTime));
        closeScaleYAnim.setInterpolator(mInterpolator);
        mCloseAnimation = new AnimatorSet();
        ((AnimatorSet) mCloseAnimation).playTogether(closeScaleXAnim, closeScaleYAnim);
    }


    @Override
    public void setGridViewBackgroundVisibility(int visible) {
        mGridViewBackground.setVisibility(visible);
    }

    @Override
    public void showToast(String message) {
        if (mToast != null) {
            mToast.cancel();
            mToast = null;
        }
        mToast = Toast.makeText(mContext, message, Toast.LENGTH_LONG);
        mToast.show();

    }


    private String getLabelByPackageName(String packageName) {
        PackageManager packageManager = mContext.getPackageManager();
        ApplicationInfo applicationInfo = null;
        try {
            applicationInfo = packageManager.getApplicationInfo(packageName, 0);
        } catch (final NameNotFoundException e) {
        }
        return (String) ((applicationInfo != null) ? packageManager.getApplicationLabel(applicationInfo) : "???");
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_BACK:
                if (event.getAction() == KeyEvent.ACTION_UP) {
                    if (mPadPresenter.isHistoryStackEmpty() == false) {
                        int preGroupId = mPadPresenter.popHistoryStack();
/*
                        mItemListener.setGroupId(preGroupId);
                        mPadGridPagerAdapter.setGroupId(preGroupId);
*/
                        startAnimation(false, 0, preGroupId);
                    } else {
                        setHideAndGroupIdInit();
                    }
                }
                break;
            case KeyEvent.KEYCODE_HOME:
                if (event.getAction() == KeyEvent.ACTION_UP) {
                    setHideAndGroupIdInit();
                }
                break;

            case KeyEvent.KEYCODE_MENU:

                break;
            default:
                break;
        }
        return false;
    }


    /**
     * @param isNormal if isNormal is false : it is DragMode
     */
    @Override
    public void setGridViewBackground(boolean isNormal) {
        Bitmap transparent = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        Bitmap bitmap = null;
        bitmap = Bitmap.createScaledBitmap(transparent, D.Board.ICON_SIZE * mPadPresenter.getPadSize(), D.Board.ICON_SIZE * mPadPresenter.getPadSize(), true);
        if (transparent != null) {
            if (D.RECYCLE) {
                transparent.recycle();
            }
            transparent = null;
        }
        Paint p = new Paint();
        p.setDither(true);
        p.setAntiAlias(true);
        if (isNormal) {
            p.setColor(PhilPad.Settings.getInt(mContext.getContentResolver(), PhilPad.Settings.SETTINGS_KEY_PAD_BACKGROUND_COLOR, Color.BLACK));
        } else {
            p.setColor(Color.CYAN);
        }
        Canvas c = new Canvas(bitmap);
        c.drawRoundRect(new RectF(0, 0, D.Board.ICON_SIZE * mPadPresenter.getPadSize(), D.Board.ICON_SIZE * mPadPresenter.getPadSize()), D.GRID_RECT_ROUND_FACTOR, D.GRID_RECT_ROUND_FACTOR, p);
        Drawable d = new BitmapDrawable(getResources(), bitmap);

        mGridViewLayout.setBackground(d);
        String filepath = PhilPad.Settings.getString(mContext.getContentResolver(), PhilPad.Settings.SETTINGS_KEY_BACKGROUNDIMAGE_PATH, null);
        if (!TextUtils.isEmpty(filepath)) {
            Picasso.with(mContext).load(filepath).into(mBackgroundImageView);

            mBackgroundImageView.setAlpha(1.0f);
            mBackgroundImageView.setVisibility(View.VISIBLE);
            setBackgroundColor(Color.TRANSPARENT);
        } else {
            setBackgroundColor(PhilPad.Settings.getInt(mContext.getContentResolver(), PhilPad.Settings.SETTINGS_KEY_BACKGROUND_COLOR, mContext.getResources().getColor(R.color.color_background)));
            mBackgroundImageView.setVisibility(View.GONE);
        }
    }

    @Override
    public void invalidateViews() {
        mGridView.invalidateViews();
    }

    @Override
    public void setShowPopupEnable(boolean enable) {
        mShowPopup = enable;
    }


    public void setAdViewResume(int gravity) {
        if (mIsPremium == true)
            return;
        if (adView != null && mAdmobEnable == true) {
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.CENTER_HORIZONTAL);
            if ((gravity & Gravity.BOTTOM) == Gravity.BOTTOM) {
                params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            } else if ((gravity & Gravity.TOP) == Gravity.TOP) {
                params.topMargin = (int) (25 * mPadPresenter.getDpScale());
                params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            }

            updateViewLayout(adView, params);
            adView.loadAd(adRequest);
        }
    }

    public void setAdViewPause() {
        if (mIsPremium == true)
            return;
        if (adView != null && mAdmobEnable == true) {
            adView.destroy();
        }
    }

    @Override
    public void vibrate(int time) {
        if (PhilPad.Settings.getBoolean(mContext.getContentResolver(), PhilPad.Settings.SETTINGS_KEY_PAD_HAPTIC_ENABLE, true)) {
            mVibrator.vibrate(time);
        }
    }


    @Override
    public void showOptionMenu(boolean isDynamic) {
        PopupMenu menu = new PopupMenu(mContext);
        menu.setHeaderTitle(mContext.getString(R.string.options));

        // Add Menu (Android menu like style)
        menu.setWidth(300);
        if (!isDynamic) {
            menu.add(D.Icon.PadPopupMenu.Add, R.string.popupmenu_add).setIcon(Utils.getDrawable(mContext, R.drawable.ic_add_24dp));
            menu.add(D.Icon.PadPopupMenu.Delete, R.string.popupmenu_delete).setIcon(Utils.getDrawable(mContext, R.drawable.ic_delete_24dp));
            menu.add(D.Icon.PadPopupMenu.Move, R.string.popupmenu_move).setIcon(Utils.getDrawable(mContext, R.drawable.ic_open_with_24dp));
            menu.add(D.Icon.PadPopupMenu.Rename, R.string.popupmenu_rename).setIcon(Utils.getDrawable(mContext, R.drawable.ic_mode_edit_24dp));
        }

        menu.setOnItemSelectedListener(new PopupMenu.OnItemSelectedListener() {
            @Override
            public void onItemSelected(MenuItem item) {
                switch (item.getItemId()) {
                    case D.Icon.PadPopupMenu.Add: {
                        showOptionMenuDetails();
                    }
                    break;
                    case D.Icon.PadPopupMenu.Move: {
                        mPadPresenter.setDragMode(true);
                        PadUtils.Toast(mContext, R.string.toast_drag_help);
                    }
                    break;
                    case D.Icon.PadPopupMenu.Delete: {
                        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                        builder.setMessage(R.string.dialog_areyousure_delete).setCancelable(true).setPositiveButton(R.string.dialog_yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mPadPresenter.removePadItem();
                                mPadPresenter.setGroupIcon();

                            }
                        }).setNegativeButton(R.string.dialog_no, null).create();
                        AlertDialog alert = builder.create();
                        WindowManager.LayoutParams params = alert.getWindow().getAttributes();
                        params.y = mMainPadLayout.getHeight() / 2;
                        params.gravity = mGravity;
                        alert.getWindow().setAttributes(params);
                        alert.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                        alert.show();
                    }
                    break;
                    case D.Icon.PadPopupMenu.PenWindow: {

                        PadItemInfo info = PhilPad.Pads.getPadItemInfo(mContext.getContentResolver(), mPadPresenter.getGroupId(), mPadPresenter.getStartPosition());
                        try {
                            Intent intent = Intent.parseUri(info.getPackageName(), 0);
                            if (TextUtils.isEmpty(intent.getPackage())) {
                                intent.setPackage(intent.getComponent().getPackageName());
                            }

                            Intent intentMulti = mContext.getPackageManager().getLaunchIntentForPackage(intent.getPackage());
                            intentMulti.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            Utils.makeMultiWindowIntent(intentMulti, 0.6f);
                            mContext.startActivity(intentMulti);
                            setHideAndGroupIdInit();
                        } catch (Exception e) {
                            PadUtils.Toast(mContext, R.string.toast_not_found, mGravity, 0, mMainPadLayout.getHeight() / 2);
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setData(Uri.parse("market://details?id=" + info.getPackageName()));
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            mContext.startActivity(intent);
                        }
                    }
                    break;
                    case D.Icon.PadPopupMenu.PenWindowForLastApp: {
                        PadItemInfo info = PhilPad.Pads.getPadItemInfo(mContext.getContentResolver(), mPadPresenter.getGroupId(), mPadPresenter.getStartPosition());
                        try {
                            ComponentName componentName = ComponentName.unflattenFromString(info.getExtraData());
                            if (componentName != null) {
                                Intent intentMulti = mContext.getPackageManager().getLaunchIntentForPackage(componentName.getPackageName());
                                intentMulti.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                Utils.makeMultiWindowIntent(intentMulti, 0.6f);
                                mContext.startActivity(intentMulti);
                                setHideAndGroupIdInit();
                            } else {
                                Intent intentMulti = mContext.getPackageManager().getLaunchIntentForPackage(info.getExtraData());
                                intentMulti.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                Utils.makeMultiWindowIntent(intentMulti, 0.6f);
                                mContext.startActivity(intentMulti);
                                setHideAndGroupIdInit();
                            }
                        } catch (Exception e) {
                            PadUtils.Toast(mContext, R.string.toast_not_found, mGravity, 0, mMainPadLayout.getHeight() / 2);
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setData(Uri.parse("market://details?id=" + info.getPackageName()));
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            mContext.startActivity(intent);
                        }
                    }
                    break;


                    case D.Icon.PadPopupMenu.Rename: {
                        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                        final EditText editText = new EditText(mContext);
                        final PadItemInfo info = PhilPad.Pads.getPadItemInfo(mContext.getContentResolver(), mPadPresenter.getGroupId(), mPadPresenter.getStartPosition());
                        editText.setText(info.getTitle());
                        builder.setView(editText).setMessage(R.string.popupmenu_rename).setCancelable(true).setPositiveButton(R.string.dialog_yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                PhilPad.Pads.setPadItem(mContext, info.getGroupId(), info.getPositionId(), info.getType(), info.getPackageName(), editText.getText().toString(), info.getImage(), info.getToGroupId(), info.getExtraData());
                            }
                        }).setNegativeButton(R.string.dialog_no, null).create();
                        AlertDialog alert = builder.create();

                        alert.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                        alert.show();
                    }
                    break;
                }
            }
        });


        try {
            PadItemInfo info = PhilPad.Pads.getPadItemInfo(mContext.getContentResolver(), mPadPresenter.getGroupId(), mPadPresenter.getStartPosition());
            if (info != null) {
                if (IntentCompat.EXTRA_WINDOW_MODE != null && info.getType() == PhilPad.Pads.PAD_TYPE_APPLICATION) {
                    Intent intent = Intent.parseUri(info.getPackageName(), 0);

                    String packageName = intent.getComponent().getPackageName();
                    if (!TextUtils.isEmpty(packageName)) {
                        menu.add(D.Icon.PadPopupMenu.PenWindow, R.string.popupmenu_penwindow).setIcon(mContext.getPackageManager().getApplicationIcon(packageName));
                    } else {
                        menu.add(D.Icon.PadPopupMenu.PenWindow, R.string.popupmenu_penwindow);
                    }
                } else if (IntentCompat.EXTRA_WINDOW_MODE != null && info.getType() == PhilPad.Pads.PAD_TYPE_TOOLS) {
                    if (Integer.parseInt(info.getExtraData()) == D.Tools.TOOLS_TYPE_LASTAPP) {
                        Intent intent = Intent.parseUri(info.getPackageName(), 0);

                        String packageName = intent.getComponent().getPackageName();
                        if (!TextUtils.isEmpty(packageName)) {
                            menu.add(D.Icon.PadPopupMenu.PenWindowForLastApp, R.string.popupmenu_penwindow).setIcon(mContext.getPackageManager().getApplicationIcon(packageName));
                        } else {
                            menu.add(D.Icon.PadPopupMenu.PenWindowForLastApp, R.string.popupmenu_penwindow);
                        }
                    }
                }
            }
        } catch (Exception e) {
        }
        try {
            menu.show(mMainPadLayout, mGravity);
        } catch (IllegalStateException e) {

        }
    }

    private void showOptionMenuDetails() {
        PopupMenu menu = new PopupMenu(mContext);
        menu.setHeaderTitle(mContext.getString(R.string.options));
        menu.setOnItemSelectedListener(new PopupMenu.OnItemSelectedListener() {
            @Override
            public void onItemSelected(MenuItem item) {
                switch (item.getItemId()) {
                    case D.Icon.ITEM_SELECTED_FOLDER: {
                        PadItemInfo info = PhilPad.Pads.getPadItemInfo(mContext.getContentResolver(), mPadPresenter.getGroupId(), mPadPresenter.getStartPosition());
                        if (info != null && (info.getType() != PhilPad.Pads.PAD_TYPE_NULL)) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                            builder.setMessage(R.string.dialog_areyousure_overwrite).setCancelable(true).setPositiveButton(R.string.dialog_yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Bitmap transparent = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
                                    Bitmap bitmap = null;
                                    bitmap = Bitmap.createScaledBitmap(transparent, (int) (96 * mPadPresenter.getDpScale()), (int) (96 * mPadPresenter.getDpScale()), true);
                                    if (transparent != null) {
                                        if (D.RECYCLE) {
                                            transparent.recycle();
                                        }
                                        transparent = null;
                                    }

                                    Paint p = new Paint();
                                    p.setDither(true);
                                    p.setFlags(Paint.ANTI_ALIAS_FLAG);
                                    int folderColor = PhilPad.Settings.getInt(mContext.getContentResolver(), PhilPad.Settings.SETTINGS_KEY_PAD_FOLDER_COLOR, Utils.getColor(mContext, R.color.color_folder_background));
                                    p.setColor(folderColor);
//                                        p.setAlpha(100);
                                    Paint pp = new Paint();
                                    pp.setDither(true);
                                    pp.setFlags(Paint.ANTI_ALIAS_FLAG);
                                    Canvas c = new Canvas(bitmap);
                                    c.drawRoundRect(new RectF(0, 0, (int) (96 * mPadPresenter.getDpScale()), (int) (96 * mPadPresenter.getDpScale())), D.GRID_RECT_ROUND_FACTOR, D.GRID_RECT_ROUND_FACTOR, p);

                                    ContextWrapper cw = new ContextWrapper(mContext);
                                    File directory = cw.getDir(D.DIR_PATH_DEFAULT, Context.MODE_PRIVATE);
                                    File mypath = new File(directory, "group_" + mPadPresenter.getGroupId() + "_" + mPadPresenter.getStartPosition() + ".png");
                                    FileOutputStream fos = null;
                                    try {
                                        fos = new FileOutputStream(mypath);
                                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                                    } catch (FileNotFoundException e) {
                                        e.printStackTrace();
                                    } finally {
                                        if (fos != null) {
                                            try {
                                                fos.close();
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                    PhilPad.Pads.setPadItem(mContext, mPadPresenter.getGroupId(), mPadPresenter.getStartPosition(), PhilPad.Pads.PAD_TYPE_GROUP, null, mContext.getString(R.string.unnamed_folder),
                                            "file://" + mypath.getPath(), 0, null);
                                    if (bitmap != null) {
                                        L.d("recycle");
                                        if (D.RECYCLE) {
                                            bitmap.recycle();
                                        }
                                        bitmap = null;
                                    }
                                    if (mPadPresenter.getGroupId() != 0) {
                                        PhilPad.Pads.setGroupIcon(mContext, mPadPresenter.getGroupId(), mPadPresenter.getPadSize());
                                    }
                                    Intent bi = new Intent(mContext, SelectItemActivity.class);
                                    int lastGroupId = PhilPad.Settings.getInt(mContext.getContentResolver(), PhilPad.Settings.SETTINGS_KEY_LAST_GROUPID, 5);
                                    bi.putExtra(PadUtils.INTENT_DATA_GROUPID, lastGroupId);
//                            bi.putExtra(PadUtils.INTENT_DATA_LISTID, mPadPresenter.getStartPosition());
                                    bi.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    mContext.startActivity(bi);
                                    setHideAndGroupIdInit();
                                }
                            }).setNegativeButton(R.string.dialog_no, null).create();
                            AlertDialog alert = builder.create();
                            WindowManager.LayoutParams params = alert.getWindow().getAttributes();
                            params.y = mMainPadLayout.getHeight() / 2;
                            params.gravity = mGravity;
                            alert.getWindow().setAttributes(params);
                            alert.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                            alert.show();
                        } else {
                            if (mPadPresenter.getGroupId() == 1) {
                                PadUtils.Toast(mContext, R.string.toast_not_available_dock, mGravity, 0, mMainPadLayout.getHeight() / 2);
                                return;
                            }

                            Bitmap transparent = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
                            Bitmap bitmap = null;
                            bitmap = Bitmap.createScaledBitmap(transparent, (int) (96 * mPadPresenter.getDpScale()), (int) (96 * mPadPresenter.getDpScale()), true);

                            Paint p = new Paint();
                            p.setDither(true);
                            p.setFlags(Paint.ANTI_ALIAS_FLAG);
                            int folderColor = PhilPad.Settings.getInt(mContext.getContentResolver(), PhilPad.Settings.SETTINGS_KEY_PAD_FOLDER_COLOR, Utils.getColor(mContext, R.color.color_folder_background));
                            p.setColor(folderColor);
//                                        p.setAlpha(100);
                            Paint pp = new Paint();
                            pp.setDither(true);
                            pp.setFlags(Paint.ANTI_ALIAS_FLAG);
                            Canvas c = new Canvas(bitmap);
                            c.drawRoundRect(new RectF(0, 0, (int) (96 * mPadPresenter.getDpScale()), (int) (96 * mPadPresenter.getDpScale())), D.GRID_RECT_ROUND_FACTOR, D.GRID_RECT_ROUND_FACTOR, p);

                            ContextWrapper cw = new ContextWrapper(mContext);
                            File directory = cw.getDir(D.DIR_PATH_DEFAULT, Context.MODE_PRIVATE);
                            File mypath = new File(directory, "group_" + mPadPresenter.getGroupId() + "_" + mPadPresenter.getStartPosition() + ".png");
                            FileOutputStream fos = null;
                            try {
                                fos = new FileOutputStream(mypath);
                                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);

                            } catch (IOException e) {
                                e.printStackTrace();
                            } finally {
                                if (fos != null) {
                                    try {
                                        fos.close();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                            PhilPad.Pads.setPadItem(mContext, mPadPresenter.getGroupId(), mPadPresenter.getStartPosition(), PhilPad.Pads.PAD_TYPE_GROUP, null, mContext.getString(R.string.unnamed_folder), "file://" + mypath.getPath(),
                                    0, null);
                            if (bitmap != null) {
                                if (D.RECYCLE) {
                                    bitmap.recycle();
                                }
                                bitmap = null;
                            }

                            if (mPadPresenter.getGroupId() != 0) {
                                PhilPad.Pads.setGroupIcon(mContext, mPadPresenter.getGroupId(), mPadPresenter.getPadSize());
                            }
                            Intent bi = new Intent(mContext, SelectItemActivity.class);
                            int lastGroupId = PhilPad.Settings.getInt(mContext.getContentResolver(), PhilPad.Settings.SETTINGS_KEY_LAST_GROUPID, 5);
                            bi.putExtra(PadUtils.INTENT_DATA_GROUPID, lastGroupId);
//                            bi.putExtra(PadUtils.INTENT_DATA_LISTID, mPadPresenter.getStartPosition());
                            bi.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            mContext.startActivity(bi);
                            setHideAndGroupIdInit();
                        }
                    }
                    break;
                    case D.Icon.ITEM_SELECTED_APPLICATIONS: {
                        PadItemInfo info = PhilPad.Pads.getPadItemInfo(mContext.getContentResolver(), mPadPresenter.getGroupId(), mPadPresenter.getStartPosition());
                        Intent bi = new Intent(mContext, SelectItemActivity.class);
                        bi.putExtra(PadUtils.INTENT_DATA_GROUPID, mPadPresenter.getGroupId());
                        bi.putExtra(PadUtils.INTENT_DATA_LISTID, mPadPresenter.getStartPosition());
                        bi.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        mContext.startActivity(bi);
                        setHideAndGroupIdInit();
                    }
                    break;

                    case D.Icon.ITEM_SELECTED_SHORTCUT: {
                        PadItemInfo info = PhilPad.Pads.getPadItemInfo(mContext.getContentResolver(), mPadPresenter.getGroupId(), mPadPresenter.getStartPosition());
                        if (info != null && (info.getType() != PhilPad.Pads.PAD_TYPE_NULL)) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                            builder.setMessage(R.string.dialog_areyousure_overwrite).setCancelable(true).setPositiveButton(R.string.dialog_yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent bi = new Intent(mContext, AppShortCutListActivity.class);
                                    bi.putExtra(PadUtils.INTENT_DATA_GROUPID, mPadPresenter.getGroupId());
                                    bi.putExtra(PadUtils.INTENT_DATA_LISTID, mPadPresenter.getStartPosition());
                                    bi.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    mContext.startActivity(bi);
                                    setHideAndGroupIdInit();
                                }
                            }).setNegativeButton(R.string.dialog_no, null).create();
                            AlertDialog alert = builder.create();
                            WindowManager.LayoutParams params = alert.getWindow().getAttributes();
                            params.y = mMainPadLayout.getHeight() / 2;
                            params.gravity = mGravity;
                            alert.getWindow().setAttributes(params);
                            alert.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                            alert.show();
                        } else {
                            Intent bi = new Intent(mContext, AppShortCutListActivity.class);
                            bi.putExtra(PadUtils.INTENT_DATA_GROUPID, mPadPresenter.getGroupId());
                            bi.putExtra(PadUtils.INTENT_DATA_LISTID, mPadPresenter.getStartPosition());
                            bi.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            mContext.startActivity(bi);
                            setHideAndGroupIdInit();
                        }
                    }
                    break;
                    case D.Icon.ITEM_SELECTED_WIDGET: {
                        PadItemInfo info = PhilPad.Pads.getPadItemInfo(mContext.getContentResolver(), mPadPresenter.getGroupId(), mPadPresenter.getStartPosition());
                        if (info != null && (info.getType() != PhilPad.Pads.PAD_TYPE_NULL)) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                            builder.setMessage(R.string.dialog_areyousure_overwrite).setCancelable(true).setPositiveButton(R.string.dialog_yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent bi = new Intent(mContext, AppWidgetListActivity.class);
                                    bi.putExtra(PadUtils.INTENT_DATA_GROUPID, mPadPresenter.getGroupId());
                                    bi.putExtra(PadUtils.INTENT_DATA_LISTID, mPadPresenter.getStartPosition());
                                    bi.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    mContext.startActivity(bi);
                                    setHideAndGroupIdInit();
                                }
                            }).setNegativeButton(R.string.dialog_no, null).create();
                            AlertDialog alert = builder.create();
                            WindowManager.LayoutParams params = alert.getWindow().getAttributes();
                            params.y = mMainPadLayout.getHeight() / 2;
                            params.gravity = mGravity;
                            alert.getWindow().setAttributes(params);
                            alert.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                            alert.show();
                        } else {
                            Intent bi = new Intent(mContext, AppWidgetListActivity.class);
                            bi.putExtra(PadUtils.INTENT_DATA_GROUPID, mPadPresenter.getGroupId());
                            bi.putExtra(PadUtils.INTENT_DATA_LISTID, mPadPresenter.getStartPosition());
                            bi.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            mContext.startActivity(bi);
                            setHideAndGroupIdInit();
                        }
                    }
                    break;

                    case D.Icon.ITEM_SELECTED_SETTINGS: {
                    }
                    break;
                    case D.Icon.ITEM_SELECTED_QUICKCALL: {
                    }
                    break;
                    case D.Icon.ITEM_SELECTED_CURRENTAPP: {
                        String currentPackageName = null;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            UsageStatsManager mUsageStatsManager = (UsageStatsManager) mContext.getSystemService("usagestats");
                            long time = System.currentTimeMillis();
                            // We get usage stats for the last 10 seconds
                            List<UsageStats> stats = mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000 * 60 * 60 * 24, time);
                            // Sort the stats by the last time used

                            Collections.sort(stats, new Comparator<UsageStats>() {
                                @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                                @Override
                                public int compare(UsageStats lhs, UsageStats rhs) {
                                    if (rhs.getLastTimeUsed() > lhs.getLastTimeUsed())
                                        return 1;
                                    else {
                                        return -1;
                                    }
                                }
                            });


                            if (stats != null && stats.size() > 0) {
                                currentPackageName = stats.get(0).getPackageName();
                            }
                        } else {
                            ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
                            // get the info from the currently running task
                            List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
                            L.d("topActivity CURRENT Activity ::" + taskInfo.get(0).topActivity.getClassName());
                            ComponentName componentInfo = taskInfo.get(0).topActivity;
                            currentPackageName = componentInfo.getPackageName();
                        }

                        L.d("currentPackageName : " + currentPackageName);
                        final PackageManager pm = mContext.getPackageManager();
                        ApplicationInfo ai;
                        try {
                            ai = pm.getApplicationInfo(currentPackageName, 0);
                        } catch (final PackageManager.NameNotFoundException e) {
                            ai = null;
                        }
                        final String currentApplicationName = (String) (ai != null ? pm.getApplicationLabel(ai) : "(unknown)");
                        L.d("currentApplicationName : " + currentApplicationName);
                        if (currentApplicationName.equals("(unknown)") && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                            PadUtils.Toast(mContext, R.string.toast_not_found_launchinfo);
                            return;
                        }

                        final String packageName = currentPackageName;
                        PadItemInfo info = PhilPad.Pads.getPadItemInfo(mContext.getContentResolver(), mPadPresenter.getGroupId(), mPadPresenter.getStartPosition());
                        if (info != null && (info.getType() != PhilPad.Pads.PAD_TYPE_NULL)) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                            builder.setMessage(String.format(getResources().getString(R.string.dialog_areyousure_overwrite_as_this_app), currentApplicationName)).setCancelable(true)
                                    .setPositiveButton(R.string.dialog_yes, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Cursor cursor = null;
                                            try {
                                                cursor = mContext.getContentResolver().query(PhilPad.Apps.CONTENT_URI, new String[]{
                                                        PhilPad.Apps.COLUMN_NAME_COMPONENT_NAME, PhilPad.Apps.COLUMN_NAME_TITLE, PhilPad.Apps.COLUMN_NAME_IMAGEFILE
                                                }, PhilPad.Apps.COLUMN_NAME_COMPONENT_NAME + " LIKE ?", new String[]{
                                                        "%" + packageName + "%"
                                                }, null);
                                                if (cursor != null && cursor.moveToFirst()) {
                                                    String name = cursor.getString(cursor.getColumnIndex(PhilPad.Apps.COLUMN_NAME_COMPONENT_NAME));
                                                    String imagePath = cursor.getString(cursor.getColumnIndex(PhilPad.Apps.COLUMN_NAME_IMAGEFILE));
                                                    String applicationName = cursor.getString(cursor.getColumnIndex(PhilPad.Apps.COLUMN_NAME_TITLE));
                                                    PhilPad.Pads
                                                            .setPadItem(mContext, mPadPresenter.getGroupId(), mPadPresenter.getStartPosition(), PhilPad.Pads.PAD_TYPE_APPLICATION, name, applicationName, imagePath, 0, null);
                                                    PhilPad.Pads.setGroupIcon(mContext, mPadPresenter.getGroupId(), mPadPresenter.getPadSize());
                                                } else {
                                                    L.d("query not found");
                                                    PadUtils.Toast(mContext, R.string.toast_not_found_launchinfo);
                                                }
                                            } finally {
                                                if (cursor != null) {
                                                    cursor.close();
                                                }
                                            }
                                        }
                                    }).setNegativeButton(R.string.dialog_no, null).create();
                            AlertDialog alert = builder.create();
                            WindowManager.LayoutParams params = alert.getWindow().getAttributes();
                            params.y = mMainPadLayout.getHeight() / 2;
                            params.gravity = mGravity;
                            alert.getWindow().setAttributes(params);
                            alert.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                            alert.show();
                        } else {

                            Cursor cursor = null;
                            try {
                                cursor = mContext.getContentResolver().query(PhilPad.Apps.CONTENT_URI, new String[]{
                                        PhilPad.Apps.COLUMN_NAME_COMPONENT_NAME, PhilPad.Apps.COLUMN_NAME_TITLE, PhilPad.Apps.COLUMN_NAME_IMAGEFILE
                                }, PhilPad.Apps.COLUMN_NAME_COMPONENT_NAME + " LIKE ?", new String[]{
                                        "%" + packageName + "%"
                                }, null);
                                if (cursor != null && cursor.moveToFirst()) {
                                    String name = cursor.getString(cursor.getColumnIndex(PhilPad.Apps.COLUMN_NAME_COMPONENT_NAME));
                                    String imagePath = cursor.getString(cursor.getColumnIndex(PhilPad.Apps.COLUMN_NAME_IMAGEFILE));
                                    String applicationName = cursor.getString(cursor.getColumnIndex(PhilPad.Apps.COLUMN_NAME_TITLE));
                                    PhilPad.Pads.setPadItem(mContext, mPadPresenter.getGroupId(), mPadPresenter.getStartPosition(), PhilPad.Pads.PAD_TYPE_APPLICATION, name, applicationName, imagePath, 0, null);
                                    PhilPad.Pads.setGroupIcon(mContext, mPadPresenter.getGroupId(), mPadPresenter.getPadSize());
                                } else {
                                    PadUtils.Toast(mContext, R.string.toast_not_found_launchinfo);
                                }
                            } finally {
                                if (cursor != null) {
                                    cursor.close();
                                }
                            }
                        }
                    }
                    break;
                }
            }
        });
        // Add Menu (Android menu like style)
        menu.setWidth(300);
        menu.add(D.Icon.ITEM_SELECTED_APPLICATIONS, R.string.add_link_list_applications).setIcon(Utils.getDrawable(mContext, R.drawable.ic_apps_24dp));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (PadUtils.isUsageAccessEnable(mContext)) {
                menu.add(D.Icon.ITEM_SELECTED_CURRENTAPP, R.string.add_link_list_currentapp).setIcon(Utils.getDrawable(mContext, R.drawable.ic_play_arrow_24dp));
            }
        } else {
            menu.add(D.Icon.ITEM_SELECTED_CURRENTAPP, R.string.add_link_list_currentapp).setIcon(Utils.getDrawable(mContext, R.drawable.ic_play_arrow_24dp));
        }
        menu.add(D.Icon.ITEM_SELECTED_FOLDER, R.string.add_link_list_group).setIcon(Utils.getDrawable(mContext, R.drawable.ic_folder_open_24dp));
        menu.add(D.Icon.ITEM_SELECTED_SECRET_FOLDER, R.string.add_link_list_group_secret).setIcon(Utils.getDrawable(mContext, R.drawable.ic_lock_outline_24dp));
        menu.show(mMainPadLayout, mGravity);
    }

    private void showOptionMenuFileOpenOnGesture(final int gesture) {
        PopupMenu menu = new PopupMenu(mContext);
        menu.setHeaderTitle(mContext.getString(R.string.options));
        menu.setOnItemSelectedListener(new PopupMenu.OnItemSelectedListener() {
            @Override
            public void onItemSelected(MenuItem item) {
                Intent bi = new Intent(mContext, AddFileOpenActivity.class);
                bi.putExtra(PadUtils.INTENT_DATA_GROUPID, D.GROUPID_GESTURE);
                bi.putExtra(PadUtils.INTENT_DATA_LISTID, gesture);
                switch (item.getItemId()) {
                    case D.Icon.PadPopupMenuFileOpen.Image: {
                        bi.putExtra(PadUtils.INTENT_DATA_MIMETYPE, "image/*");
                    }
                    break;
                    case D.Icon.PadPopupMenuFileOpen.Audio: {
                        bi.putExtra(PadUtils.INTENT_DATA_MIMETYPE, "audio/*");
                    }
                    break;
                    case D.Icon.PadPopupMenuFileOpen.Video: {
                        bi.putExtra(PadUtils.INTENT_DATA_MIMETYPE, "video/*");
                    }
                    break;
                }
                bi.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(bi);
                setHideAndGroupIdInit();
            }
        });
        // Add Menu (Android menu like style)
        menu.setWidth(300);
        menu.add(D.Icon.PadPopupMenuFileOpen.Image, R.string.popupmenu_image).setIcon(Utils.getDrawable(mContext, R.drawable.ic_photo_24dp));
        menu.add(D.Icon.PadPopupMenuFileOpen.Audio, R.string.popupmenu_audio).setIcon(Utils.getDrawable(mContext, R.drawable.ic_music_video_black_24px));
        menu.add(D.Icon.PadPopupMenuFileOpen.Video, R.string.popupmenu_video).setIcon(Utils.getDrawable(mContext, R.drawable.ic_movie_24dp));

        menu.show(mMainPadLayout, mGravity);
    }

    private void showOptionMenuToolsOnGesture(final int gesture) {
        PopupMenu menu = new PopupMenu(mContext);
        menu.setHeaderTitle(mContext.getString(R.string.options));
        menu.setOnItemSelectedListener(new PopupMenu.OnItemSelectedListener() {
            @Override
            public void onItemSelected(MenuItem item) {

                Bitmap bitmap = null;
                int id = item.getItemId();

                switch (id) {
                    case D.Icon.PadPopupMenuTools.LastApp: {
                        bitmap = PadUtils.getBitmapFromDrawable(mContext, Utils.getDrawable(mContext, R.drawable.ic_undo_24dp), 96 * (int) mPadPresenter.getDpScale(), 96 * (int) mPadPresenter.getDpScale());
                        updateCurrentApps(mContext);
                    }
                    break;
                    case D.Icon.PadPopupMenuTools.RecentApplicationInPad: {
                        bitmap = PadUtils.getBitmapFromDrawable(mContext, Utils.getDrawable(mContext, R.drawable.ic_grid_on_24dp), 96 * (int) mPadPresenter.getDpScale(), 96 * (int) mPadPresenter.getDpScale());
                        updateCurrentApps(mContext);
                    }
                    break;

                    case D.Icon.PadPopupMenuTools.Context: {
                        bitmap = PadUtils.getBitmapFromDrawable(mContext, Utils.getDrawable(mContext, R.drawable.ic_info_24dp), 96 * (int) mPadPresenter.getDpScale(), 96 * (int) mPadPresenter.getDpScale());
                    }
                    break;
                    case D.Icon.PadPopupMenuTools.Home: {
                        bitmap = PadUtils.getBitmapFromDrawable(mContext, Utils.getDrawable(mContext, R.drawable.ic_home_24dp), 96 * (int) mPadPresenter.getDpScale(), 96 * (int) mPadPresenter.getDpScale());
                    }
                    break;
                    case D.Icon.PadPopupMenuTools.Indicator: {
                        bitmap = PadUtils.getBitmapFromDrawable(mContext, Utils.getDrawable(mContext, R.drawable.ic_vertical_align_bottom_24dp), 96 * (int) mPadPresenter.getDpScale(), 96 * (int) mPadPresenter.getDpScale());
                    }
                    break;
                    case D.Icon.PadPopupMenuTools.RecentApplication: {
                        bitmap = PadUtils.getBitmapFromDrawable(mContext, Utils.getDrawable(mContext, R.drawable.ic_filter_none_24dp), 96 * (int) mPadPresenter.getDpScale(), 96 * (int) mPadPresenter.getDpScale());
                    }
                    break;
                    case D.Icon.PadPopupMenuTools.Close: {
                        bitmap = PadUtils.getBitmapFromDrawable(mContext, Utils.getDrawable(mContext, R.drawable.ic_close_24dp), 96 * (int) mPadPresenter.getDpScale(), 96 * (int) mPadPresenter.getDpScale());
                    }
                    break;

                    case D.Icon.PadPopupMenuTools.BackButton: {
                        bitmap = PadUtils.getBitmapFromDrawable(mContext, Utils.getDrawable(mContext, R.drawable.ic_keyboard_backspace_24dp), 96 * (int) mPadPresenter.getDpScale(), 96 * (int) mPadPresenter.getDpScale());
                    }
                    break;

                    case D.Icon.PadPopupMenuTools.PadSettings: {
                        bitmap = PadUtils.getBitmapFromDrawable(mContext, Utils.getDrawable(mContext, R.drawable.ic_settings_24dp), 96 * (int) mPadPresenter.getDpScale(), 96 * (int) mPadPresenter.getDpScale());
                    }
                    break;

                    case D.Icon.PadPopupMenuTools.HotspotDisable: {
                        bitmap = PadUtils.getBitmapFromDrawable(mContext, Utils.getDrawable(mContext, R.drawable.ic_block_24dp), 96 * (int) mPadPresenter.getDpScale(), 96 * (int) mPadPresenter.getDpScale());
                    }
                    break;
                }
                String mypath = PadUtils.makeImageIcon(mContext, bitmap, mPadPresenter.getDpScale(), String.valueOf(id), D.GROUPID_GESTURE, gesture);
                PadUtils.setFunctionItem(mContext, id, D.GROUPID_GESTURE, gesture, mypath, mPadPresenter.getPadSize());
                if (bitmap != null) {
                    L.d("recycle");
                    if (D.RECYCLE) {
                        bitmap.recycle();
                    }
                    bitmap = null;

                }

            }
        });
        // Add Menu (Android menu like style)
        menu.setWidth(300);
        menu.add(D.Icon.PadPopupMenuTools.LastApp, R.string.settings_function_type_lastapp).setIcon(Utils.getDrawable(mContext, R.drawable.ic_undo_24dp));
        menu.add(D.Icon.PadPopupMenuTools.RecentApplicationInPad, R.string.settings_function_type_recentapplication_onpad).setIcon(Utils.getDrawable(mContext, R.drawable.ic_grid_on_24dp));

        menu.add(D.Icon.PadPopupMenuTools.Context, R.string.settings_function_type_context).setIcon(Utils.getDrawable(mContext, R.drawable.ic_info_24dp));
        menu.add(D.Icon.PadPopupMenuTools.Home, R.string.settings_function_type_home).setIcon(Utils.getDrawable(mContext, R.drawable.ic_home_24dp));
        menu.add(D.Icon.PadPopupMenuTools.Indicator, R.string.settings_function_type_indicator).setIcon(Utils.getDrawable(mContext, R.drawable.ic_vertical_align_bottom_24dp));
        menu.add(D.Icon.PadPopupMenuTools.RecentApplication, R.string.settings_function_type_recentapplication).setIcon(Utils.getDrawable(mContext, R.drawable.ic_filter_none_24dp));
        menu.add(D.Icon.PadPopupMenuTools.Close, R.string.settings_function_type_close).setIcon(Utils.getDrawable(mContext, R.drawable.ic_close_24dp));
        menu.add(D.Icon.PadPopupMenuTools.BackButton, R.string.settings_function_type_backbutton).setIcon(Utils.getDrawable(mContext, R.drawable.ic_keyboard_backspace_24dp));
        menu.add(D.Icon.PadPopupMenuTools.PadSettings, R.string.settings_function_type_padsettings).setIcon(Utils.getDrawable(mContext, R.drawable.ic_settings_24dp));
        menu.add(D.Icon.PadPopupMenuTools.HotspotDisable, R.string.settings_function_type_trigger_area_detect_disable).setIcon(Utils.getDrawable(mContext, R.drawable.ic_block_24dp));

        menu.show(mMainPadLayout, mGravity);
    }

    public void showOptionMenuGesture(final int gesture) {
        PopupMenu menu = new PopupMenu(mContext);
        int stringID;
        switch (gesture) {
            case D.Gesture.Command.GESTURE_LEFT:
                stringID = R.string.pref_gesture_left;
                break;
            case D.Gesture.Command.GESTURE_RIGHT:
                stringID = R.string.pref_gesture_right;
                break;
            case D.Gesture.Command.GESTURE_UP:
                stringID = R.string.pref_gesture_up;
                break;
            case D.Gesture.Command.GESTURE_DOWN:
                stringID = R.string.pref_gesture_down;
                break;
            default:
                stringID = R.string.options;
                break;
        }
        menu.setHeaderTitle(mContext.getString(stringID));
        menu.setOnItemSelectedListener(new PopupMenu.OnItemSelectedListener() {
            @Override
            public void onItemSelected(MenuItem item) {
                switch (item.getItemId()) {
                    case D.Icon.ITEM_SELECTED_FOLDER: {
                        Bitmap transparent = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
                        Bitmap bitmap = null;
                        bitmap = Bitmap.createScaledBitmap(transparent, (int) (96 * mPadPresenter.getDpScale()), (int) (96 * mPadPresenter.getDpScale()), true);

                        Paint p = new Paint();
                        p.setDither(true);
                        p.setFlags(Paint.ANTI_ALIAS_FLAG);
                        p.setColor(Color.DKGRAY);
                        p.setAlpha(100);
                        Paint pp = new Paint();
                        pp.setDither(true);
                        pp.setFlags(Paint.ANTI_ALIAS_FLAG);
                        Canvas c = new Canvas(bitmap);
                        c.drawRoundRect(new RectF(0, 0, (int) (96 * mPadPresenter.getDpScale()), (int) (96 * mPadPresenter.getDpScale())), D.GRID_RECT_ROUND_FACTOR, D.GRID_RECT_ROUND_FACTOR, p);

                        ContextWrapper cw = new ContextWrapper(mContext);
                        File directory = cw.getDir(D.DIR_PATH_DEFAULT, Context.MODE_PRIVATE);
                        File mypath = new File(directory, "group_" + D.GROUPID_GESTURE + "_" + gesture + ".png");
                        FileOutputStream fos = null;
                        try {
                            fos = new FileOutputStream(mypath);
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);

                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            if (fos != null) {
                                try {
                                    fos.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        if (bitmap != null) {
                            L.d("recycle");
                            bitmap.recycle();
                            bitmap = null;
                        }

                        PhilPad.Pads.setPadItem(mContext, D.GROUPID_GESTURE, gesture, PhilPad.Pads.PAD_TYPE_GROUP, null, mContext.getString(R.string.unnamed_folder),
                                "file://" + mypath.getPath(), 0, null);


                    }
                    break;

                    case D.Icon.ITEM_SELECTED_APPLICATION: {
                        Intent bi = new Intent(mContext, AppListActivity.class);
                        bi.putExtra(PadUtils.INTENT_DATA_GROUPID, D.GROUPID_GESTURE);
                        bi.putExtra(PadUtils.INTENT_DATA_LISTID, gesture);
                        bi.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        mContext.startActivity(bi);
                        setHideAndGroupIdInit();
                    }
                    break;

                    case D.Icon.ITEM_SELECTED_SHORTCUT: {
                        Intent bi = new Intent(mContext, AppShortCutListActivity.class);
                        bi.putExtra(PadUtils.INTENT_DATA_GROUPID, D.GROUPID_GESTURE);
                        bi.putExtra(PadUtils.INTENT_DATA_LISTID, gesture);
                        bi.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        mContext.startActivity(bi);
                        setHideAndGroupIdInit();
                    }
                    break;
                    case D.Icon.ITEM_SELECTED_WIDGET: {
                        Intent bi = new Intent(mContext, AppWidgetListActivity.class);
                        bi.putExtra(PadUtils.INTENT_DATA_GROUPID, D.GROUPID_GESTURE);
                        bi.putExtra(PadUtils.INTENT_DATA_LISTID, gesture);
                        bi.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        mContext.startActivity(bi);
                        setHideAndGroupIdInit();
                    }
                    break;
                    case D.Icon.ITEM_SELECTED_TOOLS: {
                        showOptionMenuToolsOnGesture(gesture);
                    }
                    break;

                    case D.Icon.ITEM_SELECTED_SETTINGS: {
                    }
                    break;
                    case D.Icon.ITEM_SELECTED_QUICKCALL: {
                    }
                    break;

                    case D.Icon.ITEM_SELECTED_FILEOPEN: {
                        showOptionMenuFileOpenOnGesture(gesture);
                    }
                    break;
                    case D.Icon.ITEM_SELECTED_CURRENTAPP: {


                        //TODO
                        ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
                        // get the info from the currently running task
                        List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
                        L.d("topActivity CURRENT Activity ::" + taskInfo.get(0).topActivity.getClassName());
                        ComponentName componentInfo = taskInfo.get(0).topActivity;
                        final String currentPackageName = componentInfo.getPackageName();
                        L.d("topActivity getPackageName : " + componentInfo.getPackageName());
                        final PackageManager pm = mContext.getPackageManager();
                        Cursor cursor = null;
                        try {
                            cursor = mContext.getContentResolver().query(PhilPad.Apps.CONTENT_URI, new String[]{
                                    PhilPad.Apps.COLUMN_NAME_COMPONENT_NAME, PhilPad.Apps.COLUMN_NAME_TITLE, PhilPad.Apps.COLUMN_NAME_IMAGEFILE
                            }, PhilPad.Apps.COLUMN_NAME_COMPONENT_NAME + "=?", new String[]{
                                    currentPackageName
                            }, null);
                            if (cursor != null && cursor.moveToFirst()) {
                                String packageName = cursor.getString(cursor.getColumnIndex(PhilPad.Apps.COLUMN_NAME_COMPONENT_NAME));
                                String imagePath = cursor.getString(cursor.getColumnIndex(PhilPad.Apps.COLUMN_NAME_IMAGEFILE));
                                String applicationName = cursor.getString(cursor.getColumnIndex(PhilPad.Apps.COLUMN_NAME_TITLE));
                                PhilPad.Pads.setPadItem(mContext, D.GROUPID_GESTURE, gesture, PhilPad.Pads.PAD_TYPE_APPLICATION, packageName, applicationName, imagePath, 0, null);
                                PhilPad.Pads.setGroupIcon(mContext, D.GROUPID_GESTURE, mPadPresenter.getPadSize());
                            } else {
                                PadUtils.Toast(mContext, R.string.toast_not_found_launchinfo);
                            }
                        } finally {
                            if (cursor != null) {
                                cursor.close();
                            }
                        }

                    }
                    break;
                }
            }
        });


        // Add Menu (Android menu like style)
        menu.setWidth(300);
        menu.add(D.Icon.ITEM_SELECTED_APPLICATION, R.string.add_link_list_application).setIcon(Utils.getDrawable(mContext, R.drawable.ic_stop_24dp));
        menu.add(D.Icon.ITEM_SELECTED_CURRENTAPP, R.string.add_link_list_currentapp).setIcon(Utils.getDrawable(mContext, R.drawable.ic_play_circle_fill_24dp));
        menu.add(D.Icon.ITEM_SELECTED_FOLDER, R.string.add_link_list_group).setIcon(Utils.getDrawable(mContext, R.drawable.ic_folder_open_24dp));
        menu.add(D.Icon.ITEM_SELECTED_SHORTCUT, R.string.add_link_list_shortcut).setIcon(Utils.getDrawable(mContext, R.drawable.redo2));
        menu.add(D.Icon.ITEM_SELECTED_WIDGET, R.string.add_link_list_widget).setIcon(Utils.getDrawable(mContext, R.drawable.ic_now_widgets_24dp));
        menu.add(D.Icon.ITEM_SELECTED_TOOLS, R.string.add_link_list_tools).setIcon(Utils.getDrawable(mContext, R.drawable.ic_settings_applications_24dp));
        menu.add(D.Icon.ITEM_SELECTED_FILEOPEN, R.string.add_link_list_fileopen).setIcon(Utils.getDrawable(mContext, R.drawable.ic_attachment_24dp));
        menu.show(mMainPadLayout, mGravity);
    }

    @Override
    public boolean getShowPopupEnable() {
        return mShowPopup;
    }

    @Override
    public void setShowFromAssist(boolean b) {
        mShowFromAssist = b;
    }

    @Override
    public ImageView getGridViewBackground() {
        return mGridViewBackground;
    }


}