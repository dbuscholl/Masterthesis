package common.network;

import common.gtfs.TripStop;
import database.SQLFormatTools;
import javafx.scene.paint.Stop;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * This is the entity class used for trip intermediates. It defers because trias provides different information on different
 * services. Verbindungsauskunft doesnt provide geocoordinates for each intermediate for example while Ortsinformationen does.
 */
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

    /**
     * empty constructor
     */
    public StopPoint() {
    }

    /**
     * parameterized constructor
     * @param position position of the stoppoint inside a trip leg
     * @param ref id of the stop
     * @param name name of the stop
     * @param bay plattform for trains where it departs
     * @param arrivalTime time of arrival
     * @param arrivalTimeEstimated realtime of arrival
     * @param departureTime time of departure
     * @param departureTimeEstimated realtime of departure
     */
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

    /**
     * creates a StopPoint instance based on a JSONObject. This is very useful for http requests which come in json usually
     * @param json
     */
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

    /**
     * creatse a json object from the class instance
     * @return a json object from the class instance
     */
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

    /**
     *
     * @return a json string representation of the class instance containing all parameters
     */
    @Override
    public String toString() {
        JSONObject json = toJSON();

        if (json == null) {
            return "null";
        } else {
            return json.toString();
        }
    }

    /**
     * checks if a delay is set for this stopPoint
     * @return true if there is a delay set, false if not
     */
    public boolean hasCalculatedDelay() {
        return delay != 0;
    }

    /**
     * This function check if the stopPoint is part of a trip leg which is provided as parameter
     * @param t the trip to check if the stopPoint is part of
     * @return true if the stopPoint is part of the trip leg, false if not
     */
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

    /**
     * Checks StopPoint equality which is given, when either object is the same, or the parameters position, name, arrival
     * time, departure time and ref are equal.
     * @param other
     * @return
     */
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

    /**
     * getter
     * @return position of the stoppoint inside a trip leg
     */
    public int getPosition() {
        return position;
    }

    /**
     * setter
     * @param position position of the stoppoint inside a trip leg
     */
    public void setPosition(int position) {
        this.position = position;
    }

    /**
     * getter
     * @return ref id of the stop
     */
    public String getRef() {
        return ref;
    }

    /**
     * setter
     * @param ref ref id of the stop
     */
    public void setRef(String ref) {
        this.ref = ref;
    }

    /**
     * getter
     * @return name of the stop
     */
    public String getName() {
        return name;
    }

    /**
     * setter
     * @param name name of the stop
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * getter
     * @return plattform for trains where it departs
     */
    public String getBay() {
        return bay;
    }

    /**
     * setter
     * @param bay plattform for trains where it departs
     */
    public void setBay(String bay) {
        this.bay = bay;
    }

    /**
     * getter
     * @return time of arrival
     */
    public String getArrivalTime() {
        return arrivalTime;
    }

    /**
     * setter
     * @param arrivalTime time of arrival
     */
    public void setArrivalTime(String arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    /**
     * getter
     * @return realtime of arrival
     */
    public String getArrivalTimeEstimated() {
        return arrivalTimeEstimated;
    }

    /**
     * setter
     * @param arrivalTimeEstimated realtime of arrival
     */
    public void setArrivalTimeEstimated(String arrivalTimeEstimated) {
        this.arrivalTimeEstimated = arrivalTimeEstimated;
    }

    /**
     * getter
     * @return  time of departure
     */
    public String getDepartureTime() {
        return departureTime;
    }

    /**
     * setter
     * @param departureTime time of departure
     */
    public void setDepartureTime(String departureTime) {
        this.departureTime = departureTime;
    }

    /**
     * getter
     * @return realtime of departure
     */
    public String getDepartureTimeEstimated() {
        return departureTimeEstimated;
    }

    /**
     * setter
     * @param departureTimeEstimated realtime of departure
     */
    public void setDepartureTimeEstimated(String departureTimeEstimated) {
        this.departureTimeEstimated = departureTimeEstimated;
    }

    /**
     * getter
     * @return latitude of stoppoint
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * setter
     * @param latitude latitude of stoppoint
     */
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    /**
     * getter
     * @return longitude of stoppoint
     */
    public double getLongitude() {
        return longitude;
    }

    /**
     * setter
     * @param longitude longitude of stoppoint
     */
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    /**
     * getter
     * @return the distance to the custom user delay which was measured if set
     */
    public int getMinDistance() {
        return minDistance;
    }

    /**
     * setter
     * @param minDistance the distance to the custom user delay which was measured
     */
    public void setMinDistance(int minDistance) {
        this.minDistance = minDistance;
    }

    /**
     * getter
     * @return delay assigned to this stoppoint
     */
    public double getDelay() {
        return delay;
    }

    /**
     * sets the delay from a custom location timestamp by checkeing the difference between departure / arrival time from
     * the stoppoint.
     * @param locationTime the time in milliseconds of measurement of the location resp. the time of which the delay should
     *                     be calculated from.
     */
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

    /**
     * setter
     * @param delay amount of delay for this stoppoint
     */
    public void setDelay(double delay) {
        this.delay = delay;
    }

    /**
     * getter
     * @return position of the stoppoint inside its trip leg
     */
    public int getStopSequence() {
        return stopSequence;
    }

    /**
     * setter
     * @param stopSequence position of the stoppoint inside its trip leg
     */
    public void setStopSequence(int stopSequence) {
        this.stopSequence = stopSequence;
    }

    /**
     * determines the stopsequence from gtfs strcture as TRIAS stop sequence is meant to be the position after user
     * departure and gtfs uses the position after vehicle departure from planned trip origin
     * @param fullTrip the gtfs trip of which the stop sequence should be read.
     * @return position of the stop inside the full trip of gtfs
     */
    public int getStopSequenceInGTFSTrip(ArrayList<TripStop> fullTrip) {
        for (TripStop ts : fullTrip) {
            if (ts.getStop_name().equals(name)) {
                return ts.getStop_sequence();
            }
        }
        return -1;
    }
}
