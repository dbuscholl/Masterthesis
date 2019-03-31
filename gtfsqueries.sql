# All delays for specific trip
SELECT * FROM vvs.delays WHERE tripId= '8.T0.31-162-j19-2.1.H';
SELECT * FROM vvs.delays WHERE tripId= '8.T0.31-162-j19-2.1.H' and date(`timestamp`) = "2019-02-19";

# stop times information for specific trip
#SELECT * FROM vvs.stop_times LEFT JOIN stops ON stop_times.stop_id = stops.stop_id WHERE trip_id = "8.T0.31-162-j19-2.1.H";


#SELECT * FROM vvs.stop_times AS ta1 LEFT JOIN stops ON ta1.stop_id = stops.stop_id WHERE stop_name = "Heumaden Rose" OR stop_name = "Heumaden Bockelstr." AND (Select count(*) FROM stop_times AS ta2 WHERE ta1.trip_id = ta2.trip_id) > 1
#SELECT trips.service_id, trips.trip_id, arrival_time, departure_time, stop_sequence, stop_name, trip_headsign, route_short_name FROM vvs.stop_timstop_timeses LEFT JOIN stops ON stop_times.stop_id = stops.stop_id LEFT JOIN trips ON trips.trip_id = stop_times.trip_id LEFT JOIN routes ON routes.route_id = trips.route_id WHERE stop_name = "Heumaden Rose" AND departure_time = "09:16:00"
# route details for specific trip
#SELECT * FROM trips LEFT JOIN routes ON routes.route_id = trips.route_id WHERE trip_id = "145.T0.20-7-j19-1.1.H"

# Grouped by tripId and Date with avg
#SELECT avg(delay), tripId, route_short_name, trip_headsign, min(timestamp) as timestamp, min(stop_sequence) as stop_sequence FROM vvs.delays LEFT JOIN trips on trips.trip_id = delays.tripId LEFT JOIN routes on routes.route_id = trips.route_id WHERE route_short_name LIKE "65" AND stop_sequence BETWEEN 6 AND 11 AND time(`timestamp`) BETWEEN "06:25:00" AND "06:35:00" GROUP BY tripId, date(`timestamp`) ORDER BY timestamp DESC

# average delay per route and day
#SELECT avg(delay), route_short_name, min(date(timestamp)) as date FROM vvs.delays LEFT JOIN trips on trips.trip_id = delays.tripId LEFT JOIN routes on routes.route_id = trips.route_id GROUP BY route_short_name, date(`timestamp`) ORDER BY route_short_name, date asc;

# latest delays with route and trip headsign
# CREATE INDEX idx_tid ON delays(tripId);
# SELECT delay, tripId, route_short_name, trip_headsign, timestamp FROM vvs.delays LEFT JOIN trips on trips.trip_id = delays.tripId LEFT JOIN routes on routes.route_id = trips.route_id WHERE route_short_name LIKE "U7" ORDER BY timestamp DESC;

# overall average delay entries per trip
#SELECT DISTINCT avg(counting) FROM (SELECT count(delay) as counting, tripId fROM delays GROUP BY tripId) x;