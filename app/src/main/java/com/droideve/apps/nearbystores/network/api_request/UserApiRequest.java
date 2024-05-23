package com.droideve.apps.nearbystores.network.api_request;
import com.android.volley.Response;

public class UserApiRequest extends SimpleRequest {
    public UserApiRequest(int method, String url, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        super(method, url, listener, errorListener);
    }
}
