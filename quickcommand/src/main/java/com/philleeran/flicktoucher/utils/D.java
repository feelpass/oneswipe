
package com.philleeran.flicktoucher.utils;

import com.philleeran.flicktoucher.R;

public class D {

    final public static boolean RECYCLE = false;

    final public static float GRID_RECT_ROUND_FACTOR = 15.0f;

    final public static int GROUPID_GROUND = 0;

    final public static int GROUPID_RECENT = 1;

    final public static int GROUPID_GESTURE = 2;

    final public static int GROUPID_NOTIFICATION = 3;

    final public static int GROUPID_CALLLOG = 4;

    public static final String FLURRY_API_KEY = "62NPMPTMKB6N3VC2G7R6"; // where YOUR_API_KEY

    public static final int WIDGET_HOST_ID = 1411;

    public static final String SETTINGS_PASSWORD = "1411141114111411";

    public static final String PACKAGE_NAME = "com.philleeran.flicktoucher";

    public static final String PAD_SERVICE_NAME = "com.philleeran.flicktoucher.service.PhilPadService";

    public static final String ACTION_ENABLE_HOTSPOT_DETECT = "android.intent.action.HOTSPOT_ENABLE";

    public static final String ACTION_DISABLE_HOTSPOT_DETECT = "android.intent.action.HOTSPOT_DISABLE";


    public static final String ACTION_RESTART_PERSISTENTSERVICE = "android.intent.action.RESTART_PERSISTENTSERVICE";

    public static final String URL_LINK_TO_YOUTUBE = "https://youtu.be/NPvDOlRSkiY";

    public static final String FILE_PATH_RECENT_APPLICATIONS = "recent_applications_path";

    public static final String FILE_PATH_NOTIFICATION_PAD= "notification_path";

    public static final String DIR_PATH_DEFAULT = "imageDir";

    public static final int PAD_SIZE_5_5 = 5;

    public static final int PAD_SIZE_4_4 = 4;

    public static final int PAD_SIZE_3_3 = 3;

    public static final int PAD_SIZE_2_2 = 2;

    public static final String ACTION_WIFI_STATE_CHANGE = "android android.net.wifi.STATE_CHANGE";
    public static final String ACTION_QUICKCOMMAND_SHOWPADVIEW= "com.philleeran.flicktoucher.intent.action.SHOWPADVIEW";



    public static final int TUTORIAL_STEP_0_TOUCH_AND_DRAG_HOTSPOT = 0;
    public static final int TUTORIAL_STEP_1_LONG_TOUCH_EMPTY_AND_ADD_DYNAMIC_ITEM = 1;
    public static final int TUTORIAL_STEP_2_TOUCH_DYNAMIC_ITEM = 2;
    public static final int TUTORIAL_STEP_3_TOUCH_EMPTY_ITEM = 3;
    public static final int TUTORIAL_STEP_4_LONG_TOUCH_EMPTY_AND_ADD_APPLICATION = 4;
    public static final int TUTORIAL_STEP_5_TOUCH_TOOL_ITEM = 5;
    public static final int TUTORIAL_STEP_6_TOUCH_EMPTY = 6;
    public static final int ONLONGCLICK_FROM_HOVER = 100;



    //analysys

    public static final int DIMENSION_ENABLE_MODE = 1;

    public static int REQUEST_TYPE_NORMAL = 0;
    public static int REQUEST_TYPE_TUTORIAL = 1;

    public static class Admob {
        public static final String AD_UNIT_ID_SETTINGACTIVITY = "ca-app-pub-4508576092107998/3235556265";

        public static final String AD_UNIT_ID_APPLISTACTIVITY = "ca-app-pub-4508576092107998/5362934266";

        public static final String AD_UNIT_ID_PADBOARD = "ca-app-pub-4508576092107998/6839667468";

        public static final String AD_DEVELOPER_DEVICE_ID = "BBB7664E5004AA8371792E8C8CFCA17B";

    }

    public static class Board {
        public static final int ICON_SIZE = 64;
//        public static final int ICON_INNER_SIZE = 54;
//        public static final int ICON_MARGIN_SIZE = 5;
        public static final int ICON_INNER_SIZE = 48;
        public static final int ICON_MARGIN_SIZE = 8;
    }
    public static final int ICON_MAX_COUNT = 25;

    public static final int ANIMATION_DURATION = 250;
    public static final float TIRIGGER_DEFAULT_ALPHA = 0.5f;
    public static class Gesture {
        public static final int GESTURE_TYPE_BACK = 0;

        public static final int GESTURE_TYPE_CLOSE = 1;

        public static final int GESTURE_TYPE_LAUNCH = 2;

        public static final int GESTURE_TYPE_KILLAPPLICATION = 3;

        public static final int GESTURE_TYPE_CONTEXT = 4;

        public static final int GESTURE_TYPE_RECENTAPPLICATION = 5;

        public static final int GESTURE_TYPE_HOME = 6;

        public static final int GESTURE_TYPE_INDICATOR = 7;

        public static final int GESTURE_TYPE_APPINFO = 8;

        public static final int GESTURE_TYPE_PADSETTINGS = 9;

        public static final int GESTURE_TYPE_HOTSPOT_DETECT_DISABLE = 10;

        public final static int[] mGestureTypeString = {
                R.string.settings_gesture_type_back,// R.string.settings_gesture_type_close,
                                                    // R.string.settings_gesture_type_launch,
                                                    // R.string.settings_gesture_type_killprocess,
                R.string.settings_gesture_type_context, R.string.settings_gesture_type_recentapplication, R.string.settings_gesture_type_home, R.string.settings_gesture_type_indicator,
                R.string.settings_gesture_type_appinfo, R.string.settings_gesture_type_padsettings,
        };

        public final static String[] mGestureTypeValue = {
                String.valueOf(GESTURE_TYPE_BACK), String.valueOf(GESTURE_TYPE_CLOSE), String.valueOf(GESTURE_TYPE_LAUNCH), String.valueOf(GESTURE_TYPE_KILLAPPLICATION),
                String.valueOf(GESTURE_TYPE_CONTEXT), String.valueOf(GESTURE_TYPE_RECENTAPPLICATION), String.valueOf(GESTURE_TYPE_HOME), String.valueOf(GESTURE_TYPE_INDICATOR),
                String.valueOf(GESTURE_TYPE_APPINFO), String.valueOf(GESTURE_TYPE_PADSETTINGS)
        };

        public static class Command {
            final static public int GESTURE_NONE = -1;

            final static public int GESTURE_UP = 0;

            final static public int GESTURE_DOWN = 1;

            final static public int GESTURE_LEFT = 2;

            final static public int GESTURE_RIGHT = 3;

            final static public int GESTURE_UP_LEFT = 4;

            final static public int GESTURE_UP_RIGHT = 5;

            final static public int GESTURE_DOWN_LEFT = 6;

            final static public int GESTURE_DOWN_RIGHT = 7;

            final static public int GESTURE_LEFT_UP = 8;

            final static public int GESTURE_LEFT_DOWN = 9;

            final static public int GESTURE_RIGHT_UP = 10;

            final static public int GESTURE_RIGHT_DOWN = 11;
        }

    }

    public static class Tools {


        public static final int TOOLS_TYPE_HOME = 0;

        public static final int TOOLS_TYPE_INDICATOR = 1;

        public static final int TOOLS_TYPE_RECENTAPPLICATION = 2;

        public static final int TOOLS_TYPE_CONTEXT = 3;

        public static final int TOOLS_TYPE_CLOSE = 4;

        public static final int TOOLS_TYPE_PADSETTINGS = 5;

        public static final int TOOLS_TYPE_HOTSPOT_DETECT_DISABLE = 6;

        @Deprecated
        public static final int TOOLS_TYPE_ALL_APPLICATIONS = 7;

        public static final int TOOLS_TYPE_RECENTAPPLICATION_INPAD = 8;

        public static final int TOOLS_TYPE_LASTAPP = 9;

        public static final int TOOLS_TYPE_FLASH_ON_OFF = 10;

        public static final int TOOLS_TYPE_WIFI_ON_OFF = 11;

        public static final int TOOLS_TYPE_VOLUME_UP = 12;

        public static final int TOOLS_TYPE_VOLUME_DOWN = 13;

        public static final int TOOLS_TYPE_ROTATION_ON_OFF = 14;

        public static final int TOOLS_TYPE_BLUETOOTH_ON_OFF = 15;

        public static final int TOOLS_TYPE_BACK_BUTTON = 16;

        public static final int TOOLS_TYPE_MEDIA_PLAY = 17;

        public static final int TOOLS_TYPE_MEDIA_PAUSE = 18;

        public static final int TOOLS_TYPE_MEDIA_PREV = 19;

        public static final int TOOLS_TYPE_MEDIA_NEXT = 20;

        public static final int TOOLS_TYPE_MEDIA_STOP = 21;

        public static final int TOOLS_TYPE_AIRPLANE_ON_OFF = 22;

        public static final int TOOLS_TYPE_NOFITICATIONPAD = 23;

        public static final int TOOLS_TYPE_KILL_BACKGROUND_APP = 24;


        /*
         * public final static String[] mFunctionTypeValue = {
         * String.valueOf(FUNCTION_TYPE_HOME),//
         * String.valueOf(FUNCTION_TYPE_INDICATOR),//
         * String.valueOf(FUNCTION_TYPE_RECENTAPPLICATION),//
         * String.valueOf(FUNCTION_TYPE_CONTEXT),//
         * String.valueOf(FUNCTION_TYPE_CLOSE),//
         * String.valueOf(FUNCTION_TYPE_PADSETTINGS), //
         * String.valueOf(FUNCTION_TYPE_GPS),//
         * String.valueOf(FUNCTION_TYPE_RINGMODE),//
         * String.valueOf(FUNCTION_TYPE_AIRPLANE),//
         * String.valueOf(FUNCTION_TYPE_DISPLAY_BRIGHT),//
         * String.valueOf(FUNCTION_TYPE_LOTATION),//
         * String.valueOf(FUNCTION_TYPE_BLUETOOTH),//
         * String.valueOf(FUNCTION_TYPE_BATTERY),//
         * String.valueOf(FUNCTION_TYPE_CLEARMEMORY),//
         * String.valueOf(FUNCTION_TYPE_MENUBUTTON),//
         * String.valueOf(FUNCTION_TYPE_BACKBUTTON),//
         * String.valueOf(FUNCTION_TYPE_FLASH),//
         * String.valueOf(FUNCTION_TYPE_AUTOSYNC),//
         * String.valueOf(FUNCTION_TYPE_DISPLAYSLEEP),//
         * String.valueOf(FUNCTION_TYPE_LOCKENABLE), };
         */
    }

    public static class DynamicPadOption{
        public final static int DYNAMIC_PAD_OPTION_NONE = 0;
        public final static int DYNAMIC_PAD_OPTION_PENWINDOW = 1;
        public final static int DYNAMIC_PAD_OPTION_FORCE_CLOSE = 2;
    }
    public static class InteractionOption{
        public final static int INTERACTION_OPTION_SWIPE_AND_TOUCH = 0;
        public final static int INTERACTION_OPTION_SWIPE_AND_UP = 1;
    }



    public static class Icon {

        public final static int ITEM_SELECTED_FOLDER = 0;

        public final static int ITEM_SELECTED_APPLICATIONS = 10;

        public final static int ITEM_SELECTED_APPLICATION = 1;

        public final static int ITEM_SELECTED_CURRENTAPP = 2;

        public final static int ITEM_SELECTED_SHORTCUT = 3;

        public final static int ITEM_SELECTED_WIDGET = 4;

        public final static int ITEM_SELECTED_TOOLS = 5;

        public final static int ITEM_SELECTED_SETTINGS = 6;

        public final static int ITEM_SELECTED_QUICKCALL = 7;

        public final static int ITEM_SELECTED_FILEOPEN = 8;

        public final static int ITEM_SELECTED_SECRET_FOLDER = 9;


        public static class PadPopupMenu {
            public final static int Add = 0;

            public final static int Move = 1;

            public final static int Delete = 2;

            public final static int PenWindow = 3;

            public final static int PenWindowForLastApp = 4;

            public final static int Rename = 5;

        }

        public static class PadPopupMenuFileOpen {
            public final static int Text = 0;

            public final static int Image = 1;

            public final static int Audio = 2;

            public final static int Video = 3;
        }

        public static class PadPopupMenuTools {
            public final static int Home = Tools.TOOLS_TYPE_HOME;

            public final static int Indicator = Tools.TOOLS_TYPE_INDICATOR;

            public final static int RecentApplication = Tools.TOOLS_TYPE_RECENTAPPLICATION;

            public final static int Context = Tools.TOOLS_TYPE_CONTEXT;

            public final static int Close = Tools.TOOLS_TYPE_CLOSE;

            public final static int PadSettings = Tools.TOOLS_TYPE_PADSETTINGS;

            public final static int HotspotDisable = Tools.TOOLS_TYPE_HOTSPOT_DETECT_DISABLE;

            public final static int RecentApplicationInPad = Tools.TOOLS_TYPE_RECENTAPPLICATION_INPAD;

            public final static int LastApp = Tools.TOOLS_TYPE_LASTAPP;

            public final static int FlashOnOff = Tools.TOOLS_TYPE_FLASH_ON_OFF;

            public final static int WifiOnOff = Tools.TOOLS_TYPE_WIFI_ON_OFF;

            public final static int VolumeUp = Tools.TOOLS_TYPE_VOLUME_UP;

            public final static int VolumeDown = Tools.TOOLS_TYPE_VOLUME_DOWN;

            public final static int RotationOnOff = Tools.TOOLS_TYPE_ROTATION_ON_OFF;

            public final static int BluetoothOnOff = Tools.TOOLS_TYPE_BLUETOOTH_ON_OFF;

            public final static int AirplaneOnOff = Tools.TOOLS_TYPE_AIRPLANE_ON_OFF;

            public final static int BackButton = Tools.TOOLS_TYPE_BACK_BUTTON;

            public final static int MediaPlay = Tools.TOOLS_TYPE_MEDIA_PLAY;

            public final static int MediaPause = Tools.TOOLS_TYPE_MEDIA_PAUSE;

            public final static int MediaPrev = Tools.TOOLS_TYPE_MEDIA_PREV;

            public final static int MediaNext = Tools.TOOLS_TYPE_MEDIA_NEXT;

            public final static int MediaStop = Tools.TOOLS_TYPE_MEDIA_STOP;
            public final static int Booster = Tools.TOOLS_TYPE_KILL_BACKGROUND_APP;


        }

        public static class PadPopupMenuGesture {
            public final static int Back = Gesture.GESTURE_TYPE_BACK;

            public final static int Close = Gesture.GESTURE_TYPE_CLOSE;

            public final static int Launch = Gesture.GESTURE_TYPE_LAUNCH;

            public final static int KillApplication = Gesture.GESTURE_TYPE_KILLAPPLICATION;

            public final static int Context = Gesture.GESTURE_TYPE_CONTEXT;

            public final static int RecentApplication = Gesture.GESTURE_TYPE_RECENTAPPLICATION;

            public final static int Home = Gesture.GESTURE_TYPE_HOME;

            public final static int Indicator = Gesture.GESTURE_TYPE_INDICATOR;

            public final static int AppInfo = Gesture.GESTURE_TYPE_APPINFO;

            public final static int PadSettings = Gesture.GESTURE_TYPE_PADSETTINGS;

            public final static int HotspotDisable = Gesture.GESTURE_TYPE_HOTSPOT_DETECT_DISABLE;
        }
    }

    public static class Context {
        public final static int[] mContextIconArray = {
                R.drawable.ic_play_shopping_bag_24dp,//
                R.drawable.ic_info_24dp, //
                R.drawable.ic_delete_24dp, //
                R.drawable.ic_share_24dp
        };

        public final static int[] mContextStringIdArray = {
                R.string.context_list_googleplay, //
                R.string.context_list_applicationinfo,//
                R.string.context_list_delete,//
                R.string.context_list_share,
        };

        public final static int ITEM_SELECTED_GOOGLEPLAY = 0;

        public final static int ITEM_SELECTED_APPLICATIONINFO = 1;

        public final static int ITEM_SELECTED_DELETE = 2;

        public final static int ITEM_SELECTED_SHARE = 3;


    }

}
