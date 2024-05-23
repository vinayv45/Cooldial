package com.droideve.apps.nearbystores.activities;

import android.Manifest;
import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;

import com.droideve.apps.nearbystores.location.UserSettingLocation;
import com.droideve.apps.nearbystores.booking.controllers.services.GenericNotifyEvent;
import com.droideve.apps.nearbystores.location.LocationSettingPopup;
import com.droideve.apps.nearbystores.network.api_request.ApiRequest;
import com.droideve.apps.nearbystores.network.api_request.ApiRequestListeners;
import com.droideve.apps.nearbystores.parser.Parser;
import com.droideve.apps.nearbystores.utils.NSLog;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentTransaction;

import com.droideve.apps.nearbystores.AppController;
import com.droideve.apps.nearbystores.R;
import com.droideve.apps.nearbystores.Services.BusMessage;
import com.droideve.apps.nearbystores.appconfig.AppConfig;
import com.droideve.apps.nearbystores.appconfig.Constances;
import com.droideve.apps.nearbystores.controllers.SettingsController;
import com.droideve.apps.nearbystores.controllers.categories.CategoryController;
import com.droideve.apps.nearbystores.controllers.sessions.SessionsController;
import com.droideve.apps.nearbystores.controllers.users.UserController;
import com.droideve.apps.nearbystores.dtmessenger.MessengerHelper;
import com.droideve.apps.nearbystores.fragments.V2MainFragment;
import com.droideve.apps.nearbystores.load_manager.ViewManager;
import com.droideve.apps.nearbystores.fragments.NavigationDrawerFragment;
import com.droideve.apps.nearbystores.parser.api_parser.CategoryParser;
import com.droideve.apps.nearbystores.utils.Tools;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import static android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND;
import static android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE;
import static com.droideve.apps.nearbystores.appconfig.AppConfig.APP_DEBUG;

import java.util.Map;


public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, V2MainFragment.Listener {

    //Declare variable
    public static final int REQUEST_CHECK_SETTINGS = 0x1;
    public static final int REQUEST_CHECK_SETTINGS_MAIN = 0x2;
    public static int height = 0;
    public static int width = 0;
    public static Menu mainMenu;
    private static boolean opened = false;
    public ViewManager mViewManager;
    Toolbar toolbar;
    private Tracker mTracker;
    public static boolean isOpend() {
        return opened;
    }

    public static int getScreenWidth() {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    public static boolean isAppInForeground(Context context) {

        ActivityManager.RunningAppProcessInfo appProcessInfo = new ActivityManager.RunningAppProcessInfo();
        ActivityManager.getMyMemoryState(appProcessInfo);
        if (appProcessInfo.importance == IMPORTANCE_FOREGROUND || appProcessInfo.importance == IMPORTANCE_VISIBLE) {
            return true;
        }

        KeyguardManager km = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        // App is foreground, but screen is locked, so show notification
        return km.inKeyguardRestrictedInputMode();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        opened = false;
    }

    private V2MainFragment mV2MainFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_activity);

        //init google analytics
        AppController application = (AppController) getApplication();
        mTracker = application.getDefaultTracker();

        //get measure of the screen
        Display display = getWindowManager().getDefaultDisplay();
        width = getScreenWidth();
        height = display.getHeight();

        //setup view manager
        mViewManager = new ViewManager(this);
        mViewManager.setLoadingView(findViewById(R.id.loading));
        mViewManager.setContentView(findViewById(R.id.content_my_store));
        mViewManager.setErrorView(findViewById(R.id.error));
        mViewManager.setEmptyView(findViewById(R.id.empty));
        mViewManager.showContent();

        //setup toolbar at the top
        setupToolbar();


        //setup main fragment
        mV2MainFragment = new V2MainFragment();

        int size = (getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK);
        if (size == Configuration.SCREENLAYOUT_SIZE_LARGE || size == Configuration.SCREENLAYOUT_SIZE_XLARGE) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            NavigationDrawerFragment fragNDF = new NavigationDrawerFragment();
            FragmentTransaction transactionNDF = getSupportFragmentManager().beginTransaction();
            // Replace whatever is in the fragment_container view with this fragment,
            // and add the transaction to the back stack
            transactionNDF.replace(R.id.nav_container, fragNDF, V2MainFragment.TAG);
            //transaction.addToBackStack(null);
            // Commit the transaction
            transactionNDF.commit();
        }else{
            NavigationDrawerFragment NaDrawerFrag =
                    (NavigationDrawerFragment) getSupportFragmentManager().findFragmentById(R.id.frag_nav_drawer);
            NaDrawerFrag.setUp(
                    R.id.frag_nav_drawer,
                    (DrawerLayout) findViewById(R.id.drawerLayout),
                    toolbar);
        }


        mV2MainFragment = new V2MainFragment();
        mV2MainFragment.setListener(this);
        //get supported fragment
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack
        transaction.replace(R.id.main_container, mV2MainFragment, V2MainFragment.TAG);
        //transaction.addToBackStack(null);

        // Commit the transaction
        transaction.commit();

        //load categories
        getCategories();

        //check user session and validity
        UserController.checkUserConnection(this);

        //check and request notification permission
        requestNotificationPermission();

    }


    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu, menu);
        mainMenu = menu;
        //updateBadge();
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        menu.findItem(R.id.search_icon).setVisible(true);
        menu.findItem(R.id.share_post).setVisible(false);

        return super.onPrepareOptionsMenu(menu);

    }


    //Manage menu item
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.map_action) {
            startActivity(new Intent(this, MapStoresListActivity.class));
        } else if (item.getItemId() == R.id.search_icon) {
            startActivity(new Intent(this, CustomSearchActivity.class));
        } else if (item.getItemId() == R.id.logout_icon) {
            SessionsController.logOut();
            finish();
            startActivity(new Intent(this, SplashActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        opened = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    private LocationSettingPopup mLocationSettingPopup;
    private void showLocationPopup() {

        mLocationSettingPopup = LocationSettingPopup.newInstance(this, launcher,new LocationSettingPopup.LACDListener() {
            @Override
            public void onKeepCurrentLoc() {
                //keep using device GPS instead
                UserSettingLocation.saveLoc();

                //notify all the app components
                EventBus.getDefault().post(new GenericNotifyEvent(LocationSettingPopup.LOCATION_CHANGED));
            }
            @Override
            public void onChangeLoc(LocationSettingPopup.MyLoc loc) {
                /*
                *Use selected location by google places instead GPS
                */
                //create object loc
                UserSettingLocation settingLocation = new UserSettingLocation();
                settingLocation.setLat(loc.getLat());
                settingLocation.setLng(loc.getLng());
                settingLocation.setLoc_name(loc.getName());
                settingLocation.setCurrentLoc(false);

                //save loc in the device database
                UserSettingLocation.saveLoc(settingLocation);

                //notify all the app components
                EventBus.getDefault().post(new GenericNotifyEvent(LocationSettingPopup.LOCATION_CHANGED));
            }
        }).show();

    }

    ActivityResultLauncher<Intent> launcher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if(mLocationSettingPopup != null){
                        mLocationSettingPopup.result(result);
                    }
                }
            });


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(mV2MainFragment!=null){
            mV2MainFragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Subscribe
    public void onMessageEvent(GenericNotifyEvent event) {
        //receive event click for update locaition
        if (event.message != null && event.message.equals(LocationSettingPopup.LOCATION_PICKED)) {
            showLocationPopup();
        }
    }

    @Subscribe
    public void onNewNotifs(BusMessage bus) {
        if (bus.getType() == BusMessage.GET_NBR_NEW_NOTIFS) {
            if (AppConfig.APP_DEBUG)
                if (MessengerHelper.NbrMessagesManager.getNbrTotalMessages() > 0) {
                    Toast.makeText(this, "New message " + MessengerHelper.NbrMessagesManager.getNbrTotalMessages()
                            , Toast.LENGTH_LONG).show();
                }
        }
    }

    private void setupToolbar() {

        toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.app_name));

        Tools.setSystemBarColor(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mTracker.setScreenName("Image~" + MainActivity.class.getName());
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    //Get all categories from server and save them in  the database
    private void getCategories() {
        ApiRequest.newGetInstance(Constances.API.API_USER_GET_CATEGORY, new ApiRequestListeners() {
            @Override
            public void onSuccess(Parser parser) {
                final CategoryParser mCategoryParser = new CategoryParser(parser);
                if (mCategoryParser.getSuccess() == 1) {
                    //update list categories
                    CategoryController.insertCategories(
                            mCategoryParser.getCategories()
                    );
                }
            }
            @Override
            public void onFail(Map<String, String> errors) {

            }
        });

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

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        Bundle extras = intent.getExtras();
        String event;
        if (extras != null) {
            event = extras.getString("Notified");
            if (APP_DEBUG) {
                NSLog.i("Notified", "Event notified  " + event);
            }
        } else {
            if (APP_DEBUG) {
                NSLog.i("Notified", "Extras are NULL");
            }

        }
    }



    @Override
    public void onBackPressed() {

        if (NavigationDrawerFragment.getInstance() != null)
            NavigationDrawerFragment.getInstance().closeDrawers();

        V2MainFragment mf = (V2MainFragment) getSupportFragmentManager().findFragmentByTag(V2MainFragment.TAG);

        if (mf.ifFirstFragment()) {
            if (AppConfig.RATE_US_FORCE) {
                if (SettingsController.rateOnApp(this)) {
                    super.onBackPressed();
                }
            } else {
                super.onBackPressed();
            }

        } else {
            mf.setCurrentFragment(0);
        }

    }

    @Override
    public void onScrollHorizontal(int pos) {
        NSLog.e("onScrollHorizontal", " Pos- " + pos);

        if (pos == 0) {
            hide_default_toolbar();
        } else {
            show_default_toolbar();
        }
    }

    @Override
    public void onScrollVertical(int scrollXs, int scrollY) {


        NSLog.e("onScrollVertical", " scrollY- " + scrollY);

        if (scrollY > 600)
            show_default_toolbar();
        else
            hide_default_toolbar();

    }

    /*
     * Hide home header
     */


    private void show_default_toolbar() {

        TextView titleToolbar = toolbar.findViewById(R.id.textToolbar);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && titleToolbar != null) {
            toolbar.setBackgroundColor(getColor(R.color.colorPrimary));
            titleToolbar.setTextColor(getColor(R.color.white));
            titleToolbar.setVisibility(View.VISIBLE);
        }

        toolbar.setVisibility(View.VISIBLE);

    }

    /*
     * Show home header
     */


    private void hide_default_toolbar() {

        TextView titleToolbar = toolbar.findViewById(R.id.textToolbar);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && titleToolbar != null) {
            toolbar.setBackground(getDrawable(R.drawable.v2_toolbar_gradient));
            titleToolbar.setTextColor(getColor(R.color.white));
            titleToolbar.setVisibility(View.GONE);
        }

        toolbar.setVisibility(View.GONE);

    }

    private void requestNotificationPermission(){

        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED) {
            // You can use the API that requires the permission.

        } else if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.POST_NOTIFICATIONS)) {
            NSLog.e(this.getClass().getName(),"Camera Permission is required");
        } else{
            // You can directly ask for the permission.
            // The registered ActivityResultCallback gets the result of this request.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requestPermissionLauncher.launch(
                        Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    //do something
                } else {
                    NSLog.e(this.getClass().getName(),"Camera Permission is required");
                }
            });

}
