package de.dbuscholl.fahrplanauskunft.network.xml;

import org.jdom2.JDOMException;
import org.jdom2.Namespace;

import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.TimeZone;

import de.dbuscholl.fahrplanauskunft.FormatTools;

/**
 * Request Document for a TripInfo to the TRIAS interface Verbindungsaufkunft. The string representation can be obtained
 * by calling toString. It's content is usually pasted into the request payload.
 */
public class TripInfoRequest extends XMLDocument {
    /**
     * Initializes an XML Document via resourceName for use of templates in form of xml document files
     *
     * @param resourceStream the resource to be used for this class from the file
     * @throws JDOMException when parsing goes wrong
     * @throws IOException when reading goes wrong
     */
    public TripInfoRequest(InputStream resourceStream) throws JDOMException, IOException {
        super(resourceStream);
    }

    /**
     * builds the request by inserting the corresponding parameters into the right place.
     * @param origin starting point of the trip as id
     * @param destination target point of the trip as id
     * @param departureTime time of departure as TRIAS UTC String which is bit different than normal UTC
     */
    public void buildRequest(String origin, String destination, String departureTime) {
        Namespace n = Namespace.getNamespace("http://www.vdv.de/trias");

        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Europe/Berlin"));

        findElementByName("RequestTimestamp").setText(FormatTools.formatTrias(cal.getTime()));
        findElementByName("Origin").getChild("LocationRef",n).getChild("StopPointRef",n).setText(origin);
        findElementByName("Destination").getChild("LocationRef",n).getChild("StopPointRef",n).setText(destination);
        findElementByName("DepArrTime").setText(departureTime);
    }
}
