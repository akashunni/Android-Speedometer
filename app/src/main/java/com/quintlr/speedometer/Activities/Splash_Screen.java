package com.quintlr.speedometer.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.quintlr.speedometer.Preferences.SharedPrefs;

public class Splash_Screen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (SharedPrefs.isFirstTime(getApplicationContext())){
            SharedPrefs.setFirstTime(getApplicationContext());
            Intent intent = new Intent(getApplicationContext(), Instructions.class);
            startActivity(intent);
            finish();
        }else {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }

    }
}
