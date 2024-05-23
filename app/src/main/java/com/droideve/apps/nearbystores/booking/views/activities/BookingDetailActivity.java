package com.droideve.apps.nearbystores.booking.views.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.res.ResourcesCompat;

import com.droideve.apps.nearbystores.R;
import com.droideve.apps.nearbystores.activities.StoreDetailActivity;
import com.droideve.apps.nearbystores.appconfig.Constances;
import com.droideve.apps.nearbystores.booking.controllers.OrdersController;
import com.droideve.apps.nearbystores.booking.controllers.parser.ReservationParser;
import com.droideve.apps.nearbystores.booking.controllers.restApis.OrderApis;
import com.droideve.apps.nearbystores.booking.modals.Item;
import com.droideve.apps.nearbystores.booking.modals.Reservation;
import com.droideve.apps.nearbystores.classes.Store;
import com.droideve.apps.nearbystores.controllers.SettingsController;
import com.droideve.apps.nearbystores.controllers.sessions.SessionsController;
import com.droideve.apps.nearbystores.customView.AlertBottomSheetDialog;
import com.droideve.apps.nearbystores.network.api_request.ApiRequest;
import com.droideve.apps.nearbystores.network.api_request.ApiRequestListeners;
import com.droideve.apps.nearbystores.parser.Parser;
import com.droideve.apps.nearbystores.utils.DateUtils;
import com.droideve.apps.nearbystores.utils.NSToast;
import com.droideve.apps.nearbystores.utils.QRCodeUtil;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class BookingDetailActivity extends AppCompatActivity implements OrderApis.OrderRestAPisDelegate {

    Toolbar toolbar;
    @BindView(R.id.toolbar_title)
    TextView toolbarTitle;
    @BindView(R.id.toolbar_subtitle)
    TextView toolbarDescription;

    @BindView(R.id.booking_id)
    TextView order_id;
    @BindView(R.id.delivery_on)
    TextView delivery_on;

    @BindView(R.id.services)
    TextView services;

    @BindView(R.id.items_wrapper)
    LinearLayout item_wrapper;

    @BindView(R.id.store_name)
    TextView store_name;
    @BindView(R.id.owner_address)
    TextView owner_address;

    @BindView(R.id.contact_btn_owner)
    AppCompatButton contact_btn_owner;


    @BindView(R.id.detail_btn_owner)
    AppCompatButton detail_btn_owner;


    @BindView(R.id.order_status)
    TextView order_status;

    @BindView(R.id.payment_status)
    TextView payment_status;

    @BindView(R.id.qrcode_image)
    ImageView qrcode_image;

    @BindView(R.id.qrcode_container)
    LinearLayout qrcode_container;

    @BindView(R.id.bookingResultLayout)
    LinearLayout bookingResultLayout;

    private Reservation mReservation;
    private OrderApis call;
    private Menu mMenu;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.booking_detail_fragment);
        ButterKnife.bind(this);

        initToolbar();

        //delegate a listener to retrieve data
        call = OrderApis.newInstance();
        call.delegate = this;


        loadFromAPi();

        if (getIntent() != null && getIntent().hasExtra("id"))
            mReservation = OrdersController.findOrderById(getIntent().getExtras().getInt("id"));


        if(mReservation != null)
            retrieveDataFromOrder();


    }


    private void loadFromAPi() {

        ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage(getString(R.string.loading));
        pd.show();

        int id = getIntent().getExtras().getInt("id");

        ApiRequest.newPostInstance(Constances.API.API_BOOKING_GET, new ApiRequestListeners() {
            @Override
            public void onSuccess(Parser parser) {
                ReservationParser mReservationParser = new ReservationParser(parser);
                if(parser.getSuccess() == 1){
                    //insert
                    OrdersController.insertOrders(mReservationParser.getOrders());
                    //get from database
                    mReservation = OrdersController.findOrderById(getIntent().getExtras().getInt("id"));
                    //setup views
                    retrieveDataFromOrder();
                }

                //hide progress
                pd.dismiss();
            }
            @Override
            public void onFail(Map<String, String> errors) {
                pd.dismiss();
            }

        }, Map.of(
                "booking_id", String.valueOf(id)
        ));

    }


    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (android.R.id.home == item.getItemId()) {
            finish();
        } else if (item.getItemId() == R.id.report_icon) {

            AlertBottomSheetDialog.newInstance(this).setlisteners(new AlertBottomSheetDialog.Listeners() {
                @Override
                public void onConfirm() {
                    //cancel booking
                    cancelBooking();
                }

                @Override
                public void onDismiss() {

                }
            }).show();

        }

        return super.onOptionsItemSelected(item);
    }

    private void cancelBooking() {

        if(mReservation == null)
            return;

        ApiRequest.newPostInstance(Constances.API.API_UPDATE_BOOKING_CLIENT, new ApiRequestListeners() {
            @Override
            public void onSuccess(Parser parser) {
                if(parser.getSuccess() == 1){
                    NSToast.show(getString(R.string.booking_status_updated));
                    loadFromAPi();
                }
            }

            @Override
            public void onFail(Map<String, String> errors) {

            }
        }, Map.of(
                "user_id", String.valueOf(SessionsController.getSession().getUser().getId()),
                "id", String.valueOf(mReservation.getId()),
                "status", "-1"
        ));


    }

    private void retrieveDataFromOrder() {

        if (mReservation == null)
            return;

        toolbarTitle.setText(String.format(getString(R.string.booking_detail),"#" + mReservation.getId()));

        String inputDate = DateUtils.prepareOutputDate(mReservation.getCreated_at(), "dd MMMM yyyy  hh:mm", this);
        order_id.setText("#" + mReservation.getId());
        delivery_on.setText(inputDate);

        //set status with color
        String[] arrayStatus = mReservation.getStatus().split(";");
        if (arrayStatus.length > 0) {
            order_status.setText(arrayStatus[0].substring(0, 1).toUpperCase() + arrayStatus[0].substring(1));
            if (arrayStatus[1] != null && !arrayStatus[0].equals("null")) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    order_status.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(arrayStatus[1])));
                }
            }
        }

        //set status with color

        if(mReservation.getPayment_status_data().equals("0")){
            payment_status.setText(getString(R.string.unpaid));
        }else {
            String[] paymentStatus = mReservation.getPayment_status_data().split(";");
            if (paymentStatus.length > 0) {

                if (paymentStatus[0] != null && paymentStatus[0].equalsIgnoreCase("cod_paid"))
                    paymentStatus[0] = getString(R.string.paid_cash);
                else if (paymentStatus[0] != null && paymentStatus[0].equalsIgnoreCase("cod"))
                    paymentStatus[0] = getString(R.string.payment_spot);

                payment_status.setText(paymentStatus[0].substring(0, 1).toUpperCase() + paymentStatus[0].substring(1));
                if (paymentStatus.length > 1 && paymentStatus[1] != null && !paymentStatus[0].equals("null")) {
                    payment_status.setTextColor(Color.parseColor(paymentStatus[1]));
                }else {
                    payment_status.setTextColor(ResourcesCompat.getColor(getResources(),R.color.green,null));
                }
            }
        }

        if (mReservation.getItems() != null && mReservation.getItems().size() > 0) {

            StringBuilder servicesTxt = new StringBuilder();
            for (Item item : mReservation.getItems()) {

                servicesTxt.append(item.getName()).append("\n");
                String[] arrayServices = item.getService().split(",");

                if (arrayServices.length > 0) {
                    for (String serv : arrayServices) {
                        serv = serv.trim();
                        servicesTxt.append(" -\t").append(serv).append("\n");
                    }
                    servicesTxt.append("\n");
                }
            }


            services.setText(servicesTxt.toString());
            item_wrapper.setVisibility(View.VISIBLE);
        } else {
            item_wrapper.setVisibility(View.GONE);
        }


        //call apis to retrieve store and customer detail from ids
        HashMap paramsStoreAPI = new HashMap<>();
        paramsStoreAPI.put("store_id", String.valueOf(mReservation.getId_store()));
        call.getStoreDetail(paramsStoreAPI);


        //display qr code
        setupQRCode();



        //update top menu
        updateMenu();


        //display result
        bookingResultLayout.setVisibility(View.VISIBLE);
    }



    public void initToolbar() {

        toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayUseLogoEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        toolbarDescription.setVisibility(View.GONE);
    }


    @Override
    public void onStoreSuccess(Store storeData) {

        store_name.setText(storeData.getName());
        owner_address.setText(storeData.getAddress());

        //button click listener
        contact_btn_owner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String phoneNumber = storeData.getPhone();
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + phoneNumber.trim()));
                if (ActivityCompat.checkSelfPermission(BookingDetailActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    String[] permission = new String[]{Manifest.permission.CALL_PHONE};
                    SettingsController.requestPermissionM(BookingDetailActivity.this, permission);
                    return;
                }
                startActivity(intent);

            }
        });

        detail_btn_owner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(new Intent(BookingDetailActivity.this, StoreDetailActivity.class));
                intent.putExtra("id", storeData.getId());
                startActivity(intent);
                
            }
        });

    }


    @Override
    public void onError(OrderApis object, Map<String, String> errors) {

    }



    void setupQRCode(){

        Bitmap bitmap = QRCodeUtil.generate(this, "booking:" +mReservation.getId());
        //put bitmap inside view
        qrcode_image.setImageBitmap(bitmap);
        //show qr code presenter
        qrcode_image.setVisibility(View.VISIBLE);

    }




    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.home_menu, menu);

        mMenu = menu;

        menu.findItem(R.id.bookmarks_icon).setVisible(false);
        menu.findItem(R.id.share_post).setVisible(false);
        menu.findItem(R.id.report_icon).setVisible(false);

        return true;
    }


    private void updateMenu() {

        if(mMenu == null)
            return;

        if(mReservation == null)
            return;

        if(mReservation.getStatus_id() != 0)
            return;


        mMenu.findItem(R.id.report_icon).setVisible(true);
        mMenu.findItem(R.id.report_icon).setTitle(getString(R.string.cancel));


    }
}
