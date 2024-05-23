package com.droideve.apps.nearbystores.network.api_request;

import com.droideve.apps.nearbystores.parser.Parser;

import java.util.Map;

public interface ApiRequestListeners {
    void onSuccess(Parser parser);
    void onFail(Map<String, String> errors);
}
