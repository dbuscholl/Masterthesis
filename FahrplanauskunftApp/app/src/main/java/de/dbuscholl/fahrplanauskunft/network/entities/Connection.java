package de.dbuscholl.fahrplanauskunft.network.entities;

import java.util.ArrayList;

public class Connection {
    private String id;
    private String startTime;
    private String endTime;
    private ArrayList<Trip> legs;

    public Connection() {}

    public Connection(String id, String startTimeU, String endTime, ArrayList<Trip> legs) {
        this.id = id;
        this.startTime = startTimeU;
        this.endTime = endTime;
        this.legs = legs;
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
}
