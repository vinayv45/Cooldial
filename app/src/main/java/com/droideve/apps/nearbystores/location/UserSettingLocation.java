package com.droideve.apps.nearbystores.location;

import com.droideve.apps.nearbystores.utils.Session;

public class UserSettingLocation {

    private Double lat = 0.0;
    private Double lng = 0.0;
    private String loc_name = "";
    private Boolean isCurrentLoc = true;

    final static String KEY_LAT="SessUserLocationLat";
    final static String KEY_LNG="SessUserLocationLng";
    final static String KEY_LOC_NAME="SessUserLocationName";
    final static String KEY_LOC_IS_CURRENT="SessUserLocationIsCurrentLoc";

    public static void saveLoc(UserSettingLocation loc){
        Session.getInstance()
                .saveValue(UserSettingLocation.KEY_LAT, loc.getLat().floatValue())
                .saveValue(UserSettingLocation.KEY_LNG, loc.getLng().floatValue())
                .saveValue(UserSettingLocation.KEY_LOC_NAME, String.valueOf(loc.getLoc_name()))
                .saveValue(UserSettingLocation.KEY_LOC_IS_CURRENT, loc.isCurrentLoc).commit();
    }

    public static void saveLoc(){
        Session.getInstance()
                .saveValue(UserSettingLocation.KEY_LAT, 0.0F)
                .saveValue(UserSettingLocation.KEY_LNG, 0.0F)
                .saveValue(UserSettingLocation.KEY_LOC_NAME, "")
                .saveValue(UserSettingLocation.KEY_LOC_IS_CURRENT, true).commit();
    }

    public static UserSettingLocation getLoc(){
        Session sess = Session.getInstance();
        UserSettingLocation loc = new UserSettingLocation();
        loc.setLoc_name(sess.getValue(UserSettingLocation.KEY_LOC_NAME, "Unknown"));
        loc.setLat(sess.getValue(UserSettingLocation.KEY_LAT, 0.0F));
        loc.setLng(sess.getValue(UserSettingLocation.KEY_LNG, 0.0F));
        loc.setCurrentLoc(sess.getValue(UserSettingLocation.KEY_LOC_IS_CURRENT, true));
        return loc;
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

    public String getLoc_name() {
        return loc_name;
    }

    public void setLoc_name(String loc_name) {
        this.loc_name = loc_name;
    }

    public Boolean isCurrentLoc() {
        return isCurrentLoc;
    }

    public void setCurrentLoc(Boolean currentLoc) {
        isCurrentLoc = currentLoc;
    }
}
