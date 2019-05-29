package database.procedures;

import database.GTFS;
import database.UserDataDatabase;
import common.network.*;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import utilities.Chronometer;
import utilities.MathToolbox;

import java.sql.SQLException;
import java.util.ArrayList;

/**
 * this class takes care of the import procedure for user recorded data. At first it parses the json and then imports
 * answer and delay list depending on which one is set or both if both are set. This is done by migrating the text values
 * of the questionnaire into numbers which are better for statistical calculations. For the location import there is a bit
 * more complex code algorithm going on.
 * <p>For every location which was recorded from the users device this algorithm searches for the closest stop point and
 * adds it as closest. If there is already one assigned it checks whether the new one is closer. This is done to get more
 * accurate results because only the delay which was measured at the closest to the station should be used.</p>
 */
public class UserRecordingImporter {
    private static Logger logger = Logger.getLogger(UserRecordingData.class);

    /**
     * this function is the starting point for this procedure. It coordinates the whole process by starting corresponding
     * functions.
     * @param json the json which contains all data to import.
     */
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

    /**
     * this function imports the answers from the userrecordingdata by first checking for the gtfs trip ids to assign the
     * answers to the right trips and then starting the actual process
     * @param data
     */
    private static void importAnswers(UserRecordingData data) {
        Connection c = data.getConnection();

        try {
            addTripIdsToConnection(c);

            addAnswersToDatabase(data);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * This function takes care of providing the trip IDs to the connection by asking the database for the right trip IDs
     * and setting these results into the connections trips
     * @param c the connection for which the trip IDs should be inserted
     * @throws SQLException when the database throws errors
     */
    private static void addTripIdsToConnection(Connection c) throws SQLException {
        for (Trip t : c.getLegs()) {
            String tripId = GTFS.getGTFSTripId(t);
            t.setGTFSTripId(tripId);
        }

    }

    /**
     * this function adds the answers from the given data into the database by mapping number values to the text values.
     * See {@link Answer} for mapping info.
     * @param data the data which should be imported to the database. Must include connection and answers attribute.
     * @throws SQLException when the database throws errors
     */
    private static void addAnswersToDatabase(UserRecordingData data) throws SQLException {
        for (int i = 0; i < data.getAnswers().size(); i++) {
            ArrayList<Trip> legs = data.getConnection().getLegs();

            if (i < legs.size()) {
                String gtfsTripId = legs.get(i).getGTFSTripId();
                Answer answer = data.getAnswers().get(i);
                String nextTripId = i + 1 >= legs.size() ? null : legs.get(i + 1).getGTFSTripId();

                UserDataDatabase.addAnswer(gtfsTripId, answer, nextTripId);
            }
        }
    }

    /**
     * this is the final function call which adds the data with updated delay information into the database.
     * @param c the connection containing the stops that whose new delay data is inserted into the database
     * @throws SQLException when the database throws errors
     */
    private static void addRecordingToDatabase(Connection c) throws SQLException {
        for (Trip t : c.getLegs()) {
            String tripId = t.getGTFSTripId();

            if (t.getBoarding().hasCalculatedDelay()) {
                UserDataDatabase.addRecordingData(tripId, t.getBoarding());
            }

            for (StopPoint s : t.getIntermediates()) {
                if (s.hasCalculatedDelay()) {
                    UserDataDatabase.addRecordingData(tripId, s);
                }
            }

            if (t.getAlighting().hasCalculatedDelay()) {
                UserDataDatabase.addRecordingData(tripId, t.getAlighting());
            }
        }
    }

    /**
     * this is the first function call for importing the location data. it creates a stoplist with all stops of the whole
     * connection that have to be processed and adds stopsequences corresponding to the gtfs trip to it to get
     * @param data the data which should be processed. Must include connections and locations attribute.
     */
    private static void importLocationData(UserRecordingData data) {
        ArrayList<StopPoint> stops = data.getConnection().extractAllStops();

        try {
            GTFS.getLocationDataForStopList(stops);
            GTFS.addStopSequencesForConnection(data.getConnection());
            findNearestStations(data);
            addRecordingToDatabase(data.getConnection());

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * This function parses a list of locations to find the nearest stations of the connection which is included in the
     * parameter. For every location which was recorded from the users device this algorithm searches for the closest stop point and
     * adds it as closest. If there is already one assigned it checks whether the new one is closer. This is done to get more
     * accurate results because only the delay which was measured at the closest to the station should be used.
     * @param data the data which should be processed. Must include connections and locations attribute
     * @return the list of stopPoints which were updated.
     */
    private static ArrayList<StopPoint> findNearestStations(UserRecordingData data) {
        ArrayList<CustomLocation> locations = data.getLocations();
        ArrayList<StopPoint> stops = data.getConnection().extractAllStops();
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
                if (!data.getConnection().isAnAlighting(closestStopPoint)) {
                    closestStopPoint.setDelay(l.getTime());
                }
                closestStopPoint.setMinDistance((int) currentDistance);
                stopsWithUpdatedData.add(closestStopPoint);
            }
        }
        return stopsWithUpdatedData;
    }
}
