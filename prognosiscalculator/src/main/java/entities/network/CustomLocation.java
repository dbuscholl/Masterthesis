package entities.network;

import org.json.JSONException;
import org.json.JSONObject;

public class CustomLocation {
    private double latitude;
    private double longitude;
    private double altitude;
    private float accuracy;
    private long time;

    public CustomLocation() {

    }

    public CustomLocation(double latitude, double longitude, double altitude, float accuracy, long time) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
        this.accuracy = accuracy;
        this.time = time;
    }

    public CustomLocation(JSONObject json) {
        try {
            latitude = json.has("latitude") ? json.getDouble("latitude") : 0;
            longitude = json.has("longitude") ? json.getDouble("longitude") : 0;
            altitude = json.has("altitude") ? json.getDouble("altitude") : 0;
            accuracy = json.has("accuracy") ? json.getFloat("accuracy") : Float.MAX_VALUE;
            time = json.has("time") ? json.getLong("time") : 0;
        } catch (JSONException e) {
            throw new NullPointerException(e.getMessage());
        }
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public double getAltitude() {
        return altitude;
    }

    public void setAccuracy(float accuracy) {
        this.accuracy = accuracy;
    }

    public float getAccuracy() {
        return accuracy;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getTime() {
        return time;
    }
}
