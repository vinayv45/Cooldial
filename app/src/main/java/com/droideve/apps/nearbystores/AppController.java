package com.droideve.apps.nearbystores;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;

import com.droideve.apps.nearbystores.utils.LocaleHelper;
import com.droideve.apps.nearbystores.utils.NSLog;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;
import androidx.multidex.BuildConfig;
import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.droideve.apps.nearbystores.appconfig.AppConfig;
import com.droideve.apps.nearbystores.appconfig.Constances;
import com.droideve.apps.nearbystores.helper.MyPreferenceManager;
import com.droideve.apps.nearbystores.network.ServiceHandler;
import com.droideve.apps.nearbystores.security.Security;
//import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.security.ProviderInstaller;
import com.google.android.libraries.places.api.Places;
import com.google.firebase.FirebaseApp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by Droideve on 6/2/2016.
 */
public class AppController extends MultiDexApplication {

    public static final String TAG = AppController.class
            .getSimpleName();
    private static String fcmToken = "";
    private static HashMap<String, String> tokens = null;
    private static ArrayList<String> listLangsIndex = null;
    private static AppController mInstance;
    private MyPreferenceManager pref;
    private Tracker mTracker;
    private RequestQueue mRequestQueue;

    public synchronized static boolean isTokenFound() {
        SharedPreferences sharedPref = getInstance().getSharedPreferences("tokens", Context.MODE_PRIVATE);
        String token = sharedPref.getString("token-0", "");

        return !token.equals("");
    }

    public synchronized static HashMap<String, String> getTokens() {

        tokens = new HashMap<>();
        SharedPreferences sharedPref = getInstance().getSharedPreferences("tokens", Context.MODE_PRIVATE);

        tokens.put("apiKey", "00-1");
        tokens.put("macadr", sharedPref.getString("macadr", ServiceHandler.getMacAddr()));
        tokens.put("token-0", sharedPref.getString("token-0", ""));
        tokens.put("token-1", sharedPref.getString("token-1", ""));
        tokens.put("ipAddress", "value");

        NSLog.e(TAG, "getTokens");

        return tokens;
    }

    public synchronized static HashMap<String, String> setTokens(String macadr, String token0, String token1) {
        SharedPreferences sharedPref = getInstance().getSharedPreferences("tokens", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("apiKey", "00-1");
        editor.putString("macadr", macadr);
        editor.putString("token-0", token0);
        editor.putString("token-1", token1);
        editor.putString("uid", token1);
        editor.putString("ipAddress", "value");
        editor.commit();

        tokens = new HashMap<>();
        tokens.put("apiKey", "00-1");
        tokens.put("macadr", macadr);
        tokens.put("token-0", token0);
        tokens.put("token-1", token1);
        tokens.put("ipAddress", "value");

        NSLog.e(TAG, "setTokens");

        return tokens;
    }


    /*
        DCMESSENGER Init
     */

    public static synchronized AppController getInstance() {
        return mInstance;
    }

    public static boolean isRTL() {
        return isRTL(Locale.getDefault());
    }

    public static boolean isRTL(Locale locale) {
        final int directionality = Character.getDirectionality(locale.getDisplayName().charAt(0));
        return directionality == Character.DIRECTIONALITY_RIGHT_TO_LEFT ||
                directionality == Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC;
    }

    public MyPreferenceManager getPrefManager() {
        if (pref == null) {
            pref = new MyPreferenceManager(this);
        }
        return pref;
    }

    private void appInit() {
        mInstance = this;
        parseAppConfig();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    /**
     * Gets the default {@link Tracker} for this {@link Application}.
     *
     * @return tracker
     */
    synchronized public Tracker getDefaultTracker() {
        if (mTracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
            mTracker = analytics.newTracker(getString(R.string.analytics));
        }
        return mTracker;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        //initialize app config
        appInit();

        //initialize app security
        Security.init(this);

        //initialize admobs
//        MobileAds.initialize(this);

        //initialize Realm Database
        Realm.init(this);
        Realm.setDefaultConfiguration(getDefaultRealmConfig());

        //initialize Firebase & Chat realtime
        FirebaseApp.initializeApp(this);

        //initialize Google places
        initPlacesAPi(this);


    }


    private static final int REALM_SCHEMA_VERSION = 395;
    public static RealmConfiguration getDefaultRealmConfig() {
        return new RealmConfiguration.Builder()
                .name(BuildConfig.APPLICATION_ID + ".realm")
                .deleteRealmIfMigrationNeeded()
                .schemaVersion(REALM_SCHEMA_VERSION)
                .allowWritesOnUiThread(true)
                .build();
    }

    public static RealmConfiguration getBusinessRealmConfig() {
        return new RealmConfiguration.Builder()
                .name(BuildConfig.APPLICATION_ID + ".realm.business")
                .deleteRealmIfMigrationNeeded()
                .schemaVersion(REALM_SCHEMA_VERSION)
                .allowWritesOnUiThread(true)
                .build();
    }

    private void initPlacesAPi(Context context) {

        String apiKey = getString(R.string.places_api_key);

        /**
         * Initialize Places. For simplicity, the API key is hard-coded. In a production
         * environment we recommend using a secure mechanism to manage API keys.
         */
        if (!Places.isInitialized()) {
            Places.initialize(context, apiKey);
        }

    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        return mRequestQueue;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        System.gc();
        if (AppConfig.APP_DEBUG) {
            NSLog.e("Application", "Memory  cleaned !!");
        }
    }

    private void parseAppConfig() {


        AppConfig.BASE_URL = getResources().getString(R.string.BASE_URL);

        AppConfig.ENABLE_CHAT = Boolean.parseBoolean(getResources().getString(R.string.ENABLE_CHAT));

        AppConfig.RATE_US_FORCE = Boolean.parseBoolean(getResources().getString(R.string.RATE_US_ON_PLAY_STORE_FORCE));
        AppConfig.ANDROID_API_KEY = getResources().getString(R.string.ANDROID_API_KEY);
        AppConfig.ENABLE_INTRO_SLIDER = Boolean.parseBoolean(getResources().getString(R.string.ENABLE_INTRO_SLIDER));
        AppConfig.OFFERS_NUMBER_PER_ROW = Integer.parseInt(getResources().getString(R.string.OFFERS_NUMBER_PER_ROW));

        AppConfig.SHOW_ADS = Boolean.parseBoolean(getResources().getString(R.string.SHOW_ADS));
        AppConfig.SHOW_INTERSTITIAL_AT_FIRST = Boolean.parseBoolean(getResources().getString(R.string.SHOW_INTERSTITIAL_AT_FIRST));

        //chat config
        Constances.BASE_URL = getResources().getString(R.string.BASE_URL);
        Constances.BASE_URL_API = getResources().getString(R.string.BASE_URL_API);
        Constances.PRIVACY_POLICY_URL = getResources().getString(R.string.PRIVACY_POLICY_URL);
        Constances.TERMS_OF_USE_URL = getResources().getString(R.string.TERMS_OF_USE_URL);


        AppConfig.ENABLE_PEOPLE_AROUND_ME = Boolean.parseBoolean(getResources().getString(R.string.ENABLE_PEOPLE_AROUND_ME));
        AppConfig.ENABLE_SOCIAL_MEDIA_AUTH = Boolean.parseBoolean(getResources().getString(R.string.ENABLE_SOCIAL_MEDIA_AUTH));

        AppConfig.FORMAT_24 = Boolean.parseBoolean(getResources().getString(R.string.FORMAT24));


    }

    public void updateAndroidSecurityProvider(Activity callingActivity) {
        try {
            ProviderInstaller.installIfNeeded(this);
        } catch (GooglePlayServicesRepairableException e) {
            // Thrown when Google Play Services is not installed, up-to-date, or enabled
            // Show dialog to allow users to install, update, or otherwise enable Google Play services.
            GooglePlayServicesUtil.getErrorDialog(e.getConnectionStatusCode(), callingActivity, 0);
        } catch (GooglePlayServicesNotAvailableException e) {
            NSLog.e("SecurityException", "Google Play Services not available.");
        }
    }


}
