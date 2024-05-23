package com.droideve.apps.nearbystores.activities;

import static com.droideve.apps.nearbystores.controllers.sessions.SessionsController.isLogged;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.balysv.materialripple.MaterialRippleLayout;
import com.bumptech.glide.Glide;
import com.droideve.apps.nearbystores.R;
import com.droideve.apps.nearbystores.animation.ImageLoaderAnimation;
import com.droideve.apps.nearbystores.appconfig.AppConfig;
import com.droideve.apps.nearbystores.appconfig.Constances;
import com.droideve.apps.nearbystores.booking.controllers.services.GenericNotifyEvent;
import com.droideve.apps.nearbystores.booking.views.activities.ServiceOptionsActivity;
import com.droideve.apps.nearbystores.booking.views.fragments.ServiceOptionsFragment;
import com.droideve.apps.nearbystores.booking.views.fragments.ServiceOptionsWidget;
import com.droideve.apps.nearbystores.classes.Discussion;
import com.droideve.apps.nearbystores.classes.OpeningTime;
import com.droideve.apps.nearbystores.classes.Setting;
import com.droideve.apps.nearbystores.classes.Store;
import com.droideve.apps.nearbystores.classes.User;
import com.droideve.apps.nearbystores.controllers.CampagneController;
import com.droideve.apps.nearbystores.controllers.SettingsController;
import com.droideve.apps.nearbystores.controllers.sessions.SessionsController;
import com.droideve.apps.nearbystores.controllers.stores.StoreController;
import com.droideve.apps.nearbystores.customView.GalleryStoreCustomView;
import com.droideve.apps.nearbystores.customView.OfferCustomView;
import com.droideve.apps.nearbystores.fragments.SlideshowDialogFragment;
import com.droideve.apps.nearbystores.helper.AppHelper;
import com.droideve.apps.nearbystores.load_manager.ViewManager;
import com.droideve.apps.nearbystores.location.GPStracker;
import com.droideve.apps.nearbystores.location.LocationSettingPopup;
import com.droideve.apps.nearbystores.location.Position;
import com.droideve.apps.nearbystores.network.ServiceHandler;
import com.droideve.apps.nearbystores.network.VolleySingleton;
import com.droideve.apps.nearbystores.network.api_request.SimpleRequest;
import com.droideve.apps.nearbystores.parser.api_parser.StoreParser;
import com.droideve.apps.nearbystores.parser.tags.Tags;
import com.droideve.apps.nearbystores.unbescape.html.HtmlEscape;
import com.droideve.apps.nearbystores.utils.DateUtils;
import com.droideve.apps.nearbystores.utils.NSLog;
import com.droideve.apps.nearbystores.utils.OfferUtils;
import com.droideve.apps.nearbystores.utils.TextUtils;
import com.droideve.apps.nearbystores.utils.Utils;
//import com.google.android.gms.ads.AdListener;
//import com.google.android.gms.ads.AdRequest;
//import com.google.android.gms.ads.AdView;
//import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;
import com.wuadam.awesomewebview.AwesomeWebView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import io.realm.RealmList;


public class StoreDetailActivity extends SimpleActivity implements
        GoogleMap.OnMapLoadedCallback, OnMapReadyCallback, View.OnClickListener {

    @BindView(R.id.app_bar)
    Toolbar toolbar;
    @BindView(R.id.badge_category)
    TextView badge_category;
    @BindView(R.id.badge_closed)
    TextView badge_closed;
    @BindView(R.id.badge_open)
    TextView badge_open;
    @BindView(R.id.image)
    ImageView image;
    @BindView(R.id.scroll_view)
    NestedScrollView scrollView;
    @BindView(R.id.opening_time_container)
    LinearLayout opening_time_container;

    @BindView(R.id.opening_time_content)
    TextView opening_time_content;

    @BindView(R.id.opening_time_label)
    TextView opening_time_label;

    @BindView(R.id.images_layout)
    RelativeLayout images_layout;
    @BindView(R.id.nbrPictures)
    TextView nbrPictures;

    @BindView(R.id.review_layout)
    RelativeLayout review_layout;
    @BindView(R.id.review_rate)
    TextView review_rate;
    @BindView(R.id.review_comment)
    TextView review_comment;

    @BindView(R.id.distance_layout)
    RelativeLayout distanceLayout;
    @BindView(R.id.distance_title)
    TextView distance_title;
    @BindView(R.id.distanceValue)
    TextView distanceValue;

    @BindView(R.id.adsLayout)
    LinearLayout adsLayout;
//    @BindView(R.id.adView)
//    AdView mAdView;

    @BindView(R.id.description_content)
    TextView description_content;
    @BindView(R.id.header_title)
    TextView header_title;
    @BindView(R.id.header_subtitle)
    TextView header_subtitle;
    @BindView(R.id.progressMapLL)
    LinearLayout progressMapLL;

    //Declare buttons bar
    @BindView(R.id.btnLocation)
    ImageButton btnLocation;
    @BindView(R.id.websiteBtn)
    ImageButton btnWebsite;
    @BindView(R.id.btn_chat)
    ImageButton btn_chat_customer;
    @BindView(R.id.btn_share)
    ImageButton btn_share;
    @BindView(R.id.btn_phone)
    ImageButton phoneBtn;

    @BindView(R.id.bottomBtnBook)
    MaterialRippleLayout bottomBtnBook;

    @BindView(R.id.bottomBtnAddToBookmarkBtn)
    MaterialRippleLayout bottomBtnAddToBookmarkBtn;

    @BindView(R.id.bottomBtnContact)
    MaterialRippleLayout bottomBtnContact;

    //youtube player
    private YouTubePlayerView youTubePlayerView;


    @OnClick(R.id.bottomBtnBook)
    void onBottomOrderClick(View view) {

        if(storeData.getBook() == 0 && !storeData.getAffiliate_link().equals("")){
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(storeData.getAffiliate_link()));
            startActivity(browserIntent);
            return;
        }

        if (!SessionsController.isLogged()) {
            Intent intent = new Intent(StoreDetailActivity.this, LoginV2Activity.class);
            startActivity(intent);
            finish();
            return;
        }

        if(storeData != null && storeData.getServices().size() == 0){
            return;
        }


        EventBus.getDefault().postSticky(new ServiceOptionsWidget.NotifyBackServiceOptionHandleSelection());

    }

    @OnClick(R.id.bottomBtnContact)
    void onBottomContactClick(View view) {

        if(SessionsController.isLogged()
                && storeData != null && storeData.getCanChat() == 1
                && SettingsController.isModuleEnabled("messenger")
        ){
            startInbox();
        }else if(storeData != null && !storeData.getPhone().trim().equals("")){
            call();
        }else if(storeData != null && !storeData.getWebsite().trim().equals("")){
            new AwesomeWebView.Builder(StoreDetailActivity.this)
                    .statusBarColorRes(R.color.colorPrimary)
                    .theme(R.style.FinestWebViewAppTheme)
                    .titleColor(ResourcesCompat.getColor(getResources(), R.color.colorWhite, null))
                    .urlColor(ResourcesCompat.getColor(getResources(), R.color.colorWhite, null))
                    .show(storeData.getWebsite());
        }

    }

    @OnClick(R.id.bottomBtnAddToBookmarkBtn)
    void onBottomAddToBookmarkClick(View view) {
        bookMarkToggle();
    }

    /*
    * Declare variables
     */
    private Context context;
    private ViewManager mViewManager;
    private Store storeData;
    private GoogleMap mMap;
    private GPStracker mGPS;
    private Menu mMenu;
    private BottomSheetDialog mBottomSheetDialog;


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(null);
        setContentView(R.layout.activity_detail_store);

        //Bind views
        ButterKnife.bind(this);
        //contents of menu have changed, and menu should be redrawn.
        invalidateOptionsMenu();

        initParams();

        //setup views
        setupViews();

        //setup view manager showError loading content
        setupViewManager();

        //handle button click event
        handleButtonClickEvent();

        //youtube player
        setupYoutubePlayer();

        //setup the ADMOB
        setupAdmob();

        //campaign handler
        try {
            int cid = Integer.parseInt(getIntent().getExtras().getString("cid"));
            CampagneController.markView(cid);
        } catch (Exception e) {e.printStackTrace();}

        //listing store data
        listingstoreData();


        //button click listener
        btn_chat_customer.setOnClickListener(this);
        btn_share.setOnClickListener(this);
        phoneBtn.setOnClickListener(this);

    }

    private void setupYoutubePlayer() {
        youTubePlayerView = findViewById(R.id.youtube_player_view);
        getLifecycle().addObserver(youTubePlayerView);
    }


    private void listingstoreData() {
        (new Handler()).postDelayed(new Runnable() {
            @Override
            public void run() {
                getStore();
            }
        }, 500);

    }


    private void initParams() {
        //Set current context
        context = this;
        mGPS = new GPStracker(context);

    }

    private void setupViewManager() {

        //setup view manager
        mViewManager = new ViewManager(this);
        mViewManager.setLoadingView(findViewById(R.id.loading));
        mViewManager.setContentView(findViewById(R.id.content));
        mViewManager.setErrorView(findViewById(R.id.error));
        mViewManager.setEmptyView(findViewById(R.id.empty));

        mViewManager.setListener(new ViewManager.CallViewListener() {
            @Override
            public void onContentShown() {
                scrollView.setNestedScrollingEnabled(true);
            }

            @Override
            public void onErrorShown() {

            }

            @Override
            public void onEmptyShown() {
                scrollView.setNestedScrollingEnabled(false);
            }

            @Override
            public void onLoadingShown() {

            }
        });

        mViewManager.showLoading();

    }


    private void setupViews() {

        //setup toolbar
        setupToolbar(toolbar);
        getAppBarSubtitle().setVisibility(View.GONE);

        //setup scroll with header
        setupScrollNHeader(
                scrollView,
                (LinearLayout) findViewById(R.id.header_detail),
                SimpleHeaderSize.HALF,
                findViewById(R.id.store_detail)
        );


        //setup header views
        setupHeader();

        btnLocation.setOnClickListener(this);
        btnWebsite.setOnClickListener(this);
    }




    private void setupHeader() {

        //setup all badge
        setupBadges();

    }

    private void setupBadges() {


        Drawable badge_closed_background = badge_closed.getBackground();
        if (badge_closed_background instanceof ShapeDrawable) {
            ((ShapeDrawable) badge_closed_background).getPaint().setColor(ContextCompat.getColor(this, R.color.orange_600));
        } else if (badge_closed_background instanceof GradientDrawable) {
            ((GradientDrawable) badge_closed_background).setColor(ContextCompat.getColor(this, R.color.orange_600));
        } else if (badge_closed_background instanceof ColorDrawable) {
            ((ColorDrawable) badge_closed_background).setColor(ContextCompat.getColor(this, R.color.orange_600));
        }

        Drawable badge_open_background = badge_open.getBackground();
        if (badge_closed_background instanceof ShapeDrawable) {
            ((ShapeDrawable) badge_open_background).getPaint().setColor(ContextCompat.getColor(this, R.color.seaGreen));
        } else if (badge_closed_background instanceof GradientDrawable) {
            ((GradientDrawable) badge_open_background).setColor(ContextCompat.getColor(this, R.color.seaGreen));
        } else if (badge_closed_background instanceof ColorDrawable) {
            ((ColorDrawable) badge_open_background).setColor(ContextCompat.getColor(this, R.color.seaGreen));
        }

    }


    private void updateCategoryBadge(String title) {
        updateCategoryBadge(title, null);
    }

    private void updateCategoryBadge(String title, String color_hex) {

        int color = ContextCompat.getColor(this, R.color.colorPrimary);

        try {
            if (color_hex != null && !color_hex.equals("null"))
                color = Color.parseColor(color_hex);
        } catch (Exception e) {
            NSLog.e("colorParser", e.getMessage());
        }

        Drawable badge_cat_background = badge_category.getBackground();
        if (badge_cat_background instanceof ShapeDrawable) {
            ((ShapeDrawable) badge_cat_background).getPaint().setColor(color);
        } else if (badge_cat_background instanceof GradientDrawable) {
            ((GradientDrawable) badge_cat_background).setColor(color);
        } else if (badge_cat_background instanceof ColorDrawable) {
            ((ColorDrawable) badge_cat_background).setColor(color);
        }

        badge_category.setText(title);

    }


    private void setupAdmob() {


        if (AppConfig.SHOW_ADS) {

//            mAdView.setVisibility(View.VISIBLE);
//            AdRequest adRequest = new AdRequest.Builder().build();
//
//
//            mAdView.loadAd(adRequest);
//            mAdView.setAdListener(new AdListener() {
//                @Override
//                public void onAdClicked() {
//                    // Code to be executed when the user clicks on an ad.
//                }
//
//                @Override
//                public void onAdClosed() {
//                    // Code to be executed when the user is about to return
//                    // to the app after tapping on an ad.
//                }
//
//                @Override
//                public void onAdFailedToLoad(LoadAdError adError) {
//                    // Code to be executed when an ad request fails.
//                    mAdView.setVisibility(View.GONE);
//                    adsLayout.setVisibility(View.GONE);
//                }
//
//                @Override
//                public void onAdImpression() {
//                    // Code to be executed when an impression is recorded
//                    // for an ad.
//                }
//
//                @Override
//                public void onAdLoaded() {
//                    super.onAdLoaded();
//                    mAdView.setVisibility(View.VISIBLE);
//                    adsLayout.setVisibility(View.VISIBLE);
//
//                }
//
//                @Override
//                public void onAdOpened() {
//                    // Code to be executed when an ad opens an overlay that
//                    // covers the screen.
//                }
//            });
//
//            mAdView.loadAd(adRequest);


        } else
            adsLayout.setVisibility(View.GONE);

    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.home_menu, menu);

        mMenu = menu;

        menu.findItem(R.id.bookmarks_icon).setVisible(false);
        menu.findItem(R.id.share_post).setVisible(false);
        menu.findItem(R.id.report_icon).setVisible(true);

        return true;
    }

    private void initOfferRV(int store_id) {
        Map<String, Object> optionalParams = new HashMap<>();
        optionalParams.put("store_id", String.valueOf(store_id));
        //custom view
        OfferCustomView horizontalOfferList = findViewById(R.id.horizontalOfferList);
        if (storeData.getLastOffer().equals("")) {
            horizontalOfferList.setVisibility(View.GONE);
        } else {
            horizontalOfferList.setVisibility(View.VISIBLE);
            horizontalOfferList.loadData(false, optionalParams);
            findViewById(R.id.card_show_more).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(StoreDetailActivity.this, OffersListActivity.class));
                    
                }
            });
        }

    }


    private void initGalleryRV(int store_id, int galleryCount) {
        GalleryStoreCustomView horizontalGalleryList = findViewById(R.id.horizontalGalleryList);
        if (galleryCount > 0) {
            horizontalGalleryList.setVisibility(View.VISIBLE);
            horizontalGalleryList.loadData(store_id, Constances.ModulesConfig.STORE_MODULE, this, false);

        } else {
            horizontalGalleryList.setVisibility(View.GONE);

        }
    }


    private void getStore() {

        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();

        int store_id = 0;

        //get it from external url (deep linking)
        try {

            Intent appLinkIntent = getIntent();
            String appLinkAction = appLinkIntent.getAction();
            Uri appLinkData = appLinkIntent.getData();

            if (appLinkAction.equals(Intent.ACTION_VIEW)) {

                if (AppConfig.APP_DEBUG)
                    Toast.makeText(getApplicationContext(), appLinkData.toString(), Toast.LENGTH_LONG).show();
                store_id = Utils.dp_get_id_from_url(appLinkData.toString(), Constances.ModulesConfig.STORE_MODULE);

                if (AppConfig.APP_DEBUG)
                    Toast.makeText(getApplicationContext(), "The ID: " + store_id + " " + appLinkAction, Toast.LENGTH_LONG).show();
            }

        } catch (Exception e) {

        }


        //get it from internal app
        if (store_id == 0) {

            Bundle bundle = getIntent().getExtras();
            store_id = bundle.getInt("id");
            try {
                if (store_id == 0) {
                    store_id = Integer.parseInt(bundle.getString("id"));
                }
            } catch (Exception e) {
                store_id = 0;
            }
        }


        if (AppConfig.APP_DEBUG)
            NSLog.e("_2_store_id", String.valueOf(store_id));

        final Store mStore = StoreController.getStore(store_id);

        //GET DATA FROM API IF NETWORK IS AVAILABLE
        if (ServiceHandler.isNetworkAvailable(this)) {
            syncStore(store_id);
        } else {
            //IF NOT GET THE ITEM FROM THE DATABASE
            if (mStore != null && mStore.isLoaded()) {
                storeData = mStore;
                mViewManager.showContent();
                setupstoreDataViews();
            }
        }


        realm.commitTransaction();
    }

    private void setupstoreDataViews() {

        setBookmarkIcons();

        if (storeData.getGallery() > 0) {

            initGalleryRV(storeData.getId(), storeData.getGallery());
        }

        if (storeData.getListImages() != null && storeData.getListImages().size() > 0 && storeData.getListImages().get(0)
                .getUrl500_500() != null) {

            Glide.with(getBaseContext())
                    .load(storeData.getListImages().get(0)
                            .getUrl500_500())
                    .fitCenter().placeholder(ImageLoaderAnimation.glideLoader(context))
                    .into(image);

        } else {

            Glide.with(getBaseContext())
                    .load(R.drawable.def_logo)
                    .centerCrop().placeholder(R.drawable.def_logo)
                    .into(image);

        }


        if (storeData.getCategory_color() != null)
            updateCategoryBadge(storeData.getCategory_name(), storeData.getCategory_color());
        else
            updateCategoryBadge(storeData.getCategory_name());


        if (storeData.getOpening() == 0 || storeData.getOpening() == -1) {
            if (storeData.getOpening_time_table_list() != null && storeData.getOpening_time_table_list().size() > 0)
                badge_closed.setVisibility(View.VISIBLE);
            else
                badge_closed.setVisibility(View.GONE);

            badge_open.setVisibility(View.GONE);
        } else if (storeData.getOpening() == 1) {

            badge_closed.setVisibility(View.GONE);
            badge_open.setVisibility(View.VISIBLE);
        }

        parseOpeningTime();


        float rated = (float) storeData.getVotes();
        DecimalFormat decim = new DecimalFormat("#.##");
        review_rate.setText(decim.format(rated));
        if (storeData.getNbr_votes() != null && !storeData.getNbr_votes().equals(""))
            review_comment.setText(
                    String.format(this.getString(R.string.reviews), storeData.getNbr_votes())
            );


        if (storeData.getPhone().trim().equals("")) {
            phoneBtn.setVisibility(View.GONE);
        }

        if (storeData.getWebsite() != null && !storeData.getWebsite().equals("null")) {
            btnWebsite.setOnClickListener(this);
            btnWebsite.setVisibility(View.VISIBLE);
        } else {
            btnWebsite.setVisibility(View.GONE);
        }


        int nbrPics = storeData.getListImages() != null ? storeData.getListImages().size() : 1;
        nbrPictures.setText(nbrPics + "");

        if (mGPS.getLatitude() == 0 && mGPS.getLongitude() == 0) {
            distanceLayout.setVisibility(View.GONE);
        }


        Position newPosition = new Position();
        if (mGPS.getLatitude() == 0 && mGPS.getLongitude() == 0) {
            distanceValue.setVisibility(View.GONE);
        }

        Double mDistance = newPosition.distance(mGPS.getLatitude(), mGPS.getLongitude(), storeData.getLatitude(), storeData.getLongitude());
        parseDisitanceByUnit(mDistance);


        getAppBarTitle().setText(storeData.getName());
        header_title.setText(storeData.getName());
        header_subtitle.setText(storeData.getAddress());


        (new Handler()).postDelayed(new Runnable() {
            @Override
            public void run() {
                attachMap();
            }
        }, 2000);


        //make links in a TextView clickable
        description_content.setMovementMethod(LinkMovementMethod.getInstance());
        new decodeHtml().execute(storeData.getDetail());

        initOfferRV(storeData.getId());


        if (storeData.getCanChat() == 1 && AppConfig.ENABLE_CHAT) {
            btn_chat_customer.setVisibility(View.VISIBLE);
        } else {
            btn_chat_customer.setVisibility(View.GONE);
        }


        if (storeData.getBook() == 1
                && SettingsController.isModuleEnabled(Constances.ModulesConfig.BOOKING_MODULE)
                && storeData.getServices().size()>0
        ) {
            bottomBtnBook.setVisibility(View.VISIBLE);
            bottomBtnContact.setVisibility(View.GONE);
        }else if(storeData.getBook() == 0 && !storeData.getAffiliate_link().equals("")){
            bottomBtnBook.setVisibility(View.VISIBLE);
            bottomBtnContact.setVisibility(View.GONE);
        }else {
            bottomBtnContact.setVisibility(View.VISIBLE);
            bottomBtnBook.setVisibility(View.GONE);
        }


        //if store has video_url show the layout  , else hide the view
        if (storeData.getVideo_url() != null && !storeData.getVideo_url().equals("null") && !storeData.getVideo_url().equals("")) {


            //show ad
            (new Handler()).postDelayed(new Runnable() {
                @Override
                public void run() {

                    youTubePlayerView.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
                        @Override
                        public void onReady(@NonNull YouTubePlayer youTubePlayer) {
                            String videoId = AppHelper.extractYoutubeVideoId(storeData.getVideo_url());
                            if (videoId != null) youTubePlayer.cueVideo(videoId, 0);
                        }
                    });

                    findViewById(R.id.video_layout).setVisibility(View.VISIBLE);

                }
            }, 2000);


        } else {
            findViewById(R.id.video_layout).setVisibility(View.GONE);
        }


        //setup product variants
        setupServicesWidget();


        mViewManager.showContent();


    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe
    public void onCartUpdateReceiver(ServiceOptionsWidget.NotifyServiceOptionHandleSelection content) {
        String price = String.format(OfferUtils.parseCurrencyFormat(
                content.price,
                OfferUtils.defaultCurrency()));
        ((AppCompatButton)this.bottomBtnBook.findViewById(R.id.bookBtn)).setText(String.format(getString(R.string.book_btn_price), price));
    }

    private void setupServicesWidget(){

        if (storeData.getNbrServices()>0 && storeData.getBook() == 1) {

            Bundle bundle = new Bundle();
            bundle.putInt(Constances.ModulesConfig.STORE_MODULE, storeData.getId());

            ServiceOptionsWidget fragment = new ServiceOptionsWidget();
            fragment.setArguments(bundle);

            FragmentManager manager = getSupportFragmentManager();
            manager.beginTransaction()
                    .replace(R.id.store_services_container, fragment)
                    .commit();

            findViewById(R.id.store_services).setVisibility(View.VISIBLE);

        }else {
            findViewById(R.id.store_services).setVisibility(View.GONE);
        }
    }

    private void parseDisitanceByUnit(Double mDistance) {
        SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(this);
        String distance_unit = sh.getString("distance_unit", "mile");

        String disStr;
        if (distance_unit.equals("km")) {
            disStr = Utils.prepareDistanceKm(mDistance)
                    + " " +
                    Utils.getDistanceByKm(mDistance).toLowerCase();
        } else {
            disStr = Utils.prepareDistanceMiles(mDistance)
                    + " " +
                    Utils.getDistanceMiles(mDistance).toLowerCase();
        }
        distanceValue.setText(disStr);
    }


    private void parseOpeningTime() {
        /*
         * Opening time table
         */

        Setting isEnabled = SettingsController.findSettingFiled("OPENING_TIME_ENABLED");
        if(isEnabled != null && !Boolean.parseBoolean(isEnabled.getValue())){
            opening_time_container.setVisibility(View.GONE);
        }





        if (storeData.getOpening() == 1 || storeData.getOpening() == -1) {

            opening_time_container.setVisibility(View.VISIBLE);
            String opt_string = "";

            Setting dateFormat = SettingsController.findSettingFiled("DATE_FORMAT");

            String timeFormat = "24";
            if(dateFormat != null){
                 timeFormat = dateFormat.getValue().equals("24") ? "HH:mm" : "hh:mm a";
            }else{
                 timeFormat = AppConfig.FORMAT_24 ? "HH:mm" : "hh:mm a";
            }


            for (int i = 0; i < storeData.getOpening_time_table_list().size(); i++) {

                OpeningTime opt = storeData.getOpening_time_table_list().get(i);


                String formatted_opening = DateUtils.getPrepareSimpleDate("01-01-2011 " + opt.getOpening(), timeFormat);
                String formatted_closing = DateUtils.getPrepareSimpleDate("01-01-2011 " + opt.getClosing(), timeFormat);

                String opening_status = "";

                String opening_day = opt.getDay().substring(0, 3);

                if (storeData.getOpening() == 1 && DateUtils.getCurrentDay().toLowerCase().equals(opening_day.toLowerCase())) {
                    opening_status = false?" \t - \t<b><font color=" + ContextCompat.getColor(this, R.color.seaGreen) + ">" + getString(R.string.open_now) + "</font><b>":"";
                } else if (storeData.getOpening() == -1 && DateUtils.getCurrentDay().toLowerCase().equals(opening_day.toLowerCase())) {
                    opening_status = false?"\t - \t<b><font color=\"red\">" + getString(R.string.closed) + "</font><b>":"";
                }

                //translate language
                if (opt.getDay() != null) {
                    switch (opt.getDay().toLowerCase()) {
                        case "monday":
                            opening_day = getString(R.string.monday);
                            break;
                        case "tuesday":
                            opening_day = getString(R.string.tuesday);
                            break;
                        case "wednesday":
                            opening_day = getString(R.string.wednesday);
                            break;
                        case "thursday":
                            opening_day = getString(R.string.thursday);
                            break;
                        case "friday":
                            opening_day = getString(R.string.friday);
                            break;
                        case "saturday":
                            opening_day = getString(R.string.saturday);
                            break;
                        case "sunday":
                            opening_day = getString(R.string.sunday);
                            break;

                    }

                }


                if (opt.getOpening().equals(opt.getClosing())) {
                    if(opening_status.equals(""))
                        opening_status = " --";
                    opt_string = opt_string + "<b>" + TextUtils.capitalizeFirstLetter(opening_day) + "</b>: <i> " + opening_status + "</i> <br>";
                } else {
                    opt_string = opt_string + "<b>" + TextUtils.capitalizeFirstLetter(opening_day) + "</b>: <i> " + formatted_opening + " - " + formatted_closing + " " + opening_status + "</i> <br>";
                }


            }

            opening_time_content.setText(Html.fromHtml(opt_string));

        } else {
            opening_time_container.setVisibility(View.GONE);
        }


        badge_open.setVisibility(View.GONE);
        badge_closed.setVisibility(View.GONE);


        /*
         * End Opening time table
         */
    }

    public void syncStore(final int store_id) {

        RequestQueue queue = VolleySingleton.getInstance(this).getRequestQueue();

        mViewManager.showLoading();

        SimpleRequest request = new SimpleRequest(Request.Method.POST,
                Constances.API.API_USER_GET_STORES, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                parse_store_data(response);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                NSLog.e("ERROR", error.toString());
                mViewManager.showError();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("limit", "1");
                params.put("store_id", String.valueOf(store_id));
                if (mGPS.canGetLocation()) {
                    params.put("latitude", mGPS.getLatitude() + "");
                    params.put("longitude", mGPS.getLongitude() + "");
                }


                params.put("current_date", DateUtils.getUTC("yyyy-MM-dd HH:mm"));
                params.put("current_tz", TimeZone.getDefault().getID());

                NSLog.e("StoreDetailActivity", "  params :" + params.toString());

                return params;
            }

        };


        request.setRetryPolicy(new DefaultRetryPolicy(SimpleRequest.TIME_OUT,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        queue.add(request);


    }

    private void parse_store_data(String response) {

        try {

            NSLog.e("responseStoresString", response);

            JSONObject jsonObject = new JSONObject(response);

            final StoreParser mStoreParser = new StoreParser(jsonObject);
            RealmList<Store> list = mStoreParser.getStore();

            if (list.size() > 0) {

                StoreController.insertStores(list);
                storeData = list.get(0);
                setupstoreDataViews();

            } else {
                mViewManager.showEmpty();
            }

        } catch (JSONException e) {
            //send a rapport to support
            e.printStackTrace();
            mViewManager.showError();

        }
    }


    public void notificationAgreement(final int bookmark_id, final int user_id, final int notificationStatus) {

        RequestQueue queue = VolleySingleton.getInstance(this).getRequestQueue();

        SimpleRequest request = new SimpleRequest(Request.Method.POST,
                Constances.API.API_NOTIFICATIONS_AGREEMENT, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {

                    if (AppConfig.APP_DEBUG) {
                        NSLog.e("notificationAgreement", "response  : " + response);
                    }

                    JSONObject jsonObject = new JSONObject(response);

                    if (jsonObject.getInt(Tags.SUCCESS) == 1) {
                        Toast.makeText(StoreDetailActivity.this, "Notification agreement granted for this business ", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(StoreDetailActivity.this, "Something went wrong , please try later ", Toast.LENGTH_SHORT).show();
                    }

                } catch (JSONException e) {
                    //send a rapport to support
                    e.printStackTrace();

                    mViewManager.showError();

                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (AppConfig.APP_DEBUG) {
                    NSLog.e("ERROR", error.toString());
                }
                mViewManager.showError();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();

                params.put("bookmark_id", String.valueOf(bookmark_id));
                params.put("user_id", String.valueOf(user_id));
                params.put("agreement", String.valueOf(notificationStatus)); //todo : set the agreement according to the store status

                if (AppConfig.APP_DEBUG) {
                    NSLog.e("notificationAgreement", "params :" + params.toString());
                }

                return params;
            }

        };


        request.setRetryPolicy(new DefaultRetryPolicy(SimpleRequest.TIME_OUT,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        queue.add(request);


    }

    private void attachMap() {

        try {

            SupportMapFragment mSupportMapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.mapping);
            if (mSupportMapFragment == null) {
                androidx.fragment.app.FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                mSupportMapFragment = SupportMapFragment.newInstance();
                mSupportMapFragment.setRetainInstance(true);
                fragmentTransaction.replace(R.id.mapping, mSupportMapFragment).commit();
            }
            if (mSupportMapFragment != null) {
                mSupportMapFragment.getMapAsync(this);
            }

        } catch (Exception e) {
            progressMapLL.setVisibility(View.GONE);
        }

    }

    @Override
    public void onMapLoaded() {
        Toast.makeText(getApplicationContext(), "Map is ready ", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        mMap = googleMap;
        if (storeData.getLatitude() != null && storeData.getLatitude() != null) {

            double TraderLat = storeData.getLatitude();
            double TraderLng = storeData.getLongitude();
            LatLng customerPosition = new LatLng(TraderLat, TraderLng);
            //move map
            moveToPosition(mMap, customerPosition);
        }

        progressMapLL.setVisibility(View.GONE);

    }

    private void moveToPosition(GoogleMap gm, LatLng targetPosition) {

        gm.moveCamera(CameraUpdateFactory.newLatLngZoom(targetPosition, 16));
        gm.getUiSettings().setZoomControlsEnabled(true);
        gm.addMarker(new MarkerOptions()
                .title(this.getString(R.string.your_destination))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker))
                .position(targetPosition));
    }

    private void call(){
        try {

            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + storeData.getPhone().trim()));
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {

                String[] permission = new String[]{Manifest.permission.CALL_PHONE};
                SettingsController.requestPermissionM(StoreDetailActivity.this, permission);

                return;
            }
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getApplicationContext(), getString(R.string.store_call_error) + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void startInbox(){

        if (isLogged()) {

            int userId = 0;
            try {
                userId = storeData.getUser().getId();
            } catch (Exception e) {
                userId = storeData.getUser_id();
            }

            Intent intent = new Intent(StoreDetailActivity.this, MessengerActivity.class);
            intent.putExtra("type", Discussion.DISCUSION_WITH_USER);
            intent.putExtra("userId", userId);
            intent.putExtra("storeName", storeData.getName());
            startActivity(intent);

        } else {
            Intent intent = new Intent(StoreDetailActivity.this, LoginV2Activity.class);
            startActivity(intent);
            finish();
        }

    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.btn_phone) {
            call();
        } else if (v.getId() == R.id.btn_chat) {
            startInbox();
        } else if (v.getId() == R.id.btn_share) {

            @SuppressLint({"StringFormatInvalid", "LocalSuppress", "StringFormatMatches"}) String shared_text =
                    String.format(getString(R.string.shared_text),
                            storeData.getName(),
                            getString(R.string.app_name),
                            storeData.getLink()
                    );

            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, shared_text);
            sendIntent.setType("text/plain");
            startActivity(sendIntent);

        } else if (v.getId() == R.id.websiteBtn) {

            new AwesomeWebView.Builder(StoreDetailActivity.this)
                    .statusBarColorRes(R.color.colorPrimary)
                    .theme(R.style.FinestWebViewAppTheme)
                    .titleColor(ResourcesCompat.getColor(getResources(), R.color.colorWhite, null))
                    .urlColor(ResourcesCompat.getColor(getResources(), R.color.colorWhite, null))
                    .show(storeData.getWebsite());

        } else if (v.getId() == R.id.btnLocation) {

            Uri gmmIntentUri = Uri.parse(String.format(Locale.ENGLISH, "http://maps.google.com/maps?q=loc:%f,%f", storeData.getLatitude(), storeData.getLongitude()));
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            if (mapIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(mapIntent);
            }
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {


        if (android.R.id.home == item.getItemId()) {
            finish();
        } else if (item.getItemId() == R.id.share_post) {

            if (storeData != null) {
                @SuppressLint({"StringFormatInvalid", "LocalSuppress", "StringFormatMatches"}) String shared_text =
                        String.format(getString(R.string.shared_text),
                                storeData.getName(),
                                getString(R.string.app_name),
                                storeData.getLink() != null ? storeData.getLink() : ""
                        );

                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, shared_text);
                sendIntent.setType("text/plain");
                startActivity(sendIntent);
            }

        } else if (item.getItemId() == R.id.bookmarks_icon) {

            bookMarkToggle();

        } else if (item.getItemId() == R.id.report_icon) {

            if (!SessionsController.isLogged()) {
                startActivity(new Intent(StoreDetailActivity.this, LoginV2Activity.class));
                
            } else {

                Intent intent = new Intent(StoreDetailActivity.this, ReportIssueActivity.class);
                intent.putExtra("id", storeData.getId());
                intent.putExtra("owner_id", storeData.getUser_id());
                intent.putExtra("name", storeData.getName());
                intent.putExtra("link", storeData.getLink());
                intent.putExtra("module", "store");
                startActivity(intent);
            }


        }

        return super.onOptionsItemSelected(item);
    }

    private void bookMarkToggle(){
        if (!isLogged()) {

            startActivity(new Intent(StoreDetailActivity.this, LoginV2Activity.class));
            return;
        }
        try {
            User currentUser = SessionsController.getSession().getUser();
            if (storeData.getSaved() > 0) {
                removeStoreToBookmarks(this, currentUser.getId(), storeData.getId());
            } else {
                saveStoreToBookmarks(this, currentUser.getId(), storeData.getId());
            }
        } catch (Exception e) {
            //send a rapport to support
            if (AppConfig.APP_DEBUG) e.printStackTrace();
        }
    }

    private void setBookmarkIcons() {

        // Menu
        MenuItem bookmarksItemMenu = mMenu.findItem(R.id.bookmarks_icon);
        if (bookmarksItemMenu != null) {
            if ((SessionsController.isLogged() && storeData.getSaved() > 0)) {
                Drawable ic_fav = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_favourite,null);
                ic_fav.setTint(ResourcesCompat.getColor(getResources(), R.color.white,null));
                bookmarksItemMenu.setIcon(ic_fav);
            } else {
                Drawable ic_fav = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_favourite_outline,null);
                ic_fav.setTint(ResourcesCompat.getColor(getResources(), R.color.white,null));
                bookmarksItemMenu.setIcon(ic_fav);
            }
        }

        //Bottom Icon
        if ((SessionsController.isLogged() && storeData.getSaved() > 0)) {
            Drawable ic_fav = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_favourite,null);
            ic_fav.setTint(ResourcesCompat.getColor(getResources(), R.color.favourite_color,null));
            ((ImageView)bottomBtnAddToBookmarkBtn.findViewById(R.id.bottomBtnAddToBookmarkIcon)).setImageDrawable(ic_fav);
        } else {
            Drawable ic_fav = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_favourite_outline,null);
            ic_fav.setTint(ResourcesCompat.getColor(getResources(), R.color.favourite_color,null));
            ((ImageView)bottomBtnAddToBookmarkBtn.findViewById(R.id.bottomBtnAddToBookmarkIcon)).setImageDrawable(ic_fav);
        }
    }

    private void handleButtonClickEvent() {

        review_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ReviewsActivity.class);
                intent.putExtra("store_id", storeData.getId());
                context.startActivity(intent);
            }
        });

        images_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (storeData != null && storeData.getListImages() != null && storeData.getListImages().size() > 0) {
                    SlideshowDialogFragment.newInstance().show(StoreDetailActivity.this, storeData.getListImages(), 0);
                }
            }
        });


    }

    private void showBottomSheetDialog(final int bookmark_id) {

        if(!AppConfig.NOTIFICATION_AGREEMENT)
            return;

        final View view = getLayoutInflater().inflate(R.layout.notifyme_sheet, null);
        ((TextView) view.findViewById(R.id.name)).setText(R.string.receive_notif);
        ((TextView) view.findViewById(R.id.address)).setText(R.string.recieve_notif_confirmation);
        (view.findViewById(R.id.bt_close)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBottomSheetDialog.dismiss();
            }
        });

        (view.findViewById(R.id.bt_details)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notificationAgreement(bookmark_id, SessionsController.getSession().getUser().getId(), 1);
                mBottomSheetDialog.dismiss();
            }
        });

        mBottomSheetDialog = new BottomSheetDialog(this);
        mBottomSheetDialog.setContentView(view);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mBottomSheetDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        mBottomSheetDialog.show();
        mBottomSheetDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                mBottomSheetDialog = null;
            }
        });
    }

    public void saveStoreToBookmarks(final Context context, final int user_id, final int int_id) {

        RequestQueue queue = VolleySingleton.getInstance(context).getRequestQueue();
        SimpleRequest request = new SimpleRequest(Request.Method.POST,
                Constances.API.API_BOOKMARK_STORE_SAVE, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                if (AppConfig.APP_DEBUG) {
                    NSLog.e("response", response);
                }

                try {

                    JSONObject jsonObject = new JSONObject(response);

                    if (jsonObject.getInt(Tags.SUCCESS) == 1) {

                        storeData = StoreController.doSave(storeData.getId(), 1);
                        if (storeData != null) {
                            setBookmarkIcons();
                        }
                        showBottomSheetDialog(jsonObject.getInt(Tags.RESULT));

                    } else {
                        Toast.makeText(getApplicationContext(), R.string.something_went_wrong, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    //send a rapport to support
                    e.printStackTrace();
                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (AppConfig.APP_DEBUG) {
                    NSLog.e("ERROR", error.toString());
                }


            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();

                params.put("user_id", String.valueOf(user_id));
                params.put("store_id", String.valueOf(int_id));

                return params;
            }

        };

        request.setRetryPolicy(new DefaultRetryPolicy(SimpleRequest.TIME_OUT,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        queue.add(request);

    }

    public void removeStoreToBookmarks(final Context context, final int user_id, final int int_id) {

        RequestQueue queue = VolleySingleton.getInstance(context).getRequestQueue();
        SimpleRequest request = new SimpleRequest(Request.Method.POST,
                Constances.API.API_BOOKMARK_STORE_REMOVE, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                if (AppConfig.APP_DEBUG) {
                    NSLog.e("response", response);
                }

                try {

                    JSONObject jsonObject = new JSONObject(response);

                    if (jsonObject.getInt(Tags.SUCCESS) == 1) {
                        storeData = StoreController.doSave(storeData.getId(), 0);
                        if (storeData != null) {
                            setBookmarkIcons();
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "Something went wrong , please try later ", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    //send a rapport to support
                    e.printStackTrace();
                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (AppConfig.APP_DEBUG) {
                    NSLog.e("ERROR", error.toString());
                }


            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();

                params.put("user_id", String.valueOf(user_id));
                params.put("store_id", String.valueOf(int_id));

                return params;
            }

        };

        request.setRetryPolicy(new DefaultRetryPolicy(SimpleRequest.TIME_OUT,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        queue.add(request);

    }

    private class decodeHtml extends AsyncTask<String, String, String> {
        @Override
        protected void onPostExecute(final String text) {
            super.onPostExecute(text);
            description_content.setText(Html.fromHtml(text));
            Realm realm = Realm.getDefaultInstance();
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    try {
                        storeData.setDetail(text);
                        realm.copyToRealmOrUpdate(storeData);
                    } catch (Exception e) {

                    }

                }
            });
        }

        @Override
        protected String doInBackground(String... params) {
            return HtmlEscape.unescapeHtml(params[0]);
        }
    }


}
