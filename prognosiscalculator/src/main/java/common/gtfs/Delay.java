package common.gtfs;

import common.network.StopPoint;
import common.network.Trip;
import database.GTFS;

import java.sql.SQLException;
import java.util.ArrayList;

public class Delay {
    // define the maximum allowed delay. All Delays which are bigger will be ignored
    private static final int MAX_DELAY_HOURS = 4;

    private long delayId;
    private String tripId;
    private int delay;
    private String timestamp;
    private int stop_sequence;

    public Delay() {}

    public Delay(long delayId, String tripId, int delay, String timestamp, int stop_sequence) {
        this.delayId = delayId;
        this.tripId = tripId;
        this.delay = delay;
        this.timestamp = timestamp;
        this.stop_sequence = stop_sequence;
    }

    public long getDelayId() {
        return delayId;
    }

    public void setDelayId(long delayId) {
        this.delayId = delayId;
    }

    public String getTripId() {
        return tripId;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
    }

    public int getDelay() {
        return delay;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public int getStop_sequence() {
        return stop_sequence;
    }

    public void setStop_sequence(int stop_sequence) {
        this.stop_sequence = stop_sequence;
    }

    public static ArrayList<Delay> getDelaysAtDate(String date, ArrayList<Delay> stack) {
        ArrayList<Delay> filteredDelays = new ArrayList<>();

        for(Delay d : stack) {
            if(d.getTimestamp().contains(date)) {
                filteredDelays.add(d);
            }
        }

        return filteredDelays;
    }

    public static ArrayList<String> getDateValues(ArrayList<Delay> stack) {
        ArrayList<String> values = new ArrayList<>();

        for(Delay d : stack) {
            String date = d.getTimestamp().split("\\s+")[0];
            if(! values.contains(date)) {
                values.add(date);
            }
        }

        return values;
    }


    public static ArrayList<Integer> getDelayValues(ArrayList<Delay> stack) {
        ArrayList<Integer> values = new ArrayList<>();

        for(Delay d : stack) {
            values.add(d.getDelay());
        }

        return values;
    }

    public static Delay getDelayForStopSequence(int stopSequence, ArrayList<Delay> stack) {
        if(stack.isEmpty()) {
            return null;
        }

        int distance = Math.abs(stack.get(0).getStop_sequence() - stopSequence);
        int index = 0;
        for(int i = 1; i < stack.size(); i++){
            int cdistance = Math.abs(stack.get(i).getStop_sequence() - stopSequence);
            if(cdistance < distance){
                index = i;
                distance = cdistance;
            }
        }

        return stack.get(index);
    }


    public static ArrayList<Delay> getDelaysGreaterThan(int threshhold, ArrayList<Delay> stack) {
        ArrayList<Delay> filteredDelays = new ArrayList<>();

        for(Delay d : stack) {
            if(d.getDelay() >= threshhold && d.getDelay() < MAX_DELAY_HOURS * 3600) {
                filteredDelays.add(d);
            }
        }

        return filteredDelays;
    }

    public static ArrayList<Delay> getDelaysForStopPoint(Trip t, StopPoint s, ArrayList<Delay> stack) throws SQLException {
        ArrayList<Delay> filteredDelays = new ArrayList<>();

        if(! s.isMemberOf(t)) {
            return filteredDelays;
        }

        // get stop sequence from GTFS Trip for Delay values
        ArrayList<TripStop> fullTrip = GTFS.getFullTrip(t.getGTFSTripId());
        int stopSequence = s.getStopSequenceInGTFSTrip(fullTrip);

        // for every date of the delays list get the delay of the stop sequence
        ArrayList<String> dateValues = Delay.getDateValues(stack);
        for (String value : dateValues) {
            ArrayList<Delay> delaysAtDate = Delay.getDelaysAtDate(value, stack);
            if (delaysAtDate != null && !delaysAtDate.isEmpty()) {
                filteredDelays.add(Delay.getDelayForStopSequence(stopSequence, delaysAtDate));
            }
        }

        return filteredDelays;
    }
}
