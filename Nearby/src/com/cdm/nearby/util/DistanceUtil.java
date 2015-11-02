package com.cdm.nearby.util;

import com.cdm.nearby.common.L;

import java.text.DecimalFormat;

/**
 * Created with IntelliJ IDEA.
 * User: cdm
 * Date: 2/17/13
 * Time: 3:46 PM
 */
public class DistanceUtil {
    public static final int TYPE_WALK = 1;
    public static final int TYPE_BUS = 2;
    public static final int TYPE_CAR = 3;

    private static final int SPEED_WALK = 50;
    private static final int SPEED_BUS = 300;
    private static final int SPEED_CAR = 500;

    public static String convert(int distance) {


        try {
            float d = 0;
            d = (float) distance;

            if (d < 1000) {
                return d + "m";
            } else {
                return (float) (d / 1000) + "km";
            }

        } catch (NumberFormatException e) {
            L.e(e.getMessage(), e);
            return "未知距离";
        }
    }

    public static String calculateTime(int type, int distance) {
       int timeMinutes = 0;
       switch (type){
           case TYPE_WALK:
               timeMinutes = distance/SPEED_WALK;
               break;
           case TYPE_BUS:
               timeMinutes = distance/SPEED_BUS;
               break;
           case TYPE_CAR:
               timeMinutes = distance/SPEED_CAR;
               break;
       }

        if(timeMinutes < 60){
            if(timeMinutes == 0){
                timeMinutes = 1;
            }
            return timeMinutes + "分钟";
        } else {
            float timeHours = ((float)timeMinutes)/60;
            DecimalFormat decimalFormat = new DecimalFormat(".00");
            return decimalFormat.format(timeHours) + "小时";
        }


    }
}
