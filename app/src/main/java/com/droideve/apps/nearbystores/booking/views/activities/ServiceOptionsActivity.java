package com.droideve.apps.nearbystores.booking.views.activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;

import com.droideve.apps.nearbystores.R;
import com.droideve.apps.nearbystores.appconfig.Constances;
import com.droideve.apps.nearbystores.booking.views.fragments.ServiceOptionsFragment;

import butterknife.BindView;
import butterknife.ButterKnife;


public class ServiceOptionsActivity extends AppCompatActivity {

    Toolbar toolbar;
    @BindView(R.id.toolbar_title)
    TextView toolbarTitle;
    @BindView(R.id.toolbar_subtitle)
    TextView toolbarDescription;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.standard_fragment);
        ButterKnife.bind(this);

        initToolbar();

        if (getIntent().hasExtra("store")) {
            Bundle bundle = new Bundle();
            bundle.putInt(Constances.ModulesConfig.STORE_MODULE, getIntent().getIntExtra(Constances.ModulesConfig.STORE_MODULE, -1));

            ServiceOptionsFragment fragment = new ServiceOptionsFragment();
            fragment.setArguments(bundle);

            FragmentManager manager = getSupportFragmentManager();
            manager.beginTransaction()
                    .replace(R.id.container, fragment)
                    .commit();

        } else {
            Toast.makeText(this, R.string.something_went_wrong, Toast.LENGTH_LONG).show();
            finish();
        }


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (android.R.id.home == item.getItemId()) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    public void initToolbar() {

        toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayUseLogoEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbarDescription.setVisibility(View.GONE);

        toolbarTitle.setText(R.string.service_booking);


    }
}
