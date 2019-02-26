package de.dbuscholl.fahrplanauskunft.gui;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by deepshikha on 24/11/16.
 */

public class GoogleService extends Service implements LocationListener {
    private ArrayList<Location> locations = new ArrayList<>();
    private final IBinder gpsBinder = new GpsBinder();

    boolean isGPSEnable = false;
    boolean isNetworkEnable = false;
    LocationManager locationManager;
    Location location;

    double latitude, longitude;
    float accuracy;

    private Handler mHandler = new Handler();
    private Timer mTimer = null;
    long notify_interval = 5000;

    public static String str_receiver = "service.locationreceiver";
    Intent intent;


    public GoogleService() {

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return gpsBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mTimer = new Timer();
        mTimer.schedule(new TimerTaskToGetLocation(), 5, notify_interval);
        intent = new Intent(str_receiver);
    }

    @Override
    public void onLocationChanged(Location location) {

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

    private void fn_getlocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            mTimer.cancel();
            return;
        }
        locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
        isGPSEnable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetworkEnable = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);


        if (isGPSEnable) {
            location = null;
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, this);
            if (locationManager != null) {
                location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (location != null) {
                    Log.e("latitude", location.getLatitude() + "");
                    Log.e("longitude", location.getLongitude() + "");
                    Log.e("accuracy", location.getAccuracy() + "");
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                    accuracy = location.getAccuracy();
                    fn_update(location);
                }
            }
        } else if (isNetworkEnable) {
            location = null;
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, this);
            if (locationManager != null) {
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                if (location != null) {

                    Log.e("latitude", location.getLatitude() + "");
                    Log.e("longitude", location.getLongitude() + "");
                    Log.e("accuracy", location.getAccuracy() + "");

                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                    accuracy = location.getAccuracy();
                    fn_update(location);
                }
            }
        }
    }

    public ArrayList<Location> getLocations() {
        return locations;
    }

    private class TimerTaskToGetLocation extends TimerTask {
        @Override
        public void run() {

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    fn_getlocation();
                }
            });

        }
    }

    private void fn_update(Location location) {
        locations.add(location);
    }


    public class GpsBinder extends Binder {
        public GoogleService getService() {
            return GoogleService.this;
        }
    }
}