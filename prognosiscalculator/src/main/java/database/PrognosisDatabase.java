package database;

import common.network.Trip;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * This is an additional database class providing operation which have to do with prognosis data. <b>Note: With the next
 * update some functions from {@link GTFS} will be added here for semantic reasons.</b>
 */
public class PrognosisDatabase {

    /**
     * this function inserts a placeholder into the prognosis table to indicate a running calculation
     * @param tripId trip ID from gtfs for which the prognosis is calculated
     * @param operatingDay operatingDayRef parameter from TRIAS for which the prognosis is calculated
     * @param journeyRef journeyRef parameter which is kind of an ID from TRIAS for which the prognosis is calculated
     * @return true if the blank was inserted successful, false if not
     * @throws SQLException when something goes wrong during database operations
     */
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

    /**
     * gets a blank back as {@link PrognosisItem} which is already inserted into the database.
     * @param operatingDay operatingDayRef parameter from TRIAS for which the prognosis is calculated
     * @param journeyRef journeyRef parameter which is kind of an ID from TRIAS for which the prognosis is calculated
     * @return blank reference as prognosis item
     * @throws SQLException when something goes wrong during database operations
     */
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

    /**
     * updates the database with new prognosis data. There has to be a blank inserted in the database with the same
     * operatingDayRef and journeyRef before.
     * @param legs a list of legs containing the prognosis with
     * @param output
     * @return
     * @throws SQLException
     */
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

    /**
     * removes a blank from the prognosis database table
     * @param operatingDay operatingDayRef parameter from TRIAS for which the prognosis should be removed
     * @param journeyRef journeyRef parameter which is kind of an ID from TRIAS for which the prognosis should be removed
     * @return true if removing was successful, false if not
     * @throws SQLException when something goes wrong during a database operation
     */
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

    /**
     * this function creates the table with its structure where all answers collected by the questionnaire are stored
     * @throws SQLException  when something goes wrong during a database operation
     */
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

    /**
     * this function creates the table with its structure where all delay data recorded by the user is stored
     * @throws SQLException when something goes wrong during a database operation
     */
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

    /**
     * this function creates the table with its structure where all completed prognosis calculations are stored
     * @throws SQLException when something goes wrong during a database operation
     */
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

    /**
     * Prognosis Item entity class containing the json of calculation and the timestamp when it was created. This is
     * important for caching
     */
    public static class PrognosisItem {
        private long timestamp;
        private String json;

        /**
         * empty constructor
         */
        public PrognosisItem() {
        }

        /**
         * parameterized constructor
         * @param timestamp timestamp when the prognosis was calculated
         * @param json the json encoded result of the calculation
         */
        public PrognosisItem(long timestamp, String json) {
            this.timestamp = timestamp;
            this.json = json;
        }

        /**
         * getter
         * @return timestamp when the prognosis was calculated
         */
        public long getTimestamp() {
            return timestamp;
        }

        /**
         * setter
         * @param timestamp timestamp when the prognosis was calculated
         */
        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }

        /**
         * getter
         * @return the json encoded result of the calculation
         */
        public String getJson() {
            return json;
        }

        /**
         * setter
         * @param json the json encoded result of the calculation
         */
        public void setJson(String json) {
            this.json = json;
        }
    }
}
