package com.droideve.apps.nearbystores.business_manager.views.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.budiyev.android.codescanner.AutoFocusMode;
import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.DecodeCallback;
import com.budiyev.android.codescanner.ErrorCallback;
import com.budiyev.android.codescanner.ScanMode;
import com.bumptech.glide.Glide;
import com.droideve.apps.nearbystores.AppController;
import com.droideve.apps.nearbystores.R;
import com.droideve.apps.nearbystores.activities.OfferDetailActivity;
import com.droideve.apps.nearbystores.animation.ImageLoaderAnimation;
import com.droideve.apps.nearbystores.appconfig.AppConfig;
import com.droideve.apps.nearbystores.appconfig.Constances;
import com.droideve.apps.nearbystores.booking.controllers.parser.ReservationParser;
import com.droideve.apps.nearbystores.booking.modals.Reservation;
import com.droideve.apps.nearbystores.booking.views.activities.BookingDetailActivity;
import com.droideve.apps.nearbystores.business_manager.api.BusinessApiRequest;
import com.droideve.apps.nearbystores.business_manager.models.BusinessUser;
import com.droideve.apps.nearbystores.classes.Coupon;
import com.droideve.apps.nearbystores.classes.User;
import com.droideve.apps.nearbystores.controllers.SettingsController;
import com.droideve.apps.nearbystores.network.api_request.ApiRequestListeners;
import com.droideve.apps.nearbystores.parser.Parser;
import com.droideve.apps.nearbystores.parser.api_parser.CouponParser;
import com.droideve.apps.nearbystores.parser.api_parser.UserParser;
import com.droideve.apps.nearbystores.utils.NSLog;
import com.droideve.apps.nearbystores.utils.NSToast;
import com.github.siyamed.shapeimageview.CircularImageView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.zxing.Result;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;


public class QRCodeScannerActivity extends AppCompatActivity implements DecodeCallback, ErrorCallback {

    String TAG = QRCodeScannerActivity.class.getName();
    @BindView(R.id.scanner_view)
    CodeScannerView scannerView;
    @BindView(R.id.app_bar)
    Toolbar toolbar;

    private TextView toolbar_title;
    private TextView toolbar_desc;
    private CodeScanner codeScanner;

    public static void startView(Context context, ActivityResultLauncher<Intent> launcher, int business_user_id){
        Intent intent = new Intent(context, QRCodeScannerActivity.class);
        intent.putExtra("business_user_id",business_user_id);
        launcher.launch(intent);
    }

    private int business_user_id;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcodescanner);
        ButterKnife.bind(this);

        business_user_id = getIntent().getExtras().getInt("business_user_id");

        //setup top toolbar
        initToolbar();

        //setup camera
        setupCameraScanner();

        //get logged user
        getSessionFromDB();



    }


    private BusinessUser mBusinessUser = null;
    private void getSessionFromDB(){

        Realm realm = Realm.getInstance(AppController.getBusinessRealmConfig());
        mBusinessUser = realm.where(BusinessUser.class).equalTo("id", 1).findFirst();

    }

    private BottomSheetDialog loaderBottomSheetDialog;
    private BottomSheetDialog resultBottomSheetDialog;



    private void setupCameraScanner(){

        codeScanner = new CodeScanner(this, scannerView);
        // Parameters (default values)
        codeScanner.setCamera(CodeScanner.CAMERA_BACK); // or CAMERA_FRONT or specific camera id
        codeScanner.setFormats(CodeScanner.ALL_FORMATS);  // list of type BarcodeFormat,
        // ex. listOf(BarcodeFormat.QR_CODE)

        codeScanner.setAutoFocusMode(AutoFocusMode.SAFE);  // or CONTINUOUS
        codeScanner.setScanMode(ScanMode.SINGLE);// or CONTINUOUS or PREVIEW
        codeScanner.setAutoFocusEnabled(true);// Whether to enable auto focus or not
        codeScanner.setFlashEnabled(false);

        codeScanner.setDecodeCallback(this);
        codeScanner.setErrorCallback(this);

        //request camera permission from user
        requestPermission();

        //test
        if(AppConfig.APP_DEBUG){
            test("profile:697ef061205025dfd1f6f15a808298ca");
        }
    }

    void test(String code){

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

            }
        });

        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do something after 100ms
                treatmentDecodedData(code);
            }
        }, 3000);

    }

    private void requestPermission(){

        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED) {
            // You can use the API that requires the permission.
            codeScanner.startPreview();
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {
            NSLog.e(this.getClass().getName(),"Camera Permission is required");
        } else{
            // You can directly ask for the permission.
            // The registered ActivityResultCallback gets the result of this request.
            requestPermissionLauncher.launch(
                    Manifest.permission.CAMERA);
        }
    }

    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    codeScanner.startPreview();
                } else {
                    NSLog.e(this.getClass().getName(),"Camera Permission is required");
                }
            });


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (android.R.id.home == item.getItemId()) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void initToolbar() {

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayUseLogoEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        Drawable bmicon = ResourcesCompat.getDrawable(getResources(),R.drawable.ic_close_white_24dp,null);
        DrawableCompat.setTint(
                DrawableCompat.wrap(bmicon),
                ContextCompat.getColor(this, R.color.white)
        );

        toolbar.setNavigationIcon(bmicon);

        toolbar_title = toolbar.findViewById(R.id.toolbar_title);
        toolbar_title.setText(getString(R.string.qr_code_scanner));
        toolbar_title.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        toolbar_desc = toolbar.findViewById(R.id.toolbar_subtitle);
        toolbar_desc.setVisibility(View.GONE);
    }

    @Override
    public void onDecoded(@NonNull Result result) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                treatmentDecodedData(result.getText());
            }
        });
    }

    @Override
    public void onError(@NonNull Throwable thrown) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        codeScanner.startPreview();
    }

    @Override
    protected void onPause() {
        codeScanner.releaseResources();
        super.onPause();
    }


    private void treatmentDecodedData(String decodedContent){

        String[] data = decodedContent.split(":");

        if(data[0].equals("coupon")){

            if(!SettingsController.isModuleEnabled("qrcoupon")){
                codeScanner.startPreview();
                return;
            }

            showLoaderDialog(getString(R.string.checking_value_coupon));
            //call api for checking profile
            BusinessApiRequest.newPostInstance(Constances.API.API_CHECK_COUPON, new ApiRequestListeners() {
                @Override
                public void onSuccess(Parser parser) {

                    if(parser.getSuccess() == 1){
                        parse_user_coupon(parser);
                    }else {
                        codeScanner.startPreview();
                        loaderBottomSheetDialog.dismiss();
                        NSToast.show(getString(R.string.coupon_not_valid));
                    }

                }

                @Override
                public void onFail(Map<String, String> errors) {
                    NSLog.e(TAG,"onFail="+errors.toString());
                    NSToast.show(getString(R.string.something_went_wrong));
                }

            }, Map.of(
                    "coupon_code", data[1],
                    "client_id", data[2],
                    "business_user_id", String.valueOf(mBusinessUser.user.getId())
            ));

        }else if(data[0].equals("profile")){

            showLoaderDialog(getString(R.string.checking_value_profile));

            //call api for checking profile
            BusinessApiRequest.newPostInstance(Constances.API.API_FIND_USER_BY_TOKEN, new ApiRequestListeners() {
                @Override
                public void onSuccess(Parser parser) {

                    if(parser.getSuccess() == 1){
                        parse_user_profile(parser);
                    }else {
                        codeScanner.startPreview();
                        loaderBottomSheetDialog.dismiss();
                        NSToast.show(getString(R.string.user_not_found));
                    }

                }

                @Override
                public void onFail(Map<String, String> errors) {
                    NSLog.e(TAG,"onFail="+errors.toString());
                    NSToast.show(getString(R.string.something_went_wrong));
                }

            }, Map.of(
                    "token", data[1],
                    "type", "tokenUserAuth",
                    "business_user_id", String.valueOf(mBusinessUser.user.getId())
            ));


        }else if(data[0].equals("booking")){

            if(!SettingsController.isModuleEnabled("booking")){
                codeScanner.startPreview();
                return;
            }

            //call api for checking booking
            showLoaderDialog(getString(R.string.checking_value_booking));

            BusinessApiRequest.newPostInstance(Constances.API.API_CHECK_BOOKING, new ApiRequestListeners() {
                @Override
                public void onSuccess(Parser parser) {

                    if(parser.getSuccess() == 1){
                        parse_booking(parser);
                    }else {

                        codeScanner.startPreview();
                        loaderBottomSheetDialog.dismiss();

                        NSToast.show(getString(R.string.booking_not_found));

                        NSLog.e(Constances.API.API_CHECK_BOOKING,parser.getErrors().toString());
                    }

                }

                @Override
                public void onFail(Map<String, String> errors) {
                    NSLog.e(TAG,"onFail="+errors.toString());
                    NSToast.show(getString(R.string.coupon_not_valid));
                }

            }, Map.of(
                    "id", data[1],
                    "business_user_id", String.valueOf(business_user_id)
            ));

        }else{
            codeScanner.startPreview();
        }

    }

    private void parse_user_coupon(Parser parser) {

        CouponParser couponParser = new CouponParser(parser);

        if(couponParser.getSuccess() == 0){
            NSToast.show(getString(R.string.coupon_not_valid));
            return;
        }

        if(couponParser.getCoupons().size() == 0){
            NSToast.show(getString(R.string.coupon_not_valid));
            return;
        }

        //show dialog
        showScannedResultForCoupon(couponParser.getCoupons().get(0));

    }

    private void parse_user_profile(Parser parser) {

        UserParser mUserParser = new UserParser(parser);
        showScannedResultForProfile(mUserParser.getUser().get(0));

    }

    private void parse_booking(Parser parser) {

        ReservationParser mReservationParser = new ReservationParser(parser);

        if(mReservationParser.getOrders().size()==0){
            loaderBottomSheetDialog.dismiss();
            NSToast.show(getString(R.string.booking_not_found));
            return;
        }

        showScannedResultForBooking(mReservationParser.getOrders().get(0));

    }

    private void showLoaderDialog(String label) {

        loaderBottomSheetDialog = new BottomSheetDialog(this);
        loaderBottomSheetDialog.setContentView(R.layout.bottom_sheet_scan_loader_lyout);

        loaderBottomSheetDialog.setCancelable(false);

        ((TextView)loaderBottomSheetDialog.findViewById(R.id.label)).setText(label);

        loaderBottomSheetDialog.findViewById(R.id.cancelBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //close the window
                loaderBottomSheetDialog.dismiss();
                //continue scanning
                codeScanner.startPreview();
            }
        });


        loaderBottomSheetDialog.show();

    }

    //show user dialog
    private void showScannedResultForProfile(User profile) {

        resultBottomSheetDialog = new BottomSheetDialog(this);
        resultBottomSheetDialog.setContentView(R.layout.bottom_sheet_scan_profile_result_lyout);

        ((TextView)resultBottomSheetDialog.findViewById(R.id.scan_result_label)).setText(profile.getName());
        ((TextView)resultBottomSheetDialog.findViewById(R.id.scan_result_subLabel)).setText("@"+profile.getUsername());

        if(profile.getImages() != null){
            Glide.with(this).load(profile.getImages().getUrl200_200())
                    .fitCenter().placeholder(ImageLoaderAnimation.glideLoader(this))
                    .into(((CircularImageView)resultBottomSheetDialog.findViewById(R.id.user_image)));
        }

        //check user email
        if (profile.getConfirmed()==1) {
            Drawable verified = new IconicsDrawable(this).icon(CommunityMaterial.Icon.cmd_check_circle).color(ResourcesCompat.getColor(getResources(), R.color.green, null)).sizeDp(22);
            ((TextView)resultBottomSheetDialog.findViewById(R.id.scan_result_description)).setCompoundDrawables(verified,null,null,null);
            ((TextView)resultBottomSheetDialog.findViewById(R.id.scan_result_description)).setCompoundDrawablePadding(10);
            ((TextView)resultBottomSheetDialog.findViewById(R.id.scan_result_description)).setText(getString(R.string.user_verified));
            ((TextView)resultBottomSheetDialog.findViewById(R.id.scan_result_description)).setTextColor(ResourcesCompat.getColor(getResources(),R.color.green,null));
        }else if (profile.getConfirmed()==0) {
            Drawable unverified = new IconicsDrawable(this).icon(CommunityMaterial.Icon2.cmd_information_outline).color(ResourcesCompat.getColor(getResources(), R.color.orange_600, null)).sizeDp(22);
            ((TextView)resultBottomSheetDialog.findViewById(R.id.scan_result_description)).setCompoundDrawables(unverified,null,null,null);
            ((TextView)resultBottomSheetDialog.findViewById(R.id.scan_result_description)).setCompoundDrawablePadding(10);
            ((TextView)resultBottomSheetDialog.findViewById(R.id.scan_result_description)).setText(getString(R.string.user_unverified));
            ((TextView)resultBottomSheetDialog.findViewById(R.id.scan_result_description)).setTextColor(ResourcesCompat.getColor(getResources(),R.color.orange_600,null));
        }

        if (profile.getStatus()==-1) {
            Drawable unverified = new IconicsDrawable(this).icon(CommunityMaterial.Icon.cmd_close).color(ResourcesCompat.getColor(getResources(), R.color.red, null)).sizeDp(22);
            ((TextView)resultBottomSheetDialog.findViewById(R.id.scan_result_description)).setCompoundDrawables(unverified,null,null,null);
            ((TextView)resultBottomSheetDialog.findViewById(R.id.scan_result_description)).setCompoundDrawablePadding(10);
            ((TextView)resultBottomSheetDialog.findViewById(R.id.scan_result_description)).setText(getString(R.string.user_disabled));
            ((TextView)resultBottomSheetDialog.findViewById(R.id.scan_result_description)).setTextColor(ResourcesCompat.getColor(getResources(),R.color.red,null));
        }

        resultBottomSheetDialog.findViewById(R.id.continueBtn).setVisibility(View.GONE);
        resultBottomSheetDialog.findViewById(R.id.validateBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //close the window
                resultBottomSheetDialog.dismiss();
            }
        });

        resultBottomSheetDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                //continue scanning
                codeScanner.startPreview();
            }
        });

        loaderBottomSheetDialog.dismiss();
        resultBottomSheetDialog.show();

    }

    /*
    Booking Dialog
    */
    private void showScannedResultForBooking(Reservation mReservation) {

        resultBottomSheetDialog = new BottomSheetDialog(this);
        resultBottomSheetDialog.setContentView(R.layout.bottom_sheet_scan_booking_result_lyout);

        //booking ID
        ((TextView)resultBottomSheetDialog.findViewById(R.id.booking_id)).setText(
                String.format(getString(R.string.booking_id), mReservation.getId())
        );


        //client Name
        ((TextView)resultBottomSheetDialog.findViewById(R.id.client_name)).setText(mReservation.getClient_name());
        ((TextView)resultBottomSheetDialog.findViewById(R.id.client_name)).setTextColor(ResourcesCompat.getColor(getResources(),R.color.green,null));
        Drawable accountIcon = new IconicsDrawable(this).icon(CommunityMaterial.Icon.cmd_account_outline).color(ResourcesCompat.getColor(getResources(), R.color.green, null)).sizeDp(18);
        ((TextView)resultBottomSheetDialog.findViewById(R.id.client_name)).setCompoundDrawables(accountIcon,null,null,null);
        ((TextView)resultBottomSheetDialog.findViewById(R.id.client_name)).setCompoundDrawablePadding(10);


        //set status with color
        String[] rsvStatus = mReservation.getStatus().split(";");
        if (rsvStatus.length > 0) {

            ((TextView)resultBottomSheetDialog.findViewById(R.id.statusRsv)).setText(rsvStatus[0].substring(0, 1).toUpperCase() + rsvStatus[0].substring(1));
            Drawable booking = new IconicsDrawable(this).icon(CommunityMaterial.Icon.cmd_calendar).color(ResourcesCompat.getColor(getResources(), R.color.white, null)).sizeDp(14);
            ((TextView)resultBottomSheetDialog.findViewById(R.id.statusRsv)).setCompoundDrawables(booking,null,null,null);
            ((TextView)resultBottomSheetDialog.findViewById(R.id.statusRsv)).setCompoundDrawablePadding(10);

            if (rsvStatus[1] != null && !rsvStatus[0].equals("null")) {
                ((TextView)resultBottomSheetDialog.findViewById(R.id.statusRsv)).setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(rsvStatus[1])));
            }
        }

        //payment status
        String[] paymentStatus = mReservation.getPayment_status().split(";");

        if (paymentStatus.length > 1) {

            ((TextView)resultBottomSheetDialog.findViewById(R.id.statusPayment)).setText(paymentStatus[0].substring(0, 1).toUpperCase() + paymentStatus[0].substring(1));
            Drawable booking = new IconicsDrawable(this).icon(CommunityMaterial.Icon.cmd_cash_100).color(ResourcesCompat.getColor(getResources(), R.color.white, null)).sizeDp(14);
            ((TextView)resultBottomSheetDialog.findViewById(R.id.statusPayment)).setCompoundDrawables(booking,null,null,null);
            ((TextView)resultBottomSheetDialog.findViewById(R.id.statusPayment)).setCompoundDrawablePadding(10);

            if (paymentStatus.length>1 &&  paymentStatus[1] != null && !paymentStatus[0].equals("null")) {
                    ((TextView)resultBottomSheetDialog.findViewById(R.id.statusPayment)).setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(paymentStatus[1])));
            }else {
                    ((TextView)resultBottomSheetDialog.findViewById(R.id.statusPayment)).setBackgroundTintList(ColorStateList.valueOf(ResourcesCompat.getColor(getResources(),R.color.green,null)));
            }

        }else{

            ((TextView)resultBottomSheetDialog.findViewById(R.id.statusPayment)).setText(getString(R.string.unpaid));
            Drawable booking = new IconicsDrawable(this).icon(CommunityMaterial.Icon.cmd_cash_100).color(ResourcesCompat.getColor(getResources(), R.color.white, null)).sizeDp(14);
            ((TextView)resultBottomSheetDialog.findViewById(R.id.statusPayment)).setCompoundDrawables(booking,null,null,null);
            ((TextView)resultBottomSheetDialog.findViewById(R.id.statusPayment)).setCompoundDrawablePadding(10);
        }

        //done
        resultBottomSheetDialog.findViewById(R.id.validateBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //close the window
                resultBottomSheetDialog.dismiss();
            }
        });

        //see booking details
        resultBottomSheetDialog.findViewById(R.id.seeMoreDetails).setVisibility(View.VISIBLE);
        resultBottomSheetDialog.findViewById(R.id.seeMoreDetails).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //close the window
                resultBottomSheetDialog.dismiss();

                //open booking details
                Intent intent = new Intent(QRCodeScannerActivity.this, BookingDetailActivity.class);
                intent.putExtra("id", mReservation.getId());
                startActivity(intent);

            }
        });

        resultBottomSheetDialog.findViewById(R.id.updateStatusBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                showBottomSheetBookingUpdateStatus(mReservation);

            }
        });

        resultBottomSheetDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                //continue scanning
                codeScanner.startPreview();
            }
        });

        loaderBottomSheetDialog.dismiss();
        resultBottomSheetDialog.show();

    }

    private void showBottomSheetBookingUpdateStatus(Reservation mReservation){

        BottomSheetDialog bottomDialog = new BottomSheetDialog(this);
        bottomDialog.setCancelable(false);
        bottomDialog.setContentView(R.layout.bottom_sheet_booking_update_status_lyout);
        bottomDialog.show();

        //Update booking Status
        bottomDialog.findViewById(R.id.confirmBooking).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                bottomDialog.dismiss();
                codeScanner.startPreview();

                updateBookingStatus(mReservation,1);
            }
        });


        bottomDialog.findViewById(R.id.cancelBooking).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                bottomDialog.dismiss();
                codeScanner.startPreview();

                updateBookingStatus(mReservation,-1);

            }
        });

        bottomDialog.findViewById(R.id.dismissBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomDialog.dismiss();
                resultBottomSheetDialog.show();
            }
        });

        resultBottomSheetDialog.hide();
    }

    private void updateBookingStatus(Reservation mReservation,int status){

        BusinessApiRequest.newPostInstance(Constances.API.API_UPDATE_BOOKING_BUSINESS, new ApiRequestListeners() {
            @Override
            public void onSuccess(Parser parser) {

                if(parser.getSuccess() == 0){
                    NSToast.show(getString(R.string.something_went_wrong));
                }

            }

            @Override
            public void onFail(Map<String, String> errors) {
                NSLog.e("onFail",errors.toString());
            }
        },  Map.of(
                "id", String.valueOf(mReservation.getId()),
                "business_user_id", String.valueOf(mBusinessUser.user.getId()),
                "status", String.valueOf(status)
        ));

    }

    /*
    Coupon Dialog
    */
    private void showScannedResultForCoupon(Coupon mCoupon) {

        resultBottomSheetDialog = new BottomSheetDialog(this);
        resultBottomSheetDialog.setContentView(R.layout.bottom_sheet_scan_coupon_result_lyout);

        //Label
        ((TextView)resultBottomSheetDialog.findViewById(R.id.coupon_label)).setText(mCoupon.getLabel());

        //client Name
        ((TextView)resultBottomSheetDialog.findViewById(R.id.client_name)).setText(mCoupon.getUser_coupon());
        ((TextView)resultBottomSheetDialog.findViewById(R.id.client_name)).setTextColor(ResourcesCompat.getColor(getResources(),R.color.green,null));
        Drawable accountIcon = new IconicsDrawable(this).icon(CommunityMaterial.Icon.cmd_account_outline).color(ResourcesCompat.getColor(getResources(), R.color.green, null)).sizeDp(18);
        ((TextView)resultBottomSheetDialog.findViewById(R.id.client_name)).setCompoundDrawables(accountIcon,null,null,null);
        ((TextView)resultBottomSheetDialog.findViewById(R.id.client_name)).setCompoundDrawablePadding(10);


        //set status with color
        if(mCoupon.getStatus() == 0){
            ((TextView)resultBottomSheetDialog.findViewById(R.id.statusCpn)).setText(getString(R.string.coupon_unverified));
            ((TextView)resultBottomSheetDialog.findViewById(R.id.statusCpn)).setBackgroundTintList(ColorStateList.valueOf(ResourcesCompat.getColor(getResources(),R.color.orange_600,null)));
        }else if(mCoupon.getStatus() == 1){
            ((TextView)resultBottomSheetDialog.findViewById(R.id.statusCpn)).setText(getString(R.string.coupon_verified));
            ((TextView)resultBottomSheetDialog.findViewById(R.id.statusCpn)).setBackgroundTintList(ColorStateList.valueOf(ResourcesCompat.getColor(getResources(),R.color.green,null)));
        }else if(mCoupon.getStatus() == 2){
            ((TextView)resultBottomSheetDialog.findViewById(R.id.statusCpn)).setText(getString(R.string.coupon_used));
            ((TextView)resultBottomSheetDialog.findViewById(R.id.statusCpn)).setBackgroundTintList(ColorStateList.valueOf(ResourcesCompat.getColor(getResources(),R.color.blue,null)));
        }else if(mCoupon.getStatus() == -1){
            ((TextView)resultBottomSheetDialog.findViewById(R.id.statusCpn)).setText(getString(R.string.coupon_canceled));
            ((TextView)resultBottomSheetDialog.findViewById(R.id.statusCpn)).setBackgroundTintList(ColorStateList.valueOf(ResourcesCompat.getColor(getResources(),R.color.red,null)));
        }


        //done
        resultBottomSheetDialog.findViewById(R.id.validateBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //close the window
                resultBottomSheetDialog.dismiss();
            }
        });

        //see offer details
        resultBottomSheetDialog.findViewById(R.id.seeMoreDetails).setVisibility(View.VISIBLE);
        resultBottomSheetDialog.findViewById(R.id.seeMoreDetails).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //close the window
                resultBottomSheetDialog.dismiss();

                //open booking details
                Intent intent = new Intent(QRCodeScannerActivity.this, OfferDetailActivity.class);
                intent.putExtra("id", mCoupon.getOffer_id());
                startActivity(intent);

            }
        });

        resultBottomSheetDialog.findViewById(R.id.updateStatusBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                showBottomSheetCouponUpdateStatus(mCoupon);

            }
        });

        resultBottomSheetDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                //continue scanning
                codeScanner.startPreview();
            }
        });

        loaderBottomSheetDialog.dismiss();
        resultBottomSheetDialog.show();

    }

    private void showBottomSheetCouponUpdateStatus(Coupon mCoupon){

        BottomSheetDialog bottomDialog = new BottomSheetDialog(this);
        bottomDialog.setCancelable(false);
        bottomDialog.setContentView(R.layout.bottom_sheet_coupon_update_status_lyout);
        bottomDialog.show();

        //Update booking Status
        bottomDialog.findViewById(R.id.mark_as_unverified).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                bottomDialog.dismiss();
                codeScanner.startPreview();

                //call api
                updateCouponStatus(mCoupon,0);

            }
        });


        bottomDialog.findViewById(R.id.mark_as_verified).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                bottomDialog.dismiss();
                codeScanner.startPreview();

                //call api
                updateCouponStatus(mCoupon,1);

            }
        });

        bottomDialog.findViewById(R.id.mark_as_used).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                bottomDialog.dismiss();
                codeScanner.startPreview();

                //call api
                updateCouponStatus(mCoupon,2);

            }
        });


        bottomDialog.findViewById(R.id.mark_as_declined).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                bottomDialog.dismiss();
                codeScanner.startPreview();

                //call api
                updateCouponStatus(mCoupon,-1);

            }
        });

        bottomDialog.findViewById(R.id.dismissBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomDialog.dismiss();
                resultBottomSheetDialog.show();
            }
        });


        resultBottomSheetDialog.hide();
    }

    private void updateCouponStatus(Coupon coupon,int status){

        BusinessApiRequest.newPostInstance(Constances.API.API_UPDATE_COUPON_STATUS, new ApiRequestListeners() {
            @Override
            public void onSuccess(Parser parser) {

                if(parser.getSuccess() == 1){
                    NSLog.e("updateCouponStatus",parser.json.toString());
                }else{
                    NSToast.show(getString(R.string.something_went_wrong));
                }

            }

            @Override
            public void onFail(Map<String, String> errors) {
                NSLog.e("onFail",errors.toString());
            }
        },  Map.of(
                "id", String.valueOf(coupon.getId()),
                "business_user_id", String.valueOf(mBusinessUser.user.getId()),
                "status", String.valueOf(status),
                "offer_id", String.valueOf(coupon.getOffer_id())
        ));

    }

}


