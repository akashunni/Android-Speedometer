package com.quintlr.speedometer;

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
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ContextThemeWrapper;
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
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity implements LocationListener, SensorEventListener,
        OnMapReadyCallback, SharedPreferences.OnSharedPreferenceChangeListener{
    ValuesTextView speedo, odometer;
    UnitsTextView speed, odo;
    private final String DISTANCE = "Distance";
    Button reset_btn;
    TextView position, altitude, direction;
    ImageButton settings_btn, curr_location, search, map_type, zoom_out;
    String units_speedo,units_odo;
    LocationManager locationManager;
    Location prev_location = null;
    boolean current_location_pressed = false, got_location = false;
    final int LOCATION_REQUEST_CODE = 1;
    double distance = 0, lat_value=0, long_value=0;
    int distance_refresh = 0, speed_refresh = 0;
    GoogleMap googleMap;
    Context context = MainActivity.this;
    SharedPrefs sharedPrefs_distance;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        speedo = (ValuesTextView) findViewById(R.id.speedo);
        odometer = (ValuesTextView) findViewById(R.id.odometer);
        reset_btn = (Button) findViewById(R.id.reset_btn);
        position = (TextView) findViewById(R.id.position);
        settings_btn = (ImageButton) findViewById(R.id.settingsbtn);
        curr_location = (ImageButton) findViewById(R.id.curr_location);
        search = (ImageButton) findViewById(R.id.search_btn);
        map_type = (ImageButton) findViewById(R.id.map_type);
        zoom_out = (ImageButton) findViewById(R.id.zoom_out);
        altitude = (TextView) findViewById(R.id.altitude);
        direction = (TextView) findViewById(R.id.direction);
        speed = (UnitsTextView) findViewById(R.id.speedounits);
        odo = (UnitsTextView) findViewById(R.id.odometerunits);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        sharedPrefs_distance = new SharedPrefs(context, DISTANCE);
        distance = sharedPrefs_distance.getDoubleValue();
        if(distance==0){
            reset_btn.setEnabled(false);
        }

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (checkPermission()) {
            Log.d("akash", "onCreate: permission mil gaya");
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        } else {
            requestPermission();
        }

        // reset btn click listener //
        reset_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                distance = 0;
                sharedPrefs_distance.changePrefs(distance);
                odometer.setText(String.format("%4.02f", distance));
            }
        });

        // Image Button Click Listeners //
        // settings btn click listener //
        settings_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, Preferences.class);
                startActivity(intent);
            }
        });
        // current location click listener //
        curr_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                current_location_pressed = !current_location_pressed;
                if(current_location_pressed){
                    DrawableCompat.setTint(curr_location.getDrawable(), ContextCompat.getColor(context, R.color.green));
                    currentLocation(true);
                }else {
                    DrawableCompat.setTint(curr_location.getDrawable(), ContextCompat.getColor(context, R.color.pureWhite));
                }
            }
        });
        // search button click listener //
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText search_location = new EditText(context);
                final AlertDialog.Builder alertBuilder = new AlertDialog.Builder(new ContextThemeWrapper(context, R.style.AlertTheme));
                alertBuilder.setTitle("Search a location");
                search_location.setInputType(InputType.TYPE_CLASS_TEXT);
                alertBuilder.setView(search_location);
                alertBuilder.setPositiveButton("SEARCH", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(context, "Searching...", Toast.LENGTH_SHORT).show();
                        dialog.cancel();
                        searchLocation(search_location.getText().toString());
                    }
                });
                alertBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                AlertDialog alertDialog = alertBuilder.create();
                if(alertDialog != null && !alertDialog.isShowing()) {
                    alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                    alertDialog.show();
                }
            }
        });
        // map type button click listener //
        map_type.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(googleMap.getMapType() == GoogleMap.MAP_TYPE_NORMAL){
                    DrawableCompat.setTint(map_type.getDrawable(), ContextCompat.getColor(context, R.color.green));
                    googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                }
                else{
                    DrawableCompat.setTint(map_type.getDrawable(), ContextCompat.getColor(context, R.color.pureWhite));
                    googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                }
            }
        });
        //zoom_out button listener. //
        zoom_out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                float zoomValue = googleMap.getCameraPosition().zoom;
                zoomValue--;
                CameraUpdate cameraUpdate = CameraUpdateFactory.zoomTo(zoomValue);
                googleMap.animateCamera(cameraUpdate);
            }
        });
        /////////////////////////////////////////////////////////////////////

        // Advertisement details are hidden due to privacy.
        MobileAds.initialize(this, "ca-app-pub-6048897646109743~2196223717");
        AdView adView = (AdView) findViewById(R.id.ad_view);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                //.addTestDevice("DC4B67B8D30D957B6CD72BACEC70E787")      //6.0.1
                //.addTestDevice("3A63F534B511962CDA59A771AB882BB3")   //5.1.1
                .build();
        if (adView != null) {
            adView.loadAd(adRequest);
        }
        //
        // binding map to fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        setUnits();

    }

    public void setUnits(){
        units_speedo = sharedPreferences.getString("units_speedo","1");
        units_odo = sharedPreferences.getString("units_odo","1");
        switch (units_speedo){
            case "1":speed.setText("km/hr");
                break;
            case "2":speed.setText("mi/hr");
                break;
            case "3":speed.setText("mt/sc");
                break;
        }
        switch (units_odo){
            case "1":odo.setText("km");
                odometer.setText(String.format("%4.02f", distance/1000));
                break;
            case "2":odo.setText("mi");
                odometer.setText(String.format("%4.02f", distance/1609.344));
                break;
            case "3":odo.setText("mt");
                odometer.setText(String.format("%4.02f", distance));
                break;
        }
    }

    public void currentLocation(boolean zoom){
        promptGPS();
        if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            LatLng latLng = null;
            if(got_location){
                latLng = new LatLng(lat_value, long_value);
            }else if(googleMap.isMyLocationEnabled()){
                latLng = new LatLng(googleMap.getMyLocation().getLatitude(), googleMap.getMyLocation().getLongitude());
                Toast.makeText(context, "No Satellite signals, location may be inaccurate", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(context, "No Satellite signals, try outdoors", Toast.LENGTH_SHORT).show();
            }
            if(latLng!=null){
                CameraUpdate cameraUpdate;
                if(zoom)
                    cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 18);
                else
                    cameraUpdate = CameraUpdateFactory.newLatLng(latLng);
                googleMap.animateCamera(cameraUpdate);
            }
        }else{
            DrawableCompat.setTint(curr_location.getDrawable(), ContextCompat.getColor(context, R.color.pureWhite));
        }
    }


    public void promptGPS(){
        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Location is TURNED OFF. To calculate values and position, please enable Location. Would you like to do it now?")
                    .setCancelable(false)
                    .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                    })
                    .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                            dialog.cancel();
                            Toast.makeText(context, "Can't retrieve location. To access, enable location", Toast.LENGTH_SHORT).show();
                        }
                    });
            final AlertDialog alert = builder.create();
            alert.show();
        }
    }

    public boolean checkPermission() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return false;
        } else {
            return true;
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    public void requestPermission() {
        AlertDialog alertDialog;
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                Log.d("akash", "requestPermission: 2");
                final AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
                alertBuilder.setTitle("Permission Required!");
                alertBuilder.setMessage("We need your location to calculate the speed. So, turning on the permission to use location is mandatory. Would you like to turn it on now?");
                alertBuilder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
                    }
                });
                alertBuilder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
                alertDialog = alertBuilder.create();
                alertDialog.show();
            }

        }

    }

    void searchLocation(String location){
        googleMap.clear();
        //location = "Sydney";
        List<Address> address_list = null;
        if(location!=null && !location.equals("")){
            Geocoder geocoder = new Geocoder(context);
            try {
                address_list = geocoder.getFromLocationName(location,1);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Address address;
        if (address_list != null) {
            if(!address_list.isEmpty()){
                address = address_list.get(0);
                LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                googleMap.addMarker(new MarkerOptions().position(latLng).title(location));
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 10);
                //CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(new LatLngBounds(latLng,latLng), 15);
                googleMap.animateCamera(cameraUpdate);
            }else
                Toast.makeText(MainActivity.this, "No location exists with this name", Toast.LENGTH_LONG).show();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case LOCATION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(), "Permission Granted !", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "You need to grant permission for using this app.", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    @Override
    public void onLocationChanged(Location curr_location) {
        lat_value = curr_location.getLatitude();
        long_value = curr_location.getLongitude();
        position.setText("Pos : ("+String.format("%3.02f", lat_value)+", "+String.format("%3.02f", long_value)+")");
        altitude.setText("Alt : "+curr_location.getAltitude()+" m");
        reset_btn.setEnabled(true);
        got_location = true;

        if(current_location_pressed){
            currentLocation(false);
        }

        if (curr_location.hasSpeed()) {
            speed_refresh = 0;
            double speed = 0;
            switch (units_speedo){
                case "1":speed = curr_location.getSpeed()*(18/5);
                    break;
                case "2":speed = curr_location.getSpeed()*2.2369;
                    break;
                case "3":speed = curr_location.getSpeed();
                    break;
            }

            ///// check for overspeed /////
            if (speed <= 999.9)
                speedo.setText(String.format("%3.01f", speed));
            else
                speedo.setText("high");
            //////
        } else {
            speed_refresh++;
            if(speed_refresh>=3)
                speedo.setText("----");
        }
        if (prev_location != null) {
            //distance += curr_location.distanceTo(prev_location);
            if (distance_refresh == 7) {
                Location new_old_location = prev_location;
                distance += curr_location.distanceTo(new_old_location);
                distance_refresh = 0;
                double display_distance=0;
                switch (units_odo){
                    case "1": display_distance = distance/1000;
                        break;
                    case "2": display_distance = distance/1609.344;
                        break;
                    case "3": display_distance = distance;
                        break;
                }
                if (display_distance <= 999.99){
                    odometer.setText(String.format("%4.02f", display_distance));
                    sharedPrefs_distance.changePrefs(distance);
                }
                else {
                    distance = 0;
                }
            }
            distance_refresh++;
        }
        prev_location = curr_location;
    }


    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {
        Toast.makeText(MainActivity.this, "Waiting for GPS signals.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProviderDisabled(String provider) {
        promptGPS();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR){
            float[] in = new float[16];
            float[] R = new float[16];
            float[] orientationVals = new float[4];
            int dir;
            in[0] = 1;
            in[4] = 1;
            in[8] = 1;
            in[12] = 1;
            SensorManager.getRotationMatrixFromVector(in, event.values);
            SensorManager.remapCoordinateSystem(in, SensorManager.AXIS_Y, SensorManager.AXIS_Z, in);
            SensorManager.getOrientation(in, orientationVals);
            dir = (int) Math.toDegrees(orientationVals[0]);
            direction.setText("Dir : "+dir);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onMapReady(GoogleMap Map) {
        Log.d("akash", "onMapReady: ");
        this.googleMap = Map;
        if(checkPermission()) {
            googleMap.setMyLocationEnabled(true);
            googleMap.setTrafficEnabled(true);
        }else
            requestPermission();
        googleMap.getUiSettings().setMyLocationButtonEnabled(false);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        setUnits();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(checkPermission()){
            locationManager.removeUpdates(this);
        }
    }

}