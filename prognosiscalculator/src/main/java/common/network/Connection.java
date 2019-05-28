package common.network;

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
     * @param startTime planned start time of the connection
     * @param endTime planned end time of the connection
     * @param legs the parts of the connection containing more travel information.
     */
    public Connection(String id, String startTime, String endTime, ArrayList<Trip> legs) {
        this.id = id;
        this.startTime = startTime;
        this.endTime = endTime;
        this.legs = legs;
    }

    /**
     * creates an instance of the connection class by a jsonObject which is very useful for http requests
     * @param json the json Object from which the attributes should be set
     */
    public Connection(JSONObject json) {
        try {
            id = json.has("id") ? json.getString("id") : null;
            startTime = json.getString("startTime");
            endTime = json.getString("endTime");
            if (!json.has("legs") || json.getJSONArray("legs").length() == 0) {
                legs = new ArrayList<>();
            } else {
                JSONArray legs = json.getJSONArray("legs");
                this.legs = new ArrayList<>();

                for (int i = 0; i < legs.length(); i++) {
                    JSONObject leg = legs.getJSONObject(i);
                    if (leg.getString("type").equalsIgnoreCase("interchange")) {
                        continue;
                    }
                    Trip t = new Trip(leg);
                    this.legs.add(t);
                }
            }
        } catch (JSONException e) {
            throw new NullPointerException(e.getMessage());
        }
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

        if (json == null) {
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

    /**
     * returns a list of stoppoints for the whole connection including the intermediates
     * @return a list of stoppoints for the whole connection including the intermediates
     */
    public ArrayList<StopPoint> extractAllStops() {
        ArrayList<StopPoint> stops = new ArrayList<>();

        for (Trip t : legs) {
            stops.add(t.getBoarding());
            stops.addAll(t.getIntermediates());
            stops.add(t.getAlighting());
        }

        return stops;
    }

    /**
     * This function checks whether the given stopPoint is an alighting inside of the connection of any trip leg.
     * @param s the stop which should be checked
     * @return true if it is an alighting, false if not.
     */
    public boolean isAnAlighting(StopPoint s) {
        for (Trip t : legs) {
            StopPoint alighting = t.getAlighting();
            if (alighting == s) {
                return true;
            }

            boolean sameName = alighting.getName().equals(s.getName());
            boolean sameArrivalTime = alighting.getArrivalTime().equals(s.getArrivalTime());
            if (sameName && sameArrivalTime) {
                return true;
            }
        }
        return false;
    }
}
