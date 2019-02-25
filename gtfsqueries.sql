# All delays for specific trip
#SELECT * FROM vvs.delays WHERE tripId= '8.T0.31-162-j19-2.1.H';

# stop times information for specific trip
#SELECT * FROM vvs.stop_times LEFT JOIN stops ON stop_times.stop_id = stops.stop_id WHERE trip_id = "89.T0.30-65-j19-1.3.H";

# route details for specific trip
#SELECT * FROM trips LEFT JOIN routes ON routes.route_id = trips.route_id WHERE trip_id = "8.T0.31-162-j19-2.1.H"

# Grouped by tripId and Date with avg
#SELECT avg(delay), tripId, route_short_name, trip_headsign, min(timestamp) as timestamp, min(stop_sequence) as stop_sequence FROM vvs.delays LEFT JOIN trips on trips.trip_id = delays.tripId LEFT JOIN routes on routes.route_id = trips.route_id WHERE route_short_name LIKE "65" AND stop_sequence BETWEEN 6 AND 11 AND time(`timestamp`) BETWEEN "06:25:00" AND "06:35:00" GROUP BY tripId, date(`timestamp`) ORDER BY timestamp DESC

# average delay per route and day
#SELECT avg(delay), route_short_name, min(date(timestamp)) as date FROM vvs.delays LEFT JOIN trips on trips.trip_id = delays.tripId LEFT JOIN routes on routes.route_id = trips.route_id GROUP BY route_short_name, date(`timestamp`) ORDER BY route_short_name, date asc;

# latest delays with route and trip headsign
# CREATE INDEX idx_tid ON delays(tripId);
# SELECT delay, tripId, route_short_name, trip_headsign, timestamp FROM vvs.delays LEFT JOIN trips on trips.trip_id = delays.tripId LEFT JOIN routes on routes.route_id = trips.route_id WHERE route_short_name LIKE "U7" ORDER BY timestamp DESC;

# overall average delay entries per trip
#SELECT DISTINCT avg(counting) FROM (SELECT count(delay) as counting, tripId fROM delays GROUP BY tripId) x;