
package com.philleeran.flicktoucher.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.MenuItem;

import com.philleeran.flicktoucher.IPhilPad;
import com.philleeran.flicktoucher.PadUtils;
import com.philleeran.flicktoucher.R;
import com.philleeran.flicktoucher.db.PhilPad;
import com.philleeran.flicktoucher.utils.D;
import com.philleeran.flicktoucher.utils.L;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

@SuppressWarnings("deprecation")
public class AppIconResetActivity extends Activity implements ServiceConnection {

    IPhilPad mPad;

    private ProgressDialog progressDialog;

    private PackageManager mPackageManager;

    private List<ResolveInfo> mApps;

    private Context mContext;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;

        mPackageManager = getPackageManager();

        new AppListUpdateTask().execute();
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.loading));
        progressDialog.setCancelable(false);
        progressDialog.show();

        Intent serviceIntent = new Intent(PadUtils.ACTION_PHILPAD_SERVICE);
        L.d("startPhilPadService");
        serviceIntent.setPackage(PadUtils.ACTION_PHILPAD_PACKAGE);
        serviceIntent.setAction(PadUtils.ACTION_PHILPAD_SERVICE);
        bindService(serviceIntent, this, BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        try {
            if (mPad != null) {
                mPad.hotspotEnable(true);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        super.onResume();
    }

    @Override
    protected void onDestroy() {

        unbindService(this);
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        mPad = IPhilPad.Stub.asInterface(service);
        try {
            mPad.hotspotEnable(true);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        mPad = null;
    }

    public class AppListUpdateTask extends AsyncTask<Void, Void, Cursor> {

        protected Cursor doInBackground(Void... args) {
            Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

            mContext.getContentResolver().delete(PhilPad.Apps.CONTENT_URI, null, null);

            mApps = mPackageManager.queryIntentActivities(mainIntent, 0);
            Cursor c = null;

            for (ResolveInfo info : mApps) {
                String packageName = info.activityInfo.packageName;
                String activityName = info.activityInfo.name;
                Resources res = null;
                try {
                    res = mPackageManager.getResourcesForApplication(packageName);
                } catch (PackageManager.NameNotFoundException e) {
                    L.e(e);
                }

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


                cv.put(PhilPad.Apps.COLUMN_NAME_IMAGEFILE, "file://" + mypath.getPath());
                Uri uri = getContentResolver().insert(PhilPad.Apps.CONTENT_URI, cv);
                if (bitmap != null) {
                    bitmap.recycle();
                    bitmap = null;
                }
            }


            return c;
        }

        protected void onPostExecute(Cursor result) {
            if (progressDialog != null) {
                try {
                    progressDialog.dismiss();

                    PhilPad.Pads.getPadItemInfosCursorInGroup(mContext, D.GROUPID_GROUND, 4 * 4);
                    mPad.hotspotEnable(true);
                    mPad.notifyReDrawGridViewBackground();
                    finish();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                progressDialog = null;
            }
        }
    }

}