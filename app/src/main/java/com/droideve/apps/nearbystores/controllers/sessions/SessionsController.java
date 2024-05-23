package com.droideve.apps.nearbystores.controllers.sessions;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.droideve.apps.nearbystores.utils.NSLog;

import com.droideve.apps.nearbystores.AppController;
import com.droideve.apps.nearbystores.appconfig.AppConfig;
import com.droideve.apps.nearbystores.classes.Session;
import com.droideve.apps.nearbystores.classes.User;

import io.realm.Realm;


public class SessionsController {

    private static final int aisession = 1;
    private static Session session;

    public static boolean isLogged() {

        Session session = getSession();

        if (session != null && session.isValid()) {
            User user = session.getUser();
            return user != null && user.isValid();
        }

        return false;
    }

    public static Session getSession() {

        try {
            Realm mRealm = Realm.getDefaultInstance();
            session = mRealm.where(Session.class).equalTo("sessionId", aisession).findFirst();
            if (session == null) {
                session = new Session();
                session.setSessionId(aisession);
            }
        } catch (Exception e) {

            if (AppConfig.APP_DEBUG)
                e.printStackTrace();
        }

        return session;
    }


    public static void updateSession(final User user) {
        if (SessionsController.isLogged()) {
            Realm realm = Realm.getDefaultInstance();
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    realm.copyToRealmOrUpdate(user);
                }
            });
        }
    }


    public static Session createSession(final User user,final String token) {
        //save session
        getLocalDatabase.setUserId(user.getId());

        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.where(Session.class).findAll().deleteAllFromRealm();
                realm.where(User.class).findAll().deleteAllFromRealm();
            }
        });

        Session session = getSession();

        if (session != null) {
            session.setUser(user);
            session.setToken(token);
            realm.beginTransaction();
            realm.copyToRealmOrUpdate(session);
            realm.commitTransaction();
        }

        return session;
    }

    public static void logOut() {

        Realm mRealm = Realm.getDefaultInstance();
        mRealm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                mRealm.where(Session.class).findAll().deleteAllFromRealm();
                mRealm.where(User.class).findAll().deleteAllFromRealm();
            }
        });

        getLocalDatabase.setUserId(0);

        GuestController.clear();
    }


    public static class getLocalDatabase {

        public static boolean isLogged() {
            return getUserId() > 0;
        }

        public static int getUserId() {
            SharedPreferences sharedPref = AppController.getInstance().getSharedPreferences("usession", Context.MODE_PRIVATE);
            return sharedPref.getInt("user_id", 0);
        }

        public static void setUserId(int id) {
            SharedPreferences sharedPref = AppController.getInstance().getSharedPreferences("usession", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putInt("user_id", id);
            editor.apply();
        }

        public static int getGuestId() {
            SharedPreferences sharedPref = AppController.getInstance().getSharedPreferences("usession", Context.MODE_PRIVATE);
            return sharedPref.getInt("guest_id", 0);
        }

        public static void setGuestId(int id) {
            SharedPreferences sharedPref = AppController.getInstance().getSharedPreferences("usession", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putInt("guest_id", id);
            editor.apply();

        }

    }


}
