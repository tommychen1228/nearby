package com.cdm.nearby.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.MapActivity;

/**
 * Created with IntelliJ IDEA.
 * User: cdm
 * Date: 2/15/13
 * Time: 2:53 PM
 */
public class BaseBaiduMapActivity extends MapActivity {

    protected BMapManager mapManager;
    protected LayoutInflater layoutInflater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        layoutInflater = LayoutInflater.from(this);

        mapManager = ((MyApplication)getApplication()).getMapManager();

    }

    @Override
    protected void onPause() {
        mapManager.stop();
        super.onPause();
    }

    @Override
    protected void onResume() {
        mapManager.start();
        super.onResume();
    }

    @Override
    protected boolean isRouteDisplayed() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    protected void initMapActivity(){
        super.initMapActivity(mapManager);
        mapManager.start();
    }


}
