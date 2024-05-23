package com.droideve.apps.nearbystores.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.droideve.apps.nearbystores.BuildConfig;
import com.droideve.apps.nearbystores.R;
import com.droideve.apps.nearbystores.appconfig.Constances;
import com.droideve.apps.nearbystores.customView.AlertBottomSheetDialog;
import com.droideve.apps.nearbystores.utils.DataUtils;
import com.droideve.apps.nearbystores.utils.LocaleHelper;
import com.droideve.apps.nearbystores.utils.NSLog;
import com.google.android.material.appbar.AppBarLayout;
import com.wuadam.awesomewebview.AwesomeWebView;

import java.util.Locale;

public class SettingActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {


    private static int getResIdFromAttribute(final Activity activity, final int attr) {
        if (attr == 0) {
            return 0;
        }
        final TypedValue typedvalueattr = new TypedValue();
        activity.getTheme().resolveAttribute(attr, typedvalueattr, true);
        return typedvalueattr.resourceId;
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        AppBarLayout bar;

        LinearLayout root = (LinearLayout) findViewById(android.R.id.list).getParent().getParent().getParent();
        bar = (AppBarLayout) LayoutInflater.from(this).inflate(R.layout.appbar_setting, root, false);
        root.addView(bar, 0);

        Toolbar Tbar = (Toolbar) bar.getChildAt(0);
        Tbar.setClickable(true);

        int resId = getResIdFromAttribute(this, R.attr.homeAsUpIndicator);
        Drawable arrow = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_close_white_24dp, null);
        // arrow.setColorFilter(ResourcesCompat.getColor(getResources(),R.color.white,null), PorterDuff.Mode.MULTIPLY);
        Tbar.setNavigationIcon(arrow);

        TextView title = Tbar.findViewById(R.id.toolbar_title);
        TextView toolbar_description = Tbar.findViewById(R.id.toolbar_subtitle);
        title.setTextColor(ResourcesCompat.getColor(getResources(), R.color.white, null));
        toolbar_description.setTextColor(ResourcesCompat.getColor(getResources(), R.color.white, null));

        toolbar_description.setVisibility(View.GONE);
        title.setText(getString(R.string.settings));


        Tbar.setNavigationOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });

        setupSimplePreferencesScreen();

    }

    private void setupSimplePreferencesScreen() {

        addPreferencesFromResource(R.xml.settings);

        PreferenceScreen screen = getPreferenceScreen();

        Preference app_version = findPreference("app_version");
        app_version.setSummary(BuildConfig.VERSION_NAME);

        //links
        Preference app_term_of_uses = findPreference("app_term_of_uses");
        Preference app_privacy = findPreference("app_privacy");

        //unit
        findPreference("notif_global").setOnPreferenceChangeListener(
                new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        requestNotificationPermission();
                        return true;
                    }
                }

        );

        //unit
        ListPreference distance_unit = (ListPreference) findPreference("distance_unit");
        distance_unit.setOnPreferenceChangeListener(
                new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {

                        //It is required to recreate the activity to reflect the change in UI.
                        finishAffinity();

                        Intent intent = new Intent(getApplicationContext(), SplashActivity.class);
                        startActivity(intent);

                        return true;
                    }
                }

        );

        /*
         *   setup languages selector
         */
        //setupLanguageSelector();

        app_term_of_uses.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                new AwesomeWebView.Builder(SettingActivity.this)
                        .showMenuOpenWith(false)
                        .statusBarColorRes(R.color.colorPrimary)
                        .theme(R.style.FinestWebViewAppTheme)
                        .titleColor(
                                ResourcesCompat.getColor(getResources(), R.color.white, null)
                        ).urlColor(
                                ResourcesCompat.getColor(getResources(), R.color.white, null)
                        ).show(Constances.TERMS_OF_USE_URL);
                return false;
            }
        });

        app_privacy.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                new AwesomeWebView.Builder(SettingActivity.this)
                        .showMenuOpenWith(false)
                        .statusBarColorRes(R.color.colorPrimary)
                        .theme(R.style.FinestWebViewAppTheme)
                        .titleColor(
                                ResourcesCompat.getColor(getResources(), R.color.white, null)
                        ).urlColor(
                                ResourcesCompat.getColor(getResources(), R.color.white, null)
                        ).show(Constances.PRIVACY_POLICY_URL);

                return false;
            }
        });


    }

    private void setupLanguageSelector() {

        ListPreference changeLanguage = (ListPreference) findPreference("changeLanguage");
        changeLanguage.setEnabled(false);
        changeLanguage.setShouldDisableView(true);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String defaultLanguageCode = sharedPref.getString("changeLanguage", "en");
        String defaultLanguageTitle = sharedPref.getString("changeLanguage", "English");

        String storedLanguages = DataUtils.getValue(DataUtils.TAG_LANGUAGES);
        CharSequence[] splinted = storedLanguages.split(",");
        CharSequence[] languageEntries = new CharSequence[splinted.length];
        CharSequence[] languageEntryValues = new CharSequence[splinted.length];

        int i = 0;
        for (CharSequence item : storedLanguages.split(",")) {

            CharSequence[] language = item.toString().split(":");
            languageEntries[i] = language[1] + " (" + language[0] + ")";
            languageEntryValues[i] = language[0];

            if (defaultLanguageCode.equals(language[0])) {
                defaultLanguageTitle = language[1].toString();
            }

            i++;
        }

        changeLanguage.setEntries(languageEntries);
        changeLanguage.setEntryValues(languageEntryValues);
        changeLanguage.setSummary(defaultLanguageTitle);


        changeLanguage.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {

            //Change language setting
                                                             LocaleHelper.setLocale(getApplicationContext(), newValue.toString());

                                                             //It is required to recreate the activity to reflect the change in UI.
                                                             AlertBottomSheetDialog mAlertBottomSheetDialog = AlertBottomSheetDialog.newInstance(SettingActivity.this);
                                                             mAlertBottomSheetDialog.setlisteners(new AlertBottomSheetDialog.Listeners() {
                                                                 @Override
                                                                 public void onConfirm() {
                                                                     finishAffinity();
                                                                     Intent intent = new Intent(getApplicationContext(), SplashActivity.class);
                                                                     startActivity(intent);
                                                                 }

                                                                 @Override
                                                                 public void onDismiss() {

                                                                 }

                                                             });

                                                             mAlertBottomSheetDialog.titleView().setText(getString(R.string.change_language));
                                                             mAlertBottomSheetDialog.bodyView().setText(getString(R.string.to_apply_languageMessage));
                                                             mAlertBottomSheetDialog.show();

                                                             return true;
                                                         }
                                                     }

        );

    }


    private void requestNotificationPermission() {

        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED) {
            // You can use the API that requires the permission.

        } else if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.POST_NOTIFICATIONS)) {
            NSLog.e(this.getClass().getName(), "Camera Permission is required");
        }
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        int radius = PreferenceManager.getDefaultSharedPreferences(this).getInt("distance_value", 100);
        String val = String.valueOf(radius);
        if (radius == 100) {
            val = "+" + radius;
        }
    }


}
