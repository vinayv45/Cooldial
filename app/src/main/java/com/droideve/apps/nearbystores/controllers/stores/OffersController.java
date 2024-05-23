package com.droideve.apps.nearbystores.controllers.stores;

import com.droideve.apps.nearbystores.classes.Offer;

import java.util.ArrayList;
import java.util.List;

import com.droideve.apps.nearbystores.classes.Store;
import com.droideve.apps.nearbystores.utils.NSLog;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Created by Droideve on 11/12/2017.
 */

public class OffersController {


    public static Offer findOfferById(int id) {
        Realm realm = Realm.getDefaultInstance();
        return realm.where(Offer.class).equalTo("id", id).findFirst();
    }

    public static Offer getOffer(int id) {
        Realm realm = Realm.getDefaultInstance();
        return realm.where(Offer.class).equalTo("id", id).findFirst();
    }

    public static List<Offer> findOffersByStoreId(int id) {
        Realm realm = Realm.getDefaultInstance();
        RealmResults result = realm.where(Offer.class).equalTo("store_id", id)
                .sort("id", Sort.DESCENDING).findAll();
        List<Offer> array = new ArrayList<>();
        array.addAll(result.subList(0, result.size()));
        return array;
    }

    public static RealmList<Offer> list() {

        Realm realm = Realm.getDefaultInstance();
        RealmResults result = realm.where(Offer.class).findAll();

        RealmList<Offer> results = new RealmList<Offer>();
        results.addAll(result.subList(0, result.size()));

        return results;
    }


    public static Offer doSave(final int id, final int status) {

        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        Offer obj = realm.where(Offer.class).equalTo("id", id).findFirst();
        obj.setSaved(status);
        realm.copyToRealmOrUpdate(obj);
        realm.commitTransaction();

        return obj;
    }

    public static void deleteAllOffers(int id) {
        Realm realm = Realm.getDefaultInstance();
        final RealmResults result = realm.where(Offer.class).equalTo("store_id", id)
                .sort("id", Sort.DESCENDING).findAll();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                result.deleteAllFromRealm();
            }
        });
    }

    public static boolean insertOffers(final RealmList<Offer> list) {

        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                for (Offer offer : list) {
                    realm.copyToRealmOrUpdate(offer);
                }
            }
        });
        return true;
    }

    public static boolean save(Offer offer) {

        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.copyToRealmOrUpdate(offer);
            }
        });
        return true;
    }


    public static void removeAll() {

        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                RealmResults result = realm.where(Offer.class).findAll();
                result.deleteAllFromRealm();

            }
        });

    }

}
