package com.droideve.apps.nearbystores.parser.api_parser;


import com.droideve.apps.nearbystores.appconfig.AppContext;
import com.droideve.apps.nearbystores.classes.Images;
import com.droideve.apps.nearbystores.classes.User;
import com.droideve.apps.nearbystores.parser.Parser;
import com.droideve.apps.nearbystores.parser.tags.Tags;

import org.json.JSONException;
import org.json.JSONObject;

import io.realm.RealmList;

public class UserParser extends Parser {

    public UserParser(JSONObject json) {
        super(json);
    }
    public UserParser(Parser parser) {
        this.json = parser.json;
    }
    public RealmList<User> getUser() {

        RealmList<User> list = new RealmList<User>();

        try {

            JSONObject json_array = json.getJSONObject(Tags.RESULT);

            for (int i = 0; i < json_array.length(); i++) {

                JSONObject json_user = json_array.getJSONObject(i + "");
                User user = new User();

                user.setId(json_user.getInt("id_user"));
                user.setName(json_user.getString("name"));


                try {
                    user.setStatus(json_user.getInt("status"));
                } catch (Exception ex) {
                    user.setStatus(json_user.getInt("user_status"));
                }

                user.setConfirmed(json_user.getInt("confirmed"));

                if (json_user.has("distance") && !json_user.isNull("distance"))
                    user.setDistance(json_user.getDouble("distance"));


                user.setUsername(json_user.getString("username"));
                user.setEmail(json_user.getString("email"));
                user.setPhone(json_user.getString("telephone"));
                user.setType(User.TYPE_LOGGED);

                try {
                    user.setAuth(json_user.getString("typeAuth"));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                if (json_user.has("country_name") && !json_user.isNull("country_name"))
                    user.setCountry(json_user.getString("country_name"));

                if (json_user.has("token") && !json_user.isNull("token"))
                    user.setToken(json_user.getString("token"));

                if (json_user.has("blocked") && !json_user.isNull("blocked"))
                    user.setBlocked(json_user.getBoolean("blocked"));

                if (json_user.has("is_online") && !json_user.isNull("is_online"))
                    user.setOnline(json_user.getBoolean("is_online"));


                try {

                    if (json_user.has("images") && !json_user.isNull("images")) {
                        ImagesParser imgp = new ImagesParser(json_user.getJSONObject("images"));
                        if (imgp.getImagesList().size() > 0) {

                            RealmList<Images> listImages = imgp.getImagesList();
                            for (int j = 0; j < listImages.size(); j++) {
                                listImages.get(j).setType(Images.USER);
                            }
                            user.setImages(imgp.getImagesList().get(0));
                        }

                    }

                } catch (Exception e) {
                    if (AppContext.DEBUG)
                        e.printStackTrace();
                }

                list.add(user);

            }

        } catch (JSONException e) {
            if (AppContext.DEBUG)
                e.printStackTrace();
        }


        return list;
    }


}
