package entities.network;

import org.json.JSONException;
import org.json.JSONObject;

public class StopPoint  {
    private int position;
    private String ref;
    private String name;
    private String bay;
    private String arrivalTime;
    private String arrivalTimeEstimated;
    private String departureTime;
    private String departureTimeEstimated;

    public StopPoint() {
    }

    public StopPoint(int position, String ref, String name, String bay, String arrivalTime, String arrivalTimeEstimated, String departureTime, String departureTimeEstimated) {
        this.position = position;
        this.ref = ref;
        this.name = name;
        this.bay = bay;
        this.arrivalTime = arrivalTime;
        this.arrivalTimeEstimated = arrivalTimeEstimated;
        this.departureTime = departureTime;
        this.departureTimeEstimated = departureTimeEstimated;
    }

    public StopPoint(JSONObject json) {
        try {
            position = json.has("position") ? json.getInt("position") : -1;
            ref = json.has("ref") ? json.getString("ref") : null;
            name = json.has("name") ? json.getString("name") : null;
            bay = json.has("bay") ? json.getString("bay") : null;
            arrivalTime = json.has("arrivalTime") ? json.getString("arrivalTime") : null;
            arrivalTimeEstimated = json.has("arrivalTimeEstimated") ? json.getString("arrivalTimeEstimated") : null;
            departureTime = json.has("departureTime") ? json.getString("departureTime") : null;
            departureTimeEstimated = json.has("departureTimeEstimated") ? json.getString("departureTimeEstimated") : null;
        } catch (JSONException e) {
            throw new NullPointerException(e.getMessage());
        }
    }

    public JSONObject toJSON() {
        JSONObject stopPoint = new JSONObject();
        try {
            stopPoint.put("position",position);
            stopPoint.put("ref",ref==null?"":ref);
            stopPoint.put("name",name==null?"":name);
            stopPoint.put("bay",bay==null?"":bay);
            stopPoint.put("arrivalTime",arrivalTime==null?"":arrivalTime);
            stopPoint.put("arrivalTimeEstimated",arrivalTimeEstimated==null?"":arrivalTimeEstimated);
            stopPoint.put("departureTime",departureTime==null?"":departureTime);
            stopPoint.put("departureTimeEstimated",departureTimeEstimated==null?"":departureTimeEstimated);
            return stopPoint;
        }catch (JSONException e) {
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

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBay() {
        return bay;
    }

    public void setBay(String bay) {
        this.bay = bay;
    }

    public String getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(String arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public String getArrivalTimeEstimated() {
        return arrivalTimeEstimated;
    }

    public void setArrivalTimeEstimated(String arrivalTimeEstimated) {
        this.arrivalTimeEstimated = arrivalTimeEstimated;
    }

    public String getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(String departureTime) {
        this.departureTime = departureTime;
    }

    public String getDepartureTimeEstimated() {
        return departureTimeEstimated;
    }

    public void setDepartureTimeEstimated(String departureTimeEstimated) {
        this.departureTimeEstimated = departureTimeEstimated;
    }
}
