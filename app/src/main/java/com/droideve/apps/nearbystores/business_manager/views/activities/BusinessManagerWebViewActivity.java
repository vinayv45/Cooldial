package com.droideve.apps.nearbystores.business_manager.views.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.droideve.apps.nearbystores.AppController;
import com.droideve.apps.nearbystores.R;
import com.droideve.apps.nearbystores.appconfig.AppConfig;
import com.droideve.apps.nearbystores.appconfig.Constances;
import com.droideve.apps.nearbystores.booking.controllers.OrdersController;
import com.droideve.apps.nearbystores.business_manager.models.BusinessUser;
import com.droideve.apps.nearbystores.business_manager.models.ModuleB;
import com.droideve.apps.nearbystores.business_manager.models.ModulePrivilegeB;
import com.droideve.apps.nearbystores.classes.User;
import com.droideve.apps.nearbystores.controllers.SettingsController;
import com.droideve.apps.nearbystores.network.api_request.ApiRequest;
import com.droideve.apps.nearbystores.network.api_request.ApiRequestListeners;
import com.droideve.apps.nearbystores.parser.Parser;
import com.droideve.apps.nearbystores.parser.api_parser.ModuleParser;
import com.droideve.apps.nearbystores.parser.api_parser.UserParser;
import com.droideve.apps.nearbystores.parser.tags.Tags;
import com.droideve.apps.nearbystores.utils.NSLog;
import com.droideve.apps.nearbystores.utils.NSToast;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import im.delight.android.webview.AdvancedWebView;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;


public class BusinessManagerWebViewActivity extends AppCompatActivity implements AdvancedWebView.Listener,  NavigationView.OnNavigationItemSelectedListener {

    @BindView(R.id.webview)
    AdvancedWebView mWebView;
    @BindView(R.id.my_drawer_layout)
    DrawerLayout drawerLayout;
    @BindView(R.id.app_bar)
    Toolbar toolbar;



    private BusinessUser mBusinessUser = null;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private String link;
    private TextView toolbar_title;
    private TextView toolbar_desc;
    private String TAG = BusinessManagerWebViewActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business_manager);
        ButterKnife.bind(this);

        //setup top toolbar
        initToolbar();

        //setup navigation menu
        setupDrawerLayout();

        //update menu navigation and display features
        updateNavigation();

        mWebView.setListener(this, this);

        //load dashboard view
        loadWebDashboard();

        //get session frop realm database
        getSessionFromDB();

        //load requirements
        loadRequirements();
    }

    private void loadRequirements(){

        OrdersController.loadBookingPaymentStatus();

    }

    private void getSessionFromDB(){

        Realm realm = Realm.getInstance(AppController.getBusinessRealmConfig());
        mBusinessUser = realm.where(BusinessUser.class).equalTo("id", 1).findFirst();

        if(mBusinessUser != null){
            updateNavigation();
        }

    }

    private void logoutFromAll(){

        Realm realm = Realm.getInstance(AppController.getBusinessRealmConfig());
        realm.beginTransaction();
        mBusinessUser = realm.where(BusinessUser.class).equalTo("id", 1).findFirst();

        if(mBusinessUser != null)
            mBusinessUser.deleteFromRealm();

        mBusinessUser = null;
        realm.commitTransaction();

    }

    private void loadWebDashboard(){
        mWebView.loadUrl(AppConfig.BASE_URL + "/webdashboard");
    }

    private void loadWebDashboardWithAction(String action){
        mWebView.loadUrl(AppConfig.BASE_URL + "/webDashboardAction?action="+action);
    }

    private void loadWebDashboardNoLoggedWithAction(String action){
        mWebView.loadUrl(AppConfig.BASE_URL + "/webDashboardActionNoLogged?action="+action);
    }

    public void initToolbar() {

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayUseLogoEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        Drawable bmicon = ResourcesCompat.getDrawable(getResources(),R.drawable.ic_close_white_24dp,null);
        DrawableCompat.setTint(
                DrawableCompat.wrap(bmicon),
                ContextCompat.getColor(this, R.color.white)
        );

        toolbar.setNavigationIcon(bmicon);

        toolbar_title = toolbar.findViewById(R.id.toolbar_title);
        toolbar_title.setText(getString(R.string.business_manager));
        toolbar_title.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        toolbar_desc = toolbar.findViewById(R.id.toolbar_subtitle);
        toolbar_desc.setVisibility(View.GONE);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.business_manager_menu, menu);

        return true;

    }

    protected AdvancedWebView getWebView(){
        return mWebView;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        if (android.R.id.home == item.getItemId()) {

            HashMap<String, String> mb_fields = new HashMap<String, String>();
            mb_fields.put("action", "manage_business_options");
            mb_fields.put("device", "android");

            Gson gson = new Gson();
            String data = gson.toJson(mb_fields);

            callJavaScript(mWebView, "handle_device_events", data);

            return true;

        } else if (R.id.manage_business_options == item.getItemId()) {
            finish();
            return true;
        } else if (R.id.nav_account == item.getItemId()) {

            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @SuppressLint("NewApi")
    @Override
    protected void onResume() {
        super.onResume();
        mWebView.onResume();

    }

    @SuppressLint("NewApi")
    @Override
    protected void onPause() {
        mWebView.onPause();
        // ...
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mWebView.onDestroy();
        // ...
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        mWebView.onActivityResult(requestCode, resultCode, intent);
        // ...
    }

    @Override
    public void onBackPressed() {
        if (!mWebView.onBackPressed()) {
            return;
        }
        // ...
        super.onBackPressed();

    }

    @Override
    public void onPageStarted(String url, Bitmap favicon) {
        toolbar.findViewById(R.id.progressLayout).setVisibility(View.VISIBLE);
    }


    @Override
    public void onPageFinished(String url) {
        NSLog.e("onPageFinished",url);
        toolbar.findViewById(R.id.progressLayout).setVisibility(View.GONE);
        userBusinessUpdateCall(url);
    }

    @Override
    public void onPageError(int errorCode, String description, String failingUrl) {

    }

    @Override
    public void onDownloadRequested(String url, String suggestedFilename, String mimeType, long contentLength, String contentDisposition, String userAgent) {
    }

    @Override
    public void onExternalPageRequest(String url) {
    }


    private void callJavaScript(WebView view, final String methodName, String param) {

        view.getSettings().setJavaScriptEnabled(true);

        StringBuilder stringBuilder = new StringBuilder();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            stringBuilder.append("try{");
        } else {
            stringBuilder.append("javascript:try{");
        }

        stringBuilder.append(methodName);
        stringBuilder.append("(");
        stringBuilder.append(param);


        stringBuilder.append(")}catch(error){console.error(error.message);}");
        final String call = stringBuilder.toString();
        NSLog.i(TAG, methodName + " : call=" + call);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            view.evaluateJavascript(call, new ValueCallback<String>() {
                @Override
                public void onReceiveValue(String s) {
                    NSLog.i(TAG, methodName + " : callback=" + s);
                }
            });
        } else {
            view.loadUrl(call);
        }


    }



    private void updateNavigation(){

        NavigationView nv = ((NavigationView)findViewById(R.id.bm_navigation));

        nv.setNavigationItemSelectedListener(this);

        MenuItem nav_dashboard = (MenuItem) nv.getMenu().findItem(R.id.nav_dashboard);
        MenuItem nav_scan_qrcode = (MenuItem) nv.getMenu().findItem(R.id.nav_scan_qrcode);
        MenuItem nav_account = (MenuItem) nv.getMenu().findItem(R.id.nav_account);
        MenuItem nav_logout = (MenuItem) nv.getMenu().findItem(R.id.nav_logout);
        MenuItem nav_plans = (MenuItem) nv.getMenu().findItem(R.id.nav_plans);

        if(SettingsController.isModuleEnabled("pack")){
            nav_plans.setVisible(true);
        }

        if(mBusinessUser==null){
            nav_scan_qrcode.setVisible(false);
            nav_account.setVisible(false);
            nav_logout.setVisible(false);
            return;
        }


        nav_dashboard.setVisible(true);
        nav_account.setVisible(true);
        nav_logout.setVisible(true);
        nav_plans.setVisible(false);

        if(hasAccess("qrcoupon","scan_qrcode_mobile")){
            nav_scan_qrcode.setVisible(true);
        }


    }

    private boolean moduleIsEnabled(String module){
        if(mBusinessUser == null)
            return false;

        ModuleB m = mBusinessUser.findModule(module);

        if(module == null)
            return false;

        return (m.isEnabled() == 1);
    }

    private boolean hasAccess(String module, String action){

        if(mBusinessUser == null)
            return false;

        ModuleB m = mBusinessUser.findModule(module);

        if(module == null)
            return false;

        if(m.isEnabled()==0)
            return false;

       ModulePrivilegeB mModulePrivilege = mBusinessUser.findPrivilege(module,action);

        return (mModulePrivilege != null && mModulePrivilege.enabled);
    }


    private void setupDrawerLayout(){
        // drawer layout instance to toggle the menu icon to open
        // drawer and back button to close drawer
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.nav_open, R.string.nav_close);
        // pass the Open and Close toggle for the drawer layout listener
        // to toggle the button
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        // to make the Navigation drawer icon always appear on the action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        if(item.getItemId() == R.id.nav_dashboard){
            loadWebDashboard();
        }else if(item.getItemId() == R.id.nav_scan_qrcode){
            //open
            //showScanBottomSheetDialog();
            startQRScanner();
        }else if(item.getItemId() == R.id.nav_account){
            loadWebDashboardWithAction("my_account");
        }else if(item.getItemId() == R.id.nav_logout){
            //logout from dashboard
            loadWebDashboardWithAction("logout");

            //update session
            logoutFromAll();

            //update navigation menu
            updateNavigation();
        }else if(item.getItemId() == R.id.nav_plans){
            loadWebDashboardNoLoggedWithAction("subscription");
        }

        //close menu navigation
        drawerLayout.closeDrawers();

        return false;
    }

    private void startQRScanner(){
        QRCodeScannerActivity.startView(this,qrcodeScannerLauncher, mBusinessUser.user.getId());
    }

    private void userBusinessUpdateCall(String url){

        if(url.contains("user/login")){
            //update navigation menu
            logoutFromAll();
            updateNavigation();
            return;
        }

        String token = getQueryValue(url,"userToken");
        if(token == null)
            return;

        ApiRequest.newPostInstance(Constances.API.API_FIND_USER_BY_TOKEN, new ApiRequestListeners() {
            @Override
            public void onSuccess(Parser parser) {

                final UserParser mUsersParser = new UserParser(parser);
                RealmList<User> list = mUsersParser.getUser();

                if(list.size() == 0){
                    NSToast.show(getString(R.string.dont_have_permission));
                    return;
                }

               loadModules(list.get(0));

            }

            @Override
            public void onFail(Map<String, String> errors) {

            }
        }, Map.of(
                "token", String.valueOf(token),
                "limit", String.valueOf(1)
        ));

        return;
    }

    private void loadModules(User user){


        ApiRequest.newPostInstance(Constances.API.API_AVAILABLE_MODULES, new ApiRequestListeners() {
            @Override
            public void onSuccess(Parser parser) {
                final ModuleParser mModuleParser = new ModuleParser(parser);
                int success = Integer.parseInt(mModuleParser.getStringAttr(Tags.SUCCESS));
                if(success == 1)
                    createBusinessUserObject(user,ModuleB.copyModules(mModuleParser.getModules()));
            }

            @Override
            public void onFail(Map<String, String> errors) {

            }
        }, Map.of(
                "user_id", String.valueOf(user.getId())
        ));

    }

    private void createBusinessUserObject(User user, RealmList<ModuleB> modules){

        Realm realm = Realm.getInstance(AppController.getBusinessRealmConfig());
        realm.beginTransaction();
         mBusinessUser = new BusinessUser();
         mBusinessUser.user = user;
         mBusinessUser.availableModules = modules;
         realm.commitTransaction();

         //save into database
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.copyToRealmOrUpdate(mBusinessUser);
            }
        });

         //reload navigation
        updateNavigation();
    }



    public String getQueryValue(String url,String key) {
        Uri uri = Uri.parse(url);
        return uri.getQueryParameter(key);
    }

    ActivityResultLauncher<Intent> qrcodeScannerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == AutocompleteActivity.RESULT_OK) {
                        NSLog.e("qrcodeScannerLauncher",result.getData().toString());
                    }
                }
            });

}
