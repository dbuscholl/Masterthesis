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

    public TripWorker(ScheduledTrip trip) {
        this.trip = trip;
    }

    @Override
    public void run() {
        try {
            Connection c = new Connection();
            DepartureBoardRequest dbr = new DepartureBoardRequest();

            dbr.buildRequest(trip.getStop_id(), FormatTools.makeTimeForTrias(trip.getDeparture_time()));
            String departureBoardResult = c.sendPostXML(dbr.toString());
            XMLDocument departureBoard = XMLDocument.documentFromString(departureBoardResult);

            Database database = new Database();
            ArrayList<TripStop> tripDetails = database.getTripDetails(trip.getTrip_id());

            ArrayList<Element> stopEventResults = departureBoard.findElementsByName("StopEventResult");
            for (Element result : stopEventResults) {
                String journeyRef = "";
                String operatingDayRef = "";

                for (Element e : result.getDescendants(new ElementFilter("JourneyRef"))) {
                    journeyRef = e.getTextNormalize();
                }
                for (Element e : result.getDescendants(new ElementFilter("OperatingDayRef"))) {
                    operatingDayRef = e.getTextNormalize();
                }

                TripInfoRequest tir = new TripInfoRequest();
                tir.buildRequest(operatingDayRef, journeyRef);
                c = new Connection();
                XMLDocument tripInfo = XMLDocument.documentFromString(c.sendPostXML(tir.toString()));
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

                ArrayList<TripStop> stops = null;
                try {
                    stops = FormatTools.xmlToTripStop(stopElements, namespace);
                } catch (NullPointerException e) {
                    log.error("Stopping analysis for Trip " + trip.getRoute_short_name() + ": " + trip.getTrip_headsign() + " because of errors");
                    return;
                }

                // TODO: Check if trip from TRIAS matches trip from Database
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
}
