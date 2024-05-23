package com.droideve.apps.nearbystores.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;

import com.droideve.apps.nearbystores.R;
import com.droideve.apps.nearbystores.appconfig.Constances;
import com.droideve.apps.nearbystores.fragments.CouponsFragment;
import com.droideve.apps.nearbystores.fragments.ListEventFragment;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;


public class MyCouponsListActivity extends AppCompatActivity {

    @BindView(R.id.toolbar_title)
    TextView APP_TITLE_VIEW;
    @BindView(R.id.toolbar_subtitle)
    TextView APP_DESC_VIEW;
    @BindView(R.id.app_bar)
    Toolbar toolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple);
        ButterKnife.bind(this);

        //setup toolbar
        initToolbar();

        Bundle b = new Bundle();

        CouponsFragment fragment = new CouponsFragment();
        fragment.setArguments(b);
        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction()
                .replace(R.id.container, fragment)
                .commit();

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

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayUseLogoEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        APP_DESC_VIEW.setVisibility(View.GONE);
        APP_TITLE_VIEW.setText(R.string.my_coupons);
        APP_DESC_VIEW.setVisibility(View.GONE);

    }


    @Override
    protected void onResume() {
        super.onResume();
    }


}
