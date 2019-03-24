package de.dbuscholl.fahrplanauskunft.network.entities;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Trip implements Parcelable {
    private Service service;
    private StopPoint boarding;
    private StopPoint alighting;
    private ArrayList<StopPoint> intermediates = new ArrayList<>();
    private int legId;
    private TripType type;
    private String interchangeType;

    public Trip() {
    }

    public Trip(Service service, StopPoint boarding, StopPoint alighting, ArrayList<StopPoint> intermediates, int legId, TripType type) {
        this.service = service;
        this.boarding = boarding;
        this.alighting = alighting;
        this.intermediates = intermediates;
        this.legId = legId;
    }

    protected Trip(Parcel in) {
        service = in.readParcelable(Service.class.getClassLoader());
        boarding = in.readParcelable(StopPoint.class.getClassLoader());
        alighting = in.readParcelable(StopPoint.class.getClassLoader());
        intermediates = in.createTypedArrayList(StopPoint.CREATOR);
        legId = in.readInt();
        type = TripType.valueOf(in.readString());
        interchangeType = in.readString();
    }

    public static final Creator<Trip> CREATOR = new Creator<Trip>() {
        @Override
        public Trip createFromParcel(Parcel in) {
            return new Trip(in);
        }

        @Override
        public Trip[] newArray(int size) {
            return new Trip[size];
        }
    };

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

        if(json==null) {
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(service,flags);
        dest.writeParcelable(boarding,flags);
        dest.writeParcelable(alighting,flags);
        dest.writeList(intermediates);
        dest.writeInt(legId);
        dest.writeString(type.name());
        dest.writeString(interchangeType);
    }

    public enum TripType {
        TIMED, INTERCHANGE, CONTINUOUS;
    }
}
