package de.dbuscholl.fahrplanauskunft.network.xml;

import org.jdom2.JDOMException;
import org.jdom2.Namespace;

import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.TimeZone;

import de.dbuscholl.fahrplanauskunft.FormatTools;

public class TripInfoRequest extends XMLDocument {
    /**
     * Initializes an XML Document via resourceName for use of templates
     *
     * @param resourceStream the resource to be used for this class
     * @throws JDOMException
     * @throws IOException
     */
    public TripInfoRequest(InputStream resourceStream) throws JDOMException, IOException {
        super(resourceStream);
    }


    public void buildRequest(String origin, String destination, String departureTime) {
        Namespace n = Namespace.getNamespace("http://www.vdv.de/trias");

        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Europe/Berlin"));

        findElementByName("RequestTimestamp").setText(FormatTools.formatTrias(cal.getTime()));
        findElementByName("Origin").getChild("LocationRef",n).getChild("StopPointRef",n).setText(origin);
        findElementByName("Destination").getChild("LocationRef",n).getChild("StopPointRef",n).setText(destination);
        findElementByName("DepArrTime").setText(departureTime);
    }
}
