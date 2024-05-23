package com.droideve.apps.nearbystores.activities;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.droideve.apps.nearbystores.R;
import com.droideve.apps.nearbystores.appconfig.Constances;
import com.droideve.apps.nearbystores.controllers.sessions.SessionsController;
import com.droideve.apps.nearbystores.network.api_request.ApiRequest;
import com.droideve.apps.nearbystores.network.api_request.ApiRequestListeners;
import com.droideve.apps.nearbystores.parser.Parser;
import com.droideve.apps.nearbystores.parser.api_parser.UserParser;
import com.droideve.apps.nearbystores.parser.tags.Tags;
import com.droideve.apps.nearbystores.utils.QRCodeUtil;

import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MyQrCodeActivity extends AppCompatActivity {


    final String TAG = MyQrCodeActivity.class.toString();
    //Toolbar
    @BindView(R.id.toolbar_title)
    TextView APP_TITLE_VIEW;
    @BindView(R.id.toolbar_subtitle)
    TextView APP_DESC_VIEW;
    @BindView(R.id.app_bar)
    Toolbar toolbar;

    @BindView(R.id.name_of_user)
    TextView name_of_user;

    //Qrocde field
    @BindView(R.id.qrcode_image)
    ImageView qrcode_image;

    //Progress bar
    @BindView(R.id.progressBar)
    ProgressBar progressBar;


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myqrcode);
        ButterKnife.bind(this);

        if(!SessionsController.isLogged()){
            Toast.makeText(this,R.string.login_first,Toast.LENGTH_LONG).show();
            finish();
        }

        //setup toolbar
        initToolbar();

        //setup qr code
        setupQRCode();

        //setup name of user
        setupNameOfUser();

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }else if (item.getItemId() == R.id.refresh) {
            setupQRCode();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.qrcode_menu, menu);
        return true;
    }

    private void setupQRCode(){

        if(!SessionsController.isLogged())
            return;

        getQRCode();

    }

    private void setupNameOfUser(){

        //get name of user
        String name = SessionsController.getSession().getUser().getName();

        //split and get first name
        String[] arrOfStr = name.split(" ");

        //update view
        name_of_user.setText(String.format(getString(R.string.here_is_my_code), arrOfStr[0] ));
    }


    public void initToolbar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayUseLogoEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        APP_DESC_VIEW.setVisibility(View.GONE);
        APP_TITLE_VIEW.setText(R.string.MyQrCode);
        APP_DESC_VIEW.setVisibility(View.GONE);

    }


    private void getQRCode() {

        //show progressbar
        progressBar.setVisibility(View.VISIBLE);

        //hide qr code presenter
        qrcode_image.setVisibility(View.GONE);

        //request new qr code token from server
        int user_id = SessionsController.getSession().getUser().getId();
        ApiRequest.newPostInstance(Constances.API.API_GENERATE_QRCODE_TOKEN, new ApiRequestListeners() {
                    @Override
                    public void onSuccess(Parser parser) {
                        validateResponse(parser);
                    }

                    @Override
                    public void onFail(Map<String, String> errors) {
                        //hide progress bar
                        progressBar.setVisibility(View.VISIBLE);
                    }
                }, Map.of(
                        "user_id", String.valueOf(user_id))
        );


    }

    void validateResponse(Parser parser){

        if(!parser.getStringAttr(Tags.RESULT).equals("")){
            //gererate qr code
            String QRCODE_ACTION = "profile:";
            Bitmap bitmap = QRCodeUtil.generate(this, QRCODE_ACTION +parser.getStringAttr(Tags.RESULT));
            //put bitmap inside view
            qrcode_image.setImageBitmap(bitmap);
            //show qr code presenter
            qrcode_image.setVisibility(View.VISIBLE);
        }

        //hide progress bar
        progressBar.setVisibility(View.GONE);

    }

}
