package com.akashunni.speedometer.Activities;

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
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatImageView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.akashunni.speedometer.Fonts.UnitsTextView;
import com.akashunni.speedometer.Fonts.ValuesTextView;
import com.akashunni.speedometer.Preferences.OdoUnitsPreferenceDialog;
import com.akashunni.speedometer.Preferences.SharedPrefs;
import com.akashunni.speedometer.Preferences.SpeedoUnitsPreferenceDialog;
import com.akashunni.speedometer.R;
import com.akashunni.speedometer.Utilities.ChangeColor;
import com.akashunni.speedometer.Utilities.Conversions;
import com.akashunni.speedometer.Utilities.OdoValues;
import com.akashunni.speedometer.Utilities.SpeedoValues;

public class MainActivity extends FragmentActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        OnMapReadyCallback,
        View.OnClickListener,
        View.OnLongClickListener,
        AppSettings.AppSettingsFragment.AppSettingsChangeListener,
        SpeedoUnitsPreferenceDialog.SpeedoUnitClickListener,
        OdoUnitsPreferenceDialog.OdoUnitClickListener,
        LocationListener,
        SensorEventListener,
        PlaceSelectionListener{

    private ValuesTextView speedo, odo, speedobg, odobg;
    private UnitsTextView speedoUnits, odoUnits, speedoUnitsbg, odoUnitsbg;
    private Button resetBtn;
    private View separator;
    private TextView latitude, longitude, altitude, direction, accuracy;
    private GoogleApiClient googleApiClient;
    private GoogleMap googleMap;
    private SupportMapFragment mapFragment;
    private PlaceAutocompleteFragment searchFragment;
    private AppCompatImageView btn_currLoc, btn_search, btn_mapType, btn_navigation, btn_settings;
    private FragmentManager fragmentManager = getSupportFragmentManager();
    static InterstitialAd interstitialAd;
    public static final int LOCATION_PERMISSION_ID = 9999;
    public static final int REQUEST_CHECK_SETTINGS = 777;
    public static final float SMALLEST_DISPLACEMENT = 0.5f;
    public static final long UPDATE_INTERVAL = 2*1000;
    private int speedRefresh = 0, distanceRefresh = 0;
    private Vibrator vibrator;
    private long vibratePattern []= {0, 600, 1000};
    private Location prevLocation = null;
    Location lastLocation;
    private static boolean showLastLocation = true, currentLocationPressed = false, got_location = false;
    private LocationManager locationManager;
    float speed = 0, distance = 0, alt_value = 0, acc_value = 0, degrees = 0;
    private String display_distance="";
    double lat_value = 0, long_value = 0;
    String TAG = "test";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setAppTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Initializing variables
        speedo = (ValuesTextView) findViewById(R.id.speedo);
        speedobg = (ValuesTextView) findViewById(R.id.speedobg);
        odo = (ValuesTextView) findViewById(R.id.odo);
        odobg = (ValuesTextView) findViewById(R.id.odobg);
        speedoUnits = (UnitsTextView) findViewById(R.id.speedounits);
        speedoUnitsbg = (UnitsTextView) findViewById(R.id.speedounitsbg);
        odoUnits = (UnitsTextView) findViewById(R.id.odounits);
        odoUnitsbg = (UnitsTextView) findViewById(R.id.odounitsbg);
        resetBtn = (Button) findViewById(R.id.reset);
        latitude = (TextView) findViewById(R.id.latitude);
        longitude = (TextView) findViewById(R.id.longitude);
        altitude = (TextView) findViewById(R.id.altitude);
        direction = (TextView) findViewById(R.id.direction);
        accuracy = (TextView) findViewById(R.id.accuracy);
        btn_currLoc = (AppCompatImageView) findViewById(R.id.currLoc);
        btn_search = (AppCompatImageView) findViewById(R.id.search);
        btn_mapType = (AppCompatImageView) findViewById(R.id.mapType);
        btn_navigation = (AppCompatImageView) findViewById(R.id.navigation);
        btn_settings = (AppCompatImageView) findViewById(R.id.settings);
        separator = findViewById(R.id.speedo_sep_horizontal);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        //Setting onClickListeners to buttons
        btn_currLoc.setOnClickListener(this);
        btn_search.setOnClickListener(this);
        btn_mapType.setOnClickListener(this);
        btn_navigation.setOnClickListener(this);
        btn_settings.setOnClickListener(this);
        speedo.setOnClickListener(this);
        speedoUnits.setOnClickListener(this);
        odo.setOnClickListener(this);
        odoUnits.setOnClickListener(this);
        resetBtn.setOnClickListener(this);
        resetBtn.setOnLongClickListener(this);

        // Setting color for buttons
        setButtonsColor();

        // Setting backlit color
        setBacklitColor();

        // Initialising vibrator
        vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);

        // setting on map changed listener
        AppSettings.AppSettingsFragment.setOnMapStyleChangeListener(this);

        //attach fragment map with this activity.
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_map);
        mapFragment.getMapAsync(this);

        //attach search fragment with this activity.
        searchFragment = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.search_fragment);
        android.app.FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.hide(searchFragment);
        fragmentTransaction.commit();
        searchFragment.setOnPlaceSelectedListener(this);

        //create googleAPIClient.
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .addApi(Places.GEO_DATA_API)
                    .build();
        }

        //show ad in adView
        MobileAds.initialize(getApplicationContext(), getResources().getString(R.string.banner_ad_unit_id));
        AdView adView = (AdView) findViewById(R.id.adView);
        adView.loadAd(new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice("E7A76C31163F0B32B56A88C78F40E833") //OP3T
                .build());

        loadInterstitialAd(getApplicationContext());

        //assigning distance value
        distance = SharedPrefs.getDistance(getApplicationContext());

        // setting speedo & odo units
        setSpeedoUnits();
        setOdoUnits();
    }

    // save values on rotation
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("showLastLocation", showLastLocation);
        outState.putBoolean("currentLocationPressed", currentLocationPressed);
        outState.putBoolean("got_location", got_location);
        outState.putFloat("speed", speed);
        outState.putFloat("distance", distance);
        outState.putString("display_distance", display_distance);
        outState.putDouble("lat_value", lat_value);
        outState.putDouble("long_value", long_value);
        outState.putFloat("alt_value", alt_value);
        outState.putFloat("acc_value", acc_value);
        outState.putFloat("degrees", degrees);
        Log.d(TAG, "onSaveInstanceState: "+lat_value+"::"+long_value);
        super.onSaveInstanceState(outState);
    }

    // restore values after rotation
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        showLastLocation = savedInstanceState.getBoolean("showLastLocation");
        currentLocationPressed = savedInstanceState.getBoolean("currentLocationPressed");
        got_location = savedInstanceState.getBoolean("got_location");
        speed = savedInstanceState.getFloat("speed");
        distance = savedInstanceState.getFloat("distance");
        display_distance = savedInstanceState.getString("display_distance");
        lat_value = savedInstanceState.getDouble("lat_value");
        long_value = savedInstanceState.getDouble("long_value");
        alt_value = savedInstanceState.getFloat("alt_value");
        acc_value = savedInstanceState.getFloat("acc_value");
        degrees = savedInstanceState.getFloat("degrees");
        if (currentLocationPressed){
            ChangeColor.ofButtonDrawableToActive(getApplicationContext(), btn_currLoc);
        }else {
            ChangeColor.ofButtonDrawableToNormal(getApplicationContext(), btn_currLoc);
        }
        if (got_location){
            setSpeedoValues();
            setOdoValues();
        }
        displayLatLngValues();
        if (alt_value != 0) {
            altitude.setText(String.valueOf(alt_value).concat(" mts."));
        }
        if (acc_value != 0) {
            accuracy.setText(String.valueOf(Character.toString((char) 177) + " " + acc_value + " mts."));
        }
        if (degrees != 0) {
            direction.setText(String.valueOf(Conversions.degreesToDirection(getApplicationContext(), degrees)+" ("+degrees+(char)176+")"));
        }
        Log.d(TAG, "onRestoreInstanceState: "+lat_value+"::"+long_value);
        super.onRestoreInstanceState(savedInstanceState);
    }

    public void permissionForSpeedUI() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("To calculate SPEED, you need to enable Location. Click YES to enable Location, NO to continue using the app without Location.")
                .setCancelable(false)
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        requestLocationPermission();
                    }
                })
                .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                        ConstraintLayout constraintLayout = (ConstraintLayout) findViewById(R.id.constaintLayoutMain);
                        Snackbar.make(constraintLayout, "Cannot retrieve speed.", Snackbar.LENGTH_LONG)
                                .setAction("RETRY", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        // on clicking Retry
                                        permissionForSpeedUI();
                                    }
                                })
                                .show();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    // onClick listener for buttons
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.currLoc:
                currentLocationPressed = !currentLocationPressed;
                Log.d(TAG, "Current Location Pressed = " + currentLocationPressed);
                trackCurrentLocation(true);
                break;
            case R.id.search:
                android.app.FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                if (searchFragment.isHidden()) {
                    fragmentTransaction.show(searchFragment);
                    ChangeColor.ofButtonDrawableToActive(getApplicationContext(), btn_search);
                } else {
                    fragmentTransaction.hide(searchFragment);
                    ChangeColor.ofButtonDrawableToNormal(getApplicationContext(), btn_search);
                }
                fragmentTransaction.commit();
                break;
            case R.id.mapType:
                if (googleMap.getMapType() == GoogleMap.MAP_TYPE_NORMAL) {
                    ChangeColor.ofButtonDrawableToActive(getApplicationContext(), btn_mapType);
                    googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                } else {
                    ChangeColor.ofButtonDrawableToNormal(getApplicationContext(), btn_mapType);
                    googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                }
                break;
            case R.id.navigation:
                Toast.makeText(this, "Coming Soon..!", Toast.LENGTH_SHORT).show();
                break;
            case R.id.settings:
                Intent i = new Intent(getApplicationContext(), AppSettings.class);
                startActivity(i);
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
            case R.id.reset:
                Toast.makeText(this, "Long Press the button to RESET.", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    public boolean onLongClick(View v) {
        switch (v.getId()) {
            case R.id.reset:
                distance = 0;
                SharedPrefs.setDistance(getApplicationContext(), 0);
                setOdoValues();
                return true;
            default:
                return false;
        }
    }

    //Interstitial ad.
    static void loadInterstitialAd(Context context){
        interstitialAd = new InterstitialAd(context);
        interstitialAd.setAdUnitId(context.getResources().getString(R.string.interstitial_ad_unit_id));
        interstitialAd.loadAd(new AdRequest.Builder()
                .addTestDevice("E7A76C31163F0B32B56A88C78F40E833")
                .build());
    }
    
    // setting the values

    void displayLatLngValues() {
        if (PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("DMS", true)){
            latitude.setText(Conversions.fromDecimalToDMS(getApplicationContext(), lat_value));
            longitude.setText(Conversions.fromDecimalToDMS(getApplicationContext(), long_value));
        }else {
            latitude.setText(Conversions.decimalPrecision(getApplicationContext(), lat_value));
            longitude.setText(Conversions.decimalPrecision(getApplicationContext(), long_value));
        }
    }

    void setAppTheme(){
        if (PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("theme", true)){
            setTheme(R.style.DarkTheme);
        }else {
            setTheme(R.style.LightTheme);
        }
    }

    void setBacklitColor(){
        // setting lcd backlit properties
        if (PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("lcdBacklit", true)){
            ChangeColor.ofTextView(speedobg, getResources().getColor(R.color.lcdbg));
            ChangeColor.ofTextView(speedoUnitsbg, getResources().getColor(R.color.lcdbg));
            ChangeColor.ofTextView(odobg, getResources().getColor(R.color.lcdbg));
            ChangeColor.ofTextView(odoUnitsbg, getResources().getColor(R.color.lcdbg));
        }else {
            ChangeColor.ofTextView(speedobg, getResources().getColor(R.color.transparent));
            ChangeColor.ofTextView(speedoUnitsbg, getResources().getColor(R.color.transparent));
            ChangeColor.ofTextView(odobg, getResources().getColor(R.color.transparent));
            ChangeColor.ofTextView(odoUnitsbg, getResources().getColor(R.color.transparent));
        }
    }

    private void setButtonsColor() {
        ChangeColor.ofButtonDrawableToNormal(getApplicationContext(), btn_currLoc);
        ChangeColor.ofButtonDrawableToNormal(getApplicationContext(), btn_search);
        ChangeColor.ofButtonDrawableToNormal(getApplicationContext(), btn_mapType);
        ChangeColor.ofButtonDrawableToNormal(getApplicationContext(), btn_navigation);
        ChangeColor.ofButtonDrawableToNormal(getApplicationContext(), btn_settings);
        ChangeColor.ofButtonDrawableToNormal(getApplicationContext(), resetBtn);
        ChangeColor.ofView(getApplicationContext(), separator);
    }

    void setMapStyle() {
        //Setting the map style
        switch (Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("mapStyle", "0"))) {
            //Standard
            case 0:
                googleMap.setMapStyle(MapStyleOptions
                        .loadRawResourceStyle(getApplicationContext(), R.raw.map_style_standard));
                break;
            //Silver
            case 1:
                googleMap.setMapStyle(MapStyleOptions
                        .loadRawResourceStyle(getApplicationContext(), R.raw.map_style_silver));
                break;
            //Retro
            case 2:
                googleMap.setMapStyle(MapStyleOptions
                        .loadRawResourceStyle(getApplicationContext(), R.raw.map_style_retro));
                break;
            //Dark
            case 3:
                googleMap.setMapStyle(MapStyleOptions
                        .loadRawResourceStyle(getApplicationContext(), R.raw.map_style_dark));
                break;
            //Night
            case 4:
                googleMap.setMapStyle(MapStyleOptions
                        .loadRawResourceStyle(getApplicationContext(), R.raw.map_style_night));
                break;
            //Aubergune
            case 5:
                googleMap.setMapStyle(MapStyleOptions
                        .loadRawResourceStyle(getApplicationContext(), R.raw.map_style_aubergine));
                break;
        }
    }

    void setSpeedoUnits() {
        switch (PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getInt("speedoUnits", 0)) {
            case 0:
                speedoUnits.setText(R.string.km_hr);
                break;
            case 1:
                speedoUnits.setText(R.string.mi_hr);
                break;
            case 2:
                speedoUnits.setText(R.string.mt_sec);
                break;
        }
        if (got_location){
            setSpeedoValues();
        }
    }

    void setSpeedoValues(){
        if (speed == -1){
            if (speedRefresh >= 3){
                speedo.setText(R.string.hyphen_4);
                speedRefresh = 0;
            }
        }else if (speed > 999.99){
            speedo.setText("high");
        }else {
            speedo.setText(String.format("%3.01f", speed));
            speedRefresh = 0;
        }

        if (speed >= Float.parseFloat(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("speedLimit", "0"))){
            if (vibrator.hasVibrator()){
                vibrator.vibrate(vibratePattern, 0);
            }
            ChangeColor.ofTextView(speedo, getResources().getColor(R.color.red));
        }else {
            if (vibrator.hasVibrator()){
                vibrator.cancel();
            }
            ChangeColor.ofTextViewToNormal(getApplicationContext(), speedo);
        }
    }

    void setOdoUnits() {
        switch (PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getInt("odoUnits", 0)) {
            case 0:
                odoUnits.setText(R.string.km);
                break;
            case 1:
                odoUnits.setText(R.string.mi);
                break;
            case 2:
                odoUnits.setText(R.string.mt);
                break;
        }
        setOdoValues();
    }

    void setOdoValues(){
        display_distance = OdoValues.getDisplayDistance(getApplicationContext(), distance);
        if (Float.parseFloat(display_distance) <= 999.99){
            Log.d(TAG, "setOdoValues: "+display_distance);
            odo.setText(display_distance);
        }else {
            distance = 0;
        }
    }

    // setting the map style when changed.
    @Override
    public void onMapStyleChanged() {
        setMapStyle();
    }

    @Override
    public void onAppThemeChanged() {
        setAppTheme();
        setButtonsColor();
        recreate();
    }

    @Override
    public void onBacklitChanged() {
        setBacklitColor();
    }

    @Override
    public void onLocationUnitsChanged() {
        displayLatLngValues();
    }

    @Override
    public void onPrecisionChanged() {
        displayLatLngValues();
    }

    @Override
    protected void onStart() {
        super.onStart();
        googleApiClient.connect();
        if (!checkLocationPermission()) {
            // didn't get the permissions
            permissionForSpeedUI();
        } else if (!isLocationEnabled()) {
            // we have the permissions and location is disabled.
            // requesting of location will be done from the isLocationEnabled() [above] method.
            Toast.makeText(this, "Enable Location to calculate SPEED!", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onResume() {
        mapFragment.onResume();
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        googleApiClient.disconnect();
        Log.d(TAG, "--- REMOVING LOCATION UPDATES ---");
        locationManager.removeUpdates(this);
        vibrator.cancel();
    }

    public static boolean gotLocation(){
        return got_location;
    }

    // check if location is enabled.
    boolean isLocationEnabled() {
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
            // location enabled
            requestLocationUpdates();
            return true;
        }
        // Location is disabled.
        Log.d(TAG, "isLocationEnabled: False");
        return false;
    }

    // Requesting location updates from respective providers.
    void requestLocationUpdates(){
        if (checkLocationPermission()){
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                Log.d(TAG, "+++ REQUESTING GPS LOCATION UPDATES +++");
                // High Accuracy
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        UPDATE_INTERVAL, SMALLEST_DISPLACEMENT, this);
            } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                Log.d(TAG, "+++ REQUESTING NETWORK LOCATION UPDATES +++");
                // Low Accuracy
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                        UPDATE_INTERVAL, SMALLEST_DISPLACEMENT, this);
            }
        }
    }

    // enabling the Location (dialog shown if not enabled initially)
    public void enableLocation() {
        Log.d(TAG, "enableLocation: ");
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("powerConsumption", "102")));
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(1000);
        locationRequest.setSmallestDisplacement(SMALLEST_DISPLACEMENT);

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
                        requestLocationUpdates();
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

    // result of enabling the location via dialog from enableLocation() method
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            // enable GPS result
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Log.d(TAG, "GPS Turned ON...");
                        requestLocationUpdates();
                        if (currentLocationPressed) {
                            trackCurrentLocation(true);
                        }
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

    // this method is executed when current location is pressed.
    void trackCurrentLocation(boolean zoom) {
        Log.d(TAG, "trackCurrentLocation: ");
        if (checkLocationPermission()) {
            googleMap.setMyLocationEnabled(true);
            if (currentLocationPressed) {
                if (isLocationEnabled()) {
                    ChangeColor.ofButtonDrawableToActive(getApplicationContext(), btn_currLoc);
                    LatLng latLng = null;
                    if (got_location) {
                        latLng = new LatLng(lat_value, long_value);
                    } else {
                        Toast.makeText(getApplicationContext(), "No Satellite signals, try outdoors", Toast.LENGTH_SHORT).show();
                    }
                    if (latLng != null) {
                        CameraUpdate cameraUpdate;
                        if (zoom){
                            cameraUpdate = CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder()
                                    .target(latLng)
                                    .zoom(18)// min = 2.0, max = 21.0
                                    .bearing(degrees)
                                    .tilt(googleMap.getCameraPosition().tilt)
                                    .build());
                        }
                        else{
                            cameraUpdate = CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder()
                                    .target(latLng)
                                    .bearing(degrees)
                                    .tilt(googleMap.getCameraPosition().tilt)
                                    .build());
                        }
                        googleMap.animateCamera(cameraUpdate);
                    }
                } else {
                    enableLocation();
                }
            } else {
                ChangeColor.ofButtonDrawableToNormal(getApplicationContext(), btn_currLoc);
            }
        } else {
            requestLocationPermission();
        }
    }

    // Permissions
    void gotLocationPermission() {
        if (currentLocationPressed) {
            Log.d(TAG, "CALLING trackCurrentLocation from gotLocationPermission");
            trackCurrentLocation(true);
        } else if (!isLocationEnabled()) {
            enableLocation();
        }
    }

    boolean checkLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    void requestLocationPermission() {
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
        switch (requestCode) {
            case LOCATION_PERMISSION_ID:
                if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    // if permission is not granted.
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                            // onDenyClick

                        } else {
                            // don't show again checked and deny clicked.
                            // called always on start if that option was checked.
                            permissionUI();
                        }
                    }
                } else {
                    // allow clicked.
                    // called always on start if that option was clicked.
                    // write the funcs here from where permission was requested.
                    gotLocationPermission();

                }
                break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public void permissionUI() {
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

    // Location listener - triggers when the location is changed.
    @Override
    public void onLocationChanged(Location currentLocation) {
        lat_value = currentLocation.getLatitude();
        long_value = currentLocation.getLongitude();
        // got_location is used in trackCurrentLocation to obtain lat_value & long_value
        got_location = true;
        // Displaying the latitude & longitude
        displayLatLngValues();
        // setting altitude value
        if (currentLocation.hasAltitude()){
            alt_value = (float) currentLocation.getAltitude();
            altitude.setText(String.valueOf(Conversions.decimalPrecision(getApplicationContext(), alt_value)).concat(" mts."));
        }else {
            altitude.setText(R.string.not_available);
        }
        // setting direction value
        if (currentLocation.hasBearing()){
            degrees = currentLocation.getBearing();
            direction.setText(String.valueOf(Conversions.degreesToDirection(getApplicationContext(), degrees)+" ("+degrees+(char)176+")"));
        }else {
            direction.setText(R.string.not_available);
        }

        // setting accuracy value
        if (currentLocation.hasAccuracy()){
            acc_value = currentLocation.getAccuracy();
            accuracy.setText(String.valueOf(Character.toString((char)177)+" "+
                    Conversions.decimalPrecision(getApplicationContext(), acc_value)+" mts."));
        }else {
            accuracy.setText(R.string.not_available);
        }

        if (currentLocationPressed){
            trackCurrentLocation(true);
        }

        //////////////////////////////////////////////////
        // The Speed thing...
        speed = SpeedoValues.getDisplaySpeed(currentLocation,
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getInt("speedoUnits", 0));
        // updating the speed refresh value per location change!
        if (speed == -1) {
            speedRefresh++;
        }
        // setting the corresponding speed value in UI
        setSpeedoValues();

        //////////////////////////////////////////////////
        // The distance thing...
        if (currentLocation.hasAccuracy() &&
                (OdoValues.getDistance(prevLocation, currentLocation) >= acc_value)){
            distance += OdoValues.getDistance(prevLocation, currentLocation);
        }
        // saving distance in shared preferences.
        SharedPrefs.setDistance(getApplicationContext(), distance);
        // sets the odo values in UI
        setOdoValues();


        Log.d(TAG, "onLocationChanged: "+lat_value+" "+long_value+" S:"+currentLocation.getSpeed()+" D:"+distance
        +" Ac:"+acc_value);

        prevLocation = currentLocation;
    }

    //Google API's part
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        // getting & setting the lastLocation.
        Log.d(TAG, "LocationServices API connected...");
        if (checkLocationPermission()) {
            if (showLastLocation) {
                showLastLocation = false;
                lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
                if (lastLocation != null) {
                    Log.d(TAG, "Got the Last Location...");
                    lat_value = lastLocation.getLatitude();
                    long_value = lastLocation.getLongitude();
                    displayLatLngValues();
                    CameraUpdate cameraUpdate = CameraUpdateFactory
                            .newLatLngZoom(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()), 18);
                    googleMap.animateCamera(cameraUpdate);
                } else {
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

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    // triggered when location is turned ON.
    @Override
    public void onProviderEnabled(String provider) {
        Log.d(TAG, "onProviderEnabled: ");
    }

    // triggered when location is turned OFF from ON
    @Override
    public void onProviderDisabled(String provider) {
        Log.d(TAG, "onProviderDisabled: ");
        Toast.makeText(this, "Enable GPS to calculate SPEED!", Toast.LENGTH_LONG).show();
        // disabling the button.
        currentLocationPressed = false;
        // calling this so that the tint of the button is changed.
        trackCurrentLocation(false);
    }

    // for search fragment.
    @Override
    public void onPlaceSelected(Place place) {
        currentLocationPressed = false;
        trackCurrentLocation(false);
        // Removes all markers, polylines, polygons, overlays, etc from the map.
        googleMap.clear();
        googleMap.addMarker(new MarkerOptions()
                .position(place.getLatLng())
                .title(place.getName().toString()));
        googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(place.getViewport(), 50));
    }

    @Override
    public void onError(Status status) {

    }
}