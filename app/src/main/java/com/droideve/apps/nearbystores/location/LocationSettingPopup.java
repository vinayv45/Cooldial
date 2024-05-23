package com.droideve.apps.nearbystores.location;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.location.Geocoder;
import android.view.View;
import android.view.Window;


import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;

import com.droideve.apps.nearbystores.R;
import com.droideve.apps.nearbystores.utils.NSLog;
import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;


import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class LocationSettingPopup {

    public static String LOCATION_PICKED = "locationSettingFinished";
    public static String LOCATION_CHANGED = "locationSettingChanged";

    public static LocationSettingPopup newInstance(Context context,ActivityResultLauncher<Intent> launcher, LACDListener listerner){
        LocationSettingPopup lsp = new LocationSettingPopup(context);
        lsp.listener = listerner;
        lsp.launcher = launcher;
        lsp.setup();
        return lsp;
    }

    protected ActivityResultLauncher<Intent> launcher;
    protected Context context;
    protected Dialog dialog;
    protected LACDListener listener;

    public LocationSettingPopup(Context context) {
        this.context = context;
    }

    private void setup() {


        dialog = new Dialog(this.context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_place_autocomplet);
        dialog.setCancelable(true);
        dialog.findViewById(R.id.change_location_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Set the fields to specify which types of place data to return.
                List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG);
                // Start the autocomplete intent.
                Intent intent = new Autocomplete.IntentBuilder(
                        AutocompleteActivityMode.FULLSCREEN, fields)
                        .build(context);

                //setup launcher
                launcher.launch(intent);

                dialog.dismiss();

            }
        });

        dialog.findViewById(R.id.keep_current_location).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listener != null){
                    listener.onKeepCurrentLoc();
                }

                dialog.dismiss();
            }
        });


    }

    public void result(ActivityResult result){

        if (result.getResultCode() == AutocompleteActivity.RESULT_OK) {
            Place place = Autocomplete.getPlaceFromIntent(result.getData());
                        if (place != null && place.getLatLng() != null) {
                            LocationSettingPopup.MyLoc loc = new LocationSettingPopup.MyLoc();
                            loc.lat = place.getLatLng().latitude;
                            loc.lng = place.getLatLng().longitude;
                            loc.city = place.getName();
                            loc.name = place.getAddress();

                            if(listener != null){
                                listener.onChangeLoc(loc);
                            }
                        }

        } else if (result.getResultCode() == AutocompleteActivity.RESULT_ERROR) {
            // TODO: Handle the showError.
            Status status = Autocomplete.getStatusFromIntent(result.getData());
            NSLog.i("CustomSearchFrag", status.getStatusMessage());
        } else if (result.getResultCode() == AutocompleteActivity.RESULT_CANCELED) {
            // The user canceled the operation.
        }

    }

    public LocationSettingPopup show(){
        dialog.show();
        return this;
    }

    public interface LACDListener {
        void onKeepCurrentLoc();
        void onChangeLoc(MyLoc loc);
    }

    public class MyLoc{

        private String name;
        private Double lat;
        private Double lng;
        private String city;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Double getLat() {
            return lat;
        }

        public void setLat(Double lat) {
            this.lat = lat;
        }

        public Double getLng() {
            return lng;
        }

        public void setLng(Double lng) {
            this.lng = lng;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public MyLoc() {

        }
    }

}
