package Processes;

import Database.Database;
import Database.FormatTools;
import Database.ScheduledTrip;
import Database.TripStop;
import Database.Delay;
import Network.Connection;
import Network.DepartureBoardRequest;
import Network.TripInfoRequest;
import Network.XMLDocument;
import org.apache.log4j.Logger;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.filter.ElementFilter;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;

public class TripWorker {
    private Logger log = Logger.getLogger(this.getClass().getName());

    private ScheduledTrip tripInfo;
    private ArrayList<TripStop> triasStops;
    private ArrayList<TripStop> gtfsStops;
    private ArrayList<Delay> delays;
    private TripInfoRequest tripInfoRequest;
    private boolean stopRecording = false;

    private Namespace namespace = Namespace.getNamespace("http://www.vdv.de/trias");

    public TripWorker(ScheduledTrip tripInfo) {
        this.tripInfo = tripInfo;
    }

    public void prepare() {
        try {
            XMLDocument departureBoard = getDepartureBoardXml();

            ArrayList<TripStop> gtfsStops = Database.getTripDetails(tripInfo.getTrip_id());

            ArrayList<Element> stopEventResults = departureBoard.findElementsByName("StopEventResult");
            ArrayList<TripStop> triasStops = null;
            boolean foundInTrias = false;
            for (Element result : stopEventResults) {
                XMLDocument response = getTripInfoFromDepartureBoardItem(result);
                triasStops = createTriasStopsFromResponse(response);
                if (triasStops == null) {
                    log.error("Error while creating Stops from TRIAS EKAP Result");
                    return;
                }

                boolean equal = TripStop.checkEquality(gtfsStops, triasStops);
                if (equal) {
                    log.debug(tripInfo.getRoute_short_name() + ": " + tripInfo.getTrip_headsign() + " was found in TRIAS! Departure GTFS: " + tripInfo.getDeparture_time() + ", Departure TRIAS: " + triasStops.get(0).getDeparture_time());
                    foundInTrias = true;
                    break;
                } else {
                    log.debug(tripInfo.getRoute_short_name() + ": " + tripInfo.getTrip_headsign() + " was NOT found in TRIAS! Departure GTFS: " + tripInfo.getDeparture_time() + ", Departure TRIAS: " + triasStops.get(0).getDeparture_time());
                }
            }

            if (foundInTrias) {
                this.triasStops = triasStops;
                this.gtfsStops = gtfsStops;
                delays = new ArrayList<Delay>();
            } else {
                log.error("Cannot record delay for :" + tripInfo.getRoute_short_name() + ": " + tripInfo.getTrip_headsign() + " because it was not found in TRIAS Real World");
                stopRecording = true;
            }
        } catch (JDOMException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void getNewDelay() throws IOException, JDOMException, ParseException {
        Connection c = new Connection();
        XMLDocument tripInfo = XMLDocument.documentFromString(c.sendPostXML(tripInfoRequest.toString()));
        delays.add(getDelayFromResponse(tripInfo));
        stopRecording = checkTripEnding(tripInfo);
    }

    public void addToDatabase() throws SQLException, ClassNotFoundException {
        // TODO: implement
    }

    private XMLDocument getDepartureBoardXml() throws IOException, JDOMException, ParseException {
        Connection c = new Connection();
        DepartureBoardRequest dbr = new DepartureBoardRequest();

        dbr.buildRequest(tripInfo.getStop_id(), FormatTools.makeTimeForTrias(tripInfo.getDeparture_time()));
        String departureBoardResult = c.sendPostXML(dbr.toString());
        return XMLDocument.documentFromString(departureBoardResult);
    }

    private XMLDocument getTripInfoFromDepartureBoardItem(Element result) throws IOException, JDOMException, ParseException {
        String journeyRef = "";
        String operatingDayRef = "";

        for (Element e : result.getDescendants(new ElementFilter("JourneyRef"))) {
            journeyRef = e.getTextNormalize();
        }
        for (Element e : result.getDescendants(new ElementFilter("OperatingDayRef"))) {
            operatingDayRef = e.getTextNormalize();
        }

        tripInfoRequest = new TripInfoRequest();
        tripInfoRequest.buildRequest(operatingDayRef, journeyRef);
        Connection c = new Connection();
        XMLDocument tripInfo = XMLDocument.documentFromString(c.sendPostXML(tripInfoRequest.toString()));

        return tripInfo;
    }

    private ArrayList<TripStop> createTriasStopsFromResponse(XMLDocument tripInfo) throws ParseException {
        ArrayList<Element> stopElements = new ArrayList<>();

        for (Element e : tripInfo.getDocument().getDescendants(new ElementFilter("PreviousCall"))) {
            stopElements.add(e);
        }
        for (Element e : tripInfo.getDocument().getDescendants(new ElementFilter("CurrentPosition"))) {
            stopElements.add(e);
        }
        for (Element e : tripInfo.getDocument().getDescendants(new ElementFilter("OnwardCall"))) {
            stopElements.add(e);
        }

        ArrayList<TripStop> triasStops = null;
        try {
            triasStops = FormatTools.xmlToTripStop(stopElements, namespace);
        } catch (NullPointerException e) {
            log.error("Stopping analysis for Trip " + this.tripInfo.getRoute_short_name() + ": " + this.tripInfo.getTrip_headsign() + " because of errors");
            return triasStops;
        } catch (ParseException e) {
            return null;
        }
        return triasStops;
    }

    private Delay getDelayFromResponse(XMLDocument tripInfo) throws ParseException {
        ArrayList<Element> stopElements = new ArrayList<>();

        for (Element e : tripInfo.getDocument().getDescendants(new ElementFilter("PreviousCall"))) {
            stopElements.add(e);
        }
        TripStop gtfsStop = null;
        for (int i = 0; i < gtfsStops.size(); i++) {
            TripStop t = gtfsStops.get(i);
            if (t.getStop_sequence() - 1 == i) {
                gtfsStop = t;
                break;
            }
        }

        TripStop triasStop = FormatTools.xmlToTripStop(stopElements.subList(stopElements.size() - 1, stopElements.size() - 1), namespace).get(0);
        Date timetabled = FormatTools.timeFormat.parse(triasStop.getArrival_time());
        Date estimated = FormatTools.timeFormat.parse(triasStop.getArrival_time_estimated());
        long seconds = (estimated.getTime() - timetabled.getTime()) / 1000;
        return new Delay(gtfsStop, Math.toIntExact(seconds));
    }

    private boolean checkTripEnding(XMLDocument tripInfo) {
        ArrayList<Element> stopElements = new ArrayList<>();

        for (Element e : tripInfo.getDocument().getDescendants(new ElementFilter("OnwardCall"))) {
            stopElements.add(e);
        }
        return stopElements.size() > 0;
    }

    public boolean isStopRecording() {
        return stopRecording;
    }
}
