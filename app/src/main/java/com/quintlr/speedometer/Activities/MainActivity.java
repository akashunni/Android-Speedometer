package com.quintlr.speedometer.Activities;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatImageView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
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

import java.util.ArrayList;

public class MainActivity extends FragmentActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        OnMapReadyCallback,
        View.OnClickListener,
        MapStylePreferenceDialog.MapStyleClickListener,
        SpeedoUnitsPreferenceDialog.SpeedoUnitClickListener,
        OdoUnitsPreferenceDialog.OdoUnitClickListener,
        android.location.LocationListener,
        SensorEventListener{

    private ValuesTextView speedo, odo;
    private UnitsTextView speedoUnits, odoUnits;
    private TextView latitude, longitude, altitude, direction;
    private GoogleApiClient googleApiClient;
    private GoogleMap googleMap;
    private AppCompatImageView btn_currLoc, btn_search, btn_mapType, btn_mapStyle, btn_settings;
    private FragmentManager fragmentManager = getSupportFragmentManager();
    public static final int LOCATION_PERMISSION_ID = 9999;
    public static final int SMS_PERMISSION_ID = 8888, REQUEST_CHECK_SETTINGS = 777;
    Location lastLocation;
    private boolean showLastLocation = true, currentLocationPressed = false, got_location = false;
    private LocationManager locationManager;
    double distance = 0, lat_value=0, long_value=0;
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
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

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
                currentLocationPressed = !currentLocationPressed;
                Log.d(TAG, "Current Location Pressed = "+currentLocationPressed);
                trackCurrentLocation(true);
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
        isGPSEnabled();
    }

    @Override
    protected void onStop() {
        super.onStop();
        googleApiClient.disconnect();
        locationManager.removeUpdates(this);
    }

    boolean isGPSEnabled(){
        if (((LocationManager) getSystemService(Context.LOCATION_SERVICE)).isProviderEnabled(LocationManager.GPS_PROVIDER)){
            GPSisOn();
            return true;
        }
        return false;
    }

    void GPSisOn(){
        if (checkLocationPermission()){
            Log.d(TAG, "GPSisOn: requestLocationUpdates");
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        }
    }

    public void enableGPS() {
        Log.d(TAG, "enableGPS: ");
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(30 * 1000);
        locationRequest.setFastestInterval(5 * 1000);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
                .setAlwaysShow(true);

        PendingResult<LocationSettingsResult> pendingResult =
                LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());

        pendingResult.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                switch (result.getStatus().getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        Log.d(TAG, "onResult: SUCCESS");
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        Log.d(TAG, "onResult: RESOLUTION REQUIRED");
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            result.getStatus().startResolutionForResult(
                                    MainActivity.this,
                                    REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            // enable GPS result
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Log.d(TAG, "GPS Turned ON...");
                        trackCurrentLocation(true);
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.d(TAG, "GPS Request cancelled...");
                        break;
                    default:
                        break;
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    void trackCurrentLocation(boolean zoom){
        Log.d(TAG, "trackCurrentLocation: ");
        if (checkLocationPermission()) {
            googleMap.setMyLocationEnabled(true);
            if (currentLocationPressed) {
                if (isGPSEnabled()) {
                    DrawableCompat.setTint(btn_currLoc.getDrawable(), ContextCompat.getColor(getApplicationContext(), R.color.green));
                    LatLng latLng = null;
                    if (got_location) {
                        latLng = new LatLng(lat_value, long_value);
                    } else {
                        Toast.makeText(getApplicationContext(), "No Satellite signals, try outdoors", Toast.LENGTH_SHORT).show();
                    }
                    if (latLng != null) {
                        CameraUpdate cameraUpdate;
                        if (zoom)
                            cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 18);
                        else
                            cameraUpdate = CameraUpdateFactory.newLatLng(latLng);
                        googleMap.animateCamera(cameraUpdate);
                    }
                } else {
                    enableGPS();
                }
            } else {
                DrawableCompat.setTint(btn_currLoc.getDrawable(), ContextCompat.getColor(getApplicationContext(), R.color.pureWhite));
                Log.d(TAG, "removing updates for location manager...");
                locationManager.removeUpdates(this);
            }
        }else {
            requestLocationPermission();
        }
    }

    // Permissions
    boolean checkSMSPermission(){
        return ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED;
    }

    void requestSMSPermission(){
        if (!checkSMSPermission()){
            /* The permission is NOT already granted.
             Check if the user has been asked about this permission already and denied
             it. If so, we want to give more explanation about why the permission is needed.*/
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.SEND_SMS)) {
                    /* Show our own UI next time when the user denied for the permission*/
                    // calls when dialog shows after the first denial.
                    //Log.d("akash", "checkAndGetPermissions: IF->RATIONALE");
                }
                /* Fire off an async request to actually get the permission
                 This will show the standard permission request dialog UI*/
                requestPermissions(new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_ID);
            }
        }
    }

    void gotLocationPermission(){
        if (currentLocationPressed){
            Log.d(TAG, "CALLING trackCurrentLocation from gotLocationPermission");
            trackCurrentLocation(true);
        }
    }

    boolean checkLocationPermission(){
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    void requestLocationPermission(){
        if (!checkLocationPermission()) {
            /* The permission is NOT already granted.
             Check if the user has been asked about this permission already and denied
             it. If so, we want to give more explanation about why the permission is needed.*/
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    /* Show our own UI next time when the user denied for the permission*/
                    // calls when dialog shows after the first denial.
                    //Log.d("akash", "checkAndGetPermissions: IF->RATIONALE");
                }
                /* Fire off an async request to actually get the permission
                 This will show the standard permission request dialog UI*/
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_ID);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case LOCATION_PERMISSION_ID:
                if (grantResults.length==1 && grantResults[0] == PackageManager.PERMISSION_DENIED){
                    // if permission is not granted.
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)){
                            // onDenyClick

                        }else {
                            // don't show again checked and deny clicked.
                            // called always on start if that option was checked.
                            permissionUI();
                        }
                    }
                }else {
                    // allow clicked.
                    // called always on start if that option was clicked.
                    // write the funcs here from where permission was requested.
                    gotLocationPermission();

                }
                break;

            case SMS_PERMISSION_ID:
                break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public void permissionUI(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Permission to access the songs from your device was denied. You cannot see the content or play any music unless the permission is GRANTED. Would you like to do it now?")
                .setCancelable(false)
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();

                        Toast.makeText(MainActivity.this, "Click on PERMISSIONS & grant access to Storage & RESTART THE APP.", Toast.LENGTH_LONG).show();
                        //open app settings.
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);

                    }
                })
                .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                        ConstraintLayout constraintLayout = (ConstraintLayout) findViewById(R.id.constaintLayoutMain);
                        Snackbar.make(constraintLayout, "Cannot retrieve songs.", Snackbar.LENGTH_LONG)
                                .setAction("RETRY", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        // on clicking Retry
                                        permissionUI();
                                    }
                                })
                                .show();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    //Google API's part
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        // getting & setting the lastLocation.
        Log.d(TAG, "LocationServices API connected...");
        if (checkLocationPermission()){
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
        googleMap.setTrafficEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(false);
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


    // Sensor changed listener
    @Override
    public void onSensorChanged(SensorEvent event) {

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    // Location listener
    @Override
    public void onLocationChanged(Location location) {
        lat_value = location.getLatitude();
        long_value = location.getLongitude();
        latitude.setText(String.valueOf(lat_value));
        longitude.setText(String.valueOf(long_value));
        Log.d(TAG, "onLocationChanged: "+lat_value+" "+long_value);
        altitude.setText(String.valueOf(location.getAltitude()));
        got_location = true;
        if (currentLocationPressed){
            trackCurrentLocation(false);
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}