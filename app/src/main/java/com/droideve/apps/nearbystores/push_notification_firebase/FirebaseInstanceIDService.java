package com.droideve.apps.nearbystores.push_notification_firebase;

import android.content.Context;

import com.droideve.apps.nearbystores.controllers.sessions.SessionsController;
import com.droideve.apps.nearbystores.network.api_request.ApiRequest;
import com.droideve.apps.nearbystores.network.api_request.ApiRequestListeners;
import com.droideve.apps.nearbystores.parser.Parser;
import com.droideve.apps.nearbystores.utils.NSLog;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.droideve.apps.nearbystores.AppController;
import com.droideve.apps.nearbystores.location.GPStracker;
import com.droideve.apps.nearbystores.appconfig.Constances;
import com.droideve.apps.nearbystores.classes.Guest;
import com.droideve.apps.nearbystores.controllers.sessions.GuestController;
import com.droideve.apps.nearbystores.dtmessenger.TokenInstance;
import com.droideve.apps.nearbystores.network.ServiceHandler;
import com.droideve.apps.nearbystores.network.VolleySingleton;
import com.droideve.apps.nearbystores.network.api_request.SimpleRequest;
import com.droideve.apps.nearbystores.parser.api_parser.GuestParser;
import com.droideve.apps.nearbystores.parser.tags.Tags;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.droideve.apps.nearbystores.appconfig.AppConfig.APP_DEBUG;

import androidx.annotation.NonNull;


public class FirebaseInstanceIDService extends FirebaseMessagingService {


    private static final String TAG = "FirebaseInstanceID";

    public static void regenerate() {

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            updateServer("");
                            NSLog.w(this.getClass().getName(), "Fetching FCM registration token failed", task.getException());
                            return;
                        }

                        // Get new FCM registration token
                        String token = task.getResult();
                        updateServer(token);
                    }
                });

    }

    public static void updateServer(String token){

        RequestQueue queue = VolleySingleton.getInstance(AppController.getInstance()).getRequestQueue();
        SimpleRequest request = new SimpleRequest(Request.Method.POST,
                Constances.API.API_USER_REGISTER_TOKEN, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {

                    if (APP_DEBUG)
                        NSLog.e("registerTokenResponse", response);

                    JSONObject js = new JSONObject(response);

                    GuestParser mGuestParser = new GuestParser(js);
                    int success = Integer.parseInt(mGuestParser.getStringAttr(Tags.SUCCESS));
                    if (success == 1) {
                        final List<Guest> list = mGuestParser.getData();
                        if (list.size() > 0) {
                            GuestController.saveGuest(list.get(0));
                            refreshPositionGuest(list.get(0), AppController.getInstance());
                        }
                    }

                } catch (JSONException e) {
                    if (APP_DEBUG)
                        e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (APP_DEBUG) {
                    NSLog.e("ERROR_Firebase", error.toString());
                }

            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();

                params.put("fcm_id", token);
                params.put("sender_id", TokenInstance.getSenderID());
                params.put("platform", "android");
                params.put("mac_adr", ServiceHandler.getMacAddr());

                if (APP_DEBUG) {
                    NSLog.e(TAG, "TokenToSend" + token);
                    NSLog.e("reloadToken", params.toString());
                }

                return params;
            }

        };


        request.setRetryPolicy(new DefaultRetryPolicy(SimpleRequest.TIME_OUT,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        queue.add(request);

    }

    private static void refreshPositionGuest(final Guest mGuest, final Context context) {


        GPStracker gps = new GPStracker(context);
        if (mGuest == null && !gps.canGetLocation()) {
            return;
        }

        final int guestId = mGuest.getId();
        final double lat = gps.getLatitude();
        final double lng = gps.getLongitude();
        int userId = 0;

        if(SessionsController.isLogged()){
            userId = SessionsController.getSession().getUser().getId();
        }

        ApiRequest.newPostInstance(Constances.API.API_REFRESH_POSITION, new ApiRequestListeners() {
            @Override
            public void onSuccess(Parser parser) {

                if(parser.getSuccess() == 0){
                    FirebaseInstanceIDService.regenerate();
                }

            }

            @Override
            public void onFail(Map<String, String> errors) {

            }
        }, Map.of(
                "guest_id", String.valueOf(guestId),
                "lat", String.valueOf(lat),
                "lng", String.valueOf(lng),
                "userId", String.valueOf(userId),
                "platform", "android"
        ));
    }

    @Override
    public void onNewToken(String s) {
        NSLog.e("NEW_TOKEN", s);
    }

}