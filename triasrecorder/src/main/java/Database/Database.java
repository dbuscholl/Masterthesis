package Database;

import Static.Settings;
import com.mysql.jdbc.exceptions.jdbc4.CommunicationsException;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.TimeZone;

public class Database {
    private static BasicDataSource ds;

    private static Logger log = Logger.getLogger(Database.class);

    private Database() {
    }

    public static Connection getDataSource() throws SQLException {
        if (ds == null) {
            ds = new BasicDataSource();
            ds.setUrl("jdbc:mysql://" + Settings.getDbhost() + ":" + Settings.getDbport() + "/" + Settings.getDbname());
            ds.setUsername(Settings.getDbuser());
            ds.setPassword(Settings.getDbpass());
            ds.setMinIdle(2);
            ds.setMaxIdle(5);
            ds.setMaxOpenPreparedStatements(100);
        }
        return ds.getConnection();
    }

    public static String checkValidDatabaseStructure() throws SQLException {
        ArrayList<String> missingTables = new ArrayList<>();
        ArrayList<String> missingColumns = new ArrayList<>();

        HashMap<String, HashMap<String, String>> map = TableConfigurations.getMap();
        Connection ds = getDataSource();
        DatabaseMetaData meta = ds.getMetaData();
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
                if (!table.equals("delays")) {
                    missingTables.add(table);
                }
            }
        }

        if (missingColumns.size() > 0 || missingTables.size() > 0) {
            StringBuffer missing = new StringBuffer("Missing Tables: ");
            missing.append(String.join(", ", missingTables));
            missing.append("\n");
            missing.append(String.join(", ", missingColumns));
            ds.close();
            return missing.toString();
        } else {
            // TODO: Check delays table
            return "";
        }
    }

    public static ArrayList<IgnoreService> getIgnoringServiceIds() throws SQLException {
        ArrayList<IgnoreService> ignoringServices = new ArrayList<IgnoreService>();
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Europe/Berlin"));
        String dowColumn = FormatTools.getColumnStringDayOfWeek(cal.get(Calendar.DAY_OF_WEEK));

        Connection ds = getDataSource();
        PreparedStatement s = ds.prepareStatement("SELECT DISTINCT calendar_dates.service_id, exception_type FROM `calendar_dates` LEFT JOIN calendar ON calendar_dates.service_id = calendar.service_id where `date` = CURDATE() OR (date != CURDATE() AND exception_type = 1) and start_date <= ? AND end_date >= ? AND " + dowColumn + " = 1 ORDER BY `date` ASC");
        s.setString(1, FormatTools.sqlDateFormat.format(cal.getTime()));
        s.setString(2, FormatTools.sqlDateFormat.format(cal.getTime()));
        ResultSet rs = s.executeQuery();
        while (rs.next()) {
            ignoringServices.add(new IgnoreService(rs.getString("service_id"), rs.getInt("exception_type")));
        }
        rs.close();
        s.close();
        ds.close();
        return ignoringServices;
    }

    public static ArrayList<ScheduledTrip> getNextScheduledTrips(ArrayList<IgnoreService> ignoringServices) throws SQLException {
        ArrayList<ScheduledTrip> trips = new ArrayList<>();
        String timeAddition = Settings.getNextTripsTimeAmount();
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Europe/Berlin"));

        Connection ds = getDataSource();
        PreparedStatement s = ds.prepareStatement("SELECT trips.route_id, trips.trip_id, trips.service_id, stops.stop_id, stops.stop_name, route_short_name, trip_headsign, arrival_time, departure_time FROM `trips` LEFT JOIN calendar ON calendar.service_id = trips.service_id LEFT JOIN routes ON trips.route_id = routes.route_id LEFT JOIN stop_times ON stop_times.trip_id = trips.trip_id LEFT JOIN stops ON stop_times.stop_id = stops.stop_id WHERE `start_date` <= ? and `end_date` >= ? and " + FormatTools.getColumnStringDayOfWeek(cal.get(Calendar.DAY_OF_WEEK)) + " = 1 and stop_sequence = 1 and arrival_time BETWEEN ? AND AddTime(?, ?) ORDER BY departure_time");
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
        ds.close();
        return trips;
    }

    public static ArrayList<TripStop> getTripDetails(String trip_id) throws SQLException {
        ArrayList<TripStop> stops = new ArrayList<>();

        Connection ds = getDataSource();
        PreparedStatement s = ds.prepareStatement("SELECT arrival_time, departure_time, stops.stop_id, stop_name, stop_sequence, pickup_type, drop_off_type FROM `stop_times` LEFT JOIN stops ON stop_times.stop_id = stops.stop_id WHERE `trip_id` = ? ORDER BY stop_sequence");
        s.setString(1, trip_id);
        ResultSet rs = s.executeQuery();

        while (rs.next()) {
            stops.add(new TripStop(rs.getString("arrival_time"), rs.getString("departure_time"), rs.getString("stop_id"), rs.getString("stop_name"), rs.getInt("stop_sequence"), rs.getInt("pickup_type"), rs.getInt("drop_off_type"), TripStop.Type.GTFS));
        }

        rs.close();
        s.close();
        ds.close();
        return stops;
    }


    public static void addDelays(ScheduledTrip tripInfo, ArrayList<Delay> delays) throws SQLException {
        Connection ds = getDataSource();

        PreparedStatement s = ds.prepareStatement("INSERT INTO `delays`(`tripId`, `delay`, `timestamp`,`stop_sequence`) VALUES (?,?,?,?)");
        for (Delay d : delays) {
            s.setString(1, tripInfo.getTrip_id());
            s.setInt(2, d.getSeconds());
            s.setString(3, FormatTools.sqlDatetimeFormat.format(d.getTimestamp()));
            s.setInt(4, d.getGtfsStop().getStop_sequence());
            s.addBatch();
        }
        s.executeBatch();
        s.close();
        ds.close();
    }

    public static void stop() throws SQLException {
        ds.close();
    }
}
