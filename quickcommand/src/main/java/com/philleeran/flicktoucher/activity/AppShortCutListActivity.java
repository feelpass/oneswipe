
package com.philleeran.flicktoucher.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.content.ServiceConnection;
import android.content.pm.ComponentInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.os.RemoteException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.philleeran.flicktoucher.IPhilPad;
import com.philleeran.flicktoucher.PadUtils;
import com.philleeran.flicktoucher.R;
import com.philleeran.flicktoucher.db.PhilPad;
import com.philleeran.flicktoucher.utils.D;
import com.philleeran.flicktoucher.utils.L;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

public class AppShortCutListActivity extends Activity implements OnItemClickListener {

    IPhilPad mPadBind;

    int index = 0;

    private Context mContext = null;

    private ListView mListView;

    private PadAddViewAdapter mPadAddViewAdapter;

    private LinearLayout mPadListViewLayout;

    private int mGroupId;

    private int mPositionId;

    private PackageManager mPackageManager;

    private ProgressDialog progressDialog;

    private List<ResolveInfo> mApps;
    private int mPadSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        mPadSize = Integer.parseInt(PhilPad.Settings.getString(mContext.getContentResolver(), PhilPad.Settings.SETTINGS_KEY_PAD_SIZE, "4"));

        setContentView(R.layout.app_list_activity);

        mPackageManager = getPackageManager();

        mPadListViewLayout = (LinearLayout) findViewById(R.id.listview_layout);
        mListView = new ListView(mContext);
        Intent mainIntent = new Intent(Intent.ACTION_CREATE_SHORTCUT, null);
        mApps = mPackageManager.queryIntentActivities(mainIntent, 0);
        mPadAddViewAdapter = new PadAddViewAdapter(mContext, mListView);
        mListView.setAdapter(mPadAddViewAdapter);
        mListView.setOnItemClickListener(this);
        mPadListViewLayout.addView(mListView);
        Intent intent = getIntent();

        mGroupId = intent.getIntExtra(PadUtils.INTENT_DATA_GROUPID, 0);
        mPositionId = intent.getIntExtra(PadUtils.INTENT_DATA_LISTID, 0);

        Intent serviceIntent = new Intent(PadUtils.ACTION_PHILPAD_SERVICE);
        L.d("startPhilPadService");
        serviceIntent.setPackage(PadUtils.ACTION_PHILPAD_PACKAGE);
        serviceIntent.setAction(PadUtils.ACTION_PHILPAD_SERVICE);
        bindService(serviceIntent, conn, BIND_AUTO_CREATE);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    class PadAddViewAdapter extends BaseAdapter {
        private Context mContext;

        ViewHolder viewHolder = null;

        public PadAddViewAdapter(Context context, ListView mListView) {
            mContext = context;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                v = LayoutInflater.from(mContext).inflate(R.layout.list_icon_item, null);
                viewHolder = new ViewHolder();
                viewHolder.imageView = (ImageView) v.findViewById(R.id.item_image);
                viewHolder.textView = (TextView) v.findViewById(R.id.item_text);
            } else {
                viewHolder = (ViewHolder) v.getTag();
            }
            viewHolder.imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            viewHolder.imageView.setImageDrawable(mApps.get(position).loadIcon(mPackageManager));
            // imageLoader.displayImage("drawable://" +
            // mApps.get(position).loadIcon(mPackageManager),
            // viewHolder.imageView, options);
            viewHolder.textView.setText(mApps.get(position).loadLabel(mPackageManager));
            v.setTag(viewHolder);
            return v;
        }

        class ViewHolder {
            protected TextView textView;

            protected ImageView imageView;
        }

        public final int getCount() {
            return mApps.size();
        }

        public final Object getItem(int position) {
            return null;
        }

        public final long getItemId(int position) {
            return position;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {

        Intent createShortcutIntent = new Intent(Intent.ACTION_CREATE_SHORTCUT);
        ComponentInfo ci = mApps.get(position).activityInfo;
        createShortcutIntent.setComponent(new ComponentName(ci.packageName, ci.name));
        startActivityForResult(createShortcutIntent, 0);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_CANCELED) {
            finish();
            return;
        }
        Intent intent = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT);
        String name = data.getStringExtra(Intent.EXTRA_SHORTCUT_NAME);
        Bitmap bitmap = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON);

        ShortcutIconResource iconResource = null;

        if (bitmap != null && bitmap instanceof Bitmap) {
            ContextWrapper cw = new ContextWrapper(getApplicationContext());
            File directory = cw.getDir(D.DIR_PATH_DEFAULT, Context.MODE_PRIVATE);
            File mypath = new File(directory, "shortcut_" + mGroupId + "_" + mPositionId + "_" + name + ".png");
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(mypath);
                ((Bitmap) bitmap).compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            PhilPad.Pads.setPadItem(mContext, mGroupId, mPositionId, PhilPad.Pads.PAD_TYPE_SHORTCUT, "shortcut", null, "file://" + mypath.getPath(),0,  intent.toUri(0));
            PhilPad.Pads.setGroupIcon(mContext, mGroupId, mPadSize);
        } else {
            Parcelable extra = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE);
            if (extra != null && extra instanceof ShortcutIconResource) {
                iconResource = (ShortcutIconResource) extra;
                try {
                    final PackageManager packageManager = mContext.getPackageManager();
                    Resources resources;
                    resources = packageManager.getResourcesForApplication(iconResource.packageName);
                    final int id = resources.getIdentifier(iconResource.resourceName, null, null);
                    Bitmap bm = BitmapFactory.decodeResource(resources, id);
                    if (bm != null) {
                        ContextWrapper cw = new ContextWrapper(getApplicationContext());
                        File directory = cw.getDir(D.DIR_PATH_DEFAULT, Context.MODE_PRIVATE);
                        File mypath = new File(directory, "shortcut_" + mGroupId + "_" + mPositionId + "_" + name + ".png");
                        FileOutputStream fos = null;
                        try {
                            fos = new FileOutputStream(mypath);
                            bm.compress(Bitmap.CompressFormat.PNG, 100, fos);
                            fos.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        PhilPad.Pads.setPadItem(mContext, mGroupId, mPositionId, PhilPad.Pads.PAD_TYPE_SHORTCUT, "shortcut", null, "file://" + mypath.getPath(),0, intent.toUri(0));
                        PhilPad.Pads.setGroupIcon(mContext, mGroupId, mPadSize);
                    }
                } catch (NameNotFoundException e1) {
                    e1.printStackTrace();
                }
            }
        }
        try {
            mPadBind.notifyReDrawGridView();
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        finish();
    }

    public class ListAppTask extends AsyncTask<Void, Void, Void> {

        protected Void doInBackground(Void... args) {
            Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

            mApps = mPackageManager.queryIntentActivities(mainIntent, 0);
            return null;
        }

        protected void onPostExecute(Void result) {
            if (progressDialog != null) {
                progressDialog.dismiss();
                progressDialog = null;
            }
            mListView.setVisibility(View.VISIBLE);
        }
    }

    ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mPadBind = IPhilPad.Stub.asInterface(service);
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mPadBind = null;
        }
    };

}
