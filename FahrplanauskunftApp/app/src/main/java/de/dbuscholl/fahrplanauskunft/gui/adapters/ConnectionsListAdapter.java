package de.dbuscholl.fahrplanauskunft.gui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import de.dbuscholl.fahrplanauskunft.FormatTools;
import de.dbuscholl.fahrplanauskunft.R;
import de.dbuscholl.fahrplanauskunft.network.entities.Connection;
import de.dbuscholl.fahrplanauskunft.network.entities.Trip;

public class ConnectionsListAdapter extends ArrayAdapter<Connection> {

    private ArrayList<Connection> data;
    private Context context;
    private int lastPosition = -1;

    public ConnectionsListAdapter(ArrayList<Connection> data, Context context) {
        super(context, R.layout.connections_listitem, data);
        this.context = context;
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Connection c = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag

        final View result;

        if (convertView == null) {

            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.connections_listitem, parent, false);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        // get items
        viewHolder.startTime = (TextView) convertView.findViewById(R.id.resultStartTime);
        viewHolder.startTimeDelay = (TextView) convertView.findViewById(R.id.resultStartTimeDelay);
        viewHolder.endTime = (TextView) convertView.findViewById(R.id.resultEndTime);
        viewHolder.endTimeDelay = (TextView) convertView.findViewById(R.id.resultEndTimeDelay);
        viewHolder.services = (TextView) convertView.findViewById(R.id.services);
        viewHolder.duration = (TextView) convertView.findViewById(R.id.duration);

        lastPosition = position;
        String startTime = FormatTools.parseTriasTime(c.getStartTime());
        String endTime = FormatTools.parseTriasTime(c.getEndTime());

        viewHolder.startTimeDelay.setText("");
        if (c.getLegs().get(0).getType() == Trip.TripType.TIMED) {
            String startTimeEstimated = c.getLegs().get(0).getBoarding().getDepartureTimeEstimated();
            if (startTimeEstimated != null) {
                viewHolder.startTimeDelay.setText(getDelayForTextView(c.getStartTime(), startTimeEstimated));
            }
        }

        viewHolder.endTimeDelay.setText("");
        if (c.getLegs().get(c.getLegs().size() - 1).getType() == Trip.TripType.TIMED) {
            String endTimeEstimated = c.getLegs().get(c.getLegs().size() - 1).getAlighting().getArrivalTimeEstimated();
            if (endTimeEstimated != null) {
                viewHolder.startTimeDelay.setText(getDelayForTextView(c.getEndTime(), endTimeEstimated));
            }
        }


        //setValues
        viewHolder.startTime.setText(startTime);
        viewHolder.endTime.setText(endTime);
        viewHolder.duration.setText(getDurationForTextiView(c.getStartTime(), c.getEndTime()));
        viewHolder.services.setText(getLineString(c.getLegs()));


        // Return the completed view to render on screen
        return convertView;
    }

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

    private String getDelayForTextView(String timetabled, String estimated) {
        long difference = FormatTools.getTriasDifference(timetabled, estimated) / 1000 / 60;
        StringBuilder s = new StringBuilder();
        s.append("+").append(difference);
        return s.toString();
    }

    private String getDurationForTextiView(String startTime, String endTime) {
        long difference = FormatTools.getTriasDifference(startTime, endTime) / 1000 / 60;
        long minute = difference % 60;
        long hour = difference / 60;
        StringBuilder s = new StringBuilder();
        s.append(hour).append(":").append(minute);
        return s.toString();
    }

    // View lookup cache
    private static class ViewHolder {
        TextView startTime;
        TextView startTimeDelay;
        TextView endTime;
        TextView endTimeDelay;
        TextView services;
        TextView duration;
    }
}
