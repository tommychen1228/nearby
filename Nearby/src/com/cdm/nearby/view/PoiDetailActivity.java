package com.cdm.nearby.view;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.*;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import com.baidu.mapapi.*;
import com.cdm.nearby.DistanceUtil;
import com.cdm.nearby.R;
import com.cdm.nearby.modal.Location;
import com.cdm.nearby.modal.LocationManager;
import com.cdm.nearby.modal.Poi;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: cdm
 * Date: 2/16/13
 * Time: 10:10 AM
 */
public class PoiDetailActivity extends BaseBaiduMapActivity {


    private static final int MODE_WALK = 1;
    private static final int MODE_BUS = 2;
    private static final int MODE_CAR = 3;

    private ImageButton backButton;
    private ImageButton routeButton;
    private RadioGroup modeRadioGroup;
    private MapView poiMapView;
    private MapController mapController;
    private TextView distanceTextView;
    private TextView nameTextView;
    private TextView addressTextView;
    private LinearLayout phoneContainer;
    private PathOverlay pathOverlay;
    private PinOverlay pinOverlay;
    private RouteOverlay routeOverlay;
    private ProgressDialog progressDialog;

    private Poi poi;
    private int mode;
    private Location myLocation;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.poi_detail);
        super.initMapActivity();

        Intent data = getIntent();
        poi = (Poi) data.getSerializableExtra("poi");

        progressDialog = new ProgressDialog(this);

        backButton = (ImageButton) findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        routeButton = (ImageButton) findViewById(R.id.routeButton);
        routeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (mode){
                    case MODE_WALK:
                        showWalkRouteOverlay();
                        break;
                    case MODE_BUS:
                        //显示公交路线
                        showBusRouteOverlay();
                        break;
                    case MODE_CAR:
                        showCarRouteOverlay();
                        break;
                }
            }
        });

        modeRadioGroup = (RadioGroup) findViewById(R.id.modeRadioGroup);
        modeRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {

                int radioButtonId = radioGroup.getCheckedRadioButtonId();
                View radioButton = radioGroup.findViewById(radioButtonId);
                int idx = radioGroup.indexOfChild(radioButton);

                switch (idx) {
                    case 0:
                        showWalkPathOverlay();
                        break;
                    case 1:
                        showBusPathOverlay();
                        break;
                    case 2:
                        showCarPathOverlay();
                        break;

                }
            }
        });

        distanceTextView = (TextView) findViewById(R.id.distanceTextView);

        nameTextView = (TextView) findViewById(R.id.nameTextView);
        nameTextView.setText(poi.getName());

        addressTextView = (TextView) findViewById(R.id.addressTextView);
        addressTextView.setText(poi.getAddress());

        phoneContainer = (LinearLayout) findViewById(R.id.phoneContainer);
        if ("".equals(poi.getPhone())) {

            TextView emptyPhoneTextView = (TextView) layoutInflater.inflate(R.layout.poi_detail_phone_text_view, phoneContainer, false);
            emptyPhoneTextView.setText("暂无电话");
            phoneContainer.addView(emptyPhoneTextView);

        } else {
            String[] phones = poi.getPhone().split(";");
            for (final String phone : phones) {
                TextView phoneTextView = (TextView) layoutInflater.inflate(R.layout.poi_detail_phone_text_view, phoneContainer, false);
                phoneTextView.setText(phone);
                phoneTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Uri uri = Uri.parse("tel:" + phone);
                        Intent intent = new Intent(Intent.ACTION_CALL, uri);
                        startActivity(intent);
                    }
                });

                phoneContainer.addView(phoneTextView);
            }
        }

        poiMapView = (MapView) findViewById(R.id.poiMapView);
        poiMapView.setDrawOverlayWhenZooming(true);

        mapController = poiMapView.getController();

        LocationManager locationManager = new LocationManager(this);
        myLocation = locationManager.getLastLocation();

        zoomToFit();

        pinOverlay = new PinOverlay();
        poiMapView.getOverlays().add(pinOverlay);
        poiMapView.invalidate();

        modeRadioGroup.check(modeRadioGroup.getChildAt(0).getId());
    }



    class PathOverlay extends Overlay {

        private ArrayList<ArrayList<GeoPoint>> arrayPoints;


        public PathOverlay(ArrayList<ArrayList<GeoPoint>> arrayPoints) {

            if (myLocation == null) {
                return;
            }

            this.arrayPoints = arrayPoints;


        }

        @Override
        public void draw(Canvas canvas, MapView mapView, boolean b) {


            Paint paint = new Paint();
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(5);
            paint.setColor(Color.GREEN);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setStrokeJoin(Paint.Join.ROUND);

            Projection projection = poiMapView.getProjection();

            Path path = new Path();
            Point startPoint = projection.toPixels(arrayPoints.get(0).get(0), null);
            path.moveTo(startPoint.x, startPoint.y);

            for (int i = 0; i < arrayPoints.size(); i++) {
                for (int j = 0; j < arrayPoints.get(i).size(); j++) {
                    Point point = projection.toPixels(arrayPoints.get(i).get(j), null);
                    path.lineTo(point.x, point.y);
                }
            }

            canvas.drawPath(path, paint);

            super.draw(canvas, mapView, b);
        }
    }


    class PinOverlay extends ItemizedOverlay<OverlayItem> {
        private OverlayItem startItem;
        private OverlayItem endItem;
        private OverlayItem myLocationItem;

        public PinOverlay() {
            super(null);

            if (myLocation == null) {
                return;
            }


            GeoPoint startPoint = new GeoPoint((int) (myLocation.getLatitude() * 1E6), (int) (myLocation.getLongitude() * 1E6));

            myLocationItem = new OverlayItem(startPoint, null, null);
            myLocationItem.setMarker(boundCenter(getResources().getDrawable(R.drawable.ic_current_loc)));

            startItem = new OverlayItem(startPoint, null, null);
            startItem.setMarker(boundCenterBottom(getResources().getDrawable(R.drawable.ic_loc_from)));


            GeoPoint endPoint = new GeoPoint((int) (poi.getLatitude() * 1E6), (int) (poi.getLongitude() * 1E6));
            endItem = new OverlayItem(endPoint, null, null);
            endItem.setMarker(boundCenterBottom(getResources().getDrawable(R.drawable.ic_loc_to)));


            populate();

        }

        @Override
        protected OverlayItem createItem(int i) {
            switch (i) {
                case 0:
                    return myLocationItem;
                case 1:
                    return startItem;
                default:
                    return endItem;

            }
        }

        @Override
        public int size() {
            return 3;
        }
    }


    private void zoomToFit() {

        int minLat = Integer.MAX_VALUE;
        int maxLat = Integer.MIN_VALUE;
        int minLon = Integer.MAX_VALUE;
        int maxLon = Integer.MIN_VALUE;


        int lat = (int) (myLocation.getLatitude() * 1E6);
        int lon = (int) (myLocation.getLongitude() * 1E6);

        maxLat = Math.max(lat, maxLat);
        minLat = Math.min(lat, minLat);
        maxLon = Math.max(lon, maxLon);
        minLon = Math.min(lon, minLon);

        lat = (int) (poi.getLatitude() * 1E6);
        lon = (int) (poi.getLongitude() * 1E6);

        maxLat = Math.max(lat, maxLat);
        minLat = Math.min(lat, minLat);
        maxLon = Math.max(lon, maxLon);
        minLon = Math.min(lon, minLon);


        mapController.zoomToSpan(Math.abs(maxLat - minLat), Math.abs(maxLon - minLon));

        mapController.animateTo(new GeoPoint( (maxLat + minLat)/2,(maxLon + minLon)/2 ));

    }

    private void showWalkPathOverlay() {

        mode = MODE_WALK;

        progressDialog.setMessage("获取步行路线中...");
        progressDialog.show();

        distanceTextView.setText(null);

        poiMapView.getOverlays().clear();
        poiMapView.getOverlays().add(pinOverlay);
        poiMapView.invalidate();

        MKSearch search = new MKSearch();
        search.init(mapManager, new MKSearchListener() {
            @Override
            public void onGetPoiResult(MKPoiResult mkPoiResult, int i, int i2) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void onGetTransitRouteResult(MKTransitRouteResult result, int error) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void onGetDrivingRouteResult(MKDrivingRouteResult result, int error) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void onGetWalkingRouteResult(MKWalkingRouteResult result, int error) {

                progressDialog.dismiss();

                if (error != 0 || result == null) {
                    Toast.makeText(PoiDetailActivity.this, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
                    return;
                }


                String distanceInfo = "全程约" + DistanceUtil.convert(poi.getDistance()) + ", 耗时约" + DistanceUtil.calculateTime(DistanceUtil.TYPE_WALK, poi.getDistance());
                distanceTextView.setText(distanceInfo);

                MKRoutePlan plan = result.getPlan(0);
                MKRoute route = plan.getRoute(0);

                pathOverlay = new PathOverlay(route.getArrayPoints());
                poiMapView.getOverlays().add(pathOverlay);
                poiMapView.invalidate();

            }

            @Override
            public void onGetAddrResult(MKAddrInfo mkAddrInfo, int i) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void onGetBusDetailResult(MKBusLineResult mkBusLineResult, int i) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void onGetSuggestionResult(MKSuggestionResult mkSuggestionResult, int i) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void onGetRGCShareUrlResult(String s, int i) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void onGetPoiDetailSearchResult(int i, int i2) {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        });


        GeoPoint startPoint = new GeoPoint((int) (myLocation.getLatitude() * 1E6), (int) (myLocation.getLongitude() * 1E6));
        GeoPoint endPoint = new GeoPoint((int) (poi.getLatitude() * 1E6), (int) (poi.getLongitude() * 1E6));

        MKPlanNode startNode = new MKPlanNode();
        startNode.pt = startPoint;
        MKPlanNode endNode = new MKPlanNode();
        endNode.pt = endPoint;

        search.walkingSearch(myLocation.getCity(), startNode, myLocation.getCity(), endNode);
    }

    private void showBusPathOverlay() {

        mode = MODE_BUS;

        progressDialog.setMessage("获取公交路线中...");
        progressDialog.show();

        distanceTextView.setText(null);

        poiMapView.getOverlays().clear();
        poiMapView.getOverlays().add(pinOverlay);
        poiMapView.invalidate();

        MKSearch search = new MKSearch();
        search.init(mapManager, new MKSearchListener() {
            @Override
            public void onGetPoiResult(MKPoiResult mkPoiResult, int i, int i2) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void onGetTransitRouteResult(MKTransitRouteResult result, int error) {
                progressDialog.dismiss();

                if (error != 0 || result == null) {
                    Toast.makeText(PoiDetailActivity.this, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
                    return;
                }


                String distanceInfo = "全程约" + DistanceUtil.convert(poi.getDistance()) + ", 耗时约" + DistanceUtil.calculateTime(DistanceUtil.TYPE_BUS, poi.getDistance());
                distanceTextView.setText(distanceInfo);

                MKTransitRoutePlan plan = result.getPlan(0);
                MKRoute route = plan.getRoute(0);

                pathOverlay = new PathOverlay(route.getArrayPoints());
                poiMapView.getOverlays().add(pathOverlay);
                poiMapView.invalidate();
            }

            @Override
            public void onGetDrivingRouteResult(MKDrivingRouteResult result, int error) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void onGetWalkingRouteResult(MKWalkingRouteResult result, int error) {


            }

            @Override
            public void onGetAddrResult(MKAddrInfo mkAddrInfo, int i) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void onGetBusDetailResult(MKBusLineResult mkBusLineResult, int i) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void onGetSuggestionResult(MKSuggestionResult mkSuggestionResult, int i) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void onGetRGCShareUrlResult(String s, int i) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void onGetPoiDetailSearchResult(int i, int i2) {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        });


        GeoPoint startPoint = new GeoPoint((int) (myLocation.getLatitude() * 1E6), (int) (myLocation.getLongitude() * 1E6));
        GeoPoint endPoint = new GeoPoint((int) (poi.getLatitude() * 1E6), (int) (poi.getLongitude() * 1E6));

        MKPlanNode startNode = new MKPlanNode();
        startNode.pt = startPoint;
        MKPlanNode endNode = new MKPlanNode();
        endNode.pt = endPoint;

        search.transitSearch(myLocation.getCity(), startNode, endNode);
    }

    private void showCarPathOverlay() {

        mode = MODE_CAR;

        progressDialog.setMessage("获取驾车路线中...");
        progressDialog.show();

        distanceTextView.setText(null);

        poiMapView.getOverlays().clear();
        poiMapView.getOverlays().add(pinOverlay);
        poiMapView.invalidate();

        MKSearch search = new MKSearch();
        search.init(mapManager, new MKSearchListener() {
            @Override
            public void onGetPoiResult(MKPoiResult mkPoiResult, int i, int i2) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void onGetTransitRouteResult(MKTransitRouteResult result, int error) {

            }

            @Override
            public void onGetDrivingRouteResult(MKDrivingRouteResult result, int error) {
                progressDialog.dismiss();

                if (error != 0 || result == null) {
                    Toast.makeText(PoiDetailActivity.this, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
                    return;
                }

                String distanceInfo = "全程约" + DistanceUtil.convert(poi.getDistance()) + ", 耗时约" + DistanceUtil.calculateTime(DistanceUtil.TYPE_CAR, poi.getDistance());
                distanceTextView.setText(distanceInfo);

                MKRoutePlan plan = result.getPlan(0);
                MKRoute route = plan.getRoute(0);

                pathOverlay = new PathOverlay(route.getArrayPoints());
                poiMapView.getOverlays().add(pathOverlay);
                poiMapView.invalidate();

            }

            @Override
            public void onGetWalkingRouteResult(MKWalkingRouteResult result, int error) {


            }

            @Override
            public void onGetAddrResult(MKAddrInfo mkAddrInfo, int i) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void onGetBusDetailResult(MKBusLineResult mkBusLineResult, int i) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void onGetSuggestionResult(MKSuggestionResult mkSuggestionResult, int i) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void onGetRGCShareUrlResult(String s, int i) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void onGetPoiDetailSearchResult(int i, int i2) {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        });


        GeoPoint startPoint = new GeoPoint((int) (myLocation.getLatitude() * 1E6), (int) (myLocation.getLongitude() * 1E6));
        GeoPoint endPoint = new GeoPoint((int) (poi.getLatitude() * 1E6), (int) (poi.getLongitude() * 1E6));

        MKPlanNode startNode = new MKPlanNode();
        startNode.pt = startPoint;
        MKPlanNode endNode = new MKPlanNode();
        endNode.pt = endPoint;

        search.drivingSearch(myLocation.getCity(), startNode, myLocation.getCity(), endNode);
    }



    private void showWalkRouteOverlay() {

        progressDialog.setMessage("获取步行路线中...");
        progressDialog.show();

        poiMapView.getOverlays().clear();
        poiMapView.getOverlays().add(pinOverlay);
        poiMapView.invalidate();

        MKSearch search = new MKSearch();
        search.init(mapManager, new MKSearchListener() {
            @Override
            public void onGetPoiResult(MKPoiResult mkPoiResult, int i, int i2) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void onGetTransitRouteResult(MKTransitRouteResult result, int error) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void onGetDrivingRouteResult(MKDrivingRouteResult result, int error) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void onGetWalkingRouteResult(MKWalkingRouteResult result, int error) {

                progressDialog.dismiss();

                if (error != 0 || result == null) {
                    Toast.makeText(PoiDetailActivity.this, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
                    return;
                }

                MKRoutePlan plan = result.getPlan(0);
                MKRoute route = plan.getRoute(0);

                routeOverlay = new RouteOverlay(PoiDetailActivity.this, poiMapView);
                routeOverlay.setData(route);
                poiMapView.getOverlays().add(routeOverlay);

                poiMapView.invalidate();

            }

            @Override
            public void onGetAddrResult(MKAddrInfo mkAddrInfo, int i) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void onGetBusDetailResult(MKBusLineResult mkBusLineResult, int i) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void onGetSuggestionResult(MKSuggestionResult mkSuggestionResult, int i) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void onGetRGCShareUrlResult(String s, int i) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void onGetPoiDetailSearchResult(int i, int i2) {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        });


        GeoPoint startPoint = new GeoPoint((int) (myLocation.getLatitude() * 1E6), (int) (myLocation.getLongitude() * 1E6));
        GeoPoint endPoint = new GeoPoint((int) (poi.getLatitude() * 1E6), (int) (poi.getLongitude() * 1E6));

        MKPlanNode startNode = new MKPlanNode();
        startNode.pt = startPoint;
        MKPlanNode endNode = new MKPlanNode();
        endNode.pt = endPoint;

        search.walkingSearch(myLocation.getCity(), startNode, myLocation.getCity(), endNode);
    }

    private void showBusRouteOverlay() {


        progressDialog.setMessage("获取公交路线中...");
        progressDialog.show();

        poiMapView.getOverlays().clear();
        poiMapView.getOverlays().add(pinOverlay);
        poiMapView.invalidate();

        MKSearch search = new MKSearch();
        search.init(mapManager, new MKSearchListener() {
            @Override
            public void onGetPoiResult(MKPoiResult mkPoiResult, int i, int i2) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void onGetTransitRouteResult(MKTransitRouteResult result, int error) {
                progressDialog.dismiss();

                if (error != 0 || result == null) {
                    Toast.makeText(PoiDetailActivity.this, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
                    return;
                }

                MKTransitRoutePlan plan = result.getPlan(0);
                MKRoute route = plan.getRoute(0);

                routeOverlay = new RouteOverlay(PoiDetailActivity.this, poiMapView);
                routeOverlay.setData(route);
                poiMapView.getOverlays().add(routeOverlay);

                poiMapView.invalidate();
            }

            @Override
            public void onGetDrivingRouteResult(MKDrivingRouteResult result, int error) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void onGetWalkingRouteResult(MKWalkingRouteResult result, int error) {


            }

            @Override
            public void onGetAddrResult(MKAddrInfo mkAddrInfo, int i) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void onGetBusDetailResult(MKBusLineResult mkBusLineResult, int i) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void onGetSuggestionResult(MKSuggestionResult mkSuggestionResult, int i) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void onGetRGCShareUrlResult(String s, int i) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void onGetPoiDetailSearchResult(int i, int i2) {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        });


        GeoPoint startPoint = new GeoPoint((int) (myLocation.getLatitude() * 1E6), (int) (myLocation.getLongitude() * 1E6));
        GeoPoint endPoint = new GeoPoint((int) (poi.getLatitude() * 1E6), (int) (poi.getLongitude() * 1E6));

        MKPlanNode startNode = new MKPlanNode();
        startNode.pt = startPoint;
        MKPlanNode endNode = new MKPlanNode();
        endNode.pt = endPoint;

        search.transitSearch(myLocation.getCity(), startNode, endNode);
    }

    private void showCarRouteOverlay() {

        progressDialog.setMessage("获取驾车路线中...");
        progressDialog.show();

        poiMapView.getOverlays().clear();
        poiMapView.getOverlays().add(pinOverlay);
        poiMapView.invalidate();

        MKSearch search = new MKSearch();
        search.init(mapManager, new MKSearchListener() {
            @Override
            public void onGetPoiResult(MKPoiResult mkPoiResult, int i, int i2) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void onGetTransitRouteResult(MKTransitRouteResult result, int error) {

            }

            @Override
            public void onGetDrivingRouteResult(MKDrivingRouteResult result, int error) {
                progressDialog.dismiss();

                if (error != 0 || result == null) {
                    Toast.makeText(PoiDetailActivity.this, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
                    return;
                }

                MKRoutePlan plan = result.getPlan(0);
                MKRoute route = plan.getRoute(0);

                routeOverlay = new RouteOverlay(PoiDetailActivity.this, poiMapView);
                routeOverlay.setData(route);
                poiMapView.getOverlays().add(routeOverlay);

                poiMapView.invalidate();

            }

            @Override
            public void onGetWalkingRouteResult(MKWalkingRouteResult result, int error) {


            }

            @Override
            public void onGetAddrResult(MKAddrInfo mkAddrInfo, int i) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void onGetBusDetailResult(MKBusLineResult mkBusLineResult, int i) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void onGetSuggestionResult(MKSuggestionResult mkSuggestionResult, int i) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void onGetRGCShareUrlResult(String s, int i) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void onGetPoiDetailSearchResult(int i, int i2) {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        });


        GeoPoint startPoint = new GeoPoint((int) (myLocation.getLatitude() * 1E6), (int) (myLocation.getLongitude() * 1E6));
        GeoPoint endPoint = new GeoPoint((int) (poi.getLatitude() * 1E6), (int) (poi.getLongitude() * 1E6));

        MKPlanNode startNode = new MKPlanNode();
        startNode.pt = startPoint;
        MKPlanNode endNode = new MKPlanNode();
        endNode.pt = endPoint;

        search.drivingSearch(myLocation.getCity(), startNode, myLocation.getCity(), endNode);
    }
}