package com.cdm.nearby.modal;

import android.content.Context;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

/**
 * Created with IntelliJ IDEA.
 * User: cdm
 * Date: 2/14/13
 * Time: 11:44 PM
 */
public class LocationManager extends BaseManger {
    private static Location lastLocation;
    private LocationClient locationClient;
    private LocationManagetListener locationManagetListener;

    public LocationManager(Context context) {
        super(context);

        LocationClientOption option = new LocationClientOption();
        option.setCoorType("bd09ll");
        option.setAddrType("all");
        option.setOpenGps(true);

        locationClient = new LocationClient(context);
        locationClient.setLocOption(option);

        locationClient.registerLocationListener(new BDLocationListener() {
            @Override
            public void onReceiveLocation(BDLocation bdLocation) {
                lastLocation = new Location();
                lastLocation.setAddress(bdLocation.getAddrStr());
                lastLocation.setLatitude(bdLocation.getLatitude());
                lastLocation.setLongitude(bdLocation.getLongitude());
                lastLocation.setCity(bdLocation.getCity());

                if(locationManagetListener != null){
                    locationManagetListener.onReceiveLocation(lastLocation);
                }
            }

            @Override
            public void onReceivePoi(BDLocation bdLocation) {

            }
        });


    }

    public Location getLastLocation() {
        return lastLocation;
    }

    public void requestLocation(LocationManagetListener listener) {
        locationManagetListener = listener;

        locationClient.start();
        locationClient.requestLocation();
    }

    public interface LocationManagetListener {

        public void onReceiveLocation(Location location);
    }
}
