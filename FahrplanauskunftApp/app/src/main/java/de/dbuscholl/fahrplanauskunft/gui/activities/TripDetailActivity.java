package de.dbuscholl.fahrplanauskunft.gui.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

import de.dbuscholl.fahrplanauskunft.FormatTools;
import de.dbuscholl.fahrplanauskunft.R;
import de.dbuscholl.fahrplanauskunft.common.App;
import de.dbuscholl.fahrplanauskunft.common.Constants;
import de.dbuscholl.fahrplanauskunft.gui.ConnectionsListView;
import de.dbuscholl.fahrplanauskunft.gui.services.TripRecordingService;
import de.dbuscholl.fahrplanauskunft.gui.fragments.ConnectionsFragment;
import de.dbuscholl.fahrplanauskunft.network.entities.PrognosisCalculationItem;
import de.dbuscholl.fahrplanauskunft.network.entities.PrognosisCalculationResult;
import de.dbuscholl.fahrplanauskunft.network.tasks.PrognosisTask;
import de.dbuscholl.fahrplanauskunft.network.tasks.TripInfoDownloadTask;
import de.dbuscholl.fahrplanauskunft.network.entities.Connection;

/**
 * this is a rather big activity because as soon as it is started several processes occur.
 * <p>the <b>first process</b> tries to get prognosis calculation for the selected connection by asking the backend
 * server. As long as it is still calculating a loading spinner is displayed on the top.</p>
 * <p>the <b>second process</b> creates the actual layout, but an own class is used for that called ConnectionsListAdapter.
 * It takes care about the right visualization of the trip details</p>
 * <p>the <b>third process</b> check wheter a user is allowed to record a trip. This is given when departure time is no more
 * than 25 minutes in future and less than 3 minutes in the past. On the other hand it check wheter the alighting of the
 * connection is no longer than 60 minutes later. The user has the opportunity to start the questionnaire for the selected
 * trip, because he might just forgot to record the trip.</p>
 */
public class TripDetailActivity extends AppCompatActivity implements PrognosisTask.SuccessEvent {
    private static final int REQUEST_PERMISSIONS = 100;
    boolean boolean_permission;
    ScrollView layout;
    Connection connection;
    TripRecordingService gpsService;
    boolean isBound = false;
    SharedPreferences mPref;
    SharedPreferences.Editor medit;
    ServiceConnectedCallback serviceConnectedCallback;
    PrognosisTask pt;
    LinearLayout spinner;

    /**
     * Starts the three explained processes:
     * <p>the <b>first process</b> tries to get prognosis calculation for the selected connection by asking the backend
     * server. As long as it is still calculating a loading spinner is displayed on the top.</p>
     * <p>the <b>second process</b> creates the actual layout, but an own class is used for that called ConnectionsListAdapter.
     * It takes care about the right visualization of the trip details</p>
     * <p>the <b>third process</b> check wheter a user is allowed to record a trip. This is given when departure time is no more
     * than 25 minutes in future and less than 3 minutes in the past. On the other hand it check wheter the alighting of the
     * connection is no longer than 60 minutes later. The user has the opportunity to start the questionnaire for the selected
     * trip, because he might just forgot to record the trip.</p>
     * @param savedInstanceState android stuff
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_detail);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        int position = getIntent().getIntExtra("position", -1);
        if (position == -1) {
            onBackPressed();
        }
        connection = ConnectionsFragment.getCurrentResult().get(position);

        getPrognosis();

        TextView startStation = findViewById(R.id.result_tripstart_text);
        TextView endStation = findViewById(R.id.result_tripend_text);
        TextView dateTextView = findViewById(R.id.result_tripdate);
        spinner = findViewById(R.id.result_spinner);

        startStation.setText(connection.getLegs().get(0).getBoarding().getName());
        endStation.setText(connection.getLegs().get(connection.getLegs().size() - 1).getAlighting().getName());
        dateTextView.setText(FormatTools.parseTriasDate(connection.getStartTime()));

        ConnectionsListView clv = new ConnectionsListView(getApplicationContext()).build(connection);
        layout = findViewById(R.id.result_content);
        layout.addView(clv);

        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getTimeZone("Europe/Berlin"));

        String arrivalTime = connection.getLegs().get(connection.getLegs().size() - 1).getAlighting().getArrivalTime();
        String departureTime = connection.getLegs().get(0).getBoarding().getDepartureTime();
        long diffAlight = (FormatTools.parseTrias(arrivalTime, null).getTime() - cal.getTime().getTime()) / 1000 / 60;
        long diffBoarding = (FormatTools.parseTrias(departureTime, null).getTime() - cal.getTime().getTime()) / 1000 / 60;

        if (diffAlight > (Constants.TRIP_QUESTIONNAIRE_MINDIFF * -1) && diffAlight < Constants.TRIP_QUESTIONNAIRE_MAXDIFF) {
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Snackbar make = Snackbar.make(layout, Constants.MSG_TAKEN_TRIP_PROMPT, Snackbar.LENGTH_INDEFINITE);
                    make.setAction("Ja!", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Questionnaire q = new Questionnaire(TripDetailActivity.this, connection);
                            q.startForPastConnection();
                        }
                    });
                    make.show();
                }
            }, 2000);
        }

        if (diffBoarding > (Constants.TRIP_RECORIDNG_MINDIFF * -1) && diffBoarding < Constants.TRIP_RECORDING_MAXDIFF) {
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Snackbar make = Snackbar.make(layout, Constants.MSG_RECORD_TRIP_PROMPT, Snackbar.LENGTH_INDEFINITE);
                    make.setAction("Ja!", new Action() {
                    });
                    make.show();
                }
            }, 3000);
        }

    }

    /**
     * sends a request to the server to get prognosis data for selected connection. It first encodes the connections as
     * JSON.
     */
    private void getPrognosis() {
        try {
            JSONObject sendingData = new JSONObject();
            JSONObject c = connection.toJSON();
            sendingData.put("connection", c==null?"":c);
            pt = new PrognosisTask(this, getApplicationContext());
            pt.execute(sendingData.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    /**
     * updates the view by revisualizing it but this time with the result of the prognosis request. This is called by the
     * PrognosisTask class.
     * @param items
     */
    @Override
    public void onSuccess(ArrayList<PrognosisCalculationResult> items) {
        spinner.setVisibility(View.GONE);
        if(layout.getChildCount() > 0) {
            layout.removeAllViews();

            ConnectionsListView clv = new ConnectionsListView(getApplicationContext());
            clv.setPrognosis(items);
            clv.build(connection);
            layout.addView(clv);

        }
    }

    /**
     * checks wheter permission for gps exists and asks the user for it. This will trigger the onRequestPermissionResult
     */
    private void fn_permission() {
        if ((ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            if ((ActivityCompat.shouldShowRequestPermissionRationale(TripDetailActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION))) {
            } else {
                ActivityCompat.requestPermissions(TripDetailActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSIONS);
            }
            Toast.makeText(getApplicationContext(), Constants.ERRORMSG_GPS_NOPERMISSION, Toast.LENGTH_SHORT).show();
        } else {
            boolean_permission = true;
        }
    }

    /**
     * this is called when the window where the user is asked to grant gps permission was closed. Based on the reuslt the
     * snackbar is shown again with the possibility to record the trip so that the user doesn't need to reload the acitivity
     * by himself.
     * @param requestCode type of request
     * @param permissions permissions to be allowed
     * @param grantResults permissions granted if there are any
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_PERMISSIONS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    boolean_permission = true;
                    Snackbar make = Snackbar.make(layout, Constants.MSG_RECORD_TRIP_PROMPT, Snackbar.LENGTH_INDEFINITE);
                    make.setAction("Ja!", new Action() {
                    });
                    make.show();
                } else {
                    Toast.makeText(getApplicationContext(), Constants.MSG_GPS_ALLOW_PROMPT, Toast.LENGTH_LONG).show();

                }
            }
        }
    }

    /**
     * This functions is the bridge between the TripRecordingService and the acitivity from which it was started. It fills
     * the class attribute with the reference to the running server via binder logic.
     */
    private ServiceConnection gpsConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            TripRecordingService.GpsBinder binder = (TripRecordingService.GpsBinder) service;
            gpsService = binder.getService();
            isBound = true;
            int size = gpsService.getLocations().size();
            //Toast.makeText(getApplicationContext(), String.valueOf(size), Toast.LENGTH_SHORT).show();
            if(serviceConnectedCallback!=null) {
                serviceConnectedCallback.onConnected();
            }
            unbindService(gpsConnection);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    };

    /**
     * This is the action which starts when the user clicked yes when being asked to record a trip. It firsts starts
     * the permission check which opens a dialog asking the user to grant gps position permission if neccessary. Then it
     * checks wheter the service is already running and starts it if not. However the third step is the binding, so even
     * if the service wasn't started before, it now is and can be bound. The last step is to add the selected connection
     * to the list of recording trips of the service.
     */
    private class Action implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            fn_permission();
            if (boolean_permission) {
                Intent intent = new Intent(getApplicationContext(), TripRecordingService.class);
                if (!App.isMyServiceRunning(TripRecordingService.class, getApplicationContext())) {
                    Log.d(TripDetailActivity.this.getClass().getName(), "started service");
                    ContextCompat.startForegroundService(getApplicationContext(), intent);
                }
                Log.d(TripDetailActivity.this.getClass().getName(), "Service seems to be running");
                if (!isBound) {
                    serviceConnectedCallback = new ServiceConnectedCallback() {
                        @Override
                        public void onConnected() {
                            boolean added = gpsService.addConnection(connection);
                            if (!added) {
                                Toast.makeText(getApplicationContext(), Constants.ERRORMSG_NOT_ADDED_TO_SERVICE, Toast.LENGTH_LONG).show();
                                return;
                            }
                            String request = TripInfoDownloadTask.getRequest();
                            if (request != null) {
                                gpsService.addRequestString(request);
                            }
                        }
                    };

                    bindService(intent, gpsConnection, Context.BIND_AUTO_CREATE);
                    Log.d(TripDetailActivity.this.getClass().getName(), "Bound service");
                }
            } else {
                Toast.makeText(getApplicationContext(), Constants.MSG_GPS_ALLOW_PROMPT, Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Callback by binder interface which is triggered as soon as the service is bound.
     */
    private interface ServiceConnectedCallback {
        void onConnected();
    }

    /**
     * android stuff. Cancel a running prognosis task to stop overload for server and client because calculation might take
     * some time
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(pt!=null) {
            pt.cancel(true);
        }
    }
}
