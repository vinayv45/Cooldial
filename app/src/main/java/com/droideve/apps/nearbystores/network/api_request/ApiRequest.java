package com.droideve.apps.nearbystores.network.api_request;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.droideve.apps.nearbystores.AppController;
import com.droideve.apps.nearbystores.appconfig.AppConfig;
import com.droideve.apps.nearbystores.network.VolleySingleton;
import com.droideve.apps.nearbystores.parser.Parser;
import com.droideve.apps.nearbystores.utils.NSLog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ApiRequest extends SimpleRequest {

    public static void newPostInstance(String url, ApiRequestListeners listeners){
        ApiRequest.newPostInstance(url,listeners,new HashMap<>(),new HashMap<>());
    }

    public static void newPostInstance(String url, ApiRequestListeners listeners, Map<String, String> myParams){
        ApiRequest.newPostInstance(url,listeners,myParams,new HashMap<>());
    }

    public static void newPostInstance(String url, ApiRequestListeners listeners,
                                       Map<String, String> myParams,
                                       Map<String, String> customHeaders){

        if(AppConfig.APP_DEBUG){
            NSLog.e(url,myParams.toString());
        }

        RequestQueue queue  = VolleySingleton.getInstance(AppController.getInstance()).getRequestQueue();

        ApiRequest request = new ApiRequest(Request.Method.POST,
                url, listeners){
            @Override
            protected Map<String, String> getParams() {
                return myParams;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(SimpleRequest.TIME_OUT,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        queue.add(request);

    }

    public static void newGetInstance(String url, ApiRequestListeners listeners){
        ApiRequest.newGetInstance(url,listeners,new HashMap<>());
    }

    public static void newGetInstance(String url, ApiRequestListeners listeners, Map<String, String> myParams){

        if(AppConfig.APP_DEBUG){
            NSLog.e(url,myParams.toString());
        }

        RequestQueue queue  = VolleySingleton.getInstance(AppController.getInstance()).getRequestQueue();
        ApiRequest request = new ApiRequest(Method.GET,
                url, listeners){

            @Override
            protected Map<String, String> getParams() {
                return myParams;
            }

        };

        request.setRetryPolicy(new DefaultRetryPolicy(SimpleRequest.TIME_OUT,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        queue.add(request);

    }

    public ApiRequest(int method, String url, ApiRequestListeners listeners) {

        super(method, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                JSONObject jsonObject = null;
                try {

                    jsonObject = new JSONObject(response);
                    final Parser mParser = new Parser(jsonObject);

                    //listeners
                    if(listeners != null){
                        listeners.onSuccess(mParser);
                    }

                    NSLog.d("JsonResponseParser",response);

                } catch (JSONException e) {
                    e.printStackTrace();
                    if(listeners != null){
                        listeners.onFail(Map.of(
                                "Error", "Json ERROR"
                        ));
                    }

                    NSLog.d("JsonErrorResponseParser",response);
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(listeners != null){
                    listeners.onFail(Map.of(
                            "Error", error.toString()
                    ));
                }
            }
        });
    }

}
