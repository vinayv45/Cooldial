package com.droideve.apps.nearbystores.customView;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;

import com.droideve.apps.nearbystores.network.api_request.ApiRequest;
import com.droideve.apps.nearbystores.network.api_request.ApiRequestListeners;
import com.droideve.apps.nearbystores.parser.Parser;
import com.droideve.apps.nearbystores.utils.NSLog;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.cooltechworks.views.shimmer.ShimmerRecyclerView;
import com.droideve.apps.nearbystores.R;
import com.droideve.apps.nearbystores.activities.EventDetailActivity;
import com.droideve.apps.nearbystores.activities.OfferDetailActivity;
import com.droideve.apps.nearbystores.activities.StoreDetailActivity;
import com.droideve.apps.nearbystores.adapter.pager.BannerAdapter;
import com.droideve.apps.nearbystores.appconfig.Constances;
import com.droideve.apps.nearbystores.classes.Banner;
import com.droideve.apps.nearbystores.network.VolleySingleton;
import com.droideve.apps.nearbystores.network.api_request.SimpleRequest;
import com.droideve.apps.nearbystores.parser.api_parser.BannerParser;
import com.droideve.apps.nearbystores.parser.tags.Tags;
import com.droideve.apps.nearbystores.views.HorizontalView;
import com.wuadam.awesomewebview.AwesomeWebView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.droideve.apps.nearbystores.appconfig.AppConfig.APP_DEBUG;

import me.huseyinozer.TooltipIndicator;

public class SliderCustomView extends HorizontalView implements BannerAdapter.OnItemClickListener {

    private Context mContext;
    private View slider_container;
    private ViewPager viewPager;
    private LinearLayout layout_dots;
    private BannerAdapter adapterBanners;
    private Runnable runnable = null;
    private Handler handler = new Handler();
    private ShimmerRecyclerView shimmerRecycler;
    private View mainContainer;
    private Map<String, Object> optionalParams;


    public SliderCustomView(Context context) {
        super(context);
        mContext = context;
    }

    public SliderCustomView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;

        setOrientation(LinearLayout.HORIZONTAL);
        setGravity(Gravity.CENTER_HORIZONTAL);


        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.v3_slider_viewpager, this, true);

        mainContainer = getChildAt(0).findViewById(R.id.slider_container_layout);


        //start showLoading shimmer effect
        shimmerRecycler = getChildAt(0).findViewById(R.id.shimmer_view_container);
        slider_container = getChildAt(0).findViewById(R.id.slider_container);

        initSlider();

    }


    public void loadData(boolean fromDatabase) {

        shimmerRecycler.showShimmerAdapter();

        if (!fromDatabase) {
            loaDataFromApi();
        }
    }

    private void loaDataFromApi() {

        slider_container.setVisibility(GONE);

        ApiRequest.newPostInstance(Constances.API.API_GET_SLIDERS, new ApiRequestListeners() {
            @Override
            public void onSuccess(Parser parser) {

                final BannerParser mBannerParser = new BannerParser(parser);

                if (mBannerParser.getSuccess() == 1) {
                    adapterBanners.setbanners(mBannerParser.getBanners());
                    //stop showLoading animation
                    shimmerRecycler.hideShimmerAdapter();

                    if (adapterBanners.getCount() > 0) {
                        slider_container.setVisibility(VISIBLE);
                        mainContainer.setVisibility(VISIBLE);

                    } else {
                        //hide the view until showLoading data
                        mainContainer.setVisibility(GONE);

                    }
                }

            }

            @Override
            public void onFail(Map<String, String> errors) {
                shimmerRecycler.hideShimmerAdapter();
            }
        });


    }


    public void startAutoSlider() {
        runnable = new Runnable() {
            @Override
            public void run() {
                int pos = viewPager.getCurrentItem();
                pos = pos + 1;
                if (pos >= adapterBanners.getCount()) pos = 0;
                viewPager.setCurrentItem(pos);
                handler.postDelayed(runnable, 6000);
            }
        };
        handler.postDelayed(runnable, 6000);

    }


    private void initSlider() {
        //List<Banner> items = BannersController.getArrayList();
        //sldier_img_layout = view;
        viewPager = slider_container.findViewById(R.id.pager);

        //hide the view until showLoading data
        slider_container.setVisibility(GONE);
        // if (items.size()>0){

        adapterBanners = new BannerAdapter(mContext, new ArrayList<Banner>(), viewPager);
        //adapterBanners.setbanners(items);
        adapterBanners.setOnItemClickListener(this);


        viewPager.setAdapter(adapterBanners);

        // displaying selected image first
        viewPager.setCurrentItem(0);

    }

    @Override
    public void onItemClick(View view, Banner obj) {

        if (APP_DEBUG)
            Toast.makeText(getContext(), getContext().getString(R.string.redirected_to) + obj.getModule(), Toast.LENGTH_LONG).show();

        handleClickBanner(obj);
    }

    private void handleClickBanner(Banner obj) {
        if (obj != null) {
            switch (obj.getModule()) {
                case Constances.ModulesConfig.STORE_MODULE:
                    Intent intentStore = new Intent(mContext, StoreDetailActivity.class);
                    intentStore.putExtra("id", obj.getModule_id());
                    mContext.startActivity(intentStore);
                    break;
                case Constances.ModulesConfig.OFFER_MODULE:
                    Intent intentOffer = new Intent(mContext, OfferDetailActivity.class);
                    intentOffer.putExtra("id", obj.getModule_id());
                    mContext.startActivity(intentOffer);
                    break;
                case Constances.ModulesConfig.EVENT_MODULE:
                    Intent intentEvent = new Intent(mContext, EventDetailActivity.class);
                    intentEvent.putExtra("id", obj.getModule_id());
                    mContext.startActivity(intentEvent);
                    break;
                default:
                    new AwesomeWebView.Builder(mContext)
                            .statusBarColorRes(R.color.colorPrimary)
                            .theme(R.style.FinestWebViewAppTheme)
                            .show(obj.getModule_id());
                    break;

            }
        }
    }


    private void setCustomAttribute(Context context, @Nullable AttributeSet attrs) {

        optionalParams = new HashMap<>();
        //get the attributes specified in attrs.xml using the name we included
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs,
                R.styleable.SliderCustomView, 0, 0);

        try {
            //get the text and colors specified using the names in attrs.xml
            optionalParams.put("height", a.getInteger(R.styleable.SliderCustomView_sliderItemHeight, 0));
            optionalParams.put("width", a.getInteger(R.styleable.SliderCustomView_sliderItemWidth, 0));

        } finally {
            a.recycle();
        }
    }
}
