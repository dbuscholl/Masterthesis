package common.gtfs;

/**
 * Contains a lot of information on a trip which is scheduled
 */
public class ScheduledTrip {
    private String route_id;
    private String trip_id;
    private String service_id;
    private String stop_id;
    private String stop_name;
    private String route_short_name;
    private String trip_headsign;
    private String arrival_time;
    private String departure_time;

    /**
     * empty contructor
     */
    public ScheduledTrip() {
    }


    /**
     *
     * @param route_id ID that uniquely identifies a route
     * @param trip_id ID that identifies a trip
     * @param service_id ID that uniquely identifies a set of dates when service is available for one or more routes.
     *                   This value is referenced from the OccurringService class.
     * @param stop_id ID that uniquely identifies a stop from which the service departs
     * @param stop_name contains the name of a stop, station, or station entrance, a name that people understand in the local and tourist vernacular.
     * @param route_short_name contains the short name of a route. This will often be a short, abstract identifier like
     *                         "32", "100X", or "Green" that riders use to identify a route, but which doesn't give any
     *                         indication of what places the route serves
     * @param trip_headsign contains the text that appears on a sign that identifies the trip's destination to passengers
     * @param arrival_time specifies the arrival time at a specific stop for a specific trip on a route
     * @param departure_time specifies the departure time from a specific stop for a specific trip on a route
     */
    public ScheduledTrip(String route_id, String trip_id, String service_id, String stop_id, String stop_name, String route_short_name, String trip_headsign, String arrival_time, String departure_time) {
        this.route_id = route_id;
        this.trip_id = trip_id;
        this.service_id = service_id;
        this.stop_id = stop_id;
        this.stop_name = stop_name;
        this.route_short_name = route_short_name;
        this.trip_headsign = trip_headsign;
        this.arrival_time = arrival_time;
        this.departure_time = departure_time;
    }

    /**
     *
     * @return ID that uniquely identifies a route
     */
    public String getRoute_id() {
        return route_id;
    }

    /**
     *
     * @param route_id ID that uniquely identifies a route
     */
    public void setRoute_id(String route_id) {
        this.route_id = route_id;
    }

    /**
     *
     * @return ID that identifies a trip
     */
    public String getTrip_id() {
        return trip_id;
    }

    /**
     *
     * @param trip_id ID that identifies a trip
     */
    public void setTrip_id(String trip_id) {
        this.trip_id = trip_id;
    }

    /**
     *
     * @return ID that uniquely identifies a set of dates when service is available for one or more routes.
     * This value is referenced from the OccurringService class.
     */
    public String getService_id() {
        return service_id;
    }

    /**
     *
     * @param service_id ID that uniquely identifies a set of dates when service is available for one or more routes.
     *                   This value is referenced from the OccurringService class
     */
    public void setService_id(String service_id) {
        this.service_id = service_id;
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
     * @return contains the name of a stop, station, or station entrance, a name that people understand in the local and
     * tourist vernacular
     */
    public String getStop_name() {
        return stop_name;
    }

    /**
     *
     * @param stop_name contains the name of a stop, station, or station entrance, a name that people understand in the
     *                  local and tourist vernacular
     */
    public void setStop_name(String stop_name) {
        this.stop_name = stop_name;
    }

    /**
     *
     * @return contains the short name of a route. This will often be a short, abstract identifier like
     * "32", "100X", or "Green" that riders use to identify a route, but which doesn't give any indication of what
     * places the route serves
     */
    public String getRoute_short_name() {
        return route_short_name;
    }

    /**
     *
     * @param route_short_name contains the short name of a route. This will often be a short, abstract identifier like
     *                         "32", "100X", or "Green" that riders use to identify a route, but which doesn't give any
     *                         indication of what places the route serves
     */
    public void setRoute_short_name(String route_short_name) {
        this.route_short_name = route_short_name;
    }

    /**
     *
     * @return contains the text that appears on a sign that identifies the trip's destination to passengers
     */
    public String getTrip_headsign() {
        return trip_headsign;
    }

    /**
     *
      * @param trip_headsign contains the text that appears on a sign that identifies the trip's destination to passengers
     */
    public void setTrip_headsign(String trip_headsign) {
        this.trip_headsign = trip_headsign;
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
     * @param arrival_time specifies the arrival time at a specific stop for a specific trip on a route
     */
    public void setArrival_time(String arrival_time) {
        this.arrival_time = arrival_time;
    }

    /**
     *
     * @return specifies the departure time from a specific stop for a specific trip on a route
     */
    public String getDeparture_time() {
        return departure_time;
    }

    /**
     *
     * @param departure_time specifies the departure time from a specific stop for a specific trip on a route
     */
    public void setDeparture_time(String departure_time) {
        this.departure_time = departure_time;
    }

    /**
     * This method returns a friendly string which can be used for logging. It uses the pattern <i>route_short_name: stop_name -> trip_headsign</i>.
     * See Constructor or class doc for more Details on the patterns variables.
     * @return a friendly string which can be used for logging
     */
    public String getFriendlyName() {
        return getRoute_short_name() + ": " + getStop_name() + " -> " + getTrip_headsign();
    }
}
