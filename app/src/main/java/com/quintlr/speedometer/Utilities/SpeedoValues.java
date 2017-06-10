package com.quintlr.speedometer.Utilities;

import android.location.Location;

/**
 * Created by akash on 25/5/17.
 */

public class SpeedoValues {

    public static float getDisplaySpeed(Location currentLocation, int units) {
        if (currentLocation.hasSpeed()) {
            switch (units) {
                // km/hr
                case 0:
                    return currentLocation.getSpeed() * (18 / 5);
                // miles/hr
                case 1:
                    return (float) (currentLocation.getSpeed() * 2.23694);
                // meter/sec
                case 2:
                    return currentLocation.getSpeed();
                // error
                default:
                    return -1;
            }
        } else {
            return -1;
        }
    }

}
