package de.dbuscholl.fahrplanauskunft.gui.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import de.dbuscholl.fahrplanauskunft.FormatTools;
import de.dbuscholl.fahrplanauskunft.R;
import de.dbuscholl.fahrplanauskunft.gui.GoogleService;
import de.dbuscholl.fahrplanauskunft.gui.fragments.ConnectionsFragment;
import de.dbuscholl.fahrplanauskunft.network.entities.Connection;
import de.dbuscholl.fahrplanauskunft.network.entities.Service;
import de.dbuscholl.fahrplanauskunft.network.entities.StopPoint;
import de.dbuscholl.fahrplanauskunft.network.entities.Trip;

public class ResultDetailActivity extends AppCompatActivity {
    private int stdFontsize = 14;
    private int bigFontsize = 18;
    private static final int REQUEST_PERMISSIONS = 100;
    boolean boolean_permission;
    SharedPreferences mPref;
    SharedPreferences.Editor medit;

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

        final Connection connection = ConnectionsFragment.getCurrentResult().get(position);

        TextView startStation = findViewById(R.id.result_tripstart_text);
        TextView endStation = findViewById(R.id.result_tripend_text);
        TextView dateTextView = findViewById(R.id.result_tripdate);

        startStation.setText(connection.getLegs().get(0).getBoarding().getName());
        endStation.setText(connection.getLegs().get(connection.getLegs().size() - 1).getAlighting().getName());
        dateTextView.setText(FormatTools.parseTriasDate(connection.getStartTime()));

        LinearLayout result = new LinearLayout(getApplicationContext());
        result.setOrientation(LinearLayout.VERTICAL);

        ArrayList<Trip> legs = connection.getLegs();
        for (int i = 0; i < legs.size(); i++) {
            Trip t = legs.get(i);
            if (t.getType() == Trip.TripType.TIMED) {
                LinearLayout tripLayout = getTimedTripLayout(t);
                result.addView(tripLayout);
            } else {
                LinearLayout interchangeLayout = getInterchangeTripLayout(t);
                result.addView(interchangeLayout);
            }
            if (i != legs.size() - 1) {
                result.addView(getStrongDivider());
            }
        }

        final ScrollView layout = findViewById(R.id.result_content);
        layout.addView(result);

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
                            Questionnaire q = new Questionnaire(ResultDetailActivity.this, connection);
                            q.startForPastConnection();
                        }
                    });
                    make.show();
                }
            }, 2000);
        }

        if (diffBoarding > -2 && diffBoarding < 15) {
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Snackbar make = Snackbar.make(layout, "Möhctest du diese Verbindung nutzen??", Snackbar.LENGTH_INDEFINITE);
                    make.setAction("Ja!", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            fn_permission();
                            mPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                            medit = mPref.edit();
                            if (boolean_permission) {

                                if (mPref.getString("service", "").matches("")) {
                                    medit.putString("service", "service").commit();

                                    Intent intent = new Intent(getApplicationContext(), GoogleService.class);
                                    startService(intent);

                                } else {
                                    Toast.makeText(getApplicationContext(), "Service is already running", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(getApplicationContext(), "Please enable the gps", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    make.show();
                }
            }, 3000);
        }

    }

    private void fn_permission() {
        if ((ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {

            if ((ActivityCompat.shouldShowRequestPermissionRationale(ResultDetailActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION))) {


            } else {
                ActivityCompat.requestPermissions(ResultDetailActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION

                        },
                        REQUEST_PERMISSIONS);

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

                } else {
                    Toast.makeText(getApplicationContext(), "Please allow the permission", Toast.LENGTH_LONG).show();

                }
            }
        }
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String latutide = intent.getStringExtra("latutide");
            String longitude = intent.getStringExtra("longitude");
            String accuracy = intent.getStringExtra("accuracy");

            Log.d(ResultDetailActivity.this.getClass().getName(), latutide);
            Log.d(ResultDetailActivity.this.getClass().getName(), longitude);
            Log.d(ResultDetailActivity.this.getClass().getName(), accuracy);
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(broadcastReceiver, new IntentFilter(GoogleService.str_receiver));

    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }

    private LinearLayout getInterchangeTripLayout(Trip trip) {
        LinearLayout interchangeLayout = new LinearLayout(getApplicationContext());
        interchangeLayout.setOrientation(LinearLayout.VERTICAL);

        TextView header = new TextView(getApplicationContext());
        header.setTextSize(TypedValue.COMPLEX_UNIT_SP, bigFontsize);
        header.setTextColor(Color.BLACK);
        header.setText("Umstieg:");
        interchangeLayout.addView(header);

        LinearLayout start = new LinearLayout(getApplicationContext());
        start.setOrientation(LinearLayout.HORIZONTAL);

        TextView startTime = new TextView(getApplicationContext());
        startTime.setTextSize(TypedValue.COMPLEX_UNIT_SP, stdFontsize);
        startTime.setTextColor(Color.BLACK);
        startTime.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.2f));
        startTime.setText(FormatTools.parseTriasTime(trip.getBoarding().getDepartureTime()));
        start.addView(startTime);

        TextView startName = new TextView(getApplicationContext());
        startName.setTextSize(TypedValue.COMPLEX_UNIT_SP, stdFontsize);
        startName.setTextColor(Color.BLACK);
        startName.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.8f));
        startName.setText(trip.getBoarding().getName());
        start.addView(startName);
        interchangeLayout.addView(start);

        LinearLayout end = new LinearLayout(getApplicationContext());
        end.setOrientation(LinearLayout.HORIZONTAL);

        TextView endTime = new TextView(getApplicationContext());
        endTime.setTextSize(TypedValue.COMPLEX_UNIT_SP, stdFontsize);
        endTime.setTextColor(Color.BLACK);
        endTime.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.2f));
        endTime.setText(FormatTools.parseTriasTime(trip.getAlighting().getArrivalTime()));
        end.addView(endTime);

        TextView endName = new TextView(getApplicationContext());
        endName.setTextSize(TypedValue.COMPLEX_UNIT_SP, stdFontsize);
        endName.setTextColor(Color.BLACK);
        endName.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.8f));
        endName.setText(trip.getAlighting().getName());
        end.addView(endName);
        interchangeLayout.addView(end);

        return interchangeLayout;
    }

    private LinearLayout getTimedTripLayout(Trip trip) {
        LinearLayout tripLayout = new LinearLayout(getApplicationContext());
        tripLayout.setOrientation(LinearLayout.VERTICAL);

        TextView headerText = new TextView(getApplicationContext());
        headerText.setTextSize(TypedValue.COMPLEX_UNIT_SP, bigFontsize);
        headerText.setTextColor(Color.BLACK);
        Service s = trip.getService();
        headerText.setText(s.getRailName() + " " + s.getLineName() + " -> " + s.getDesitnation());
        tripLayout.addView(headerText);

        tripLayout.addView(getStopPointLayout(trip.getBoarding()));

        tripLayout.addView(getDivider());

        for (StopPoint stop : trip.getIntermediates()) {
            tripLayout.addView(getStopPointLayout(stop));
            tripLayout.addView(getDivider());
        }
        tripLayout.addView(getStopPointLayout(trip.getAlighting()));

        return tripLayout;
    }

    private LinearLayout getStopPointLayout(StopPoint stop) {
        LinearLayout boarding = new LinearLayout(getApplicationContext());
        boarding.setOrientation(LinearLayout.HORIZONTAL);

        String timeValue, delayValue = "";
        boolean late = false;
        if (stop.getDepartureTime() != null) {
            timeValue = FormatTools.parseTriasTime(stop.getDepartureTime());
            if (stop.getDepartureTimeEstimated() != null) {
                long difference = FormatTools.getTriasDifference(stop.getDepartureTime(), stop.getDepartureTimeEstimated()) / 1000 / 60;
                delayValue = "+" + String.valueOf(difference);
                if (difference > 5) {
                    late = true;
                }
            }
        } else {
            timeValue = FormatTools.parseTriasTime(stop.getArrivalTime());
            if (stop.getArrivalTimeEstimated() != null) {
                long difference = FormatTools.getTriasDifference(stop.getArrivalTime(), stop.getArrivalTimeEstimated()) / 1000 / 60;
                delayValue = "+" + String.valueOf(difference);
                if (difference > 5) {
                    late = true;
                }
            }
        }


        TextView time = new TextView(getApplicationContext());
        time.setTextSize(TypedValue.COMPLEX_UNIT_SP, stdFontsize);
        time.setTextColor(Color.BLACK);
        time.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.11f));
        time.setText(timeValue);
        boarding.addView(time);

        TextView delay = new TextView(getApplicationContext());
        delay.setTextSize(TypedValue.COMPLEX_UNIT_SP, stdFontsize);
        delay.setTextColor(Color.rgb(124, 179, 66));
        delay.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.09f));
        delay.setText(delayValue);
        if (late) {
            delay.setTextColor(Color.RED);
        }
        boarding.addView(delay);

        TextView name = new TextView(getApplicationContext());
        name.setTextSize(TypedValue.COMPLEX_UNIT_SP, stdFontsize);
        name.setTextColor(Color.BLACK);
        name.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.8f));
        StringBuilder builder = new StringBuilder(stop.getName());
        if (stop.getBay() != null) {
            builder.append(", ");
            builder.append(stop.getBay());
        }
        name.setText(builder.toString());
        boarding.addView(name);

        return boarding;
    }

    public View getDivider() {
        View v = new View(getApplicationContext());
        v.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                5
        ));
        v.setBackgroundColor(Color.parseColor("#cdcdcd"));

        return v;
    }

    public View getStrongDivider() {
        View v = new View(getApplicationContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 7);
        params.setMargins(0, 24, 0, 24);
        v.setLayoutParams(params);
        v.setBackgroundColor(Color.parseColor("#B3B3B3"));

        return v;
    }
}
