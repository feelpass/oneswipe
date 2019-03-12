
package com.philleeran.flicktoucher.view.pad;

import android.content.Context;
import android.view.View;
import android.widget.RelativeLayout;

import com.philleeran.flicktoucher.R;
import com.philleeran.flicktoucher.db.LocalSettings;

public class HotSpotView extends RelativeLayout {

    Context mContext;

    public final View mView;

    public final float DPSCALE = getResources().getDisplayMetrics().density;

    public final int DISPLAY_WIDTH = getResources().getDisplayMetrics().widthPixels;

    public final int DISPLAY_HEIGHT = getResources().getDisplayMetrics().heightPixels;
    private RelativeLayout.LayoutParams mParams;
    public HotSpotView(Context context, int width, int height) {
        super(context);
        mContext = context;

        mView = new View(mContext);
        mView.setBackgroundResource(R.drawable.no_bullet_normal);
//        mView.setBackgroundColor(0x00000000);
        mView.setMinimumWidth(width);
        mView.setMinimumHeight(height);
//        setBackgroundColor(Color.MAGENTA);
        setAlpha(LocalSettings.mTrigerDefaultAlpha);

        setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams((int) (width * DPSCALE), (int) (height * DPSCALE));
        mParams = new RelativeLayout.LayoutParams((int) (width * DPSCALE), (int) (height * DPSCALE));
        mView.setLayoutParams(params);
        addView(mView);
    }

    public void updateLayoutParams(int width, int height, int marginRight, int marginBottom) {
        mParams.width = (int) (width * DPSCALE);
        mParams.height = (int) (height * DPSCALE);
        mParams.setMargins(0, 0, (int) (marginRight * DPSCALE), (int) (marginBottom * DPSCALE));
        setLayoutParams(mParams);
    }

    public void setBackground(int resouce)
    {
        mView.setBackgroundResource(resouce);
    }


    public void setTouchListener(OnTouchListener listener) {
        mView.setOnTouchListener(listener);
    }
}
