package com.droideve.apps.nearbystores.appconfig;

import com.droideve.apps.nearbystores.classes.Category;

import java.util.List;
import com.droideve.apps.nearbystores.utils.NSLog;

public class AppConfig {

    //Set the link to the app store account
    public static final String PLAY_STORE_URL = "";
    public static String BASE_URL = "https://cooldial.in/";
    public static List<Category> TabsConfig = null;

    // To verify if the app is build on Debug r Release Mode
    public static boolean APP_DEBUG = true;

    //enable notification sound
    public static boolean NOTIFICATION_SOUND = true;

    //use safe more to encryt sending data (Use with API connection)
    public static boolean SAFE_MODE = false;

    // THIS IS TO CHANGE THE HOME STYLE HOME_V2 OR HOME_V3
    public static String HOME_STYLE = "homeStyle2";

    // Set to true if you want to display ads in all views.
    public static boolean SHOW_ADS = true;
    public static boolean SHOW_INTERSTITIAL_AT_FIRST = true;

    public static boolean ENABLE_CHAT = false;
    public static boolean CHAT_WITH_FIREBASE = true;
    public static String ANDROID_API_KEY = "";

    public static boolean RATE_US_FORCE = true;

    // ENABLE INTRO SLIDER WHEN THE APP START FOR THE FIRST TIME
    public static boolean ENABLE_INTRO_SLIDER = true;

    public static int OFFERS_NUMBER_PER_ROW;
    public static boolean ENABLE_PEOPLE_AROUND_ME = false;
    public static boolean ENABLE_SOCIAL_MEDIA_AUTH = false;

    public static boolean FORMAT_24 = true;
    public static boolean NOTIFICATION_AGREEMENT = false;

    public static class GCMConfig {
        // flag to identify whether to show single line
        // or multi line text in push notification tray
        public static boolean appendNotificationMessages = false;

    }
}
