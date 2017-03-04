package com.quintlr.speedometer.Activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.AppCompatImageView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.quintlr.speedometer.Fonts.UnitsTextView;
import com.quintlr.speedometer.Fonts.ValuesTextView;
import com.quintlr.speedometer.Preferences.MapStylePreferenceDialog;
import com.quintlr.speedometer.Preferences.OdoUnitsPreferenceDialog;
import com.quintlr.speedometer.Preferences.SpeedoUnitsPreferenceDialog;
import com.quintlr.speedometer.R;

public class MainActivity extends FragmentActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        OnMapReadyCallback,
        View.OnClickListener,
        MapStylePreferenceDialog.MapStyleClickListener,
        SpeedoUnitsPreferenceDialog.SpeedoUnitClickListener,
        OdoUnitsPreferenceDialog.OdoUnitClickListener{

    private ValuesTextView speedo, odo;
    private UnitsTextView speedoUnits, odoUnits;
    private TextView latitude, longitude, altitude, direction;
    private GoogleApiClient googleApiClient;
    private GoogleMap googleMap;
    private AppCompatImageView btn_currLoc, btn_search, btn_mapType, btn_mapStyle, btn_settings;
    private FragmentManager fragmentManager = getSupportFragmentManager();
    Location lastLocation;
    private boolean showLastLocation = true;
    String TAG  = "test";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Initializing variables
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
        speedo.setOnClickListener(this);
        speedoUnits.setOnClickListener(this);
        odo.setOnClickListener(this);
        odoUnits.setOnClickListener(this);

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

        setSpeedoUnits();
        setOdoUnits();
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
                MapStylePreferenceDialog mapStylePreferenceDialog = new MapStylePreferenceDialog();
                mapStylePreferenceDialog.show(fragmentManager, "mapStyle");
                mapStylePreferenceDialog.setOnClickListener(this);
                break;
            case R.id.settings:

                break;
            case R.id.speedounits:
            case R.id.speedo:
                SpeedoUnitsPreferenceDialog speedoUnitsPreferenceDialog = new SpeedoUnitsPreferenceDialog();
                speedoUnitsPreferenceDialog.show(fragmentManager, "speedoUnits");
                speedoUnitsPreferenceDialog.setOnClickListener(this);
                break;
            case R.id.odounits:
            case R.id.odo:
                OdoUnitsPreferenceDialog odoUnitsPreferenceDialog = new OdoUnitsPreferenceDialog();
                odoUnitsPreferenceDialog.show(fragmentManager, "odoUnits");
                odoUnitsPreferenceDialog.setOnClickListener(this);
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

    void setSpeedoUnits(){
        switch (PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getInt("speedoUnits", 0)){
            case 0:
                speedoUnits.setText("km/hr");
                break;
            case 1:
                speedoUnits.setText("mi/hr");
                break;
            case 2:
                speedoUnits.setText("mt/sc");
                break;
        }
    }

    void setOdoUnits(){
        switch (PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getInt("odoUnits", 0)){
            case 0:
                odoUnits.setText("km");
                break;
            case 1:
                odoUnits.setText("mi");
                break;
            case 2:
                odoUnits.setText("mt");
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

    //MapStyle click listener
    @Override
    public void onMapStyleClickListener() {
        setMapStyle();
    }

    //Speedo unit click listener
    @Override
    public void onSpeedoUnitClickListener() {
        setSpeedoUnits();
    }

    //Odo unit click listener
    @Override
    public void onOdoUnitClickListener() {
        setOdoUnits();
    }
}