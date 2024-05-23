package com.droideve.apps.nearbystores.customView;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import com.droideve.apps.nearbystores.activities.LoginV2Activity;
import com.droideve.apps.nearbystores.controllers.sessions.SessionsController;
import com.droideve.apps.nearbystores.fragments.CustomSearchFragment;
import com.droideve.apps.nearbystores.network.api_request.ApiRequest;
import com.droideve.apps.nearbystores.network.api_request.ApiRequestListeners;
import com.droideve.apps.nearbystores.parser.Parser;
import com.droideve.apps.nearbystores.utils.NSLog;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cooltechworks.views.shimmer.ShimmerRecyclerView;
import com.droideve.apps.nearbystores.AppController;
import com.droideve.apps.nearbystores.location.GPStracker;
import com.droideve.apps.nearbystores.R;
import com.droideve.apps.nearbystores.activities.OfferDetailActivity;
import com.droideve.apps.nearbystores.adapter.lists.OfferListAdapter;
import com.droideve.apps.nearbystores.animation.Animation;
import com.droideve.apps.nearbystores.appconfig.Constances;
import com.droideve.apps.nearbystores.classes.Offer;
import com.droideve.apps.nearbystores.controllers.stores.OffersController;
import com.droideve.apps.nearbystores.network.ServiceHandler;
import com.droideve.apps.nearbystores.parser.api_parser.OfferParser;
import com.droideve.apps.nearbystores.parser.tags.Tags;
import com.droideve.apps.nearbystores.utils.DateUtils;
import com.droideve.apps.nearbystores.utils.NSToast;
import com.droideve.apps.nearbystores.utils.Utils;
import com.droideve.apps.nearbystores.views.HorizontalView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import io.realm.RealmList;

import static com.droideve.apps.nearbystores.appconfig.AppConfig.APP_DEBUG;

public class OfferCustomView extends HorizontalView implements OfferListAdapter.ClickListener {

    private Context mContext;
    private OfferListAdapter adapter;
    private RecyclerView listView;
    private Map<String, Object> optionalParams;
    private Map<String, Object> req_QueryParams = new HashMap<String, Object>();
    private ShimmerRecyclerView shimmerRecycler;
    private View mainContainer;


    public OfferCustomView(Context context) {
        super(context);
        mContext = context;
        setRecyclerViewAdapter();
    }

    public OfferCustomView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;


        setCustomAttribute(context, attrs);

        setRecyclerViewAdapter();

    }


    public void loadData(boolean fromDatabase, Map<String, Object> optionalParams) {

        shimmerRecycler.showShimmerAdapter();
        listView.setVisibility(GONE);

        //OFFLINE MODE
        if (ServiceHandler.isNetworkAvailable(mContext)) {
            if (!fromDatabase) loadDataFromAPi(optionalParams);
            else loadDataFromDB(optionalParams);
        } else {
            loadDataFromDB(optionalParams);
        }
    }


    public void loadDataFromDB(final Map<String, Object> optionalParams) {
        //ensure the data exist on the database if not load it from api
        RealmList<Offer> list = OffersController.list();
        if (!list.isEmpty()) {
            adapter.removeAll();
            if (!list.isEmpty()) {
                adapter.addAllItems(list);
                listView.setVisibility(VISIBLE);
                shimmerRecycler.hideShimmerAdapter();
            } else {
                listView.setVisibility(GONE);
                shimmerRecycler.hideShimmerAdapter();
            }
            adapter.notifyDataSetChanged();
        } else {
            loadDataFromAPi(optionalParams);
        }

    }


    private void loadDataFromAPi(final Map<String, Object> _optionalParams) {

        Map<String, String> params = new HashMap<String, String>();

        GPStracker mGPS = new GPStracker(mContext);
        params.put("latitude", mGPS.getLatitude() + "");
        params.put("longitude", mGPS.getLongitude() + "");

        params.put("token", Utils.getToken(mContext));
        params.put("mac_adr", ServiceHandler.getMacAddr());


        params.put("limit", String.valueOf(optionalParams.get("strLimit")));
        params.put("page", 1 + "");

        params.put("date", DateUtils.getUTC("yyyy-MM-dd H:m:s"));
        params.put("timezone", TimeZone.getDefault().getID());

        if (_optionalParams.containsKey("store_id"))
            params.put("store_id", String.valueOf(_optionalParams.get("store_id")));

        for (Map.Entry<String, Object> set :
                _optionalParams.entrySet()) {
            params.put(set.getKey(), (String) set.getValue());
        }

        for (Map.Entry<String, String> entry : params.entrySet()) {
            req_QueryParams.put(entry.getKey(), String.valueOf(entry.getValue()));
        }

        NSLog.e("loadOffersParams",params.toString());

        ApiRequest.newPostInstance(Constances.API.API_GET_OFFERS, new ApiRequestListeners() {
            @Override
            public void onSuccess(Parser parser) {

                final OfferParser mOfferParser = new OfferParser(parser);

                RealmList<Offer> list = mOfferParser.getOffers();

                adapter.removeAll();
                adapter.addAllItems(list);

                //save data into database
                if (list.size() > 0)
                    OffersController.insertOffers(list);

                //hide the custom event view when the there's no event on the adapter
                if (adapter.getItemCount() == 0) {
                    mainContainer.setVisibility(GONE);
                } else {
                    mainContainer.setVisibility(VISIBLE);
                    shimmerRecycler.hideShimmerAdapter();
                    listView.setVisibility(VISIBLE);
                }

                String limit_param = optionalParams != null && optionalParams.containsKey("strLimit") ? String.valueOf(optionalParams.get("strLimit")) : "30";
                int limit = Integer.parseInt(limit_param);

                if (limit < mOfferParser.getIntArg(Tags.COUNT)) {
                    Animation.startZoomEffect(getChildAt(0).findViewById(R.id.card_show_more));
                }

            }

            @Override
            public void onFail(Map<String, String> errors) {

            }
        },params);


    }

    private void setRecyclerViewAdapter() {

        setOrientation(LinearLayout.HORIZONTAL);
        setGravity(Gravity.CENTER_HORIZONTAL);

        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.v3_horizontal_list_offers, this, true);

        mainContainer = getChildAt(0).findViewById(R.id.offer_container);

        //layout direction
        mainContainer.setLayoutDirection(AppController.isRTL() ? View.LAYOUT_DIRECTION_RTL : View.LAYOUT_DIRECTION_LTR);

        //header setup
        if ((Boolean) optionalParams.get("displayHeader")) {
            mainContainer.findViewById(R.id.layoutOffersHeader).setVisibility(VISIBLE);
        } else {
            mainContainer.findViewById(R.id.layoutOffersHeader).setVisibility(GONE);
        }

        if (optionalParams.containsKey("header") && optionalParams.get("header") != null)
            ((TextView) getChildAt(0).findViewById(R.id.card_title)).setText((String) optionalParams.get("header"));

        //setup show more view
        TextView showMore = getChildAt(0).findViewById(R.id.card_show_more);

        Drawable arrowIcon = getResources().getDrawable(R.drawable.ic_arrow_forward_white_18dp);
        if (AppController.isRTL()) {
            arrowIcon = getResources().getDrawable(R.drawable.ic_baseline_arrow_back_18);
        }

        DrawableCompat.setTint(
                DrawableCompat.wrap(arrowIcon),
                ContextCompat.getColor(mContext, R.color.colorPrimary)
        );

        if (!AppController.isRTL()) {
            showMore.setCompoundDrawablesWithIntrinsicBounds(null, null, arrowIcon, null);
        } else {
            showMore.setCompoundDrawablesWithIntrinsicBounds(arrowIcon, null, null, null);
        }


        showMore.findViewById(R.id.card_show_more).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                req_QueryParams.put("module", "offer"); //assign module

                if(optionalParams.containsKey("header")){
                    req_QueryParams.put("custom_title", optionalParams.get("header"));
                    req_QueryParams.put("custom_sub_title", optionalParams.get("header"));
                }

                CustomSearchFragment.showResultFilter(mContext, (HashMap<String, Object>) req_QueryParams);
            }
        });


        //start showLoading shimmerRecycler effect
        shimmerRecycler = getChildAt(0).findViewById(R.id.shimmer_view_container);

        if ((Boolean) optionalParams.get("loader")) {
            shimmerRecycler.showShimmerAdapter();
        } else {
            shimmerRecycler.hideShimmerAdapter();
        }


        listView = getChildAt(0).findViewById(R.id.list);
        adapter = new OfferListAdapter(mContext, new ArrayList<Offer>(), true, (Float) optionalParams.get("width"), (Float) optionalParams.get("height"));
        listView.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(mContext);
        mLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);


        listView.setLayoutManager(mLayoutManager);
        listView.setAdapter(adapter);
        adapter.setClickListener(this);

    }

    @Override
    public void itemClicked(View view, int position) {
        if (APP_DEBUG)
            NSLog.e("_1_offer_id", String.valueOf(adapter.getItem(position).getId()));

        Intent intent = new Intent(mContext, OfferDetailActivity.class);
        intent.putExtra("id", adapter.getItem(position).getId());
        mContext.startActivity(intent);
    }


    @Override
    public void likeClicked(View view, int position) {

        ImageView imageView = (ImageView) view;

        Offer object = adapter.getItem(position);

        if(!SessionsController.isLogged()){
            mContext.startActivity(new Intent(mContext, LoginV2Activity.class));
            return;
        }

        //prepare request params
        Map<String, String> params = new HashMap<String, String>();
        params.put("user_id", String.valueOf(SessionsController.getSession().getUser().getId()));
        params.put("offer_id", String.valueOf(object.getId()));

        //change icon
        if(object.getSaved() == 1){
            imageView.setImageDrawable(ResourcesCompat.getDrawable(getResources(),R.drawable.ic_favourite_outline,null));
        }else{
            imageView.setImageDrawable(ResourcesCompat.getDrawable(getResources(),R.drawable.ic_favourite,null));
        }

        //disable click
        imageView.setEnabled(false);

        //execute api
        ApiRequest.newPostInstance((object.getSaved()==1) ? Constances.API.API_BOOKMARK_OFFER_REMOVE : Constances.API.API_BOOKMARK_OFFER_SAVE, new ApiRequestListeners() {
            @Override
            public void onSuccess(Parser parser) {
                //enable click
                imageView.setEnabled(true);

                //check api successful
                if(parser.getSuccess()==1){

                    //show message
                    if(object.getSaved() == 1){
                        NSToast.show(getResources().getString(R.string.removeSuccessful));
                    }else{
                        NSToast.show(getResources().getString(R.string.saveSuccessful));
                    }

                    //update database
                    OffersController.doSave(object.getId(), object.getSaved()==1?0:1);

                    //refresh adapter
                    adapter.getItem(position).setSaved( (object.getSaved()==1?0:1) );
                }
            }

            @Override
            public void onFail(Map<String, String> errors) {
                imageView.setEnabled(true);
                if(object.getSaved() == 0){
                    imageView.setImageDrawable(ResourcesCompat.getDrawable(getResources(),R.drawable.ic_favourite_outline,null));
                }else{
                    imageView.setImageDrawable(ResourcesCompat.getDrawable(getResources(),R.drawable.ic_favourite,null));
                }
            }
        },params);



    }


    private void setCustomAttribute(Context context, @Nullable AttributeSet attrs) {

        optionalParams = new HashMap<>();
        //get the attributes specified in attrs.xml using the name we included
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.OfferCustomView, 0, 0);

        try {
            //get the text and colors specified using the names in attrs.xml
            optionalParams.put("strLimit", a.getInteger(R.styleable.OfferCustomView_ocvLimit, 30));
            optionalParams.put("displayHeader", a.getBoolean(R.styleable.OfferCustomView_ccDisplayHeader, true));
            optionalParams.put("height", a.getDimension(R.styleable.OfferCustomView_offerItemHeight, 0));
            optionalParams.put("width", a.getDimension(R.styleable.OfferCustomView_offerItemWidth, 0));
            optionalParams.put("loader", a.getBoolean(R.styleable.OfferCustomView_ocvLoader, true));
            optionalParams.put("header", a.getString(R.styleable.OfferCustomView_ocvHeader));

        } finally {
            a.recycle();
        }
    }

}
