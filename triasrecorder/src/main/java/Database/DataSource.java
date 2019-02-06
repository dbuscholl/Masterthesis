package Database;

import Database.Entities.*;
import Static.Settings;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.log4j.Logger;

import java.sql.*;
import java.text.ParseException;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.TimeZone;

/**
 * The main class for accessing and operating on the Database. It currently uses DBCP TriasConnection Pooling to serve
 * multiple requests. No other package should use this class. It's build with the singleton pattern.
 */
public class DataSource {
    private static BasicDataSource ds;

    private static Logger log = Logger.getLogger(DataSource.class);

    private DataSource() {
    }

    /**
     * @return a connection with which we can prepare statements on the database if one is free.
     * @throws SQLException
     */
    private static Connection getDataSource() throws SQLException {
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

    /**
     * checks wheter the database structure is currently valid or not. This is important because the GTFS data could be
     * manually imported and if the datatypes are not valid or values that are not optional are null the whole recorder
     * won't work
     *
     * @return String concatenation of columns or tables which are missing or wrong structured in the databse
     * @throws SQLException if anything goes wrong
     */
    public static String checkValidDatabaseStructure() throws SQLException {
        ArrayList<String> missingTables = new ArrayList<>();
        ArrayList<String> missingColumns = new ArrayList<>();

        HashMap<String, HashMap<String, String>> map = TableConfigurations.getMap();
        Connection ds = getDataSource();

        //reading out all tables from the DB-Meta-Data
        DatabaseMetaData meta = ds.getMetaData();
        ResultSet tables = meta.getTables(null, null, "%", null);

        // already set delays table as msising because of iteration. This is corrected below if found in meta
        boolean delaysMissing = true;

        while (tables.next()) {
            String table = tables.getString(3);

            //correcting boolean if delays where found
            if (table.equals("delays")) {
                delaysMissing = false;
            }

            //now check the columsn if we found the table
            if (map.containsKey(table)) {
                ResultSet cols = meta.getColumns(null, null, table, "%");
                while (cols.next()) {
                    String column = cols.getString(4);
                    if (!map.get(table).containsKey(column)) {
                        missingColumns.add(table + ": " + column);
                    }
                }
            } else {
                // delays table is handled later so don't add to missing tables
                if (!table.equals("delays")) {
                    missingTables.add(table);
                }
            }
        }

        // if anything from the GTFS-specification is missing (not delays table)
        if (missingColumns.size() > 0 || missingTables.size() > 0) {
            StringBuffer missing = new StringBuffer("Missing Tables: ");
            missing.append(String.join(", ", missingTables));
            missing.append("\n");
            missing.append(String.join(", ", missingColumns));
            ds.close();
            return missing.toString();
        }

        // now handle delays table by simply creating it
        if (delaysMissing) {
            boolean success = createDelaysTable(ds);
            if (!success) {
                return "delays";
            }
        }
        ds.close();
        return "";
    }

    /**
     * creates the delays table inside the database
     *
     * @param ds TriasConnection object to be used. Will grab it's own conenction if none provided
     * @return true wheter table was successfully created, false if not
     * @throws SQLException this sometimes can happen...
     */
    public static boolean createDelaysTable(Connection ds) throws SQLException {
        boolean providedConnection = true;
        if (ds == null) {
            providedConnection = false;
            ds = getDataSource();
        }
        PreparedStatement s = ds.prepareStatement("CREATE TABLE IF NOT EXISTS `delays` (`id` int(10) UNSIGNED NOT NULL, `tripId` varchar(255) NOT NULL, `delay` int(11) NOT NULL, `timestamp` datetime NOT NULL, `stop_sequence` int(11) NOT NULL, PRIMARY KEY  (`id`))");
        int execute = s.executeUpdate();
        s.close();
        if (execute != 0) {
            log.error("Could not create table delays!");
            ds.close();
            return false;
        }
        // also set primary key attributes
        s = ds.prepareStatement("ALTER TABLE `delays` MODIFY `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT");
        execute = s.executeUpdate();
        s.close();
        if (execute != 0) {
            log.error("Could not alter table delays!");
            ds.close();
            return false;
        }
        if (!providedConnection) ds.close();
        return true;
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

        Connection ds = getDataSource();
        PreparedStatement s = ds.prepareStatement("SELECT DISTINCT calendar_dates.service_id, exception_type FROM `calendar_dates` LEFT JOIN calendar ON calendar_dates.service_id = calendar.service_id where `date` = ? OR (date != ? AND exception_type = 1) and start_date <= ? AND end_date >= ? AND " + dowColumn + " = 1 ORDER BY `date` ASC");
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

    public static ArrayList<ScheduledTrip> getNextScheduledTrips(ArrayList<IgnoreService> ignoringServices) throws SQLException {
        return getNextScheduledTrips(ignoringServices, null);
    }

    /**
     * returns a list of nex Scheduled Trips but you need to provide a ignoring service list. Make sure your IgnoreServices
     * are for the date you query
     *
     * @param ignoringServices list of Ignored Services which should be ignored in the result. If null provided, no trips
     *                         will be ignored, but that should not be used, because this doesn't happen in reality
     * @param datetime         string representating the datetime in <i>yyyy-MM-dd HH:mm:ss</i>
     * @return list of next scheduled trips
     * @throws SQLException
     */
    public static ArrayList<ScheduledTrip> getNextScheduledTrips(ArrayList<IgnoreService> ignoringServices, String datetime) throws SQLException {
        ArrayList<ScheduledTrip> trips = new ArrayList<>();
        String timeAddition = Settings.getNextTripsTimeAmount();
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Europe/Berlin"));

        if (datetime != null) {
            try {
                SQLFormatTools.sqlDatetimeFormat.setTimeZone(TimeZone.getTimeZone("Europe/Berlin"));
                cal.setTime(SQLFormatTools.sqlDatetimeFormat.parse(datetime));
            } catch (ParseException e) {
                cal = Calendar.getInstance(TimeZone.getTimeZone("Europe/Berlin"));
            }
        }

        Connection ds = getDataSource();
        PreparedStatement s = ds.prepareStatement("SELECT trips.route_id, trips.trip_id, trips.service_id, stops.stop_id, stops.stop_name, route_short_name, trip_headsign, arrival_time, departure_time FROM `trips` LEFT JOIN calendar ON calendar.service_id = trips.service_id LEFT JOIN routes ON trips.route_id = routes.route_id LEFT JOIN stop_times ON stop_times.trip_id = trips.trip_id LEFT JOIN stops ON stop_times.stop_id = stops.stop_id WHERE `start_date` <= ? and `end_date` >= ? and " + SQLFormatTools.getColumnStringDayOfWeek(cal.get(Calendar.DAY_OF_WEEK)) + " = 1 and stop_sequence = 1 and arrival_time BETWEEN ? AND AddTime(?, ?) ORDER BY departure_time");
        s.setString(1, SQLFormatTools.sqlDateFormat.format(cal.getTime()));
        s.setString(2, SQLFormatTools.sqlDateFormat.format(cal.getTime()));
        s.setString(3, SQLFormatTools.timeFormat.format(cal.getTime()));
        s.setString(4, SQLFormatTools.timeFormat.format(cal.getTime()));
        s.setString(5, timeAddition);
        ResultSet rs = s.executeQuery();

        // some fail safe
        if (ignoringServices == null) {
            ignoringServices = new ArrayList<>();
        }

        // throw out services, which should be ignored because they are in the ignore list
        while (rs.next()) {
            boolean skip = false;
            for (IgnoreService is : ignoringServices) {
                if (rs.getString("service_id").equals(is.getService_id())) {
                    skip = true;
                    break;
                }
            }
            if (!skip) {
                ScheduledTrip trip = new ScheduledTrip(rs.getString("route_id"), rs.getString("trip_id"), rs.getString("service_id"), rs.getString("stop_id"), rs.getString("stop_name"), rs.getString("route_short_name"), rs.getString("trip_headsign"), rs.getString("arrival_time"), rs.getString("departure_time"));
                String at = trip.getArrival_time();
                String dt = trip.getDeparture_time();
                if ((at == null && dt == null) && (at.equals("") && dt.equals(""))) {
                    log.info("Skipping " + trip.getFriendlyName() + " because of invalid arrival / departure time. (S: " + trip.getService_id() + ", T: " + trip.getTrip_id() + ")");
                    continue;
                } else if ((at == null || at.equals("")) && !dt.equals("")) {
                    trip.setArrival_time(dt);
                    trips.add(trip);
                } else if ((dt == null || dt.equals("")) && !at.equals("")) {
                    trip.setDeparture_time(at);
                    trips.add(trip);
                } else {
                    trips.add(trip);
                }
            } else {
                log.info("Skipping " + rs.getString("route_short_name") + " " + rs.getString("trip_headsign") + " scheduled at " + rs.getString("arrival_time") + " (S: " + rs.getString("service_id") + ", T: " + rs.getString("trip_id") + ")");
            }
        }
        rs.close();
        s.close();
        ds.close();
        return trips;
    }

    /**
     * This gives you a full trip with all stops and times from the GTFS-Database
     *
     * @param trip_id the GTFS-TripId of the trip you want to obtain
     * @return a full trip with all stops and times from the GTFS-Database as an ArrayList of TripStops
     * @throws SQLException
     */
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


    /**
     * Inserts an array of delays into the database
     *
     * @param tripInfo ScheduledTrip object for which it should be stored in the database. Important to reference a delay with a trip.
     * @param delays   ArrayList of Delay objects
     * @throws SQLException
     */
    public static void addDelays(ScheduledTrip tripInfo, ArrayList<Delay> delays) throws SQLException {
        Connection ds = getDataSource();

        PreparedStatement s = ds.prepareStatement("INSERT INTO `delays`(`tripId`, `delay`, `timestamp`,`stop_sequence`) VALUES (?,?,?,?)");
        for (Delay d : delays) {
            s.setString(1, tripInfo.getTrip_id());
            s.setInt(2, d.getSeconds());
            s.setString(3, SQLFormatTools.sqlDatetimeFormat.format(Date.from(d.getTimestamp().atZone(ZoneId.of("Europe/Berlin")).toInstant())));
            s.setInt(4, d.getGtfsStop().getStop_sequence());
            s.addBatch();
        }
        s.executeBatch();
        s.close();
        ds.close();
    }

}