package com.droideve.apps.nearbystores.customView;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import com.droideve.apps.nearbystores.activities.CustomSearchActivity;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.cooltechworks.views.shimmer.ShimmerRecyclerView;
import com.droideve.apps.nearbystores.AppController;
import com.droideve.apps.nearbystores.location.GPStracker;
import com.droideve.apps.nearbystores.R;
import com.droideve.apps.nearbystores.activities.StoreDetailActivity;
import com.droideve.apps.nearbystores.activities.StoresListActivity;
import com.droideve.apps.nearbystores.adapter.lists.StoreListAdapter;
import com.droideve.apps.nearbystores.animation.Animation;
import com.droideve.apps.nearbystores.appconfig.Constances;
import com.droideve.apps.nearbystores.classes.Store;
import com.droideve.apps.nearbystores.controllers.stores.StoreController;
import com.droideve.apps.nearbystores.network.ServiceHandler;
import com.droideve.apps.nearbystores.network.VolleySingleton;
import com.droideve.apps.nearbystores.network.api_request.SimpleRequest;
import com.droideve.apps.nearbystores.parser.api_parser.StoreParser;
import com.droideve.apps.nearbystores.parser.tags.Tags;
import com.droideve.apps.nearbystores.utils.NSToast;
import com.droideve.apps.nearbystores.views.HorizontalView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import io.realm.RealmList;

import static com.droideve.apps.nearbystores.appconfig.AppConfig.APP_DEBUG;

public class StoreCustomView extends HorizontalView implements StoreListAdapter.ClickListener {

    private Context mContext;
    private StoreListAdapter adapter;
    private RecyclerView listView;
    private HashMap<String, Object> searchParams;
    private Map<String, Object> optionalParams;
    private Map<String, Object> req_QueryParams = new HashMap<String, Object>();
    private ShimmerRecyclerView shimmerRecycler;
    private View storeContainer;


    public StoreCustomView(Context context) {
        super(context);
        mContext = context;
        setRecyclerViewAdapter();
    }

    public StoreCustomView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;

        setCustomAttribute(context, attrs);

        setRecyclerViewAdapter();
    }


    public void loadData(boolean fromDatabase, HashMap<String, Object> _searchParams) {

        searchParams = _searchParams;

        shimmerRecycler.showShimmerAdapter();
        listView.setVisibility(GONE);

        //OFFLINE MODE
        if (ServiceHandler.isNetworkAvailable(mContext)) {

            if (!fromDatabase) loadDataFromAPi();
            else loadDataFromDB();
        } else {
            loadDataFromDB();
        }

    }

    public void loadDataFromDB() {
        //ensure the data exist on the database if not load it from api
        RealmList<Store> list = StoreController.list();
        if (!list.isEmpty()) {
            adapter.clear();
            if (!list.isEmpty()) {
                adapter.addAllItems(list);
                listView.setVisibility(VISIBLE);
                storeContainer.setVisibility(VISIBLE);
                shimmerRecycler.hideShimmerAdapter();
            } else {
                listView.setVisibility(GONE);
                storeContainer.setVisibility(GONE);

            }
            adapter.notifyDataSetChanged();
        } else {
            loadDataFromAPi();
        }

    }


    private void loadDataFromAPi() {

        Map<String, String> params = new HashMap<String, String>();

        GPStracker mGPS = new GPStracker(mContext);
        params.put("latitude", String.valueOf(mGPS.getLatitude()));
        params.put("longitude", String.valueOf(mGPS.getLongitude()));

        params.put("limit", String.valueOf(optionalParams.get("strLimit")));
        params.put("page", 1 + "");
        params.put("order_by", Constances.OrderByFilter.NEARBY);

        if (searchParams != null && !searchParams.isEmpty()) {
            for (Map.Entry<String, Object> entry : searchParams.entrySet()) {
                params.put(entry.getKey(), String.valueOf(entry.getValue()));
            }
        }

        for (Map.Entry<String, String> entry : params.entrySet()) {
            req_QueryParams.put(entry.getKey(), String.valueOf(entry.getValue()));
        }

        ApiRequest.newPostInstance(Constances.API.API_USER_GET_STORES, new ApiRequestListeners() {
            @Override
            public void onSuccess(Parser parser) {
                parse_data(parser);
            }

            @Override
            public void onFail(Map<String, String> errors) {

            }
        },params);


    }

    private void parse_data(Parser parser){

        final StoreParser mStoreParser = new StoreParser(parser);
        if (mStoreParser.getSuccess() == 1) {

            RealmList<Store> list = mStoreParser.getStore();
            adapter.removeAll();
            adapter.addAllItems(list);

            //save data into database
            if (list.size() > 0)
                StoreController.insertStores(list);

            if (adapter.getItemCount() > 0) {
                storeContainer.setVisibility(VISIBLE);
                listView.setVisibility(VISIBLE);
            } else {
                storeContainer.setVisibility(GONE);
                listView.setVisibility(GONE);
            }


            adapter.notifyDataSetChanged();


            String limit_param = optionalParams != null && optionalParams.containsKey("strLimit") ? String.valueOf(optionalParams.get("strLimit")) : "30";
            int limit = Integer.parseInt(limit_param);

            if (limit < mStoreParser.getIntArg(Tags.COUNT)) {
                Animation.startZoomEffect(getChildAt(0).findViewById(R.id.card_show_more));
            }
        }

        shimmerRecycler.hideShimmerAdapter();

    }

    private void setRecyclerViewAdapter() {


        setOrientation(LinearLayout.HORIZONTAL);
        setGravity(Gravity.CENTER_HORIZONTAL);


        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.v3_horizontal_list_store, this, true);

        //setup show more view
        storeContainer = getChildAt(0);

        //layout direction
        storeContainer.setLayoutDirection(AppController.isRTL() ? View.LAYOUT_DIRECTION_RTL : View.LAYOUT_DIRECTION_LTR);

        //header setup
        if (optionalParams.containsKey("header") && optionalParams.get("header") != null)
            ((TextView) storeContainer.findViewById(R.id.card_title)).setText((String) optionalParams.get("header"));

        //setup show more view
        TextView showMore = storeContainer.findViewById(R.id.card_show_more);

        showMore.setCompoundDrawablePadding(5);
        Drawable arrowIcon = getResources().getDrawable(R.drawable.ic_baseline_chevron_right_24);
        if (AppController.isRTL()) {
            arrowIcon = getResources().getDrawable(R.drawable.ic_baseline_chevron_right_24);
        }

        DrawableCompat.setTint(
                DrawableCompat.wrap(arrowIcon),
                ContextCompat.getColor(mContext, R.color.colorAccent)
        );

        if (!AppController.isRTL()) {
            showMore.setCompoundDrawablesWithIntrinsicBounds(null, null, arrowIcon, null);
        } else {
            showMore.setCompoundDrawablesWithIntrinsicBounds(arrowIcon, null, null, null);
        }


        showMore.findViewById(R.id.card_show_more).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                req_QueryParams.put("module", "store"); //assign module

                if(optionalParams.containsKey("header")){
                    req_QueryParams.put("custom_title", optionalParams.get("header"));
                    req_QueryParams.put("custom_sub_title", optionalParams.get("header"));
                }

                CustomSearchFragment.showResultFilter(mContext, (HashMap<String, Object>) req_QueryParams);
            }
        });


        //list item setup
        listView = getChildAt(0).findViewById(R.id.list);
        adapter = new StoreListAdapter(mContext, new ArrayList<Store>(), true, (Float) optionalParams.get("width"), (Float) optionalParams.get("height"));
        //start showLoading shimmer effect
        shimmerRecycler = getChildAt(0).findViewById(R.id.shimmer_view_container);

        if ((Boolean) optionalParams.get("loader")) {
            shimmerRecycler.setVisibility(VISIBLE);
        } else {
            shimmerRecycler.hideShimmerAdapter();
        }

        listView.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(mContext);
        mLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);

        if (AppController.isRTL())
            mLayoutManager.setReverseLayout(true);

        listView.setLayoutManager(mLayoutManager);
        listView.setAdapter(adapter);
        adapter.setClickListener(this);


    }

    @Override
    public void itemClicked(View view, int position) {

        if (APP_DEBUG)
            NSLog.e("_1_store_id", String.valueOf(adapter.getItem(position).getId()));

        Intent intent = new Intent(mContext, StoreDetailActivity.class);
        intent.putExtra("id", adapter.getItem(position).getId());
        mContext.startActivity(intent);
    }

    @Override
    public void likeClicked(View view, int position) {

        ImageView imageView = (ImageView) view;

        Store object = adapter.getItem(position);

        if(!SessionsController.isLogged()){
            mContext.startActivity(new Intent(mContext, LoginV2Activity.class));
            return;
        }

        //prepare request params
        Map<String, String> params = new HashMap<String, String>();
        params.put("user_id", String.valueOf(SessionsController.getSession().getUser().getId()));
        params.put("store_id", String.valueOf(object.getId()));

        //change icon
        if(object.getSaved() == 1){
            imageView.setImageDrawable(ResourcesCompat.getDrawable(getResources(),R.drawable.ic_favourite_outline,null));
        }else{
            imageView.setImageDrawable(ResourcesCompat.getDrawable(getResources(),R.drawable.ic_favourite,null));
        }

        //disable click
        imageView.setEnabled(false);

        //execute api
        ApiRequest.newPostInstance((object.getSaved()==1) ? Constances.API.API_BOOKMARK_STORE_REMOVE : Constances.API.API_BOOKMARK_STORE_SAVE, new ApiRequestListeners() {
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
                    StoreController.doSave(object.getId(), object.getSaved()==1?0:1);

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
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs,
                R.styleable.StoreCustomView, 0, 0);

        try {
            //get the text and colors specified using the names in attrs.xml
            optionalParams.put("strLimit", a.getInteger(R.styleable.StoreCustomView_strLimit, 30));
            optionalParams.put("height", a.getDimension(R.styleable.StoreCustomView_storeItemHeight, 0));
            optionalParams.put("width", a.getDimension(R.styleable.StoreCustomView_storeItemWidth, 0));
            optionalParams.put("loader", a.getBoolean(R.styleable.StoreCustomView_strLoader, true));
            optionalParams.put("header", a.getString(R.styleable.StoreCustomView_strHeader));

        } finally {
            a.recycle();
        }
    }


}
