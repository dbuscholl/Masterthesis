package de.dbuscholl.fahrplanauskunft.network.entities;

import android.os.Parcel;
import android.os.Parcelable;

import org.jdom2.Element;
import org.jdom2.JDOMException;

import java.io.IOException;
import java.util.ArrayList;

import de.dbuscholl.fahrplanauskunft.common.Constants;
import de.dbuscholl.fahrplanauskunft.network.xml.XMLDocument;

/**
 * This entity class contains information about a station. A station can have information like geocoordinates and name.
 * This class is not used for intermediates of a trip leg. It is used for stations for the autocomplete view.
 */
public class Station {
    private String ref;
    private String name;
    private String locationRef;
    private String locationName;
    private float longitude;
    private float latitude;

    /**
     * empty constructor
     */
    public Station() {}

    /**
     * parameterized constructor
     * @param ref id of the station
     * @param name name of the station
     * @param locationRef reference about the location of the station which is usually the city
     * @param locationName name of the location which is usually the name of the city
     * @param longitude longitude
     * @param latitude latitude
     */
    public Station(String ref, String name, String locationRef, String locationName, float longitude, float latitude) {
        this.ref = ref;
        this.name = name;
        this.locationRef = locationRef;
        this.locationName = locationName;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    /**
     * getter
     * @return id of the station
     */
    public String getRef() {
        return ref;
    }

    /**
     * setter
     * @param ref id of the station
     */
    public void setRef(String ref) {
        this.ref = ref;
    }

    /**
     * getter
     * @return name of the station
     */
    public String getName() {
        return name;
    }

    /**
     * setter
     * @param name name of the station
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * getter
     * @return reference about the location of the station which is usually the city
     */
    public String getLocationRef() {
        return locationRef;
    }

    /**
     * setter
     * @param locationRef reference about the location of the station which is usually the city
     */
    public void setLocationRef(String locationRef) {
        this.locationRef = locationRef;
    }

    /**
     * getter
     * @return name of the location which is usually the name of the city
     */
    public String getLocationName() {
        return locationName;
    }

    /**
     * setter
     * @param locationName name of the location which is usually the name of the city
     */
    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    /**
     * getter
     * @return longitude
     */
    public float getLongitude() {
        return longitude;
    }

    /**
     * setter
     * @param longitude longitude
     */
    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

    /**
     * getter
     * @return latitude
     */
    public float getLatitude() {
        return latitude;
    }

    /**
     * setter
     * @param latitude latitude
     */
    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    /**
     * returns name + locationname as a readable string for autocomplete view
     * @return
     */
    @Override
    public String toString() {
        return name + ", " + locationName;
    }

    /**
     * creates a list of station instances by a trias string result.
     * @param result the xml result string given by trias containing all stops
     * @return a list of stations
     */
    public static ArrayList<Station> stationListFromTriasResult(String result) {
        ArrayList<Station> stations = new ArrayList<>();

        try {
            ArrayList<Element> elements = XMLDocument.documentFromString(result).findElementsByName("Location");

            for(Element e : elements) {
                try {
                    String ref = e.getChild("StopPoint", Constants.NAMESPACE).getChild("StopPointRef", Constants.NAMESPACE).getTextNormalize();
                    String name = e.getChild("StopPoint", Constants.NAMESPACE).getChild("StopPointName", Constants.NAMESPACE).getChild("Text", Constants.NAMESPACE).getTextNormalize();
                    String locationRef = e.getChild("StopPoint", Constants.NAMESPACE).getChild("LocalityRef", Constants.NAMESPACE).getTextNormalize();
                    String locationName = e.getChild("LocationName", Constants.NAMESPACE).getChild("Text", Constants.NAMESPACE).getTextNormalize();
                    float longitude = 0;
                    float latitude = 0;
                    try {
                        longitude = Float.parseFloat(e.getChild("GeoPosition", Constants.NAMESPACE).getChild("Longitude", Constants.NAMESPACE).getTextNormalize());
                        latitude = Float.parseFloat(e.getChild("GeoPosition", Constants.NAMESPACE).getChild("Latitude", Constants.NAMESPACE).getTextNormalize());
                    } catch (NumberFormatException ignored) {
                    }
                    stations.add(new Station(ref,name,locationRef,locationName,longitude,latitude));
                }
                catch (NullPointerException exception) {
                }
            }
        } catch (JDOMException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        return stations;
    }
}
