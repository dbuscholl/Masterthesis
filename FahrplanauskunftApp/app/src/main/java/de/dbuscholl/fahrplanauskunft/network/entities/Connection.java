package de.dbuscholl.fahrplanauskunft.network.entities;

import java.util.ArrayList;

public class Connection {
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
        if(size <= 0) {
            return false;
        }

        //departure equality
        StopPoint departureThis = legs.get(0).getBoarding();
        StopPoint departureOther = other.legs.get(0).getBoarding();

        if (! (departureThis.getName().equals(departureOther.getName()))) {
            return false;
        }
        if (! (departureThis.getDepartureTime().equals(departureOther.getDepartureTime()))) {
            return false;
        }

        //arrival equality
        StopPoint arrivalThis = legs.get(size-1).getAlighting();
        StopPoint arrivalOther = legs.get(size-1).getAlighting();
        if(! (arrivalThis.getName().equals(arrivalOther.getName()))) {
            return false;
        }
        if(! (arrivalThis.getArrivalTime().equals(arrivalOther.getDepartureTime()))) {
            return false;
        }

        return true;
    }
}
