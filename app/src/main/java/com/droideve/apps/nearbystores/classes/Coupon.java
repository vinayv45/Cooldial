package com.droideve.apps.nearbystores.classes;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Coupon extends RealmObject {

    @PrimaryKey
    private int id;
    private int offer_id;
    private String label;
    private String code;
    private int status;
    private Images image;
    private String user_coupon;
    private String store_name;
    private int store_id;

    public String getUser_coupon() {
        return user_coupon;
    }

    public void setUser_coupon(String user_coupon) {
        this.user_coupon = user_coupon;
    }

    public String getStore_name() {
        return store_name;
    }

    public void setStore_name(String store_name) {
        this.store_name = store_name;
    }

    public int getStore_id() {
        return store_id;
    }

    public void setStore_id(int store_id) {
        this.store_id = store_id;
    }

    public Images getImage() {
        return image;
    }

    public void setImage(Images image) {
        this.image = image;
    }

    public int getId() {

        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getOffer_id() {
        return offer_id;
    }

    public void setOffer_id(int offer_id) {
        this.offer_id = offer_id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
