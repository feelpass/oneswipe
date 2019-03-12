
package com.philleeran.flicktoucher.view.manage;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;

import android.preference.SwitchPreference;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.apache.commons.io.FileUtils;


import com.philleeran.flicktoucher.PadUtils;
import com.philleeran.flicktoucher.R;
import com.philleeran.flicktoucher.db.PhilPad;
import com.philleeran.flicktoucher.utils.L;
import com.philleeran.flicktoucher.utils.Utils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ManageFragment extends PreferenceFragment implements OnPreferenceClickListener {

    private PackageManager pm;

    private ProgressDialog mProgress;

    private AlertDialog mDialog;

    private Preference mClearData;
    private Preference mVersion;

    private PreferenceCategory mDeveloperCategory;
    private Preference mDeveloperModePref;
    private Preference mDeveloperPremiumModePref;

    private int clickCount;
    private Thread mThread;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        pm = getActivity().getPackageManager();

        addPreferencesFromResource(R.xml.manage_options);

        mClearData = findPreference("key_clear_data");
        mClearData.setOnPreferenceClickListener(this);
        mClearData.setSummary(getDataSize());

        mVersion = findPreference("key_version");
        mVersion.setOnPreferenceClickListener(this);

        if (isDeveloper()) {
            L.v("you are developer!");
            addDeveloperPreference();
        }

        try {
            String versionName = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionName;
            if (versionName.startsWith("DE") || versionName.startsWith("de")) {
               // addFeaturePreference();
            }
        } catch (PackageManager.NameNotFoundException e) {
        }

        clickCount = 0;
    }
    private Boolean isDebugUser() {
        return Build.TYPE.equals("eng") || Build.TYPE.equals("userdebug");//NOPMD
    }

    private Boolean isDeveloper() {
        if(PhilPad.Settings.getBoolean(getActivity().getContentResolver(), PhilPad.Settings.SETTINGS_KEY_DEVELOPER_MODE, false) == true) {
            return true;
        } else {
            return false;
        }
    }

    private void removeDeveloperPreference() {
        if (mDeveloperCategory != null) {
            getPreferenceScreen().removePreference(mDeveloperCategory);
            mDeveloperCategory = null;
        }
    }
    private void addDeveloperPreference() {
        if (mDeveloperCategory == null) {
            boolean developer = PhilPad.Settings.getBoolean(getActivity().getContentResolver(), PhilPad.Settings.SETTINGS_KEY_DEVELOPER_MODE, false);
            mDeveloperCategory = new PreferenceCategory(getActivity());
            mDeveloperCategory.setTitle(R.string.settings_title_developer);
            getPreferenceScreen().addPreference(mDeveloperCategory);

            mDeveloperModePref = new SwitchPreference(getActivity());
            mDeveloperModePref.setTitle(R.string.settings_title_developer_mode);
            mDeveloperModePref.setOnPreferenceChangeListener( new OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    PhilPad.Settings.putBoolean(getActivity().getContentResolver(), PhilPad.Settings.SETTINGS_KEY_DEVELOPER_MODE, (Boolean) newValue);
                    ((SwitchPreference) mDeveloperModePref).setChecked((Boolean) newValue);
                    if ((Boolean) newValue == false) {
                        removeDeveloperPreference();
                    }
                    return true;
                }
            });
            mDeveloperModePref.setDefaultValue(developer);
            mDeveloperCategory.addPreference(mDeveloperModePref);

            mDeveloperPremiumModePref = new SwitchPreference(getActivity());
            mDeveloperPremiumModePref.setTitle(R.string.settings_title_developer_premium_mode);
            mDeveloperPremiumModePref.setOnPreferenceChangeListener( new OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    PhilPad.Settings.putBoolean(getActivity().getContentResolver(), PhilPad.Settings.SETTINGS_KEY_IS_PREMIUM, (Boolean)newValue);
                    ((SwitchPreference) mDeveloperPremiumModePref).setChecked((Boolean) newValue);
                    return true;
                }
            });
            mDeveloperPremiumModePref.setDefaultValue(PhilPad.Settings.getBoolean(getActivity().getContentResolver(), PhilPad.Settings.SETTINGS_KEY_IS_PREMIUM, false));
            mDeveloperCategory.addPreference(mDeveloperPremiumModePref);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        setListSummary();
    }

    private void setListSummary() {
        String packageName = getActivity().getPackageName();
        String versionName = Utils.getVersionNameByPackageName(getActivity(), packageName);
        int versionCode = Utils.getVersionCodeByPackageName(getActivity(), packageName);
        String buildTime = Utils.getApkBuildDate(getActivity(), packageName);
        mVersion.setSummary(versionCode + " / " + versionName + " / " + buildTime);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        removeDeveloperPreference();
    }
    private void showDialogInner() {
        try {
            DialogFragment newFragment = MyAlertDialogFragment.newInstance();
            newFragment.setTargetFragment(this, 0);
            newFragment.show(getFragmentManager(), "dialog ");
        } catch (NullPointerException e) {
            L.e("Nullpointer exception in showDialogInner");
        } catch (IllegalStateException e) {
            L.e("Illegal State exception in showDialogInner");
        }
    }


    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference == mClearData) {

            if (preference == mClearData) { //NOPMD
                showDialogInner();
            }
        } else if (preference == mVersion) {
            L.v("developer check! clickCount=" + clickCount);
            clickCount++;
            if (clickCount > 5) {
                if (isDeveloper() == false) {
                    showPasswordDialog(getActivity());
                }
                clickCount = 0;
            }
        }
        return false;
    }

    private void showPasswordDialog(Context context) {
        final LinearLayout linear = (LinearLayout) View.inflate(context, R.layout.dialog_password, null);
        final EditText editTextPassword = (EditText) linear.findViewById(R.id.password_engine_select_edittext);
        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }
        mDialog = new AlertDialog.Builder(context)//
                .setTitle(R.string.developer_options)//
                .setView(linear)//
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("MMddHHmm");
                        Date currentTime = new Date();
                        String mTime = mSimpleDateFormat.format(currentTime);
                        String password = editTextPassword.getText().toString();
                        if (password.equals(mTime)) {
                            PhilPad.Settings.putBoolean(getActivity().getContentResolver(), PhilPad.Settings.SETTINGS_KEY_DEVELOPER_MODE, true);
                            PhilPad.Settings.putBoolean(getActivity().getContentResolver(), PhilPad.Settings.SETTINGS_KEY_IS_PREMIUM, true);
                            addDeveloperPreference();
                        } else {
                            PadUtils.ToastLong(getActivity(), "Fail");
                        }
                    }
                }).setNegativeButton(android.R.string.cancel, null).create();
        mDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(editTextPassword, InputMethodManager.SHOW_IMPLICIT);
            }
        });
        mDialog.show();
        mDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
        editTextPassword.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() < 4) {
                    mDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                } else {
                    mDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                }
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void afterTextChanged(Editable s) {
            }
        });
        CheckBox mShowPassword = (CheckBox) linear.findViewById(R.id.password_trackee_checkbox);
        mShowPassword.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked == true) {
                    editTextPassword.setTransformationMethod(null);
                    editTextPassword.setSelection(editTextPassword.length());
                } else if (isChecked == false) {
                    PasswordTransformationMethod passwdtm = new PasswordTransformationMethod();
                    editTextPassword.setTransformationMethod(passwdtm);
                    editTextPassword.setSelection(editTextPassword.length());
                }
            }
        });
    }
    private void startProgress() {
        if (mProgress == null) {
            mProgress = new ProgressDialog(getActivity());
            mProgress.dismiss();
            mProgress.setTitle("Loading...");
            mProgress.setCancelable(false);
            mProgress.setButton(Dialog.BUTTON_NEUTRAL, getString(android.R.string.cancel), new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    mThread.interrupt();
                }
            });
            mProgress.show();
            mThread = new Thread(new DeveloperChecker(), "developer check");
            mThread.start();
        }
    }

    class DeveloperChecker implements Runnable {

        public void run() {
            long a = System.currentTimeMillis();
            if (checkDeveloper()) {
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(getActivity(), "You are a developer!", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                    }
                });
            }
            closeDialog();
            L.v("elapsed=" + (System.currentTimeMillis() - a));
        }
    }

    private boolean checkDeveloper() {
        List<PackageInfo> info = pm.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);
        for (final PackageInfo pi : info) {
            boolean isSystem = (pi.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
            boolean isSystemUpdate = (pi.applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0;
            boolean isInterrupted = Thread.currentThread().isInterrupted();

            if (isInterrupted) {
                L.v("isInterrupted=" + isInterrupted);
                return false;
            }
        }
        return false;
    }

    private void closeDialog() {
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                if (mProgress != null) {
                    mProgress.dismiss();
                    mProgress = null;
                }
            }
        });
    }

    private void updateDialogMessage(final CharSequence label) {
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                if (mProgress != null) {
                    mProgress.setMessage(label);
                }
            }
        });
    }

    private String getDataSize() {
        File[] dataDirs = new File[] {
                new File(getActivity().getApplicationInfo().dataDir)
        };

        long totalSize = 0;
        String cacheSize;

        for (File dir : dataDirs) {
            if (!dir.getName().equals("lib")) {
                totalSize += FileUtils.sizeOfDirectory(dir);
            }
        }
        cacheSize = FileUtils.byteCountToDisplaySize(totalSize);
        L.v(cacheSize);
        return cacheSize;
    }









    public static class MyAlertDialogFragment extends DialogFragment {

        public static MyAlertDialogFragment newInstance() {
            MyAlertDialogFragment frag = new MyAlertDialogFragment();
            Bundle args = new Bundle();
            frag.setArguments(args);
            return frag;
        }

        //SNMC-HOMESHELL-P120926-7092-VARUNAIRON-START
        @Override
        public void onCancel(DialogInterface dialog) {
            super.onCancel(dialog);
            int id = getArguments().getInt("id");

        }
        //SNMC-HOMESHELL-P120926-7092-VARUNAIRON-END


        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return new AlertDialog.Builder(getActivity())
                    .setTitle(getActivity().getText(R.string.dialog_clear_user_data_text))
                    .setMessage(getActivity().getText(R.string.dialog_clear_data_dlg_text))
                    .setPositiveButton(R.string.dialog_clear_dlg_ok,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // Clear user data here
                                    clearApplicationData(getActivity());
                                }
                            })
                    .setNegativeButton(R.string.dialog_no, null)
                    .create();
        }

        private static void clearApplicationData(Context context) {
            ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            boolean res = am.clearApplicationUserData();
            if (res == true) {
                Toast.makeText(context, "clear application data complete", Toast.LENGTH_SHORT).show();
            }
        }
    }





}
