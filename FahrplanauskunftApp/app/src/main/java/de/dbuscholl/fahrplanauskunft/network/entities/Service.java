package de.dbuscholl.fahrplanauskunft.network.entities;

public class Service {
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
}
