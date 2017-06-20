package com.quintlr.speedometer.Utilities;

import android.content.Context;
import android.location.Location;
import android.preference.PreferenceManager;

/**
 * Created by akash on 21/6/17.
 */

public class Conversions {
    public static String fromDecimalToDMS(Context context, double location){
        String DMS = Location.convert(location, Location.FORMAT_SECONDS);
        DMS = DMS.replaceFirst(":", String.valueOf((char)176)+" ");
        DMS = DMS.replaceFirst(":", "' ");
        int lastIndex = DMS.indexOf('.') + Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context).getString("precision", "4")) + 1;
        if (lastIndex < DMS.length()){
            DMS = DMS.substring(0, lastIndex);
        }
        return DMS + "\"";
    }

    public static String decimalPrecision(Context context, double value){
        String newLocation = String.valueOf(value);
        int lastIndex = newLocation.indexOf('.') + Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context).getString("precision", "4")) + 1;
        if (lastIndex < newLocation.length()){
            newLocation = newLocation.substring(0, lastIndex);
        }
        return newLocation;
    }
}
