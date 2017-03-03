package com.quintlr.speedometer;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Akash on 22-Aug-16.
 */
public class SharedPrefs {
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    private String PREFS_NAME;
    SharedPrefs(Context c, String PREFS_NAME){
        sharedPreferences = c.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.PREFS_NAME = PREFS_NAME;
    }
    void changePrefs(boolean value){
        editor = sharedPreferences.edit();
        editor.putBoolean(PREFS_NAME,value);
        editor.apply();
    }
    void changePrefs(double value){
        editor = sharedPreferences.edit();
        editor.putString(PREFS_NAME,Double.toString(value));
        editor.apply();
    }

    boolean getBoolValue(){
        return sharedPreferences.getBoolean(PREFS_NAME,false);
    }

    Double getDoubleValue(){
        if(sharedPreferences.getString(PREFS_NAME,"").equals(""))
            return 0.0;
        else
            return Double.valueOf(sharedPreferences.getString(PREFS_NAME,""));
    }

}
