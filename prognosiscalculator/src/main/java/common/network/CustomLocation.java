package common.network;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * A custom location gives information about a user position. It contains the geocoordinates parts latitude and longitude
 * and also altitude, accuray and the time when it was measured.
 */
public class CustomLocation {
    private double latitude;
    private double longitude;
    private double altitude;
    private float accuracy;
    private long time;

    /**
     * empty constructor. Nothing special
     */
    public CustomLocation() {

    }

    /**
     * Parameterized Constructor
     * @param latitude latitude
     * @param longitude longitude
     * @param altitude altitude
     * @param accuracy accuracy in meters
     * @param time timestamp when the measuring of the point took place
     */
    public CustomLocation(double latitude, double longitude, double altitude, float accuracy, long time) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
        this.accuracy = accuracy;
        this.time = time;
    }

    /**
     * creates a custom loccation instance from a json object. Sets all attributes.
     * @param json the json object of which the class should be initialized
     */
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


    /**
     * sets the latitude
     * @param latitude latitude
     */
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    /**
     * gets the latitude
     * @return latitude
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * sets the longitude
     * @param longitude longitude
     */
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    /**
     * gets the longitude
     * @return the longitude
     */
    public double getLongitude() {
        return longitude;
    }

    /**
     * sets the altitude
     * @param altitude altitude
     */
    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    /**
     * gets the altitude
     * @return the altitude
     */
    public double getAltitude() {
        return altitude;
    }

    /**
     * sets the accuracy in meters
     * @param accuracy the accuracy in meters
     */
    public void setAccuracy(float accuracy) {
        this.accuracy = accuracy;
    }

    /**
     * gets the accuracy
     * @return the accuracy in meters
     */
    public float getAccuracy() {
        return accuracy;
    }

    /**
     * sets the timestamp when te measuring took place
     * @param time the timestamp when te measuring took place
     */
    public void setTime(long time) {
        this.time = time;
    }

    /**
     * gets the timestamp
     * @return the timestamp when te measuring took place
     */
    public long getTime() {
        return time;
    }
}
