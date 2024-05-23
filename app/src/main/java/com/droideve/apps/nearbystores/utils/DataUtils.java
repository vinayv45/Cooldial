package com.droideve.apps.nearbystores.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.droideve.apps.nearbystores.AppController;
import com.droideve.apps.nearbystores.appconfig.AppConfig;

public class DataUtils {

    public static final String TAG_LANGUAGES = "synced_languages";

    public static void saveValue(String key, String value){
        SharedPreferences sharedPref = AppController.getInstance().getSharedPreferences("localDataSession", Context.MODE_PRIVATE);
        sharedPref.edit().putString(key,value).commit();
    }

    public static String getValue(String key){
        SharedPreferences sharedPref = AppController.getInstance().getSharedPreferences("localDataSession", Context.MODE_PRIVATE);
        return sharedPref.getString(key,"");
    }

}
