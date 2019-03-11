package de.dbuscholl.fahrplanauskunft.gui.services;

import android.Manifest;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
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
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import de.dbuscholl.fahrplanauskunft.FormatTools;
import de.dbuscholl.fahrplanauskunft.R;
import de.dbuscholl.fahrplanauskunft.gui.activities.MainActivity;
import de.dbuscholl.fahrplanauskunft.gui.activities.Questionnaire;
import de.dbuscholl.fahrplanauskunft.network.TripInfoDownloadTask;
import de.dbuscholl.fahrplanauskunft.network.entities.Connection;

import static de.dbuscholl.fahrplanauskunft.gui.App.CHANNEL_ID;

public class TripRecordingService extends Service implements LocationListener {
    private final IBinder gpsBinder = new GpsBinder();

    boolean isGPSEnable = false;
    boolean isNetworkEnable = false;
    LocationManager locationManager;
    Location location;

    private Handler mHandler = new Handler();
    private Timer mTimer = null;
    long notify_interval = 20000;

    public static String str_receiver = "service.locationreceiver";
    Intent intent;

    private ArrayList<Location> locations = new ArrayList<>();
    private static List<Connection> recordingQueue = Collections.synchronizedList(new ArrayList<Connection>());
    private static List<String> requestStrings = Collections.synchronizedList(new ArrayList<String>());
    private Connection currentlyRecordingTrip;
    private String currentlyRecordingTripRequest;
    private String realtimeArrival;
    private long lastRealtimeObtain;

    public TripRecordingService() {

    }

    // ----------------------- LIFECYCLE OVERRIDES ----------------------------------
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

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //make this foreground service
        /*
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        int highScore = sharedPref.getInt("size", -2);
        Toast.makeText(this, String.valueOf(highScore), Toast.LENGTH_LONG).show();
        */

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        Notification not = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("ÖPNV Location Tracker")
                .setContentText("Tracking Trip")
                .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, not);
        return START_REDELIVER_INTENT;
    }

    //------------------------- END LIFECYCLE OVERRIDES ---------------------------------------

    private class TimerTaskToGetLocation extends TimerTask {
        @Override
        public void run() {

            mHandler.post(new Runnable() {
                @Override
                public void run() {

                    // get next Trip to recording if nothing recording
                    if (currentlyRecordingTrip == null) {
                        int next = getNextScheduledTripIndex();
                        currentlyRecordingTrip = recordingQueue.get(next);
                        try {
                            currentlyRecordingTripRequest = requestStrings.get(next);
                        } catch (IndexOutOfBoundsException e) {
                            currentlyRecordingTripRequest = null;
                        }

                        int size = currentlyRecordingTrip.getLegs().size();
                        if (size > 0) {
                            String arrivalTimeEstimated = currentlyRecordingTrip.getLegs().get(size - 1).getAlighting().getArrivalTimeEstimated();
                            if (arrivalTimeEstimated != null && !(arrivalTimeEstimated.equals(""))) {
                                realtimeArrival = arrivalTimeEstimated;
                                lastRealtimeObtain = Calendar.getInstance().getTimeInMillis();
                            }
                        }

                    }

                    // repeating procedure for recording
                    if (currentlyRecordingTrip != null) {
                        getlocation();
                        if (isTripEnding()) {
                            startQuestionaire();
                            endCurrentTripRecording();
                            // TODO: send recorded locations to server
                        }
                    }

                    long now = Calendar.getInstance().getTimeInMillis();
                    if (realtimeArrival != null && !(realtimeArrival.equals(""))) {
                        if (now - lastRealtimeObtain > 180000) {
                            updateRealtimeArrival();
                        }
                    }
                }
            });

        }
    }

    private int getNextScheduledTripIndex() {
        int index = -1;
        long smallestTime = Long.MAX_VALUE;

        if (recordingQueue.isEmpty()) {
            return -1;
        }

        try {
            for (int i = 0; i < recordingQueue.size(); i++) {
                Connection c = recordingQueue.get(i);

                long time = FormatTools.parseTrias(c.getStartTime(), null).getTime();
                if (time < smallestTime) {
                    smallestTime = time;
                    index = i;
                }
            }
        } catch (NullPointerException e) {
            return -1;
        }

        if (index >= 0) {
            return index;
        }

        return -1;
    }

    private void getlocation() {
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
                    Log.e("length", String.valueOf(locations.size()));

                    update(location);
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
                    Log.e("length", String.valueOf(locations.size()));

                    update(location);
                }
            }
        }
    }

    private void updateRealtimeArrival() {
        TripInfoDownloadTask tidt = new TripInfoDownloadTask();
        tidt.setOnSuccessEvent(new TripInfoDownloadTask.SuccessEvent() {
            @Override
            public void onSuccess(ArrayList<Connection> result) {
                for (Connection c : result) {
                    if (c.equals(currentlyRecordingTrip)) {
                        int size = c.getLegs().size();
                        if (size > 0) {
                            String arrival = c.getLegs().get(size - 1).getAlighting().getArrivalTimeEstimated();
                            if (arrival != null && !(arrival.equals(""))) {
                                realtimeArrival = arrival;
                            }
                        }
                        lastRealtimeObtain = Calendar.getInstance().getTimeInMillis();
                    }
                }
            }
        });
        tidt.execute(currentlyRecordingTripRequest);
    }

    private boolean isTripEnding() {
        try {

            long time = Calendar.getInstance().getTimeInMillis();
            if (currentlyRecordingTrip != null) {
                String arrivalTimeEstimated = currentlyRecordingTrip.getLegs().get(currentlyRecordingTrip.getLegs().size() - 1).getAlighting().getArrivalTimeEstimated();

                long endTime;

                if (realtimeArrival != null && !(realtimeArrival.equals(""))) {
                    endTime = FormatTools.parseTrias(realtimeArrival, null).getTime();
                } else {
                    endTime = FormatTools.parseTrias(currentlyRecordingTrip.getEndTime(), null).getTime();
                }

                if (endTime != 0) {
                    return endTime <= time;
                }
            }

        } catch (NullPointerException e) {
            return true;
        }

        return true;
    }

    private void endCurrentTripRecording() {
        recordingQueue.remove(currentlyRecordingTrip);
        currentlyRecordingTrip = null;
        if (currentlyRecordingTripRequest != null) {
            requestStrings.remove(currentlyRecordingTripRequest);
            currentlyRecordingTripRequest = null;
        }
        realtimeArrival = null;
        lastRealtimeObtain = 0;
        locations.clear();
    }

    private void startQuestionaire() {
        Questionnaire q = new Questionnaire(this.getApplicationContext(), currentlyRecordingTrip);
        q.setRecordingData(new ArrayList<>(locations));
        q.startForPastConnection();
    }

    public ArrayList<Location> getLocations() {
        return locations;
    }

    public boolean addConnection(Connection connection) {
        try {
            long now = Calendar.getInstance().getTimeInMillis();

            if (!recordingQueue.isEmpty()) {
                long newStartTime = FormatTools.parseTrias(connection.getStartTime(), null).getTime();
                long newEndTime = FormatTools.parseTrias(connection.getEndTime(), null).getTime();

                if (newStartTime < now) {
                    return false;
                }

                for (Connection c : recordingQueue) {
                    long oldStartTime = FormatTools.parseTrias(c.getStartTime(), null).getTime();
                    long oldEndTime = FormatTools.parseTrias(c.getEndTime(), null).getTime();

                    // before existing trip
                    if (newEndTime < oldStartTime) {
                        continue;
                    }
                    // after existing trip
                    if (newStartTime > oldEndTime) {
                        continue;
                    }
                    return false;
                }
            }
        } catch (NullPointerException e) {
            return false;
        }
        recordingQueue.add(connection);
        return true;
    }


    public void addRequestString(String request) {

    }

    private void update(Location location) {
        /*
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("size", locations.size());
        editor.commit();
        */
        locations.add(location);
    }

    public class GpsBinder extends Binder {
        public TripRecordingService getService() {
            return TripRecordingService.this;
        }
    }
}