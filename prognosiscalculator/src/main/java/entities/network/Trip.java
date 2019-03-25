package entities.network;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Trip {
    private Service service;
    private StopPoint boarding;
    private StopPoint alighting;
    private ArrayList<StopPoint> intermediates = new ArrayList<>();
    private int legId = -1;
    private TripType type;
    private String interchangeType;
    private String GTFSTripId;

    public Trip() {
    }

    public Trip(Service service, StopPoint boarding, StopPoint alighting, ArrayList<StopPoint> intermediates, int legId, TripType type) {
        this.service = service;
        this.boarding = boarding;
        this.alighting = alighting;
        this.intermediates = intermediates;
        this.legId = legId;
    }

    public Trip(JSONObject json) {
        try {
            legId = json.has("legId") ? json.getInt("legId") : -1;
            type = json.has("type") ? TripType.valueOf(json.getString("type")) : null;
            interchangeType = json.has("interchangeType") ? json.getString("interchangeType") : null;

            service = json.has("service") ? new Service(json.getJSONObject("service")) : null;

            boarding = new StopPoint(json.getJSONObject("boarding"));
            alighting = new StopPoint(json.getJSONObject("alighting"));

            if(json.has("intermediates") && json.getJSONArray("intermediates").length() > 0) {
                this.intermediates = new ArrayList<>();
                JSONArray intermediates = json.getJSONArray("intermediates");

                for(int i = 0; i < intermediates.length(); i++) {
                    JSONObject intermediate = intermediates.getJSONObject(i);
                    StopPoint stop = new StopPoint(intermediate);
                    this.intermediates.add(stop);
                }
            }

        } catch (JSONException e) {
            throw new NullPointerException(e.getMessage());
        }
    }

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

    @Override
    public String toString() {
        JSONObject json = toJSON();

        if (json == null) {
            return "null";
        } else {
            return json.toString();
        }
    }

    public String getInterchangeType() {
        return interchangeType;
    }

    public void setInterchangeType(String interchangeType) {
        this.interchangeType = interchangeType;
    }

    public TripType getType() {
        return type;
    }

    public void setType(TripType type) {
        this.type = type;
    }

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }

    public StopPoint getBoarding() {
        return boarding;
    }

    public void setBoarding(StopPoint boarding) {
        this.boarding = boarding;
    }

    public StopPoint getAlighting() {
        return alighting;
    }

    public void setAlighting(StopPoint alighting) {
        this.alighting = alighting;
    }

    public ArrayList<StopPoint> getIntermediates() {
        return intermediates;
    }

    public void setIntermediates(ArrayList<StopPoint> intermediates) {
        this.intermediates = intermediates;
    }

    public void setLegId(int legId) {
        this.legId = legId;
    }

    public int getLegId() {
        return legId;
    }

    public String getGTFSTripId() {
        return GTFSTripId;
    }

    public void setGTFSTripId(String GTFSTripId) {
        this.GTFSTripId = GTFSTripId;
    }

    public enum TripType {
        TIMED, INTERCHANGE, CONTINUOUS;
    }
}
