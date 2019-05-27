package de.dbuscholl.fahrplanauskunft.gui.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import de.dbuscholl.fahrplanauskunft.FormatTools;
import de.dbuscholl.fahrplanauskunft.R;
import de.dbuscholl.fahrplanauskunft.common.Constants;
import de.dbuscholl.fahrplanauskunft.network.entities.Connection;
import de.dbuscholl.fahrplanauskunft.network.entities.Trip;

/**
 * Visualizes a list of connections which is returned by the Verbindungsauskunft-service by TRIAS. It contains a list of
 * possible ways to travel from A to B via transit vehicles.
 */
public class ConnectionsListAdapter extends ArrayAdapter<Connection> {

    private ArrayList<Connection> data;
    private Context context;
    private int lastPosition = -1;

    /**
     * Constructor...
     * @param data data which should be visualized
     * @param context application context
     */
    public ConnectionsListAdapter(ArrayList<Connection> data, Context context) {
        super(context, R.layout.listitem_connection, data);
        this.context = context;
        this.data = data;
    }

    /**
     *
     * @return number of connections which are visualized in total
     */
    @Override
    public int getCount() {
        return data.size();
    }

    /**
     * visualizes a connection from the data
     * @param position position in the connections array
     * @param convertView for caching
     * @param parent the parent in which the view is inserted
     * @return a view containing the full visualization of the connection
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(position > data.size()) {
            return null;
        }
        // Get the data item for this position
        Connection c = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag

        final View result;

        if (convertView == null) {

            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.listitem_connection, parent, false);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        // get items
        viewHolder.startTime = convertView.findViewById(R.id.resultStartTime);
        viewHolder.startTimeDelay = convertView.findViewById(R.id.resultStartTimeDelay);
        viewHolder.endTime = convertView.findViewById(R.id.resultEndTime);
        viewHolder.endTimeDelay = convertView.findViewById(R.id.resultEndTimeDelay);
        viewHolder.services = convertView.findViewById(R.id.services);
        viewHolder.duration = convertView.findViewById(R.id.duration);

        lastPosition = position;
        String departureTime = c.getLegs().get(0).getBoarding().getDepartureTime();
        String arrivalTime = c.getLegs().get(c.getLegs().size() - 1).getAlighting().getArrivalTime();
        String startTime = FormatTools.parseTriasTime(departureTime);
        String endTime = FormatTools.parseTriasTime(arrivalTime);

        // insert realtime for boarding
        viewHolder.startTimeDelay.setText("");
        if (c.getLegs().get(0).getType() == Trip.TripType.TIMED) {
            String startTimeEstimated = c.getLegs().get(0).getBoarding().getDepartureTimeEstimated();
            if (startTimeEstimated != null) {
                long diff = getDelayForTextView(departureTime, startTimeEstimated);
                if (diff > 5) {
                    viewHolder.startTimeDelay.setTextColor(Constants.COLOR_DELAY_LATE);
                }
                viewHolder.startTimeDelay.setText("+" + String.valueOf(diff));
            }
        }

        // insert realtime for alighting
        viewHolder.endTimeDelay.setText("");
        if (c.getLegs().get(c.getLegs().size() - 1).getType() == Trip.TripType.TIMED) {
            String endTimeEstimated = c.getLegs().get(c.getLegs().size() - 1).getAlighting().getArrivalTimeEstimated();
            if (endTimeEstimated != null) {
                long diff = getDelayForTextView(arrivalTime, endTimeEstimated);
                if (diff > 5) {
                    viewHolder.endTimeDelay.setTextColor(Constants.COLOR_DELAY_LATE);
                }
                viewHolder.endTimeDelay.setText("+" + String.valueOf(diff));
            }
        }


        //setValues for caching
        viewHolder.startTime.setText(startTime);
        viewHolder.endTime.setText(endTime);
        viewHolder.duration.setText(getDurationForTextiView(c.getStartTime(), c.getEndTime()));
        viewHolder.services.setText(getLineString(c.getLegs()));


        // Return the completed view to render on screen
        return convertView;
    }

    /**
     * generates the "linestring" which is a concatenation of all legs services containing vehicle type and linenumber
     * @param legs
     * @return
     */
    private String getLineString(ArrayList<Trip> legs) {
        StringBuilder s = new StringBuilder();

        for (int i = 0; i < legs.size(); i++) {
            Trip t = legs.get(i);
            if (!(t.getType() == Trip.TripType.TIMED)) {
                continue;
            }
            String railName = t.getService().getRailName();
            if (railName != null) {
                s.append(railName).append(" ");
            }
            String lineName = t.getService().getLineName();
            if (lineName != null) {
                s.append(lineName).append(" ");
            }
            if (i != legs.size() - 1) {
                s.append(" - ");
            }
        }

        return s.toString();
    }

    /**
     * calculates the delay for the text view. TRIAS doenst return a delay value, but it returns a realtime time
     * when the vehicle will arrive at boarding, so we need to calculate difference between planned departure and estimated
     * departure. This is done by FormatTools class
     * @param timetabled timetabled time where it is planned to arrive
     * @param estimated the time where TRIAS estimates it to come because of the realtime gps data
     * @return difference in minutes between timetabled time and estimated time
     */
    private long getDelayForTextView(String timetabled, String estimated) {
        long difference = FormatTools.getTriasDifference(timetabled, estimated) / 1000 / 60;
        return difference;
    }

    /**
     * calculates the trip duration for a connection by subtracting start time from end time. Makes the result readable
     * by splitting into hours and minutes and concatenating into a String with format "HH:mm"
     * @param startTime time where the boarding takes place
     * @param endTime time where the alighting takes place
     * @return readable duration string formatted to "HH:mm" e.g "1:32"
     */
    private String getDurationForTextiView(String startTime, String endTime) {
        long difference = FormatTools.getTriasDifference(startTime, endTime) / 1000 / 60;
        long minute = difference % 60;
        long hour = difference / 60;
        StringBuilder s = new StringBuilder();
        s.append(hour).append(":").append(minute);
        return s.toString();
    }

    /**
     * View lockup cache to not read everything again all the time.
     */
    private static class ViewHolder {
        TextView startTime;
        TextView startTimeDelay;
        TextView endTime;
        TextView endTimeDelay;
        TextView services;
        TextView duration;
    }
}
