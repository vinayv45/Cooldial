package com.droideve.apps.nearbystores.network;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.LruCache;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;


public class VolleySingleton {


    private static VolleySingleton sInstance = null;
    private RequestQueue mRequestQueue;

    private VolleySingleton(Context context) {

        mRequestQueue = Volley.newRequestQueue(context);
    }

    public static synchronized VolleySingleton getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new VolleySingleton(context);
        }
        return sInstance;
    }

    public RequestQueue getRequestQueue() {
        return mRequestQueue;
    }

}
