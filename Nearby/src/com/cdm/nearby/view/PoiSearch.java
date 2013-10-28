package com.cdm.nearby.view;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import com.cdm.nearby.R;
import com.cdm.nearby.common.L;
import com.cdm.nearby.modal.Location;
import com.cdm.nearby.modal.LocationManager;
import com.cdm.nearby.modal.Poi;
import com.cdm.nearby.modal.PoiManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: cdm
 * Date: 2/16/13
 * Time: 5:19 PM
 */
public class PoiSearch extends BaseActivity {
    private ImageButton backButton;
    private ImageButton searchButton;
    private EditText keywordEditText;
    private ListView poiListView;
    private PoiListAdapter poiListAdapter;
    private View footer;

    private List<Poi> pois = new ArrayList<Poi>();
    private int page = 1;
    private int pageCount = 20;
    private int range = 5000;
    private Location myLocation;
    private boolean isLoading = false;
    private String keyword;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.poi_search);

        LocationManager locationManager = new LocationManager(PoiSearch.this);
        myLocation = locationManager.getLastLocation();

        footer = layoutInflater.inflate(R.layout.poi_list_footer, null);
        footer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadMoreData();
            }
        });

        backButton = (ImageButton) findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        searchButton = (ImageButton) findViewById(R.id.searchButton);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                search();

            }
        });

        keywordEditText = (EditText) findViewById(R.id.keywordEditText);

        poiListView = (ListView) findViewById(R.id.poiListView);
        poiListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Poi poi = pois.get(i);
                gotoPoiDetail(poi);
            }
        });
        poiListView.addFooterView(footer);

        poiListAdapter = new PoiListAdapter(this);
    }

    private void search() {

        keyword = keywordEditText.getText().toString();

        if (isLoading) {
            return;
        }

        isLoading = true;

        showLoadingFooter();


        page = 1;

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("刷新中...");
        progressDialog.show();

        LocationManager locationManager = new LocationManager(PoiSearch.this);
        myLocation = locationManager.getLastLocation();

        final PoiManager poiManager = new PoiManager(PoiSearch.this);

        AsyncTask<Integer, Integer, Integer> asyncTask = new AsyncTask<Integer, Integer, Integer>() {
            private List<Poi> searchResult = new ArrayList<Poi>();

            @Override
            protected Integer doInBackground(Integer... integers) {
                try {

                    searchResult = poiManager.search(keyword, myLocation.getLongitude(), myLocation.getLatitude(), range, page, pageCount);
                } catch (Exception e) {
                    L.e(e.getMessage(), e);
                    return -1;
                }

                return 0;
            }

            @Override
            protected void onPostExecute(Integer integer) {
                progressDialog.dismiss();
                showLoadMoreFooter();

                isLoading = false;

                if (integer == 0) {

                    if (searchResult.isEmpty()) {
                        Toast.makeText(PoiSearch.this, "暂无数据", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    pois.clear();
                    pois.addAll(searchResult);

                    poiListAdapter.clearPois();
                    poiListAdapter.addPois(searchResult);
                    poiListView.setAdapter(poiListAdapter);
                } else {
                    Toast.makeText(PoiSearch.this, "刷新失败", Toast.LENGTH_SHORT).show();
                }
            }
        };

        asyncTask.execute();


    }

    private void loadMoreData() {
        if (isLoading) {
            return;
        }

        isLoading = true;

        showLoadingFooter();

        LocationManager locationManager = new LocationManager(this);
        myLocation = locationManager.getLastLocation();

        final PoiManager poiManager = new PoiManager(this);

        page++;

        AsyncTask<Integer, Integer, Integer> asyncTask = new AsyncTask<Integer, Integer, Integer>() {
            private List<Poi> searchResult = new ArrayList<Poi>();

            @Override
            protected Integer doInBackground(Integer... integers) {
                try {

                    searchResult = poiManager.search(keyword, myLocation.getLongitude(), myLocation.getLatitude(), range, page, pageCount);
                } catch (Exception e) {
                    L.e(e.getMessage(), e);
                    return -1;
                }

                return 0;
            }

            @Override
            protected void onPostExecute(Integer integer) {

                showLoadMoreFooter();

                isLoading = false;

                if (integer == 0) {

                    if (searchResult.isEmpty()) {
                        Toast.makeText(PoiSearch.this, "没有更多数据", Toast.LENGTH_SHORT).show();
                    } else {
                        pois.addAll(searchResult);
                        poiListAdapter.addPois(searchResult);
                        poiListAdapter.notifyDataSetChanged();
                    }


                } else {
                    Toast.makeText(PoiSearch.this, "刷新失败", Toast.LENGTH_SHORT).show();
                }
            }
        };

        asyncTask.execute();
    }

    private void showLoadMoreFooter() {
        TextView titleTextView = (TextView) footer.findViewById(R.id.titleTextView);
        titleTextView.setText("加载更多");
    }

    private void showLoadingFooter() {
        TextView titleTextView = (TextView) footer.findViewById(R.id.titleTextView);
        titleTextView.setText("加载中...");
    }

    private void gotoPoiDetail(Poi poi){
        Intent intent = new Intent(this, PoiDetailActivity.class);
        intent.putExtra("poi", poi);
        startActivity(intent);
    }
}