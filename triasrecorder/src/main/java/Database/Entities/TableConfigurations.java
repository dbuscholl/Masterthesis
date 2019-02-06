package Database.Entities;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class is used for validating the database structures for GTFS Times. It contains all tablenames and fields.
 * It also indicates which of them are required and which datatype in SQL is used. The representations is like follows:
 * <p><code>HashMap<Tablename-String><HashMap<Columnname-String><SQL-Datatype-String>></code></p>
 * It also contains an ArrayList with column names which are optional and can be nulled.<br>
 */
public class TableConfigurations {
    private static final HashMap<String, HashMap<String, String>> tableMap = new HashMap<>();
    private static final ArrayList<String> optionals = new ArrayList<>();

    /**
     * Static Class, so no constructors needed
     */
    private TableConfigurations(){}

    /**
     * returns the entire tablemap.
     * @return the hashmap containing the tablename as String for the key. It's value is another hashmap containing the
     * columnname as String for the key and the Datatype used in SQL for the value.
     */
    public static HashMap<String, HashMap<String, String>> getMap() {
        if(tableMap.isEmpty()) {
            initializeTableMap();
        }
        return tableMap;
    }

    /**
     *
     * @return the arraylist containing String representations of the column names which are optional and can be nulled
     */
    public static ArrayList<String> getOptionals() {
        if(optionals.size() == 0) {
            initializeOptionals();
        }
        return optionals;
    }

    /**
     * The Main initializer for the class.
     */
    private static void initializeTableMap() {
        tableMap.put("agency", new HashMap<>());
        tableMap.put("stops", new HashMap<>());
        tableMap.put("routes", new HashMap<>());
        tableMap.put("trips", new HashMap<>());
        tableMap.put("stop_times", new HashMap<>());
        tableMap.put("calendar", new HashMap<>());
        tableMap.put("calendar_dates", new HashMap<>());
        tableMap.put("fare_rules", new HashMap<>());
        tableMap.put("fare_attributes", new HashMap<>());
        tableMap.put("shapes", new HashMap<>());
        tableMap.put("frequencies", new HashMap<>());
        tableMap.put("transfers", new HashMap<>());
        tableMap.put("feed_info", new HashMap<>());

        tableMap.get("agency").put("agency_id", "varchar(255)");
        tableMap.get("agency").put("agency_name", "varchar(255)");
        tableMap.get("agency").put("agency_url", "varchar(255)");
        tableMap.get("agency").put("agency_timezone", "varchar(255)");
        tableMap.get("agency").put("agency_lang", "varchar(255)");
        tableMap.get("agency").put("agency_phone", "varchar(255)");
        tableMap.get("agency").put("agency_fare_url", "varchar(255)");
        tableMap.get("agency").put("agency_email", "varchar(255)");

        tableMap.get("stops").put("stop_id", "varchar(255)");
        tableMap.get("stops").put("stop_code", "varchar(255)");
        tableMap.get("stops").put("stop_name", "varchar(255)");
        tableMap.get("stops").put("stop_desc", "varchar(255)");
        tableMap.get("stops").put("stop_lat", "varchar(255)");
        tableMap.get("stops").put("stop_lon", "varchar(255)");
        tableMap.get("stops").put("zone_id", "varchar(255)");
        tableMap.get("stops").put("stop_url", "varchar(255)");
        tableMap.get("stops").put("location_type", "varchar(255)");
        tableMap.get("stops").put("parent_station", "varchar(255)");
        tableMap.get("stops").put("stop_timezone", "varchar(255)");
        tableMap.get("stops").put("wheelchair_boarding", "varchar(255)");

        tableMap.get("routes").put("route_id", "varchar(255)");
        tableMap.get("routes").put("agency_id", "varchar(255)");
        tableMap.get("routes").put("route_short_name", "varchar(255)");
        tableMap.get("routes").put("route_long_name", "varchar(255)");
        tableMap.get("routes").put("route_desc", "varchar(255)");
        tableMap.get("routes").put("route_type", "varchar(255)");
        tableMap.get("routes").put("route_url", "varchar(255)");
        tableMap.get("routes").put("route_color", "varchar(255)");
        tableMap.get("routes").put("route_text_color", "varchar(255)");
        tableMap.get("routes").put("route_sort_order", "varchar(255)");

        tableMap.get("trips").put("route_id", "varchar(255)");
        tableMap.get("trips").put("service_id", "varchar(255)");
        tableMap.get("trips").put("trip_id", "varchar(255)");
        tableMap.get("trips").put("trip_headsign", "varchar(255)");
        tableMap.get("trips").put("trip_short_name", "varchar(255)");
        tableMap.get("trips").put("direction_id", "varchar(255)");
        tableMap.get("trips").put("block_id", "varchar(255)");
        tableMap.get("trips").put("shape_id", "varchar(255)");
        tableMap.get("trips").put("wheelchair_accessible", "varchar(255)");
        tableMap.get("trips").put("bikes_allowed", "varchar(255)");

        tableMap.get("stop_times").put("trip_id", "varchar(255)");
        tableMap.get("stop_times").put("arrival_time", "time");
        tableMap.get("stop_times").put("departure_time", "time");
        tableMap.get("stop_times").put("stop_id", "varchar(255)");
        tableMap.get("stop_times").put("stop_sequence", "varchar(255)");
        tableMap.get("stop_times").put("stop_headsign", "varchar(255)");
        tableMap.get("stop_times").put("pickup_type", "varchar(255)");
        tableMap.get("stop_times").put("drop_off_type", "varchar(255)");
        tableMap.get("stop_times").put("shape_dist_traveled", "varchar(255)");
        tableMap.get("stop_times").put("timepoint", "varchar(255)");

        tableMap.get("calendar").put("service_id", "varchar(255)");
        tableMap.get("calendar").put("monday", "varchar(255)");
        tableMap.get("calendar").put("tuesday", "varchar(255)");
        tableMap.get("calendar").put("wednesday", "varchar(255)");
        tableMap.get("calendar").put("thursday", "varchar(255)");
        tableMap.get("calendar").put("friday", "varchar(255)");
        tableMap.get("calendar").put("saturday", "varchar(255)");
        tableMap.get("calendar").put("sunday", "varchar(255)");
        tableMap.get("calendar").put("start_date", "date");
        tableMap.get("calendar").put("end_date", "date");

        tableMap.get("calendar_dates").put("service_id", "varchar(255)");
        tableMap.get("calendar_dates").put("date", "varchar(255)");
        tableMap.get("calendar_dates").put("exception_type", "varchar(255)");

        tableMap.get("fare_attributes").put("fare_id", "varchar(255)");
        tableMap.get("fare_attributes").put("price", "varchar(255)");
        tableMap.get("fare_attributes").put("currency_type", "varchar(255)");
        tableMap.get("fare_attributes").put("payment_method", "varchar(255)");
        tableMap.get("fare_attributes").put("transfers", "varchar(255)");
        tableMap.get("fare_attributes").put("agency_id", "varchar(255)");
        tableMap.get("fare_attributes").put("transfer_duration", "varchar(255)");

        tableMap.get("fare_rules").put("fare_id", "varchar(255)");
        tableMap.get("fare_rules").put("route_id", "varchar(255)");
        tableMap.get("fare_rules").put("origin_id", "varchar(255)");
        tableMap.get("fare_rules").put("destination_id", "varchar(255)");
        tableMap.get("fare_rules").put("contains_id", "varchar(255)");

        tableMap.get("shapes").put("shape_id","varchar(255)");
        tableMap.get("shapes").put("shape_pt_lat","varchar(255)");
        tableMap.get("shapes").put("shape_pt_lon    ","varchar(255)");
        tableMap.get("shapes").put("shape_pt_sequence","varchar(255)");
        tableMap.get("shapes").put("shape_dist_traveled","varchar(255)");

        tableMap.get("frequencies").put("trip_id","varchar(255)");
        tableMap.get("frequencies").put("start_time","varchar(255)");
        tableMap.get("frequencies").put("end_time","varchar(255)");
        tableMap.get("frequencies").put("headway_secs","varchar(255)");
        tableMap.get("frequencies").put("exact_times","varchar(255)");

        tableMap.get("transfers").put("from_stop_id","varchar(255)");
        tableMap.get("transfers").put("to_stop_id","varchar(255)");
        tableMap.get("transfers").put("transfer_type","varchar(255)");
        tableMap.get("transfers").put("min_transfer_time","varchar(255)");

        tableMap.get("feed_info").put("feed_publisher_name","varchar(255)");
        tableMap.get("feed_info").put("feed_publisher_url","varchar(255)");
        tableMap.get("feed_info").put("feed_lang","varchar(255)");
        tableMap.get("feed_info").put("feed_start_date","varchar(255)");
        tableMap.get("feed_info").put("feed_end_date","varchar(255)");
        tableMap.get("feed_info").put("feed_version","varchar(255)");
        tableMap.get("feed_info").put("feed_contact_email","varchar(255)");
        tableMap.get("feed_info").put("feed_contact_url","varchar(255)");
    }

    /**
     * initalizes the optionals arraylist
     */
    public static void initializeOptionals() {
        optionals.add("calendar_dates");
        optionals.add("fare_attributes");
        optionals.add("fare_rules");
        optionals.add("shapes");
        optionals.add("frequencies");
        optionals.add("transfers");
        optionals.add("feed_info");
        optionals.add("agency_lang");
        optionals.add("agency_phone");
        optionals.add("agency_fare_url");
        optionals.add("agency_email");
        optionals.add("stop_code");
        optionals.add("stop_desc");
        optionals.add("zone_id");
        optionals.add("stop_url");
        optionals.add("location_type");
        optionals.add("parent_station");
        optionals.add("stop_timezone");
        optionals.add("wheelchair_boarding");
        optionals.add("route_short_name");
        optionals.add("route_long_name");
        optionals.add("route_desc");
        optionals.add("route_url");
        optionals.add("route_color");
        optionals.add("trip_headsign");
        optionals.add("trip_short_name");
        optionals.add("direction_id");
        optionals.add("block_id");
        optionals.add("shape_id");
        optionals.add("wheelchair_accessible");
        optionals.add("bikes_allowed");
        optionals.add("stop_headsign");
        optionals.add("pickup_type");
        optionals.add("drop_off_type");
        optionals.add("shape_dist_traveled");
        optionals.add("timepoint");
        optionals.add("transfer_duration");
        optionals.add("route_id");
        optionals.add("origin_id");
        optionals.add("destination_id");
        optionals.add("contains_id");
        optionals.add("shape_dist_traveled");
        optionals.add("exact_times");
        optionals.add("min_transfer_time");
        optionals.add("feed_start_date");
        optionals.add("feed_end_date");
        optionals.add("feed_version");
        optionals.add("feed_contact_email");
        optionals.add("feed_contact_url");
    }
}
