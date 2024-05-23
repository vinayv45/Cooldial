package com.droideve.apps.nearbystores.classes;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class ModulePrivilege extends RealmObject {
    @PrimaryKey
    public String action;
    public boolean enabled;


}