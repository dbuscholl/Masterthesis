package database;

import entities.network.Answer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class UserData {
    public static void addAnswer(String tripId, Answer answer) throws SQLException {
        Connection c = DataSource.getConnection();
        PreparedStatement s = c.prepareStatement("INSERT INTO `user_answers`(`trip_id`, `capacity`, `cleanness`, `delay`, `successfullyInterchanged`) VALUES (?,?,?,?,?)");
        s.setString(1, tripId);
        s.setInt(2, answer.getCapacity());
        s.setInt(3, answer.getCleanness());
        s.setInt(4, answer.getDelay());
        s.setBoolean(5, answer.isSuccessfullyInterchanged());
        s.execute();
        s.close();
        c.close();
    }
}
