package com.droideve.apps.nearbystores.classes;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Module extends RealmObject {

    @PrimaryKey
    private String name;
    private int enabled;
    private RealmList<ModulePrivilege> privileges;

    public RealmList<ModulePrivilege> getPrivileges() {
        return privileges;
    }

    public void setPrivileges(RealmList<ModulePrivilege> privileges) {
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




}
