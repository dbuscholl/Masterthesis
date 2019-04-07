package database;

import common.network.Trip;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class PrognosisDatabase {
    public static boolean insertBlank(String tripId, String operatingDay, String journeyRef) throws SQLException {
        Connection c = DataSource.getConnection();

        PreparedStatement s = c.prepareStatement("INSERT INTO `prognosis`(`trip_id`, `operatingDayRef`, `journeyRef`) VALUES (?,?,?)");
        s.setString(1, tripId);
        s.setString(2, operatingDay);
        s.setString(3, journeyRef);
        boolean execute = s.execute();

        s.close();
        c.close();

        return execute;
    }

    public static PrognosisItem getBlank(String operatingDay, String journeyRef) throws SQLException {
        PrognosisItem item = new PrognosisItem();
        Connection c = DataSource.getConnection();

        PreparedStatement s = c.prepareStatement("SELECT * FROM `prognosis` WHERE `operatingDayRef` = ? AND `journeyRef` = ?");
        s.setString(1, operatingDay);
        s.setString(2, journeyRef);
        ResultSet rs = s.executeQuery();
        if (rs.next()) {
            item.setJson(rs.getString("prognosted_delay"));
            item.setTimestamp(rs.getTimestamp("timestamp").getTime());
        } else {
            item = null;
        }

        rs.close();
        s.close();
        c.close();

        return item;
    }

    public static boolean update(ArrayList<Trip> legs, JSONArray output) throws SQLException {
        for (int i = 0; i < legs.size(); i++) {
            Trip t = legs.get(i);
            String operatingDayRef = t.getService().getOperatingDayRef();
            String journeyRef = t.getService().getJourneyRef();
            if (i >= output.length()) {
                return false;
            }
            JSONObject json = output.getJSONObject(i);

            Connection c = DataSource.getConnection();
            PreparedStatement s = c.prepareStatement("UPDATE `prognosis` SET `prognosted_delay`= ? WHERE `operatingDayRef` = ? AND `journeyRef` = ?");
            s.setString(1, json.toString());
            s.setString(2, operatingDayRef);
            s.setString(3, journeyRef);
            int execute = s.executeUpdate();

            s.close();
            c.close();

            if (execute == 0) {
                return false;
            }
        }
        return true;
    }

    public static boolean removeEntry(String operatingDay, String journeyRef) throws SQLException {
        Connection c = DataSource.getConnection();

        PreparedStatement s = c.prepareStatement("DELETE FROM `prognosis` WHERE `operatingDayRef` = ? AND `journeyRef` = ?");
        s.setString(1, operatingDay);
        s.setString(2, journeyRef);
        int affected = s.executeUpdate();

        s.close();
        c.close();

        return affected > 0;
    }

    public static void createUserAnswersTable() throws SQLException {
        Connection c = DataSource.getConnection();
        PreparedStatement s = c.prepareStatement("CREATE TABLE IF NOT EXISTS `user_answers` (" +
                "`trip_id` varchar(255) NOT NULL," +
                "`capacity` int(11) NOT NULL," +
                "`cleanness` int(11) NOT NULL," +
                "`delay` int(11) NOT NULL," +
                "`successfullyInterchanged` tinyint(1) NOT NULL," +
                "`interchangeToTripId` varchar(255) DEFAULT NULL," +
                "`timestamp` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                "PRIMARY KEY (`trip_id`,`timestamp`)," +
                "KEY `trip_id` (`trip_id`))");

        s.execute();
        s.close();
        c.close();
    }

    public static void createUserRecordingsTable() throws SQLException {
        Connection c = DataSource.getConnection();
        PreparedStatement s = c.prepareStatement("CREATE TABLE IF NOT EXISTS `user_recordings` (" +
                "`trip_id` varchar(255) NOT NULL," +
                "`stop_sequence` int(11) NOT NULL," +
                "`delay` double NOT NULL," +
                "`minDistanceToStop` int(11) NOT NULL," +
                "`timestamp` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                "KEY `trip_id` (`trip_id`))");

        s.execute();
        s.close();
        c.close();
    }

    public static void createPrognosisTable() throws SQLException {
        Connection c = DataSource.getConnection();
        PreparedStatement s = c.prepareStatement("CREATE TABLE IF NOT EXISTS `prognosis` (" +
                "`trip_id` varchar(255) NOT NULL," +
                "`journeyRef` varchar(50) NOT NULL," +
                "`operatingDayRef` varchar(50) NOT NULL," +
                "`prognosted_delay` text," +
                "`timestamp` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                "PRIMARY KEY (`journeyRef`,`operatingDayRef`))");

        s.execute();
        s.close();
        c.close();
    }

    public static class PrognosisItem {
        private long timestamp;
        private String json;

        public PrognosisItem() {
        }

        public PrognosisItem(long timestamp, String json) {
            this.timestamp = timestamp;
            this.json = json;
        }


        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }

        public String getJson() {
            return json;
        }

        public void setJson(String json) {
            this.json = json;
        }
    }
}
