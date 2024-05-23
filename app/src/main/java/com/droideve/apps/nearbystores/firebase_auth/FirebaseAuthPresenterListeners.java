package com.droideve.apps.nearbystores.firebase_auth;

public interface FirebaseAuthPresenterListeners {
    void onSuccess(FireAuthResult result);
    void onError(String error);
}
