package de.dbuscholl.fahrplanauskunft.network.entities;

import java.util.ArrayList;

public class Trip {
    private Service service;
    private StopPoint boarding;
    private StopPoint alighting;
    private ArrayList<StopPoint> intermediates = new ArrayList<>();
    private int legId;
    private TripType type;
    private String interchangeType;

    public Trip() {}

    public Trip(Service service, StopPoint boarding, StopPoint alighting, ArrayList<StopPoint> intermediates, int legId, TripType type) {
        this.service = service;
        this.boarding = boarding;
        this.alighting = alighting;
        this.intermediates = intermediates;
        this.legId = legId;
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

    public enum TripType {
        TIMED, INTERCHANGE, CONTINUOUS;
    }
}
