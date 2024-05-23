package com.droideve.apps.nearbystores.firebase_auth;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.droideve.apps.nearbystores.R;
import com.droideve.apps.nearbystores.utils.NSLog;
import com.droideve.apps.nearbystores.utils.NSToast;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginBehavior;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.BeginSignInResult;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Arrays;


public class FirebaseAuthPresenter implements FirebaseAuthViewListeners {

    static String TAG = "FirebaseAuthPresenter";
    private Context mContext;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuthPresenterListeners listeners;

    private SignInClient oneTapClient;

    private static final int REQ_ONE_TAP = 2;
    private boolean showOneTapUI = true;


    public FirebaseAuthPresenter(Context context){
        this.mContext = context;
        this.firebaseAuth = FirebaseAuth.getInstance();
    }


    public FirebaseAuthPresenterListeners getListeners() {
        return listeners;
    }

    public void setListeners(FirebaseAuthPresenterListeners listeners) {
        this.listeners = listeners;
    }

    @Override
    public void onFireAuthResume() {
        if (firebaseAuth == null)
            return;
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
    }


    @Override
    public void onFireAuthResult(int requestCode, int resultCode, @Nullable Intent data) {

        // Pass the activity result back to the Facebook SDK
        mCallbackManager.onActivityResult(requestCode, resultCode, data);

        if(requestCode != REQ_ONE_TAP){
            return;
        }

        try {
            SignInCredential credential = oneTapClient.getSignInCredentialFromIntent(data);
            String idToken = credential.getGoogleIdToken();
            if (idToken !=  null) {
                // Got an ID token from Google. Use it to authenticate
                // with Firebase.
                NSLog.e("onUIResult","Got ID token.");

                // Got an ID token from Google. Use it to authenticate
                // with Firebase.
                AuthCredential firebaseCredential = GoogleAuthProvider.getCredential(idToken, null);
                firebaseAuth.signInWithCredential(firebaseCredential)
                        .addOnCompleteListener((Activity) mContext, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    NSLog.d(TAG, "signInWithCredential:success");

                                    FirebaseUser user = firebaseAuth.getCurrentUser();
                                    FireAuthResult result = new FireAuthResult();
                                    result.setToken(idToken);
                                    result.setUniqueid(user.getUid());
                                    result.setUsername(user.getUid());
                                    result.setEmail(user.getEmail());
                                    result.setName(user.getDisplayName());
                                    result.setAvatar(user.getPhotoUrl().getPath());
                                    result.setSource("google");
                                    if(listeners != null){
                                        listeners.onSuccess(result);
                                        return;
                                    }

                                } else {
                                    // If sign in fails, display a message to the user.
                                    NSLog.w(TAG, "signInWithCredential:failure", task.getException());
                                    if(listeners != null){
                                        listeners.onError(task.getException().getMessage());
                                        return;
                                    }
                                }
                            }
                        });
            }
        } catch (ApiException e) {
            e.printStackTrace();
            if(listeners != null){
                listeners.onError(e.getMessage());
                return;
            }
        }


    }

    private CallbackManager mCallbackManager;

    @Override
    public void signInWithFacebook() {

        if(firebaseAuth != null){
            firebaseAuth.signOut();
        }

        // Initialize Facebook Login button
        mCallbackManager = CallbackManager.Factory.create();

        LoginManager mLoginManager = LoginManager.getInstance();
        mLoginManager.setLoginBehavior(LoginBehavior.DIALOG_ONLY)
                .logInWithReadPermissions((Activity) mContext, Arrays.asList("email", "public_profile"));
        mLoginManager.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "facebook:onError", error);
            }
        });

    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener((Activity) mContext, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = firebaseAuth.getCurrentUser();

                            FireAuthResult result = new FireAuthResult();
                            result.setToken(token.getToken());
                            result.setUniqueid(user.getUid());
                            result.setUsername(user.getUid());
                            result.setEmail(user.getEmail());
                            result.setName(user.getDisplayName());
                            result.setAvatar(user.getPhotoUrl().getPath());
                            result.setSource("facebook");

                            if(listeners != null){
                                listeners.onSuccess(result);
                                return;
                            }

                        } else {
                            // If sign in fails, display a message to the user.
                            if(listeners != null){
                                listeners.onError(task.getException().getMessage());
                                return;
                            }
                        }
                    }
                });
    }

    @Override
    public void signInWithGoogle() {

        mCallbackManager = CallbackManager.Factory.create();

        if(firebaseAuth != null){
            firebaseAuth.signOut();
        }

        oneTapClient = Identity.getSignInClient(mContext);

        BeginSignInRequest.GoogleIdTokenRequestOptions.Builder builder = BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                .setSupported(true)
                // Your server's client ID, not your Android client ID.
                .setServerClientId(mContext.getString(R.string.default_web_client_id))
                .setFilterByAuthorizedAccounts(false);

        BeginSignInRequest signInRequest = BeginSignInRequest.builder()
                .setGoogleIdTokenRequestOptions(builder.build())
                .build();

        oneTapClient.beginSignIn(signInRequest).addOnSuccessListener(new OnSuccessListener<BeginSignInResult>() {
            @Override
            public void onSuccess(BeginSignInResult beginSignInResult) {
                try {
                    ((Activity)mContext).startIntentSenderForResult(
                            beginSignInResult.getPendingIntent().getIntentSender(), REQ_ONE_TAP,
                            null, 0, 0, 0);
                } catch (IntentSender.SendIntentException e) {
                    NSLog.e(TAG, "Couldn't start One Tap UI: " + e.getLocalizedMessage());
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                NSLog.e("onFailure",e.getMessage());
                NSToast.show(e.getMessage());
            }
        });

    }


    @Override
    public void signOut() {

    }


}


interface FirebaseAuthViewListeners{
    void onFireAuthResume();
    void onFireAuthResult(int requestCode, int resultCode, @Nullable Intent data);
    void signInWithFacebook();
    void signInWithGoogle();
    void signOut();
}

