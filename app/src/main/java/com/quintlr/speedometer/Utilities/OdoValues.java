package com.quintlr.speedometer.Utilities;

import android.location.Location;

/**
 * Created by akash on 25/5/17.
 */

public class OdoValues {

    public static float getDisplayDistance(float distance, int units) {
        switch (units) {
            // km
            case 0:
                return distance / 1000;
            // miles
            case 1:
                return (float) (distance / 1609.344);
            // meter
            case 2:
                return distance;
            // error
            default:
                return -1;
        }
    }

    public static float getDistance(Location prevLocation, Location currentLocation){
        if (prevLocation != null && currentLocation != null){
            return currentLocation.distanceTo(prevLocation);
        }else {
            return 0;
        }
    }

}
