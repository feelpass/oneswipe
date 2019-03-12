
package com.philleeran.flicktoucher.view.settings;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.util.AttributeSet;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.philleeran.flicktoucher.PadUtils;
import com.philleeran.flicktoucher.R;
import com.philleeran.flicktoucher.activity.AppIconResetActivity;
import com.philleeran.flicktoucher.activity.MemoryCleanAppListActivity;
import com.philleeran.flicktoucher.activity.SeekBarPreference;
import com.philleeran.flicktoucher.activity.ThemeSelectActivity;
import com.philleeran.flicktoucher.colorpicker.ColorPickerPreference;
import com.philleeran.flicktoucher.db.LocalSettings;
import com.philleeran.flicktoucher.db.PadItemInfo;
import com.philleeran.flicktoucher.db.PhilPad;
import com.philleeran.flicktoucher.utils.D;
import com.philleeran.flicktoucher.utils.L;
import com.philleeran.flicktoucher.utils.Utils;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;

public class SettingFragment extends PreferenceFragment {

    protected int mCount = 0;

    private AlertDialog mDialog;

    private CheckBoxPreference checkBoxTriggerVisible;

    private PreferenceScreen intentPrefTheme;

    private PreferenceScreen intentPrefDataAppListCache;

    private ColorPickerPreference mColorPickerPreferenceBackground;

    private ColorPickerPreference mColorPickerPreferencePadBackground;

    private PreferenceScreen intentPrefHotSpot;

    private PreferenceScreen intentPrefHotSpotCustomize;


    private ListPreference listPrefPadSize;

    private ListPreference listPrefDynamicPadOption;

    private ListPreference listPrefInteraction;


    private PreferenceScreen intentPrefSetDefaultColor;

    private CheckBoxPreference checkBoxPrefHaptic;

    private CheckBoxPreference checkBoxPrefAnimation;

    private CheckBoxPreference checkBoxPrefGesture;

    private CheckBoxPreference checkBoxPrefInfoView;

    private CheckBoxPreference checkBoxPrefShowTitle;

    private PreferenceScreen intentPrefTranslateHelp;

    private PreferenceScreen intentPrefTranslateThanks;

    private PreferenceScreen intentPrefRate;

    private PreferenceScreen intentPrefFacebookLike;

    private PreferenceScreen intentPrefFeedback;

    private PreferenceScreen intentPrefShare;

    private PreferenceScreen intentPrefVersion;

    private PreferenceScreen intentAppsExcludeWhileBoosting;
    private Context mContext;
    private SeekBarPreference seekBarPrefVibrationLevel;
    private SeekBarPreference seekBarPrefAnimationDuratitonLevel;


    @Override
    public void onResume() {
        super.onResume();

        mColorPickerPreferenceBackground.onColorChanged(PhilPad.Settings.getInt(mContext.getContentResolver(), PhilPad.Settings.SETTINGS_KEY_BACKGROUND_COLOR,
                mContext.getResources().getColor(R.color.color_background)));
        mColorPickerPreferencePadBackground.onColorChanged(PhilPad.Settings.getInt(mContext.getContentResolver(), PhilPad.Settings.SETTINGS_KEY_PAD_BACKGROUND_COLOR, mContext.getResources()
                .getColor(R.color.color_pad_background)));

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getActivity();
        setPreferenceScreen(createPreferenceHierarchy());

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    private PreferenceScreen createPreferenceHierarchy() {
        // Root
        PreferenceScreen root = getPreferenceManager().createPreferenceScreen(mContext);
        // PreferenceScreen intentPref;
        PreferenceCategory inlinePrefCat;

        // // Inline preferences
        inlinePrefCat = new PreferenceCategory(mContext);
        inlinePrefCat.setTitle(R.string.pref_category_basic);
        root.addPreference(inlinePrefCat);

        intentPrefHotSpot = getPreferenceManager().createPreferenceScreen(mContext);
        intentPrefHotSpot.setTitle(R.string.pref_trigger_area_title);
        intentPrefHotSpot.setIntent(new Intent(mContext, TriggerSettingActivity.class));
        root.addPreference(intentPrefHotSpot);

        checkBoxTriggerVisible = new CheckBoxPreference(mContext);
        checkBoxTriggerVisible.setTitle(R.string.pref_trigger_visible);
        checkBoxTriggerVisible.setDefaultValue(PhilPad.Settings.getBoolean(mContext.getContentResolver(), PhilPad.Settings.SETTINGS_KEY_PAD_TRIGGER_VISIBLE, true));
        checkBoxTriggerVisible.setSummary(R.string.pref_trigger_visible_summary);
        checkBoxTriggerVisible.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                PhilPad.Settings.putBoolean(mContext.getContentResolver(), PhilPad.Settings.SETTINGS_KEY_PAD_TRIGGER_VISIBLE, (Boolean) newValue);
                if ((Boolean) newValue) {
                    LocalSettings.mTrigerDefaultAlpha = D.TIRIGGER_DEFAULT_ALPHA;

                    try {
                        ((SettingsActivity) getActivity()).mPad.notiHotspotsVisible(true);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }

                } else {
                    LocalSettings.mTrigerDefaultAlpha = 0.0f;
                    try {
                        ((SettingsActivity) getActivity()).mPad.notiHotspotsVisible(false);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                return true;
            }
        });
        root.addPreference(checkBoxTriggerVisible);

        intentPrefHotSpotCustomize = getPreferenceManager().createPreferenceScreen(mContext);
        intentPrefHotSpotCustomize.setTitle(R.string.pref_trigger_area_title);
        intentPrefHotSpotCustomize.setIntent(new Intent(mContext, SettingsHotspotsCustomizeActivity.class));

        listPrefPadSize = new ListPreference(mContext);
        listPrefPadSize.setTitle(R.string.pref_title_pad_size);
        String padSize = PhilPad.Settings.getString(mContext.getContentResolver(), PhilPad.Settings.SETTINGS_KEY_PAD_SIZE, "4");
        listPrefPadSize.setSummary(padSize + "x" + padSize);
        listPrefPadSize.setEntries(R.array.settings_padsize);
        listPrefPadSize.setEntryValues(R.array.settings_padsize_value);
        listPrefPadSize.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                L.d("onPreferenceChange");
                PhilPad.Settings.putString(mContext.getContentResolver(), PhilPad.Settings.SETTINGS_KEY_PAD_SIZE, (String) newValue);
                ((SettingsActivity) (mContext)).mSwitch.setChecked(false);
                String padSize = PhilPad.Settings.getString(mContext.getContentResolver(), PhilPad.Settings.SETTINGS_KEY_PAD_SIZE, "4");
                listPrefPadSize.setSummary(padSize + "x" + padSize);
                ArrayList<PadItemInfo> infos = PhilPad.Pads.getPadGroups(mContext.getContentResolver(), PhilPad.Pads.DEFAULT_SORT_ORDER);
                PhilPad.Pads.setGroupIcon(mContext, D.GROUPID_GROUND, Integer.parseInt(padSize));
                for (PadItemInfo info : infos) {
                    PhilPad.Pads.setGroupIcon(mContext, info.getToGroupId(), Integer.parseInt(padSize));
                    L.d("group Id : " + info.getType() + " , " + info.getToGroupId());
                }
                return true;
            }
        });
        listPrefPadSize.setDefaultValue(padSize);
        root.addPreference(listPrefPadSize);

        listPrefDynamicPadOption = new ListPreference(mContext);
        listPrefDynamicPadOption.setTitle(R.string.pref_title_pad_dynamic_pad_option);
        listPrefDynamicPadOption.setEntries(R.array.settings_dynamic_option);
        listPrefDynamicPadOption.setEntryValues(R.array.settings_dynamic_option_value);
        listPrefDynamicPadOption.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                int dynamicOption = Integer.parseInt((String) newValue);
                PhilPad.Settings.putInt(mContext.getContentResolver(), PhilPad.Settings.SETTINGS_KEY_DYNAMIC_OPTION, Integer.parseInt((String) newValue));
                switch (dynamicOption) {
                    case D.DynamicPadOption.DYNAMIC_PAD_OPTION_NONE:
                        listPrefDynamicPadOption.setSummary(R.string.pref_title_pad_dynamic_pad_option_summary_none);
                        break;
                    case D.DynamicPadOption.DYNAMIC_PAD_OPTION_PENWINDOW:
                        listPrefDynamicPadOption.setSummary(R.string.pref_title_pad_dynamic_pad_option_summary_penwindow);
                        break;
                    case D.DynamicPadOption.DYNAMIC_PAD_OPTION_FORCE_CLOSE:
                        listPrefDynamicPadOption.setSummary(R.string.pref_title_pad_dynamic_pad_option_summary_forceclose);
                        break;
                    default:
                        listPrefDynamicPadOption.setSummary(R.string.pref_title_pad_dynamic_pad_option_summary_none);
                        break;
                }
                return true;
            }
        });

        int dynamicOption = PhilPad.Settings.getInt(mContext.getContentResolver(), PhilPad.Settings.SETTINGS_KEY_DYNAMIC_OPTION, 0);
        switch (dynamicOption) {
            case D.DynamicPadOption.DYNAMIC_PAD_OPTION_NONE:
                listPrefDynamicPadOption.setSummary(R.string.pref_title_pad_dynamic_pad_option_summary_none);
                break;
            case D.DynamicPadOption.DYNAMIC_PAD_OPTION_PENWINDOW:
                listPrefDynamicPadOption.setSummary(R.string.pref_title_pad_dynamic_pad_option_summary_penwindow);
                break;
            case D.DynamicPadOption.DYNAMIC_PAD_OPTION_FORCE_CLOSE:
                listPrefDynamicPadOption.setSummary(R.string.pref_title_pad_dynamic_pad_option_summary_forceclose);
                break;
            default:
                listPrefDynamicPadOption.setSummary(R.string.pref_title_pad_dynamic_pad_option_summary_none);
                break;
        }

        listPrefDynamicPadOption.setDefaultValue(String.valueOf(PhilPad.Settings.getInt(mContext.getContentResolver(), PhilPad.Settings.SETTINGS_KEY_DYNAMIC_OPTION, 0)));

        listPrefInteraction = new ListPreference(mContext);
        listPrefInteraction.setTitle(R.string.pref_title_pad_interaction);
        listPrefInteraction.setEntries(R.array.settings_interaction_option);
        listPrefInteraction.setEntryValues(R.array.settings_interaction_option_value);
        listPrefInteraction.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                int interactionOption = Integer.parseInt((String) newValue);
                PhilPad.Settings.putInt(mContext.getContentResolver(), PhilPad.Settings.SETTINGS_KEY_INTERACTION_OPTION, Integer.parseInt((String) newValue));
                switch (interactionOption) {
                    case D.InteractionOption.INTERACTION_OPTION_SWIPE_AND_TOUCH:
                        listPrefInteraction.setSummary(R.string.pref_title_pad_interaction_summary_swipe_and_touch);
                        break;
                    case D.InteractionOption.INTERACTION_OPTION_SWIPE_AND_UP:
                        listPrefInteraction.setSummary(R.string.pref_title_pad_interaction_summary_swipe_and_up);
                        break;
                }
                return true;
            }
        });
        int interactionOption = PhilPad.Settings.getInt(mContext.getContentResolver(), PhilPad.Settings.SETTINGS_KEY_INTERACTION_OPTION, D.InteractionOption.INTERACTION_OPTION_SWIPE_AND_TOUCH);
        switch (interactionOption) {
            case D.InteractionOption.INTERACTION_OPTION_SWIPE_AND_TOUCH:
                listPrefInteraction.setSummary(R.string.pref_title_pad_interaction_summary_swipe_and_touch);
                break;
            case D.InteractionOption.INTERACTION_OPTION_SWIPE_AND_UP:
                listPrefInteraction.setSummary(R.string.pref_title_pad_interaction_summary_swipe_and_up);
                break;
            default:
                listPrefInteraction.setSummary("none");
                break;
        }
        listPrefInteraction.setDefaultValue(String.valueOf(PhilPad.Settings.getInt(mContext.getContentResolver(), PhilPad.Settings.SETTINGS_KEY_INTERACTION_OPTION, D.InteractionOption.INTERACTION_OPTION_SWIPE_AND_UP)));

        root.addPreference(listPrefInteraction);

        inlinePrefCat = new PreferenceCategory(mContext);
        inlinePrefCat.setTitle(R.string.pref_booster);
        root.addPreference(inlinePrefCat);

        intentAppsExcludeWhileBoosting = getPreferenceManager().createPreferenceScreen(mContext);
        intentAppsExcludeWhileBoosting.setTitle(R.string.pref_booster_apps_excluded_while_boosting_title);
        intentAppsExcludeWhileBoosting.setIntent(new Intent(mContext, MemoryCleanAppListActivity.class));
        root.addPreference(intentAppsExcludeWhileBoosting);


        inlinePrefCat = new PreferenceCategory(mContext);
        inlinePrefCat.setTitle(R.string.pref_category_data);
        root.addPreference(inlinePrefCat);

        intentPrefDataAppListCache = getPreferenceManager().createPreferenceScreen(mContext);
        intentPrefDataAppListCache.setTitle(R.string.pref_data_app_list_cache_reset_title);
        intentPrefDataAppListCache.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setMessage(R.string.dialog_areyoushare).setCancelable(true).setPositiveButton(R.string.dialog_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(getActivity(), AppIconResetActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                }).setNegativeButton(R.string.dialog_no, null).create().show();
                return true;
            }
        });
        intentPrefDataAppListCache.setIntent(new Intent(mContext, AppIconResetActivity.class));
        intentPrefDataAppListCache.setSummary(R.string.pref_data_app_list_cache_reset_summary);
        root.addPreference(intentPrefDataAppListCache);

        inlinePrefCat = new PreferenceCategory(mContext);
        inlinePrefCat.setTitle(R.string.pref_category_style);
        root.addPreference(inlinePrefCat);

        intentPrefTheme = getPreferenceManager().createPreferenceScreen(mContext);
        intentPrefTheme.setTitle(R.string.pref_theme_select_title);
        intentPrefTheme.setIntent(new Intent(mContext, ThemeSelectActivity.class));
        intentPrefTheme.setSummary(R.string.pref_theme_select_summary);
        root.addPreference(intentPrefTheme);

        mColorPickerPreferenceBackground = new ColorPickerPreference(mContext);
        mColorPickerPreferenceBackground.setKey(PhilPad.Settings.SETTINGS_KEY_BACKGROUND_COLOR);
        mColorPickerPreferenceBackground.setTitle(R.string.dialog_color_picker_background);
        mColorPickerPreferenceBackground.setSummary(R.string.dialog_color_picker_background_summary);
        mColorPickerPreferenceBackground.setDefaultValue(PhilPad.Settings.getInt(mContext.getContentResolver(), PhilPad.Settings.SETTINGS_KEY_BACKGROUND_COLOR,
                mContext.getResources().getColor(R.color.color_background)));

        mColorPickerPreferenceBackground.setHexValueEnabled(true);
        mColorPickerPreferenceBackground.setAlphaSliderEnabled(true);
        mColorPickerPreferenceBackground.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                try {
                    PhilPad.Settings.putInt(mContext.getContentResolver(), PhilPad.Settings.SETTINGS_KEY_BACKGROUND_COLOR, (Integer) newValue);
                    ((SettingsActivity) mContext).mPad.notifyReDrawGridViewBackground();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                return true;
            }
        });
        root.addPreference(mColorPickerPreferenceBackground);

        mColorPickerPreferencePadBackground = new ColorPickerPreference(mContext);
        mColorPickerPreferencePadBackground.setKey(PhilPad.Settings.SETTINGS_KEY_PAD_BACKGROUND_COLOR);
        mColorPickerPreferencePadBackground.setTitle(R.string.dialog_color_picker_pad_background);
        mColorPickerPreferencePadBackground.setSummary(R.string.dialog_color_picker_pad_background_summary);
        mColorPickerPreferencePadBackground.setDefaultValue(PhilPad.Settings.getInt(mContext.getContentResolver(), PhilPad.Settings.SETTINGS_KEY_PAD_BACKGROUND_COLOR, mContext.getResources()
                .getColor(R.color.color_pad_background)));

        mColorPickerPreferencePadBackground.setHexValueEnabled(true);
        mColorPickerPreferencePadBackground.setAlphaSliderEnabled(true);
        mColorPickerPreferencePadBackground.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                try {
                    PhilPad.Settings.putInt(mContext.getContentResolver(), PhilPad.Settings.SETTINGS_KEY_PAD_BACKGROUND_COLOR, (Integer) newValue);
                    ((SettingsActivity) mContext).mPad.notifyReDrawGridViewBackground();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                return true;
            }
        });
        root.addPreference(mColorPickerPreferencePadBackground);

        intentPrefSetDefaultColor = getPreferenceManager().createPreferenceScreen(mContext);
        intentPrefSetDefaultColor.setTitle(R.string.pref_title_default);
        intentPrefSetDefaultColor.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setMessage(R.string.dialog_areyoushare).setCancelable(true).setPositiveButton(R.string.dialog_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        PhilPad.Settings.putInt(mContext.getContentResolver(), PhilPad.Settings.SETTINGS_KEY_PAD_BACKGROUND_COLOR, Utils.getColor(mContext, R.color.color_pad_background));
                        PhilPad.Settings.putInt(mContext.getContentResolver(), PhilPad.Settings.SETTINGS_KEY_PAD_FOLDER_COLOR, Utils.getColor(mContext, R.color.color_folder_background));
                        PhilPad.Settings.putInt(mContext.getContentResolver(), PhilPad.Settings.SETTINGS_KEY_PAD_SPECIAL_COLOR, Utils.getColor(mContext, R.color.color_special_background));
                        PhilPad.Settings.putInt(mContext.getContentResolver(), PhilPad.Settings.SETTINGS_KEY_BACKGROUND_COLOR, Utils.getColor(mContext, R.color.color_background));

                        PhilPad.Settings.putString(mContext.getContentResolver(), PhilPad.Settings.SETTINGS_KEY_BACKGROUNDIMAGE_PATH, "");

                        ArrayList<PadItemInfo> infos = PhilPad.Pads.getPadGroups(mContext.getContentResolver(), PhilPad.Pads.DEFAULT_SORT_ORDER);
                        String padSize = PhilPad.Settings.getString(mContext.getContentResolver(), PhilPad.Settings.SETTINGS_KEY_PAD_SIZE, "4");
                        PhilPad.Pads.setGroupIcon(mContext, D.GROUPID_GROUND, Integer.parseInt(padSize));
                        for (PadItemInfo info : infos) {
                            PhilPad.Pads.setGroupIcon(mContext, info.getToGroupId(), Integer.parseInt(padSize));
                            L.d("group Id : " + info.getType() + " , " + info.getToGroupId());
                        }
                        try {
                            ((SettingsActivity) mContext).mPad.notifyReDrawGridViewBackground();
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                }).setNegativeButton(R.string.dialog_no, null).create().show();
                return true;
            }
        });

        checkBoxPrefHaptic = new CheckBoxPreference(mContext);
        checkBoxPrefHaptic.setTitle(R.string.pref_haptic_title);
        checkBoxPrefHaptic.setDefaultValue(PhilPad.Settings.getBoolean(mContext.getContentResolver(), PhilPad.Settings.SETTINGS_KEY_PAD_HAPTIC_ENABLE, true));
        checkBoxPrefHaptic.setSummary(R.string.pref_haptic_summary);
        checkBoxPrefHaptic.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                PhilPad.Settings.putBoolean(mContext.getContentResolver(), PhilPad.Settings.SETTINGS_KEY_PAD_HAPTIC_ENABLE, (Boolean) newValue);
                return true;
            }
        });
        root.addPreference(checkBoxPrefHaptic);

        XmlPullParser parser = getResources().getXml(R.xml.seekbar_pref_vibration_level_options);
        AttributeSet attributeSet = null;

        int state = 0;
        do {
            try {
                state = parser.next();
            } catch (XmlPullParserException e1) {
                L.e(e1);
            } catch (IOException e1) {
                L.e(e1);
            }
            if (state == XmlPullParser.START_TAG) {
                if (parser.getName().equals("com.philleeran.flicktoucher.activity.SeekBarPreference")) {
                    attributeSet = Xml.asAttributeSet(parser);
                    break;
                }
            }
        } while (state != XmlPullParser.END_DOCUMENT);

        seekBarPrefVibrationLevel = new SeekBarPreference(mContext, attributeSet);
        seekBarPrefVibrationLevel.setTitle(R.string.pref_vibration_level_title);
        seekBarPrefVibrationLevel.setDefaultValue(PhilPad.Settings.getInt(mContext.getContentResolver(), PhilPad.Settings.SETTINGS_KEY_PAD_VIBRATION_LEVEL, 10));
        seekBarPrefVibrationLevel.setSummary(PhilPad.Settings.getInt(mContext.getContentResolver(), PhilPad.Settings.SETTINGS_KEY_PAD_VIBRATION_LEVEL, 10) + "/100");
        seekBarPrefVibrationLevel.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                PhilPad.Settings.putInt(mContext.getContentResolver(), PhilPad.Settings.SETTINGS_KEY_PAD_VIBRATION_LEVEL, (Integer) newValue);
                LocalSettings.mVibrationTime = (Integer) newValue;
                seekBarPrefVibrationLevel.setSummary(LocalSettings.mVibrationTime + "/100");
                return false;
            }
        });

        root.addPreference(seekBarPrefVibrationLevel);

        checkBoxPrefAnimation = new CheckBoxPreference(mContext);
        checkBoxPrefAnimation.setTitle(R.string.pref_title_animation);
        boolean bAnimation = PhilPad.Settings.getBoolean(mContext.getContentResolver(), PhilPad.Settings.SETTINGS_KEY_PAD_ANIMATION_ENABLE, true);
        checkBoxPrefAnimation.setDefaultValue(bAnimation);
        checkBoxPrefAnimation.setSummary(bAnimation == true ? R.string.pref_category_style_summary_animation_enabled : R.string.pref_category_style_summary_animation_disabled);
        checkBoxPrefAnimation.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                PhilPad.Settings.putBoolean(mContext.getContentResolver(), PhilPad.Settings.SETTINGS_KEY_PAD_ANIMATION_ENABLE, (Boolean) newValue);
                if (seekBarPrefAnimationDuratitonLevel != null) {
                    seekBarPrefAnimationDuratitonLevel.setEnabled((Boolean) newValue);
                }
                if ((Boolean) newValue) {
                    checkBoxPrefAnimation.setSummary(R.string.pref_category_style_summary_animation_enabled);
                } else {
                    checkBoxPrefAnimation.setSummary(R.string.pref_category_style_summary_animation_disabled);
                }
                return true;
            }
        });
        root.addPreference(checkBoxPrefAnimation);

        XmlPullParser parser2 = getResources().getXml(R.xml.seekbar_pref_animation_duration_options);
        AttributeSet attributeSet2 = null;

        int state2 = 0;
        do {
            try {
                state2 = parser2.next();
            } catch (XmlPullParserException e1) {
                L.e(e1);
            } catch (IOException e1) {
                L.e(e1);
            }
            if (state2 == XmlPullParser.START_TAG) {
                if (parser2.getName().equals("com.philleeran.flicktoucher.activity.SeekBarPreference")) {
                    attributeSet2 = Xml.asAttributeSet(parser2);
                    break;
                }
            }
        } while (state2 != XmlPullParser.END_DOCUMENT);


        seekBarPrefAnimationDuratitonLevel = new SeekBarPreference(mContext, attributeSet2);
        seekBarPrefAnimationDuratitonLevel.setTitle(R.string.pref_animation_duration_level_title);
        seekBarPrefAnimationDuratitonLevel.setDefaultValue(PhilPad.Settings.getInt(mContext.getContentResolver(), PhilPad.Settings.SETTINGS_KEY_PAD_ANIMATION_DURATION_LEVEL, D.ANIMATION_DURATION));
        seekBarPrefAnimationDuratitonLevel.setSummary(PhilPad.Settings.getInt(mContext.getContentResolver(), PhilPad.Settings.SETTINGS_KEY_PAD_ANIMATION_DURATION_LEVEL, D.ANIMATION_DURATION) + " msec");
        seekBarPrefAnimationDuratitonLevel.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                PhilPad.Settings.putInt(mContext.getContentResolver(), PhilPad.Settings.SETTINGS_KEY_PAD_ANIMATION_DURATION_LEVEL, (Integer) newValue);
                LocalSettings.mAnimationDurationTime = (Integer) newValue;
                seekBarPrefAnimationDuratitonLevel.setSummary(LocalSettings.mAnimationDurationTime + " msec");
                return false;
            }
        });

        seekBarPrefAnimationDuratitonLevel.setEnabled(bAnimation);

        root.addPreference(seekBarPrefAnimationDuratitonLevel);

        checkBoxPrefGesture = new CheckBoxPreference(mContext);
        checkBoxPrefGesture.setTitle(R.string.pref_title_gesture);
        boolean bGesture = PhilPad.Settings.getBoolean(mContext.getContentResolver(), PhilPad.Settings.SETTINGS_KEY_PAD_GESTURE_ENABLE, true);
        checkBoxPrefGesture.setDefaultValue(bGesture);
        checkBoxPrefGesture.setSummary(bGesture == true ? R.string.pref_category_style_summary_gesture_enabled : R.string.pref_category_style_summary_gesture_disabled);
        checkBoxPrefGesture.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                PhilPad.Settings.putBoolean(mContext.getContentResolver(), PhilPad.Settings.SETTINGS_KEY_PAD_GESTURE_ENABLE, (Boolean) newValue);
                if ((Boolean) newValue) {
                    checkBoxPrefGesture.setSummary(R.string.pref_category_style_summary_gesture_enabled);
                } else {
                    checkBoxPrefGesture.setSummary(R.string.pref_category_style_summary_gesture_disabled);
                }
                return true;
            }
        });
        root.addPreference(checkBoxPrefGesture);


        checkBoxPrefInfoView = new CheckBoxPreference(mContext);
        checkBoxPrefInfoView.setTitle(R.string.pref_title_statusinfo);
        boolean bInfoView = PhilPad.Settings.getBoolean(mContext.getContentResolver(), PhilPad.Settings.SETTINGS_KEY_PAD_STATUSINFO_ENABLE, true);
        checkBoxPrefInfoView.setDefaultValue(bInfoView);
        checkBoxPrefInfoView.setSummary(bInfoView == true ? R.string.pref_category_style_summary_statusinfo_enabled : R.string.pref_category_style_summary_statusinfo_disabled);
        checkBoxPrefInfoView.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                PhilPad.Settings.putBoolean(mContext.getContentResolver(), PhilPad.Settings.SETTINGS_KEY_PAD_STATUSINFO_ENABLE, (Boolean) newValue);
                if ((Boolean) newValue) {
                    checkBoxPrefInfoView.setSummary(R.string.pref_category_style_summary_statusinfo_enabled);
                } else {
                    checkBoxPrefInfoView.setSummary(R.string.pref_category_style_summary_statusinfo_disabled);
                }
                return true;
            }
        });
        root.addPreference(checkBoxPrefInfoView);

        checkBoxPrefShowTitle = new CheckBoxPreference(mContext);
        checkBoxPrefShowTitle.setTitle(R.string.pref_title_show_title);
        bInfoView = PhilPad.Settings.getBoolean(mContext.getContentResolver(), PhilPad.Settings.SETTINGS_KEY_PAD_SHOW_NAME_ENABLE, true);
        checkBoxPrefShowTitle.setDefaultValue(bInfoView);
        checkBoxPrefShowTitle.setSummary(bInfoView == true ? R.string.pref_category_style_summary_show_title_enabled : R.string.pref_category_style_summary_show_title_disabled);
        checkBoxPrefShowTitle.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                PhilPad.Settings.putBoolean(mContext.getContentResolver(), PhilPad.Settings.SETTINGS_KEY_PAD_SHOW_NAME_ENABLE, (Boolean) newValue);
                if ((Boolean) newValue) {
                    LocalSettings.mShowTitle = true;
                    checkBoxPrefShowTitle.setSummary(R.string.pref_category_style_summary_show_title_enabled);
                } else {
                    LocalSettings.mShowTitle = false;
                    checkBoxPrefShowTitle.setSummary(R.string.pref_category_style_summary_show_title_disabled);
                }
                return true;
            }
        });
        root.addPreference(checkBoxPrefShowTitle);


        inlinePrefCat = new PreferenceCategory(mContext);
        inlinePrefCat.setTitle(R.string.pref_category_translations);
        root.addPreference(inlinePrefCat);

        intentPrefTranslateHelp = getPreferenceManager().createPreferenceScreen(mContext);
        intentPrefTranslateHelp.setTitle(R.string.pref_title_translate_help_quickswipe);
        intentPrefTranslateHelp.setSummary(R.string.pref_title_translate_help_quickswipe_summary);

        Intent trEmailIntent = new Intent(Intent.ACTION_SEND);
        trEmailIntent.setType("message/rfc822");
        trEmailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{
                "philleeran@gmail.com"
        });
        trEmailIntent.putExtra(Intent.EXTRA_SUBJECT, mContext.getPackageName() + " Help Translate");
        trEmailIntent.putExtra(Intent.EXTRA_TEXT, PadUtils.getDeviceInfo(mContext));
        intentPrefTranslateHelp.setIntent(trEmailIntent);
        root.addPreference(intentPrefTranslateHelp);

        intentPrefTranslateThanks = getPreferenceManager().createPreferenceScreen(mContext);
        intentPrefTranslateThanks.setTitle(R.string.pref_title_translate_thanks);
        intentPrefTranslateThanks.setSummary(R.string.pref_title_translate_thanks_summary);
        intentPrefTranslateThanks.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setMessage(R.string.dialog_translations_thanks_to_message).setCancelable(true).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).create();
                AlertDialog alert = builder.create();
                alert.show();
                return true;
            }
        });

        root.addPreference(intentPrefTranslateThanks);

        inlinePrefCat = new PreferenceCategory(mContext);
        inlinePrefCat.setTitle(R.string.pref_category_etc);
        root.addPreference(inlinePrefCat);

        intentPrefFacebookLike = getPreferenceManager().createPreferenceScreen(mContext);
        intentPrefFacebookLike.setTitle(R.string.pref_title_like_facebook);
        intentPrefFacebookLike.setIntent(PadUtils.getOpenFacebookIntent(mContext));
        intentPrefFacebookLike.setSummary(R.string.pref_title_like_facebook_summary);
        root.addPreference(intentPrefFacebookLike);


        intentPrefRate = getPreferenceManager().createPreferenceScreen(mContext);
        intentPrefRate.setTitle(R.string.pref_title_rate);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("market://details?id=" + mContext.getPackageName()));
        intentPrefRate.setIntent(intent);
        intentPrefRate.setSummary(R.string.pref_title_rate_summary);
        root.addPreference(intentPrefRate);


        intentPrefFeedback = getPreferenceManager().createPreferenceScreen(mContext);
        intentPrefFeedback.setTitle(R.string.pref_title_feedback);
        intentPrefFeedback.setSummary(R.string.pref_title_feedback_summary);
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("message/rfc822");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{
                "philleeran@gmail.com"
        });


        emailIntent.putExtra(Intent.EXTRA_SUBJECT, mContext.getPackageName() + " Feedback");
        emailIntent.putExtra(Intent.EXTRA_TEXT, PadUtils.getDeviceInfo(mContext));
        intentPrefFeedback.setIntent(emailIntent);
        root.addPreference(intentPrefFeedback);

        intentPrefShare = getPreferenceManager().createPreferenceScreen(mContext);
        intentPrefShare.setTitle(R.string.pref_title_share);
        intentPrefShare.setSummary(R.string.pref_title_share_summary);

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
        StringBuilder sAux = new StringBuilder();
        sAux.append(getString(R.string.pref_title_share_message));
        sAux.append("https://play.google.com/store/apps/details?id=com.philleeran.flicktoucher \n\n");
        shareIntent.putExtra(Intent.EXTRA_TEXT, sAux.toString());
        intentPrefShare.setIntent(Intent.createChooser(shareIntent, getString(R.string.pref_title_share_chooseone)));
        root.addPreference(intentPrefShare);


/*
        intentPrefTutorial = getPreferenceManager().createPreferenceScreen(mContext);
        intentPrefTutorial.setTitle(R.string.pref_title_tutorial);
        intentPrefTutorial.setIntent(new Intent(mContext, Tutorial2Activity.class));
        root.addPreference(intentPrefTutorial);
*/

        intentPrefVersion = getPreferenceManager().createPreferenceScreen(mContext);
        intentPrefVersion.setTitle(R.string.pref_version_title);
        intentPrefVersion.setSummary(getVersion());
        intentPrefVersion.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if (PhilPad.Settings.getBoolean(mContext.getContentResolver(), PhilPad.Settings.SETTINGS_KEY_IS_PREMIUM, false)) {
                    Intent intent = new Intent();
                    ComponentName componentName = new ComponentName(mContext, SettingsDeveloperOptionsActivity.class);
                    intent.setComponent(componentName);
                    startActivity(intent);
                    return true;
                } else {
                    if (mCount++ <= 10) {
                        return false;
                    } else {
                        mCount = 0;
                        if (PhilPad.Settings.getInt(mContext.getContentResolver(), PhilPad.Settings.SETTINGS_KEY_BACKGROUND_COLOR, 0xFFFFFFFF) == 0xFF000000
                                && PhilPad.Settings.getInt(mContext.getContentResolver(), PhilPad.Settings.SETTINGS_KEY_PAD_BACKGROUND_COLOR, 0xFFFFFFFF) == 0xFF000000) {
                            showPasswordDialog(mContext);
                        }
                    }
                    return false;
                }
            }
        });
        root.addPreference(intentPrefVersion);
        return root;
    }

    private String getVersion() {
        StringBuilder sb = new StringBuilder();
        sb.append(getAppVersionName());
        sb.append(" / build(");
        sb.append(getAppVersionCode());
        sb.append(")");
        return sb.toString();
    }

    private String getAppVersionName() {
        try {
            return mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), PackageManager.GET_UNINSTALLED_PACKAGES).versionName.toString();
        } catch (PackageManager.NameNotFoundException e) {
            return "unknown";
        }
    }

    private String getAppVersionCode() {
        try {
            return String.valueOf(mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), PackageManager.GET_UNINSTALLED_PACKAGES).versionCode);
        } catch (PackageManager.NameNotFoundException e) {
            return "unknown";
        }
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
                        String password = editTextPassword.getText().toString();
                        if (password.equals(D.SETTINGS_PASSWORD)) {
                            PhilPad.Settings.putBoolean(mContext.getContentResolver(), PhilPad.Settings.SETTINGS_KEY_IS_PREMIUM, true);
                            PadUtils.ToastShort(mContext, R.string.toast_premium);
                            L.d("showPasswordDialog true");
                        } else {
                            PhilPad.Settings.putBoolean(mContext.getContentResolver(), PhilPad.Settings.SETTINGS_KEY_DEVELOPER_MODE, false);
                            L.d("showPasswordDialog false");
                        }
                    }
                }).setNegativeButton(android.R.string.cancel, null).create();
        mDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(editTextPassword, InputMethodManager.SHOW_IMPLICIT);
            }
        });
        mDialog.show();
        mDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
        editTextPassword.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() < 8) {
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
                } else {
                    if (isChecked == false) {
                        PasswordTransformationMethod passwdtm = new PasswordTransformationMethod();
                        editTextPassword.setTransformationMethod(passwdtm);
                        editTextPassword.setSelection(editTextPassword.length());
                    }
                }
            }
        });
    }
}
