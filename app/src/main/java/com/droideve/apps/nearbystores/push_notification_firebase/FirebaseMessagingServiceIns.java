package com.droideve.apps.nearbystores.push_notification_firebase;

import android.util.Log;
import com.droideve.apps.nearbystores.utils.NSLog;

import com.droideve.apps.nearbystores.appconfig.AppConfig;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;


public class FirebaseMessagingServiceIns extends FirebaseMessagingService {

    public static final String TAG = "FirebaseMessaging";


    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        NSLog.e("NEW_TOKEN",s);
    }


    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        if (AppConfig.APP_DEBUG) {
            NSLog.d(TAG, "From " + remoteMessage.toString());
            NSLog.d(TAG, "From: " + remoteMessage.getFrom());
        }

        Map<String, String> messageFromOwnServer = remoteMessage.getData();
        try {
            showNotification(messageFromOwnServer);
        } catch (Exception e) {
            try {
                //showNotification(remoteMessage.getNotification().getBody());
            } catch (Exception e1) {
                if (AppConfig.APP_DEBUG)
                    e1.printStackTrace();
            }

            if (AppConfig.APP_DEBUG)
                e.printStackTrace();
        }


    }


    private void showNotification(Map<String, String> message) {

        if (AppConfig.APP_DEBUG) {
            NSLog.e(TAG, "InCommingData " + message.toString());
        }

        DTNotificationManager mCampaignNotifManager = new DTNotificationManager(this, message);
        mCampaignNotifManager.push();

    }
}
