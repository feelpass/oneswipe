
package com.philleeran.flicktoucher.view.pad;

import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.RelativeLayout;

import com.philleeran.flicktoucher.R;
import com.philleeran.flicktoucher.db.PhilPad;
import com.philleeran.flicktoucher.utils.D;

public class WidgetViewLayout extends RelativeLayout {
    private Context mContext;

    private AppWidgetManager mAppWidgetManager;

    private AppWidgetHost mAppWidgetHost;

    private LayoutParams mLayoutParam;

    public WidgetViewLayout(Context context) {
        super(context);
        mContext = context;
        setBackgroundColor(PhilPad.Settings.getInt(context.getContentResolver(), PhilPad.Settings.SETTINGS_KEY_BACKGROUND_COLOR, getResources().getColor(R.color.color_background)));

        setVisibility(View.INVISIBLE);
        setGravity(Gravity.CENTER);
        mAppWidgetManager = AppWidgetManager.getInstance(mContext);
        mAppWidgetHost = new AppWidgetHost(mContext, D.WIDGET_HOST_ID);
        mAppWidgetHost.startListening();
        mLayoutParam = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        mLayoutParam.addRule(RelativeLayout.CENTER_IN_PARENT);
    }

    public void setWidgetByAppWidgetId(int appWidgetId) {
        removeAllViews();
        AppWidgetProviderInfo info = mAppWidgetManager.getAppWidgetInfo(appWidgetId);

        addView(mAppWidgetHost.createView(mContext, appWidgetId, info), mLayoutParam);

        setVisibility(View.VISIBLE);
    }

}
