package de.dbuscholl.fahrplanauskunft.network.xml;

import org.jdom2.JDOMException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.TimeZone;

import de.dbuscholl.fahrplanauskunft.FormatTools;

/**
 * This class stores the xml representation of a request to the TRIAS interface. It can be received by calling toString.
 */
public class LocationAutocompleteRequest extends XMLDocument {

    /**
     * XML Document by Inputstream which is an XML-Document stored as android resource
     * @param is the insputstream of the resource file xml
     * @throws JDOMException When parsing goes wrong
     * @throws IOException when reading goes wrong
     */
    public LocationAutocompleteRequest(InputStream is) throws JDOMException, IOException {
        super(is);
    }

    /**
     * Inserts the missing parameters into the xml document
     * @param stop stop name for which autocomplete results should be retrieved
     */
    public void buildRequest(String stop) {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Europe/Berlin"));

        findElementByName("RequestTimestamp").setText(FormatTools.formatTrias(cal.getTime()));
        findElementByName("LocationName").setText(stop);
    }
}
