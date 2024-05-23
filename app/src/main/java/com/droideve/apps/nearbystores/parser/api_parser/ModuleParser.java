package com.droideve.apps.nearbystores.parser.api_parser;



import com.droideve.apps.nearbystores.appconfig.AppContext;
import com.droideve.apps.nearbystores.classes.Module;
import com.droideve.apps.nearbystores.classes.ModulePrivilege;
import com.droideve.apps.nearbystores.parser.Parser;
import com.droideve.apps.nearbystores.parser.tags.Tags;
import com.droideve.apps.nearbystores.utils.NSLog;
import com.droideve.apps.nearbystores.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

import io.realm.RealmList;


public class ModuleParser extends Parser {

    public ModuleParser(JSONObject json) {
        super(json);
    }
    public ModuleParser(Parser parser) {
        this.json = parser.json;
    }
    public RealmList<Module> getModules() {

        RealmList<Module> list = new RealmList<Module>();
        try {

            JSONObject json_array = json.getJSONObject(Tags.RESULT);

            for (int i = 0; i < json_array.length(); i++) {

                JSONObject json_modules = json_array.getJSONObject(i + "");
                Module mModule = new Module();

                mModule.setEnabled(json_modules.getInt("enabled"));
                mModule.setName(json_modules.getString("module_name"));


                if(json_modules.has("privileges")){
                    NSLog.e("privileges",json_modules.getJSONObject("privileges").toString());
                    mModule.setPrivileges(
                            parse_privilege(json_modules.getJSONObject("privileges"))
                    );
                }

                list.add(mModule);

            }

        } catch (JSONException e) {
            if (AppContext.DEBUG)
                e.printStackTrace();
        }


        return list;
    }

   private RealmList<ModulePrivilege> parse_privilege(JSONObject json){

        RealmList<ModulePrivilege> list = new RealmList<ModulePrivilege>();

        try {
            Iterator<String> keys = json.keys();
            while(keys.hasNext()) {
                String key = keys.next();
                ModulePrivilege mModulePrivilege = new ModulePrivilege();
                mModulePrivilege.action = key;
                mModulePrivilege.enabled = json.getBoolean(key);
                list.add(mModulePrivilege);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return list;
    }

}
