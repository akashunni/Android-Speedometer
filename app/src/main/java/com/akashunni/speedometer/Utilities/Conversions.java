package com.akashunni.speedometer.Utilities;

import android.content.Context;
import android.location.Location;
import android.preference.PreferenceManager;

import com.akashunni.speedometer.Activities.MainActivity;
import com.akashunni.speedometer.R;

/**
 * Created by akash on 21/6/17.
 */

public class Conversions {
    public static String fromDecimalToDMS(Context context, double location){
        if (location == 0 && !MainActivity.gotLocation())
            return context.getResources().getString(R.string.not_available);
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
        if (value == 0 && !MainActivity.gotLocation())
            return context.getResources().getString(R.string.not_available);
        String newValue = String.valueOf(value);
        int lastIndex = newValue.indexOf('.') + Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context).getString("precision", "4")) + 1;
        if (lastIndex < newValue.length()){
            newValue = newValue.substring(0, lastIndex);
        }
        return newValue;
    }

    // takes degrees as input and returns the respective direction.
    public static String degreesToDirection(Context context, double degrees){
        if (degrees >= 348.75 && degrees <= 360 || degrees >= 0 && degrees < 11.25){
            return "N";
        }else if (degrees >= 11.25 && degrees < 33.75){
            return "NNE";
        }else if (degrees >= 33.75 && degrees < 56.25){
            return "NE";
        }else if (degrees >= 56.25 && degrees < 78.75){
            return "ENE";
        }else if (degrees >= 78.75 && degrees < 101.25){
            return "E";
        }else if (degrees >= 101.25 && degrees < 123.75){
            return "ESE";
        }else if (degrees >= 123.75 && degrees < 146.25){
            return "SE";
        }else if (degrees >= 146.25 && degrees < 168.75){
            return "SSE";
        }else if (degrees >= 168.75 && degrees < 191.25){
            return "S";
        }else if (degrees >= 191.25 && degrees < 213.75){
            return "SSW";
        }else if (degrees >= 213.75 && degrees < 236.25){
            return "SW";
        }else if (degrees >= 236.25 && degrees < 258.75){
            return "WSW";
        }else if (degrees >= 258.75 && degrees < 281.25){
            return "W";
        }else if (degrees >= 281.25 && degrees < 303.75){
            return "WNW";
        }else if (degrees >= 303.75 && degrees < 326.25){
            return "NW";
        }else if (degrees >= 326.25 && degrees < 348.75){
            return "NNW";
        }
        return context.getResources().getString(R.string.not_available);
    }
}
