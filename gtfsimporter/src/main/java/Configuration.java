import java.io.File;

/**
 * This class takes care of the configuration stuff. It is a container for mysql setting which were entered with the GUI
 * and helps the Importer by providing information about the allowed filenames for GTFS strcutre.
 */
public class Configuration {
    private static File selectedFile;
    private static String mysqlHost;
    private static int mysqlPort;
    private static String mysqlUsername;
    private static String mysqlPassword;
    private static String mysqlDatabase;

    public static final String[] filenames = {
            "agency.txt", "stops.txt", "routes.txt", "trips.txt",
            "stop_times.txt", "calendar.txt", "calendar_dates.txt",
            "fare_attributes.txt", "fare_rules.txt", "shapes.txt",
            "frequencies.txt", "transfers.txt", "feed_info.txt"};

    /**
     * getter
     * @return the file which was selected to be imported.
     */
    public static File getSelectedFile() {
        return selectedFile;
    }

    /**
     * setter
     * @param selectedFile sets the file which was selected by the user through GUI
     */
    public static void setSelectedFile(File selectedFile) {
        Configuration.selectedFile = selectedFile;
    }

    /**
     * getter
     * @return the host for mysql database
     */
    public static String getMysqlHost() {
        return mysqlHost;
    }

    /**
     * setter
     * @param mysqlHost the host for mysql database
     */
    public static void setMysqlHost(String mysqlHost) {
        Configuration.mysqlHost = mysqlHost;
    }

    /**
     * getter
     * @return the port for mysql database
     */
    public static int getMysqlPort() {
        return mysqlPort;
    }

    /**
     * setter
     * @param mysqlPort the port for mysql database
     */
    public static void setMysqlPort(int mysqlPort) {
        Configuration.mysqlPort = mysqlPort;
    }

    /**
     * getter
     * @return the username for the mysql database
     */
    public static String getMysqlUsername() {
        return mysqlUsername;
    }

    /**
     * setter
     * @param mysqlUsername the username for the mysql database
     */
    public static void setMysqlUsername(String mysqlUsername) {
        Configuration.mysqlUsername = mysqlUsername;
    }

    /**
     * getter
     * @return the database name for the mysql database
     */
    public static String getMysqlDatabase() {
        return mysqlDatabase;
    }

    /**
     * setter
     * @param mysqlDatabase the database name for the mysql database
     */
    public static void setMysqlDatabase(String mysqlDatabase) {
        Configuration.mysqlDatabase = mysqlDatabase;
    }

    /**
     * getter
     * @return the password to use for the mysql database
     */
    public static String getMysqlPassword() {
        return mysqlPassword;
    }

    /**
     * setter
     * @param mysqlPassword the password to use for the mysql database
     */
    public static void setMysqlPassword(String mysqlPassword) {
        Configuration.mysqlPassword = mysqlPassword;
    }
}
