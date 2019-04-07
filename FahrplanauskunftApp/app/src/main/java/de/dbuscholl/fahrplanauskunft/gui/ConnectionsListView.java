package de.dbuscholl.fahrplanauskunft.gui;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

import de.dbuscholl.fahrplanauskunft.FormatTools;
import de.dbuscholl.fahrplanauskunft.network.entities.Connection;
import de.dbuscholl.fahrplanauskunft.network.entities.PrognosisCalculationItem;
import de.dbuscholl.fahrplanauskunft.network.entities.PrognosisCalculationResult;
import de.dbuscholl.fahrplanauskunft.network.entities.Service;
import de.dbuscholl.fahrplanauskunft.network.entities.StopPoint;
import de.dbuscholl.fahrplanauskunft.network.entities.Trip;

public class ConnectionsListView extends LinearLayout {
    private Context context;
    private int stdFontsize = 14;
    private int bigFontsize = 18;
    private ArrayList<PrognosisCalculationResult> prognosis;

    public ConnectionsListView(Context context) {
        super(context);
        this.context = context;
        setOrientation(LinearLayout.VERTICAL);
    }

    public void setPrognosis(ArrayList<PrognosisCalculationResult> prognosis) {
        this.prognosis = prognosis;
    }

    public ConnectionsListView build(Connection connection) {
        ArrayList<Trip> legs = connection.getLegs();
        for (int i = 0; i < legs.size(); i++) {
            Trip t = legs.get(i);
            if (t.getType() == Trip.TripType.TIMED) {
                PrognosisCalculationItem p = null;
                if (prognosis != null) {
                    for (PrognosisCalculationResult r : prognosis) {
                        if (r.getService().getLineName().equals(t.getService().getLineName())) {
                            p = r.getPrognosis();
                        }
                    }
                }

                LinearLayout tripLayout = getTimedTripLayout(t, p);
                addView(tripLayout);
            } else {
                LinearLayout interchangeLayout = getInterchangeTripLayout(t);
                addView(interchangeLayout);
            }
            if (i != legs.size() - 1) {
                addView(getStrongDivider());
            }
        }

        return this;
    }

    private LinearLayout getInterchangeTripLayout(Trip trip) {
        LinearLayout interchangeLayout = new LinearLayout(context);
        interchangeLayout.setOrientation(LinearLayout.VERTICAL);

        TextView header = new TextView(context);
        header.setTextSize(TypedValue.COMPLEX_UNIT_SP, bigFontsize);
        header.setTextColor(Color.BLACK);
        header.setText("Umstieg:");
        interchangeLayout.addView(header);

        LinearLayout start = new LinearLayout(context);
        start.setOrientation(LinearLayout.HORIZONTAL);

        TextView startTime = new TextView(context);
        startTime.setTextSize(TypedValue.COMPLEX_UNIT_SP, stdFontsize);
        startTime.setTextColor(Color.BLACK);
        startTime.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.2f));
        startTime.setText(FormatTools.parseTriasTime(trip.getBoarding().getDepartureTime()));
        start.addView(startTime);

        TextView startName = new TextView(context);
        startName.setTextSize(TypedValue.COMPLEX_UNIT_SP, stdFontsize);
        startName.setTextColor(Color.BLACK);
        startName.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.8f));
        startName.setText(trip.getBoarding().getName());
        start.addView(startName);
        interchangeLayout.addView(start);

        LinearLayout end = new LinearLayout(context);
        end.setOrientation(LinearLayout.HORIZONTAL);

        TextView endTime = new TextView(context);
        endTime.setTextSize(TypedValue.COMPLEX_UNIT_SP, stdFontsize);
        endTime.setTextColor(Color.BLACK);
        endTime.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.2f));
        endTime.setText(FormatTools.parseTriasTime(trip.getAlighting().getArrivalTime()));
        end.addView(endTime);

        TextView endName = new TextView(context);
        endName.setTextSize(TypedValue.COMPLEX_UNIT_SP, stdFontsize);
        endName.setTextColor(Color.BLACK);
        endName.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.8f));
        endName.setText(trip.getAlighting().getName());
        end.addView(endName);
        interchangeLayout.addView(end);

        return interchangeLayout;
    }

    private LinearLayout getTimedTripLayout(Trip trip, PrognosisCalculationItem prognosis) {
        LinearLayout tripLayout = new LinearLayout(context);
        tripLayout.setOrientation(LinearLayout.VERTICAL);

        TextView headerText = new TextView(context);
        headerText.setTextSize(TypedValue.COMPLEX_UNIT_SP, bigFontsize);
        headerText.setTextColor(Color.BLACK);
        Service s = trip.getService();
        headerText.setText(s.getRailName() + " " + s.getLineName() + " -> " + s.getDesitnation());
        tripLayout.addView(headerText);

        if (prognosis != null) {
            String boarding = String.valueOf(prognosis.getDelayBoardingRegular() / 60);
            TextView boardingPrognosis = getPrognosis(boarding + " Minuten erwartete Verspätung bei der Abfahrt");
            tripLayout.addView(boardingPrognosis);
        }

        tripLayout.addView(getStopPointLayout(trip.getBoarding()));

        tripLayout.addView(getDivider());

        for (StopPoint stop : trip.getIntermediates()) {
            tripLayout.addView(getStopPointLayout(stop));
            tripLayout.addView(getDivider());
        }
        tripLayout.addView(getStopPointLayout(trip.getAlighting()));

        if (prognosis != null) {
            String alighting = String.valueOf(prognosis.getDelayAlightingRegular() / 60);
            String exception = String.valueOf(prognosis.getDelayException() / 60);
            String propability = String.format(Locale.GERMANY, "%.2f", prognosis.getExceptionPropability());

            TextView alightingPrognosis = getPrognosis(alighting + " Minuten erwartete Verspätung bei der Ankunft");
            TextView exceptionPrognosis = getPrognosis("Zu " + propability + "% tritt eine Verspätung von maximal " + exception + " Minuten auf!");

            tripLayout.addView(alightingPrognosis);
            tripLayout.addView(exceptionPrognosis);
        }

        return tripLayout;
    }

    private LinearLayout getStopPointLayout(StopPoint stop) {
        LinearLayout boarding = new LinearLayout(context);
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


        TextView time = new TextView(context);
        time.setTextSize(TypedValue.COMPLEX_UNIT_SP, stdFontsize);
        time.setTextColor(Color.BLACK);
        time.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.11f));
        time.setText(timeValue);
        boarding.addView(time);

        TextView delay = new TextView(context);
        delay.setTextSize(TypedValue.COMPLEX_UNIT_SP, stdFontsize);
        delay.setTextColor(Color.rgb(124, 179, 66));
        delay.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.09f));
        delay.setText(delayValue);
        if (late) {
            delay.setTextColor(Color.RED);
        }
        boarding.addView(delay);

        TextView name = new TextView(context);
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
        View v = new View(context);
        v.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                5
        ));
        v.setBackgroundColor(Color.parseColor("#cdcdcd"));

        return v;
    }

    public View getStrongDivider() {
        View v = new View(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 7);
        params.setMargins(0, 24, 0, 24);
        v.setLayoutParams(params);
        v.setBackgroundColor(Color.parseColor("#B3B3B3"));

        return v;
    }

    public TextView getPrognosis(String text) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 12, 0, 12);

        TextView textView = new TextView(context);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, stdFontsize);
        textView.setTextColor(Color.BLACK);
        textView.setLayoutParams(params);
        textView.setText(text);

        return textView;
    }
}
