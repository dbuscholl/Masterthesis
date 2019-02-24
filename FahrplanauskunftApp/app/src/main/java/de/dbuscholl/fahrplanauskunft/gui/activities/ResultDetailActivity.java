package de.dbuscholl.fahrplanauskunft.gui.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import de.dbuscholl.fahrplanauskunft.FormatTools;
import de.dbuscholl.fahrplanauskunft.R;
import de.dbuscholl.fahrplanauskunft.gui.fragments.ConnectionsFragment;
import de.dbuscholl.fahrplanauskunft.network.entities.Connection;
import de.dbuscholl.fahrplanauskunft.network.entities.Service;
import de.dbuscholl.fahrplanauskunft.network.entities.StopPoint;
import de.dbuscholl.fahrplanauskunft.network.entities.Trip;

public class ResultDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.result_activity);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        int position = getIntent().getIntExtra("position", -1);
        if (position == -1) {
            onBackPressed();
        }

        Connection connection = ConnectionsFragment.getCurrentResult().get(position);

        TextView startStation = findViewById(R.id.result_tripstart_text);
        TextView endStation = findViewById(R.id.result_tripend_text);
        TextView dateTextView = findViewById(R.id.result_tripdate);

        startStation.setText(connection.getLegs().get(0).getBoarding().getName());
        endStation.setText(connection.getLegs().get(connection.getLegs().size() - 1).getAlighting().getName());
        dateTextView.setText(FormatTools.parseTriasDate(connection.getStartTime()));

        LinearLayout wrapper = new LinearLayout(getApplicationContext());
        wrapper.setOrientation(LinearLayout.VERTICAL);

        for (Trip t : connection.getLegs()) {
            if (t.getType() == Trip.TripType.TIMED) {
                LinearLayout tripLayout = getTimedTripLayout(t);
                wrapper.addView(tripLayout);
            } else {
                LinearLayout interchangeLayout = getInterchangeTripLayout(t);
                wrapper.addView(interchangeLayout);
            }
        }

        LinearLayout layout = findViewById(R.id.result_content);
        layout.addView(wrapper);
    }

    private LinearLayout getInterchangeTripLayout(Trip trip) {
        LinearLayout interchangeLayout = new LinearLayout(getApplicationContext());
        interchangeLayout.setOrientation(LinearLayout.VERTICAL);

        TextView header = new TextView(getApplicationContext());
        header.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        header.setTextColor(Color.BLACK);
        header.setText("Umstieg:");
        interchangeLayout.addView(header);

        LinearLayout start = new LinearLayout(getApplicationContext());
        start.setOrientation(LinearLayout.HORIZONTAL);

        TextView startTime = new TextView(getApplicationContext());
        startTime.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        startTime.setTextColor(Color.BLACK);
        startTime.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.2f));
        startTime.setText(FormatTools.parseTriasTime(trip.getBoarding().getDepartureTime()));
        start.addView(startTime);

        TextView startName = new TextView(getApplicationContext());
        startName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        startName.setTextColor(Color.BLACK);
        startName.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.8f));
        startName.setText(trip.getBoarding().getName());
        start.addView(startName);
        interchangeLayout.addView(start);

        LinearLayout end = new LinearLayout(getApplicationContext());
        end.setOrientation(LinearLayout.HORIZONTAL);

        TextView endTime = new TextView(getApplicationContext());
        endTime.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        endTime.setTextColor(Color.BLACK);
        endTime.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.2f));
        endTime.setText(FormatTools.parseTriasTime(trip.getAlighting().getArrivalTime()));
        end.addView(endTime);

        TextView endName = new TextView(getApplicationContext());
        endName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
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
        headerText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        headerText.setTextColor(Color.BLACK);
        Service s = trip.getService();
        headerText.setText(s.getRailName() + " " + s.getLineName() + " -> " + s.getDesitnation());
        tripLayout.addView(headerText);

        tripLayout.addView(getStopPointLayout(trip.getBoarding()));

        ImageView divider = new ImageView(this);
        LinearLayout.LayoutParams lp =
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(1, 1, 1, 1);
        divider.setLayoutParams(lp);
        divider.setBackgroundColor(Color.BLACK);
        tripLayout.addView(divider);

        for (StopPoint stop : trip.getIntermediates()) {
            tripLayout.addView(getStopPointLayout(stop));
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
        time.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        time.setTextColor(Color.BLACK);
        time.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.2f));
        time.setText(timeValue);
        boarding.addView(time);

        TextView delay = new TextView(getApplicationContext());
        delay.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        delay.setTextColor(Color.GREEN);
        delay.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.1f));
        delay.setText(delayValue);
        if (late) {
            delay.setTextColor(Color.RED);
        }
        boarding.addView(delay);

        TextView name = new TextView(getApplicationContext());
        name.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        name.setTextColor(Color.BLACK);
        name.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.7f));
        StringBuilder builder = new StringBuilder(stop.getName());
        if (stop.getBay() != null) {
            builder.append(stop.getBay());
        }
        name.setText(builder.toString());
        boarding.addView(name);

        return boarding;
    }
}
