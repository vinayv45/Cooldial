package com.droideve.apps.nearbystores.activities;

import static com.droideve.apps.nearbystores.activities.MainActivity.REQUEST_CHECK_SETTINGS;
import static com.droideve.apps.nearbystores.security.Security.ANDROID_API_KEY;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

import com.droideve.apps.nearbystores.network.api_request.ApiRequest;
import com.droideve.apps.nearbystores.network.api_request.ApiRequestListeners;
import com.droideve.apps.nearbystores.utils.NSLog;

import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.droideve.apps.nearbystores.AppController;
import com.droideve.apps.nearbystores.location.GPStracker;
import com.droideve.apps.nearbystores.R;
import com.droideve.apps.nearbystores.animation.Animation;
import com.droideve.apps.nearbystores.appconfig.AppConfig;
import com.droideve.apps.nearbystores.appconfig.Constances;
import com.droideve.apps.nearbystores.controllers.categories.CategoryController;
import com.droideve.apps.nearbystores.controllers.events.EventController;
import com.droideve.apps.nearbystores.controllers.stores.OffersController;
import com.droideve.apps.nearbystores.controllers.stores.StoreController;
import com.droideve.apps.nearbystores.dtmessenger.TokenInstance;
import com.droideve.apps.nearbystores.load_manager.ViewManager;
import com.droideve.apps.nearbystores.network.ServiceHandler;
import com.droideve.apps.nearbystores.parser.Parser;
import com.droideve.apps.nearbystores.parser.tags.Tags;
import com.droideve.apps.nearbystores.push_notification_firebase.FirebaseInstanceIDService;
import com.droideve.apps.nearbystores.utils.CommunApiCalls;
import com.droideve.apps.nearbystores.utils.MessageDialog;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.Map;

public class SplashActivity extends AppCompatActivity implements ViewManager.CustomView, View.OnClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    final String TAG = SplashActivity.class.getName();

    //location
    private FusedLocationProviderClient fusedLocationClient;

    //view managr
    public ViewManager mViewManager;
    //init request http
    private boolean firstAppLaunch = false;
    private ProgressBar progressBar;

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);


        progressBar = findViewById(R.id.progressBar);


        AppController.getInstance().updateAndroidSecurityProvider(this);

        //refresh guest id
        FirebaseInstanceIDService.regenerate();

        //background zoom effect
        ImageView splashImage = findViewById(R.id.splashImage);
        Animation.startZoomEffect(splashImage);


        //refresh data when network is available
        if (ServiceHandler.isNetworkAvailable(this)) {
            OffersController.removeAll();
            StoreController.removeAll();
            EventController.removeAll();
            CategoryController.removeAll();
        }

        //setup view manager
        mViewManager = new ViewManager(this);
        mViewManager.setLoadingView(findViewById(R.id.loading));
        mViewManager.setErrorView(findViewById(R.id.error));
        mViewManager.setEmptyView(findViewById(R.id.empty));
        mViewManager.setCustumizeView(this);

        //request location
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        //sync app settings
        CommunApiCalls.appSettings();
    }

    @Override
    protected void onStart() {
        super.onStart();

        boolean loaded = false, requiredGpsON = false;

        //sync available modules
        CommunApiCalls.availableModulesAPI(this);

        //load languages
        CommunApiCalls.loadLanguages();

        try {
            loaded = getIntent().getExtras().getBoolean("loaded");
            requiredGpsON = getIntent().getExtras().getBoolean("requiredGpsON");
        } catch (Exception e) {

        }


        //check gps app permission
        //Apply permission for all devices (Version > 5)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && this.checkSelfPermission(
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && this.checkSelfPermission(
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION},
                    101);
        } else {

            if (!AppController.isTokenFound()) {
                firstAppLaunch = true;
                mViewManager.showLoading();
                appInit();
            } else {
                firstAppLaunch = false;
                // re check permission for app
                if (requiredGpsON) {
                    settingAppLocation(firstAppLaunch);
                } else if (loaded == false) {
                    mViewManager.showLoading();
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            settingAppLocation(firstAppLaunch);
                        }
                    }, 1500);
                } else {
                    settingAppLocation(firstAppLaunch);
                }
            }
        }


    }


    @Override
    public void customErrorView(View v) {

    }

    @Override
    public void customLoadingView(View v) {

    }

    @Override
    public void customEmptyView(View v) {

    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.connect) {
            startActivity(new Intent(SplashActivity.this, LoginV2Activity.class));
            finish();
        }

    }

    private void startMain() {

        Intent intent = new Intent(SplashActivity.this, IntroSliderActivity.class);

        try {
            intent.putExtra("chat", getIntent().getExtras().getBoolean("chat"));
        } catch (Exception e) {
            if (AppConfig.APP_DEBUG) e.printStackTrace();
        }

        startActivity(intent);
        finish();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }


    private void appInit() {
        final String device_token = TokenInstance.getTokenID(this);
        final String mac_address = ServiceHandler.getMacAddr();

        ApiRequest.newPostInstance(Constances.API.API_APP_INIT, new ApiRequestListeners() {
            @Override
            public void onSuccess(Parser parser) {
                app_init_parser(parser);
            }

            @Override
            public void onFail(Map<String, String> errors) {
                //show message showError
                MessageDialog.newDialog(SplashActivity.this).onCancelClick(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MessageDialog.getInstance().hide();
                        finish();
                    }
                }).onOkClick(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        appInit();
                        MessageDialog.getInstance().hide();
                    }
                }).setContent("Error with initialization!").show();

            }
        }, Map.of(
                "device_token", device_token,
                "mac_address", mac_address,
                "mac_adr", mac_address,
                "crypto_key", ANDROID_API_KEY
        ));

    }

    private void app_init_parser(Parser mParser){

        final String device_token = TokenInstance.getTokenID(this);
        final String mac_address = ServiceHandler.getMacAddr();

        int success = Integer.parseInt(mParser.getStringAttr(Tags.SUCCESS));
        if (success == 1) {

            //get app token
            final String token = mParser.getStringAttr("token");

            //save app token
            AppController.setTokens(mac_address, device_token, token);

            //start next page
            startActivity(new Intent(SplashActivity.this, IntroSliderActivity.class));
            finish();

        } else {

            //show message showError
            MessageDialog.newDialog(SplashActivity.this).onCancelClick(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MessageDialog.getInstance().hide();
                    finish();
                }
            }).onOkClick(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    appInit();
                    MessageDialog.getInstance().hide();
                }
            }).setContent("Token isn't valid!").show();

        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 101:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    GPStracker gps = new GPStracker(this);
                    settingAppLocation(firstAppLaunch);
                } else {
                    settingAppLocation(firstAppLaunch);
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public void settingAppLocation(final boolean initApp) {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    // Logic to handle location object
                    NSLog.e(TAG, "Lat:" + location.getLatitude() + "-Lng:" + location.getLongitude());
                }
            }
        });


        GoogleApiClient mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).build();
        mGoogleApiClient.connect();

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(30 * 1000);
        locationRequest.setFastestInterval(5 * 1000);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true); //this is the key ingredient

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                final LocationSettingsStates state = result.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        if (initApp)
                            appInit();
                        else
                            startMain();
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied. But could be fixed by showing the user
                        // a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(SplashActivity.this, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the showError.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way to fix the
                        // settings so we won't show the dialog.
                        break;
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        GPStracker gps = new GPStracker(getApplicationContext());
                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                GPStracker gps = new GPStracker(getApplicationContext());
                                gps.getLongitude();
                                gps.getLatitude();

                                progressBar.setVisibility(View.GONE);

                                if (firstAppLaunch) appInit();
                                else startMain();
                            }
                        }, 3500);

                        break;
                    case Activity.RESULT_CANCELED:
                        settingAppLocation(firstAppLaunch);//keep asking if imp or do whatever
                        break;
                }
                break;
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
