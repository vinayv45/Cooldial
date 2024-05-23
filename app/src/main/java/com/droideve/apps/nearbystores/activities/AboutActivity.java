package com.droideve.apps.nearbystores.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.droideve.apps.nearbystores.R;
import com.droideve.apps.nearbystores.appconfig.AppConfig;
import com.droideve.apps.nearbystores.appconfig.Constances;
import com.droideve.apps.nearbystores.classes.Setting;
import com.droideve.apps.nearbystores.controllers.SettingsController;
import com.droideve.apps.nearbystores.unbescape.html.HtmlEscape;
import com.wuadam.awesomewebview.AwesomeWebView;

import java.net.URI;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;

public class AboutActivity extends AppCompatActivity implements View.OnClickListener {


    @BindView(R.id.about_app_version)
    TextView version;
    @BindView(R.id.about_description)
    TextView description;
    @BindView(R.id.about_version)
    TextView verion_content;

    @BindView(R.id.toolbar_title)
    TextView APP_TITLE_VIEW;
    @BindView(R.id.toolbar_subtitle)
    TextView APP_DESC_VIEW;
    @BindView(R.id.app_bar)
    Toolbar toolbar;



    @BindView(R.id.mail_container)
    LinearLayout mail_container;
    @BindView(R.id.phone_container)
    LinearLayout phone_container;
    @BindView(R.id.address_container)
    LinearLayout address_container;
    @BindView(R.id.facebook_container)
    LinearLayout facebook_container;
    @BindView(R.id.instagram_container)
    LinearLayout instagram_container;
    @BindView(R.id.twitter_container)
    LinearLayout twitter_container;
    @BindView(R.id.telegram_container)
    LinearLayout telegram_container;
    @BindView(R.id.youtube_container)
    LinearLayout youtube_container;
    @BindView(R.id.linkedin_container)
    LinearLayout linkedin_container;


    @BindView(R.id.about_description_content)
    TextView description_content;
    @BindView(R.id.about_mail_content)
    TextView about_mail_content;
    @BindView(R.id.about_phone_content)
    TextView about_phone_content;
    @BindView(R.id.about_address_content)
    TextView about_address_content;
    @BindView(R.id.about_facebook_content)
    TextView about_facebook_content;
    @BindView(R.id.about_instagram_content)
    TextView about_instagram_content;
    @BindView(R.id.about_twitter_content)
    TextView about_twitter_content;
    @BindView(R.id.about_telegram_content)
    TextView about_telegram_content;
    @BindView(R.id.about_youtube_content)
    TextView about_youtube_content;
    @BindView(R.id.about_linkedin_content)
    TextView about_linkedin_content;


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        ButterKnife.bind(this);
        initToolbar();

        APP_TITLE_VIEW.setText(getResources().getString(R.string.app_name));
        APP_TITLE_VIEW.setVisibility(View.VISIBLE);

        setup();

    }

    private void setup(){

        try {
            verion_content.setText(getApplicationContext().getPackageManager().getPackageInfo(getApplicationContext().getPackageName(), 0).versionName);
        } catch (PackageManager.NameNotFoundException e) {}

        //address
        String WEBAPP_about_us = getAppConfigValue("WEBAPP_about_us");
        if(!WEBAPP_about_us.equals("")){

            description.setText(getResources().getString(R.string.app_name));
            description_content.setMovementMethod(LinkMovementMethod.getInstance());

            new AboutActivity.decodeHtmlAboutUsDescription().execute(WEBAPP_about_us);

        }

        //mail
        String WEBAPP_contact_email = getAppConfigValue("WEBAPP_contact_email");
        if(!WEBAPP_contact_email.equals("")){
            about_mail_content.setText(WEBAPP_contact_email);
            mail_container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(Intent.ACTION_SENDTO);
                    intent.setData(Uri.parse("mailto:")); // only email apps should handle this
                    intent.putExtra(Intent.EXTRA_EMAIL, WEBAPP_contact_email);
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivity(intent);
                    }
                }
            });
        }else
            mail_container.setVisibility(View.GONE);

        mail_container.setOnClickListener(this);



        //phone
        String WEBAPP_contact_phone = getAppConfigValue("WEBAPP_contact_phone");
        if(!WEBAPP_contact_phone.equals("")){
            about_phone_content.setText(WEBAPP_contact_phone);
            about_phone_content.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view){
                    onCall("tel:"+WEBAPP_contact_phone);
                }
            });
        }else
            phone_container.setVisibility(View.GONE);

        phone_container.setOnClickListener(this);

        //address
        String WEBAPP_contact_address = getAppConfigValue("WEBAPP_contact_address");
        if(!WEBAPP_contact_address.equals(""))
            about_address_content.setText(WEBAPP_contact_address);
        else
            address_container.setVisibility(View.GONE);


        //facebook
        String WEBAPP_follow_facebook = getAppConfigValue("WEBAPP_follow_facebook");
        if(!WEBAPP_follow_facebook.equals("")){
            about_facebook_content.setText(getUsernameFromUrl(WEBAPP_follow_facebook));
            about_facebook_content.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(WEBAPP_follow_facebook));
                    startActivity(browserIntent);
                }
            });
        } else
            facebook_container.setVisibility(View.GONE);

        facebook_container.setOnClickListener(this);

        //twitter
        String WEBAPP_follow_twitter = getAppConfigValue("WEBAPP_follow_twitter");
        if(!WEBAPP_follow_twitter.equals("")) {
            about_twitter_content.setText(getUsernameFromUrl(WEBAPP_follow_twitter));
            about_twitter_content.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(WEBAPP_follow_twitter));
                    startActivity(browserIntent);
                }
            });
        }else
            twitter_container.setVisibility(View.GONE);

        twitter_container.setOnClickListener(this);

        //instagram
        String WEBAPP_follow_instagram = getAppConfigValue("WEBAPP_follow_instagram");
        if(!WEBAPP_follow_instagram.equals("")) {
            about_instagram_content.setText(getUsernameFromUrl(WEBAPP_follow_instagram));
            about_instagram_content.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(WEBAPP_follow_instagram));
                    startActivity(browserIntent);
                }
            });
        }else
            instagram_container.setVisibility(View.GONE);

        instagram_container.setOnClickListener(this);

        //telegram
        String WEBAPP_follow_telegram = getAppConfigValue("WEBAPP_follow_telegram");
        if(!WEBAPP_follow_telegram.equals("")) {
            about_telegram_content.setText(getUsernameFromUrl(WEBAPP_follow_telegram));
            about_telegram_content.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(WEBAPP_follow_telegram));
                    startActivity(browserIntent);
                }
            });
        }else
            telegram_container.setVisibility(View.GONE);

        telegram_container.setOnClickListener(this);


        //linkedin
        String WEBAPP_follow_linkedin = getAppConfigValue("WEBAPP_follow_linkedin");
        if(!WEBAPP_follow_telegram.equals("")) {
            about_linkedin_content.setText(getUsernameFromUrl(WEBAPP_follow_telegram));
            about_linkedin_content.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(WEBAPP_follow_linkedin));
                    startActivity(browserIntent);
                }
            });
        }else
            linkedin_container.setVisibility(View.GONE);

        linkedin_container.setOnClickListener(this);


        //youtube
        String WEBAPP_follow_youtube = getAppConfigValue("WEBAPP_follow_youtube");
        if(!WEBAPP_follow_youtube.equals("")) {
            about_youtube_content.setText(getString(R.string.youtube));
            about_youtube_content.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(WEBAPP_follow_youtube));
                    startActivity(browserIntent);
                }
            });
        }else
            youtube_container.setVisibility(View.GONE);

        youtube_container.setOnClickListener(this);

    }

    public void onCall(String phone) {
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.CALL_PHONE},
                    Integer.parseInt("123"));
        } else {
            startActivity(new Intent(Intent.ACTION_CALL).setData(Uri.parse(phone)));
        }
    }

    private String getAppConfigValue(String key){
        Setting e =  SettingsController.findSettingFiled(key);
        if(e != null){
            return e.getValue();
        }
        return "";
    }

    private String getUsernameFromUrl(String url){
        final URI uri = URI.create(url);
        final String path = uri.getPath();
        return path.substring(path.lastIndexOf('/') + 1); // will return what you want
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public void initToolbar() {


        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayUseLogoEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        APP_DESC_VIEW.setVisibility(View.GONE);
        APP_TITLE_VIEW.setText(R.string.About_us);
        APP_DESC_VIEW.setVisibility(View.GONE);

    }



    @Override
    public void onClick(View view) {




    }

    private class decodeHtmlAboutUsDescription extends AsyncTask<String, String, String> {
        @Override
        protected void onPostExecute(final String text) {
            super.onPostExecute(text);
            description_content.setText(Html.fromHtml(text));
        }

        @Override
        protected String doInBackground(String... params) {
            return HtmlEscape.unescapeHtml(params[0]);
        }
    }
}




