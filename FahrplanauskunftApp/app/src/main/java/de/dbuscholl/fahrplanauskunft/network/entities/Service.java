package de.dbuscholl.fahrplanauskunft.network.entities;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * entity class about all stuff which has to do with a service. A service is a trip leg resp. information about a part of
 * a trip which can be done without changing the vehicle. Checkout parameters
 */
public class Service {
    private String operatingDayRef;
    private String journeyRef;
    private String lineRef;
    private String railType;
    private String railName;
    private String lineName;
    private String route;
    private String desitnation;

    /**
     * empty constructor
     */
    public Service(){}

    /**
     * parameterized constructor
     * @param operatingDayRef id about the date when the service takes place
     * @param journeyRef id of the trip
     * @param lineRef reference of the line number
     * @param railType type of the vehicle like bus or train
     * @param railName name of the vehicle readable
     * @param lineNAme name of the line like the number
     * @param riute the "riute" it travels. Sorry, I mean route....
     * @param desitnation the destination of the vehicle
     */
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

    /**
     * Assigns all attributes from a JSON String representation
     * @param json json string representation of the service object
     */
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


    /**
     * creates a json object from the service class
     * @return json object from the service class
     */
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

    /**
     * creates a JSON String representation of the service class
     * @return a JSON String representation of the service class
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
     * @return id about the date when the service takes place
     */
    public String getOperatingDayRef() {
        return operatingDayRef;
    }

    /**
     * setter
     * @param operatingDayRef id about the date when the service takes place
     */
    public void setOperatingDayRef(String operatingDayRef) {
        this.operatingDayRef = operatingDayRef;
    }

    /**
     * getter
     * @return id of the trip
     */
    public String getJourneyRef() {
        return journeyRef;
    }

    /**
     * setter
     * @param journeyRef id of the trip
     */
    public void setJourneyRef(String journeyRef) {
        this.journeyRef = journeyRef;
    }

    /**
     * getter
     * @return reference of the line number
     */
    public String getLineRef() {
        return lineRef;
    }

    /**
     * setter
     * @param lineRef reference of the line number
     */
    public void setLineRef(String lineRef) {
        this.lineRef = lineRef;
    }

    /**
     * getter
     * @return type of the vehicle like bus or train
     */
    public String getRailType() {
        return railType;
    }

    /**
     * setter
     * @param railType type of the vehicle like bus or train
     */
    public void setRailType(String railType) {
        this.railType = railType;
    }

    /**
     * getter
     * @return name of the vehicle readable
     */
    public String getRailName() {
        return railName;
    }

    /**
     * setter
     * @param railName name of the vehicle readable
     */
    public void setRailName(String railName) {
        this.railName = railName;
    }

    /**
     * getter
     * @return name of the line like the number
     */
    public String getLineName() {
        return lineName;
    }

    /**
     * setter
     * @param lineName name of the line like the number
     */
    public void setLineName(String lineName) {
        this.lineName = lineName;
    }

    /**
     * getter
     * @return the route containing information about the travel path
     */
    public String getRoute() {
        return route;
    }

    /**
     * setter
     * @param route the route containing information about the travel path
     */
    public void setRoute(String route) {
        this.route = route;
    }

    /**
     * getter
     * @return the destination of the vehicle
     */
    public String getDesitnation() {
        return desitnation;
    }

    /**
     * setter
     * @param desitnation the destination of the vehicle
     */
    public void setDesitnation(String desitnation) {
        this.desitnation = desitnation;
    }

}
