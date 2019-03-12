package com.philleeran.flicktoucher.view.pad.command;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.WindowManager;

import com.philleeran.flicktoucher.PadUtils;
import com.philleeran.flicktoucher.R;
import com.philleeran.flicktoucher.utils.D;
import com.philleeran.flicktoucher.view.pad.PadContract;


public class RecentAppInPad implements Command {
    private final PadContract.View mView;
    private final Context mContext;
    private final PadContract.Presenter mPadPresenter;
    private int mStartPosition;
    private int mGroupId;

    public RecentAppInPad(Context context, PadContract.View view, PadContract.Presenter presenter, int groupId, int  startposition) {
        mView = view;
        mPadPresenter = presenter;
        mContext = context;
        mGroupId = groupId;
        mStartPosition = startposition;
    }

    @Override
    public void execute() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            if (!PadUtils.isUsageAccessEnable(mContext)) {
                try {
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setTitle(R.string.dialog_howtouse_title).setMessage(R.string.dialog_check_usage_access_message).setPositiveButton(R.string.dialog_yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            mContext.startActivity(intent);
                        }
                    }).create();
                    AlertDialog alert = builder.create();
                    alert.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                    alert.show();

                } catch (Exception e) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setTitle(R.string.dialog_howtouse_title).setMessage(R.string.dialog_check_usage_access_message).setPositiveButton(R.string.dialog_yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(Settings.ACTION_SETTINGS);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            mContext.startActivity(intent);

                        }
                    }).create();
                    AlertDialog alert = builder.create();
                    alert.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                    alert.show();
                    //Settings > Security > Usage access > Check SidePad
                }
                mView.setHideAndGroupIdInit();
            } else {

                mPadPresenter.pushHistoryStack(mGroupId);


                int groupId = D.GROUPID_RECENT;
                mView.startAnimation(true, mStartPosition, groupId);
            }
        } else {
            mPadPresenter.pushHistoryStack(mGroupId);


            int groupId = D.GROUPID_RECENT;
            mView.startAnimation(true, mStartPosition, groupId);
        }
    }

}
