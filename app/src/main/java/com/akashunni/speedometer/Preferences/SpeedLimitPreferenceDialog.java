package com.akashunni.speedometer.Preferences;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.renderscript.ScriptGroup;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.akashunni.speedometer.Activities.MainActivity;
import com.akashunni.speedometer.R;

/**
 * Created by akash on 5/7/17.
 */

public class SpeedLimitPreferenceDialog extends DialogFragment {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
        final EditText edittext = new EditText(getContext());
        edittext.setInputType(InputType.TYPE_CLASS_NUMBER);
        edittext.setPadding(50,0,30,50);
        edittext.setText(String.valueOf(PreferenceManager.getDefaultSharedPreferences(getContext()).getFloat(getResources().getString(R.string.speedlimitvalue), 40)));
        dialog.setTitle("Speed Limit");
        dialog.setMessage("Set the speed in "+ MainActivity.speedUnits.toUpperCase());
        dialog.setView(edittext);
        dialog.setPositiveButton("SET", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                PreferenceManager.getDefaultSharedPreferences(getContext())
                        .edit()
                        .putFloat(getResources().getString(R.string.speedlimitvalue), Float.parseFloat(edittext.getText().toString()))
                        .apply();
            }
        });
        dialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        return dialog.create();
    }

}