package database;

import common.gtfs.Delay;
import common.gtfs.TripStop;
import common.network.Service;
import common.network.StopPoint;
import common.network.Trip;
import common.prognosis.PrognosisFactor;

import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.TimeZone;

/**
 * This class gives you various of database operation functions which have to do with gtfs manipulation. It's first use case
 * is to read out lots of things from the database. It's second is to update and insert data for prognosis calculation.
 * <br><b>Note: Some function will be extracted into the class {@link PrognosisDatabase} for semantic reasons in the next
 * update of this class</b>
 */
public class GTFS {

    /**
     * gets the first found trip id for a given TRIAS Trip Instance
     * @param trip the trip for which the gtfs id should be found from the database
     * @return the first found trip id from the gtfs database
     * @throws SQLException when something goes wrong during database accessing
     */
    public static String getGTFSTripId(Trip trip) throws SQLException {
        ArrayList<String> tripIds = getGTFSTripIds(trip);
        return tripIds.size() > 0 ? tripIds.get(0) : null;
    }

    /**
     * gets a list of gtfs trip ids matching the given TRIAS trip instance
     * @param trip the trip for which the gtfs id should be found from the database
     * @return a list of gtfs trip ids as string which matched to the trip provided as parameter
     * @throws SQLException  when something goes wrong during database accessing
     */
    public static ArrayList<String> getGTFSTripIds(Trip trip) throws SQLException {
        Connection c = DataSource.getConnection();
        ArrayList<String> tripIds = new ArrayList<>();
        String departureTime = SQLFormatTools.makeTimeForGtfs(trip.getBoarding().getDepartureTime());
        String arrivalTime = SQLFormatTools.makeTimeForGtfs(trip.getAlighting().getArrivalTime());
        String date = SQLFormatTools.makeDateForGtfs(trip.getBoarding().getDepartureTime());
        boolean hasRefs = trip.getBoarding().getRef() != null && !trip.getBoarding().getRef().equals("") && trip.getAlighting().getRef() != null && !trip.getAlighting().getRef().equals("");

        String stopIdentification = "stop_name = ?";
        if (hasRefs) {
            stopIdentification = "stop_times.stop_id LIKE ?";
        }

        ArrayList<String> occurringServices = getOccurringServiceIds(date);

        PreparedStatement s = c.prepareStatement("SELECT trips.service_id, trips.trip_id, arrival_time, departure_time, stop_sequence, stop_name, trip_headsign, route_short_name FROM vvs.stop_times LEFT JOIN stops ON stop_times.stop_id = stops.stop_id LEFT JOIN trips ON trips.trip_id = stop_times.trip_id LEFT JOIN routes ON routes.route_id = trips.route_id WHERE " + stopIdentification + " AND departure_time = ?");
        s.setString(1, hasRefs ? trip.getBoarding().getRef() + "%" : trip.getBoarding().getName());
        s.setString(2, departureTime);
        ResultSet rs = s.executeQuery();
        ArrayList<String> temporary = getTripIds(occurringServices, rs);

        rs.close();
        s = c.prepareStatement("SELECT trips.service_id, trips.trip_id, arrival_time, departure_time, stop_sequence, stop_name, trip_headsign, route_short_name FROM vvs.stop_times LEFT JOIN stops ON stop_times.stop_id = stops.stop_id LEFT JOIN trips ON trips.trip_id = stop_times.trip_id LEFT JOIN routes ON routes.route_id = trips.route_id WHERE " + stopIdentification + " AND arrival_time = ?");
        s.setString(1, hasRefs ? trip.getAlighting().getRef() + "%" : trip.getAlighting().getName());
        s.setString(2, arrivalTime);
        rs = s.executeQuery();

        // add tripIds which are already found and of which the service IDs are not ignored
        while (rs.next()) {
            String service_id = rs.getString("service_id");
            if (isOccuringService(service_id, occurringServices)) {
                if (temporary.contains(rs.getString("trip_id"))) {
                    tripIds.add(rs.getString("trip_id"));
                }
            }

        }
        rs.close();
        s.close();
        c.close();

        return tripIds;
    }

    /**
     * gets a list of trip ids for all trips which occur at a given stop point at it's time which is specified inside the
     * class as attribute. This might also contain trip services which travel the wrong direction because they can accidentally
     * be also there at the same time.
     * @param stop stop point at which all trip ids should be found which occur there
     * @param arrival true if you want to use arrival time from the stop point, false to use the departure time
     * @return a list of trip ids which matches the stop point it's departure / arrival time.
     * @throws SQLException when something goes wrong during database accessing
     */
    public static ArrayList<String> getGTFSTripIds(StopPoint stop, boolean arrival) throws SQLException {
        Connection c = DataSource.getConnection();
        boolean hasRef = stop.getRef() != null && !stop.getRef().equals("");
        String time, date, column;
        if (arrival) {
            time = SQLFormatTools.makeTimeForGtfs(stop.getArrivalTime());
            date = SQLFormatTools.makeDateForGtfs(stop.getArrivalTime());
            column = "arrival_time";
        } else {
            time = SQLFormatTools.makeTimeForGtfs(stop.getDepartureTime());
            date = SQLFormatTools.makeDateForGtfs(stop.getDepartureTime());
            column = "departure_time";
        }

        ArrayList<String> occurringServices = getOccurringServiceIds(date);

        String stopQuery = "stop_name = ?";
        if (hasRef) {
            stopQuery = "stop_times.stop_id LIKE ?";
        }
        PreparedStatement s = c.prepareStatement("SELECT trips.service_id, trips.trip_id, arrival_time, departure_time, stop_sequence, stop_name, trip_headsign, route_short_name FROM vvs.stop_times LEFT JOIN stops ON stop_times.stop_id = stops.stop_id LEFT JOIN trips ON trips.trip_id = stop_times.trip_id LEFT JOIN routes ON routes.route_id = trips.route_id WHERE " + stopQuery + " AND " + column + " = ?");
        s.setString(1, hasRef ? stop.getRef() + "%" : stop.getName());
        s.setString(2, time);
        ResultSet rs = s.executeQuery();

        ArrayList<String> tripIds = getTripIds(occurringServices, rs);
        rs.close();
        s.close();
        c.close();

        return tripIds;
    }

    /**
     * gets a list of tripIds from the database for a stopPoint in combination with a service. This means this function
     * returns all IDs which occur at the stop, travel the direction of the given service and optionally at the given time.
     * @param stop stop Point for which the IDs should be found
     * @param service the service for which the IDs should be found indicating the travel direction
     * @param everything true if the departure time and date should be ignored and every trip ID of all trips at this point
     *                   with the same destination should be retrieved, false if focus on the departure time and date
     *                   provided by the stoppoint
     * @return a list of trip IDs matching the given parameters
     * @throws SQLException  when something goes wrong during database accessing
     */
    public static ArrayList<String> getGTFSTripIds(StopPoint stop, Service service, boolean everything) throws SQLException {
        Connection c = DataSource.getConnection();
        ArrayList<String> tripIds = new ArrayList<>();
        ArrayList<String> occurringServices = new ArrayList<>();
        boolean hasRef = stop.getRef() != null && !stop.getRef().equals("");

        if (!everything) {
            String date = SQLFormatTools.makeDateForGtfs(stop.getDepartureTime());
            occurringServices = getOccurringServiceIds(date);
        }

        String stopname = hasRef ? stop.getRef() + "%" : stop.getName();
        String linename = service.getLineName();
        String headsign = service.getDesitnation();
        String stopQuery = hasRef ? "stop_times.stop_id LIKE ?" : "stop_name = ?";

        PreparedStatement s = c.prepareStatement("SELECT trips.service_id, trips.trip_id FROM vvs.stop_times LEFT JOIN stops ON stop_times.stop_id = stops.stop_id LEFT JOIN trips ON trips.trip_id = stop_times.trip_id LEFT JOIN routes ON routes.route_id = trips.route_id WHERE " + stopQuery + " AND route_short_name = ? AND trip_headsign = ?");
        s.setString(1, stopname);
        s.setString(2, linename);
        s.setString(3, headsign);
        ResultSet rs = s.executeQuery();

        if (!everything) {
            tripIds = getTripIds(occurringServices, rs);
        } else {
            while (rs.next()) {
                tripIds.add(rs.getString("trip_id"));
            }
        }

        rs.close();
        s.close();
        c.close();
        return tripIds;
    }

    /**
     * This function extracts all occuring tripIDs from a result sets resp. removes all tripIDs from the result set which
     * don't occus
     * @param ignoringServices the list of services which are retrieved from a seperate function.
     * @param rs the result set of which the IDs should be extracted from
     * @return a list of TripIDs matching the given parameters
     * @throws SQLException when something goes wrong during database accessing
     */
    private static ArrayList<String> getTripIds(ArrayList<String> ignoringServices, ResultSet rs) throws SQLException {
        ArrayList<String> tripIds = new ArrayList<>();

        // add tripIds of which the service IDs are not ignored
        while (rs.next()) {
            String service_id = rs.getString("service_id");
            if (isOccuringService(service_id, ignoringServices)) {
                tripIds.add(rs.getString("trip_id"));
            }
        }
        return tripIds;
    }

    /**
     * checks wheter a service id matches theh stack of services IDs
     * @param serviceId provided by the trip / service this ID should be checked inside the stack
     * @param stack this usually contains a list of service IDs which take place / occur at a given date. This is the stack
     *              in which the function searches for the given Service ID
     * @return true if the service occurs, false if service should be ignored
     */
    private static boolean isOccuringService(String serviceId, ArrayList<String> stack) {
        if (stack.contains(serviceId)) {
            return true;
        }
        return false;
    }

    /**
     * Inserts into an array of stopPoints the geolocation information for all entries and returns itself back again.
     * @param stops the array which should be manipulated by including the geolocation data.
     * @return the list of stopPoints which was given as parameter and is now updated
     * @throws SQLException when something goes wrong during database operations
     */
    public static ArrayList<StopPoint> getLocationDataForStopList(ArrayList<StopPoint> stops) throws SQLException {
        ArrayList<String> stopnames = getStopNamesAsList(stops);
        String queryinsert = getParameterListFromArray(stopnames);

        Connection c = DataSource.getConnection();
        PreparedStatement s = c.prepareStatement("SELECT * FROM stops WHERE stop_name IN (" + queryinsert + ")");

        for (int i = 1; i <= stopnames.size(); i++) {
            String name = stopnames.get(i - 1);
            s.setString(i, name);
        }

        ResultSet rs = s.executeQuery();
        while (rs.next()) {
            String name = rs.getString("stop_name");
            String lat = rs.getString("stop_lat");
            String lon = rs.getString("stop_lon");

            for (StopPoint stop : stops) {
                if (stop.getName().equals(name)) {
                    try {
                        stop.setLatitude(Double.valueOf(lat));
                        stop.setLongitude(Double.valueOf(lon));
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }

        rs.close();
        s.close();
        c.close();

        return stops;
    }

    /**
     * Inserts all Stop Sequences of the gtfs trip inside the TRIAS connection as they differ from each otehr as trias
     * counts the stops from the origin of the users trip and gtfs from the first stop of the vehicles trip. The given connection
     * will be updated and the same returnd
     * @param connection the connection inside which the stop sequences of gtfs should be inserted into
     * @throws SQLException  when something goes wrong during database operations
     * @return the connection inside which the stop sequences of gtfs should be inserted into and which was given in as
     * parameter
     */
    public static common.network.Connection addStopSequencesForConnection(common.network.Connection connection) throws SQLException {
        for (Trip t : connection.getLegs()) {
            String tripId = t.getGTFSTripId();
            if (tripId == null) {
                tripId = getGTFSTripId(t);
                t.setGTFSTripId(tripId);
            }

            Connection c = DataSource.getConnection();
            PreparedStatement s = c.prepareStatement("SELECT stop_name, stop_sequence FROM stop_times LEFT JOIN stops ON stop_times.stop_id = stops.stop_id WHERE stop_times.trip_id = ?");
            s.setString(1, tripId);
            ResultSet rs = s.executeQuery();

            HashMap<String, Integer> sequenceMap = buildSequenceMap(rs);
            fillStops(sequenceMap, t);
            rs.close();
            s.close();
            c.close();
        }
        return connection;
    }

    /**
     * creates a map containing stop name as key and sequence as value to easy access them
     * @param rs the resultset of which the map should be build
     * @return a hashmap containing the stopname as key and the stopsequence as value.
     * @throws SQLException when something goes wrong during database operations
     */
    private static HashMap<String, Integer> buildSequenceMap(ResultSet rs) throws SQLException {
        HashMap<String, Integer> sequenceMap = new HashMap<>();
        while (rs.next()) {
            sequenceMap.put(rs.getString("stop_name"), rs.getInt("stop_sequence"));
        }
        return sequenceMap;
    }

    /**
     * fills in all stop sequences for the trips stops. The trip given in as parameter is updated-
     * @param sequenceMap the sequence map with which the trip should be updated
     * @param t the trip whose stops should be updated
     */
    private static void fillStops(HashMap<String, Integer> sequenceMap, Trip t) {
        fillStop(sequenceMap, t.getBoarding());
        for (StopPoint s : t.getIntermediates()) {
            fillStop(sequenceMap, s);
        }
        fillStop(sequenceMap, t.getAlighting());
    }

    /**
     * Inserts a stopsequence into a stoppoint from a given sequenceMap
     * @param sequenceMap the map containing stop name as key and sequence as value from which the stop should get its
     *                    sequence from
     * @param stop the stop to update
     */
    private static void fillStop(HashMap<String, Integer> sequenceMap, StopPoint stop) {
        if (sequenceMap.containsKey(stop.getName())) {
            stop.setStopSequence(sequenceMap.get(stop.getName()));
        }
    }

    /**
     * returns a list of delays which were recorded by the user with the android app based on the trip Ids provided
     * @param ids the ids for which the delays are to be found
     * @return a list of userdelays matching the given Trip IDs
     * @throws SQLException when something goes wrong during database operations
     */
    public static ArrayList<Delay> getUserDelaysForIds(ArrayList<String> ids) throws SQLException {
        return getDelaysForIds(ids, true);
    }

    /**
     * returns a list of delays which were recorded by the TRIAS recorder based on the trip Ids provided
     * @param ids the ids for which the delays are to be found
     * @return a list of delays matching the given Trip IDs
     * @throws SQLException when something goes wrong during database operations
     */
    public static ArrayList<Delay> getDelaysForIds(ArrayList<String> ids) throws SQLException {
        return getDelaysForIds(ids, false);
    }

    /**
     * returns a list of delays based on the trip Ids provided
     * @param ids the ids for which the delays are to be found
     * @param userDelays true if delays recorded by user shouzld be retrieved, false if it should based on trias delays
     * @return a list of (user)delays matching the given Trip IDs
     * @throws SQLException when something goes wrong during database operations
     */
    private static ArrayList<Delay> getDelaysForIds(ArrayList<String> ids, boolean userDelays) throws SQLException {
        ArrayList<Delay> delays = new ArrayList<>();
        String table = userDelays ? "user_recordings" : "delays";

        StringBuilder builder = new StringBuilder();
        if (ids.size() < 5) {
            for (String id : ids) {
                builder.append("tripId = ? OR ");
            }
            builder.delete(builder.length() - 4, builder.length());
        } else {
            builder.append("tripId IN(");
            for (String id : ids) {
                builder.append("?,");
            }
            builder.delete(builder.length() - 1, builder.length());
            builder.append(")");
        }

        Connection c = DataSource.getConnection();
        PreparedStatement s = c.prepareStatement("SELECT * FROM " + table + " WHERE " + builder.toString());
        for (int i = 1; i <= ids.size(); i++) {
            String id = ids.get(i - 1);
            s.setString(i, id);
        }
        ResultSet rs = s.executeQuery();

        while (rs.next()) {
            Delay d = new Delay();
            if (!userDelays) {
                d.setDelayId(rs.getInt("id"));
            }
            d.setTripId(userDelays ? rs.getString("trip_id") : rs.getString("tripId"));
            d.setDelay(rs.getInt("delay"));
            d.setTimestamp(rs.getString("timestamp"));
            d.setStop_sequence(rs.getInt("stop_sequence"));
            delays.add(d);
        }

        rs.close();
        s.close();
        c.close();

        return delays;
    }

    /**
     * Query the database for all ServiceIds which should be ignored.
     *
     * @return List of IgnorService items, so if a TripStop contains a serviceId which is in this list, it can be removed
     * @throws SQLException when something goes wrong during database operations
     */
    public static ArrayList<String> getOccurringServiceIds() throws SQLException {
        return getOccurringServiceIds(null);
    }

    public static ArrayList<TripStop> getFullTrip(String tripId) throws SQLException {
        ArrayList<TripStop> fullTrip = new ArrayList<>();

        Connection c = DataSource.getConnection();
        PreparedStatement s = c.prepareStatement("SELECT arrival_time, departure_time, stop_times.stop_id, stop_name, stop_sequence FROM vvs.stop_times LEFT JOIN stops ON stop_times.stop_id = stops.stop_id WHERE trip_id = ? ORDER BY stop_sequence");
        s.setString(1, tripId);
        ResultSet rs = s.executeQuery();

        while (rs.next()) {
            TripStop tripStop = new TripStop();
            tripStop.setArrival_time(rs.getString("arrival_time"));
            tripStop.setDeparture_time(rs.getString("departure_time"));
            tripStop.setStop_id(rs.getString("stop_id"));
            tripStop.setStop_sequence(rs.getInt("stop_sequence"));
            tripStop.setStop_name(rs.getString("stop_name"));
            tripStop.setType(TripStop.Type.GTFS);
            fullTrip.add(tripStop);
        }

        rs.close();
        s.close();
        c.close();

        return fullTrip;
    }

    /**
     * returns for the given parameters all values of interchanges. 0 if they werent successful, 1 if they were.
     * @param fromTripId the tripID from which the user interchanged
     * @param toTripId the tripID to which the user interchanged
     * @return a list of values indicating a successful interchange.
     * @throws SQLException when something goes wrong during database operations
     */
    public static ArrayList<Integer> getInterchangeValues(String fromTripId, String toTripId) throws SQLException {
        ArrayList<Integer> values = new ArrayList<>();

        Connection c = DataSource.getConnection();
        PreparedStatement s = c.prepareStatement("SELECT * FROM user_answers WHERE trip_id = ? AND interchangeToTripId = ?");
        s.setString(1, fromTripId);
        s.setString(2, toTripId);

        ResultSet rs = s.executeQuery();
        while(rs.next()) {
            values.add(rs.getInt("successfullyInterchanged"));
        }

        rs.close();
        s.close();
        c.close();

        return values;
    }

    /**
     * returns answer value for given tripIDs and type as list.
     * @param ids the Trip IDs for which the answers are to be found
     * @param type the type of question for which the answer stands for
     * @return a list of numbers representing the answers of the given question for a given trip ID
     * @throws SQLException when something goes wrong during database operations
     */
    public static ArrayList<Integer> getAnswerValues(ArrayList<String> ids, PrognosisFactor.PrognosisFactorType type) throws SQLException {
        ArrayList<Integer> values = new ArrayList<>();
        String questionmarks = getParameterListFromArray(ids);

        Connection c = DataSource.getConnection();
        PreparedStatement s = c.prepareStatement("SELECT * FROM user_answers WHERE trip_id IN(" + questionmarks + ")");

        for (int i = 1; i <= ids.size(); i++) {
            String id = ids.get(i - 1);
            s.setString(i, id);
        }

        ResultSet rs = s.executeQuery();
        switch (type) {
            case QUESTIONNAIRE_CAPACITY:
                while (rs.next())
                    values.add(rs.getInt("capacity"));
                break;
            case QUESTIONNAIRE_CLEANNESS:
                while(rs.next())
                    values.add(rs.getInt("cleanness"));
                break;
            case QUESTIONNAIRE_DELAY:
                while(rs.next())
                    values.add(rs.getInt("delay"));
                break;
        }

        rs.close();
        s.close();
        c.close();

        return values;
    }

    /**
     * Query the database for all ServiceIds which should be ignored.
     *
     * @param date the date as String with pattern <i>yyyy-MM-dd</i>. If wrong formatted it simply uses the date of now
     * @return List of IgnorService items, so if a TripStop contains a serviceId which is in this list, it can be removed
     * @throws SQLException when something goes wrong during database operations
     */
    public static ArrayList<String> getOccurringServiceIds(String date) throws SQLException {
        SimpleDateFormat datesdf = new SimpleDateFormat(SQLFormatTools.datePattern);
        ArrayList<String> occurringServices = new ArrayList<>();

        // set date to now if date is not provided or date is invalid
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Europe/Berlin"));
        if (date != null) {
            try {
                cal.setTime(datesdf.parse(date));
            } catch (ParseException e) {
                cal = Calendar.getInstance(TimeZone.getTimeZone("Europe/Berlin"));
            }
        }

        String dowColumn = SQLFormatTools.getColumnStringDayOfWeek(cal.get(Calendar.DAY_OF_WEEK));
        Connection ds = DataSource.getConnection();
        //PreparedStatement s = ds.prepareStatement("SELECT DISTINCT calendar_dates.service_id, exception_type FROM `calendar_dates` LEFT JOIN calendar ON calendar_dates.service_id = calendar.service_id where `date` = ? OR (date != ? AND exception_type = 1) and start_date <= ? AND end_date >= ? AND " + dowColumn + " = 1 ORDER BY `date` ASC");
        //PreparedStatement s = ds.prepareStatement("SELECT DISTINCT calendar.service_id, exception_type FROM `calendar` LEFT JOIN calendar_dates ON calendar_dates.service_id = calendar.service_id WHERE ((start_date >= ? OR end_date <= ? OR " + dowColumn + " = 0) AND calendar.service_id NOT IN (SELECT service_id FROM calendar_dates WHERE exception_type = 1 AND date = ?)) OR (date = ? AND exception_type = 2)");
        PreparedStatement s = ds.prepareStatement("SELECT DISTINCT calendar.service_id FROM vvs.calendar LEFT JOIN calendar_dates ON calendar_dates.service_id = calendar.service_id WHERE " + dowColumn + " = 1 AND start_date <= ? AND end_date >= ? AND calendar.service_id NOT IN (SELECT service_id FROM vvs.calendar_dates WHERE date = ? AND exception_type = 2) OR calendar.service_id IN (SELECT service_id FROM vvs.calendar_dates WHERE date = ? AND exception_type = 1) ");
        s.setString(1, datesdf.format(cal.getTime()));
        s.setString(2, datesdf.format(cal.getTime()));
        s.setString(3, datesdf.format(cal.getTime()));
        s.setString(4, datesdf.format(cal.getTime()));
        ResultSet rs = s.executeQuery();
        while (rs.next()) {
            occurringServices.add(rs.getString("service_id"));
        }
        rs.close();
        s.close();
        ds.close();
        return occurringServices;
    }

    /**
     * returns all stopnames as a list of string from a list of {@link StopPoint} Objects
     * @param stops a list of {@link StopPoint} objects from which the list of string names should be build
     * @return a list of stopnames as string.
     */
    private static ArrayList<String> getStopNamesAsList(ArrayList<StopPoint> stops) {
        ArrayList<String> stopnames = new ArrayList<>();

        for (StopPoint s : stops) {
            stopnames.add(s.getName());
        }

        return stopnames;
    }

    /**
     * Insert your array and get a string back which contains lots of question marks seperated by comma. There are as
     * many question marks as there are entries in the list. Useful for prepared statements. E.g. "?,?,?,?,?,?"
     * @param list the list which should be parsed to question marks string
     * @param <T> any type? you can insert any arraylist.
     * @return a string of question marks separated by comma with as many question marks as entries of the array list
     */
    private static <T> String getParameterListFromArray(ArrayList<T> list) {
        StringBuilder s = new StringBuilder();

        for (T item : list) {
            s.append("?,");
        }

        return s.deleteCharAt(s.length() - 1).toString();
    }

    /**
     * removes all boarding ids from the parameters array which don't travel to the same destination as the trip provided
     * @param boardingIds the IDs to check if they travel the same direction
     * @param t the trip as reference for the destination station
     * @throws SQLException when something goes wrong during a database operation
     */
    public static void removeTripIdsOfWrongDirection(ArrayList<String> boardingIds, Trip t) throws SQLException {
        ArrayList<String> toRemove = new ArrayList<>();

        for (String id : boardingIds) {
            // get full trip so we can check with the next stop
            ArrayList<TripStop> trip = GTFS.getFullTrip(id);

            // find boarding stop and check if the next station after boarding matches the one got by tripId
            // if not matching then it travels in other direction so it has to be removed
            for (int i = 0; i < trip.size(); i++) {
                TripStop ts = trip.get(i);

                // when reached boarding but not reached end of stops in trip
                if (ts.getStop_name().equals(t.getBoarding().getName()) && i < trip.size() - 1) {
                    TripStop next = trip.get(i + 1);
                    StopPoint check;

                    // we might have trips without interchange
                    if (t.getIntermediates().size() > 0) {
                        check = t.getIntermediates().get(0);
                    } else {
                        check = t.getAlighting();
                    }

                    // if name of the next stop doesn't match the one from the connection
                    if (!check.getName().equals(next.getStop_name())) {
                        toRemove.add(id);
                    }
                }
            }
        }

        for (String item : toRemove) {
            boardingIds.remove(item);
        }
    }
}
