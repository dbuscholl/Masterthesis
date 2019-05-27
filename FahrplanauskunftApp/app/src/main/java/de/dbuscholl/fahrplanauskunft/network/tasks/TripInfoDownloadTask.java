package de.dbuscholl.fahrplanauskunft.network.tasks;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.filter.ElementFilter;

import java.io.IOException;
import java.util.ArrayList;

import de.dbuscholl.fahrplanauskunft.common.Constants;
import de.dbuscholl.fahrplanauskunft.network.Client;
import de.dbuscholl.fahrplanauskunft.network.entities.Service;
import de.dbuscholl.fahrplanauskunft.network.entities.StopPoint;
import de.dbuscholl.fahrplanauskunft.network.entities.Trip;
import de.dbuscholl.fahrplanauskunft.network.entities.Connection;
import de.dbuscholl.fahrplanauskunft.network.xml.XMLDocument;

/**
 * This class is respoonsible for searching for connections by calling the corresponding TRIAS interface Verbindungsauskunft.
 * It also pareses the server result by creating an XMLDocument and extracting the right values from it. During the process
 * a progress dialog can be displayed. Also a callback can be set which is triggered when processing is done.
 */
public class TripInfoDownloadTask extends AsyncTask<String, Void, ArrayList<Connection>> {

    private SuccessEvent successEvent;
    private ProgressDialog dialog;
    private ArrayList<Connection> results;
    private static String response = null;
    private static String request = null;

    /**
     * empty constructor
     */
    public TripInfoDownloadTask() {
    }

    /**
     *  Use this constructor if you want to show a indefinite progress dialog
     * @param activity application context by this activity.
     */
    public TripInfoDownloadTask(Activity activity) {
        dialog = new ProgressDialog(activity);
    }

    /**
     * getter
     * @return the request as string which was send to the server.
     */
    public static String getRequest() {
        return request;
    }

    /**
     * Things that can be done on the main thread before execution. This is used to display the progress dialog in this case.
     */
    @Override
    protected void onPreExecute() {
        if (dialog != null) {
            dialog.setMessage(Constants.MSG_SEARCHINGTRIPS);
            dialog.show();
        }
    }


    /**
     * actual function which is called in a seperate thread to download the trip results. It also parses the result in background
     * otherwise we may have to much load on the main thread which blocks the GUI.
     * @param strings the string which will be send as request in this case
     * @return nothing because we have a callback for that and the function onPostExecute.
     */
    @Override
    protected ArrayList<Connection> doInBackground(String... strings) {
        try {
            results = new ArrayList<>();
            Client c = new Client(Constants.URL_TRIASAPI);
            request = strings[0];
            String response = c.sendPostXML(strings[0]);
            TripInfoDownloadTask.response = response;

            XMLDocument xml = XMLDocument.documentFromString(response);

            for (Element e : xml.getDocument().getDescendants(new ElementFilter("Trip"))) {
                Connection t = getTripResult(e);
                ArrayList<Trip> legs = getTripLegs(e);
                t.setLegs(legs);
                if (legs.size() > 0) {
                    results.add(t);
                }
            }
            if (successEvent != null) {
                successEvent.onSuccess(results);
            }
            return null;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JDOMException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * this will dismiss the progress dialog.
     * @param connections
     */
    @Override
    protected void onPostExecute(ArrayList<Connection> connections) {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
        super.onPostExecute(connections);
    }

    /**
     * this extracts a connection of the xml response. It is called for every response item because we get more than one
     * connection from the TRIAS interface.
     * @param e the element of the connection
     * @return Connection entity class.
     */
    private Connection getTripResult(Element e) {
        Connection t = new Connection();

        Element tripId = e.getChild("TripId", Constants.NAMESPACE);
        Element startTime = e.getChild("StartTime", Constants.NAMESPACE);
        Element endtime = e.getChild("EndTime", Constants.NAMESPACE);

        if (tripId != null) {
            t.setId(tripId.getTextNormalize());
        }
        if (startTime != null) {
            t.setStartTime(startTime.getTextNormalize());
        }
        if (endtime != null) {
            t.setEndTime(endtime.getTextNormalize());
        }
        return t;
    }

    /**
     * extracts all trip legs from the connection xml representation. Calls the corresponding function which extract the
     * actual leg. This function only decides if this is a timed or interchange leg and adds them into the arraylist.
     * @param xml xml element of the legs
     * @return ArrayList of Trips containing all the legs that take place during the connection.
     */
    private ArrayList<Trip> getTripLegs(Element xml) {
        ArrayList<Trip> triplegs = new ArrayList<>();


        for (Element e : xml.getDescendants(new ElementFilter("TripLeg"))) {
            int legId = 1;
            Element timedLeg = e.getChild("TimedLeg", Constants.NAMESPACE);
            Element interchangeLeg = e.getChild("InterchangeLeg", Constants.NAMESPACE);
            Element continuousLeg = e.getChild("ContinuousLeg", Constants.NAMESPACE);

            try {
                Trip leg = null;
                if (timedLeg != null) {
                    leg = getTimedTrip(legId, timedLeg);
                } else if (interchangeLeg != null) {
                    leg = getInterchangeTrip(legId, interchangeLeg);
                } else if (continuousLeg != null) {
                    continue;
                }
                if (leg != null) {
                    triplegs.add(leg);
                    legId++;
                }
            } catch (NullPointerException exception) {
                Log.e(getClass().getName(), exception.getMessage(), exception);
                Log.e(getClass().getName(), xml.toString());
                return null;
            }
        }

        return triplegs;
    }

    /**
     * this is only called if the element is an interchange. It extracts all information and creates a trip leg of it which
     * can be inserted at the right position inside the arraylist. This is called by getTripLegs
     * @param legId
     * @param interchangeElement
     * @return
     */
    private Trip getInterchangeTrip(int legId, Element interchangeElement) {
        Trip t = new Trip();
        t.setLegId(legId);
        t.setType(Trip.TripType.INTERCHANGE);

        Element interchangeMode = interchangeElement.getChild("InterchangeMode", Constants.NAMESPACE);
        Element stopPointRefStart = interchangeElement.getChild("LegStart", Constants.NAMESPACE).getChild("StopPointRef", Constants.NAMESPACE);
        Element stopNameStart = interchangeElement.getChild("LegStart", Constants.NAMESPACE).getChild("LocationName", Constants.NAMESPACE);
        Element stopPointRefEnd = interchangeElement.getChild("LegEnd", Constants.NAMESPACE).getChild("StopPointRef", Constants.NAMESPACE);
        Element stopNameEnd = interchangeElement.getChild("LegEnd", Constants.NAMESPACE).getChild("LocationName", Constants.NAMESPACE);
        Element startTime = interchangeElement.getChild("TimeWindowStart", Constants.NAMESPACE);
        Element endTime = interchangeElement.getChild("TimeWindowStart", Constants.NAMESPACE);

        if (stopPointRefStart == null && stopNameStart == null) {
            throw new NullPointerException(Constants.ERRORMSG_TRIAS_NOREFS);
        }
        if (stopPointRefEnd == null && stopNameEnd == null) {
            throw new NullPointerException(Constants.ERRORMSG_TRIAS_NOREFS);
        }

        StopPoint start = new StopPoint();
        if (stopPointRefStart != null) {
            start.setRef(stopPointRefStart.getTextNormalize());
        }
        if (stopNameStart != null) {
            start.setName(stopNameStart.getChildTextNormalize("Text", Constants.NAMESPACE));
        }
        if (startTime != null) {
            start.setDepartureTime(startTime.getTextNormalize());
        }
        t.setBoarding(start);

        StopPoint end = new StopPoint();
        if (stopPointRefEnd != null) {
            end.setRef(stopPointRefEnd.getTextNormalize());
        }
        if (stopNameEnd != null) {
            end.setName(stopNameEnd.getChildTextNormalize("Text", Constants.NAMESPACE));
        }
        if (endTime != null) {
            end.setArrivalTime(endTime.getTextNormalize());
        }
        t.setAlighting(end);

        t.setInterchangeType(interchangeMode.getTextNormalize());

        return t;
    }

    /**
     * extracts everything which has to do with a timed trip leg. As the stops and the service have their own extraction
     * method this is only a kind of coordinator for extraction.
     * @param legId number of the leg inside the arraylist to be inserted at the right place
     * @param timedLeg the xml representation of the timed leg element from which the infos are being extracted.
     * @return Trip entity class.
     */
    private Trip getTimedTrip(int legId, Element timedLeg) {
        Trip t = new Trip();
        t.setLegId(legId);
        t.setType(Trip.TripType.TIMED);
        Element boardingElement = timedLeg.getChild("LegBoard", Constants.NAMESPACE);

        // boarding
        StopPoint boarding = getStopPoint(boardingElement, 1);
        t.setBoarding(boarding);

        // intermediates
        int position = 2;
        ArrayList<StopPoint> intermediates = new ArrayList<>();
        for (Element intermediate : timedLeg.getDescendants(new ElementFilter("LegIntermediates"))) {
            StopPoint stop = getStopPoint(intermediate, position);
            intermediates.add(stop);
            position++;
        }
        t.setIntermediates(intermediates);

        // alighting
        StopPoint alighting = getStopPoint(timedLeg.getChild("LegAlight", Constants.NAMESPACE), position);
        t.setAlighting(alighting);

        // service info
        Element serviceItem = timedLeg.getChild("Service", Constants.NAMESPACE);
        Service service = getServiceInfo(serviceItem);
        t.setService(service);

        return t;
    }

    /**
     * extracts all service information of the corresponding xml element.
     * @param serviceElement the xml element of which the infos should be extracted.
     * @return the entity class service with all information.
     */
    private Service getServiceInfo(Element serviceElement) {
        Service service = new Service();

        Element operatingDayRef = serviceElement.getChild("OperatingDayRef", Constants.NAMESPACE);
        Element journeyRef = serviceElement.getChild("JourneyRef", Constants.NAMESPACE);
        Element lineRef = serviceElement.getChild("LineRef", Constants.NAMESPACE);
        Element mode = serviceElement.getChild("Mode", Constants.NAMESPACE);
        Element publishedLineName = serviceElement.getChild("PublishedLineName", Constants.NAMESPACE);
        Element routeDescription = serviceElement.getChild("RouteDescription", Constants.NAMESPACE);
        Element destinationText = serviceElement.getChild("DestinationText", Constants.NAMESPACE);

        service.setOperatingDayRef(operatingDayRef.getTextNormalize());
        service.setJourneyRef(journeyRef.getTextNormalize());
        service.setLineRef(lineRef.getTextNormalize());
        service.setLineName(publishedLineName.getChildTextNormalize("Text", Constants.NAMESPACE));

        // railtype
        Element modeName = mode.getChild("Name", Constants.NAMESPACE);
        if (modeName != null) {
            service.setRailName(modeName.getChild("Text", Constants.NAMESPACE).getTextNormalize());
        }
        service.setRailType(getSubmode(mode));

        if (routeDescription != null) {
            service.setRoute(routeDescription.getChildTextNormalize("RouteDescription", Constants.NAMESPACE));
        }
        if (destinationText != null) {
            service.setDesitnation(destinationText.getChild("Text", Constants.NAMESPACE).getTextNormalize());
        }

        return service;
    }

    /**
     * detemines the submode of a railtype so we can display the right vehicle type
     * @param mode element where the information might be stored
     * @return the String value of the type
     */
    private String getSubmode(Element mode) {
        for (String s : Constants.TRIAS_SUBPMODE_TYPES) {
            Element e = mode.getChild(s, Constants.NAMESPACE);
            if (e != null) {
                return e.getTextNormalize();
            }
        }
        return null;
    }

    /**
     * extracts all information about a stop from the TRIAS result and creates an entity class of it.
     * @param timedLegItem the item of which the infos should be extracted from.
     * @param position the position inside the leg to correctly insert it into the array
     * @return StopPoint entity class containing all extracted info.
     */
    private StopPoint getStopPoint(Element timedLegItem, int position) {
        StopPoint stop = new StopPoint();

        // getting all elements
        Element ref = timedLegItem.getChild("StopPointRef", Constants.NAMESPACE);
        Element name = timedLegItem.getChild("StopPointName", Constants.NAMESPACE).getChild("Text", Constants.NAMESPACE);
        Element bay = timedLegItem.getChild("PlannedBay", Constants.NAMESPACE);
        Element arrival = timedLegItem.getChild("ServiceArrival", Constants.NAMESPACE);
        Element departure = timedLegItem.getChild("ServiceDeparture", Constants.NAMESPACE);
        Element stopSeqNumber = timedLegItem.getChild("StopSeqNumber", Constants.NAMESPACE);

        stop.setRef(ref.getTextNormalize());
        stop.setName(name.getTextNormalize());

        // handling times
        if (arrival == null && departure == null) {
            throw new NullPointerException("Stop has neither Arrival nor Departure!");
        }

        if (arrival != null) {
            Element timetabledTime = arrival.getChild("TimetabledTime", Constants.NAMESPACE);
            Element estimatedTime = arrival.getChild("EstimatedTime", Constants.NAMESPACE);
            if (timetabledTime != null) {
                stop.setArrivalTime(timetabledTime.getTextNormalize());
            }
            if (estimatedTime != null) {
                stop.setArrivalTimeEstimated(estimatedTime.getTextNormalize());
            }
        }

        if (departure != null) {
            Element timetabledTime = departure.getChild("TimetabledTime", Constants.NAMESPACE);
            Element estimatedTime = departure.getChild("EstimatedTime", Constants.NAMESPACE);
            if (timetabledTime != null) {
                stop.setDepartureTime(timetabledTime.getTextNormalize());
            }
            if (estimatedTime != null) {
                stop.setDepartureTimeEstimated(estimatedTime.getTextNormalize());
            }
        }

        // handling bay
        if (bay != null) {
            stop.setBay(bay.getChild("Text", Constants.NAMESPACE).getTextNormalize());
        }

        if (position <= 0) {
            if (stopSeqNumber != null) {
                try {
                    stop.setPosition(Integer.parseInt(stopSeqNumber.getTextNormalize()));
                } catch (NumberFormatException e) {
                    stop.setPosition(0);
                }
            }
        } else {
            stop.setPosition(position);
        }

        return stop;
    }

    /**
     * getter
     * @return the response which was returned by the server as string.
     */
    public String getResponse() {
        return response;
    }

    /**
     * A callback event which is triggered when response parsed successfully
     * @param e function to be called
     */
    public void setOnSuccessEvent(SuccessEvent e) {
        successEvent = e;
    }

    /**
     * Callback interface which is triggered when the server returned it's response and the client parsed all items
     * successfully.
     */
    public interface SuccessEvent {
        public void onSuccess(ArrayList<Connection> result);
    }
}
