package com.philleeran.flicktoucher.view.pad;

import android.content.Context;
import android.view.MotionEvent;
import android.widget.ImageView;

import com.philleeran.flicktoucher.view.pad.adapter.PadGridViewAdapter;
import com.philleeran.flicktoucher.view.pad.adapter.PadGridViewAdapterContract;

public interface PadContract {
    interface View {
        void setHideAndGroupIdInit();

        Presenter getPresenter();

        android.view.View getView();

        void updateLayoutParams(int i);

        void setVisibility(int visible);

        void setGravity(int mGravity);

        void startAnimation(boolean b, int mStartPosition, int groupId);



        void setGridViewBackground(boolean b);

        void invalidateViews();

        void setShowPopupEnable(boolean b);

        int getGravity();

        void setGridViewBackgroundVisibility(int visible);

        void showToast(String message);

        void vibrate(int mVibrationTime);

        void showOptionMenu(boolean b);

        void showOptionMenuGesture(int guesture);

        boolean getShowPopupEnable();

        void setShowFromAssist(boolean b);

        ImageView getGridViewBackground();
    }


    interface Presenter extends android.view.View.OnTouchListener {
        void updateCurrentApps(Context mContext);

        void registerQuickSettingReceiver();

        void unregisterQuickSettingReceiver();

        void updateConfiguration(float dpscale, int display_width, int display_height);

        void setShowPopupEnable(boolean b);

        void setEnable(boolean b);
        void attachView(View view);
        void detachView();

        void setGravity(int mGravity);

        void setGridViewBackground(boolean b);

        void setPremium(boolean enable);

        void onHoverTouch(MotionEvent event, int mWidth, int mHeight, int mMarginX, int mMarginY);

        void setGridView(DragDropGridView mGridView, int groupidGround, PadGridViewAdapter mPadGridPagerAdapter);

        void showToast(String message);

        void setGroupId(int groupidGround);

        void setDragMode(boolean b);

        void removePadItem();

        void setGroupIcon();

        int getPadSize();

        float getDpScale();

        boolean getDragMode();

        Integer getGroupId();

        int getStartPosition();

        String getTopPackageName();

        void setPadGridViewAdapterModel(PadGridViewAdapterContract.Model mPadGridPagerAdapter);

        void setPadGridViewAdapterView(PadGridViewAdapterContract.View mPadGridPagerAdapter);

        void pushHistoryStack(int mGroupId);
        int popHistoryStack();

        boolean isHistoryStackEmpty();

        void clearHistoryStack();
    }
}
