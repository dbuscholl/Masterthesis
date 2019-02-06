package Network;


import Database.SQLFormatTools;
import org.jdom2.Element;
import org.jdom2.JDOMException;

import java.io.IOException;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * a specific XML Document representing a Request Content for the Departure Board. <b>Call build Request before sending,
 * otherwise request parameters are empty</b>
 */
public class DepartureBoardRequest extends XMLDocument {

    /**
     * Get's the template from the resources folder and prepares it. <b>Call build Request before sending,
     * otherwise request parameters are empty</b>
     * @throws JDOMException
     * @throws IOException
     */
    public DepartureBoardRequest() throws JDOMException, IOException {
        super("/departureBoardRequest.xml");
    }

    /**
     * Sets the Request Timestamp, StopId and Departure / Arrival Time inside the XML Request.
     * @param stopPointRef StopId for which the departure board should be returned
     * @param departureUTC UTC String representation of the time for which the departure board should be returned
     */
    public void buildRequest(String stopPointRef, String departureUTC) {
        Element root = document.getRootElement();

        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Europe/Berlin"));

        findElementByName("RequestTimestamp").setText(SQLFormatTools.formatTrias(cal.getTime()));
        findElementByName("StopPointRef").setText(stopPointRef);
        findElementByName("DepArrTime").setText(departureUTC);
    }
}
