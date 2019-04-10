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
import de.dbuscholl.fahrplanauskunft.gui.ConnectionsListView;
import de.dbuscholl.fahrplanauskunft.gui.services.TripRecordingService;
import de.dbuscholl.fahrplanauskunft.gui.fragments.ConnectionsFragment;
import de.dbuscholl.fahrplanauskunft.network.entities.PrognosisCalculationItem;
import de.dbuscholl.fahrplanauskunft.network.entities.PrognosisCalculationResult;
import de.dbuscholl.fahrplanauskunft.network.tasks.PrognosisTask;
import de.dbuscholl.fahrplanauskunft.network.tasks.TripInfoDownloadTask;
import de.dbuscholl.fahrplanauskunft.network.entities.Connection;

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
        if (diffAlight > -60 && diffAlight < 0) {
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Snackbar make = Snackbar.make(layout, "Bist du mit dieser Verbindung gefahren?", Snackbar.LENGTH_INDEFINITE);
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

        if (diffBoarding > -2 && diffBoarding < 25) {
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Snackbar make = Snackbar.make(layout, "Möchtest du diese Verbindung aufzeichnen?", Snackbar.LENGTH_INDEFINITE);
                    make.setAction("Ja!", new Action() {
                    });
                    make.show();
                }
            }, 3000);
        }

    }

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

    private void fn_permission() {
        if ((ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            if ((ActivityCompat.shouldShowRequestPermissionRationale(TripDetailActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION))) {
            } else {
                ActivityCompat.requestPermissions(TripDetailActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSIONS);
            }
            Toast.makeText(getApplicationContext(), "Please enable the gps", Toast.LENGTH_SHORT).show();
        } else {
            boolean_permission = true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_PERMISSIONS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    boolean_permission = true;
                    Snackbar make = Snackbar.make(layout, "Möchtest du diese Verbindung aufzeichnen?", Snackbar.LENGTH_INDEFINITE);
                    make.setAction("Ja!", new Action() {
                    });
                    make.show();
                } else {
                    Toast.makeText(getApplicationContext(), "Please allow the permission", Toast.LENGTH_LONG).show();

                }
            }
        }
    }

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
                                Toast.makeText(getApplicationContext(), "Kann nicht aufgenommen werden.", Toast.LENGTH_LONG).show();
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
                Toast.makeText(getApplicationContext(), "Please enable the gps", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private interface ServiceConnectedCallback {
        void onConnected();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(pt!=null) {
            pt.cancel(true);
        }
    }
}
