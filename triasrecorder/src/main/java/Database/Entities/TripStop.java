package Database.Entities;

import org.apache.commons.text.similarity.JaroWinklerDistance;
import org.apache.log4j.Logger;

import java.util.ArrayList;

/**
 * This class is the structural represantation for a Stop on a trip. It's essential for the whole program. Check the GTFS
 * Specification for more details on it's attributes or look at the constructor or the getters.
 */
public class TripStop {
    private Logger log = Logger.getLogger(this.getClass().getName());
    private String arrival_time;
    private String arrival_time_estimated;
    private String departure_time;
    private String departure_time_estimated;
    private String stop_id;
    private String stop_name;
    private int stop_sequence;
    private int pickup_type;
    private int drop_off_type;
    private Type type;

    public TripStop() {
    }

    /**
     *
     * @param arrival_time specifies the arrival time at a specific stop for a specific trip on a route
     * @param departure_time specifies the departure time from a specific stop for a specific trip on a route
     * @param stop_id ID that uniquely identifies a stop from which the service departs
     * @param stop_name contains the name of a stop, station, or station entrance, a name that people understand in the local and tourist vernacular
     * @param stop_sequence identifies the order of the stops for a particular trip. The values for stop_sequence are
     *                      non-negative integers, and they increase along the trip.
     * @param pickup_type indicates whether passengers are picked up at a stop as part of the normal schedule or whether
     *                    a pickup at the stop is not available
     * @param drop_off_type indicates whether passengers are dropped off at a stop as part of the normal schedule or whether a drop off at the stop is not available
     * @param type wheter this stop is from the TRIAS interface or the GTFS Database
     */
    public TripStop(String arrival_time, String departure_time, String stop_id, String stop_name, int stop_sequence, int pickup_type, int drop_off_type, Type type) {
        this.arrival_time = arrival_time;
        this.departure_time = departure_time;
        this.stop_id = stop_id;
        this.stop_name = stop_name;
        this.stop_sequence = stop_sequence;
        this.pickup_type = pickup_type;
        this.drop_off_type = drop_off_type;
        this.type = type;
    }

    /**
     * Creates a TripStop by copying another one
     * @param gtfsStop the Stop to be copied
     */
    public TripStop(TripStop gtfsStop) {
        this.arrival_time = gtfsStop.arrival_time;
        this.arrival_time_estimated = gtfsStop.arrival_time_estimated;
        this.departure_time = gtfsStop.departure_time;
        this.departure_time_estimated = gtfsStop.departure_time_estimated;
        this.stop_id = gtfsStop.stop_id;
        this.stop_name = gtfsStop.stop_name;
        this.stop_sequence = gtfsStop.stop_sequence;
        this.pickup_type = gtfsStop.pickup_type;
        this.drop_off_type = gtfsStop.drop_off_type;
        this.type = gtfsStop.type;
    }

    /**
     * checks if two lists of tripstops (usually one by GTFS and one by TRIAS) are equal, but this can also be used for the same type
     * @param gtfsStops first list, provided by GTFS
     * @param triasStops seconds list, provided by TRIAS
     * @return true wheter they are at least 85% equal or false if not. The tolerance is for differences on same stops between
     * TRIAS and GTFS because some stops might not be listed in GTFS but in TRIAS or opposite
     */
    public static boolean checkEquality(ArrayList<TripStop> gtfsStops, ArrayList<TripStop> triasStops) {
        int equalStops = 0;
        int maxValue = gtfsStops.size() > triasStops.size() ? triasStops.size() : gtfsStops.size(); // get the smaller value as maximum for the loop
        for (int i = 0; i < maxValue; i++) {
            TripStop gs = gtfsStops.get(i);
            TripStop ts = triasStops.get(i);
            if (gs.equals(ts)) {
                equalStops++;
            }
        }
        return (double) equalStops / gtfsStops.size() > 0.85; // a little tolerance because sometimes stop sequence differs but it's still the same trip;
        //(e.g. TRIAS: Feuerbach Bahnhof | GTFS: Feuerbach Bf) or different stop_sequences but right ordered
    }

    /**
     *
     * @return specifies the arrival time at a specific stop for a specific trip on a route
     */
    public String getArrival_time() {
        return arrival_time;
    }

    /**
     *
     * @param arrival_time the arrival time at a specific stop for a specific trip on a route
     */
    public void setArrival_time(String arrival_time) {
        this.arrival_time = arrival_time;
    }

    /**
     *
     * @return the departure time from a specific stop for a specific trip on a route
     */
    public String getDeparture_time() {
        return departure_time;
    }

    /**
     *
     * @param departure_time the departure time from a specific stop for a specific trip on a route
     */
    public void setDeparture_time(String departure_time) {
        this.departure_time = departure_time;
    }

    /**
     *
     * @return ID that uniquely identifies a stop from which the service departs
     */
    public String getStop_id() {
        return stop_id;
    }

    /**
     *
     * @param stop_id ID that uniquely identifies a stop from which the service departs
     */
    public void setStop_id(String stop_id) {
        this.stop_id = stop_id;
    }

    /**
     *
     * @return contains the name of a stop, station, or station entrance, a name that people understand in the local and tourist vernacular
     */
    public String getStop_name() {
        return stop_name;
    }

    /**
     *
     * @param stop_name contains the name of a stop, station, or station entrance, a name that people understand in the local and tourist vernacular
     */
    public void setStop_name(String stop_name) {
        this.stop_name = stop_name;
    }

    /**
     *
     * @return the order of the stops for a particular trip. The values for stop_sequence are
     * non-negative integers, and they increase along the trip
     */
    public int getStop_sequence() {
        return stop_sequence;
    }

    /**
     *
     * @param stop_sequence identifies the order of the stops for a particular trip. The values for stop_sequence are
     *                      non-negative integers, and they increase along the trip.
     */
    public void setStop_sequence(int stop_sequence) {
        this.stop_sequence = stop_sequence;
    }

    /**
     *
     * @return 0 - Regularly scheduled pickup <br> 1 - No pickup available <br> 2 - Must phone agency to arrange pickup <br>
     *     3 - Must coordinate with driver to arrange pickup
     */
    public int getPickup_type() {
        return pickup_type;
    }

    /**
     *
     * @param pickup_type 0 - Regularly scheduled pickup <br> 1 - No pickup available <br> 2 - Must phone agency to arrange pickup <br>
     *                    3 - Must coordinate with driver to arrange pickup
     */
    public void setPickup_type(int pickup_type) {
        this.pickup_type = pickup_type;
    }

    /**
     *
     * @return 0 - Regularly scheduled drop off <br> 1 - No drop off available <br> 2 - Must phone agency to arrange drop off
     * <br> 3 - Must coordinate with driver to arrange drop off
     */
    public int getDrop_off_type() {
        return drop_off_type;
    }

    /**
     *
     * @param drop_off_type 0 - Regularly scheduled drop off <br> 1 - No drop off available <br> 2 - Must phone agency
     *                      to arrange drop off <br> 3 - Must coordinate with driver to arrange drop off
     */
    public void setDrop_off_type(int drop_off_type) {
        this.drop_off_type = drop_off_type;
    }

    /**
     *
     * @return TRIAS for data based on TRIAS interface or GTFS for data based on the local database
     */
    public Type getType() {
        return type;
    }

    /**
     *
     * @param type TRIAS for data based on TRIAS interface or GTFS for data based on the local database
     */
    public void setType(Type type) {
        this.type = type;
    }

    /**
     *
     * @return specifies the <b>estimated</b> arrival time at a specific stop for a specific trip on a route
     */
    public String getArrival_time_estimated() {
        return arrival_time_estimated;
    }

    /**
     *
     * @param arrival_time_estimated specifies the <b>estimated</b> arrival time at a specific stop for a specific trip on a route
     */
    public void setArrival_time_estimated(String arrival_time_estimated) {
        this.arrival_time_estimated = arrival_time_estimated;
    }

    /**
     *
     * @return specifies the <b>estimated</b> departure time at a specific stop for a specific trip on a route
     */
    public String getDeparture_time_estimated() {
        return departure_time_estimated;
    }

    /**
     *
     * @param departure_time_estimated specifies the <b>estimated</b> departure time at a specific stop for a specific trip on a route
     */
    public void setDeparture_time_estimated(String departure_time_estimated) {
        this.departure_time_estimated = departure_time_estimated;
    }

    /**
     * <p>Checks wheter this TripStop is equal to another one. This is firstly determined by checking the equality of either
     * arrival time or departure time and the equality of the stop.</p>
     * <p>This can be either same stopID or at leas 70% of it's, because there are some differences between TRIAS and GTFS
     * (e.g. TRIAS: Feuerbach Bahnhof | GTFS: Feuerbach Bf). However, if the stop sequence is not the same it fails.</p>
     * @param other the other TripStop to be compared with
     * @return true wheter stops are equal or false if not
     */
    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        if (other == this) return true;
        if (!(other instanceof TripStop)) return false;
        TripStop otherTripStop = (TripStop) other;
        JaroWinklerDistance jwd = new JaroWinklerDistance();

        try {
            boolean sameStopId = stop_id.equals(otherTripStop.stop_id);
            boolean sameName = jwd.apply(getStop_name(), otherTripStop.getStop_name()) > 0.7; // 70% of String is same as the other one (Waiblingen Bf. vs Waiblingen Bahnhof)
            boolean sameStopPosition = stop_sequence == otherTripStop.stop_sequence;

            boolean sameStop = (sameStopId || sameName) && sameStopPosition;

            boolean sameArrivalTime = arrival_time.equals(otherTripStop.arrival_time);
            boolean sameDepartureTime = departure_time.equals(otherTripStop.departure_time);

            boolean sameTime = sameArrivalTime || sameDepartureTime;

            return sameStop && sameTime;
        } catch (NullPointerException e) {
            log.warn("Could not check stop equality. Some parameters are missing.");
            return false;
        } catch (Exception e) {
            log.warn("We got an exception over there", e);
            return false;
        }
    }

    @Override
    public String toString() {
        StringBuffer s = new StringBuffer();
        s.append(stop_id + ": " + stop_name + " | ");
        s.append( "TT: ->" + arrival_time + ", " + departure_time + " -> | ");
        s.append( "ET: ->" + arrival_time_estimated + ", " + departure_time_estimated + " -> | ");
        s.append(type);
        return s.toString();
    }

    /**
     * Is this stop from GTFS Database or from TRIAS interface?
     */
    public enum Type {
        GTFS, TRIAS
    }
}
