package com.droideve.apps.nearbystores.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;

import com.droideve.apps.nearbystores.activities.LoginV2Activity;
import com.droideve.apps.nearbystores.activities.ProfileActivity;
import com.droideve.apps.nearbystores.appconfig.AppConfig;
import com.droideve.apps.nearbystores.utils.NSLog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowMetrics;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentTransaction;

import com.droideve.apps.nearbystores.R;
import com.droideve.apps.nearbystores.Services.NotifyDataNotificationEvent;
import com.droideve.apps.nearbystores.classes.Notification;
import com.droideve.apps.nearbystores.controllers.sessions.SessionsController;
import com.droideve.apps.nearbystores.customView.SwipeDisabledViewPager;
import com.droideve.apps.nearbystores.dtmessenger.MessengerHelper;
import com.droideve.apps.nearbystores.events.UnseenMessagesEvent;
//import com.google.android.gms.ads.AdListener;
//import com.google.android.gms.ads.AdRequest;
//import com.google.android.gms.ads.AdSize;
//import com.google.android.gms.ads.AdView;
//import com.google.android.gms.ads.LoadAdError;
//import com.google.android.gms.ads.interstitial.InterstitialAd;
//import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationMenuView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.OnItemClickListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.droideve.apps.nearbystores.appconfig.AppConfig.APP_DEBUG;
import static com.droideve.apps.nearbystores.appconfig.AppConfig.SHOW_ADS;


public class V2MainFragment extends Fragment {

    public final static String TAG = "mainfragment";
    static BottomNavigationView navigation;

    //navigation bottom
    @BindView(R.id.navigation_bottom)
    View navigation_bottom_view;
    //viewpager
    @BindView(R.id.viewpager)
    SwipeDisabledViewPager viewPager;
    //Admob
    @BindView(R.id.adsLayout)
    LinearLayout adsLayout;
//    @BindView(R.id.adView)
//    AdView mAdView;
    //context & main acityvity
    private Context myContext;

    //Menu
    private MenuItem prevMenuItem;

    //Declare listeners
    private Listener mListener;
    private ViewPagerAdapter adapter;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.v3_fragment_main, container, false);
        ButterKnife.bind(this, rootView);


        //init view pager adapter
        initViewPagerAdapter();

        //Show Interstitial Ads
        initAmob();

        //init Navigation Bottom
        initNavigationBottomView();

        return rootView;
    }

    @Override
    public void onPause() {
//        if (mAdView != null)
//            mAdView.pause();
        super.onPause();
    }

    @Override
    public void onResume() {
//        if (mAdView != null)
//            mAdView.resume();
        super.onResume();

    }

//    private InterstitialAd mInterstitialAd;

    private void initAmob() {
        //Show Banner Ads
        if (SHOW_ADS) {

//            mAdView.setVisibility(View.VISIBLE);
//            AdRequest adRequest = new AdRequest.Builder().build();
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
//                }
//
//                @Override
//                public void onAdOpened() {
//                    // Code to be executed when an ad opens an overlay that
//                    // covers the screen.
//                }
//            });
//            mAdView.loadAd(adRequest);
        }

        //show interstitial at first
        if(SHOW_ADS && AppConfig.SHOW_INTERSTITIAL_AT_FIRST){
//            AdRequest adRequest = new AdRequest.Builder().build();
//            InterstitialAd.load(getActivity(),getString(R.string.ad_interstitial_id), adRequest,
//                    new InterstitialAdLoadCallback() {
//                        @Override
//                        public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
//                            // The mInterstitialAd reference will be null until
//                            // an ad is loaded.
//                            mInterstitialAd = interstitialAd;
//
//                            mInterstitialAd.show(getActivity());
//                            NSLog.i(TAG, "onAdLoaded");
//                        }
//
//                        @Override
//                        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
//                            // Handle the error
//                            NSLog.d(TAG, loadAdError.toString());
//                            mInterstitialAd = null;
//                        }
//                    });

        }
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();

//        if (mAdView != null)
//            mAdView.destroy();
    }

    private void initViewPagerAdapter() {
        adapter = new ViewPagerAdapter(getFragmentManager());
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(4);
        viewPager.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NSLog.e("edd","dd");
            }
        });
    }

    public boolean setCurrentFragment(int position) {

        viewPager.setCurrentItem(position);
        navigation.getMenu().getItem(position).setChecked(true);
        return true;

    }

    public boolean ifFirstFragment() {
        return viewPager.getCurrentItem() == 0;
    }

    private void initNavigationBottomView() {

        if (mListener != null)
            mListener.onScrollHorizontal(0);

        navigation = navigation_bottom_view.findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {


                switch (item.getItemId()) {
                    case R.id.navigation_home:
                        viewPager.setCurrentItem(0);

                        if (mListener != null)
                            mListener.onScrollHorizontal(0);

                        return true;
                    case R.id.navigation_favorites:
                        viewPager.setCurrentItem(1);

                        if (mListener != null)
                            mListener.onScrollHorizontal(1);

                        return true;
                    case R.id.navigation_notification:
                        viewPager.setCurrentItem(2);

                        if (mListener != null)
                            mListener.onScrollHorizontal(2);

                        return true;
                    case R.id.navigation_account:
                        if (SessionsController.isLogged()) {
                            startActivity(new Intent(getActivity(), ProfileActivity.class));
                        }else{
                            startActivity(new Intent(getActivity(), LoginV2Activity.class));
                        }
                }


                return false;
            }
        });
    }

    private void updateNotificationBadge() {

        //notificationsUnseen
        NotificationChatVPFragment.NotificationChaCount = Notification.notificationsUnseen + MessengerHelper.NbrMessagesManager.getNbrTotalMessages();

        BottomNavigationMenuView bottomNavigationMenuView =
                (BottomNavigationMenuView) navigation.getChildAt(0);

        View v = bottomNavigationMenuView.getChildAt(2);
        BottomNavigationItemView itemView = (BottomNavigationItemView) v;

        View badge = LayoutInflater.from(myContext)
                .inflate(R.layout.nav_btm_notif_badge, itemView, true);

        if (NotificationChatVPFragment.NotificationChaCount > 0) {
            badge.findViewById(R.id.notifications_badge).setVisibility(View.VISIBLE);
            int notificationCounter = NotificationChatVPFragment.NotificationChaCount;
            ((TextView) badge.findViewById(R.id.notifications_badge)).setText(notificationCounter >= 100 ? "+99" : String.valueOf(notificationCounter));
        } else {
            badge.findViewById(R.id.notifications_badge).setVisibility(View.GONE);
        }

    }

    // This method will be called when a Notification is posted (in the UI thread for Toast)
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onMessageEvent(NotifyDataNotificationEvent event) {
        if (event.message.equals("update_badges")) {
            updateNotificationBadge();
        }
    }

    // This method will be called when a Messages is posted (in the UI thread for Toast)
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onMessageEvent(UnseenMessagesEvent event) {
        if (APP_DEBUG)
            if (MessengerHelper.NbrMessagesManager.getNbrTotalMessages() > 0) {
                Toast.makeText(getActivity(), "New message " + MessengerHelper.NbrMessagesManager.getNbrTotalMessages()
                        , Toast.LENGTH_LONG).show();

            }
        updateNotificationBadge();
    }

    private void pageChangedListener(final SwipeDisabledViewPager mViewPager, final BottomNavigationView mBottomNavigationView) {


        mViewPager.addOnPageChangeListener(new SwipeDisabledViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                prevMenuItem = mBottomNavigationView.getMenu().getItem(position);


                if (prevMenuItem != null)
                    prevMenuItem.setChecked(false);
                else {
                    mBottomNavigationView.getMenu().getItem(0).setChecked(false);
                    mBottomNavigationView.getMenu().getItem(position).setChecked(true);
                }

            }

            @Override
            public void onPageScrollStateChanged(int state) {


            }

        });
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        NSLog.e("dddd",viewPager.getCurrentItem());
        NSLog.e("dddd",adapter.getPageTitle(viewPager.getCurrentItem()).toString());
        NSLog.e("dddd",adapter.getPageTitle(viewPager.getCurrentItem()).toString());

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        myContext = activity;
    }

    @Override
    public void onStart() {
        super.onStart();

        EventBus.getDefault().register(this);

        //display notification badge counter
        updateNotificationBadge();

    }

    @Override
    public void onStop() {
        super.onStop();

        EventBus.getDefault().unregister(this);

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.home_menu, menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getActivity().finish();
        } else if (item.getItemId() == R.id.search_icon) {

            CustomSearchFragment fragment = new CustomSearchFragment();
            FragmentManager manager = getFragmentManager();
            manager.beginTransaction()
                    .add(R.id.main_container, fragment)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .addToBackStack("customSearchFrag")
                    .commit();

        } else {
            Toast.makeText(myContext, item.getTitle(), Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

    public void setListener(final Listener mItemListener) {
        this.mListener = mItemListener;
    }


    public interface Listener {
        void onScrollHorizontal(int position);
        void onScrollVertical(int scrollXs, int scrollY);
    }

    public class ViewPagerAdapter extends FragmentPagerAdapter implements HomeFragment.Listener {

        int FRAGS_ITEMS_NUM = 4;
        public ViewPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        // Returns total number of pages
        @Override
        public int getCount() {
            return FRAGS_ITEMS_NUM;
        }

        // Returns the fragment to display for that page
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    HomeFragment frag = HomeFragment.newInstance(0, "Page # 1");
                    frag.setListener(this);
                    return frag;
                case 1:
                    return BookmarkFragment.newInstance(2, "Page # 2");
                case 2:
                    return NotificationChatVPFragment.newInstance(3, "Page # 4");
                case 3:
                    if (SessionsController.isLogged())
                        return ProfileFragment.newInstance(4, "Page # 3");
                    else
                        return AuthenticationFragment.newInstance(4, "Page # 3");
                default:
                    return null;
            }
        }

        // Returns the page title for the top indicator
        @Override
        public CharSequence getPageTitle(int position) {
            return "Page " + position;
        }

        @Override
        public void onScroll(int scrollX, int scrollY) {
            NSLog.e("ViewPagerAdapter", scrollX + " - " + scrollY);
            if (mListener != null)
                mListener.onScrollVertical(scrollX, scrollY);
        }
    }

}
