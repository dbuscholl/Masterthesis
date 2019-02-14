package Network;

import Database.Entities.TripStop;
import Database.SQLFormatTools;
import org.apache.log4j.Logger;
import org.jdom2.Element;
import org.jdom2.Namespace;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Little important helper class providing usefull methods when working with XML Responses from TRIAS
 */
public class XMLFormatTools {
    private static Logger log = Logger.getLogger(XMLFormatTools.class);

    /**
     * Creates an Array of TripStops from a TripInfo Result of TRIAS interface. <p><b>Note:</b> Arrival and Departure are
     * automatically filled with each other if one of them is null. This happens very often, when trips don't pause on a
     * stop!</p>
     * <p><b>Note:</b> Estimated times can be null when no realtime data is provided</p>
     *
     * @param stopElements must be DOM Elements with the tag names <i>PreviousCall</i>, <i>CurrentPosition</i> or <i>OnwardCall<i/>
     * @param namespace    For identifying elements (DTD)
     * @return ArrayList of TripStops ordered by StopSequence and containing all stops for a trip
     * @throws NullPointerException
     * @throws ParseException
     */
    public static ArrayList<TripStop> xmlToTripStop(List<Element> stopElements, Namespace namespace) throws NullPointerException, ParseException {
        ArrayList<TripStop> tsarray = new ArrayList<>();

        for (int i = 0; i < stopElements.size(); i++) {
            Element e = stopElements.get(i);
            TripStop ts = new TripStop();
            boolean noRealtime = false;

            String stopId = e.getChild("StopPointRef", namespace).getTextNormalize();
            String stopName = e.getChild("StopPointName", namespace).getChild("Text", namespace).getTextNormalize();

            String arrivalTime = null;
            String arrivalTimeEstimated = null;
            try {
                arrivalTime = e.getChild("ServiceArrival", namespace).getChild("TimetabledTime", namespace).getTextNormalize();
                arrivalTimeEstimated = e.getChild("ServiceArrival", namespace).getChild("EstimatedTime", namespace).getTextNormalize();
            } catch (NullPointerException exception) {
            }
            String departureTime = null;
            String departureTimeEstimated = null;
            try {
                departureTime = e.getChild("ServiceDeparture", namespace).getChild("TimetabledTime", namespace).getTextNormalize();
                departureTimeEstimated = e.getChild("ServiceDeparture", namespace).getChild("EstimatedTime", namespace).getTextNormalize();
            } catch (NullPointerException exception) {
            }

            if (arrivalTime == null && departureTime == null) {
                log.error("Neither Arrival time nor departure time defined for " + stopName + " - " + stopId);
                //throw new NullPointerException("Neither Arrival time nor departure time defined for " + stopName + " - " + stopId);
            }

            if (arrivalTimeEstimated == null && departureTimeEstimated == null) {
                noRealtime = true;
            }

            ts.setStop_id(stopId);
            ts.setStop_name(stopName);
            ts.setArrival_time(arrivalTime == null ? departureTime : arrivalTime);
            ts.setDeparture_time(departureTime == null ? arrivalTime : departureTime);
            ts.setArrival_time_estimated(arrivalTimeEstimated == null ? departureTimeEstimated : arrivalTimeEstimated);
            ts.setDeparture_time_estimated(departureTimeEstimated == null ? arrivalTimeEstimated : departureTimeEstimated);
            ts.setStop_sequence(i + 1);
            ts.setType(TripStop.Type.TRIAS);

            ts.setArrival_time(SQLFormatTools.makeTimeForGtfs(ts.getArrival_time()));
            ts.setDeparture_time(SQLFormatTools.makeTimeForGtfs(ts.getDeparture_time()));
            if (noRealtime) {
                ts.setArrival_time_estimated(null);
                ts.setDeparture_time_estimated(null);
            } else {
                ts.setArrival_time_estimated(SQLFormatTools.makeTimeForGtfs(ts.getArrival_time_estimated()));
                ts.setDeparture_time_estimated(SQLFormatTools.makeTimeForGtfs(ts.getDeparture_time_estimated()));
            }

            tsarray.add(ts);
        }
        return tsarray;
    }
}
