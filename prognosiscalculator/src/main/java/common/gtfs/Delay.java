package common.gtfs;

import common.network.StopPoint;
import common.network.Trip;
import database.GTFS;

import java.sql.SQLException;
import java.util.ArrayList;

/**
 * This class represents a delay. As the delays are stored next to the GTFS-strcuture it references to them. A delay
 * contains a reference to the tripID for which it was recorded just as a value in seconds and some more stuff. This
 * class also contains a lot of static filter functions with which a lot of delays can be found for different use cases.
 */
public class Delay {
    // define the maximum allowed delay. All Delays which are bigger will be ignored
    private static final int MAX_DELAY_HOURS = 4;

    private long delayId;
    private String tripId;
    private int delay;
    private String timestamp;
    private int stop_sequence;

    /**
     * empty constructor
     */
    public Delay() {}

    /**
     * parameterized constructor
     * @param delayId the id of the delay which can be obtained from the database
     * @param tripId the id of the trip for which the delay was recorded
     * @param delay the amount of delay in seconds
     * @param timestamp a timestamp indicating when the delay was measured
     * @param stop_sequence the index of the stop of a trip for which it was measured
     */
    public Delay(long delayId, String tripId, int delay, String timestamp, int stop_sequence) {
        this.delayId = delayId;
        this.tripId = tripId;
        this.delay = delay;
        this.timestamp = timestamp;
        this.stop_sequence = stop_sequence;
    }

    /**
     * getter
     * @return the id of the delay which can be obtained from the database
     */
    public long getDelayId() {
        return delayId;
    }

    /**
     * setter
     * @param delayId the id of the delay which can be obtained from the database
     */
    public void setDelayId(long delayId) {
        this.delayId = delayId;
    }

    /**
     * getter
     * @return the id of the trip for which the delay was recorded
     */
    public String getTripId() {
        return tripId;
    }

    /**
     * setter
     * @param tripId the id of the trip for which the delay was recorded
     */
    public void setTripId(String tripId) {
        this.tripId = tripId;
    }

    /**
     * getter
     * @return the amount of delay in seconds
     */
    public int getDelay() {
        return delay;
    }

    /**
     * setter
     * @param delay the amount of delay in seconds
     */
    public void setDelay(int delay) {
        this.delay = delay;
    }

    /**
     * getter
     * @return a timestamp indicating when the delay was measured
     */
    public String getTimestamp() {
        return timestamp;
    }

    /**
     * setter
     * @param timestamp a timestamp indicating when the delay was measured
     */
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * getter
     * @return the index of the stop of a trip for which it was measured
     */
    public int getStop_sequence() {
        return stop_sequence;
    }

    /**
     * setter
     * @param stop_sequence the index of the stop of a trip for which it was measured
     */
    public void setStop_sequence(int stop_sequence) {
        this.stop_sequence = stop_sequence;
    }

    /**
     * from a list of objects of this class this function extracts all delays which were recorded at a specific date
     * @param date the date of which delays should be filtered
     * @param stack the list of delays from which the ones with the given delay should be extracted from
     * @return a new list of delays but only containing timestamp at the given date
     */
    public static ArrayList<Delay> getDelaysAtDate(String date, ArrayList<Delay> stack) {
        ArrayList<Delay> filteredDelays = new ArrayList<>();

        for(Delay d : stack) {
            if(d.getTimestamp().contains(date)) {
                filteredDelays.add(d);
            }
        }

        return filteredDelays;
    }

    /**
     * returns a list containing all date values from a list of delays. The date values are extracted from the timestamp
     * @param stack the list of delays from which the dates should be extracted from
     * @return a new list of strings containing all date values of the delays .Not including duplicates!
     */
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

    /**
     * returns a list containning all delay values from a list of delay objects.
     * @param stack the list of delays from which the values should be extracted from
     * @return a new list of integers containing all delay values in seconds.
     */
    public static ArrayList<Integer> getDelayValues(ArrayList<Delay> stack) {
        ArrayList<Integer> values = new ArrayList<>();

        for(Delay d : stack) {
            values.add(d.getDelay());
        }

        return values;
    }

    /**
     * From a list of delay objects this function returns the delay which is closest to a given stop sequence.
     * @param stopSequence the stop sequence of which a delay should be obtained from.
     * @param stack list of delays from which the closest to the given stop sequence delay should be found from
     * @return the delay object that is the closest to the given stop sequence
     */
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

    /**
     * returns a new list of delay objects containing delays which are greater than a given value in seconds.
     * @param threshhold
     * @param stack
     * @return
     */
    public static ArrayList<Delay> getDelaysGreaterThan(int threshhold, ArrayList<Delay> stack) {
        ArrayList<Delay> filteredDelays = new ArrayList<>();

        for(Delay d : stack) {
            if(d.getDelay() >= threshhold && d.getDelay() < MAX_DELAY_HOURS * 3600) {
                filteredDelays.add(d);
            }
        }

        return filteredDelays;
    }

    /**
     * this function determines all delays for a stopsequence for every day. So one day will return one value. This is
     * the principle functionality for the TRIAS_EVERYDAY prognosis factor.
     * @param t the trip from which the stop comes from
     * @param s the actual stoppoint for which the delays should be obtained
     * @param stack the stack of delays which should be used to get the values from
     * @return a new list of filtered delays containing the delays for the stoppoint for every different date of the list
     * @throws SQLException when something goes wrong with the database...
     */
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
