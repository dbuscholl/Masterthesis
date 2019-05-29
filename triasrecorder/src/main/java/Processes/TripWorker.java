package Processes;

import Database.DataSource;
import Database.SQLFormatTools;
import Database.Entities.ScheduledTrip;
import Database.Entities.TripStop;
import Database.Entities.Delay;
import Network.*;
import org.apache.log4j.Logger;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.filter.ElementFilter;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

/**
 * This class takes care of all work which has to be done on a single trip.
 * <p><b>Preparation:</b> First we try to find the trips inside the TRIAS interface. All trips which are not there will
 * be marked as "broken" by checking equality between TRIAS TripInfo and GTFS Data.</p>
 * <p><b>Delay Insertion:</b> When we want to add a new Delay we first query the TRIAS Interface. If we can't find realtime
 * data then we don't add a new Delay. Else we get the last "Previous Call" which is the stop from which the vehicles departed last.
 * By subtracting the EstimatedTime with the Timetabled Time we get the amount of seconds for the Delay.</p>
 * <p><b>Finalizing: </b> If we reached our last stop we give the Delays to our Database which inserts them!</p>
 */
public class TripWorker {
    private Logger log = Logger.getLogger(this.getClass().getName());

    private ScheduledTrip gtfsTripInfo;
    private ArrayList<TripStop> triasStops = new ArrayList<>();
    private ArrayList<TripStop> gtfsStops = new ArrayList<>();
    private ArrayList<Delay> delays;
    private TripInfoRequest tripInfoRequest;
    private boolean stopRecording = false;
    private boolean brokenWorker = false;

    private Namespace namespace = Namespace.getNamespace("http://www.vdv.de/trias");
    private Date lastDelayCheck;

    /**
     * Constructor
     *
     * @param tripInfo taking the Trip which should be recorded
     */
    public TripWorker(ScheduledTrip tripInfo) {
        this.gtfsTripInfo = tripInfo;
        lastDelayCheck = new Date(0);
    }

    /**
     * <p><b>Preparation:</b> First we try to find the trips inside the TRIAS interface. All trips which are not there will
     * be marked as "broken" by checking equality between TRIAS TripInfo and GTFS Data.</p>
     */
    public void prepare() {
        try {
            XMLDocument departureBoard = getDepartureBoardXml();
            if (checkForError(departureBoard)) {
                return;
            }

            // obtain more Info about the trip
            ArrayList<TripStop> gtfsStops = DataSource.getTripDetails(gtfsTripInfo.getTrip_id());

            ArrayList<Element> stopEventResults = departureBoard.findElementsByName("StopEventResult");
            ArrayList<TripStop> triasStops = null;
            boolean foundInTrias = false;

            // get TripInfo by finding trip inside the departure board of the stop. Check every departure of the stop
            StringBuilder errorstring = new StringBuilder("");
            for (int i = 0; i < stopEventResults.size(); i++) {
                Element result = stopEventResults.get(i);

                XMLDocument response = getTripInfoFromDepartureBoardItem(result);
                // transforming into comparable data structure
                triasStops = createTriasStopsFromResponse(response);
                if (triasStops == null) {
                    log.error("Error while creating Stops from TRIAS EKAP Result");
                    log.debug(response);
                    brokenWorker = true;
                    return;
                }

                // checking equality
                boolean equal = TripStop.checkEquality(gtfsStops, triasStops);
                if (equal) {
                    foundInTrias = true;
                    //TODO: gtfsTripInfo TRIAS Edition from Result
                    break;
                } else {
                    try {
                        log.debug(getFriendlyName() + " was NOT found in TRIAS! Departure GTFS: " + gtfsTripInfo.getStop_name() + ", " + gtfsTripInfo.getDeparture_time() + ", Departure TRIAS: " + triasStops.get(0).getStop_name() + ", " + triasStops.get(0).getDeparture_time());
                    } catch (IndexOutOfBoundsException e) {
                        log.debug("L95 IndexOutOfBoundsException: " + e.getMessage());
                        log.debug(response);
                    }
                }
            }

            if (foundInTrias) {
                this.triasStops = triasStops;
                this.gtfsStops = gtfsStops;
                delays = new ArrayList<>();
            } else {
                log.warn("Cannot record delay for " + getFriendlyName() + " because it was not found in TRIAS Real World");
                log.debug(errorstring.toString());
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

    /**
     * Getting new delay information for the Trip and adding it into our Delays array
     *
     * @throws IOException
     * @throws JDOMException
     * @throws ParseException
     */
    public void getNewDelay() throws IOException, JDOMException, ParseException {
        TriasConnection c = new TriasConnection();
        XMLDocument tripInfo = XMLDocument.documentFromString(c.sendPostXML(tripInfoRequest.toString()));
        Delay d = null;
        try {
            d = getDelayFromResponse(tripInfo);
        } catch (NullPointerException e) {
            lastDelayCheck = new Date();
        }
        if (d != null) {
            delays.add(d);
        }
        stopRecording = checkTripEnding(tripInfo);
    }

    /**
     * adding all Delays collected during recording into our database
     *
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public void addDelaysToDatabase() throws SQLException, ClassNotFoundException {
        if (delays.size() > 0) {
            DataSource.addDelays(gtfsTripInfo, delays);
        }
    }

    /**
     * firing a Request to the TRIAS interface to get the departure board for the first stop of the trip so we can match
     * the trips together
     *
     * @return
     * @throws IOException
     * @throws JDOMException
     * @throws ParseException
     */
    private XMLDocument getDepartureBoardXml() throws IOException, JDOMException, ParseException {
        TriasConnection c = new TriasConnection();
        DepartureBoardRequest dbr = new DepartureBoardRequest();

        dbr.buildRequest(gtfsTripInfo.getStop_id(), SQLFormatTools.makeTimeForTrias(gtfsTripInfo.getDeparture_time()));
        String departureBoardResult = c.sendPostXML(dbr.toString());
        return XMLDocument.documentFromString(departureBoardResult);
    }

    /**
     * Firing a Reuqest to TRIAS interface to get the trip details for a trip from the departure board item
     *
     * @param result departure board request result item. A single one, not all of them!
     * @return
     * @throws IOException
     * @throws JDOMException
     * @throws ParseException
     */
    private XMLDocument getTripInfoFromDepartureBoardItem(Element result) throws IOException, JDOMException, ParseException {
        String journeyRef = "";
        String operatingDayRef = "";

        // extracting parameters from the departure board item
        for (Element e : result.getDescendants(new ElementFilter("JourneyRef"))) {
            journeyRef = e.getTextNormalize();
        }
        for (Element e : result.getDescendants(new ElementFilter("OperatingDayRef"))) {
            operatingDayRef = e.getTextNormalize();
        }

        tripInfoRequest = new TripInfoRequest();
        tripInfoRequest.buildRequest(operatingDayRef, journeyRef);
        TriasConnection c = new TriasConnection();
        XMLDocument tripInfo = XMLDocument.documentFromString(c.sendPostXML(tripInfoRequest.toString()));

        return tripInfo;
    }

    /**
     * Building the TripStop Array for better comparison and legibility
     *
     * @param tripInfo trip info request result as xml document to be parsed
     * @return TripStop Array containing all TripStops ordered by stop sequence
     * @throws ParseException
     */
    private ArrayList<TripStop> createTriasStopsFromResponse(XMLDocument tripInfo) throws ParseException {
        ArrayList<Element> stopElements = new ArrayList<>();

        // these tags implicate a stop
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
            triasStops = XMLFormatTools.xmlToTripStop(stopElements, namespace);
        } catch (NullPointerException e) {
            log.error("Stopping analysis for Trip " + this.gtfsTripInfo.getRoute_short_name() + ": " + this.gtfsTripInfo.getTrip_headsign() + " because of errors");
            return triasStops;
        } catch (NumberFormatException e) {
            log.warn("Wrong number format (" + e.getMessage() + ") for " + gtfsTripInfo.getFriendlyName(), e);
            log.debug(tripInfo.toString());
            return null;
        }
        return triasStops;
    }

    /**
     * checks the result of the Departure Board for errors
     *
     * @param document departure board request result xml document
     * @return true if error occured, false if not
     */
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

    /**
     * Extracts the part of the xml where the delay is stored and creates new Delay object with this information
     *
     * @param tripInfo trias trip info request result as xml document
     * @return Delay item
     * @throws ParseException
     */
    private Delay getDelayFromResponse(XMLDocument tripInfo) {
        ArrayList<Element> stopElements = new ArrayList<>();

        // only look at previous calls
        for (Element e : tripInfo.getDocument().getDescendants(new ElementFilter("PreviousCall"))) {
            stopElements.add(e);
        }

        if (stopElements.size() < 1) {
            return null;
        }
        // get last item as TripStop for better legibility
        TripStop triasStop = null;
        try {
            if (!stopElements.isEmpty()) {
                triasStop = XMLFormatTools.xmlToTripStop(stopElements.subList(stopElements.size() - 1, stopElements.size()), namespace).get(0);
            } else {
                brokenWorker = true;
                log.error("Error getting Delay from result. No previous stops defined.");
                log.debug(printTripInfo(gtfsTripInfo));
                log.debug("GTFS Stops: \n" + printGtfsTour());
                log.debug("XML: \n" + tripInfo.toString());
            }
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            log.error(e.getMessage());
            log.debug(printTripInfo(gtfsTripInfo));
            log.debug("GTFS Stops: \n" + printGtfsTour());
            log.debug("XML: \n" + tripInfo.toString());
            brokenWorker = true;
            return null;
        }

        // if no realtime provided
        if (triasStop.getArrival_time_estimated() == null) {
            lastDelayCheck = new Date();
            log.debug(getFriendlyName() + " has still no realtime");
            return null;
        } else {
            TripStop gtfsStop = null;
            for (TripStop t : gtfsStops) {
                if (t.getStop_id().equals(triasStop.getStop_id())) {
                    gtfsStop = t;
                    break;
                }
            }
            // if stop was not found in gtfs datastore
            if (gtfsStop == null) {
                log.debug(getFriendlyName() + " could not match to corresponding GTFS-Station");
                return null;
            }

            // parse utc timestamps and subtract them
            try {
                Date timetabled = SQLFormatTools.timeFormat.parse(triasStop.getArrival_time());
                Date estimated = SQLFormatTools.timeFormat.parse(triasStop.getArrival_time_estimated());
                long seconds = (estimated.getTime() - timetabled.getTime()) / 1000;
                int exact = Math.toIntExact(seconds);
                return new Delay(gtfsStop, exact);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                return null;
            }
        }
    }

    /**
     * check wheter the last stop was arrived or not. <b>Note:</b> This must not be the actual ending of the trip.
     * It is entirely possible that the vehicle didn't reach the last stop yet. This depends on the implementation inside TRIAS.
     * So therefore it could be that it only returns whether the timetabled time of the stop is passed!
     *
     * @param tripInfo trias trip info request result as xml document
     * @return true wheter trip is done, false if still on tour or not yet departed
     */
    private boolean checkTripEnding(XMLDocument tripInfo) {
        ArrayList<Element> stopElements = new ArrayList<>();

        for (Element e : tripInfo.getDocument().getDescendants(new ElementFilter("OnwardCall"))) {
            stopElements.add(e);
        }

        if (stopElements.size() == 0) {
            int i = 0;
        }
        return stopElements.size() < 1;
    }

    /**
     * @return the startdate of a trip as readable datetime string in Europe/Berlin Timezone
     * @throws ParseException
     */
    public Date getStartDate() {
        try {
            String time = gtfsTripInfo.getDeparture_time().equals("") ? gtfsTripInfo.getArrival_time() : gtfsTripInfo.getDeparture_time();
            Date now = new Date();
            String departureString = SQLFormatTools.sqlDateFormat.format(now) + " " + time;
            SQLFormatTools.sqlDatetimeFormat.setTimeZone(TimeZone.getTimeZone("Europe/Berlin"));
            if (time.equals("") || departureString.equals("")) {
                int i = 0;
            }
            Date departure = SQLFormatTools.sqlDatetimeFormat.parse(departureString);
            return departure;
        } catch (ParseException e) {
            return null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Checks if the amount of given seconds have passed since departure
     *
     * @param seconds
     * @return true if current time is later than x seconds after departure, false if not
     * @throws ParseException
     */
    public boolean isMoreThanAfterDeparture(int seconds) {
        try {
            return (new Date().getTime() - getStartDate().getTime()) / 1000 > seconds;
        } catch (NullPointerException e) {
            log.error(e.getMessage());
            try {
                log.debug(getStartDate() + " - " + seconds + " - " + (new Date().getTime() - getStartDate().getTime()) / 1000);
            } catch (NullPointerException ex) {
                brokenWorker = true;
                return false;
            }
            brokenWorker = true;
            return false;
        }
    }

    /**
     * checks wheter the trip has already startet (time > departure time)
     *
     * @return true or false
     * @throws ParseException
     */
    public boolean isDeparted() {
        return isMoreThanAfterDeparture(0);
    }

    /**
     * Checks if the amount of given seconds have passed since last delay insertion
     *
     * @param seconds
     * @return true if time is later than x seconds after last delay insertion or no delay recorded yet, false if not
     */
    public boolean isMoreThanAfterLastDelay(int seconds) {
        try {
            if (delays.isEmpty()) {
                Date now = new Date();
                return (now.getTime() - lastDelayCheck.getTime()) / 1000 > seconds;
            }
        } catch (NullPointerException e) {
            log.error(lastDelayCheck + " - " + seconds + "s");
            log.error((new Date().getTime() - lastDelayCheck.getTime()) / 1000 + "seconds.");
            log.error(getFriendlyName() + " is somehow broken...");
            return false;
        }

        try {
            Date last = Date.from(delays.get(delays.size() - 1).getTimestamp().atZone(ZoneId.of("Europe/Berlin")).toInstant());
            return (new Date().getTime() - last.getTime()) / 1000 > seconds;
        } catch (NullPointerException e) {
            //brokenWorker = true;
            return false;
        }
    }

    /**
     * This method returns a friendly string which can be used for logging. It uses the pattern <i>route_short_name: stop_name -> trip_headsign</i>.
     * See Constructor or class doc for more Details on the patterns variables.
     *
     * @return a friendly string which can be used for logging
     */
    public String getFriendlyName() {
        return gtfsTripInfo.getFriendlyName();
    }

    /**
     * @return true if worker is broken (e.g. not found in TRIAS), false if everything okay
     */
    public boolean isBrokenWorker() {
        return brokenWorker;
    }

    /**
     * Can be set by the <i>checkTripEnding</i> function
     *
     * @return true if recording should be stopped, false if not
     */
    public boolean isStopRecording() {
        return stopRecording;
    }

    /**
     * @return more detailed information about the trip on which we are working
     */
    public ScheduledTrip getGtfsTripInfo() {
        return gtfsTripInfo;
    }

    /**
     * @return all delays collected so far
     */
    public ArrayList<Delay> getDelays() {
        return delays;
    }

    /**
     * printing the gtfs tour to a readable string
     * @return a readable representation of the full gtfs trip
     */
    public String printGtfsTour() {
        return printTour(gtfsStops);
    }

    /**
     * printing the trias tour to a readable string
     * @return a readable representation of the full trias trip
     */
    public String printTriasTour() {
        return printTour(triasStops);
    }

    /**
     * This function creates a complete readable string out of either a trias tour or a gtfs tour. Importing is, that the
     * TripStop class is used.
     * @param tour a list of tripstops representing the tour with all intermediates.
     * @return a readable representation of the full trip
     */
    public String printTour(ArrayList<TripStop> tour) {
        StringBuffer s = new StringBuffer();
        for (TripStop t : tour) {
            s.append(t.toString()).append("\n");
        }
        return s.toString();
    }

    /**
     * This function creates a completet readable string out of the trip info which is provided by GTFS. This contains
     * information about non changing attributes such as linenumber and destination.
     * @param tripInfo the TripInfo object which should be visualized
     * @return a readable representation of the full trip.
     */
    public String printTripInfo(ScheduledTrip tripInfo) {
        return getFriendlyName() + " (S: " + tripInfo.getService_id() + ", T: " + tripInfo.getTrip_id() + ")";
    }
}
