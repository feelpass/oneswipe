
package com.philleeran.flicktoucher.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.philleeran.flicktoucher.IPhilPad;
import com.philleeran.flicktoucher.PadUtils;
import com.philleeran.flicktoucher.R;
import com.philleeran.flicktoucher.db.PhilPad;
import com.philleeran.flicktoucher.utils.D;
import com.philleeran.flicktoucher.utils.L;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class AppListActivity extends Activity implements OnItemClickListener {

    IPhilPad mPadBind;

    private Context mContext = null;

    private CountDownLatch mSignalBind;

    private PackageManager mPackageManager;

    private int mGroupId;

    private int mListId;

    private ProgressDialog progressDialog;

    private int mMode;

    private int mGestureType;

    private List<ResolveInfo> mApps;

    private PadViewAdapter mAdapter;
    private int mPadSize;


    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        mPadSize = Integer.parseInt(PhilPad.Settings.getString(mContext.getContentResolver(), PhilPad.Settings.SETTINGS_KEY_PAD_SIZE, "4"));
        mPackageManager = getPackageManager();
        setContentView(R.layout.activity_listview);
        GridView listView = (GridView) findViewById(R.id.list_view);

        Cursor cursor = mContext.getContentResolver().query(PhilPad.Apps.CONTENT_URI, new String[] {
                PhilPad.Apps._ID, PhilPad.Apps.COLUMN_NAME_IMAGEFILE, PhilPad.Apps.COLUMN_NAME_TITLE,
        }, null, null, PhilPad.Apps.DEFAULT_SORT_ORDER_TITLE_ASC);

        String[] fromColumns = {
            PhilPad.Apps.COLUMN_NAME_TITLE,
        };
        int[] toViews = {
            R.id.item_text
        };
        mAdapter = new PadViewAdapter(mContext, R.layout.list_icon_item, cursor, fromColumns, toViews);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(this);

        progressDialog = new ProgressDialog(this);

        Intent intent = getIntent();

        mMode = intent.getIntExtra(PadUtils.INTENT_DATA_APPLISTREQUEST_TYPE, PadUtils.APPLISTREQUEST_TYPE_SELECT_APPLICATION);
        mGestureType = intent.getIntExtra(PadUtils.INTENT_DATA_GESTURE_TYPE, 0);
        mGroupId = intent.getIntExtra(PadUtils.INTENT_DATA_GROUPID, 0);
        mListId = intent.getIntExtra(PadUtils.INTENT_DATA_LISTID, 0);

        mSignalBind = new CountDownLatch(1);
        Intent serviceIntent = new Intent(PadUtils.ACTION_PHILPAD_SERVICE);
        L.d("startPhilPadService");
        serviceIntent.setPackage(PadUtils.ACTION_PHILPAD_PACKAGE);
        serviceIntent.setAction(PadUtils.ACTION_PHILPAD_SERVICE);
        bindService(serviceIntent, conn, BIND_AUTO_CREATE);

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing() == false)
            finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        unbindService(conn);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.applist_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_reload_applist:
                PhilPad.Settings.putBoolean(getContentResolver(), PhilPad.Settings.SETTINGS_KEY_COMPLETE_TOTURIAL, false);
                new ListAppTask().execute();
                if (progressDialog == null) {
                    progressDialog = new ProgressDialog(this);
                }
                progressDialog.setMessage(getString(R.string.loading));
                progressDialog.setCancelable(false);
                progressDialog.show();
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ContentResolver resolver = getContentResolver();
        Cursor cursor = null;
        if (mMode == PadUtils.APPLISTREQUEST_TYPE_SELECT_APPLICATION) {
            try {
                Uri uri = ContentUris.withAppendedId(PhilPad.Apps.CONTENT_URI, id);
                cursor = resolver.query(uri, new String[] {
                        PhilPad.Apps.COLUMN_NAME_COMPONENT_NAME, PhilPad.Apps.COLUMN_NAME_TITLE, PhilPad.Apps.COLUMN_NAME_IMAGEFILE
                }, null, null, null);

                if (cursor != null && cursor.moveToFirst()) {
                    String packageName = cursor.getString(cursor.getColumnIndex(PhilPad.Apps.COLUMN_NAME_COMPONENT_NAME));
                    String imagePath = cursor.getString(cursor.getColumnIndex(PhilPad.Apps.COLUMN_NAME_IMAGEFILE));
                    String applicationName = cursor.getString(cursor.getColumnIndex(PhilPad.Apps.COLUMN_NAME_TITLE));
                    PhilPad.Pads.setPadItem(mContext, mGroupId, mListId, PhilPad.Pads.PAD_TYPE_APPLICATION, packageName, applicationName, imagePath, 0, null);
                    PhilPad.Pads.setGroupIcon(mContext, mGroupId, mPadSize);
                    try {
                        mPadBind.notifyReDrawGridView();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    finish();
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        } else if (mMode == PadUtils.APPLISTREQUEST_TYPE_SELECT_GESTURE_LAUNCH) {
            try {
                Uri uri = ContentUris.withAppendedId(PhilPad.Apps.CONTENT_URI, id);
                cursor = resolver.query(uri, new String[] {
                        PhilPad.Apps.COLUMN_NAME_COMPONENT_NAME, PhilPad.Apps.COLUMN_NAME_TITLE, PhilPad.Apps.COLUMN_NAME_IMAGEFILE
                }, null, null, null);

                if (cursor != null && cursor.moveToFirst()) {

                    String gestureSettingString = PhilPad.Settings.SETTINGS_KEY_GESTURE_TYPE_UP;
                    switch (mGestureType) {
                        case D.Gesture.Command.GESTURE_UP:
                            gestureSettingString = PhilPad.Settings.SETTINGS_KEY_GESTURE_TYPE_UP;
                            break;
                        case D.Gesture.Command.GESTURE_DOWN:
                            gestureSettingString = PhilPad.Settings.SETTINGS_KEY_GESTURE_TYPE_DOWN;
                            break;
                        case D.Gesture.Command.GESTURE_LEFT:
                            gestureSettingString = PhilPad.Settings.SETTINGS_KEY_GESTURE_TYPE_LEFT;
                            break;
                        case D.Gesture.Command.GESTURE_RIGHT:
                            gestureSettingString = PhilPad.Settings.SETTINGS_KEY_GESTURE_TYPE_RIGHT;
                            break;
                        case D.Gesture.Command.GESTURE_UP_LEFT:
                            gestureSettingString = PhilPad.Settings.SETTINGS_KEY_GESTURE_TYPE_UP_LEFT;
                            break;
                        case D.Gesture.Command.GESTURE_UP_RIGHT:
                            gestureSettingString = PhilPad.Settings.SETTINGS_KEY_GESTURE_TYPE_UP_RIGHT;
                            break;
                        case D.Gesture.Command.GESTURE_DOWN_LEFT:
                            gestureSettingString = PhilPad.Settings.SETTINGS_KEY_GESTURE_TYPE_DOWN_LEFT;
                            break;
                        case D.Gesture.Command.GESTURE_DOWN_RIGHT:
                            gestureSettingString = PhilPad.Settings.SETTINGS_KEY_GESTURE_TYPE_DOWN_RIGHT;
                            break;
                        case D.Gesture.Command.GESTURE_LEFT_UP:
                            gestureSettingString = PhilPad.Settings.SETTINGS_KEY_GESTURE_TYPE_LEFT_UP;
                            break;
                        case D.Gesture.Command.GESTURE_LEFT_DOWN:
                            gestureSettingString = PhilPad.Settings.SETTINGS_KEY_GESTURE_TYPE_LEFT_DOWN;
                            break;
                        case D.Gesture.Command.GESTURE_RIGHT_UP:
                            gestureSettingString = PhilPad.Settings.SETTINGS_KEY_GESTURE_TYPE_RIGHT_UP;
                            break;
                        case D.Gesture.Command.GESTURE_RIGHT_DOWN:
                            gestureSettingString = PhilPad.Settings.SETTINGS_KEY_GESTURE_TYPE_RIGHT_DOWN;
                            break;
                    }
                    String packageName = cursor.getString(cursor.getColumnIndex(PhilPad.Apps.COLUMN_NAME_COMPONENT_NAME));
                    String imagePath = cursor.getString(cursor.getColumnIndex(PhilPad.Apps.COLUMN_NAME_IMAGEFILE));
                    PhilPad.Settings.putInt(mContext.getContentResolver(), gestureSettingString, D.Icon.PadPopupMenuGesture.Launch);
                    PhilPad.Settings.putString(mContext.getContentResolver(), "gesture-package-" + mGestureType, packageName);
                    PhilPad.Settings.putString(mContext.getContentResolver(), "gesture-image-" + mGestureType, imagePath);
                    try {
                        mPadBind.notifyReDrawGridView();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }

                    finish();
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }

        }

    }

    ServiceConnection conn = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mPadBind = IPhilPad.Stub.asInterface(service);
            mSignalBind.countDown();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mPadBind = null;
        }
    };

    class PadCursorAdapter extends SimpleCursorAdapter {
        private ViewHolder viewHolder;

        @SuppressWarnings("deprecation")
        public PadCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to) {
            super(context, layout, c, from, to);
            mContext = context;
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            final LayoutInflater inflater = LayoutInflater.from(context);
            View v = inflater.inflate(R.layout.padslist_item, null);

            ImageView imageView = (ImageView) v.findViewById(R.id.item_image);
            TextView textView = (TextView) v.findViewById(R.id.item_text);
            viewHolder = new ViewHolder();
            viewHolder.imageView = imageView;
            viewHolder.textView = textView;
            v.setTag(viewHolder);
            return v;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            viewHolder = (ViewHolder) view.getTag();
            String applicationName = cursor.getString(cursor.getColumnIndex(PhilPad.Apps.COLUMN_NAME_TITLE));
            String imageFile = cursor.getString(cursor.getColumnIndex(PhilPad.Apps.COLUMN_NAME_IMAGEFILE));
            Picasso.with(AppListActivity.this).load(imageFile).into(viewHolder.imageView);

            viewHolder.textView.setText(applicationName);
            view.setTag(viewHolder);
        }

        class ViewHolder {
            ImageView imageView;

            TextView textView;
        }
    }

    public class ListAppTask extends AsyncTask<Void, Void, Cursor> {

        protected Cursor doInBackground(Void... args) {
            Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

            mContext.getContentResolver().delete(PhilPad.Apps.CONTENT_URI, null, null);

            mApps = mPackageManager.queryIntentActivities(mainIntent, 0);
            Cursor c = null;

            for (ResolveInfo info : mApps) {
                String packageName = info.activityInfo.packageName;
                String activityName = info.activityInfo.name;

                String componentName = packageName + "/" + activityName;
                c = getContentResolver().query(PhilPad.Apps.CONTENT_URI, new String[]{
                        PhilPad.Apps.COLUMN_NAME_COMPONENT_NAME
                }, PhilPad.Apps.COLUMN_NAME_COMPONENT_NAME + "=?", new String[]{
                        componentName
                }, null);

                if (c == null || c.getCount() > 0) {
                    continue;
                }

                ContentValues cv = new ContentValues();
                cv.put(PhilPad.Apps.COLUMN_NAME_COMPONENT_NAME, componentName);
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
                    if(!mypath2.exists())
                    {
                        fos2 = new FileOutputStream(mypath2);
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos2);
                        fos2.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                cv.put(PhilPad.Apps.COLUMN_NAME_IMAGEFILE, "file://" + mypath.getPath());
                Uri uri = getContentResolver().insert(PhilPad.Apps.CONTENT_URI, cv);
                if(bitmap != null)
                {
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
                } catch (Exception e) {
                    e.printStackTrace();
                }

                progressDialog = null;
            }
            PhilPad.Settings.putBoolean(getContentResolver(), PhilPad.Settings.SETTINGS_KEY_COMPLETE_TOTURIAL, true);
        }
    }

    class PadViewAdapter extends SimpleCursorAdapter {

        public PadViewAdapter(Context context, int layout, Cursor c, String[] from, int[] to) {
            super(context, layout, c, from, to);
            mContext = context;
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            final LayoutInflater inflater = LayoutInflater.from(context);
            View v = inflater.inflate(R.layout.list_icon_item, parent, false);
            return v;
        }

        @Override
        public void bindView(View v, Context context, Cursor cursor) {
            ImageView imageView = (ImageView) v.findViewById(R.id.item_image);
            TextView textView = (TextView) v.findViewById(R.id.item_text);
            String filePath = cursor.getString(cursor.getColumnIndex(PhilPad.Apps.COLUMN_NAME_IMAGEFILE));
            Picasso.with(AppListActivity.this).load(filePath).into(imageView);
            imageView.setVisibility(View.VISIBLE);
            textView.setText(cursor.getString(cursor.getColumnIndex(PhilPad.Apps.COLUMN_NAME_TITLE)));
        }
    }
}
