package de.dbuscholl.fahrplanauskunft.network.entities;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;


public class Service implements Parcelable {
    private String operatingDayRef;
    private String journeyRef;
    private String lineRef;
    private String railType;
    private String railName;
    private String lineName;
    private String route;
    private String desitnation;

    public Service(){}

    public Service(String operatingDayRef, String journeyRef, String lineRef, String railType, String railName, String lineNAme, String riute, String desitnation) {
        this.operatingDayRef = operatingDayRef;
        this.journeyRef = journeyRef;
        this.lineRef = lineRef;
        this.railType = railType;
        this.railName = railName;
        this.lineName = lineNAme;
        this.route = riute;
        this.desitnation = desitnation;
    }

    protected Service(Parcel in) {
        operatingDayRef = in.readString();
        journeyRef = in.readString();
        lineRef = in.readString();
        railType = in.readString();
        railName = in.readString();
        lineName = in.readString();
        route = in.readString();
        desitnation = in.readString();
    }

    public Service(JSONObject json) {
        try {
            operatingDayRef = json.has("operatingDayRef") ? json.getString("operatingDayRef") : null;
            journeyRef = json.has("journeyRef") ? json.getString("journeyRef") : null;
            lineRef = json.has("lineRef") ? json.getString("lineRef") : null;
            railType = json.has("railType") ? json.getString("railType") : null;
            railName = json.has("railName") ? json.getString("railName") : null;
            lineName = json.has("lineName") ? json.getString("lineName") : null;
            route = json.has("route") ? json.getString("route") : null;
            desitnation = json.has("desitnation") ? json.getString("desitnation") : null;
        } catch (JSONException e) {
            throw new NullPointerException(e.getMessage());
        }
    }

    public static final Creator<Service> CREATOR = new Creator<Service>() {
        @Override
        public Service createFromParcel(Parcel in) {
            return new Service(in);
        }

        @Override
        public Service[] newArray(int size) {
            return new Service[size];
        }
    };

    public JSONObject toJSON() {
        JSONObject service = new JSONObject();
        try {
            service.put("operatingDayRef", operatingDayRef==null?"":operatingDayRef);
            service.put("journeyRef",journeyRef==null?"":journeyRef);
            service.put("lineRef",lineRef==null?"":lineRef);
            service.put("railType",railType==null?"":railType);
            service.put("railName",railName==null?"":railName);
            service.put("lineName",lineName==null?"":lineName);
            service.put("route",route==null?"":route);
            service.put("desitnation",desitnation==null?"":desitnation);
            return service;
        }
        catch (JSONException e) {
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

    public String getOperatingDayRef() {
        return operatingDayRef;
    }

    public void setOperatingDayRef(String operatingDayRef) {
        this.operatingDayRef = operatingDayRef;
    }

    public String getJourneyRef() {
        return journeyRef;
    }

    public void setJourneyRef(String journeyRef) {
        this.journeyRef = journeyRef;
    }

    public String getLineRef() {
        return lineRef;
    }

    public void setLineRef(String lineRef) {
        this.lineRef = lineRef;
    }

    public String getRailType() {
        return railType;
    }

    public void setRailType(String railType) {
        this.railType = railType;
    }

    public String getRailName() {
        return railName;
    }

    public void setRailName(String railName) {
        this.railName = railName;
    }

    public String getLineName() {
        return lineName;
    }

    public void setLineName(String lineName) {
        this.lineName = lineName;
    }

    public String getRoute() {
        return route;
    }

    public void setRoute(String route) {
        this.route = route;
    }

    public String getDesitnation() {
        return desitnation;
    }

    public void setDesitnation(String desitnation) {
        this.desitnation = desitnation;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(operatingDayRef);
        dest.writeString(journeyRef);
        dest.writeString(lineRef);
        dest.writeString(railType);
        dest.writeString(railName);
        dest.writeString(lineName);
        dest.writeString(route);
        dest.writeString(desitnation);
    }
}
