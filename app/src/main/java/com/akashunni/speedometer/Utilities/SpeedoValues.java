package com.akashunni.speedometer.Utilities;

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
                    return currentLocation.getSpeed() * 3.6f;
                // miles/hr
                case 1:
                    return currentLocation.getSpeed() * 2.23694f;
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

    public static long calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);
        double c = 2 * Math.asin(Math.sqrt(a));
        long distanceInMeters = Math.round(6371000 * c);
        return distanceInMeters;
    }

}
