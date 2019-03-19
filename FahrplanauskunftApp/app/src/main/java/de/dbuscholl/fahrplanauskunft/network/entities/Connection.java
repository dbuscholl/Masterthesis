package de.dbuscholl.fahrplanauskunft.network.entities;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Connection implements Parcelable {
    private String id;
    private String startTime;
    private String endTime;
    private ArrayList<Trip> legs;

    public Connection() {
    }

    public Connection(String id, String startTimeU, String endTime, ArrayList<Trip> legs) {
        this.id = id;
        this.startTime = startTimeU;
        this.endTime = endTime;
        this.legs = legs;
    }

    protected Connection(Parcel in) {
        id = in.readString();
        startTime = in.readString();
        endTime = in.readString();
        legs = in.createTypedArrayList(Trip.CREATOR);
    }


    public static final Creator<Connection> CREATOR = new Creator<Connection>() {
        @Override
        public Connection createFromParcel(Parcel in) {
            return new Connection(in);
        }

        @Override
        public Connection[] newArray(int size) {
            return new Connection[size];
        }
    };

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

    @Override
    public String toString() {
        JSONObject json = toJSON();

        if(json==null) {
            return "null";
        } else {
            return json.toString();
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public ArrayList<Trip> getLegs() {
        return legs;
    }

    public void setLegs(ArrayList<Trip> legs) {
        this.legs = legs;
    }

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(startTime);
        dest.writeString(endTime);
        dest.writeTypedList(legs);
    }
}
