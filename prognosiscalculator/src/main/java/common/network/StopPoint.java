package common.network;

import common.gtfs.TripStop;
import database.SQLFormatTools;
import javafx.scene.paint.Stop;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class StopPoint {
    private int position;
    private String ref;
    private String name;
    private String bay;
    private String arrivalTime;
    private String arrivalTimeEstimated;
    private String departureTime;
    private String departureTimeEstimated;
    private double latitude;
    private double longitude;
    private int minDistance;
    private double delay;
    private int stopSequence = -1;

    public StopPoint() {
    }

    public StopPoint(int position, String ref, String name, String bay, String arrivalTime, String arrivalTimeEstimated, String departureTime, String departureTimeEstimated) {
        this.position = position;
        this.ref = ref;
        this.name = name;
        this.bay = bay;
        this.arrivalTime = arrivalTime;
        this.arrivalTimeEstimated = arrivalTimeEstimated;
        this.departureTime = departureTime;
        this.departureTimeEstimated = departureTimeEstimated;
    }

    public StopPoint(JSONObject json) {
        try {
            position = json.has("position") ? json.getInt("position") : -1;
            ref = json.has("ref") ? json.getString("ref") : null;
            name = json.has("name") ? json.getString("name") : null;
            bay = json.has("bay") ? json.getString("bay") : null;
            arrivalTime = json.has("arrivalTime") ? json.getString("arrivalTime") : null;
            arrivalTimeEstimated = json.has("arrivalTimeEstimated") ? json.getString("arrivalTimeEstimated") : null;
            departureTime = json.has("departureTime") ? json.getString("departureTime") : null;
            departureTimeEstimated = json.has("departureTimeEstimated") ? json.getString("departureTimeEstimated") : null;
        } catch (JSONException e) {
            throw new NullPointerException(e.getMessage());
        }
    }

    public JSONObject toJSON() {
        JSONObject stopPoint = new JSONObject();
        try {
            stopPoint.put("position", position);
            stopPoint.put("ref", ref == null ? "" : ref);
            stopPoint.put("name", name == null ? "" : name);
            stopPoint.put("bay", bay == null ? "" : bay);
            stopPoint.put("arrivalTime", arrivalTime == null ? "" : arrivalTime);
            stopPoint.put("arrivalTimeEstimated", arrivalTimeEstimated == null ? "" : arrivalTimeEstimated);
            stopPoint.put("departureTime", departureTime == null ? "" : departureTime);
            stopPoint.put("departureTimeEstimated", departureTimeEstimated == null ? "" : departureTimeEstimated);
            return stopPoint;
        } catch (JSONException e) {
            return null;
        }
    }

    @Override
    public String toString() {
        JSONObject json = toJSON();

        if (json == null) {
            return "null";
        } else {
            return json.toString();
        }
    }

    public boolean hasCalculatedDelay() {
        return delay != 0;
    }

    public boolean isMemberOf(Trip t) {
        if(t.getBoarding().equals(this) || t.getAlighting().equals(this)) {
            return true;
        }

        for(StopPoint stop : t.getIntermediates()) {
            if(stop.equals(this)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean equals(Object other) {
        if(other == null) return false;
        if(other == this) return true;
        if(other instanceof StopPoint) {
            StopPoint otherStop = (StopPoint) other;
            return position == otherStop.position &&
                    name.equals(otherStop.name) &&
                    arrivalTime.equals(otherStop.arrivalTime) &&
                    departureTime.equals(otherStop.departureTime) &&
                    ref.equals(otherStop.ref);
        }

        return false;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBay() {
        return bay;
    }

    public void setBay(String bay) {
        this.bay = bay;
    }

    public String getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(String arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public String getArrivalTimeEstimated() {
        return arrivalTimeEstimated;
    }

    public void setArrivalTimeEstimated(String arrivalTimeEstimated) {
        this.arrivalTimeEstimated = arrivalTimeEstimated;
    }

    public String getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(String departureTime) {
        this.departureTime = departureTime;
    }

    public String getDepartureTimeEstimated() {
        return departureTimeEstimated;
    }

    public void setDepartureTimeEstimated(String departureTimeEstimated) {
        this.departureTimeEstimated = departureTimeEstimated;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public int getMinDistance() {
        return minDistance;
    }

    public void setMinDistance(int minDistance) {
        this.minDistance = minDistance;
    }

    public double getDelay() {
        return delay;
    }

    public void setDelay(long locationTime) {
        long stopTime;
        if (departureTime != null) {
            stopTime = SQLFormatTools.parseTriasTime(departureTime).getTime();
        } else if (arrivalTime != null) {
            stopTime = SQLFormatTools.parseTriasTime(arrivalTime).getTime();
        } else {
            throw new NullPointerException("Arrival Time and Departure Time are both not set!");
        }

        this.delay = ((double) locationTime - stopTime) / 1000;
    }

    public void setDelay(double delay) {
        this.delay = delay;
    }

    public int getStopSequence() {
        return stopSequence;
    }

    public void setStopSequence(int stopSequence) {
        this.stopSequence = stopSequence;
    }

    public int getStopSequenceInGTFSTrip(ArrayList<TripStop> fullTrip) {
        for (TripStop ts : fullTrip) {
            if (ts.getStop_name().equals(name)) {
                return ts.getStop_sequence();
            }
        }
        return -1;
    }
}
