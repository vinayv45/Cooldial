package com.droideve.apps.nearbystores.business_manager.models;

import com.droideve.apps.nearbystores.AppController;
import com.droideve.apps.nearbystores.classes.Module;
import com.droideve.apps.nearbystores.classes.ModulePrivilege;
import com.droideve.apps.nearbystores.classes.User;
import com.droideve.apps.nearbystores.controllers.stores.StoreController;
import com.droideve.apps.nearbystores.controllers.users.UserController;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;


public class BusinessUser extends RealmObject {

    @PrimaryKey
    public int id = 1;

    public User user;
    public RealmList<ModuleB> availableModules;

    public static BusinessUser find(){
        Realm realm = Realm.getInstance(AppController.getBusinessRealmConfig());
        return realm.where(BusinessUser.class).equalTo("id", 1).findFirst();
    }

    public  ModuleB findModule(String name) {
        for (int i = 0; i < availableModules.size(); i++) {
            if(availableModules.get(i).getName().equals(name))
                return availableModules.get(i);
        }
        return  null;
    }


    public ModulePrivilegeB findPrivilege(String module_name, String action) {
        ModuleB module = findModule(module_name);

        for (int i = 0; i < module.getPrivileges().size(); i++) {
            if(module.getPrivileges().get(i).action.equals(action))
                return module.getPrivileges().get(i);
        }
        return  null;
    }


}
