package de.dbuscholl.fahrplanauskunft.network.entities;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

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
     * creatse a json object from the class instance
     * @return a json object from the class instance
     */
    public JSONObject toJSON() {
        JSONObject stopPoint = new JSONObject();
        try {
            stopPoint.put("position",position);
            stopPoint.put("ref",ref==null?"":ref);
            stopPoint.put("name",name==null?"":name);
            stopPoint.put("bay",bay==null?"":bay);
            stopPoint.put("arrivalTime",arrivalTime==null?"":arrivalTime);
            stopPoint.put("arrivalTimeEstimated",arrivalTimeEstimated==null?"":arrivalTimeEstimated);
            stopPoint.put("departureTime",departureTime==null?"":departureTime);
            stopPoint.put("departureTimeEstimated",departureTimeEstimated==null?"":departureTimeEstimated);
            return stopPoint;
        }catch (JSONException e) {
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

        if(json==null) {
            return "null";
        } else {
            return json.toString();
        }
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
}
