package database;

import common.gtfs.Delay;
import common.gtfs.IgnoreService;
import common.gtfs.TripStop;
import common.network.Service;
import common.network.StopPoint;
import common.network.Trip;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.TimeZone;

public class GTFS {

    public static String getTripId(Trip trip) throws SQLException {
        ArrayList<String> tripIds = getGTFSTripIds(trip);
        return tripIds.size() > 0 ? tripIds.get(0) : null;
    }

    public static ArrayList<String> getGTFSTripIds(Trip trip) throws SQLException {
        Connection c = DataSource.getConnection();
        ArrayList<String> tripIds = new ArrayList<>();
        String departureTime = SQLFormatTools.makeTimeForGtfs(trip.getBoarding().getDepartureTime());
        String arrivalTime = SQLFormatTools.makeTimeForGtfs(trip.getAlighting().getArrivalTime());
        String date = SQLFormatTools.makeDateForGtfs(trip.getBoarding().getDepartureTime());

        ArrayList<IgnoreService> ignoringServices = getIgnoringServiceIds(date);

        PreparedStatement s = c.prepareStatement("SELECT trips.service_id, trips.trip_id, arrival_time, departure_time, stop_sequence, stop_name, trip_headsign, route_short_name FROM vvs.stop_times LEFT JOIN stops ON stop_times.stop_id = stops.stop_id LEFT JOIN trips ON trips.trip_id = stop_times.trip_id LEFT JOIN routes ON routes.route_id = trips.route_id WHERE stop_name = ? AND departure_time = ?");
        s.setString(1, trip.getBoarding().getName());
        s.setString(2, departureTime);
        ResultSet rs = s.executeQuery();
        ArrayList<String> temporary = getTripIds(ignoringServices, rs);

        rs.close();
        s = c.prepareStatement("SELECT trips.service_id, trips.trip_id, arrival_time, departure_time, stop_sequence, stop_name, trip_headsign, route_short_name FROM vvs.stop_times LEFT JOIN stops ON stop_times.stop_id = stops.stop_id LEFT JOIN trips ON trips.trip_id = stop_times.trip_id LEFT JOIN routes ON routes.route_id = trips.route_id WHERE stop_name = ? AND arrival_time = ?");
        s.setString(1, trip.getAlighting().getName());
        s.setString(2, arrivalTime);
        rs = s.executeQuery();

        // add tripIds which are already found and of which the service IDs are not ignored
        while (rs.next()) {
            String service_id = rs.getString("service_id");
            if (isIgnoredService(service_id, ignoringServices)) continue;

            if (temporary.contains(rs.getString("trip_id"))) {
                tripIds.add(rs.getString("trip_id"));
            }
        }
        rs.close();
        s.close();
        c.close();

        return tripIds;
    }

    public static ArrayList<String> getGTFSTripIds(StopPoint stop, boolean arrival) throws SQLException {
        Connection c = DataSource.getConnection();
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

        ArrayList<IgnoreService> ignoringServices = getIgnoringServiceIds(date);

        PreparedStatement s = c.prepareStatement("SELECT trips.service_id, trips.trip_id, arrival_time, departure_time, stop_sequence, stop_name, trip_headsign, route_short_name FROM vvs.stop_times LEFT JOIN stops ON stop_times.stop_id = stops.stop_id LEFT JOIN trips ON trips.trip_id = stop_times.trip_id LEFT JOIN routes ON routes.route_id = trips.route_id WHERE stop_name = ? AND " + column + " = ?");
        s.setString(1, stop.getName());
        s.setString(2, time);
        ResultSet rs = s.executeQuery();

        ArrayList<String> tripIds = getTripIds(ignoringServices, rs);
        rs.close();
        s.close();
        c.close();

        return tripIds;
    }

    public static ArrayList<String> getGTFSTripIds(StopPoint stop, Service service, boolean everything) throws SQLException {
        Connection c = DataSource.getConnection();
        ArrayList<String> tripIds = new ArrayList<>();
        ArrayList<IgnoreService> ignoringServices = new ArrayList<>();

        if (!everything) {
            String date = SQLFormatTools.makeDateForGtfs(stop.getDepartureTime());
            ignoringServices = getIgnoringServiceIds(date);
        }

        String stopname = stop.getName();
        String linename = service.getLineName();
        String headsign = service.getDesitnation();

        PreparedStatement s = c.prepareStatement("SELECT trips.service_id, trips.trip_id FROM vvs.stop_times LEFT JOIN stops ON stop_times.stop_id = stops.stop_id LEFT JOIN trips ON trips.trip_id = stop_times.trip_id LEFT JOIN routes ON routes.route_id = trips.route_id WHERE stop_name = ? AND route_short_name = ? AND trip_headsign = ?");
        s.setString(1, stopname);
        s.setString(2, linename);
        s.setString(3, headsign);
        ResultSet rs = s.executeQuery();

        if (!everything) {
            tripIds = getTripIds(ignoringServices, rs);
        } else {
            while(rs.next()) {
                tripIds.add(rs.getString("trip_id"));
            }
        }

        rs.close();
        s.close();
        c.close();
        return tripIds;
    }

    private static ArrayList<String> getTripIds(ArrayList<IgnoreService> ignoringServices, ResultSet rs) throws SQLException {
        ArrayList<String> tripIds = new ArrayList<>();

        // add tripIds of which the service IDs are not ignored
        while (rs.next()) {
            String service_id = rs.getString("service_id");
            if (isIgnoredService(service_id, ignoringServices)) continue;
            tripIds.add(rs.getString("trip_id"));
        }
        return tripIds;
    }

    private static boolean isIgnoredService(String serviceId, ArrayList<IgnoreService> stack) {
        for (IgnoreService ignoreService : stack) {
            if (ignoreService.getService_id().equals(serviceId)) {
                return true;
            }
        }
        return false;
    }

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

        return stops;
    }

    public static void addStopSequencesForConnection(common.network.Connection connection) throws SQLException {
        for (Trip t : connection.getLegs()) {
            String tripId = t.getGTFSTripId();
            if (tripId == null) {
                tripId = getTripId(t);
                t.setGTFSTripId(tripId);
            }

            Connection c = DataSource.getConnection();
            PreparedStatement s = c.prepareStatement("SELECT stop_name, stop_sequence FROM stop_times LEFT JOIN stops ON stop_times.stop_id = stops.stop_id WHERE stop_times.trip_id = ?");
            s.setString(1, tripId);
            ResultSet rs = s.executeQuery();

            HashMap<String, Integer> sequenceMap = buildSequenceMap(rs);
            fillStops(sequenceMap, t);
        }
    }

    private static HashMap<String, Integer> buildSequenceMap(ResultSet rs) throws SQLException {
        HashMap<String, Integer> sequenceMap = new HashMap<>();
        while (rs.next()) {
            sequenceMap.put(rs.getString("stop_name"), rs.getInt("stop_sequence"));
        }
        return sequenceMap;
    }

    private static void fillStops(HashMap<String, Integer> sequenceMap, Trip t) {
        fillStop(sequenceMap, t.getBoarding());
        for (StopPoint s : t.getIntermediates()) {
            fillStop(sequenceMap, s);
        }
        fillStop(sequenceMap, t.getAlighting());
    }

    private static void fillStop(HashMap<String, Integer> sequenceMap, StopPoint stop) {
        if (sequenceMap.containsKey(stop.getName())) {
            stop.setStopSequence(sequenceMap.get(stop.getName()));
        }
    }

    public static ArrayList<Delay> getDelaysForIds(ArrayList<String> ids) throws SQLException {
        ArrayList<Delay> delays = new ArrayList<>();

        StringBuilder builder = new StringBuilder();
        for (String id : ids) {
            builder.append("tripId = ? OR");
        }
        builder.delete(builder.length() - 3, builder.length());

        Connection c = DataSource.getConnection();
        PreparedStatement s = c.prepareStatement("SELECT * FROM vvs.delays WHERE " + builder.toString());
        for (int i = 1; i <= ids.size(); i++) {
            String id = ids.get(i - 1);
            s.setString(i, id);
        }
        ResultSet rs = s.executeQuery();

        while (rs.next()) {
            Delay d = new Delay();
            d.setDelayId(rs.getInt("id"));
            d.setTripId(rs.getString("tripId"));
            d.setDelay(rs.getInt("delay"));
            d.setTimestamp(rs.getString("timestamp"));
            d.setStop_sequence(rs.getInt("stop_sequence"));
            delays.add(d);
        }

        return delays;
    }

    /**
     * Query the database for all ServiceIds which should be ignored.
     *
     * @return List of IgnorService items, so if a TripStop contains a serviceId which is in this list, it can be removed
     * @throws SQLException
     */
    public static ArrayList<IgnoreService> getIgnoringServiceIds() throws SQLException {
        return getIgnoringServiceIds(null);
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

        return fullTrip;
    }

    /**
     * Query the database for all ServiceIds which should be ignored.
     *
     * @param date the date as String with pattern <i>yyyy-MM-dd</i>. If wrong formatted it simply uses the date of now
     * @return List of IgnorService items, so if a TripStop contains a serviceId which is in this list, it can be removed
     * @throws SQLException
     */
    public static ArrayList<IgnoreService> getIgnoringServiceIds(String date) throws SQLException {
        ArrayList<IgnoreService> ignoringServices = new ArrayList<>();

        // set date to now if date is not provided or date is invalid
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Europe/Berlin"));
        if (date != null) {
            try {
                cal.setTime(SQLFormatTools.sqlDateFormat.parse(date));
            } catch (ParseException e) {
                cal = Calendar.getInstance(TimeZone.getTimeZone("Europe/Berlin"));
            }
        }

        String dowColumn = SQLFormatTools.getColumnStringDayOfWeek(cal.get(Calendar.DAY_OF_WEEK));
        Connection ds = DataSource.getConnection();
        //PreparedStatement s = ds.prepareStatement("SELECT DISTINCT calendar_dates.service_id, exception_type FROM `calendar_dates` LEFT JOIN calendar ON calendar_dates.service_id = calendar.service_id where `date` = ? OR (date != ? AND exception_type = 1) and start_date <= ? AND end_date >= ? AND " + dowColumn + " = 1 ORDER BY `date` ASC");
        PreparedStatement s = ds.prepareStatement("SELECT DISTINCT calendar.service_id, exception_type FROM `calendar` LEFT JOIN calendar_dates ON calendar_dates.service_id = calendar.service_id WHERE ((start_date >= ? OR end_date <= ? OR " + dowColumn + " = 0) AND calendar.service_id NOT IN (SELECT service_id FROM calendar_dates WHERE exception_type = 1 AND date = ?)) OR (date = ? AND exception_type = 2)");
        s.setString(1, SQLFormatTools.sqlDateFormat.format(cal.getTime()));
        s.setString(2, SQLFormatTools.sqlDateFormat.format(cal.getTime()));
        s.setString(3, SQLFormatTools.sqlDateFormat.format(cal.getTime()));
        s.setString(4, SQLFormatTools.sqlDateFormat.format(cal.getTime()));
        ResultSet rs = s.executeQuery();
        while (rs.next()) {
            ignoringServices.add(new IgnoreService(rs.getString("service_id"), rs.getInt("exception_type")));
        }
        rs.close();
        s.close();
        ds.close();
        return ignoringServices;
    }

    private static ArrayList<String> getStopNamesAsList(ArrayList<StopPoint> stops) {
        ArrayList<String> stopnames = new ArrayList<>();

        for (StopPoint s : stops) {
            stopnames.add(s.getName());
        }

        return stopnames;
    }

    private static <T> String getParameterListFromArray(ArrayList<T> list) {
        StringBuilder s = new StringBuilder();

        for (T item : list) {
            s.append("?,");
        }

        return s.deleteCharAt(s.length() - 1).toString();
    }

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
