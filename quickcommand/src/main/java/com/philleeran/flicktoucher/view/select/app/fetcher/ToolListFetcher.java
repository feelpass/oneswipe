package com.philleeran.flicktoucher.view.select.app.fetcher;


import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import com.philleeran.flicktoucher.R;
import com.philleeran.flicktoucher.db.PadItemInfo;
import com.philleeran.flicktoucher.db.PhilPad;
import com.philleeran.flicktoucher.utils.D;

import java.util.ArrayList;
import java.util.List;

public final class ToolListFetcher {

    public static void fetch(final Context context, @NonNull final LauncherItemFetcherCallback callback) {

        new AsyncTask<Void, Void, List<PadItemInfo>>() {

            @Override
            protected List<PadItemInfo> doInBackground(Void... params) {

                List<PadItemInfo> infos = new ArrayList();
                infos.add(new PadItemInfo.Builder().setType(PhilPad.Pads.PAD_TYPE_TOOLS).setTitle(context.getString(R.string.settings_function_type_lastapp)).setPackageName("function" + D.Icon.PadPopupMenuTools.LastApp).setImageFileName("android.resource://com.philleeran.flicktoucher/drawable/ic_undo_24dp_old").setExtraData(String.valueOf(D.Icon.PadPopupMenuTools.LastApp)).build());
                infos.add(new PadItemInfo.Builder().setType(PhilPad.Pads.PAD_TYPE_TOOLS).setTitle(context.getString(R.string.settings_function_type_recentapplication_onpad)).setPackageName("function" + D.Icon.PadPopupMenuTools.RecentApplicationInPad).setImageFileName("android.resource://com.philleeran.flicktoucher/drawable/ic_grid_on_24dp_old").setExtraData(String.valueOf(D.Icon.PadPopupMenuTools.RecentApplicationInPad)).build());
                infos.add(new PadItemInfo.Builder().setType(PhilPad.Pads.PAD_TYPE_TOOLS).setTitle(context.getString(R.string.settings_function_type_context)).setPackageName("function" + D.Icon.PadPopupMenuTools.Context).setImageFileName("android.resource://com.philleeran.flicktoucher/drawable/ic_info_24dp_old").setExtraData(String.valueOf(D.Icon.PadPopupMenuTools.Context)).build());
                infos.add(new PadItemInfo.Builder().setType(PhilPad.Pads.PAD_TYPE_TOOLS).setTitle(context.getString(R.string.settings_function_type_booster)).setPackageName("function" + D.Icon.PadPopupMenuTools.Booster).setImageFileName("android.resource://com.philleeran.flicktoucher/drawable/sweep4_old").setExtraData(String.valueOf(D.Icon.PadPopupMenuTools.Booster)).build());
                infos.add(new PadItemInfo.Builder().setType(PhilPad.Pads.PAD_TYPE_TOOLS).setTitle(context.getString(R.string.settings_function_type_home)).setPackageName("function" + D.Icon.PadPopupMenuTools.Home).setImageFileName("android.resource://com.philleeran.flicktoucher/drawable/ic_home_24dp_old").setExtraData(String.valueOf(D.Icon.PadPopupMenuTools.Home)).build());
                infos.add(new PadItemInfo.Builder().setType(PhilPad.Pads.PAD_TYPE_TOOLS).setTitle(context.getString(R.string.settings_function_type_indicator)).setPackageName("function" + D.Icon.PadPopupMenuTools.Indicator).setImageFileName("android.resource://com.philleeran.flicktoucher/drawable/ic_vertical_align_bottom_24dp_old").setExtraData(String.valueOf(D.Icon.PadPopupMenuTools.Indicator)).build());
                infos.add(new PadItemInfo.Builder().setType(PhilPad.Pads.PAD_TYPE_TOOLS).setTitle(context.getString(R.string.settings_function_type_recentapplication)).setPackageName("function" + D.Icon.PadPopupMenuTools.RecentApplication).setImageFileName("android.resource://com.philleeran.flicktoucher/drawable/ic_filter_none_24dp_old").setExtraData(String.valueOf(D.Icon.PadPopupMenuTools.RecentApplication)).build());
                infos.add(new PadItemInfo.Builder().setType(PhilPad.Pads.PAD_TYPE_TOOLS).setTitle(context.getString(R.string.settings_function_type_close)).setPackageName("function" + D.Icon.PadPopupMenuTools.Close).setImageFileName("android.resource://com.philleeran.flicktoucher/drawable/ic_close_24dp_old").setExtraData(String.valueOf(D.Icon.PadPopupMenuTools.Close)).build());
                infos.add(new PadItemInfo.Builder().setType(PhilPad.Pads.PAD_TYPE_TOOLS).setTitle(context.getString(R.string.settings_function_type_padsettings)).setPackageName("function" + D.Icon.PadPopupMenuTools.PadSettings).setImageFileName("android.resource://com.philleeran.flicktoucher/drawable/ic_settings_24dp_old").setExtraData(String.valueOf(D.Icon.PadPopupMenuTools.PadSettings)).build());
                infos.add(new PadItemInfo.Builder().setType(PhilPad.Pads.PAD_TYPE_TOOLS).setTitle(context.getString(R.string.settings_function_type_trigger_area_detect_disable)).setPackageName("function" + D.Icon.PadPopupMenuTools.HotspotDisable).setImageFileName("android.resource://com.philleeran.flicktoucher/drawable/ic_block_24dp_old").setExtraData(String.valueOf(D.Icon.PadPopupMenuTools.HotspotDisable)).build());
                infos.add(new PadItemInfo.Builder().setType(PhilPad.Pads.PAD_TYPE_TOOLS).setTitle(context.getString(R.string.settings_function_type_flashonoff)).setPackageName("function" + D.Icon.PadPopupMenuTools.FlashOnOff).setImageFileName("android.resource://com.philleeran.flicktoucher/drawable/ic_flash_on_24dp_old").setExtraData(String.valueOf(D.Icon.PadPopupMenuTools.FlashOnOff)).build());
                infos.add(new PadItemInfo.Builder().setType(PhilPad.Pads.PAD_TYPE_TOOLS).setTitle(context.getString(R.string.settings_function_type_wifi)).setPackageName("function" + D.Icon.PadPopupMenuTools.WifiOnOff).setImageFileName("android.resource://com.philleeran.flicktoucher/drawable/ic_signal_wifi_4_bar_24dp_old").setExtraData(String.valueOf(D.Icon.PadPopupMenuTools.WifiOnOff)).build());
                infos.add(new PadItemInfo.Builder().setType(PhilPad.Pads.PAD_TYPE_TOOLS).setTitle(context.getString(R.string.settings_function_type_volume_up)).setPackageName("function" + D.Icon.PadPopupMenuTools.VolumeUp).setImageFileName("android.resource://com.philleeran.flicktoucher/drawable/ic_volume_up_24dp_old").setExtraData(String.valueOf(D.Icon.PadPopupMenuTools.VolumeUp)).build());
                infos.add(new PadItemInfo.Builder().setType(PhilPad.Pads.PAD_TYPE_TOOLS).setTitle(context.getString(R.string.settings_function_type_volume_down)).setPackageName("function" + D.Icon.PadPopupMenuTools.VolumeDown).setImageFileName("android.resource://com.philleeran.flicktoucher/drawable/ic_volume_down_24dp_old").setExtraData(String.valueOf(D.Icon.PadPopupMenuTools.VolumeDown)).build());
                infos.add(new PadItemInfo.Builder().setType(PhilPad.Pads.PAD_TYPE_TOOLS).setTitle(context.getString(R.string.settings_function_type_rotation)).setPackageName("function" + D.Icon.PadPopupMenuTools.RotationOnOff).setImageFileName("android.resource://com.philleeran.flicktoucher/drawable/ic_screen_rotation_24dp_old").setExtraData(String.valueOf(D.Icon.PadPopupMenuTools.RotationOnOff)).build());
                infos.add(new PadItemInfo.Builder().setType(PhilPad.Pads.PAD_TYPE_TOOLS).setTitle(context.getString(R.string.settings_function_type_bluetooth)).setPackageName("function" + D.Icon.PadPopupMenuTools.BluetoothOnOff).setImageFileName("android.resource://com.philleeran.flicktoucher/drawable/ic_bluetooth_24dp_old").setExtraData(String.valueOf(D.Icon.PadPopupMenuTools.BluetoothOnOff)).build());
                infos.add(new PadItemInfo.Builder().setType(PhilPad.Pads.PAD_TYPE_TOOLS).setTitle(context.getString(R.string.settings_function_type_airplane)).setPackageName("function" + D.Icon.PadPopupMenuTools.AirplaneOnOff).setImageFileName("android.resource://com.philleeran.flicktoucher/drawable/ic_airplanemode_on_24dp_old").setExtraData(String.valueOf(D.Icon.PadPopupMenuTools.AirplaneOnOff)).build());
                infos.add(new PadItemInfo.Builder().setType(PhilPad.Pads.PAD_TYPE_TOOLS).setTitle(context.getString(R.string.settings_function_type_backbutton)).setPackageName("function" + D.Icon.PadPopupMenuTools.BackButton).setImageFileName("android.resource://com.philleeran.flicktoucher/drawable/ic_keyboard_backspace_24dp_old").setExtraData(String.valueOf(D.Icon.PadPopupMenuTools.BackButton)).build());
//            PhilPad.Settings.putInt(getActivity().getContentResolver(), PhilPad.Settings.SETTINGS_KEY_LAST_KEY_ID, lastKey + 1);
                return infos;
            }

            @Override
            protected void onPostExecute(List<PadItemInfo> launchItemList) {
                callback.onResult(launchItemList);
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
}
