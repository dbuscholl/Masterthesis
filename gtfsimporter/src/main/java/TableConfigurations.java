import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class TableConfigurations {
    private static final HashMap<String, HashMap<String, String>> tableMap = new HashMap<>();
    private static final ArrayList<String> optionals = new ArrayList<>();

    private TableConfigurations(){}

    public static HashMap<String, HashMap<String, String>> getMap() {
        if(tableMap.isEmpty()) {
            initializeTableMap();
        }
        return tableMap;
    }

    public static ArrayList<String> getOptionals() {
        if(optionals.size() == 0) {
            initializeOptionals();
        }
        return optionals;
    }

    private static void initializeTableMap() {
        tableMap.put("agency.txt", new HashMap<>());
        tableMap.put("stops.txt", new HashMap<>());
        tableMap.put("routes.txt", new HashMap<>());
        tableMap.put("trips.txt", new HashMap<>());
        tableMap.put("stop_times.txt", new HashMap<>());
        tableMap.put("calendar.txt", new HashMap<>());
        tableMap.put("calendar_dates.txt", new HashMap<>());
        tableMap.put("fare_rules.txt", new HashMap<>());
        tableMap.put("fare_attributes.txt", new HashMap<>());
        tableMap.put("shapes.txt", new HashMap<>());
        tableMap.put("frequencies.txt", new HashMap<>());
        tableMap.put("transfers.txt", new HashMap<>());
        tableMap.put("feed_info.txt", new HashMap<>());

        tableMap.get("agency.txt").put("agency_id", "varchar(255)");
        tableMap.get("agency.txt").put("agency_name", "varchar(255)");
        tableMap.get("agency.txt").put("agency_url", "varchar(255)");
        tableMap.get("agency.txt").put("agency_timezone", "varchar(255)");
        tableMap.get("agency.txt").put("agency_lang", "varchar(255)");
        tableMap.get("agency.txt").put("agency_phone", "varchar(255)");
        tableMap.get("agency.txt").put("agency_fare_url", "varchar(255)");
        tableMap.get("agency.txt").put("agency_email", "varchar(255)");

        tableMap.get("stops.txt").put("stop_id", "varchar(255)");
        tableMap.get("stops.txt").put("stop_code", "varchar(255)");
        tableMap.get("stops.txt").put("stop_name", "varchar(255)");
        tableMap.get("stops.txt").put("stop_desc", "varchar(255)");
        tableMap.get("stops.txt").put("stop_lat", "varchar(255)");
        tableMap.get("stops.txt").put("stop_lon", "varchar(255)");
        tableMap.get("stops.txt").put("zone_id", "varchar(255)");
        tableMap.get("stops.txt").put("stop_url", "varchar(255)");
        tableMap.get("stops.txt").put("location_type", "varchar(255)");
        tableMap.get("stops.txt").put("parent_station", "varchar(255)");
        tableMap.get("stops.txt").put("stop_timezone", "varchar(255)");
        tableMap.get("stops.txt").put("wheelchair_boarding", "varchar(255)");

        tableMap.get("routes.txt").put("route_id", "varchar(255)");
        tableMap.get("routes.txt").put("agency_id", "varchar(255)");
        tableMap.get("routes.txt").put("route_short_name", "varchar(255)");
        tableMap.get("routes.txt").put("route_long_name", "varchar(255)");
        tableMap.get("routes.txt").put("route_desc", "varchar(255)");
        tableMap.get("routes.txt").put("route_type", "varchar(255)");
        tableMap.get("routes.txt").put("route_url", "varchar(255)");
        tableMap.get("routes.txt").put("route_color", "varchar(255)");
        tableMap.get("routes.txt").put("route_text_color", "varchar(255)");
        tableMap.get("routes.txt").put("route_sort_order", "varchar(255)");

        tableMap.get("trips.txt").put("route_id", "varchar(255)");
        tableMap.get("trips.txt").put("service_id", "varchar(255)");
        tableMap.get("trips.txt").put("trip_id", "varchar(255)");
        tableMap.get("trips.txt").put("trip_headsign", "varchar(255)");
        tableMap.get("trips.txt").put("trip_short_name", "varchar(255)");
        tableMap.get("trips.txt").put("direction_id", "varchar(255)");
        tableMap.get("trips.txt").put("block_id", "varchar(255)");
        tableMap.get("trips.txt").put("shape_id", "varchar(255)");
        tableMap.get("trips.txt").put("wheelchair_accessible", "varchar(255)");
        tableMap.get("trips.txt").put("bikes_allowed", "varchar(255)");

        tableMap.get("stop_times.txt").put("trip_id", "varchar(255)");
        tableMap.get("stop_times.txt").put("arrival_time", "time");
        tableMap.get("stop_times.txt").put("departure_time", "time");
        tableMap.get("stop_times.txt").put("stop_id", "varchar(255)");
        tableMap.get("stop_times.txt").put("stop_sequence", "varchar(255)");
        tableMap.get("stop_times.txt").put("stop_headsign", "varchar(255)");
        tableMap.get("stop_times.txt").put("pickup_type", "varchar(255)");
        tableMap.get("stop_times.txt").put("drop_off_type", "varchar(255)");
        tableMap.get("stop_times.txt").put("shape_dist_traveled", "varchar(255)");
        tableMap.get("stop_times.txt").put("timepoint", "varchar(255)");

        tableMap.get("calendar.txt").put("service_id", "varchar(255)");
        tableMap.get("calendar.txt").put("monday", "boolean");
        tableMap.get("calendar.txt").put("tuesday", "boolean");
        tableMap.get("calendar.txt").put("wednesday", "boolean");
        tableMap.get("calendar.txt").put("thursday", "boolean");
        tableMap.get("calendar.txt").put("friday", "boolean");
        tableMap.get("calendar.txt").put("saturday", "boolean");
        tableMap.get("calendar.txt").put("sunday", "boolean");
        tableMap.get("calendar.txt").put("start_date", "varchar(255)");
        tableMap.get("calendar.txt").put("end_date", "varchar(255)");

        tableMap.get("calendar_dates.txt").put("service_id", "varchar(255)");
        tableMap.get("calendar_dates.txt").put("date", "varchar(255)");
        tableMap.get("calendar_dates.txt").put("exception_type", "integer");

        tableMap.get("fare_attributes.txt").put("fare_id", "varchar(255)");
        tableMap.get("fare_attributes.txt").put("price", "varchar(255)");
        tableMap.get("fare_attributes.txt").put("currency_type", "varchar(255)");
        tableMap.get("fare_attributes.txt").put("payment_method", "varchar(255)");
        tableMap.get("fare_attributes.txt").put("transfers", "varchar(255)");
        tableMap.get("fare_attributes.txt").put("agency_id", "varchar(255)");
        tableMap.get("fare_attributes.txt").put("transfer_duration", "varchar(255)");

        tableMap.get("fare_rules.txt").put("fare_id", "varchar(255)");
        tableMap.get("fare_rules.txt").put("route_id", "varchar(255)");
        tableMap.get("fare_rules.txt").put("origin_id", "varchar(255)");
        tableMap.get("fare_rules.txt").put("destination_id", "varchar(255)");
        tableMap.get("fare_rules.txt").put("contains_id", "varchar(255)");

        tableMap.get("shapes.txt").put("shape_id","varchar(255)");
        tableMap.get("shapes.txt").put("shape_pt_lat","varchar(255)");
        tableMap.get("shapes.txt").put("shape_pt_lon    ","varchar(255)");
        tableMap.get("shapes.txt").put("shape_pt_sequence","varchar(255)");
        tableMap.get("shapes.txt").put("shape_dist_traveled","varchar(255)");

        tableMap.get("frequencies.txt").put("trip_id","varchar(255)");
        tableMap.get("frequencies.txt").put("start_time","varchar(255)");
        tableMap.get("frequencies.txt").put("end_time","varchar(255)");
        tableMap.get("frequencies.txt").put("headway_secs","varchar(255)");
        tableMap.get("frequencies.txt").put("exact_times","varchar(255)");

        tableMap.get("transfers.txt").put("from_stop_id","varchar(255)");
        tableMap.get("transfers.txt").put("to_stop_id","varchar(255)");
        tableMap.get("transfers.txt").put("transfer_type","varchar(255)");
        tableMap.get("transfers.txt").put("min_transfer_time","varchar(255)");

        tableMap.get("feed_info.txt").put("feed_publisher_name","varchar(255)");
        tableMap.get("feed_info.txt").put("feed_publisher_url","varchar(255)");
        tableMap.get("feed_info.txt").put("feed_lang","varchar(255)");
        tableMap.get("feed_info.txt").put("feed_start_date","varchar(255)");
        tableMap.get("feed_info.txt").put("feed_end_date","varchar(255)");
        tableMap.get("feed_info.txt").put("feed_version","varchar(255)");
        tableMap.get("feed_info.txt").put("feed_contact_email","varchar(255)");
        tableMap.get("feed_info.txt").put("feed_contact_url","varchar(255)");
    }

    public static void initializeOptionals() {
        optionals.add("calendar_dates.txt");
        optionals.add("fare_attributes.txt");
        optionals.add("fare_rules.txt");
        optionals.add("shapes.txt");
        optionals.add("frequencies.txt");
        optionals.add("transfers.txt");
        optionals.add("feed_info.txt");
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
