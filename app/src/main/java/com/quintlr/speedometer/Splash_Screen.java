package com.quintlr.speedometer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class Splash_Screen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPrefs sharedPrefs = new SharedPrefs(getApplicationContext(), "First_Time");
        if(!sharedPrefs.getBoolValue()){
            sharedPrefs.changePrefs(true);
            Intent intent = new Intent(this, Instructions.class);
            startActivity(intent);
            finish();
        }else{
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }

    }
}
