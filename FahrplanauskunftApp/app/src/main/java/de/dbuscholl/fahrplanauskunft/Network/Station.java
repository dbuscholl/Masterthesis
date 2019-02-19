package de.dbuscholl.fahrplanauskunft.Network;

import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;

import java.io.IOException;
import java.util.ArrayList;

public class Station {
    private String ref;
    private String name;
    private String locationRef;
    private String locationName;
    private float longitude;
    private float latitude;

    public Station() {}
    public Station(String ref, String name, String locationRef, String locationName, float longitude, float latitude) {
        this.ref = ref;
        this.name = name;
        this.locationRef = locationRef;
        this.locationName = locationName;
        this.longitude = longitude;
        this.latitude = latitude;
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

    public String getLocationRef() {
        return locationRef;
    }

    public void setLocationRef(String locationRef) {
        this.locationRef = locationRef;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public float getLongitude() {
        return longitude;
    }

    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

    public float getLatitude() {
        return latitude;
    }

    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    @Override
    public String toString() {
        return name + ", " + locationName;
    }

    public static ArrayList<Station> stationListFromTriasResult(String result) {
        Namespace namespace = Namespace.getNamespace("http://www.vdv.de/trias");
        ArrayList<Station> stations = new ArrayList<>();

        try {
            ArrayList<Element> elements = XMLDocument.documentFromString(result).findElementsByName("Location");

            for(Element e : elements) {
                try {
                    String ref = e.getChild("StopPoint", namespace).getChild("StopPointRef", namespace).getTextNormalize();
                    String name = e.getChild("StopPoint", namespace).getChild("StopPointName", namespace).getChild("Text", namespace).getTextNormalize();
                    String locationRef = e.getChild("StopPoint", namespace).getChild("LocalityRef", namespace).getTextNormalize();
                    String locationName = e.getChild("LocationName", namespace).getChild("Text", namespace).getTextNormalize();
                    float longitude = 0;
                    float latitude = 0;
                    try {
                        longitude = Float.parseFloat(e.getChild("GeoPosition", namespace).getChild("Longitude", namespace).getTextNormalize());
                        latitude = Float.parseFloat(e.getChild("GeoPosition", namespace).getChild("Latitude", namespace).getTextNormalize());
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
