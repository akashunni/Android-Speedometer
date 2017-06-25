package com.akashunni.speedometer.Fonts;

import android.content.Context;
import android.graphics.Typeface;

/**
 * Created by Akash on 7/19/2016.
 */
public class FontFactory {
    private static FontFactory instance = new FontFactory();
    Typeface typeface_units = null, typeface_values = null;

    public static FontFactory getInstance() {
        return instance;
    }

    public Typeface getFontForUnits(Context context) {
        if (typeface_units == null) {
            typeface_units = Typeface.createFromAsset(context.getResources().getAssets(), "fonts/DSEG14ClassicMini-LightItalic.ttf");
        }
        return typeface_units;
    }

    public Typeface getFontForValues(Context context) {
        if (typeface_values == null) {
            typeface_values = Typeface.createFromAsset(context.getResources().getAssets(), "fonts/DSEG14ClassicMini-BoldItalic.ttf");
        }
        return typeface_values;
    }
}