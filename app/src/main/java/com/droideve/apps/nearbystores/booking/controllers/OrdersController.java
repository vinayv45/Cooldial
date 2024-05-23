package com.droideve.apps.nearbystores.booking.controllers;


import static com.droideve.apps.nearbystores.appconfig.Constances.API.API_VERSION;
import static com.droideve.apps.nearbystores.appconfig.Constances.BASE_URL_API;

import com.droideve.apps.nearbystores.AppController;
import com.droideve.apps.nearbystores.booking.modals.Reservation;
import com.droideve.apps.nearbystores.network.api_request.ApiRequest;
import com.droideve.apps.nearbystores.network.api_request.ApiRequestListeners;
import com.droideve.apps.nearbystores.parser.Parser;
import com.droideve.apps.nearbystores.parser.tags.Tags;
import com.droideve.apps.nearbystores.utils.NSLog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;

/**
 * Created by Droideve on 11/12/2017.
 */

public class OrdersController {


    public static Reservation findOrderById(int id) {
        Realm realm = Realm.getDefaultInstance();
        return realm.where(Reservation.class).equalTo("id", id).findFirst();
    }


    public static boolean insertOrders(final RealmList<Reservation> list) {

        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                for (Reservation reservation : list) {
                    realm.copyToRealmOrUpdate(reservation);
                }
            }
        });
        return true;
    }


    public static void removeAll() {
        Realm realm = Realm.getDefaultInstance();
        if (realm.isInTransaction()) {
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    RealmResults<Reservation> result = realm.where(Reservation.class).findAll();
                    result.deleteAllFromRealm();
                }
            });
        }

    }


    public static List<BookingPaymentStatus> loadedBookingStatuses = new ArrayList<>();

    public static void loadBookingPaymentStatus(){

        ApiRequest.newPostInstance(BASE_URL_API + "/" + API_VERSION + "/booking/getStatus", new ApiRequestListeners() {
            @Override
            public void onSuccess(Parser parser) {

                loadedBookingStatuses = new ArrayList<>();

                JSONObject result = parser.getResult();

                if(result != null){

                    for (Iterator<String> it = result.keys(); it.hasNext(); ) {
                        String key = it.next();
                        if (result.has(key)) {
                            try {

                                JSONObject json = result.getJSONObject(key);
                                BookingPaymentStatus bookingStatus = new BookingPaymentStatus();
                                bookingStatus.key = key;
                                bookingStatus.label = json.getString("label");
                                bookingStatus.color = json.getString("color");
                                loadedBookingStatuses.add(bookingStatus);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    }

                }

            }

            @Override
            public void onFail(Map<String, String> errors) {
                NSLog.e("ddd",errors.toString());
            }
        });

    }

    public static class BookingPaymentStatus{
        public String label;
        public String color;
        public String key;

        public BookingPaymentStatus() {
            this.label = label;
            this.color = color;
            this.key = key;
        }

        public BookingPaymentStatus(String key,String label, String color) {
            this.label = label;
            this.color = color;
            this.key = key;
        }
    }
}
