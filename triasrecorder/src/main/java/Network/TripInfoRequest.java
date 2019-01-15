package Network;

import Database.FormatTools;
import org.jdom2.JDOMException;

import java.io.IOException;
import java.util.Calendar;
import java.util.TimeZone;

public class TripInfoRequest extends XMLDocument {
    public TripInfoRequest() throws JDOMException, IOException {
        super("/tripInfoRequest.xml");
    }

    public void buildRequest(String operatingDayRef, String journeyRef) {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Europe/Berlin"));

        findElementByName("RequestTimestamp").setText(FormatTools.formatTrias(cal.getTime()));
        findElementByName("OperatingDayRef").setText(operatingDayRef);
        findElementByName("JourneyRef").setText(journeyRef);
    }
}
