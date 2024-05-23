package com.droideve.apps.nearbystores.utils;

import android.util.Log;
import com.droideve.apps.nearbystores.utils.NSLog;

import com.droideve.apps.nearbystores.appconfig.AppConfig;

public class NSLog {

    public static void e(String tag, String msg) {
        if (AppConfig.APP_DEBUG)
            Log.e(tag, msg);
    }

    public static void e(String tag, int msg) {
        if (AppConfig.APP_DEBUG)
            Log.e(tag, String.valueOf(msg));
    }

    public static void e(String tag, double msg) {
        if (AppConfig.APP_DEBUG)
            Log.e(tag, String.valueOf(msg));
    }

    public static void i(String tag, String msg) {
        if (AppConfig.APP_DEBUG)
            Log.i(tag, String.valueOf(msg));
    }

    public static void d(String tag, String msg) {
        if (AppConfig.APP_DEBUG)
            Log.d(tag, String.valueOf(msg));
    }

    public static void w(String tag, String msg, Exception exception) {
        if (AppConfig.APP_DEBUG)
            Log.w(tag, String.valueOf(msg),exception);
    }

}