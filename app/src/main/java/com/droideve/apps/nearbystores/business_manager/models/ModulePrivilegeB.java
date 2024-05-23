package com.droideve.apps.nearbystores.business_manager.models;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class ModulePrivilegeB extends RealmObject {
    @PrimaryKey
    public String action;
    public boolean enabled;

}