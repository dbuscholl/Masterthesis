package Processes;

import Database.Database;
import Database.FormatTools;
import Database.ScheduledTrip;
import Database.TripStop;
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

public class TripWorker extends Thread {
    private ScheduledTrip trip;
    private Logger log = Logger.getLogger(this.getClass().getName());
    private Namespace namespace = Namespace.getNamespace("http://www.vdv.de/trias");
    private TripInfoRequest tripInfoRequest;

    public TripWorker(ScheduledTrip trip) {
        this.trip = trip;
    }

    @Override
    public void run() {
        try {
            XMLDocument departureBoard = getDepartureBoardXml();

            Database database = new Database();
            ArrayList<TripStop> gtfsStops = database.getTripDetails(trip.getTrip_id());

            ArrayList<Element> stopEventResults = departureBoard.findElementsByName("StopEventResult");
            ArrayList<TripStop> triasStops;
            boolean foundInTrias = false;
            for (Element result : stopEventResults) {
                triasStops = getTripInfoFromDepartureBoardItem(result);
                if(triasStops == null) {
                    log.error("Error while creating Stops from TRIAS EKAP Result");
                    return;
                }

                boolean equal = TripStop.checkEquality(gtfsStops, triasStops);
                if (equal) {
                    log.debug(trip.getRoute_short_name() + ": " + trip.getTrip_headsign() + " was found in TRIAS! Departure GTFS: " + trip.getDeparture_time() + ", Departure TRIAS: " + triasStops.get(0).getDeparture_time());
                    foundInTrias = true;
                    break;
                } else {
                    log.debug(trip.getRoute_short_name() + ": " + trip.getTrip_headsign() + " was NOT found in TRIAS! Departure GTFS: " + trip.getDeparture_time() + ", Departure TRIAS: " + triasStops.get(0).getDeparture_time());
                }
            }

            if(foundInTrias) {
                //TODO: Start a Scheduler (TimerTask) which records the trip
            }
        } catch (JDOMException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private XMLDocument getDepartureBoardXml() throws IOException, JDOMException, ParseException {
        Connection c = new Connection();
        DepartureBoardRequest dbr = new DepartureBoardRequest();

        dbr.buildRequest(trip.getStop_id(), FormatTools.makeTimeForTrias(trip.getDeparture_time()));
        String departureBoardResult = c.sendPostXML(dbr.toString());
        return XMLDocument.documentFromString(departureBoardResult);
    }

    private ArrayList<TripStop> getTripInfoFromDepartureBoardItem(Element result) throws IOException, JDOMException, ParseException {
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

        return createTriasStopsFromResponse(tripInfo);
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
            log.error("Stopping analysis for Trip " + trip.getRoute_short_name() + ": " + trip.getTrip_headsign() + " because of errors");
            return triasStops;
        } catch (ParseException e) {
            return null;
        }
        return triasStops;
    }

}
