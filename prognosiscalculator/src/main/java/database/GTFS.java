package database;

import entities.gtfs.IgnoreService;
import entities.network.StopPoint;
import entities.network.Trip;

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
        Connection c = DataSource.getConnection();
        String departureTime = SQLFormatTools.makeTimeForGtfs(trip.getBoarding().getDepartureTime());
        String arrivalTime = SQLFormatTools.makeTimeForGtfs(trip.getAlighting().getArrivalTime());
        String date = SQLFormatTools.makeDateForGtfs(trip.getBoarding().getDepartureTime());

        ArrayList<IgnoreService> ignoringServices = getIgnoringServiceIds(date);
        ArrayList<String> temporary = new ArrayList<>();
        ArrayList<String> tripIds = new ArrayList<>();

        PreparedStatement s = c.prepareStatement("SELECT trips.service_id, trips.trip_id, arrival_time, departure_time, stop_sequence, stop_name, trip_headsign, route_short_name FROM vvs.stop_times LEFT JOIN stops ON stop_times.stop_id = stops.stop_id LEFT JOIN trips ON trips.trip_id = stop_times.trip_id LEFT JOIN routes ON routes.route_id = trips.route_id WHERE stop_name = ? AND departure_time = ?");
        s.setString(1, trip.getBoarding().getName());
        s.setString(2, departureTime);
        ResultSet rs = s.executeQuery();

        // add tripIds of which the service IDs are not ignored
        while (rs.next()) {
            String service_id = rs.getString("service_id");
            if (isIgnoredService(service_id, ignoringServices)) continue;
            temporary.add(rs.getString("trip_id"));
        }

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

        return tripIds.size() > 0 ? tripIds.get(0) : null;
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

    public static void addStopSequencesForConnection(entities.network.Connection connection) throws SQLException {
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

    /**
     * Query the database for all ServiceIds which should be ignored.
     *
     * @return List of IgnorService items, so if a TripStop contains a serviceId which is in this list, it can be removed
     * @throws SQLException
     */
    public static ArrayList<IgnoreService> getIgnoringServiceIds() throws SQLException {
        return getIgnoringServiceIds(null);
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
        PreparedStatement s = ds.prepareStatement("SELECT DISTINCT calendar.service_id, exception_type FROM `calendar` LEFT JOIN calendar_dates ON calendar_dates.service_id = calendar.service_id WHERE ((start_date >= ? OR end_date <= ? OR " + dowColumn + " = 0) AND calendar.service_id NOT IN (SELECT service_id FROM calendar_dates WHERE exception_type = 1 AND date = ?)) OR (date = ? AND exception_type = 2)\n");
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
}
