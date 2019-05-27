package de.dbuscholl.fahrplanauskunft.network.entities;

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
    public CustomLocation(){

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
