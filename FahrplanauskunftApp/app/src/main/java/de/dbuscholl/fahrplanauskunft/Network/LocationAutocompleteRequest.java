package de.dbuscholl.fahrplanauskunft.Network;

import android.content.res.Resources;

import org.jdom2.JDOMException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.TimeZone;

import de.dbuscholl.fahrplanauskunft.FormatTools;
import de.dbuscholl.fahrplanauskunft.R;

public class LocationAutocompleteRequest extends XMLDocument {

    public LocationAutocompleteRequest(InputStream is) throws JDOMException, IOException {
        super(is);
    }

    public void buildRequest(String stop) {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Europe/Berlin"));

        findElementByName("RequestTimestamp").setText(FormatTools.formatTrias(cal.getTime()));
        findElementByName("LocationName").setText(stop);
    }
}
