package com.droideve.apps.nearbystores.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.droideve.apps.nearbystores.AppController;

public class Session {
    // Shared preferences file name
    private static final String PREF_NAME = "snow-intro-slider";
    private static final String IS_FIRST_TIME_LAUNCH = "IsFirstTimeLaunchApp";
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Context _context;
    // shared pref mode
    int PRIVATE_MODE = 0;

    public Session(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    public boolean isFirstTimeLaunch() {
        return pref.getBoolean(IS_FIRST_TIME_LAUNCH, true);
    }

    public void setFirstTimeLaunch(boolean isFirstTime) {
        editor.putBoolean(IS_FIRST_TIME_LAUNCH, isFirstTime);
        editor.commit();

    }

    public Session saveValue(String key, String value) {
        editor.putString(key,value);
        return this;
    }

    public Session saveValue(String key, Float value) {
        editor.putFloat(key,value);
        return this;
    }

    public Session saveValue(String key, Integer value) {
        editor.putInt(key,value);
        return this;
    }

    public Session saveValue(String key, Boolean value) {
        editor.putBoolean(key,value);
        return this;
    }


    public double getValue(String key, Float _default) {
        try {
            return pref.getFloat(key, _default);
        }catch (Exception e){
            return _default;
        }
    }

    public String getValue(String key,String _default) {
        return pref.getString(key, _default);
    }

    public Boolean getValue(String key,Boolean _default) {
        return pref.getBoolean(key, _default);
    }

    public int getValue(String key,Integer _default) {
        return pref.getInt(key, _default);
    }

    public void commit(){
        editor.commit();
    }

    public static Session getInstance(){
        return new Session(AppController.getInstance());
    }

}
