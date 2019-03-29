package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PrognosisDatabase {
    public static boolean insertBlank(String tripId) throws SQLException {
        Connection c = DataSource.getConnection();

        PreparedStatement s = c.prepareStatement("INSERT INTO `prognosis`(`trip_id`) VALUES (?)");
        s.setString(1,tripId);
        boolean execute = s.execute();

        s.close();
        c.close();

        return execute;
    }
}
