package com.akashunni.speedometer.Utilities;

import android.content.Context;
import android.location.Location;
import android.preference.PreferenceManager;

import com.akashunni.speedometer.R;

/**
 * Created by akash on 25/5/17.
 */

public class OdoValues {

    public static String getDisplayDistance(Context context, float distance) {
        switch (PreferenceManager.getDefaultSharedPreferences(context).getInt("odoUnits", 0)) {
            // km
            case 0:
                return String.format("%4.1f", distance / 1000);
            // miles
            case 1:
                return String.format("%4.1f", (distance / 1609.344));
            // meter
            case 2:
                return String.format("%3.2f", distance);
            // error
            default:
                return context.getResources().getString(R.string.hyphen_5);
        }
    }

    public static float getDistance(Location prevLocation, Location currentLocation) {
        if (prevLocation != null && currentLocation != null) {
            return currentLocation.distanceTo(prevLocation);
        } else {
            return 0;
        }
    }

}
