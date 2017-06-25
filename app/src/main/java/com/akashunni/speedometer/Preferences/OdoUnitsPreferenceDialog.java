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

public class OdoUnitsPreferenceDialog extends DialogFragment {
    private String[] odoUnits;
    private int selectedValue;
    private SharedPreferences odoUnitsPreference;
    private OdoUnitClickListener odoUnitClickListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        odoUnitsPreference = PreferenceManager.getDefaultSharedPreferences(getContext());
        odoUnits = getResources().getStringArray(R.array.odo_units_list);
        selectedValue = odoUnitsPreference.getInt("odoUnits", 0);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
        dialog.setTitle("Select odometer units");
        dialog.setPositiveButton(null, null);
        dialog.setSingleChoiceItems(odoUnits, selectedValue, itemClickListener);
        return dialog.create();
    }

    public void setOnClickListener(OdoUnitClickListener odoUnitClickListener) {
        this.odoUnitClickListener = odoUnitClickListener;
    }

    DialogInterface.OnClickListener itemClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (selectedValue != which) {
                selectedValue = which;
                odoUnitsPreference.edit().putInt("odoUnits", selectedValue).apply();
                odoUnitClickListener.onOdoUnitClickListener();
            }
            dialog.dismiss();
        }
    };

    public interface OdoUnitClickListener {
        void onOdoUnitClickListener();
    }

}
