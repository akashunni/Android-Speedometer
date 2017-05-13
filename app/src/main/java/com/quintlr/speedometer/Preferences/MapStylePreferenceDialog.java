package com.quintlr.speedometer.Preferences;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.quintlr.speedometer.Activities.MainActivity;
import com.quintlr.speedometer.R;

/**
 * Created by akash on 4/3/17.
 */

public class MapStylePreferenceDialog extends DialogFragment{
    private String[] mapStyleNames;
    private int selectedValue;
    private SharedPreferences mapStylePreference;
    private MapStyleClickListener mapStyleClickListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mapStylePreference = PreferenceManager.getDefaultSharedPreferences(getContext());
        mapStyleNames = getResources().getStringArray(R.array.map_style_names);
        selectedValue = mapStylePreference.getInt("mapStyle", 0);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
        dialog.setTitle("Select Map Style");
        dialog.setPositiveButton(null, null);
        dialog.setSingleChoiceItems(mapStyleNames, selectedValue, itemClickListener);
        return dialog.create();
    }

    public void setOnClickListener(MapStyleClickListener mapStyleClickListener){
        this.mapStyleClickListener = mapStyleClickListener;
    }

    DialogInterface.OnClickListener itemClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (selectedValue != which){
                selectedValue = which;
                mapStylePreference.edit().putInt("mapStyle", selectedValue).apply();
                mapStyleClickListener.onMapStyleClickListener();
            }
            dialog.dismiss();
        }
    };

    public interface MapStyleClickListener{
        void onMapStyleClickListener();
    }
}