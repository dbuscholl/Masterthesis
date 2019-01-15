package Network;


import Database.FormatTools;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.filter.ElementFilter;

import java.io.IOException;
import java.util.Calendar;
import java.util.TimeZone;

public class DepartureBoardRequest extends XMLDocument {

    public DepartureBoardRequest() throws JDOMException, IOException {
        super("/departureBoardRequest.xml");
    }

    public void buildRequest(String stopPointRef, String departureUTC) {
        Element root = document.getRootElement();

        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Europe/Berlin"));

        findElementByName("RequestTimestamp").setText(FormatTools.formatTrias(cal.getTime()));
        findElementByName("StopPointRef").setText(stopPointRef);
        findElementByName("DepArrTime").setText(departureUTC);
    }
}
