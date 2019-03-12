
package com.philleeran.flicktoucher.view.context;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.philleeran.flicktoucher.PadUtils;
import com.philleeran.flicktoucher.R;
import com.philleeran.flicktoucher.utils.D;
import com.squareup.picasso.Picasso;

public class ContextActivity extends AppCompatActivity implements OnItemClickListener {

    int index = 0;

    private Context mContext = null;

    private ListView mListView;

    private PadAddViewAdapter mPadAddViewAdapter;

    private LinearLayout mPadListViewLayout;

    private String mPackageName;

    private TextView mPackageNameTextView;
    private TextView mApplicationNameTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;

        setContentView(R.layout.context_list_activity);
        mPadListViewLayout = (LinearLayout) findViewById(R.id.listview_layout);
        mPackageNameTextView = (TextView) findViewById(R.id.package_name);
        mApplicationNameTextView = (TextView) findViewById(R.id.application_name);
        mListView = new ListView(mContext);
        mPadAddViewAdapter = new PadAddViewAdapter(mContext, mListView);
        mListView.setAdapter(mPadAddViewAdapter);
        mListView.setOnItemClickListener(this);
        mPadListViewLayout.addView(mListView);
        Intent intent = getIntent();

        mPackageName = intent.getStringExtra(PadUtils.INTENT_DATA_PACKAGENAME);

        PackageInfo packageInfo = null;
        ApplicationInfo appInfo = null;
        try {
            packageInfo = getPackageManager().getPackageInfo(mPackageName, 0);
            appInfo = getPackageManager().getApplicationInfo(packageInfo.packageName, 0);
        } catch (final NameNotFoundException e) {
        }


        final String title = (String) ((appInfo != null) ? getPackageManager().getApplicationLabel(appInfo) : "???");
        mPackageNameTextView.setText(packageInfo.packageName + " / " + packageInfo.versionName + " / " + packageInfo.versionCode);
        mApplicationNameTextView.setText(title);

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
    protected void onPause() {
        super.onPause();

        if (isFinishing() == false)
            finish();
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
            Picasso.with(mContext).load(D.Context.mContextIconArray[position]).error(D.Context.mContextIconArray[position]).into(viewHolder.imageView);

            viewHolder.textView.setText(D.Context.mContextStringIdArray[position]);
            v.setTag(viewHolder);
            return v;
        }

        class ViewHolder {
            protected TextView textView;
            protected ImageView imageView;
        }

        public final int getCount() {
            return D.Context.mContextStringIdArray.length;
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
        switch (position) {
            case D.Context.ITEM_SELECTED_GOOGLEPLAY: {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("market://details?id=" + mPackageName));
                startActivity(intent);
            }
            break;
            case D.Context.ITEM_SELECTED_APPLICATIONINFO: {
                try {
                    Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(Uri.parse("package:" + mPackageName));
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(intent);
                }
            }
            break;
            case D.Context.ITEM_SELECTED_DELETE: {
                Intent i = new Intent(Intent.ACTION_DELETE);
                i.setData(Uri.parse("package:" + mPackageName));
                mContext.startActivity(i);
            }
            break;
            case D.Context.ITEM_SELECTED_SHARE: {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
                StringBuilder sAux = new StringBuilder();
                sAux.append(getString(R.string.pref_title_share_by_context));
                sAux.append("https://play.google.com/store/apps/details?id=" + mPackageName + "\n\n");
                sAux.append(getString(R.string.pref_title_share_from_quickswipe));
                shareIntent.putExtra(Intent.EXTRA_TEXT, sAux.toString());
                mContext.startActivity(shareIntent);
            }
            break;
            default:
                break;
        }
        finish();
    }


}
