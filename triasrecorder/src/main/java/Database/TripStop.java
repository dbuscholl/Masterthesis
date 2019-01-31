package Database;

public class TripStop {
    private String arrival_time;
    private String departure_time;
    private String stop_id;
    private String stop_name;
    private int stop_sequence;
    private int pickup_type;
    private int drop_off_type;
    private Type type;

    public TripStop(){
    }

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

    public int getStop_sequence() {
        return stop_sequence;
    }

    public void setStop_sequence(int stop_sequence) {
        this.stop_sequence = stop_sequence;
    }

    public int getPickup_type() {
        return pickup_type;
    }

    public void setPickup_type(int pickup_type) {
        this.pickup_type = pickup_type;
    }

    public int getDrop_off_type() {
        return drop_off_type;
    }

    public void setDrop_off_type(int drop_off_type) {
        this.drop_off_type = drop_off_type;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public enum Type {
        GTFS,TRIAS
    }
}
