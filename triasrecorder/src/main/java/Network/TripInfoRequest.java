package Network;

import Database.SQLFormatTools;
import org.jdom2.JDOMException;

import java.io.IOException;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * a specific XML Document representing a Request Content for the Trip Info. <b>Call build Request before sending,
 * otherwise request parameters are empty</b>
 */
public class TripInfoRequest extends XMLDocument {

    /**
     * Get's the template from the resources folder and prepares it. <b>Call build Request before sending,
     * otherwise request parameters are empty</b>
     * @throws JDOMException
     * @throws IOException
     */
    public TripInfoRequest() throws JDOMException, IOException {
        super("/tripInfoRequest.xml");
    }

    /**
     * Fills in the template which the needed Parameters: Request Timestamp in UTC, Operating Day Ref and Journey Ref
     * @param operatingDayRef provided by TRIAS interface (e.g. through departure board)
     * @param journeyRef provided by TRIAS interface (e.g. through departure board)
     */
    public void buildRequest(String operatingDayRef, String journeyRef) {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Europe/Berlin"));

        findElementByName("RequestTimestamp").setText(SQLFormatTools.formatTrias(cal.getTime()));
        findElementByName("OperatingDayRef").setText(operatingDayRef);
        findElementByName("JourneyRef").setText(journeyRef);
    }
}
