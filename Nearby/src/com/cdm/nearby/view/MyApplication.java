package com.cdm.nearby.view;

import android.app.Application;
import com.baidu.mapapi.BMapManager;

/**
 * Created with IntelliJ IDEA.
 * User: cdm
 * Date: 2/13/13
 * Time: 9:41 PM
 */
public class MyApplication extends Application{
    public static final String MAP_KEY = "5211E36E958115A89A5E4342C25EA414737016D7";

    private BMapManager mapManager;

    @Override
    public void onCreate() {
        mapManager = new BMapManager(this);
        mapManager.init(MAP_KEY, null);

        super.onCreate();
    }

    public void onTerminate() {

        if (mapManager != null) {
            mapManager.destroy();
            mapManager = null;
        }
        super.onTerminate();
    }

    public BMapManager getMapManager() {
        return mapManager;
    }

}
