package com.droideve.apps.nearbystores.fragments;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.droideve.apps.nearbystores.activities.LoginV2Activity;
import com.droideve.apps.nearbystores.activities.MessengerActivity;
import com.droideve.apps.nearbystores.activities.MyCouponsListActivity;
import com.droideve.apps.nearbystores.activities.ProfileActivity;
import com.droideve.apps.nearbystores.business_manager.views.activities.BusinessManagerWebViewActivity;
import com.droideve.apps.nearbystores.controllers.SettingsController;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.droideve.apps.nearbystores.R;
import com.droideve.apps.nearbystores.Services.BusStation;
import com.droideve.apps.nearbystores.activities.AboutActivity;
import com.droideve.apps.nearbystores.activities.CustomSearchActivity;
import com.droideve.apps.nearbystores.activities.MapStoresListActivity;
import com.droideve.apps.nearbystores.activities.SettingActivity;
import com.droideve.apps.nearbystores.activities.SplashActivity;
import com.droideve.apps.nearbystores.adapter.navigation.SimpleListAdapterNavDrawer;
import com.droideve.apps.nearbystores.appconfig.AppConfig;
import com.droideve.apps.nearbystores.booking.views.activities.BookingListActivity;
import com.droideve.apps.nearbystores.classes.HeaderItem;
import com.droideve.apps.nearbystores.classes.Item;
import com.droideve.apps.nearbystores.controllers.notification.NotificationController;
import com.droideve.apps.nearbystores.controllers.sessions.SessionsController;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;

import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class NavigationDrawerFragment extends Fragment implements SimpleListAdapterNavDrawer.ClickListener {

    //save instance of Navigation State
    public final String PREF_FILE_NAME = "testpref";
    public final String KEY_USER_LEARNED_DRAWER = "learned_user_drawer";


    //code of chat box
    public int INT_CHAT_BOX = 5;

    //navigation drawer
    private static DrawerLayout mDrawerLayout;

    //list items
    private List<Item> listItems = Arrays.asList();

    //action bar
    private ActionBarDrawerToggle mActionBarDrawerToggle;

    //other variables
    private boolean mUserLearedLayout;
    private boolean mFromSaveInstanceState;
    private View containerView;

    //Receycler view (list view)
    private RecyclerView drawerList;

    //init request http
    private SimpleListAdapterNavDrawer adapter;

    public static DrawerLayout getInstance() {
        return mDrawerLayout;
    }

    public void saveToPreferences(Context context, String preferenceName, String preferenceValue) {

        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putString(preferenceName, preferenceValue);
        edit.apply();

    }

    public String readFromPreferences(Context context, String preferenceName, String defaultValue) {

        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(preferenceName, defaultValue);

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mUserLearedLayout = Boolean.valueOf(readFromPreferences(getActivity(), KEY_USER_LEARNED_DRAWER, "false"));
        if (savedInstanceState != null) {
            mFromSaveInstanceState = true;
        }


    }

    @SuppressLint("WrongConstant")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.navigation_drawer_content, container, false);

        rootView.setClickable(true);

        drawerList = rootView.findViewById(R.id.drawerLayout);
        drawerList.setVisibility(View.VISIBLE);

        adapter = new SimpleListAdapterNavDrawer(getActivity(), getData());

        drawerList.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        drawerList.setLayoutManager(mLayoutManager);
        drawerList.setAdapter(adapter);

        adapter.setClickListener(this);

        return rootView;

    }

    public List<Item> getData() {

        listItems = new ArrayList<Item>();


        HeaderItem header_item = new HeaderItem();
        header_item.setName(getResources().getString(R.string.Home));
        header_item.setEnabled(true);
        if (header_item.isEnabled())
            listItems.add(header_item);

        //HOME
        Item homeItem = new Item();
        homeItem.setName(getResources().getString(R.string.Home));
        homeItem.setIconDraw(CommunityMaterial.Icon2.cmd_home_outline);
        homeItem.setID(Menu.HOME_ID);
        if (homeItem.isEnabled())
            listItems.add(homeItem);



        //GEO Stores
        Item mapStoresItem = new Item();
        mapStoresItem.setName(getResources().getString(R.string.MapStoresMenu));
        mapStoresItem.setIconDraw(CommunityMaterial.Icon2.cmd_map_outline);
        mapStoresItem.setID(Menu.MAP_STORES);
        listItems.add(mapStoresItem);

        //Qr Coupon
        if (SettingsController.isModuleEnabled("qrcoupon")) {
            Item mycoupons = new Item();
            mycoupons.setName(getResources().getString(R.string.my_coupons));
            mycoupons.setIconDraw(CommunityMaterial.Icon2.cmd_percent);
            mycoupons.setID(Menu.MY_COUPONS);
            listItems.add(mycoupons);
        }

        //Booking
        if (SettingsController.isModuleEnabled("booking")) {
            Item bookingItem = new Item();
            bookingItem.setName(getResources().getString(R.string.my_bookings));
            bookingItem.setIconDraw(CommunityMaterial.Icon.cmd_calendar);
            bookingItem.setID(Menu.MY_BOOKING);
            if (bookingItem.isEnabled())
                listItems.add(bookingItem);
        }

        //business manager
        if (SettingsController.isModuleEnabled("business_manager")) {
            Item webdashboard = new Item();
            webdashboard.setName(getResources().getString(R.string.ManageThings));
            webdashboard.setIconDraw(CommunityMaterial.Icon.cmd_briefcase_outline);
            webdashboard.setID(Menu.MANAGE_YOUR_BUSINESS);
            listItems.add(webdashboard);
        }

        //Account
        Item logout = new Item();
        logout.setName(getResources().getString(R.string.my_account));
        logout.setIconDraw(CommunityMaterial.Icon.cmd_account_outline);
        logout.setID(Menu.ACCOUNT);
        listItems.add(logout);


        //About US
        Item aboutItem = new Item();
        aboutItem.setName(getResources().getString(R.string.about));
        aboutItem.setIconDraw(CommunityMaterial.Icon2.cmd_information_outline);
        aboutItem.setID(Menu.ABOUT);

        if (aboutItem.isEnabled())
            listItems.add(aboutItem);

        //Settings
        Item settingItem = new Item();
        settingItem.setName(getResources().getString(R.string.Settings));
        settingItem.setIconDraw(CommunityMaterial.Icon2.cmd_settings_outline);
        settingItem.setID(Menu.SETTING);
        if (settingItem.isEnabled())
            listItems.add(settingItem);


        return listItems;
    }

    public void setUp(int FragId, DrawerLayout drawerlayout, final Toolbar toolbar) {

        containerView = getView().findViewById(FragId);
        mDrawerLayout = drawerlayout;

        mActionBarDrawerToggle = new ActionBarDrawerToggle(
                getActivity(),
                drawerlayout,
                toolbar,
                R.string.drawer_open,
                R.string.drawer_close
        ) {
            @Override
            public void onDrawerOpened(View drawerView) {

                super.onDrawerOpened(drawerView);
                if (!mUserLearedLayout) {
                    mUserLearedLayout = true;
                    saveToPreferences(getActivity(), KEY_USER_LEARNED_DRAWER, mUserLearedLayout + "");
                }


                getActivity().invalidateOptionsMenu();

            }

            @Override
            public void onDrawerClosed(View drawerView) {

                super.onDrawerClosed(drawerView);
                getActivity().invalidateOptionsMenu();

            }


            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);

            }
        };

        if (!mUserLearedLayout && !mFromSaveInstanceState) {
            mDrawerLayout.closeDrawer(containerView);
        }


        mDrawerLayout.setDrawerListener(mActionBarDrawerToggle);
        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mActionBarDrawerToggle.syncState();

            }
        });

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == INT_CHAT_BOX) {

            adapter.getData().get(1).setNotify(0);
            adapter.update(1, adapter.getData().get(1));

        }
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void itemClicked(View view, int position) {

        Item item = adapter.getData().get(position);
        if (item instanceof Item) {

            switch (item.getID()) {
                case Menu.HOME_ID:
                    V2MainFragment mf = (V2MainFragment) getFragmentManager().findFragmentByTag(V2MainFragment.TAG);
                    mf.setCurrentFragment(0);
                    break;
                case Menu.MANAGE_YOUR_BUSINESS:
                    Intent intent = new Intent(getActivity(), BusinessManagerWebViewActivity.class);
                    startActivity(intent);
                    break;
                case Menu.MY_BOOKING:
                    if (SessionsController.isLogged()) {
                        Intent bookingIntent = new Intent(getActivity(), BookingListActivity.class);
                        startActivity(bookingIntent);
                    } else {
                        startActivity(new Intent(getActivity(), LoginV2Activity.class));
                    }
                    break;
                case Menu.ABOUT:
                    startActivity(new Intent(getActivity(), AboutActivity.class));
                    break;
                case Menu.SETTING:
                    startActivity(new Intent(getActivity(), SettingActivity.class));
                    break;
                case Menu.MAP_STORES:
                    startActivity(new Intent(getActivity(), MapStoresListActivity.class));
                    break;
                case Menu.MY_COUPONS:
                    if (SessionsController.isLogged()) {
                        startActivity(new Intent(getActivity(), MyCouponsListActivity.class));
                    }else{
                        startActivity(new Intent(getActivity(), LoginV2Activity.class));
                    }
                    break;
               case Menu.ACCOUNT:
                    if(SessionsController.isLogged()){
                        startActivity(new Intent(getActivity(), ProfileActivity.class));
                    }else{
                        startActivity(new Intent(getActivity(), LoginV2Activity.class));
                    }
                    break;
            }

            if (mDrawerLayout != null)
                mDrawerLayout.closeDrawers();

        }


    }

    @Override
    public void onResume() {
        super.onResume();
        BusStation.getBus().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        BusStation.getBus().unregister(this);
    }

    @Subscribe
    public void onToggle(NavigationDrawerEvent object) {
        if (object.state == 1) {
            if (mDrawerLayout.isDrawerOpen(containerView))
                mDrawerLayout.closeDrawer(containerView);
            else
                mDrawerLayout.openDrawer(containerView);
        }
    }

    private static class Menu {

        static final int HOME_ID = 1;
        static final int MANAGE_YOUR_BUSINESS = 2;
        static final int CHAT = 22;
        static final int MAP_STORES = 3;
        static final int ABOUT = 6;
        static final int SETTING = 7;
        static final int MY_BOOKING = 8;
        static final int ACCOUNT = 9;
        public static final int MY_COUPONS = 10;
    }

    public static class NavigationDrawerEvent {
        private int state = 0;

        public NavigationDrawerEvent(int state) {
            this.state = state;
        }
    }


}
