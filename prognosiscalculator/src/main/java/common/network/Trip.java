package common.network;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * entity class representing trip information. A trip contains a service which provides non changing information like linenumber,
 * departure and arrival stops which are own classes containing more information.
 */
public class Trip {
    private Service service;
    private StopPoint boarding;
    private StopPoint alighting;
    private ArrayList<StopPoint> intermediates = new ArrayList<>();
    private int legId = -1;
    private TripType type;
    private String interchangeType;
    private String GTFSTripId;

    /**
     * empty constructor
     */
    public Trip() {
    }

    /**
     * parameterized constructor
     * @param service provides non changing information like linenumber
     * @param boarding stop class of boarding
     * @param alighting stop class of alighting
     * @param intermediates list of all stops occuring during the trip. Interchanges dont have intermediates
     * @param legId position inside a connection
     * @param type type of trip can be timed or interchange
     */
    public Trip(Service service, StopPoint boarding, StopPoint alighting, ArrayList<StopPoint> intermediates, int legId, TripType type) {
        this.service = service;
        this.boarding = boarding;
        this.alighting = alighting;
        this.intermediates = intermediates;
        this.legId = legId;
        this.type = type;
    }

    /**
     * Instantiates a Trip Object from a JSON Object. Filling in all Attributes from it. This is useful for http-requests
     * as their payload is encoded with json
     * @param json
     */
    public Trip(JSONObject json) {
        try {
            legId = json.has("legId") ? json.getInt("legId") : -1;
            type = json.has("type") ? TripType.valueOf(json.getString("type")) : null;
            interchangeType = json.has("interchangeType") ? json.getString("interchangeType") : null;

            if (type != null && !type.name().equalsIgnoreCase("INTERCHANGE")) {
                service = json.has("service") ? new Service(json.getJSONObject("service")) : null;
            }

            boarding = new StopPoint(json.getJSONObject("boarding"));
            alighting = new StopPoint(json.getJSONObject("alighting"));

            if (json.has("intermediates") && json.getJSONArray("intermediates").length() > 0) {
                this.intermediates = new ArrayList<>();
                JSONArray intermediates = json.getJSONArray("intermediates");

                for (int i = 0; i < intermediates.length(); i++) {
                    JSONObject intermediate = intermediates.getJSONObject(i);
                    StopPoint stop = new StopPoint(intermediate);
                    this.intermediates.add(stop);
                }
            }

        } catch (JSONException e) {
            throw new NullPointerException(e.getMessage());
        }
    }

    /**
     * creates a json object from the class instance
     * @return a json object from the class instance
     */
    public JSONObject toJSON() {
        JSONObject trip = new JSONObject();
        try {
            if (type == null) {
                return null;
            }
            trip.put("type", type.toString());
            trip.put("legId", legId);
            trip.put("interchangeType", interchangeType == null ? "" : interchangeType);

            if (service != null) {
                JSONObject json = service.toJSON();
                trip.put("service", json == null ? "" : json);
            } else {
                trip.put("service", "");
            }

            if (boarding != null) {
                JSONObject json = boarding.toJSON();
                trip.put("boarding", json == null ? "" : json);
            } else {
                trip.put("boarding", "");
            }
            if (alighting != null) {
                JSONObject json = alighting.toJSON();
                trip.put("alighting", json == null ? "" : json);
            } else {
                trip.put("alighting", "");
            }

            JSONArray intermeds = new JSONArray();
            for (StopPoint s : intermediates) {
                if (s != null) {
                    JSONObject json = s.toJSON();
                    intermeds.put(json == null ? "" : json);
                } else {
                    intermeds.put("");
                }
            }
            trip.put("intermediates", intermeds);
            return trip;
        } catch (JSONException e) {
            return null;
        }
    }

    /**
     * creates a json string representation of this class instance
     * @return a json string representation of this class instance
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
     * @return the type of interchange when the leg is an interchange e.g. walking driving
     */
    public String getInterchangeType() {
        return interchangeType;
    }

    /**
     * setter
     * @param interchangeType the type of interchange when the leg is an interchange e.g. walking driving
     */
    public void setInterchangeType(String interchangeType) {
        this.interchangeType = interchangeType;
    }

    /**
     * getter
     * @return type of trip can be timed or interchange
     */
    public TripType getType() {
        return type;
    }

    /**
     * setter
     * @param type type of trip can be timed or interchange
     */
    public void setType(TripType type) {
        this.type = type;
    }

    /**
     * getter
     * @return service providing non changing information like linenumber
     */
    public Service getService() {
        return service;
    }

    /**
     * setter
     * @param service service providing non changing information like linenumber
     */
    public void setService(Service service) {
        this.service = service;
    }

    /**
     * getter
     * @return stop class of boarding
     */
    public StopPoint getBoarding() {
        return boarding;
    }

    /**
     * setter
     * @param boarding stop class of boarding
     */
    public void setBoarding(StopPoint boarding) {
        this.boarding = boarding;
    }

    /**
     * getter
     * @return stop class of alighting
     */
    public StopPoint getAlighting() {
        return alighting;
    }

    /**
     * setter
     * @param alighting stop class of alighting
     */
    public void setAlighting(StopPoint alighting) {
        this.alighting = alighting;
    }

    /**
     * getter
     * @return list of all stops occuring during the trip. Interchanges dont have intermediates
     */
    public ArrayList<StopPoint> getIntermediates() {
        return intermediates;
    }

    /**
     * setter
     * @param intermediates list of all stops occuring during the trip. Interchanges dont have intermediates
     */
    public void setIntermediates(ArrayList<StopPoint> intermediates) {
        this.intermediates = intermediates;
    }

    /**
     * setter
     * @param legId  position inside a connection
     */
    public void setLegId(int legId) {
        this.legId = legId;
    }

    /**
     * getter
     * @return position inside a connection
     */
    public int getLegId() {
        return legId;
    }

    /**
     * getter
     * @return the gtfs trip id corresponding to the TRIAS trip
     */
    public String getGTFSTripId() {
        return GTFSTripId;
    }

    /**
     * setter
     * @param GTFSTripId the gtfs trip id corresponding to the TRIAS trip
     */
    public void setGTFSTripId(String GTFSTripId) {
        this.GTFSTripId = GTFSTripId;
    }

    /**
     * Type of trips which can occur are:<br> TIMED: The standard trip leg. <br>INTERCHANGE: user has to switch vehicle<br>
     *     CONTINUOUS: user can stay in vehicle although service change.
     *
     */
    public enum TripType {
        TIMED, INTERCHANGE, CONTINUOUS;
    }
}
