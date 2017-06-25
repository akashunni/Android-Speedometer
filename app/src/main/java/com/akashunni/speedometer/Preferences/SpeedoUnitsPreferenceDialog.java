package com.akashunni.speedometer.Preferences;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.akashunni.speedometer.R;

/**
 * Created by akash on 5/3/17.
 */

public class SpeedoUnitsPreferenceDialog extends DialogFragment {
    private String[] speedoUnits;
    private int selectedValue;
    private SharedPreferences speedoUnitsPreference;
    private SpeedoUnitClickListener speedoUnitClickListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        speedoUnitsPreference = PreferenceManager.getDefaultSharedPreferences(getContext());
        speedoUnits = getResources().getStringArray(R.array.speedo_units_list);
        selectedValue = speedoUnitsPreference.getInt("speedoUnits", 0);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
        dialog.setTitle("Select speedometer units");
        dialog.setPositiveButton(null, null);
        dialog.setSingleChoiceItems(speedoUnits, selectedValue, itemClickListener);
        return dialog.create();
    }

    public void setOnClickListener(SpeedoUnitClickListener speedoUnitClickListener) {
        this.speedoUnitClickListener = speedoUnitClickListener;
    }

    DialogInterface.OnClickListener itemClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (selectedValue != which) {
                selectedValue = which;
                speedoUnitsPreference.edit().putInt("speedoUnits", selectedValue).apply();
                speedoUnitClickListener.onSpeedoUnitClickListener();
            }
            dialog.dismiss();
        }
    };

    public interface SpeedoUnitClickListener {
        void onSpeedoUnitClickListener();
    }

}
