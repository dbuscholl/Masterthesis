package database;

import common.network.Answer;
import common.network.StopPoint;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * This is an additional class handling user-data stuff in connection with the database.
 */
public class UserDataDatabase {
    /**
     * adds an anser into the database
     * @param tripId the ID of the trip for which the answer was given
     * @param answer the actual answer value
     * @param nextTripId the next tripID if there was an interchange. This value is used to determine whether an interchange
     *                   was successful
     * @throws SQLException when something goes wrong during database operations
     */
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

    /**
     * adds a recording data (delay) into the database
     * @param tripId the ID of the trip for which the answer was given
     * @param stop the stoppoint containing a delay which should be added
     * @throws SQLException when something goes wrong during database operations
     */
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
