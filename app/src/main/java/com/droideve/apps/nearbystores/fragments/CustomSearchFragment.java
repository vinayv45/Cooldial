package com.droideve.apps.nearbystores.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.droideve.apps.nearbystores.classes.Category;
import com.droideve.apps.nearbystores.controllers.categories.CategoryController;
import com.droideve.apps.nearbystores.utils.NSLog;

import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.droideve.apps.nearbystores.R;
import com.droideve.apps.nearbystores.activities.CustomSearchActivity;
import com.droideve.apps.nearbystores.appconfig.AppConfig;
import com.droideve.apps.nearbystores.appconfig.Constances;
import com.droideve.apps.nearbystores.customView.CategoryCustomView;
import com.droideve.apps.nearbystores.databinding.V2FragmentCustomSearchBinding;
import com.droideve.apps.nearbystores.utils.Tools;
import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.textfield.TextInputEditText;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.droideve.apps.nearbystores.AppController.getInstance;


public class CustomSearchFragment extends Fragment implements View.OnClickListener {

    public static Class<?> previousPageClass = null;
    private static int mOldDistance = -1;
    private static String mOldValue = "";
    @BindView(R.id.range_seek_bar)
    SeekBar rangeSeekBar;
    @BindView(R.id.range_seek_bar_text)
    TextView range_seek_bar_text;
    @BindView(R.id.searchField)
    TextView searchField;
    @BindView(R.id.date_begin_txt)
    TextInputEditText dateBeginTxt;
    @BindView(R.id.filterStoresBtn)
    Button filterStoresBtn;
    @BindView(R.id.filterEventsBtn)
    Button filterEventsBtn;
    @BindView(R.id.filterOffersBtn)
    Button filterOffersBtn;
    @BindView(R.id.searchStores)
    LinearLayout searchStores;
    @BindView(R.id.searchOffers)
    LinearLayout searchOffers;
    @BindView(R.id.searchEvents)
    LinearLayout searchEvents;
    @BindView(R.id.searchFilterCategory)
    LinearLayout searchFilterCategory;
    @BindView(R.id.btnSearchLayout)
    Button btnSearchLayout;
    @BindView(R.id.btnsOffersPrice)
    LinearLayout btnsOffersPrice;
    @BindView(R.id.price_offer_btn)
    Button price_offer_btn;
    @BindView(R.id.discount_offer_btn)
    Button discount_offer_btn;
    @BindView(R.id.btnsOffersDiscountProps)
    LinearLayout btnsOffersDiscountProps;
    @BindView(R.id.btnsOffersPriceFormat)
    LinearLayout btnsOffersPriceFormat;
    @BindView(R.id.openStatusCB)
    AppCompatCheckBox openStatusCB;
    @BindView(R.id.orderByDate)
    Button orderByDate;
    @BindView(R.id.orderByGeo)
    Button orderByGeo;
    @BindView(R.id.btnsModules)
    LinearLayout btnsModules;

    private String distance_unit;
    private List<Category> listCats;

    private Context mContext;
    private HashMap<String, Object> searchParams;
    private int AUTOCOMPLETE_REQUEST_CODE = 1001;
    private int REQUEST_RANGE_RADIUS = -1;
    private Button[] btnOffersDiscount = new Button[4], btnOffersPrice = new Button[4];
    private Button btn_discount_unfocus, btn_price_unfocus;
    private int[] btn_id_discount = {R.id.discount_less_than_25, R.id.discount_less_than_50, R.id.discount_less_than_75, R.id.discount_less_than_100};
    private int[] btn_id_price = {R.id.price_one_number, R.id.price_two_numbers, R.id.price_three_numbers, R.id.price_four_numbers};


    public CustomSearchFragment() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);


    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        V2FragmentCustomSearchBinding binding = DataBindingUtil.inflate(
                inflater, R.layout.v2_fragment_custom_search, container, false);
        View view = binding.getRoot();
        ButterKnife.bind(this, view);


        initParams(view);

        initCategoryRV_V2(view);

        initRangeSeekBar();

        handleClickEventListener();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        //set store as a default view
        setDefaultModuleSelected(getView(), (getArguments() != null && getArguments().containsKey("selected_module")) ? getArguments().getString("selected_module") : Constances.ModulesConfig.STORE_MODULE);


        //get data from cache if exist
        if (getArguments() != null && getArguments().containsKey("useCacheFields") && getArguments().getString("useCacheFields").equals("enabled")) {

            HashMap<String, Object> cacheSearchObj = null;
            if (getArguments().containsKey("searchParams")) {  //this will serve when choosing a category from home fragment
                cacheSearchObj = (HashMap<String, Object>) getArguments().getSerializable("searchParams");
            } else { // if there's no fields then we should get the detail from cache
                cacheSearchObj = getSavedSearchFields();
            }

            if (cacheSearchObj != null) {
                final HashMap<String, Object> finalCacheSearchObj = cacheSearchObj;
                (new android.os.Handler()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        importDataFromPrefObj(finalCacheSearchObj);
                    }
                }, 800);
            }
        }


        //disable module filter buttons for maps activity
        if (previousPageClass != null)
            btnsModules.setVisibility(View.GONE);


    }



    private void offersDiscountBtnClick() {
        for (int i = 0; i < btnOffersDiscount.length; i++) {
            btnOffersDiscount[i] = btnsOffersDiscountProps.findViewById(btn_id_discount[i]);
            btnOffersDiscount[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    searchParams.put("discount_selected_value", v.getTag());

                    switch (v.getId()) {

                        case R.id.discount_less_than_25:

                            setDiscountFocus(btn_discount_unfocus, btnOffersDiscount[0]);
                            break;

                        case R.id.discount_less_than_50:
                            setDiscountFocus(btn_discount_unfocus, btnOffersDiscount[1]);
                            break;

                        case R.id.discount_less_than_75:
                            setDiscountFocus(btn_discount_unfocus, btnOffersDiscount[2]);
                            break;

                        case R.id.discount_less_than_100:
                            setDiscountFocus(btn_discount_unfocus, btnOffersDiscount[3]);
                            break;
                    }

                }
            });
        }

        btn_discount_unfocus = btnOffersDiscount[0];
    }

    private void setDiscountFocus(Button btn_discount_unfocus, Button btn_focus) {
        if (btn_discount_unfocus.getId() == btn_focus.getId()) {
            btn_focus.setSelected(!btn_focus.isSelected());
        } else {
            btn_focus.setSelected(true);
            btn_discount_unfocus.setSelected(false);
        }
        this.btn_discount_unfocus = btn_focus;

        searchParams.put("value_type", "percent");
        searchParams.put("offer_value", btn_focus.getText().toString());
    }


    private void offersPriceBtnClick() {
        for (int i = 0; i < btnOffersPrice.length; i++) {
            btnOffersPrice[i] = btnsOffersPriceFormat.findViewById(btn_id_price[i]);
            btnOffersPrice[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //save button state
                    searchParams.put("offer_selected_value", v.getTag());

                    switch (v.getId()) {
                        case R.id.price_one_number:
                            setPriceFocus(btn_price_unfocus, btnOffersPrice[0]);
                            break;
                        case R.id.price_two_numbers:
                            setPriceFocus(btn_price_unfocus, btnOffersPrice[1]);
                            break;
                        case R.id.price_three_numbers:
                            setPriceFocus(btn_price_unfocus, btnOffersPrice[2]);
                            break;

                        case R.id.price_four_numbers:
                            setPriceFocus(btn_price_unfocus, btnOffersPrice[3]);
                            break;
                    }

                }
            });
        }

        btn_price_unfocus = btnOffersPrice[0];

    }

    @SuppressLint("ResourceType")
    private void setPriceFocus(Button btn_discount_unfocus, Button btn_focus) {
        if (btn_discount_unfocus.getId() == btn_focus.getId()) {
            btn_focus.setSelected(!btn_focus.isSelected());
        } else {
            btn_focus.setSelected(true);
            btn_discount_unfocus.setSelected(false);
        }
        this.btn_price_unfocus = btn_focus;


        searchParams.put("value_type", "price");
        searchParams.put("offer_value", btn_focus.getText().toString());

    }


    private void initParams(View view) {

        mContext = view.getContext();

        searchParams = new HashMap<>();

        SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(getContext());
        distance_unit = sh.getString("distance_unit", "km");

    }


    private void initCategoryRV_V2(View view) {
        setupCategoryFilter(view);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {


        super.onActivityCreated(savedInstanceState);

    }

    private void handleClickEventListener() {
        filterStoresBtn.setOnClickListener(this);
        filterEventsBtn.setOnClickListener(this);
        filterOffersBtn.setOnClickListener(this);
        price_offer_btn.setOnClickListener(this);
        discount_offer_btn.setOnClickListener(this);
        btnSearchLayout.setOnClickListener(this);
        orderByDate.setOnClickListener(this);
        orderByGeo.setOnClickListener(this);
        dateBeginTxt.setOnClickListener(this);
        searchParams.put("order_by", Constances.OrderByFilter.NEARBY);
        //make geo location  section selected as a default
        orderByGeo.setSelected(true);
    }

    private void setDefaultModuleSelected(View view, String module_name) {
        if (module_name.equals(Constances.ModulesConfig.OFFER_MODULE)) {
            makeOfferFocusable();
        } else if (module_name.equals(Constances.ModulesConfig.EVENT_MODULE)) {
            makeEventFocusable();
        } else {
            makeStoreFocusable();
        }
    }

    private void initRangeSeekBar() {


        if (mOldDistance == -1) {
            int radius = PreferenceManager.getDefaultSharedPreferences(getContext()).getInt("distance_value", 100);
            mOldDistance = radius;
        }

        String val = String.valueOf(mOldDistance);
        if (mOldDistance == 100) {
            val = "+" + mOldDistance;
        }

        @SuppressLint("StringFormatMatches") String msg = String.format(getContext().getString(R.string.settings_notification_distance_dis), val, distance_unit);
        range_seek_bar_text.setText(msg);
        rangeSeekBar.setProgress(mOldDistance);
        range_seek_bar_text.setText(mOldValue);

        rangeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                String val = String.valueOf(progress);
                if (progress == 100) {
                    val = "+" + progress;
                }

                @SuppressLint("StringFormatMatches") String msg = String.format(getContext().getString(R.string.settings_notification_distance_dis), val, distance_unit);
                range_seek_bar_text.setText(msg);
                mOldDistance = progress;

                REQUEST_RANGE_RADIUS = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }


    private void dialogDatePickerLight(final TextInputEditText pickDateTxt) {
        Calendar cur_calender = Calendar.getInstance();
        DatePickerDialog datePicker = DatePickerDialog.newInstance(
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
                        Calendar calendar = Calendar.getInstance();
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, monthOfYear);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        long date_ship_millis = calendar.getTimeInMillis();
                        pickDateTxt.setText(Tools.getFormattedDateAPI(date_ship_millis));
                    }
                },
                cur_calender.get(Calendar.YEAR),
                cur_calender.get(Calendar.MONTH),
                cur_calender.get(Calendar.DAY_OF_MONTH)
        );
        //set dark light
        datePicker.setThemeDark(false);
        datePicker.setAccentColor(getResources().getColor(R.color.colorPrimary));
        datePicker.setMinDate(cur_calender);
        datePicker.show(getActivity().getFragmentManager(), "Datepickerdialog");
    }


    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.filterStoresBtn) {

            makeStoreFocusable();

        } else if (v.getId() == R.id.filterOffersBtn) {

            makeOfferFocusable();

        } else if (v.getId() == R.id.filterEventsBtn) {

            makeEventFocusable();

        } else if (v.getId() == R.id.price_offer_btn) {
            price_offer_btn.setSelected(true);
            discount_offer_btn.setSelected(false);
            btnsOffersDiscountProps.setVisibility(View.GONE);
            btnsOffersPriceFormat.setVisibility(View.VISIBLE);
            searchParams.put("price_offer_btn", price_offer_btn.isSelected());
            searchParams.remove("discount_offer_btn");

        } else if (v.getId() == R.id.discount_offer_btn) {
            price_offer_btn.setSelected(false);
            discount_offer_btn.setSelected(true);
            btnsOffersDiscountProps.setVisibility(View.VISIBLE);
            btnsOffersPriceFormat.setVisibility(View.GONE);

            searchParams.put("discount_offer_btn", discount_offer_btn.isSelected());
            searchParams.remove("price_offer_btn");

        } else if (v.getId() == R.id.btnSearchLayout) {
            handleSearchLayoutClick();
        } else if (v.getId() == R.id.date_begin_txt) {
            dialogDatePickerLight(dateBeginTxt);
        } else if (v.getId() == R.id.orderByGeo) {
            orderByDate.setSelected(false);
            orderByGeo.setSelected(true);
            searchParams.put("order_by", Constances.OrderByFilter.NEARBY);
        } else if (v.getId() == R.id.orderByDate) {
            orderByGeo.setSelected(false);
            orderByDate.setSelected(true);
            searchParams.put("order_by", Constances.OrderByFilter.RECENT);
        }
    }

    private void makeStoreFocusable() {

        filterStoresBtn.setSelected(true);
        filterOffersBtn.setSelected(false);
        filterEventsBtn.setSelected(false);

        searchEvents.setVisibility(View.GONE);
        searchStores.setVisibility(View.VISIBLE);
        searchOffers.setVisibility(View.GONE);

        searchParams.put("module", Constances.ModulesConfig.STORE_MODULE);
    }


    private void makeOfferFocusable() {

        searchParams.put("module", Constances.ModulesConfig.OFFER_MODULE);


        filterStoresBtn.setSelected(false);
        filterOffersBtn.setSelected(true);
        filterEventsBtn.setSelected(false);
        price_offer_btn.setSelected(true);

        searchEvents.setVisibility(View.GONE);
        searchStores.setVisibility(View.GONE);
        searchOffers.setVisibility(View.VISIBLE);

        btnsOffersPrice.setVisibility(View.VISIBLE);
        btnsOffersPriceFormat.setVisibility(View.VISIBLE);


        offersDiscountBtnClick();
        offersPriceBtnClick();
    }

    private void makeEventFocusable() {
        filterStoresBtn.setSelected(false);
        filterOffersBtn.setSelected(false);
        filterEventsBtn.setSelected(true);

        searchEvents.setVisibility(View.VISIBLE);
        searchStores.setVisibility(View.GONE);
        searchOffers.setVisibility(View.GONE);

        searchParams.put("module", Constances.ModulesConfig.EVENT_MODULE);
    }

    public static void showResultFilter(Context mContext, HashMap<String, Object> searchParams){

        Intent intent = new Intent(mContext, CustomSearchActivity.ResultFilterActivity.class);
        intent.putExtra("searchParams", searchParams);
        mContext.startActivity(intent);


    }

    private void handleSearchLayoutClick() {


        String searchFieldTxt = searchField.getText().toString().trim();
        searchParams.put("search", searchFieldTxt);
        searchParams.put("category", CategoryCustomView.itemCategoryselectedId);
        searchParams.put("category_selected_index", CategoryCustomView.itemCategoryselectedIndex);

        if (REQUEST_RANGE_RADIUS > -1) {
            if (REQUEST_RANGE_RADIUS <= 99)
                searchParams.put("radius", String.valueOf((REQUEST_RANGE_RADIUS * 1000)));
        }

        searchParams.put("openingStatus", openStatusCB.isChecked() ? 1 : 0);
        searchParams.put("date_begin", dateBeginTxt.getText().toString().trim());


        if (previousPageClass != null) {
            Intent intent = new Intent();
            previousPageClass = null;
            intent.putExtra("searchParams", searchParams);
            getActivity().setResult(Activity.RESULT_OK, intent);
            getActivity().finish();
            return;
        } else {

            //save view state
            saveCurrentSearchFields(searchParams);

            if (getArguments() != null && getArguments().containsKey("useCacheFields") && getArguments().getString("useCacheFields").equals("enabled")) {
                Intent intent = new Intent();
                intent.putExtra("searchParams", searchParams);
                getActivity().setResult(Activity.RESULT_OK, intent);
                getActivity().finish();

            } else {

                Intent intent = new Intent(mContext, CustomSearchActivity.ResultFilterActivity.class);
                intent.putExtra("searchParams", searchParams);
                getActivity().startActivity(intent);
                getActivity().finish();
            }


        }


    }


    private void saveCurrentSearchFields(final HashMap<String, Object> searchObj) {
        final SharedPreferences sharedPref = getInstance()
                .getSharedPreferences("searchObjPref", Context.MODE_PRIVATE);
        Gson gson = new Gson();
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("searchParams", gson.toJson(searchObj));
        editor.commit();
    }

    private HashMap<String, Object> getSavedSearchFields() {
        SharedPreferences saveSO = getInstance().getSharedPreferences("searchObjPref", Context.MODE_PRIVATE);

        if (saveSO != null) {
            Type type = new TypeToken<HashMap<String, String>>() {
            }.getType();
            Gson gson = new Gson();
            return gson.fromJson(saveSO.getString("searchParams", null), type);
        } else {
            return null;
        }
    }

    private void importDataFromPrefObj(final HashMap<String, Object> cacheSearchObj) {

        if (cacheSearchObj == null) {
            Toast.makeText(getActivity(), "Cache data isn't available right now", Toast.LENGTH_LONG).show();
            return;
        }

        if (AppConfig.APP_DEBUG)
            NSLog.e("cacheSearchObj", cacheSearchObj.toString());

        if (cacheSearchObj.containsKey("module")) {
            setDefaultModuleSelected(getView(), (String) cacheSearchObj.get("module"));
        }

        if (cacheSearchObj.containsKey("search")) {
            searchField.setText((String) cacheSearchObj.get("search"));
        }

        if (cacheSearchObj.containsKey("category")) {
            CategoryCustomView.itemCategoryselectedId = (int) cacheSearchObj.get("category");
        }

        if (cacheSearchObj.containsKey("category_selected_index")) {
            if (listCats != null && listCats.size() > 0 && (int) cacheSearchObj.get("category_selected_index") > 0)
                getView().findViewWithTag(listCats.get((int) cacheSearchObj.get("category_selected_index")).getNumCat()).setSelected(true);
        }

        if (cacheSearchObj.containsKey("radius")) {

            int radius = Integer.parseInt((String) cacheSearchObj.get("radius")) / 1000;
            rangeSeekBar.setProgress(radius);
            @SuppressLint("StringFormatMatches") String msg = String.format(getContext().getString(R.string.settings_notification_distance_dis), radius, distance_unit);
            range_seek_bar_text.setText(msg);
        }


        if (cacheSearchObj.containsKey("date_begin")) {
            dateBeginTxt.setText((String) cacheSearchObj.get("date_begin"));
        }


        if (cacheSearchObj.containsKey("openingStatus")) {
            openStatusCB.setChecked(((int) cacheSearchObj.get("openingStatus")) == 1);
        }


        if (cacheSearchObj.containsKey("order_by")) {
            if (cacheSearchObj.get("order_by").equals(Constances.OrderByFilter.NEARBY)) {
                orderByDate.setSelected(false);
                orderByGeo.setSelected(true);
            } else if (cacheSearchObj.get("order_by").equals(Constances.OrderByFilter.RECENT)) {
                orderByGeo.setSelected(false);
                orderByDate.setSelected(true);
            }
        }


        if (cacheSearchObj.containsKey("latitude") && cacheSearchObj.containsKey("longitude")) {
            searchParams.put("latitude", cacheSearchObj.get("latitude"));
            searchParams.put("longitude", cacheSearchObj.get("longitude"));
        }

        if (cacheSearchObj.containsKey("discount_offer_btn")) {
            discount_offer_btn.performClick();
        }

        if (cacheSearchObj.containsKey("discount_selected_value")) {
            getView().findViewWithTag(cacheSearchObj.get("discount_selected_value")).performClick();
        }

        if (cacheSearchObj.containsKey("offer_selected_value")) {
            getView().findViewWithTag(cacheSearchObj.get("offer_selected_value")).performClick();
        }

        if (cacheSearchObj.containsKey("price_offer_btn")) {
            price_offer_btn.performClick();
        }


    }


    @SuppressLint("ResourceType")
    private void setupCategoryFilter(View view) {

        HorizontalScrollView scrollView = view.findViewById(R.id.categoryWrapper);
        LinearLayout.LayoutParams customParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        customParams.setMargins(
                0,
                (int) getResources().getDimension(R.dimen.spacing_small),
                0,
                (int) getResources().getDimension(R.dimen.spacing_small));

        LinearLayout rowBtns = new LinearLayout(getContext());
        rowBtns.setOrientation(LinearLayout.HORIZONTAL);
        rowBtns.setLayoutParams(customParams);

        //get the list of group title first
        listCats = CategoryController.getArrayList();

        //add all cat first
        Category all_categories_menu = new Category(-1,
                getContext().getString(R.string.all_categories_menu), 0, null);
        listCats.add(0, all_categories_menu);


        for (Category cat : listCats) {

            Button catBtn = new Button(getContext());
            catBtn.setPadding(
                    (int) getResources().getDimension(R.dimen.spacing_large),
                    0,
                    (int) getResources().getDimension(R.dimen.spacing_large),
                    0);

            LinearLayout.LayoutParams catBtnLP = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            catBtnLP.setMargins(
                    0,
                    (int) getResources().getDimension(R.dimen.spacing_small),
                    (int) getResources().getDimension(R.dimen.spacing_small),
                    (int) getResources().getDimension(R.dimen.spacing_small)
            );
            catBtn.setLayoutParams(catBtnLP);

            catBtn.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.btn_rect_outline));

            int valueInPixels = (int) (getResources().getDimension(R.dimen.title_size_small) / getResources().getDisplayMetrics().density);
            catBtn.setTextSize(TypedValue.COMPLEX_UNIT_DIP, valueInPixels);
            catBtn.setTextColor( AppCompatResources.getColorStateList(getContext(), R.drawable.btn_rect_outline_text) );
            catBtn.setText(cat.getNameCat());
            catBtn.setTag(cat.getNumCat());
            catBtn.setId(cat.getNumCat());

            catBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    for (int i = 0; i < listCats.size(); i++) {
                        if (view.getId() == listCats.get(i).getNumCat()) {
                            getView().findViewWithTag(listCats.get(i).getNumCat()).setSelected(true);
                            CategoryCustomView.itemCategoryselectedId = cat.getNumCat();
                        } else {
                            getView().findViewWithTag(listCats.get(i).getNumCat()).setSelected(false);
                        }
                    }
                }
            });

            rowBtns.addView(catBtn);
        }


        //Category Listener


        scrollView.addView(rowBtns);


    }


}
