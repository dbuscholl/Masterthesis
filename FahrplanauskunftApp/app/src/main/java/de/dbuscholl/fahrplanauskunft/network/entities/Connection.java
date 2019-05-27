package de.dbuscholl.fahrplanauskunft.network.entities;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Entity class containing information about a connection. A connection is a full path from A to B which may contain lots
 * of interchanges and intermediates. It has an id, a start time, ending time and the legs. Legs are part of the connectnios
 * which can be done without changing the vehicle. As soon as you need to change a new leg is being noted.
 */
public class Connection {
    private String id;
    private String startTime;
    private String endTime;
    private ArrayList<Trip> legs;

    /**
     * Empty Constructor
     */
    public Connection() {
    }

    /**
     * Constructor with all parameters
     * @param id id of the connection which is usually provided by TRIAS interface
     * @param startTimeU planned start time of the connection
     * @param endTime planned end time of the connection
     * @param legs the parts of the connection containing more travel information.
     */
    public Connection(String id, String startTimeU, String endTime, ArrayList<Trip> legs) {
        this.id = id;
        this.startTime = startTimeU;
        this.endTime = endTime;
        this.legs = legs;
    }


    /**
     * creating a JSON String representation of the full connection. This will also include all trip legs and all of a
     * legs children.
     * @return JSON String of the connection
     */
    public JSONObject toJSON() {
        JSONObject connection = new JSONObject();
        try {
            connection.put("id", id == null ? "" : id);
            connection.put("startTime", startTime == null ? "" : startTime);
            connection.put("endTime", endTime == null ? "" : endTime);

            JSONArray legs = new JSONArray();
            for (Trip leg : getLegs()) {
                JSONObject json = leg.toJSON();
                legs.put(json == null ? "" : json);
            }
            connection.put("legs", legs);

        } catch (JSONException e) {
            return null;
        }

        return connection;
    }

    /**
     * Only a delegate for the toJSON method.
     * @return JSON representation of the connection
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
     * @return id of the connection
     */
    public String getId() {
        return id;
    }

    /**
     * sets the id
     * @param id id of the connection which is usually provided by TRIAS interface
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * getter
     * @return the time of the connection when it is planned to take place
     */
    public String getStartTime() {
        return startTime;
    }

    /**
     * sets the start time
     * @param startTime planned start time of the connection
     */
    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    /**
     * getter
     * @return the end time of the trip when it is planned to be finished.
     */
    public String getEndTime() {
        return endTime;
    }

    /**
     * sets the end time
     * @param endTime planned end time of the connection
     */
    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    /**
     * getter
     * @return returns the array containing all trip legs.
     */
    public ArrayList<Trip> getLegs() {
        return legs;
    }

    /**
     * sets the legs
     * @param legs the parts of the connection containing more travel information
     */
    public void setLegs(ArrayList<Trip> legs) {
        this.legs = legs;
    }

    /**
     * checking if the connections are equal by comparing reference equality and also departure time, arrival time
     * and size of trip legs
     * @param o the other object which should be compared with
     * @return true if equal, false if not
     */
    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof Connection)) {
            return false;
        }

        //legs length
        Connection other = (Connection) o;
        if (legs.size() != other.legs.size()) {
            return false;
        }

        int size = legs.size();
        if (size <= 0) {
            return false;
        }

        //departure equality
        StopPoint departureThis = legs.get(0).getBoarding();
        StopPoint departureOther = other.legs.get(0).getBoarding();

        if (!(departureThis.getName().equals(departureOther.getName()))) {
            return false;
        }
        if (!(departureThis.getDepartureTime().equals(departureOther.getDepartureTime()))) {
            return false;
        }

        //arrival equality
        StopPoint arrivalThis = legs.get(size - 1).getAlighting();
        StopPoint arrivalOther = legs.get(size - 1).getAlighting();
        if (!(arrivalThis.getName().equals(arrivalOther.getName()))) {
            return false;
        }
        if (!(arrivalThis.getArrivalTime().equals(arrivalOther.getDepartureTime()))) {
            return false;
        }

        return true;
    }
}
