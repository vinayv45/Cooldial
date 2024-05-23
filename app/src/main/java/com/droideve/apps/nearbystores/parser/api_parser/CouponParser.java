package com.droideve.apps.nearbystores.parser.api_parser;


import android.content.Context;

import com.droideve.apps.nearbystores.classes.Coupon;
import com.droideve.apps.nearbystores.parser.Parser;
import com.droideve.apps.nearbystores.parser.tags.Tags;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.realm.RealmList;


public class CouponParser extends Parser {

    public CouponParser(JSONObject json) {
        super(json);
    }

    public CouponParser(Parser parser) {
        this.json = parser.json;
    }

    public RealmList<Coupon> getCoupons() {

        RealmList<Coupon> list = new RealmList<Coupon>();

        try {

            JSONObject json_array = json.getJSONObject(Tags.RESULT);

            for (int i = 0; i < json_array.length(); i++) {
                try {

                    JSONObject json_coupon = json_array.getJSONObject(i + "");
                    Coupon coupon = new Coupon();
                    coupon.setId(json_coupon.getInt("id"));
                    coupon.setLabel(json_coupon.getString("label"));
                    coupon.setCode(json_coupon.getString("code"));
                    coupon.setOffer_id(json_coupon.getInt("offer_id"));

                    coupon.setUser_coupon(json_coupon.getString("user_coupon"));
                    coupon.setStore_name(json_coupon.getString("store_name"));
                    coupon.setStore_id(json_coupon.getInt("store_id"));
                    coupon.setStatus(json_coupon.getInt("status"));

                    try {
                        String jsonValues = "";
                        if (!json_coupon.isNull("image")) {

                            jsonValues = json_coupon.getJSONObject("image").toString();
                            JSONObject jsonObject = new JSONObject(jsonValues);
                            ImagesParser imgp = new ImagesParser(jsonObject);

                            if (imgp.getImagesList().size() > 0) {
                                coupon.setImage(imgp.getImagesList().get(0));
                            }
                        }
                    } catch (JSONException jex) {
                        coupon.setImage(null);
                    }

                    list.add(coupon);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }


        return list;
    }


    public Coupon getCoupon() {

        try {


            JSONArray json_array = json.getJSONArray(Tags.RESULT);


            try {
                JSONObject json_coupon = json_array.getJSONObject(0);
                Coupon coupon = new Coupon();
                coupon.setId(json_coupon.getInt("id"));
                coupon.setLabel(json_coupon.getString("label"));
                coupon.setStatus(json_coupon.getInt("status"));
                
                return coupon;
            } catch (JSONException e) {
                e.printStackTrace();
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }


        return null;
    }


}
