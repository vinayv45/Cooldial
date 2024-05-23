package com.droideve.apps.nearbystores.utils;

import android.util.Log;
import android.widget.Toast;

import com.droideve.apps.nearbystores.AppController;

public class NSToast {

    public static void show(String msg) {
        Toast.makeText(AppController.getInstance(),msg,Toast.LENGTH_LONG).show();
    }
}
