package com.quintlr.speedometer.Activities;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.AppCompatImageView;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.quintlr.speedometer.Fonts.UnitsTextView;
import com.quintlr.speedometer.Fonts.ValuesTextView;
import com.quintlr.speedometer.Preferences.MapStylePreferenceDialog;
import com.quintlr.speedometer.R;
import com.quintlr.speedometer.Preferences.SharedPrefs;

import java.io.IOException;
import java.util.List;

public class MainActivity extends FragmentActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        OnMapReadyCallback,
        View.OnClickListener,
        MapStylePreferenceDialog.MapStyleClickListener{

    private ValuesTextView speedo, odo;
    private UnitsTextView speedoUnits, odoUnits;
    private TextView latitude, longitude, altitude, direction;
    private GoogleApiClient googleApiClient;
    private GoogleMap googleMap;
    private AppCompatImageView btn_currLoc, btn_search, btn_mapType, btn_mapStyle, btn_settings;
    Location lastLocation;
    private boolean showLastLocation = true;
    String TAG  = "test";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        speedo = (ValuesTextView) findViewById(R.id.speedo);
        odo = (ValuesTextView) findViewById(R.id.odo);
        speedoUnits = (UnitsTextView) findViewById(R.id.speedounits);
        odoUnits = (UnitsTextView) findViewById(R.id.odounits);
        latitude = (TextView) findViewById(R.id.latitude);
        longitude = (TextView) findViewById(R.id.longitude);
        altitude = (TextView) findViewById(R.id.altitude);
        direction = (TextView) findViewById(R.id.direction);
        btn_currLoc = (AppCompatImageView) findViewById(R.id.currLoc);
        btn_search = (AppCompatImageView) findViewById(R.id.search);
        btn_mapType = (AppCompatImageView) findViewById(R.id.mapType);
        btn_mapStyle = (AppCompatImageView) findViewById(R.id.mapStyle);
        btn_settings = (AppCompatImageView) findViewById(R.id.settings);

        //Setting onClickListeners to buttons
        btn_currLoc.setOnClickListener(this);
        btn_search.setOnClickListener(this);
        btn_mapType.setOnClickListener(this);
        btn_mapStyle.setOnClickListener(this);
        btn_settings.setOnClickListener(this);

        //attach fragment map with this activity.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_map);
        mapFragment.getMapAsync(this);

        //create googleAPIClient for last location.
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        //show ad in adView
        MobileAds.initialize(getApplicationContext(), getResources().getString(R.string.banner_ad_unit_id));
        AdView adView = (AdView) findViewById(R.id.adView);
        adView.loadAd(new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice("54CEFC489DFC20BF0748DB522ED99F07") //OP3T
                .build());
    }

    // onClick listener for buttons
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.currLoc:

                break;
            case R.id.search:

                break;
            case R.id.mapType:
                if (googleMap.getMapType() == GoogleMap.MAP_TYPE_NORMAL) {
                    DrawableCompat.setTint(btn_mapType.getDrawable(), ContextCompat.getColor(getApplicationContext(), R.color.green));
                    googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                } else {
                    DrawableCompat.setTint(btn_mapType.getDrawable(), ContextCompat.getColor(getApplicationContext(), R.color.pureWhite));
                    googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                }
                break;
            case R.id.mapStyle:
                FragmentManager fragmentManager = getSupportFragmentManager();
                MapStylePreferenceDialog mapStylePreferenceDialog = new MapStylePreferenceDialog();
                mapStylePreferenceDialog.show(fragmentManager, "mapStyle");
                mapStylePreferenceDialog.setOnClickListener(this);
                break;
            case R.id.settings:

                break;
        }
    }

    void setMapStyle(){
        //Setting the map style
        switch (PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getInt("mapStyle", 0)){
            //Standard
            case 0:
                googleMap.setMapStyle(MapStyleOptions
                        .loadRawResourceStyle(getApplicationContext(),R.raw.map_style_standard));
                break;
            //Silver
            case 1:
                googleMap.setMapStyle(MapStyleOptions
                        .loadRawResourceStyle(getApplicationContext(),R.raw.map_style_silver));
                break;
            //Retro
            case 2:
                googleMap.setMapStyle(MapStyleOptions
                        .loadRawResourceStyle(getApplicationContext(),R.raw.map_style_retro));
                break;
            //Dark
            case 3:
                googleMap.setMapStyle(MapStyleOptions
                        .loadRawResourceStyle(getApplicationContext(),R.raw.map_style_dark));
                break;
            //Night
            case 4:
                googleMap.setMapStyle(MapStyleOptions
                        .loadRawResourceStyle(getApplicationContext(),R.raw.map_style_night));
                break;
            //Aubergune
            case 5:
                googleMap.setMapStyle(MapStyleOptions
                        .loadRawResourceStyle(getApplicationContext(),R.raw.map_style_aubergine));
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        googleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        googleApiClient.disconnect();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    //Google API's part
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        // getting & setting the lastLocation.
        Log.d(TAG, "LocationServices API connected...");
        if (showLastLocation){
            showLastLocation = false;
            lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            if (lastLocation != null){
                Log.d(TAG, "Got the Last Location...");
                latitude.setText(String.valueOf(lastLocation.getLatitude()));
                longitude.setText(String.valueOf(lastLocation.getLongitude()));
                CameraUpdate cameraUpdate = CameraUpdateFactory
                        .newLatLngZoom(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()), 18);
                googleMap.animateCamera(cameraUpdate);
            }else {
                Log.d(TAG, "Last Location is NULL");
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
    // till here

    //Map Ready
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "Map is ready...");
        this.googleMap = googleMap;
        setMapStyle();
    }

    @Override
    public void onMapStyleClickListener() {
        setMapStyle();
    }
}