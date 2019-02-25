package de.dbuscholl.fahrplanauskunft.network;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.filter.ElementFilter;

import java.io.IOException;
import java.util.ArrayList;

import de.dbuscholl.fahrplanauskunft.common.Constants;
import de.dbuscholl.fahrplanauskunft.network.entities.Service;
import de.dbuscholl.fahrplanauskunft.network.entities.StopPoint;
import de.dbuscholl.fahrplanauskunft.network.entities.Trip;
import de.dbuscholl.fahrplanauskunft.network.entities.Connection;
import de.dbuscholl.fahrplanauskunft.network.xml.XMLDocument;

public class TripInfoDownloadTask extends AsyncTask<String, Void, ArrayList<Connection>> {

    private static SuccessEvent successEvent;
    private ProgressDialog dialog;
    private Context context;
    private ArrayList<Connection> results;
    private String response = "";

    public TripInfoDownloadTask(Activity activity) {
        dialog = new ProgressDialog(activity);
    }

    @Override
    protected void onPreExecute() {
        dialog.setMessage("Suche nach Fahrten...");
        dialog.show();
    }


    @Override
    protected ArrayList<Connection> doInBackground(String... strings) {
        try {
            results = new ArrayList<>();
            Client c = new Client("http://efastatic.vvs.de/kleinanfrager/trias");
            String response = c.sendPostXML(strings[0]);
            this.response = response;
            XMLDocument xml = XMLDocument.documentFromString(response);

            for (Element e : xml.getDocument().getDescendants(new ElementFilter("Trip"))) {
                Connection t = getTripResult(e);
                ArrayList<Trip> legs = getTripLegs(e);
                t.setLegs(legs);
                results.add(t);
            }
            if(successEvent!=null) {
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

    @Override
    protected void onPostExecute(ArrayList<Connection> connections) {
        if (dialog.isShowing()) {
            dialog.dismiss();
        }
        super.onPostExecute(connections);
    }

    public void setContext(Context context) {
        this.context = context;
    }

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
                    Toast.makeText(context, "ContinuousLeg", Toast.LENGTH_LONG);
                    leg = new Trip();
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
            throw new NullPointerException("Name and Ref of Start is null");
        }
        if (stopPointRefEnd == null && stopNameEnd == null) {
            throw new NullPointerException("Name and Ref of End is null");
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

    private String getSubmode(Element mode) {
        String[] types = {"RailSubmode", "CoachSubmode", "MetroSubmode", "BusSubmode", "TramSubmode", "WaterSubmode", "AirSubmode", "TelecabinSubmode", "FunicularSubmode", "TaxiSubmode"};

        for (String s : types) {
            Element e = mode.getChild(s, Constants.NAMESPACE);
            if (e != null) {
                return e.getTextNormalize();
            }
        }
        return null;
    }

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

    public String getResponse() {
        return response;
    }


    public static void setOnSuccessEvent(SuccessEvent e) {
        successEvent = e;
    }

    public interface SuccessEvent {
        public void onSuccess(ArrayList<Connection> result);
    }
}
