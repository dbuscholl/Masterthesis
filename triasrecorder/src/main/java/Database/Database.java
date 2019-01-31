package Database;

import Static.Settings;
import org.apache.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.TimeZone;

public class Database {
    private static Connection con;
    private static Logger log = Logger.getLogger(Database.class);
    ArrayList<IgnoreService> ignoringServices = new ArrayList<>();
    ArrayList<ScheduledTrip> trips = new ArrayList<>();


    public Database() throws SQLException, ClassNotFoundException {
        if (con == null || con.isClosed()) {
            Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection("jdbc:mysql://" + Settings.getDbhost() + ":" + Settings.getDbport() + "/" + Settings.getDbname() + "?user=" + Settings.getDbuser() + "&password=" + Settings.getDbpass());
            log.debug("Connected to: " + Settings.getDbhost() + ":" + Settings.getDbport() + "/" + Settings.getDbname());
        }
        ignoringServices.clear();
        trips.clear();
    }

    public String checkValidDatabaseStructure() throws SQLException {
        ArrayList<String> missingTables = new ArrayList<>();
        ArrayList<String> missingColumns = new ArrayList<>();

        HashMap<String, HashMap<String, String>> map = TableConfigurations.getMap();
        DatabaseMetaData meta = con.getMetaData();
        ResultSet tables = meta.getTables(null, null, "%", null);

        while (tables.next()) {
            String table = tables.getString(3);
            if (map.containsKey(table)) {
                ResultSet cols = meta.getColumns(null, null, table, "%");
                while (cols.next()) {
                    String column = cols.getString(4);
                    if (!map.get(table).containsKey(column)) {
                        missingColumns.add(table + ": " + column);
                    }
                }
            } else {
                missingTables.add(table);
            }
        }

        if (missingColumns.size() > 0 || missingTables.size() > 0) {
            StringBuffer missing = new StringBuffer("Missing Tables: ");
            missing.append(String.join(", ", missingTables));
            missing.append("\n");
            missing.append(String.join(", ", missingColumns));
            return missing.toString();
        } else {
            return "";
        }
    }

    public ArrayList<IgnoreService> getIgnoringServiceIds() throws SQLException {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Europe/Berlin"));
        String dowColumn = FormatTools.getColumnStringDayOfWeek(cal.get(Calendar.DAY_OF_WEEK));

        PreparedStatement s = con.prepareStatement("SELECT DISTINCT calendar_dates.service_id, exception_type FROM `calendar_dates` LEFT JOIN calendar ON calendar_dates.service_id = calendar.service_id where `date` = CURDATE() OR (date != CURDATE() AND exception_type = 1) and start_date <= ? AND end_date >= ? AND " + dowColumn + " = 1 ORDER BY `date` ASC");
        s.setString(1, FormatTools.sqlDateFormat.format(cal.getTime()));
        s.setString(2, FormatTools.sqlDateFormat.format(cal.getTime()));
        ResultSet rs = s.executeQuery();
        while (rs.next()) {
            ignoringServices.add(new IgnoreService(rs.getString("service_id"), rs.getInt("exception_type")));
        }
        rs.close();
        s.close();
        return ignoringServices;
    }

    public ArrayList<ScheduledTrip> getNextScheduledTrips() throws SQLException {
        trips = new ArrayList<>();
        String timeAddition = Settings.getNextTripsTimeAmount();
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Europe/Berlin"));

        PreparedStatement s = con.prepareStatement("SELECT trips.route_id, trips.trip_id, trips.service_id, stops.stop_id, stops.stop_name, route_short_name, trip_headsign, arrival_time, departure_time FROM `trips` LEFT JOIN calendar ON calendar.service_id = trips.service_id LEFT JOIN routes ON trips.route_id = routes.route_id LEFT JOIN stop_times ON stop_times.trip_id = trips.trip_id LEFT JOIN stops ON stop_times.stop_id = stops.stop_id WHERE `start_date` <= ? and `end_date` >= ? and " + FormatTools.getColumnStringDayOfWeek(cal.get(Calendar.DAY_OF_WEEK)) + " = 1 and stop_sequence = 1 and arrival_time BETWEEN ? AND AddTime(?, ?) ORDER BY departure_time");
        s.setString(1, FormatTools.sqlDateFormat.format(cal.getTime()));
        s.setString(2, FormatTools.sqlDateFormat.format(cal.getTime()));
        s.setString(3, FormatTools.timeFormat.format(cal.getTime()));
        s.setString(4, FormatTools.timeFormat.format(cal.getTime()));
        s.setString(5, timeAddition);
        ResultSet rs = s.executeQuery();

        while (rs.next()) {
            boolean skip = false;
            for (IgnoreService is : ignoringServices) {
                if (rs.getString("service_id").equals(is.getService_id())) {
                    skip = true;
                    break;
                }
            }
            if (!skip) {
                trips.add(new ScheduledTrip(rs.getString("route_id"), rs.getString("trip_id"), rs.getString("service_id"), rs.getString("stop_id"), rs.getString("stop_name"), rs.getString("route_short_name"), rs.getString("trip_headsign"), rs.getString("arrival_time"), rs.getString("departure_time")));
            } else {
                log.info("Skipping " + rs.getString("route_short_name") + " " + rs.getString("trip_headsign") + " scheduled at " + rs.getString("arrival_time") + " (S: " + rs.getString("service_id") + ", T: " + rs.getString("trip_id") + ")");
            }
        }
        rs.close();
        s.close();
        return trips;
    }

    public ArrayList<TripStop> getTripDetails(String trip_id) throws SQLException {
        ArrayList<TripStop> stops = new ArrayList<>();

        PreparedStatement s = con.prepareStatement("SELECT arrival_time, departure_time, stops.stop_id, stop_name, stop_sequence, pickup_type, drop_off_type FROM `stop_times` LEFT JOIN stops ON stop_times.stop_id = stops.stop_id WHERE `trip_id` = ? ORDER BY stop_sequence");
        s.setString(1, trip_id);
        ResultSet rs = s.executeQuery();

        while(rs.next()) {
            stops.add(new TripStop(rs.getString("arrival_time"),rs.getString("departure_time"),rs.getString("stop_id"),rs.getString("stop_name"),rs.getInt("stop_sequence"),rs.getInt("pickup_type"),rs.getInt("drop_off_type"), TripStop.Type.GTFS));
        }

        rs.close();
        s.close();
        return stops;
    }


    public void close() {
        try {
            con.close();
        } catch (SQLException e) {
            log.warn("Could not close database connection", e);
        }
    }
}
