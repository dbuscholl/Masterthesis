package de.dbuscholl.fahrplanauskunft.gui.services;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

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
import de.dbuscholl.fahrplanauskunft.network.tasks.TripInfoDownloadTask;
import de.dbuscholl.fahrplanauskunft.network.entities.Connection;
import de.dbuscholl.fahrplanauskunft.network.entities.CustomLocation;
import de.dbuscholl.fahrplanauskunft.network.entities.StopPoint;

import static de.dbuscholl.fahrplanauskunft.common.App.CHANNEL_ID;
import static de.dbuscholl.fahrplanauskunft.common.App.CHANNEL_ID_DONE_CHANNEL;

/**
 * <p>This is the main class for recording actual trips. The insane work it does is absolutely important for the whole
 * application.</p>
 * <p>As soon as it gets started it runs an almost endless timer which has some things to do. The <b>first thing</b>
 * is to check if the recording queue is empty. If not it marks the next as currently recording trip. Otherwise it stops
 * the service because it was started unnecessarily.</p>
 * <p>The <b>second step</b> is to check wheter the selected trip is already departed. It is not necessary to record data
 * if not.</p>
 * <p>The <b>third step</b> when the trip is departed is to get the acutal geolocation data. This contains latitude, longitude,
 * accuracy and altitude.</p>
 * <p>As for the <b>last step</b> the service checks whether the trip has reached its ending by checking against the
 * realtime information provided by TRIAS. This also gets updated every three minutes.</p>
 * <p>This whole procedure repeats every 20 seconds which can be configured by notify_interval.</p>
 */
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

    private ArrayList<CustomLocation> locations = new ArrayList<>();
    private static List<Connection> recordingQueue = Collections.synchronizedList(new ArrayList<Connection>());
    private static List<String> requestStrings = Collections.synchronizedList(new ArrayList<String>());
    private static List<FinishedRecording> finishedRecordings = Collections.synchronizedList(new ArrayList<FinishedRecording>());
    private Connection currentlyRecordingTrip;
    private String currentlyRecordingTripRequest;
    private String realtimeArrival;
    private long lastRealtimeObtain;
    private boolean running = false;

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

    /**
     * This function is called when the service is started. It first starts the timer task and then sets itself as a
     * foreground service by creating the fixed notification including all listeners and stuff.
     * @param intent the one who initiated the start of the service
     * @param flags optional additional options
     * @param startId
     * @return indicator whether the service has been started successfully or not.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //make this foreground service
        if (!running) {
            mTimer = new Timer();
            mTimer.schedule(new TimerTaskToGetLocation(), 5, notify_interval);
            intent = new Intent(str_receiver);
        }

        Intent notificationClickIntent = new Intent(this, MainActivity.class);
        PendingIntent notificationClickPendingIntent = PendingIntent.getActivity(this, 0, notificationClickIntent, 0);

        Intent stopClickIntent = new Intent(this, MainActivity.class);
        stopClickIntent.putExtra("action", "stop");
        PendingIntent stopClickPendingIntent = PendingIntent.getActivity(this, 0, stopClickIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification not = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("ÖPNV Location Tracker")
                .setContentText("Tracking Trip")
                .addAction(R.drawable.ic_stop_black_24dp, "Stop", stopClickPendingIntent)
                .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                .setContentIntent(notificationClickPendingIntent)
                .build();

        startForeground(1, not);
        return START_REDELIVER_INTENT;
    }

    //------------------------- END LIFECYCLE OVERRIDES ---------------------------------------

    /**
     * This is the main class to do location tracking. It is an inner class because it works only in combination with the
     * service.
     * <p>As soon as it gets started it runs an almost endless timer which has some things to do. The <b>first thing</b>
     * is to check if the recording queue is empty. If not it marks the next as currently recording trip. Otherwise it stops
     * the service because it was started unnecessarily.</p>
     * <p>The <b>second step</b> is to check wheter the selected trip is already departed. It is not necessary to record data
     * if not.</p>
     * <p>The <b>third step</b> when the trip is departed is to get the acutal geolocation data. This contains latitude, longitude,
     * accuracy and altitude.</p>
     * <p>As for the <b>last step</b> the service checks whether the trip has reached its ending by checking against the
     * realtime information provided by TRIAS. This also gets updated every three minutes.</p>
     */
    private class TimerTaskToGetLocation extends TimerTask {
        @Override
        public void run() {
            running = true;

            mHandler.post(new Runnable() {
                @Override
                public void run() {

                    // get next Trip to recording if nothing recording
                    if (currentlyRecordingTrip == null && !recordingQueue.isEmpty()) {
                        int next = getNextScheduledTripIndex();
                        currentlyRecordingTrip = recordingQueue.get(next);
                        try {
                            currentlyRecordingTripRequest = requestStrings.get(next);
                        } catch (IndexOutOfBoundsException e) {
                            currentlyRecordingTripRequest = null;
                        }

                        if(currentlyRecordingTrip == null) {
                            stopRecording();
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


                    // check if already departed
                    // repeating procedure for recording
                    if (currentlyRecordingTrip != null) {
                        long now = Calendar.getInstance().getTimeInMillis();
                        long time = FormatTools.parseTrias(currentlyRecordingTrip.getLegs().get(currentlyRecordingTrip.getLegs().size() - 1).getBoarding().getDepartureTime(), null).getTime();
                        if (time - now < 0) {
                            getlocation();
                            if (isTripEnding()) {
                                stopRecording();
                            }
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

    /**
     * This function stops the whole recording process, closes the fixed navigation and stops the service. As there are
     * some things to do it became an own function.
     */
    public void stopRecording() {
        newFinishedRecording();
        endCurrentTripRecording();
        if (recordingQueue.isEmpty()) {
            mTimer.cancel();
            mTimer.purge();
            stopForeground(true);
            running = false;
            stopSelf();
            Log.d(this.getClass().getName(), "Service stopped");
        }
    }

    /**
     * This creates a new instance of the finished recording class containing all collected location data. This is being
     * serialized and stored into the local storage then for the questionnaire.
     */
    private void newFinishedRecording() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String json = sharedPref.getString("recordings", null);

        if (json != null) {
            finishedRecordings = new Gson().fromJson(json, new TypeToken<List<FinishedRecording>>() {
            }.getType());
        }

        if (finishedRecordings != null) {
            FinishedRecording f = new FinishedRecording();
            f.setRecordingData(locations);
            f.setConnection(currentlyRecordingTrip);
            finishedRecordings.add(f);

            SharedPreferences.Editor editor = sharedPref.edit();

            String recordings = new Gson().toJson(finishedRecordings);
            editor.putString("recordings", recordings).apply();
        }
    }

    /**
     * As the connections array is not sorted by departure time this function returns the position inside that array of which
     * the connection is the next to depart.
     * @return index of the next departing connection.
     */
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

    /**
     * This function creates a custom location object and saves it into the collected data array. First it checks if
     * the service is allowed to access gps because the user can deactivate that at any time in the system settings.
     * Then it goes for the gps and tries to get location data. If that was not successful it goes for Networklocation.
     */
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

    /**
     * This function updates the realtime arrival given by the TRIAS-interface to automatically stop a recording at the right
     * time.
     */
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

    /**
     * This function checks if the user reached the end of the trip which he wanted to record. This is currently done by
     * comparing realtime arrival time of the given connection with the current time.
     * @return true if he is at the end, false if continue recording
     */
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

                // TODO: also check if he is close to the destination station by comparing geocordinates.
                if (endTime != 0) {
                    return endTime <= time;
                }
            }

        } catch (NullPointerException e) {
            return true;
        }

        return true;
    }

    /**
     * This function ends the current trips recording. As there are some things to do for that it became an own function.
     * First it needs to remove the trip from the recordingTrips-Array. Then it removes the TripRequest and actual data
     * for the realtime arrival as there is no trip to record anymore and then it clears the collected locations array.
     * As final step it shows the notification that recording has finished and the user should start the questionnaire.
     */
    private void endCurrentTripRecording() {
        recordingQueue.remove(currentlyRecordingTrip);
        currentlyRecordingTrip = null;
        if (currentlyRecordingTripRequest != null) {
            requestStrings.remove(currentlyRecordingTripRequest);
            currentlyRecordingTripRequest = null;
        }
        realtimeArrival = null;
        lastRealtimeObtain = 0;
        locations = new ArrayList<CustomLocation>();

        sendNotification();
    }

    /**
     * Informing the user about the recordings finish this function comes to take place. But before showing the notification
     * we need to make sure if the trip is really done and this function was not called accidentally. This is done by checking
     * for the finishedrecordings array. Then it extracts service information about the trip he was recording and finally showing
     * the notification with these information.
     */
    private void sendNotification() {
        if (finishedRecordings == null || finishedRecordings.size() <= 0) {
            return;
        }

        Connection connection = finishedRecordings.get(finishedRecordings.size() - 1).getConnection();
        if (connection.getLegs() == null || connection.getLegs().size() <= 0) {
            return;
        }
        StopPoint boarding = connection.getLegs().get(0).getBoarding();
        StopPoint alighting = connection.getLegs().get(connection.getLegs().size() - 1).getAlighting();

        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.putExtra("fragment", "queue");
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID_DONE_CHANNEL)
                .setContentTitle("Fahrt nach " + alighting.getName() + " aufgezeichnet!")
                .setContentText("Tippen um Aufzeichnung abzuschließen")
                .setSmallIcon(R.drawable.ic_check_black_24dp)
                .setContentIntent(pendingIntent)
                .build();

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Service.NOTIFICATION_SERVICE);
        notificationManager.notify(2, notification);
    }

    /**
     * This function allows starting the questionnaire with the currently recording trip.
     */
    private void startQuestionaire() {
        Questionnaire q = new Questionnaire(this.getApplicationContext(), currentlyRecordingTrip);
        q.setRecordingData(new ArrayList<>(locations));
        q.startForPastConnection();
    }

    /**
     * Getter for the list of currently recorded location data.
     * @return
     */
    public ArrayList<CustomLocation> getLocations() {
        return locations;
    }

    /**
     * This function adds a new connection to the recording list. As a user cannot be at two places at the same time this
     * function also checks if the trips overlap in their time where they take place. Only when they dont it will be added!
     * @param connection the connection which should be inserted into the array
     * @return true if successful, false if not.
     */
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

    /**
     * adds optional request string to the request.
     * @param request
     */
    public void addRequestString(String request) {

    }

    /**
     * Inserts the given Android-Location-Class-Instance into the collected locations array by converting it into a
     * Instance of the CustomLocation-Class.
     * @param location Location which should be inserted.
     */
    private void update(Location location) {
        CustomLocation cl = new CustomLocation();
        cl.setLatitude(location.getLatitude());
        cl.setLongitude(location.getLongitude());
        cl.setAltitude(location.getAltitude());
        cl.setAccuracy(location.getAccuracy());
        cl.setTime(location.getTime());
        locations.add(cl);
    }

    /**
     * getter for the recordingqueue of connections
     * @return
     */
    public static List<Connection> getRecordingQueue() {
        return recordingQueue;
    }

    /**
     * getter for the list of recordings that are done
     * @return
     */
    public static List<FinishedRecording> getFinishedRecordings() {
        return finishedRecordings;
    }

    /**
     * Entity Class for a FinishedRecording. This contains the connection itself and the recordingData as array of Custom
     * Location class.
     */
    public class FinishedRecording {
        private Connection connection;
        private ArrayList<CustomLocation> recordingData;

        public Connection getConnection() {
            return connection;
        }

        public void setConnection(Connection connection) {
            this.connection = connection;
        }

        public ArrayList<CustomLocation> getRecordingData() {
            return recordingData;
        }

        public void setRecordingData(ArrayList<CustomLocation> recordingData) {
            this.recordingData = recordingData;
        }
    }

    /**
     * Binder for the GPS
     */
    public class GpsBinder extends Binder {
        public TripRecordingService getService() {
            return TripRecordingService.this;
        }
    }
}