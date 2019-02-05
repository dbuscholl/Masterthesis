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
import com.mysql.jdbc.util.TimezoneDump;
import org.apache.log4j.Logger;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.filter.ElementFilter;

import java.io.IOException;
import java.sql.SQLException;
import java.text.Normalizer;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;

public class TripWorker {
    private Logger log = Logger.getLogger(this.getClass().getName());

    private ScheduledTrip gtfsTripInfo;
    private ArrayList<TripStop> triasStops;
    private ArrayList<TripStop> gtfsStops;
    private ArrayList<Delay> delays;
    private TripInfoRequest tripInfoRequest;
    private boolean stopRecording = false;
    private boolean brokenWorker = false;

    private Namespace namespace = Namespace.getNamespace("http://www.vdv.de/trias");
    private Date lastDelayCheck;

    public TripWorker(ScheduledTrip tripInfo) {
        this.gtfsTripInfo = tripInfo;
        lastDelayCheck = new Date(0);
    }

    public void prepare() {
        try {
            XMLDocument departureBoard = getDepartureBoardXml();
            if (checkForError(departureBoard)) {
                return;
            }

            ArrayList<TripStop> gtfsStops = Database.getTripDetails(gtfsTripInfo.getTrip_id());

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
                    foundInTrias = true;
                    //TODO: gtfsTripInfo TRIAS Edition from Result
                    break;
                } else {
                    log.debug(getFriendlyName() + " was NOT found in TRIAS! Departure GTFS: " + gtfsTripInfo.getStop_name() + ", " + gtfsTripInfo.getDeparture_time() + ", Departure TRIAS: " + triasStops.get(0).getStop_name() + ", " + triasStops.get(0).getDeparture_time());
                }
            }

            if (foundInTrias) {
                this.triasStops = triasStops;
                this.gtfsStops = gtfsStops;
                delays = new ArrayList<Delay>();
            } else {
                log.warn("Cannot record delay for " + getFriendlyName() + " because it was not found in TRIAS Real World");
                brokenWorker = true;
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
        Delay d = getDelayFromResponse(tripInfo);
        if (d != null) {
            delays.add(d);
        }
        stopRecording = checkTripEnding(tripInfo);
    }

    public void addToDatabase() throws SQLException, ClassNotFoundException {
        if (delays.size() > 0) {
            Database.addDelays(gtfsTripInfo, delays);
        }
    }

    private XMLDocument getDepartureBoardXml() throws IOException, JDOMException, ParseException {
        Connection c = new Connection();
        DepartureBoardRequest dbr = new DepartureBoardRequest();

        dbr.buildRequest(gtfsTripInfo.getStop_id(), FormatTools.makeTimeForTrias(gtfsTripInfo.getDeparture_time()));
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
            log.error("Stopping analysis for Trip " + this.gtfsTripInfo.getRoute_short_name() + ": " + this.gtfsTripInfo.getTrip_headsign() + " because of errors");
            return triasStops;
        } catch (ParseException e) {
            return null;
        }
        return triasStops;
    }

    public boolean checkForError(XMLDocument document) {
        for (Element e : document.getDocument().getDescendants(new ElementFilter("ErrorMessage"))) {
            String text = e.getChild("Text", namespace).getTextNormalize();
            switch (text) {
                case "STOPEVENT_LOCATIONUNSERVED":
                    log.error("Die Haltestelle " + gtfsTripInfo.getStop_name() + "  wird überhaupt nicht von öffentlichen Verkehrsmitteln bedient.");
                    return true;
                case "STOPEVENT_DATEOUTOFRANGE":
                    log.error("Für das angefragte Datum liegen keine Fahrplandaten vor");
                    return true;
                case "STOPEVENT_LOCATIONUNKNOWN":
                    log.error("Die Haltestelle " + gtfsTripInfo.getStop_name() + "  ist unbekannt.");
                    return true;
                case "STOPEVENT_NOEVENTFOUND":
                    log.error("Im fraglichen Zeitraum wurde keine Abfahrt/Ankunft unter Einhaltung der gegebenen Optionen gefunden.");
                    return true;
            }
        }
        return false;
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

        TripStop triasStop = FormatTools.xmlToTripStop(stopElements.subList(stopElements.size() - 1, stopElements.size()), namespace).get(0);
        if (triasStop.getArrival_time_estimated() == null) {
            lastDelayCheck = new Date();
            log.debug(getFriendlyName() + " has still no realtime");
            return null;
        } else {
            Date timetabled = FormatTools.timeFormat.parse(triasStop.getArrival_time());
            Date estimated = FormatTools.timeFormat.parse(triasStop.getArrival_time_estimated());
            long seconds = (estimated.getTime() - timetabled.getTime()) / 1000;
            return new Delay(gtfsStop, Math.toIntExact(seconds));
        }
    }

    private boolean checkTripEnding(XMLDocument tripInfo) {
        ArrayList<Element> stopElements = new ArrayList<>();

        for (Element e : tripInfo.getDocument().getDescendants(new ElementFilter("OnwardCall"))) {
            stopElements.add(e);
        }
        return stopElements.size() < 1;
    }

    public Date getStartDate() throws ParseException {
        String time = gtfsTripInfo.getDeparture_time().equals("") ? gtfsTripInfo.getArrival_time() : gtfsTripInfo.getDeparture_time();
        Date now = new Date();
        String departureString = FormatTools.sqlDateFormat.format(now) + " " + time;
        FormatTools.sqlDatetimeFormat.setTimeZone(TimeZone.getTimeZone("Europe/Berlin"));
        Date departure = FormatTools.sqlDatetimeFormat.parse(departureString);
        return departure;
    }

    public boolean isMoreThanAfterDeparture(int seconds) throws ParseException {
        return (new Date().getTime() - getStartDate().getTime()) / 1000 > seconds;
    }

    public boolean isDeparted() throws ParseException {
        return isMoreThanAfterDeparture(0);
    }

    public boolean isMoreThanAfterLastDelay(int seconds) {
        try {
            if (delays.isEmpty()) {
                Date now = new Date();
                return (now.getTime() - lastDelayCheck.getTime()) / 1000 > seconds;
            }
        } catch (NullPointerException e) {
            log.error("");
        }

        Date last = Date.from(delays.get(delays.size() - 1).getTimestamp().atZone(ZoneId.of("Europe/Berlin")).toInstant());
        return (new Date().getTime() - last.getTime()) / 1000 > seconds;
    }

    public String getFriendlyName() {
        return gtfsTripInfo.getFriendlyName();
    }

    public boolean isBrokenWorker() {
        return brokenWorker;
    }

    public boolean isStopRecording() {
        return stopRecording;
    }

    public ScheduledTrip getGtfsTripInfo() {
        return gtfsTripInfo;
    }

    public ArrayList<Delay> getDelays() {
        return delays;
    }
}
