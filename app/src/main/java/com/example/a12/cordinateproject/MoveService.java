package com.example.a12.cordinateproject;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import java.util.concurrent.TimeUnit;

public class MoveService extends Service {
    private  double latitude, longitude, altitude;
    final String LOG_TAG = "serviceMoveLogs";
    private Thread moveTread;


    public void onCreate() {
        super.onCreate();
        Log.d(LOG_TAG, "serviceWasCreate");
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "serviceWasStarted");
        /*latitude = intent.getIntExtra("la",0);
        longitude = intent.getIntExtra("lo",0);
        altitude = intent.getIntExtra("al",0);*/
        //-26.902038,-48.671337, 50
        latitude = -26.902038;
        longitude = -48.671337;
        altitude= 50;

        move();
        return super.onStartCommand(intent, flags, startId);
    }

    public void onDestroy() {
        super.onDestroy();
        moveTread.interrupt();

        Log.d(LOG_TAG, "serviceWasDestroy");
    }

    public IBinder onBind(Intent intent) {
        Log.d(LOG_TAG, "serviceOnBind");
        return null;
    }

    void move() {

        moveTread= new Thread(new Runnable() {
            public void run() {
                try {
                while (!Thread.currentThread().isInterrupted()){

                        TimeUnit.SECONDS.sleep(2);
                        latitude+=randomLatitude();
                        longitude+=randomLongitude();
                        mockLocation(latitude,longitude,altitude);

                }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        moveTread.setDaemon(true);
        moveTread.start();


    }

    private double randomLongitude(){
        return (Math.random()-0.5)/100;
    }

    private double randomLatitude(){
        return (Math.random()-0.5)/100;
    }

    public void mockLocation(double latitude,double longitude,double altitude){
        LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setAccuracy( Criteria.ACCURACY_FINE );

        String mocLocationProvider = LocationManager.GPS_PROVIDER;//lm.getBestProvider( criteria, true );

        if ( mocLocationProvider == null ) {
            Toast.makeText(getApplicationContext(), "No location provider found!", Toast.LENGTH_SHORT).show();
            return;
        }

        lm.addTestProvider(mocLocationProvider, false, false, false, false, true, true, true, 0, 5);
        lm.setTestProviderEnabled(mocLocationProvider, true);

        //Location loc = new Location(mocLocationProvider);
        Location mockLocation = new Location(mocLocationProvider); // a string
        mockLocation.setLatitude(latitude);  // double
        mockLocation.setLongitude(longitude);
        //why I must set Altitude
        mockLocation.setAltitude(altitude);
        mockLocation.setTime(System.currentTimeMillis());
        mockLocation.setAccuracy(5);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            mockLocation.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
        }
        lm.setTestProviderLocation( mocLocationProvider, mockLocation);
    }

}
