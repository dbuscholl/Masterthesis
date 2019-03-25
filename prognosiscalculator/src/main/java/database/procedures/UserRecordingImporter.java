package database.procedures;

import database.GTFS;
import database.UserData;
import entities.network.Answer;
import entities.network.Connection;
import entities.network.Trip;
import entities.network.UserRecordingData;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import utilities.Chronometer;

import java.sql.SQLException;

public class UserRecordingImporter {
    private static Logger logger = Logger.getLogger(UserRecordingData.class);

    private static void doWork(JSONObject json) {
        Chronometer chronometer = new Chronometer();
        chronometer.addNow();

        UserRecordingData userRecordingData = new UserRecordingData(json);

        chronometer.addNow();
        logger.info("Done parsing data in " + chronometer.getLastDifferece() + "ms!");

        importAnswers(userRecordingData);

        chronometer.addNow();
        logger.info("Done adding answers in " + chronometer.getLastDifferece() + "ms!");
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
            if (i < data.getConnection().getLegs().size()) {
                String gtfsTripId = data.getConnection().getLegs().get(i).getGTFSTripId();
                Answer answer = data.getAnswers().get(i);
                UserData.addAnswer(gtfsTripId, answer);
            }
        }
    }
}
