package com.cdm.nearby.view;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import com.baidu.mapapi.*;
import com.cdm.nearby.R;
import com.cdm.nearby.common.L;
import com.cdm.nearby.modal.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: cdm
 * Date: 2/14/13
 * Time: 9:49 PM
 */
public class PoiListActivity extends BaseBaiduMapActivity {
    private static final int MODE_LIST = 1;
    private static final int MODE_MAP = 2;

    private ImageButton backButton;
    private ImageButton refreshButton;
    private ImageButton mapButton;
    private ImageButton listButton;
    private TextView titleTextView;
    private TextView rangeTextView;
    private View rangeContainer;
    private ListView poiListView;
    private PoiListAdapter poiListAdapter;
    private View footer;
    private MapView poiMapView;
    private MapController mapController;
    private MyPoiOverlay myPoiOverlay;
    private MyLocationOverlay myLocationOverlay;
    private View mapPopView;
    private Button nextPageButton;
    private Button previousPageButton;
    private Button locateMeButton;

    private ViewFlipper poiModeViewFlipper;

    private Category category;
    private List<Poi> pois = new ArrayList<Poi>();
    private int page = 1;
    private int pageCount = 20;
    private int range = 3000;
    private Location myLocation;
    private boolean isLoading = false;
    private int mode;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.poi_list);
        super.initMapActivity();


        LocationManager locationManager = new LocationManager(PoiListActivity.this);
        myLocation = locationManager.getLastLocation();


        footer = layoutInflater.inflate(R.layout.poi_list_footer, null);
        footer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadMoreData();
            }
        });

        poiModeViewFlipper = (ViewFlipper) findViewById(R.id.poiModeViewFlipper);

        backButton = (ImageButton) findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        refreshButton = (ImageButton) findViewById(R.id.refreshButton);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mode == MODE_LIST) {
                    refreshListData();
                } else {
                    refreshMapData();
                }

            }
        });

        mapButton = (ImageButton) findViewById(R.id.mapButton);
        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showListMode();
            }
        });

        listButton = (ImageButton) findViewById(R.id.listButton);
        listButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showMapMode();
            }
        });

        titleTextView = (TextView) findViewById(R.id.titleTextView);

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

        rangeContainer = findViewById(R.id.rangeContailer);
        rangeContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeRange();
            }
        });

        rangeTextView = (TextView) findViewById(R.id.rangeTextView);

        poiMapView = (MapView) findViewById(R.id.poiMapView);
        poiMapView.setDrawOverlayWhenZooming(true);


        mapPopView = layoutInflater.inflate(R.layout.map_pop, null);
        mapPopView.setVisibility(View.GONE);
        poiMapView.addView(mapPopView,
                new MapView.LayoutParams(MapView.LayoutParams.WRAP_CONTENT, MapView.LayoutParams.WRAP_CONTENT,
                        null, MapView.LayoutParams.TOP_LEFT));


        myPoiOverlay = new MyPoiOverlay(getResources().getDrawable(R.drawable.ic_loc_normal));
        poiMapView.getOverlays().add(myPoiOverlay);

        myLocationOverlay = new MyLocationOverlay(getResources().getDrawable(R.drawable.ic_current_loc));
        poiMapView.getOverlays().add(myLocationOverlay);

        mapController = poiMapView.getController();

        nextPageButton = (Button) findViewById(R.id.nextButton);
        nextPageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mapNextPageData();
            }
        });

        previousPageButton = (Button) findViewById(R.id.previousButton);
        previousPageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mapPreviousPageData();
            }
        });

        locateMeButton = (Button) findViewById(R.id.locateMeButton);
        locateMeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gotoMyLocation();
            }
        });

        Intent data = getIntent();
        String code = data.getStringExtra("code");
        CategoryManager categoryManager = new CategoryManager(this);

        try {
            category = categoryManager.getByCode(code);
        } catch (Exception e) {
            L.e(e.getMessage(), e);
            return;
        }

        titleTextView.setText(category.getName());

        showListMode();
    }


    private void refreshListData() {
        if (isLoading) {
            return;
        }

        isLoading = true;

        showLoadingFooter();


        page = 1;

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("刷新中...");
        progressDialog.show();

        LocationManager locationManager = new LocationManager(PoiListActivity.this);
        myLocation = locationManager.getLastLocation();

        final PoiManager poiManager = new PoiManager(PoiListActivity.this);

        AsyncTask<Integer, Integer, Integer> asyncTask = new AsyncTask<Integer, Integer, Integer>() {
            private List<Poi> searchResult = new ArrayList<Poi>();
            @Override
            protected Integer doInBackground(Integer... integers) {
                try {

                    searchResult = poiManager.search(myLocation.getLongitude(), myLocation.getLatitude(), range, page, pageCount, category.getName());
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
                        Toast.makeText(PoiListActivity.this, "暂无数据", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    pois.clear();
                    pois.addAll(searchResult);

                    poiListAdapter.clearPois();
                    poiListAdapter.addPois(searchResult);
                    poiListView.setAdapter(poiListAdapter);
                } else {
                    Toast.makeText(PoiListActivity.this, "刷新失败", Toast.LENGTH_SHORT).show();
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

                    searchResult = poiManager.search(myLocation.getLongitude(), myLocation.getLatitude(), range, page, pageCount, category.getName());
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
                        Toast.makeText(PoiListActivity.this, "没有更多数据", Toast.LENGTH_SHORT).show();
                    } else {
                        pois.addAll(searchResult);
                        poiListAdapter.addPois(searchResult);
                        poiListAdapter.notifyDataSetChanged();
                    }


                } else {
                    Toast.makeText(PoiListActivity.this, "刷新失败", Toast.LENGTH_SHORT).show();
                }
            }
        };

        asyncTask.execute();
    }

    public void changeRange() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final String[] data = new String[]{"1000m内", "2000m内", "3000m内", "4000m内", "5000m内"};
        builder.setItems(data, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                rangeTextView.setText(data[i]);
                switch (i) {
                    case 0:
                        range = 1000;
                        break;
                    case 1:
                        range = 2000;
                        break;
                    case 2:
                        range = 3000;
                        break;
                    case 3:
                        range = 4000;
                        break;
                    case 4:
                        range = 5000;
                        break;
                }

                refreshListData();
            }
        });

        builder.show();
    }

    private void showLoadMoreFooter() {
        TextView titleTextView = (TextView) footer.findViewById(R.id.titleTextView);
        titleTextView.setText("加载更多");
    }

    private void showLoadingFooter() {
        TextView titleTextView = (TextView) footer.findViewById(R.id.titleTextView);
        titleTextView.setText("加载中...");
    }

    private void showListMode() {
        mode = MODE_LIST;

        mapButton.setVisibility(View.GONE);
        listButton.setVisibility(View.VISIBLE);
        poiModeViewFlipper.setDisplayedChild(0);

        refreshListData();
    }

    private void showMapMode() {
        mode = MODE_MAP;

        mapButton.setVisibility(View.VISIBLE);
        listButton.setVisibility(View.GONE);
        poiModeViewFlipper.setDisplayedChild(1);

        refreshMapData();
    }


    private void refreshMapData() {
        if (isLoading) {
            return;
        }

        isLoading = true;

        mapPopView.setVisibility(View.GONE);

        page = 1;

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("刷新中...");
        progressDialog.show();

        LocationManager locationManager = new LocationManager(PoiListActivity.this);
        myLocation = locationManager.getLastLocation();

        final PoiManager poiManager = new PoiManager(PoiListActivity.this);

        AsyncTask<Integer, Integer, Integer> asyncTask = new AsyncTask<Integer, Integer, Integer>() {
            private List<Poi> searchResult = new ArrayList<Poi>();

            @Override
            protected Integer doInBackground(Integer... integers) {
                try {


                    searchResult = poiManager.search(myLocation.getLongitude(), myLocation.getLatitude(), range, page, pageCount, category.getName());
                } catch (Exception e) {
                    L.e(e.getMessage(), e);
                    return -1;
                }

                return 0;
            }

            @Override
            protected void onPostExecute(Integer integer) {
                progressDialog.dismiss();

                isLoading = false;

                if (integer == 0) {
                    if (searchResult.isEmpty()) {
                        Toast.makeText(PoiListActivity.this, "暂无数据", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    pois.clear();
                    pois.addAll(searchResult);

                    int minLat = Integer.MAX_VALUE;
                    int maxLat = Integer.MIN_VALUE;
                    int minLon = Integer.MAX_VALUE;
                    int maxLon = Integer.MIN_VALUE;

                    myPoiOverlay.clearItems();

                    for (Poi poi : pois) {
                        GeoPoint geoPoint = new GeoPoint((int) (poi.getLatitude() * 1E6), (int) (poi.getLongitude() * 1E6));
                        OverlayItem item = new OverlayItem(geoPoint, poi.getName(), poi.getAddress());

                        myPoiOverlay.addItem(item);

                        int lat = geoPoint.getLatitudeE6();
                        int lon = geoPoint.getLongitudeE6();

                        maxLat = Math.max(lat, maxLat);
                        minLat = Math.min(lat, minLat);
                        maxLon = Math.max(lon, maxLon);
                        minLon = Math.min(lon, minLon);

                    }


                    mapController.zoomToSpan(Math.abs(maxLat - minLat), Math.abs(maxLon - minLon));

                    mapController.animateTo(new GeoPoint( (maxLat + minLat)/2,(maxLon + minLon)/2 ));
                } else {
                    Toast.makeText(PoiListActivity.this, "刷新失败", Toast.LENGTH_SHORT).show();
                }
            }
        };

        asyncTask.execute();
    }

    private void gotoMyLocation() {
        GeoPoint myGeoPoint = new GeoPoint((int) (myLocation.getLatitude() * 1E6), (int) (myLocation.getLongitude() * 1E6));
        mapController.animateTo(myGeoPoint);
    }

    private void mapNextPageData() {
        if (isLoading) {
            return;
        }

        mapPopView.setVisibility(View.GONE);

        isLoading = true;

        page++;

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("刷新中...");
        progressDialog.show();

        LocationManager locationManager = new LocationManager(PoiListActivity.this);
        myLocation = locationManager.getLastLocation();

        final PoiManager poiManager = new PoiManager(PoiListActivity.this);

        AsyncTask<Integer, Integer, Integer> asyncTask = new AsyncTask<Integer, Integer, Integer>() {
            private List<Poi> searchResult = new ArrayList<Poi>();

            @Override
            protected Integer doInBackground(Integer... integers) {
                try {
                    searchResult = poiManager.search(myLocation.getLongitude(), myLocation.getLatitude(), range, page, pageCount, category.getName());
                } catch (Exception e) {
                    L.e(e.getMessage(), e);
                    return -1;
                }

                return 0;
            }

            @Override
            protected void onPostExecute(Integer integer) {
                progressDialog.dismiss();

                isLoading = false;

                if (integer == 0) {
                    if (searchResult.isEmpty()) {
                        Toast.makeText(PoiListActivity.this, "暂无数据", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    pois.clear();
                    pois.addAll(searchResult);


                    int minLat = Integer.MAX_VALUE;
                    int maxLat = Integer.MIN_VALUE;
                    int minLon = Integer.MAX_VALUE;
                    int maxLon = Integer.MIN_VALUE;

                    myPoiOverlay.clearItems();

                    for (Poi poi : pois) {
                        GeoPoint geoPoint = new GeoPoint((int) (poi.getLatitude() * 1E6), (int) (poi.getLongitude() * 1E6));
                        OverlayItem item = new OverlayItem(geoPoint, poi.getName(), poi.getAddress());

                        myPoiOverlay.addItem(item);

                        int lat = geoPoint.getLatitudeE6();
                        int lon = geoPoint.getLongitudeE6();

                        maxLat = Math.max(lat, maxLat);
                        minLat = Math.min(lat, minLat);
                        maxLon = Math.max(lon, maxLon);
                        minLon = Math.min(lon, minLon);

                    }

                    mapController.zoomToSpan(Math.abs(maxLat - minLat), Math.abs(maxLon - minLon));

                    mapController.animateTo(new GeoPoint( (maxLat + minLat)/2,(maxLon + minLon)/2 ));

                } else {
                    Toast.makeText(PoiListActivity.this, "刷新失败", Toast.LENGTH_SHORT).show();
                }
            }
        };

        asyncTask.execute();
    }

    private void mapPreviousPageData() {
        if (isLoading) {
            return;
        }

        mapPopView.setVisibility(View.GONE);

        page--;

        if (page == 0) {
            page = 1;
            Toast.makeText(PoiListActivity.this, "已经到第一页", Toast.LENGTH_SHORT).show();
            return;
        }


        isLoading = true;

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("刷新中...");
        progressDialog.show();

        LocationManager locationManager = new LocationManager(PoiListActivity.this);
        myLocation = locationManager.getLastLocation();

        final PoiManager poiManager = new PoiManager(PoiListActivity.this);

        AsyncTask<Integer, Integer, Integer> asyncTask = new AsyncTask<Integer, Integer, Integer>() {
            private List<Poi> searchResult = new ArrayList<Poi>();

            @Override
            protected Integer doInBackground(Integer... integers) {
                try {
                    searchResult = poiManager.search(myLocation.getLongitude(), myLocation.getLatitude(), range, page, pageCount, category.getName());
                } catch (Exception e) {
                    L.e(e.getMessage(), e);
                    return -1;
                }

                return 0;
            }

            @Override
            protected void onPostExecute(Integer integer) {
                progressDialog.dismiss();

                isLoading = false;

                if (integer == 0) {
                    if (searchResult.isEmpty()) {
                        Toast.makeText(PoiListActivity.this, "暂无数据", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    pois.clear();
                    pois.addAll(searchResult);

                    int minLat = Integer.MAX_VALUE;
                    int maxLat = Integer.MIN_VALUE;
                    int minLon = Integer.MAX_VALUE;
                    int maxLon = Integer.MIN_VALUE;

                    myPoiOverlay.clearItems();

                    for (Poi poi : pois) {
                        GeoPoint geoPoint = new GeoPoint((int) (poi.getLatitude() * 1E6), (int) (poi.getLongitude() * 1E6));
                        OverlayItem item = new OverlayItem(geoPoint, poi.getName(), poi.getAddress());

                        myPoiOverlay.addItem(item);

                        int lat = geoPoint.getLatitudeE6();
                        int lon = geoPoint.getLongitudeE6();

                        maxLat = Math.max(lat, maxLat);
                        minLat = Math.min(lat, minLat);
                        maxLon = Math.max(lon, maxLon);
                        minLon = Math.min(lon, minLon);

                    }


                    mapController.zoomToSpan(Math.abs(maxLat - minLat), Math.abs(maxLon - minLon));

                    mapController.animateTo(new GeoPoint( (maxLat + minLat)/2,(maxLon + minLon)/2 ));

                } else {
                    Toast.makeText(PoiListActivity.this, "刷新失败", Toast.LENGTH_SHORT).show();
                }
            }
        };

        asyncTask.execute();
    }

    private void gotoPoiDetail(Poi poi){
        Intent intent = new Intent(this, PoiDetailActivity.class);
        intent.putExtra("poi", poi);
        startActivity(intent);
    }

    class MyLocationOverlay extends ItemizedOverlay<OverlayItem> {
        private Drawable marker;
        private OverlayItem item;

        public MyLocationOverlay(Drawable drawable) {
            super(boundCenter(drawable));
            marker = drawable;

            if (myLocation == null) {
                return;
            }

            GeoPoint myGeoPoint = new GeoPoint((int) (myLocation.getLatitude() * 1E6), (int) (myLocation.getLongitude() * 1E6));
            item = new OverlayItem(myGeoPoint, null, null);

            populate();
        }

        @Override
        protected OverlayItem createItem(int i) {
            return item;
        }

        @Override
        public int size() {
            return 1;
        }
    }

    class MyPoiOverlay extends ItemizedOverlay<OverlayItem> {
        private Drawable marker;
        private List<OverlayItem> items = new ArrayList<OverlayItem>();

        public MyPoiOverlay(Drawable drawable) {
            super(boundCenterBottom(drawable));
            marker = drawable;
        }

        public void clearItems() {
            items.clear();
            populate();
        }

        public void addItem(OverlayItem item) {
            items.add(item);
            populate();
        }

        @Override
        protected OverlayItem createItem(int i) {
            return items.get(i);
        }

        @Override
        public int size() {
            return items.size();
        }

        @Override
        // 处理当点击事件
        protected boolean onTap(int i) {
            OverlayItem item = items.get(i);

            setFocus(item);
            // 更新气泡位置,并使之显示
            GeoPoint pt = item.getPoint();

            TextView nameTextView = (TextView) mapPopView.findViewById(R.id.nameTextView);
            TextView addressTextView = (TextView) mapPopView.findViewById(R.id.addressTextView);

            nameTextView.setText(item.getTitle());
            addressTextView.setText(item.getSnippet());

            MapView.LayoutParams layoutParams = new MapView.LayoutParams(MapView.LayoutParams.WRAP_CONTENT, MapView.LayoutParams.WRAP_CONTENT, pt, MapView.LayoutParams.BOTTOM_CENTER);
            layoutParams.y -= marker.getIntrinsicHeight();

            poiMapView.updateViewLayout(mapPopView, layoutParams);
            mapPopView.setVisibility(View.VISIBLE);

            final Poi poi = pois.get(i);
            mapPopView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    gotoPoiDetail(poi);
                }
            });

            return true;
        }

        @Override
        public boolean onTap(GeoPoint geoPoint, MapView mapView) {
            mapPopView.setVisibility(View.GONE);
            return super.onTap(geoPoint, mapView);
        }
    }

}