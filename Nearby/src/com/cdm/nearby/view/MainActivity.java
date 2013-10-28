package com.cdm.nearby.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import com.cdm.nearby.R;
import com.cdm.nearby.common.L;
import com.cdm.nearby.modal.Category;
import com.cdm.nearby.modal.CategoryManager;
import com.cdm.nearby.modal.Location;
import com.cdm.nearby.modal.LocationManager;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity {
    private ImageButton searchButton;
    private ImageButton settingButton;
    private ListView categoryListView;
    private TextView locationTextView;
    private ImageButton locateButton;
    private CategoryListAdapter categoryListAdapter;
    private List<Category> categories = new ArrayList<Category>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        CategoryManager categoryManager = new CategoryManager(this);
        categories = categoryManager.getAll();

        searchButton = (ImageButton) findViewById(R.id.searchButton);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, PoiSearch.class);
                startActivity(intent);
            }
        });

        settingButton = (ImageButton) findViewById(R.id.settingButton);
        settingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, SettingActivity.class);
                startActivity(intent);
            }
        });
        locationTextView = (TextView) findViewById(R.id.locationTextView);

        locateButton = (ImageButton) findViewById(R.id.locateButton);
        locateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                locateMe();
            }
        });

        categoryListView = (ListView) findViewById(R.id.categoryListView);

        categoryListAdapter = new CategoryListAdapter(this, categories);
        categoryListView.setAdapter(categoryListAdapter);

        locateMe();
    }


    private void locateMe() {
        LocationManager locationManager = new LocationManager(this);
        locationManager.requestLocation(new LocationManager.LocationManagetListener() {
            @Override
            public void onReceiveLocation(Location location) {
                L.d("My location " + location.getAddress());

                locationTextView.setText(location.getAddress());
            }
        });

        locationTextView.setText("定位中,请稍等...");
    }

}
