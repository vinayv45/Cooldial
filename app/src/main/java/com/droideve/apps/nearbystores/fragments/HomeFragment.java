package com.droideve.apps.nearbystores.fragments;


import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.droideve.apps.nearbystores.activities.LoginV2Activity;
import com.droideve.apps.nearbystores.activities.MyQrCodeActivity;
import com.droideve.apps.nearbystores.animation.Animation;
import com.droideve.apps.nearbystores.booking.controllers.services.GenericNotifyEvent;
import com.droideve.apps.nearbystores.business_manager.views.activities.BusinessManagerWebViewActivity;
import com.droideve.apps.nearbystores.classes.Setting;
import com.droideve.apps.nearbystores.controllers.SettingsController;
import com.droideve.apps.nearbystores.controllers.sessions.SessionsController;
import com.droideve.apps.nearbystores.location.LocationSettingPopup;
import com.droideve.apps.nearbystores.location.UserSettingLocation;
import com.droideve.apps.nearbystores.utils.NSLog;

import android.os.Handler;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.droideve.apps.nearbystores.R;
import com.droideve.apps.nearbystores.Services.BusStation;
import com.droideve.apps.nearbystores.activities.CustomSearchActivity;
import com.droideve.apps.nearbystores.activities.PeopleListActivity;
import com.droideve.apps.nearbystores.appconfig.AppConfig;
import com.droideve.apps.nearbystores.appconfig.Constances;
import com.droideve.apps.nearbystores.customView.CategoryCustomView;
import com.droideve.apps.nearbystores.customView.EventCustomView;
import com.droideve.apps.nearbystores.customView.OfferCustomView;
import com.droideve.apps.nearbystores.customView.PeopleCustomView;
import com.droideve.apps.nearbystores.customView.SliderCustomView;
import com.droideve.apps.nearbystores.customView.StoreCustomView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class HomeFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    public final static String TAG = "homefragment";

    //binding
    @BindView(R.id.mScroll)
    NestedScrollView mNestedScrollView;
    @BindView(R.id.refresh)
    SwipeRefreshLayout refresh;


    @BindView(R.id.business_manager_widget)
    CardView business_manager_widget;

    private View rootview;
    private Listener mListener;

    // newInstance constructor for creating fragment with arguments
    public static HomeFragment newInstance(int page, String title) {
        HomeFragment fragmentFirst = new HomeFragment();
        Bundle args = new Bundle();
        args.putInt("id", page);
        args.putString("title", title);
        fragmentFirst.setArguments(args);
        return fragmentFirst;
    }

    private void setupRefresListener() {
        refresh.setOnRefreshListener(this);
        refresh.setColorSchemeResources(
                R.color.colorAccent,
                R.color.colorAccent,
                R.color.colorAccent,
                R.color.colorAccent
        );
    }

    private void setup_header(View rootview) {

        //setup location picker
        setupLocationPicker();

        //Open Navigation Drawer
        ImageView navigation = rootview.findViewById(R.id.navigation);
        navigation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BusStation.getBus().post(new NavigationDrawerFragment.NavigationDrawerEvent(1));
            }
        });

        //Open filter activity
        rootview.findViewById(R.id.rlFilter).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), CustomSearchActivity.class));
            }
        });

        //Open QR code activity
        rootview.findViewById(R.id.myQrCodeAction).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SessionsController.isLogged())
                    startActivity(new Intent(getActivity(), MyQrCodeActivity.class));
                else
                    startActivity(new Intent(getActivity(), LoginV2Activity.class));
            }
        });

        //search bar listeners
        ((EditText) rootview.findViewById(R.id.searchInput)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((EditText) v).requestFocus();
                ((EditText) v).setImeActionLabel("Search", EditorInfo.IME_ACTION_UNSPECIFIED);
                ((EditText) rootview.findViewById(R.id.searchInput)).setFocusableInTouchMode(true);
                ((EditText) rootview.findViewById(R.id.searchInput)).setFocusable(true);
            }
        });


        //editor listner
        ((EditText) rootview.findViewById(R.id.searchInput)).setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    performSearch();
                    return true;
                }
                return false;
            }
        });


    }

    //Header: open result interface
    private void performSearch() {

        HashMap<String, Object> searchParams = new HashMap<>();
        searchParams.put("search", ((EditText) rootview.findViewById(R.id.searchInput)).getText().toString().trim());
        searchParams.put("module", "store"); //assign module
        CustomSearchFragment.showResultFilter(getActivity(), searchParams);

    }

    //Header: setup location picker
    private void setupLocationPicker() {

        ((Button) rootview.findViewById(R.id.locationPicker)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EventBus.getDefault().post(new GenericNotifyEvent(LocationSettingPopup.LOCATION_PICKED));
            }
        });

        //update location view
        updateLocationPickerView();

    }

    private void updateLocationPickerView() {

        //check if enabled current location and update views
        if (UserSettingLocation.getLoc().isCurrentLoc()) {

            //sync with server to get current loc name
            getCurrentLocation();

            //put default location name
            ((Button) rootview.findViewById(R.id.locationPicker)).setText(getString(R.string.current_location));
            return;
        }

        ((Button) rootview.findViewById(R.id.locationPicker)).setText(UserSettingLocation.getLoc().getLoc_name());

    }


    @Subscribe
    public void onMessageEvent(GenericNotifyEvent event) {
        //receive event click for update locaition
        if (event.message != null && event.message.equals(LocationSettingPopup.LOCATION_CHANGED)) {
            onRefresh();
        }
    }

    private void getCurrentLocation() {

        if (UserSettingLocation.getLoc().isCurrentLoc()) {

        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.v3_fragment_home, container, false);

        rootview = rootView.getRootView();

        //init butterKnife library
        ButterKnife.bind(this, rootview);

        /*
         * Setup home component
         */
        initViews();

        return rootview;
    }


    private void initViews() {

        //categories
        initCategoryRV(rootview);

        //slider
        initSliderCustomView(rootview);

        //featured store list
        initStorsFeatured(rootview);

        //nearby store list
        initStoreRV(rootview);

        //recent offer list
        initRecentOffersRV(rootview);

        //nearby offer list
        initNearbyOfferRV(rootview);

        //BusinessMaanger banner
        setupBM_BannerWidget();

        //event list
        initUpcomingEventRv(rootview);

        //users list
        initPeopleRV(rootview);

        //setup scroll list
        setupScroll();

        //refresh effect
        setupRefresListener();

        //setup header
        setup_header(rootview);

    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    private void setupScroll() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mNestedScrollView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
                @Override
                public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                    NSLog.e(getTag(), scrollX + " - " + scrollY);

                    if (mListener != null)
                        mListener.onScroll(scrollX, scrollY);
                }
            });
        }
    }

    private void initSliderCustomView(View view) {

        SliderCustomView mSliderCustomView = view.findViewById(R.id.sliderCV);
        mSliderCustomView.loadData(false);
        mSliderCustomView.startAutoSlider();
        mSliderCustomView.show();

    }



    //Home categories
    private void initCategoryRV(View view) {
        //ImageSlider
        CategoryCustomView mCategoryCustomView = view.findViewById(R.id.rectCategoryList);
        mCategoryCustomView.loadData(false);
        mCategoryCustomView.show();
    }

    //Discoiver Nearby Places
    private void initStoreRV(View view) {

        StoreCustomView mStoreCustomView = view.findViewById(R.id.horizontalStoreList);
        mStoreCustomView.loadData(false, new HashMap<>());
        mStoreCustomView.show();
    }


    private void initStorsFeatured(View view) {

        StoreCustomView mStoreCustomView = view.findViewById(R.id.featuredStores);

        HashMap<String, Object> params = new HashMap<>();
        params.put("is_featured",1);
        mStoreCustomView.loadData(false, params);
        mStoreCustomView.show();
    }

    //Discoiver Nearby Places
    private void initFeaturedStores(View view) {

        StoreCustomView mStoreCustomView = view.findViewById(R.id.horizontalFeaturedStores);
        HashMap<String, Object> optionalParams = new HashMap<>();
        optionalParams.put("order_by", Constances.OrderByFilter.TOP_RATED);

        mStoreCustomView.loadData(false, optionalParams);
        mStoreCustomView.show();

    }

    private void initRecentOffersRV(View view) {
        OfferCustomView mOfferCustomView = view.findViewById(R.id.recentOffersList);

        HashMap<String, Object> optionalParams = new HashMap<>();
        optionalParams.put("order_by", Constances.OrderByFilter.RECENT);

        mOfferCustomView.loadData(false, optionalParams);
        mOfferCustomView.show();
    }

    private void initNearbyOfferRV(View view) {

        OfferCustomView mOfferCustomView = view.findViewById(R.id.nearbyOfferList);

        HashMap<String, Object> optionalParams = new HashMap<>();
        optionalParams.put("order_by", Constances.OrderByFilter.NEARBY);

        mOfferCustomView.loadData(false, optionalParams);
        mOfferCustomView.show();

    }

    private void initPeopleRV(View view) {

        PeopleCustomView mPeopleCustomView = view.findViewById(R.id.horizontalPeopleList);
        mPeopleCustomView.loadData(false);
        view.findViewById(R.id.card_show_more).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), PeopleListActivity.class));

            }
        });

        if (AppConfig.ENABLE_PEOPLE_AROUND_ME) {
            mPeopleCustomView.show();
        } else {
            mPeopleCustomView.hide();
        }


    }



    private void setupBM_BannerWidget() {

        if(!SettingsController.isModuleEnabled("business_manager")){
            business_manager_widget.setVisibility(View.GONE);
            return;
        }

        (new Handler()).postDelayed(new Runnable() {
            @Override
            public void run() {

                ((TextView)business_manager_widget.findViewById(R.id.business_manager_widget_title))
                        .setText(String.format(getContext().getResources().getString(R.string.business_manager_widget_title), getResources().getString(R.string.app_name)));

                Animation.startZoomEffect(business_manager_widget);

                business_manager_widget.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getActivity(), BusinessManagerWebViewActivity.class);
                        startActivity(intent);
                    }
                });
            }
        }, 2000);


    }

    private void initUpcomingEventRv(View view) {
        EventCustomView mEventCustomView = view.findViewById(R.id.horizontalUpcomingEventList);

        HashMap<String, Object> optionalParams = new HashMap<>();
        optionalParams.put("order_by", Constances.OrderByFilter.UPCOMING);
        mEventCustomView.loadData(false,optionalParams);
        mEventCustomView.show();
    }

    @Override
    public void onRefresh() {

        initViews();

        refresh.setRefreshing(false);

    }

    public void setListener(final Listener mItemListener) {
        this.mListener = mItemListener;
    }

    public interface Listener {
        void onScroll(int scrollX, int scrollY);
    }


}
