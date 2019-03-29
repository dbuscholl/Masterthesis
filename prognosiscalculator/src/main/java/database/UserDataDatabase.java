package database;

import common.network.Answer;
import common.network.StopPoint;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class UserDataDatabase {
    public static void addAnswer(String tripId, Answer answer, String nextTripId) throws SQLException {
        Connection c = DataSource.getConnection();
        PreparedStatement s = c.prepareStatement("INSERT INTO `user_answers`(`trip_id`, `capacity`, `cleanness`, `delay`, `successfullyInterchanged`, `interchangeToTripId`) VALUES (?,?,?,?,?,?)");
        s.setString(1, tripId);
        s.setInt(2, answer.getCapacity());
        s.setInt(3, answer.getCleanness());
        s.setInt(4, answer.getDelay());
        s.setBoolean(5, answer.isSuccessfullyInterchanged());
        s.setString(6, nextTripId);
        s.execute();
        s.close();
        c.close();
    }

    public static void addRecordingData(String tripId, StopPoint stop) throws SQLException {
        if(tripId == null || !stop.hasCalculatedDelay() || stop.getStopSequence() == -1) {
            return;
        }

        Connection c = DataSource.getConnection();
        PreparedStatement s = c.prepareStatement("INSERT INTO `user_recordings`(`trip_id`, `stop_sequence`, `delay`, `minDistanceToStop`) VALUES (?,?,?,?)");
        s.setString(1, tripId);
        s.setInt(2, stop.getStopSequence());
        s.setDouble(3, stop.getDelay());
        s.setInt(4, stop.getMinDistance());
        s.execute();
        s.close();
        c.close();
    }
}
