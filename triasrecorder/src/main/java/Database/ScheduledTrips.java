package Database;

public class ScheduledTrips {
    private String route_id;
    private String trip_id;
    private String service_id;
    private String stop_id;
    private String stop_name;
    private String route_short_name;
    private String trip_headsign;
    private String arrival_time;
    private String departure_time;

    public ScheduledTrips(){}

    public ScheduledTrips(String route_id, String trip_id, String service_id, String stop_id, String stop_name, String route_short_name, String trip_headsign, String arrival_time, String departure_time) {
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

    public String getRoute_id() {
        return route_id;
    }

    public void setRoute_id(String route_id) {
        this.route_id = route_id;
    }

    public String getTrip_id() {
        return trip_id;
    }

    public void setTrip_id(String trip_id) {
        this.trip_id = trip_id;
    }

    public String getService_id() {
        return service_id;
    }

    public void setService_id(String service_id) {
        this.service_id = service_id;
    }

    public String getStop_id() {
        return stop_id;
    }

    public void setStop_id(String stop_id) {
        this.stop_id = stop_id;
    }

    public String getStop_name() {
        return stop_name;
    }

    public void setStop_name(String stop_name) {
        this.stop_name = stop_name;
    }

    public String getRoute_short_name() {
        return route_short_name;
    }

    public void setRoute_short_name(String route_short_name) {
        this.route_short_name = route_short_name;
    }

    public String getTrip_headsign() {
        return trip_headsign;
    }

    public void setTrip_headsign(String trip_headsign) {
        this.trip_headsign = trip_headsign;
    }

    public String getArrival_time() {
        return arrival_time;
    }

    public void setArrival_time(String arrival_time) {
        this.arrival_time = arrival_time;
    }

    public String getDeparture_time() {
        return departure_time;
    }

    public void setDeparture_time(String departure_time) {
        this.departure_time = departure_time;
    }
}
