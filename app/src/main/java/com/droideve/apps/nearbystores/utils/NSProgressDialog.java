package com.droideve.apps.nearbystores.utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;

public class NSProgressDialog {

    private static ProgressDialog instance;

    public static ProgressDialog getInstance() {
        return instance;
    }

    public NSProgressDialog(Context mContext) {

        this.mContext = mContext;

        if( instance != null &&  instance.isShowing()){
            instance.dismiss();
        }

        instance = new ProgressDialog(mContext);
        instance.setCancelable(false);
    }

    private Context mContext;
    public static NSProgressDialog newInstance(Context ctx){
        return new NSProgressDialog(ctx);
    }

    public void show(String message){
        if(instance != null){
            instance.setMessage(message);
            instance.show();
        }
    }
}
