package database.procedures;

import database.GTFS;
import database.UserData;
import entities.network.*;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import utilities.Chronometer;
import utilities.MathToolbox;

import java.sql.SQLException;
import java.util.ArrayList;

public class UserRecordingImporter {
    private static Logger logger = Logger.getLogger(UserRecordingData.class);

    public static void doWork(JSONObject json) {
        Chronometer chronometer = new Chronometer();
        chronometer.addNow();

        UserRecordingData userRecordingData = new UserRecordingData(json);

        chronometer.addNow();
        logger.info("Done parsing data in " + chronometer.getLastDifferece() + "ms!");

        importAnswers(userRecordingData);

        chronometer.addNow();
        logger.info("Done adding answers in " + chronometer.getLastDifferece() + "ms!");

        importLocationData(userRecordingData);
        chronometer.addNow();
        logger.info("Done adding recording data in " + chronometer.getLastDifferece() + "ms!");
    }

    private static void importAnswers(UserRecordingData data) {
        Connection c = data.getConnection();

        try {
            addTripIdsToConnection(c);

            addAnswersToDatabase(data);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void addTripIdsToConnection(Connection c) throws SQLException {
        for (Trip t : c.getLegs()) {
            String tripId = GTFS.getTripId(t);
            t.setGTFSTripId(tripId);
        }

    }

    private static void addAnswersToDatabase(UserRecordingData data) throws SQLException {
        for (int i = 0; i < data.getAnswers().size(); i++) {
            ArrayList<Trip> legs = data.getConnection().getLegs();

            if (i < legs.size()) {
                String gtfsTripId = legs.get(i).getGTFSTripId();
                Answer answer = data.getAnswers().get(i);
                String nextTripId = i + 1 >= legs.size() ? null : legs.get(i + 1).getGTFSTripId();

                UserData.addAnswer(gtfsTripId, answer, nextTripId);
            }
        }
    }

    private static void addRecordingToDatabase(Connection c) throws SQLException {
        for (Trip t : c.getLegs()) {
            String tripId = t.getGTFSTripId();

            if (t.getBoarding().hasCalculatedDelay()) {
                UserData.addRecordingData(tripId, t.getBoarding());
            }

            for (StopPoint s : t.getIntermediates()) {
                if (s.hasCalculatedDelay()) {
                    UserData.addRecordingData(tripId, s);
                }
            }

            if (t.getAlighting().hasCalculatedDelay()) {
                UserData.addRecordingData(tripId, t.getAlighting());
            }
        }
    }

    private static void importLocationData(UserRecordingData data) {
        ArrayList<Trip> legs = data.getConnection().getLegs();
        ArrayList<StopPoint> stops = extractStopsFromConnection(legs);

        try {
            GTFS.getLocationDataForStopList(stops);
            GTFS.addStopSequencesForConnection(data.getConnection());
            findNearestStations(data);
            addRecordingToDatabase(data.getConnection());

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static ArrayList<StopPoint> findNearestStations(UserRecordingData data) {
        ArrayList<CustomLocation> locations = data.getLocations();
        ArrayList<StopPoint> stops = extractStopsFromConnection(data.getConnection());
        ArrayList<StopPoint> stopsWithUpdatedData = new ArrayList<>();

        for (CustomLocation l : locations) {
            StopPoint closestStopPoint = null;
            float closestDistance = Float.MAX_VALUE;
            double userLat = l.getLatitude();
            double userLon = l.getLongitude();
            float currentDistance = Float.MAX_VALUE;

            for (StopPoint stop : stops) {
                double stopLat = stop.getLatitude();
                double stopLon = stop.getLongitude();

                if (stopLat <= 0 || stopLon <= 0) {
                    continue;
                }

                currentDistance = MathToolbox.meterDistanceBetween((float) userLat, (float) userLon, (float) stopLat, (float) stopLon);

                if (currentDistance < closestDistance) {
                    closestStopPoint = stop;
                    closestDistance = currentDistance;
                }
            }

            if (closestStopPoint != null) {
                if (!isAlighting(data, closestStopPoint)) {
                    closestStopPoint.setDelay(l.getTime());
                }
                closestStopPoint.setMinDistance((int) currentDistance);
                stopsWithUpdatedData.add(closestStopPoint);
            }
        }
        return stopsWithUpdatedData;
    }

    private static boolean isAlighting(UserRecordingData data, StopPoint closestStopPoint) {
        for (Trip t : data.getConnection().getLegs()) {
            if (t.getAlighting() == closestStopPoint) {
                return true;
            }
        }
        return false;
    }

    public static ArrayList<StopPoint> extractStopsFromConnection(Connection c) {
        return extractStopsFromConnection(c.getLegs());
    }

    private static ArrayList<StopPoint> extractStopsFromConnection(ArrayList<Trip> legs) {
        ArrayList<StopPoint> stops = new ArrayList<>();

        for (Trip t : legs) {
            stops.add(t.getBoarding());
            stops.addAll(t.getIntermediates());
            stops.add(t.getAlighting());
        }

        return stops;
    }
}
