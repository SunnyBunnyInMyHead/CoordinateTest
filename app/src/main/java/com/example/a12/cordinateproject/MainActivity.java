package com.example.a12.cordinateproject;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static android.location.LocationManager.GPS_PROVIDER;

public class MainActivity extends AppCompatActivity {

    private Button getCoordinates, setCoordinates;
    private TextView getLatitude, getLongitude, getAltitude;
    private TextView result;
    private EditText setLatitude, setLongitude, setAltitude;
    private final String TAG = "App";
    private LocationManager locationManager;
    private LocationListener locationListener;
    private static int satellitesInFix = 0;
    public static int satellites = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getCoordinates = (Button) findViewById(R.id.button);
        setCoordinates = (Button) findViewById(R.id.button2);
        getLatitude = (TextView) findViewById(R.id.textLatitude);
        getLongitude = (TextView) findViewById(R.id.textLongitude);
        getAltitude = (TextView) findViewById(R.id.textAltitude);
        result = (TextView) findViewById(R.id.gps_inform);

        setLatitude = (EditText) findViewById(R.id.setLatitude);
        setLongitude = (EditText) findViewById(R.id.setLongitude);
        setAltitude = (EditText) findViewById(R.id.setAltitude);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {

            @Override
            public void onLocationChanged(Location location) {
                getLatitude.setText("Latitude: " + String.valueOf(location.getLatitude()));
                getLongitude.setText("Longitude: " + String.valueOf(location.getLongitude()));
                getAltitude.setText("Altitude: " + String.valueOf(location.getAltitude()));
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {
                Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                startActivity(intent);
            }
        };
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.INTERNET
                }, 10);
                return;
            }
        } else {
            getCoordinatesButton();
        }
        setCoordinatesButton();
        serviceButtonController();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 10:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getCoordinatesButton();
                }
                return;
        }
    }

    private void getCoordinatesButton() {
        getCoordinates.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View view) {
                locationManager.requestLocationUpdates("gps", 5000, 0, locationListener);
                getGpsInform(locationManager);

            }
        });

    }

    private void setCoordinatesButton() {
        setCoordinates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                double fakeLatitude = getDoubleFromEditText(setLatitude);
                double fakeLongitude = getDoubleFromEditText(setLongitude);
                double fakeAltitude = getDoubleFromEditText(setAltitude);
                // mockLocation(-26.902038,-48.671337, 50);
                // mockLocation(fakeLatitude,fakeLongitude, fakeAltitude);
            }
        });
    }

    private void serviceButtonController() {
        ((Button) findViewById(R.id.sreviceController)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //serviceController();
            }
        });

    }

    public double getDoubleFromEditText(EditText editText) {
        return !String.valueOf(editText.getText()).equals("") ? Double.valueOf(String.valueOf(editText.getText())) : 0.0;
    }

    public void serviceController() {
        Intent intent = new Intent(this, MoveService.class);
        intent.putExtra("la", getDoubleFromEditText(setLatitude));
        intent.putExtra("lo", getDoubleFromEditText(setLongitude));
        intent.putExtra("al", getDoubleFromEditText(setAltitude));

        if (isServiceRunning(MoveService.class)) {
            stopService(intent);
            Toast.makeText(getApplicationContext(), "service off", Toast.LENGTH_SHORT).show();
        } else {
            startService(intent);
            Toast.makeText(getApplicationContext(), "service on", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }

        }
        return false;

    }

    private void getGpsInform(final LocationManager locationManager) {
        calculateCurrentSatellitesNumber();

        //https://developer.android.com/reference/android/location/Location#getAccuracy%28%29
        Location location = new Location(GPS_PROVIDER);
        Map<String, Number> map = new HashMap<>();
        Map<String, Boolean> bmap = new HashMap<>();
        map.put("satellites", satellites);
        map.put("satellitesInFix", satellitesInFix);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            map.put("location getBearingAccuracyDegrees", location.getBearingAccuracyDegrees());
        }
        map.put("location accuracy", location.getAccuracy());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            map.put("SpeedAccuracyMetersPerSecond", location.getSpeedAccuracyMetersPerSecond());
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            map.put("getVerticalAccuracyMeters (Altitude)", location.getVerticalAccuracyMeters());
        }
        bmap.put("location has a horizontal accuracy", location.hasAccuracy());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            bmap.put(" location has a bearing accuracy", location.hasBearingAccuracy());
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            bmap.put(" location has a speed accuracy", location.hasSpeedAccuracy());
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            bmap.put(" location has a vertical accuracy", location.hasVerticalAccuracy());
        }
        Date date = new Date(location.getTime());
        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<String, Number> entry:map.entrySet()){
            String key = entry.getKey();
            Number number = entry.getValue();
            stringBuilder.append(key+": "+number+"\n");
        }
        for (Map.Entry<String, Boolean> entry:bmap.entrySet()){
            String key = entry.getKey();
            Boolean value = entry.getValue();
            stringBuilder.append(key+": "+String.valueOf(value)+"\n");
        }
        stringBuilder.append(String.valueOf(date));
        result.setText(stringBuilder.toString());

    }

    //https://stackoverflow.com/questions/6580603/getting-the-number-of-satellites-from-location-object
    private void calculateCurrentSatellitesNumber() {
        satellites = 0;
        satellitesInFix = 0;
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "there are no such permission");
            return;
        }
        for (GpsSatellite sat : locationManager.getGpsStatus(null).getSatellites()) {
            if (sat.usedInFix()) {
                satellitesInFix++;
            }
            satellites++;
        }
    }

}
