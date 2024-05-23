package com.droideve.apps.nearbystores.business_manager.models;

import com.droideve.apps.nearbystores.classes.Module;
import com.droideve.apps.nearbystores.classes.ModulePrivilege;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class ModuleB extends RealmObject {

    @PrimaryKey
    private String name;
    private int enabled;
    private RealmList<ModulePrivilegeB> privileges;
    public RealmList<ModulePrivilegeB> getPrivileges() {
        return privileges;
    }

    public void setPrivileges(RealmList<ModulePrivilegeB> privileges) {
        this.privileges = privileges;
    }

    public int isEnabled() {
        return enabled;
    }

    public void setEnabled(int enabled) {
        this.enabled = enabled;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }



    public static RealmList<ModuleB> copyModules(RealmList<Module> list){

        if(list.size()==0)
            return new RealmList<>();

        RealmList<ModuleB> newList = new RealmList<>();

        for (int i = 0; i < list.size(); i++) {
            ModuleB mModule = new ModuleB();

            mModule.name = list.get(i).getName();
            mModule.privileges = copyPrivileges(list.get(i).getPrivileges());
            mModule.enabled = list.get(i).isEnabled();

            newList.add(mModule);
        }

        return newList;
    }

    public static RealmList<ModulePrivilegeB> copyPrivileges(RealmList<ModulePrivilege> list){

        if(list.size()==0)
            return new RealmList<>();

        RealmList<ModulePrivilegeB> newList = new RealmList<>();

        for (int i = 0; i < list.size(); i++) {

            ModulePrivilegeB mPModule = new ModulePrivilegeB();

            mPModule.action = list.get(i).action;
            mPModule.enabled = list.get(i).enabled;

            newList.add(mPModule);
        }

        return newList;
    }


}
