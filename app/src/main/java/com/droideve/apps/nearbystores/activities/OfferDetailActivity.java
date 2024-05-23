package com.droideve.apps.nearbystores.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.net.Uri;
import android.os.Bundle;

import com.balysv.materialripple.MaterialRippleLayout;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.droideve.apps.nearbystores.classes.Discussion;
import com.droideve.apps.nearbystores.classes.User;
import com.droideve.apps.nearbystores.controllers.SettingsController;
import com.droideve.apps.nearbystores.customView.QRCouponView;
import com.droideve.apps.nearbystores.network.api_request.ApiRequest;
import com.droideve.apps.nearbystores.network.api_request.ApiRequestListeners;
import com.droideve.apps.nearbystores.parser.Parser;
import com.droideve.apps.nearbystores.utils.NSLog;

import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;
import com.droideve.apps.nearbystores.AppController;
import com.droideve.apps.nearbystores.location.GPStracker;
import com.droideve.apps.nearbystores.R;
import com.droideve.apps.nearbystores.animation.ImageLoaderAnimation;
import com.droideve.apps.nearbystores.appconfig.AppConfig;
import com.droideve.apps.nearbystores.appconfig.Constances;
import com.droideve.apps.nearbystores.classes.Offer;
import com.droideve.apps.nearbystores.classes.Store;
import com.droideve.apps.nearbystores.controllers.CampagneController;
import com.droideve.apps.nearbystores.controllers.sessions.SessionsController;
import com.droideve.apps.nearbystores.controllers.stores.OffersController;
import com.droideve.apps.nearbystores.controllers.stores.StoreController;
import com.droideve.apps.nearbystores.customView.StoreCardCustomView;
import com.droideve.apps.nearbystores.load_manager.ViewManager;
import com.droideve.apps.nearbystores.network.ServiceHandler;
import com.droideve.apps.nearbystores.network.VolleySingleton;
import com.droideve.apps.nearbystores.network.api_request.SimpleRequest;
import com.droideve.apps.nearbystores.parser.api_parser.OfferParser;
import com.droideve.apps.nearbystores.utils.DateUtils;
import com.droideve.apps.nearbystores.utils.NSToast;
import com.droideve.apps.nearbystores.utils.OfferUtils;
import com.droideve.apps.nearbystores.utils.TextUtils;
import com.droideve.apps.nearbystores.utils.Utils;
//import com.google.android.gms.ads.AdListener;
//import com.google.android.gms.ads.AdRequest;
//import com.google.android.gms.ads.AdView;
//import com.google.android.gms.ads.LoadAdError;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.nirhart.parallaxscroll.views.ParallaxScrollView;
import com.wuadam.awesomewebview.AwesomeWebView;

import org.bluecabin.textoo.LinksHandler;
import org.bluecabin.textoo.Textoo;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.RealmList;

import static com.droideve.apps.nearbystores.appconfig.AppConfig.APP_DEBUG;
import static com.droideve.apps.nearbystores.controllers.sessions.SessionsController.isLogged;

public class OfferDetailActivity extends SimpleActivity implements ViewManager.CustomView {


    @BindView(R.id.app_bar)
    Toolbar toolbar;
    @BindView(R.id.badge_category)
    TextView badge_category;
    @BindView(R.id.badge_price)
    TextView badge_price;
    @BindView(R.id.image)
    ImageView image;
    @BindView(R.id.scroll_view)
    ParallaxScrollView scrollView;
    @BindView(R.id.description_content)
    TextView description_content;
    @BindView(R.id.header_title)
    TextView header_title;
    @BindView(R.id.adsLayout)
    LinearLayout adsLayout;

    @BindView(R.id.offer_layout)
    LinearLayout offer_layout;

//    @BindView(R.id.adView)
//    AdView mAdView;
    @BindView(R.id.offer_up_to)
    TextView offer_up_to;
    @BindView(R.id.customStoreCV)
    StoreCardCustomView customStoreCV;

    //Bottom buttons declaration
    @BindView(R.id.bottomBtnGetCoupon)
    MaterialRippleLayout bottomBtnGetCoupon;
    @BindView(R.id.bottomBtnContact)
    MaterialRippleLayout bottomBtnContact;
    @BindView(R.id.bottomBtnAddToBookmarkBtn)
    MaterialRippleLayout bottomBtnAddToBookmarkBtn;

    @OnClick(R.id.bottomBtnContact)
    void onContactAction(){

        if(offerData == null)
            return;

        Store mStore = StoreController.findStoreById(offerData.getStore_id());

        if(mStore == null){
            Intent intentStore = new Intent(this, StoreDetailActivity.class);
            intentStore.putExtra("id", mStore.getId());
            startActivity(intentStore);
            return;
        }

        if(SessionsController.isLogged()
                && mStore.getCanChat() == 1
                && SettingsController.isModuleEnabled("messenger")
        ){

            Intent intent = new Intent(this, MessengerActivity.class);
            intent.putExtra("type", Discussion.DISCUSION_WITH_USER);
            intent.putExtra("userId", mStore.getUser_id());
            intent.putExtra("storeName", mStore.getName());
            startActivity(intent);

        }else if(!mStore.getPhone().trim().equals("")){

            try {

                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + mStore.getPhone().trim()));
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {

                    String[] permission = new String[]{Manifest.permission.CALL_PHONE};
                    SettingsController.requestPermissionM(this, permission);

                    return;
                }
                startActivity(intent);

            } catch (ActivityNotFoundException e) {
                Toast.makeText(getApplicationContext(), getString(R.string.store_call_error) + e.getMessage(), Toast.LENGTH_LONG).show();
            }

        }else if(!mStore.getWebsite().trim().equals("")){
            new AwesomeWebView.Builder(this)
                    .statusBarColorRes(R.color.colorPrimary)
                    .theme(R.style.FinestWebViewAppTheme)
                    .titleColor(ResourcesCompat.getColor(getResources(), R.color.colorWhite, null))
                    .urlColor(ResourcesCompat.getColor(getResources(), R.color.colorWhite, null))
                    .show(mStore.getWebsite());
        }

    }

    @OnClick(R.id.bottomBtnGetCoupon)
    void onGetCouponAction(){

        if (!isLogged()) {
            startActivity(new Intent(OfferDetailActivity.this, LoginV2Activity.class));
            return;
        }

        Offer offer = OffersController.findOfferById(offer_id);
        if(offer != null){
            this.setupOnClickCouponView(offer);
        }

    }


    @OnClick(R.id.bottomBtnAddToBookmarkBtn)
    void onBottomAddToBookmarkClick(View view) {

        bookMarkToggle();

    }


    private ViewManager mViewManager;
    private int offer_id = 0;
    private Offer offerData;
    private Menu mMenu;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(null);
        setContentView(R.layout.activity_detail_offer);

        //Bind views
        ButterKnife.bind(this);

        //setup view manager showError loading content
        setupViewManager();

        //contents of menu have changed, and menu should be redrawn.
        invalidateOptionsMenu();

        //set up views
        setupViews();

        //setup the ADMOB
        setupAdmob();

        //populating data
        listingOffersData();


    }



    @Override
    protected void onResume() {

//        if (mAdView != null)
//            mAdView.resume();

        super.onResume();
    }

    @Override
    protected void onPause() {
//
//        if (mAdView != null)
//            mAdView.pause();

        super.onPause();
    }

    @Override
    protected void onDestroy() {

//        if (mAdView != null)
//            mAdView.destroy();

        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.home_menu, menu);

        mMenu = menu;

        /////////////////////////////
        menu.findItem(R.id.bookmarks_icon).setVisible(false);

        /////////////////////////////
        menu.findItem(R.id.share_post).setVisible(true);
        Drawable send_location = new IconicsDrawable(this)
                .icon(CommunityMaterial.Icon2.cmd_share_variant)
                .color(ResourcesCompat.getColor(getResources(), R.color.white, null))
                .sizeDp(22);
        menu.findItem(R.id.share_post).setIcon(send_location);
        /////////////////////////////

        menu.findItem(R.id.report_icon).setVisible(true);


        return true;
    }

    private void setBookmarkIcons() {

        // Menu
        MenuItem bookmarksItemMenu = mMenu.findItem(R.id.bookmarks_icon);
        if (bookmarksItemMenu != null) {
            if ((SessionsController.isLogged() && offerData.getSaved() > 0)) {
                Drawable ic_fav = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_favourite, null);
                ic_fav.setTint(ResourcesCompat.getColor(getResources(), R.color.white, null));
                bookmarksItemMenu.setIcon(ic_fav);
            } else {
                Drawable ic_fav = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_favourite_outline, null);
                ic_fav.setTint(ResourcesCompat.getColor(getResources(), R.color.white, null));
                bookmarksItemMenu.setIcon(ic_fav);
            }
        }

        //Bottom Icon
        if ( (SessionsController.isLogged() && offerData.getSaved() > 0)) {
            Drawable ic_fav = ResourcesCompat.getDrawable(getResources(),R.drawable.ic_favourite,null);
            ic_fav.setTint(ResourcesCompat.getColor(getResources(),R.color.favourite_color,null));
            ((ImageView)bottomBtnAddToBookmarkBtn.findViewById(R.id.bottomBtnAddToBookmarkIcon)).setImageDrawable(ic_fav);
        } else {
            Drawable ic_fav = ResourcesCompat.getDrawable(getResources(),R.drawable.ic_favourite_outline,null);
            ic_fav.setTint(ResourcesCompat.getColor(getResources(),R.color.favourite_color,null));
            ((ImageView)bottomBtnAddToBookmarkBtn.findViewById(R.id.bottomBtnAddToBookmarkIcon)).setImageDrawable(ic_fav);
        }

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

    }

    private void setupHeader() {

        //setup all badge
        setupBadges();

    }

    private void setupBadges() {


        Drawable badge_closed_background = badge_price.getBackground();
        if (badge_closed_background instanceof ShapeDrawable) {
            ((ShapeDrawable) badge_closed_background).getPaint().setColor(ContextCompat.getColor(this, R.color.colorPromo));
        } else if (badge_closed_background instanceof GradientDrawable) {
            ((GradientDrawable) badge_closed_background).setColor(ContextCompat.getColor(this, R.color.colorPromo));
        } else if (badge_closed_background instanceof ColorDrawable) {
            ((ColorDrawable) badge_closed_background).setColor(ContextCompat.getColor(this, R.color.colorPromo));
        }


    }


    private void updateCategoryBadge(String title, String color_hex) {

        badge_category.setVisibility(View.VISIBLE);

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
                scrollView.setEnabled(true);
            }

            @Override
            public void onErrorShown() {

            }

            @Override
            public void onEmptyShown() {
                scrollView.setEnabled(false);
            }

            @Override
            public void onLoadingShown() {

            }
        });

        mViewManager.showLoading();

    }


    private void listingOffersData() {

        try {


            //get it from external url (deep linking)
            if (offer_id == 0) {
                try {

                    Intent appLinkIntent = getIntent();
                    String appLinkAction = appLinkIntent.getAction();
                    Uri appLinkData = appLinkIntent.getData();

                    if (appLinkAction != null && appLinkAction.equals(Intent.ACTION_VIEW)) {
                        offer_id = Utils.dp_get_id_from_url(appLinkData.toString(), Constances.ModulesConfig.OFFER_MODULE);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (offer_id == 0) {
                offer_id = getIntent().getExtras().getInt("offer_id");
            }

            if (offer_id == 0) {
                offer_id = getIntent().getExtras().getInt("id");
            }


            if (offer_id == 0) {
                offer_id = Integer.parseInt(Objects.requireNonNull(getIntent().getExtras().getString("id")));
            }

        } catch (Exception e) {
            e.printStackTrace();
            finish();
        }


        final Offer mOffer = OffersController.getOffer(offer_id);

        //GET DATA FROM API IF NETWORK IS AVAILABLE
        if (ServiceHandler.isNetworkAvailable(this)) {
            syncOffer(offer_id);
        } else {
            if (mOffer != null && mOffer.isLoaded() && mOffer.isValid()) {

                mViewManager.showContent();
                setupComponents(mOffer);
                offerData = mOffer;
            }
        }



        /*
         *
         *   DATE & COUNTDOWN
         *
         */

        String date = "";


        try {
            date = mOffer.getDate_start();
            date = DateUtils.prepareOutputDate(date, "dd MMMM yyyy  hh:mm", this);
        } catch (Exception e) {
            syncOffer(offer_id);
            return;
        }


    }


    private void setupComponents(final Offer mOffer) {
        offerData = mOffer;

        getAppBarTitle().setText(mOffer.getName());
        header_title.setText(mOffer.getName());


        if (mOffer.getValue_type().equalsIgnoreCase("Percent") && (mOffer.getOffer_value() > 0 || mOffer.getOffer_value() < 0)) {
            DecimalFormat decimalFormat = new DecimalFormat("#0");
            badge_price.setText(decimalFormat.format(mOffer.getOffer_value()) + "%");
        } else {
            if (mOffer.getValue_type().equalsIgnoreCase("Price") && mOffer.getOffer_value() != 0) {

                badge_price.setText(OfferUtils.parseCurrencyFormat(
                        mOffer.getOffer_value(),
                        mOffer.getCurrency()));

            } else {
                badge_price.setText(getString(R.string.promo));
            }
        }

        badge_price.setVisibility(View.VISIBLE);

        if (mOffer.getImages() != null){

            Glide.with(getBaseContext())
                    .asBitmap()
                    .load(getBaseContext()).load(mOffer.getImages().getUrl500_500())
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap bitmap,
                                                    Transition<? super Bitmap> transition) {
                            int w =  bitmap.getWidth();
                            int h = bitmap.getHeight();

                            if(h < w ){
                                h = w;
                            }

                            float ratio = (float) w/h;
                            image.setImageBitmap(bitmap);

                            //setup scroll with header
                            setupScrollNHeaderCustomized(
                                    scrollView,
                                    (LinearLayout) findViewById(R.id.header_detail),
                                    ratio,
                                    null
                            );

                        }
                    });
        }



        description_content.setText(mOffer.getDescription());
        new TextUtils.decodeHtml(description_content).execute(mOffer.getDescription());

        Textoo
                .config(description_content)
                .linkifyWebUrls()  // or just .linkifyAll()
                .addLinksHandler(new LinksHandler() {
                    @Override
                    public boolean onClick(View view, String url) {

                        if (Utils.isValidURL(url)) {

                            new AwesomeWebView.Builder(OfferDetailActivity.this)
                                    .showMenuOpenWith(false)
                                    .statusBarColorRes(R.color.colorAccent)
                                    .theme(R.style.FinestWebViewAppTheme)
                                    .titleColor(
                                            ResourcesCompat.getColor(getResources(), R.color.white, null)
                                    ).urlColor(
                                    ResourcesCompat.getColor(getResources(), R.color.white, null)
                            ).show(url);

                            return true;
                        } else {
                            return false;
                        }
                    }
                })
                .apply();

        try {

            int cid = Integer.parseInt(getIntent().getExtras().getString("cid"));
            CampagneController.markView(cid);
        } catch (Exception e) {
            if (AppConfig.APP_DEBUG)
                e.printStackTrace();
        }

        //setup offer date
        setupOfferDate(mOffer);


        if (mOffer.getStore_id() > 0 && StoreController.getStore(mOffer.getStore_id()) != null) {
            Store storeOffers = StoreController.getStore(mOffer.getStore_id());
            customStoreCV.setupComponent(storeOffers);
            updateCategoryBadge(storeOffers.getCategory_name(), storeOffers.getCategory_color());
        } else {

            customStoreCV.loadData(mOffer.getStore_id(), false, new StoreCardCustomView.StoreListener() {
                @Override
                public void onLoaded(Store object) {
                    updateCategoryBadge(object.getCategory_name(), object.getCategory_color());
                }
            });

        }


        //setup menu icons
        setBookmarkIcons();


        //setup bottom buttons
        setupBottomButtons();

    }

    private void setupBottomButtons(){



        if(SettingsController.isModuleEnabled("qrcoupon")
                && offerData != null
                && offerData.getCoupon_config() != null
                && !offerData.getCoupon_config().equals("disabled")){

            bottomBtnGetCoupon.setVisibility(View.VISIBLE);
            bottomBtnContact.setVisibility(View.GONE);

            if( offerData != null && offerData.getHasGotCoupon()>0 ){
                ((AppCompatButton)bottomBtnGetCoupon.findViewById(R.id.subButtonGetCoupon)).setText(getString(R.string.show_coupon));
                ((AppCompatButton)bottomBtnGetCoupon.findViewById(R.id.subButtonGetCoupon)).setBackgroundDrawable(ResourcesCompat.getDrawable(getResources(),R.drawable.rounded_button_outline_3dp, null));
                ((AppCompatButton)bottomBtnGetCoupon.findViewById(R.id.subButtonGetCoupon)).setTextColor(ResourcesCompat.getColor(getResources(),R.color.colorPrimary, null));
            }else{
                ((AppCompatButton)bottomBtnGetCoupon.findViewById(R.id.subButtonGetCoupon)).setText(getString(R.string.get_coupon_code));
                ((AppCompatButton)bottomBtnGetCoupon.findViewById(R.id.subButtonGetCoupon)).setBackgroundDrawable(ResourcesCompat.getDrawable(getResources(),R.drawable.rounded_button_3dp, null));
                ((AppCompatButton)bottomBtnGetCoupon.findViewById(R.id.subButtonGetCoupon)).setTextColor(ResourcesCompat.getColor(getResources(),R.color.white, null));
            }

        }else if(SettingsController.isModuleEnabled("qrcoupon")
                && offerData != null
                && offerData.getCoupon_config() != null
                && !offerData.getCoupon_config().equals("disabled")
                && offerData.getCoupon_redeem_limit() == 0){


            bottomBtnGetCoupon.setVisibility(View.VISIBLE);
            bottomBtnContact.setVisibility(View.GONE);

            ((AppCompatButton)bottomBtnGetCoupon.findViewById(R.id.subButtonGetCoupon)).setText(getString(R.string.limited));
            ((AppCompatButton)bottomBtnGetCoupon.findViewById(R.id.subButtonGetCoupon)).setEnabled(false);

        }else{
            bottomBtnGetCoupon.setVisibility(View.GONE);
            bottomBtnContact.setVisibility(View.VISIBLE);
        }

    }

    private void setupOnClickCouponView(Offer mOffer) {

        if(!SettingsController.isModuleEnabled("qrcoupon"))
            return;

        //display button
        if(mOffer.getCoupon_config() == null || mOffer.getCoupon_config().equals("disabled"))
            return;

        if(mOffer.getCoupon_config() != null && mOffer.getCoupon_redeem_limit()==0){
            NSToast.show(getString(R.string.limit_execeeded));
            return;
        }

        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_offer_coupon_layout);
        bottomSheetDialog.show();

        QRCouponView mQrcouponView = (QRCouponView)bottomSheetDialog.findViewById(R.id.qrcouponView);
        mQrcouponView.setup().load(mOffer.getId());

    }

    @SuppressLint("StringFormatMatches")
    private void setupOfferDate(Offer mOffer) {

        String dateStartAt = "";
        String dateEndAt = "";

        try {
            dateStartAt = mOffer.getDate_start();
            dateStartAt = DateUtils.prepareOutputDate(dateStartAt, "dd MMMM yyyy", this);
        } catch (Exception e) {
            return;
        }

        try {
            dateEndAt = mOffer.getDate_end();
            dateEndAt = DateUtils.prepareOutputDate(dateEndAt, "dd MMMM yyyy", this);
        } catch (Exception e) {
            return;
        }

        String inputDateSatrt = DateUtils.prepareOutputDate(mOffer.getDate_start(), "yyyy-MM-dd HH:mm:ss", this);
        long diff_Will_Start = DateUtils.getDiff(inputDateSatrt, "yyyy-MM-dd HH:mm:ss");

        if (diff_Will_Start > 0) {
            offer_up_to.setText(String.format(getString(R.string.offer_start_at), dateStartAt));
            if (dateStartAt != null && dateStartAt.equals("null")) {
                offer_layout.setVisibility(View.GONE);
            }
        }

        String inputDateEnd = DateUtils.prepareOutputDate(mOffer.getDate_end(), "yyyy-MM-dd HH:mm:ss", this);
        long diff_will_end = DateUtils.getDiff(inputDateEnd, "yyyy-MM-dd HH:mm:ss");

        if (diff_will_end > 0 && diff_Will_Start < 0) {
            if (dateEndAt == null || dateEndAt.equals("null")) {
                offer_layout.setVisibility(View.GONE);
            } else {
                offer_up_to.setText(String.format(getString(R.string.offer_end_at), dateEndAt));
                offer_layout.setVisibility(View.VISIBLE);
            }
        }

        if (diff_Will_Start < 0 && diff_will_end < 0) {
            offer_layout.setVisibility(View.VISIBLE);
            offer_up_to.setText(String.format(getString(R.string.offer_ended_at), dateEndAt));
        }

        Drawable offerCalendar = new IconicsDrawable(this)
                .icon(CommunityMaterial.Icon.cmd_calendar)
                .color(ResourcesCompat.getColor(getResources(), R.color.colorAccent, null))
                .sizeDp(18);
        offer_up_to.setTextColor(ResourcesCompat.getColor(getResources(), R.color.colorAccent, null));
        if (AppController.isRTL()) {
            offer_up_to.setCompoundDrawables(null, null, offerCalendar, null);
        } else {
            offer_up_to.setCompoundDrawables(offerCalendar, null, null, null);
        }

        offer_up_to.setCompoundDrawablePadding(20);


    }


    private void syncOffer(final int offer_id) {

        mViewManager.showLoading();

        final GPStracker mGPS = new GPStracker(this);

        SimpleRequest request = new SimpleRequest(Request.Method.POST,
                Constances.API.API_GET_OFFERS, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {


                try {

                    if (APP_DEBUG) {
                        NSLog.e("responseOffersString", response);
                    }

                    JSONObject jsonObject = new JSONObject(response);
                    final OfferParser mOfferParser = new OfferParser(jsonObject);
                    RealmList<Offer> list = mOfferParser.getOffers();

                    if (list.size() > 0) {

                        mViewManager.showContent();
                        OffersController.insertOffers(list);
                        setupComponents(list.get(0));



                    } else {

                        Toast.makeText(OfferDetailActivity.this, getString(R.string.store_not_found), Toast.LENGTH_LONG).show();
                        finish();

                    }

                } catch (JSONException e) {
                    //send a rapport to support
                    if (APP_DEBUG)
                        e.printStackTrace();

                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (APP_DEBUG) {
                    NSLog.e("ERROR", error.toString());
                }
                mViewManager.showError();

            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();

                if (mGPS.canGetLocation()) {
                    params.put("lat", mGPS.getLatitude() + "");
                    params.put("lng", mGPS.getLongitude() + "");
                }

                params.put("limit", "1");
                params.put("offer_id", offer_id + "");

                if (APP_DEBUG) {
                    NSLog.e("ListStoreFragment", "  params getOffers :" + params.toString());
                }

                return params;
            }

        };


        request.setRetryPolicy(new DefaultRetryPolicy(SimpleRequest.TIME_OUT,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        VolleySingleton.getInstance(this).getRequestQueue().add(request);

    }


    private void setupAdmob() {

        if (AppConfig.SHOW_ADS) {

//            mAdView = findViewById(R.id.adView);
//            mAdView.setVisibility(View.VISIBLE);
//            AdRequest adRequest = new AdRequest.Builder().build();
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
//                    findViewById(R.id.adsLayout).setVisibility(View.GONE);
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
//                    findViewById(R.id.adsLayout).setVisibility(View.VISIBLE);
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
            findViewById(R.id.adsLayout).setVisibility(View.GONE);




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
    public void onBackPressed() {
        super.onBackPressed();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (android.R.id.home == item.getItemId()) {
            if (!MainActivity.isOpend()) {
                startActivity(new Intent(this, MainActivity.class));
            }
            finish();
        } else if (item.getItemId() == R.id.bookmarks_icon) {

            bookMarkToggle();

        } else if (item.getItemId() == R.id.map_action) {

            startActivity(new Intent(this, MapStoresListActivity.class));
            

        } else if (item.getItemId() == R.id.share_post) {


            @SuppressLint({"StringFormatInvalid", "LocalSuppress", "StringFormatMatches"}) String shared_text =
                    String.format(getString(R.string.shared_text),
                            offerData.getName(),
                            getString(R.string.app_name),
                            offerData.getLink()
                    );

            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, shared_text);
            sendIntent.setType("text/plain");
            startActivity(sendIntent);

        } else if(item.getItemId() == R.id.report_icon){

            if(!SessionsController.isLogged()){
                startActivity(new Intent(OfferDetailActivity.this, LoginV2Activity.class));
                
            }else{

                Intent intent = new Intent(OfferDetailActivity.this, ReportIssueActivity.class);
                intent.putExtra("id", offerData.getId());
                intent.putExtra("name", offerData.getName());
                intent.putExtra("link", offerData.getLink());
                intent.putExtra("owner_id", offerData.getUser_id());
                intent.putExtra("module", "offer");
                startActivity(intent);
            }



        }


        return super.onOptionsItemSelected(item);

    }

    private void bookMarkToggle(){
        if (!isLogged()) {
            startActivity(new Intent(OfferDetailActivity.this, LoginV2Activity.class));
            return;
        }
        try {
            User currentUser = SessionsController.getSession().getUser();
            if (offerData.getSaved() > 0) {
                removeOfferToBookmarks(this, currentUser.getId(), offerData.getId());
            } else {
                saveOfferToBookmarks(this, currentUser.getId(), offerData.getId());
            }
        } catch (Exception e) {
            //send a rapport to support
            if (AppConfig.APP_DEBUG) e.printStackTrace();
        }
    }

    public void removeOfferToBookmarks(final Context context, final int user_id, final int int_id) {

        ApiRequest.newPostInstance(Constances.API.API_BOOKMARK_OFFER_REMOVE, new ApiRequestListeners() {
            @Override
            public void onSuccess(Parser parser) {
                if (parser.getSuccess() == 1) {
                    offerData = OffersController.doSave(offerData.getId(), 0);
                    if (offerData != null) {
                        setBookmarkIcons();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), R.string.something_went_wrong, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFail(Map<String, String> errors) {

            }
        }, Map.of(
                "user_id", String.valueOf(user_id),
                "offer_id", String.valueOf(int_id)
        ));
    }

    public void saveOfferToBookmarks(final Context context, final int user_id, final int int_id) {

        ApiRequest.newPostInstance(Constances.API.API_BOOKMARK_OFFER_SAVE, new ApiRequestListeners() {
            @Override
            public void onSuccess(Parser parser) {
                if (parser.getSuccess() == 1) {
                    offerData = OffersController.doSave(offerData.getId(), 1);
                    if (offerData != null) {
                        setBookmarkIcons();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), R.string.something_went_wrong, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFail(Map<String, String> errors) {

            }
        }, Map.of(
                "user_id", String.valueOf(user_id),
                "offer_id", String.valueOf(int_id)
        ));


    }





}
