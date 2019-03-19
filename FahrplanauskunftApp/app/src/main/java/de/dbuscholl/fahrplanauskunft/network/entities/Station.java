package de.dbuscholl.fahrplanauskunft.network.entities;

import android.os.Parcel;
import android.os.Parcelable;

import org.jdom2.Element;
import org.jdom2.JDOMException;

import java.io.IOException;
import java.util.ArrayList;

import de.dbuscholl.fahrplanauskunft.common.Constants;
import de.dbuscholl.fahrplanauskunft.network.xml.XMLDocument;

public class Station implements Parcelable {
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

    protected Station(Parcel in) {
        ref = in.readString();
        name = in.readString();
        locationRef = in.readString();
        locationName = in.readString();
        longitude = in.readFloat();
        latitude = in.readFloat();
    }

    public static final Creator<Station> CREATOR = new Creator<Station>() {
        @Override
        public Station createFromParcel(Parcel in) {
            return new Station(in);
        }

        @Override
        public Station[] newArray(int size) {
            return new Station[size];
        }
    };

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(ref);
        dest.writeString(name);
        dest.writeString(locationRef);
        dest.writeString(locationName);
        dest.writeFloat(longitude);
        dest.writeFloat(latitude);
    }
}
