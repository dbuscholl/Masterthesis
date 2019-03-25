package database;

import entities.gtfs.IgnoreService;
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
            for (IgnoreService ignoreService : ignoringServices) {
                if (ignoreService.getService_id().equals(service_id)) {
                    continue;
                }
                temporary.add(rs.getString("trip_id"));
            }
        }

        rs.close();
        s.setString(1, trip.getAlighting().getName());
        s.setString(2, arrivalTime);
        rs = s.executeQuery();

        // add tripIds which are already found and of which the service IDs are not ignored
        while (rs.next()) {
            String service_id = rs.getString("service_id");
            for (IgnoreService ignoreService : ignoringServices) {
                if (ignoreService.getService_id().equals(service_id)) {
                    continue;
                }
                if (temporary.contains(rs.getString("trip_id"))) {
                    tripIds.add(rs.getString("trip_id"));
                }
            }
        }
        rs.close();
        s.close();
        c.close();

        return tripIds.size() > 0 ? tripIds.get(0) : null;
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
        PreparedStatement s = ds.prepareStatement("SELECT DISTINCT calendar.service_id, exception_type FROM `calendar` LEFT JOIN calendar_dates ON calendar_dates.service_id = calendar.service_id WHERE (start_date <= ? AND end_date >= ? AND " + dowColumn + " = 0 AND calendar.service_id NOT IN (SELECT service_id FROM calendar_dates WHERE exception_type = 1 AND date = ?)) OR (date = ? AND exception_type = 2)");
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
}
