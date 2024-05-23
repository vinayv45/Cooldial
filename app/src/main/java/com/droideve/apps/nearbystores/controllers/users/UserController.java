package com.droideve.apps.nearbystores.controllers.users;

import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;

import com.droideve.apps.nearbystores.activities.LoginV2Activity;
import com.droideve.apps.nearbystores.network.api_request.ApiRequest;
import com.droideve.apps.nearbystores.network.api_request.ApiRequestListeners;
import com.droideve.apps.nearbystores.parser.Parser;
import com.droideve.apps.nearbystores.utils.NSLog;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.droideve.apps.nearbystores.AppController;
import com.droideve.apps.nearbystores.R;
import com.droideve.apps.nearbystores.activities.CustomSearchActivity;
import com.droideve.apps.nearbystores.activities.MainActivity;
import com.droideve.apps.nearbystores.appconfig.AppConfig;
import com.droideve.apps.nearbystores.appconfig.Constances;
import com.droideve.apps.nearbystores.classes.User;
import com.droideve.apps.nearbystores.controllers.sessions.SessionsController;
import com.droideve.apps.nearbystores.network.VolleySingleton;
import com.droideve.apps.nearbystores.network.api_request.SimpleRequest;
import com.droideve.apps.nearbystores.parser.api_parser.UserParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import io.realm.Realm;
import io.realm.RealmList;

/**
 * Created by Droideve on 7/13/2017.
 */

public class UserController {

    private static int nbrOfCheck = 0;

    public static boolean insertUsers(final RealmList<User> list) {

        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.copyToRealmOrUpdate(list);
            }
        });
        return true;

    }

    public static void checkUserConnection(final FragmentActivity context) {
        checkUserWithThread(context);
    }

    public static void checkUserWithThread(final FragmentActivity context) {

        if (nbrOfCheck > 0)
            return;

        if(!SessionsController.isLogged())
            return;

        User user = SessionsController.getSession().getUser();
        final String email = user.getEmail();
        final String userid = String.valueOf(user.getId());
        final String username = user.getUsername();

        ApiRequest.newPostInstance(Constances.API.API_USER_CHECK_CONNECTION, new ApiRequestListeners() {
            @Override
            public void onSuccess(Parser parser) {

                UserParser mUserParser = new UserParser(parser);
                if (mUserParser.getSuccess() == 0 || mUserParser.getSuccess() == -1) {
                    userLogoutAlert(context);
                    nbrOfCheck = 0;
                } else {
                    RealmList<User> list = mUserParser.getUser();
                    if (list.size() > 0) {
                        SessionsController.createSession(list.get(0),list.get(0).getToken());
                    }
                    nbrOfCheck++;
                }
            }

            @Override
            public void onFail(Map<String, String> errors) {

            }
        }, Map.of(
                "email", email ,
                "userid", userid,
                "username", username
        ));



    }


    public static void userLogoutAlert(final FragmentActivity activity) {

        new android.app.AlertDialog.Builder(activity)
                .setTitle(activity.getString(R.string.Logout) + "!")
                .setMessage(activity.getString(R.string.logout_alert))
                .setPositiveButton(activity.getString(R.string.Login), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // continue with delete

                        SessionsController.logOut();
                        ActivityCompat.finishAffinity(activity);
                        activity.startActivity(new Intent(activity, LoginV2Activity.class));

                    }
                })
                .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        SessionsController.logOut();
                        ActivityCompat.finishAffinity(activity);
                        activity.startActivity(new Intent(activity, MainActivity.class));

                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();


    }
}
