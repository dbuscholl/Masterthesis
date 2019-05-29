package Static;

/**
 * Structural representation of the Settings
 */
public class Settings {
    private static String dbhost;
    private static String dbport;
    private static String dbuser;
    private static String dbpass;
    private static String dbname;
    private static String nextTripsTimeAmount;

    /**
     * getter
     * @return the hostname of the database
     */
    public static String getDbhost() {
        return dbhost;
    }

    /**
     * setter
     * @param dbhost the hostname of the database
     */
    public static void setDbhost(String dbhost) {
        Settings.dbhost = dbhost;
    }

    /**
     * getter
     * @return the portnumber of the database connection
     */
    public static String getDbport() {
        return dbport;
    }

    /**
     * setter
     * @param dbport the portnumber of the database connection
     */
    public static void setDbport(String dbport) {
        Settings.dbport = dbport;
    }

    /**
     * getter
     * @return the username for the database
     */
    public static String getDbuser() {
        return dbuser;
    }

    /**
     * setter
     * @param dbuser the username for the database
     */
    public static void setDbuser(String dbuser) {
        Settings.dbuser = dbuser;
    }

    /**
     * getter
     * @return the password for the user for the database
     */
    public static String getDbpass() {
        return dbpass;
    }

    /**
     * setter
     * @param dbpass the password for the user for the database
     */
    public static void setDbpass(String dbpass) {
        Settings.dbpass = dbpass;
    }

    /**
     * getter
     * @return the name of the database where all data is stored
     */
    public static String getDbname() {
        return dbname;
    }

    /**
     * setter
     * @param dbname the name of the database where all data is stored
     */
    public static void setDbname(String dbname) {
        Settings.dbname = dbname;
    }

    /**
     * getter
     * @return the time to wait until checking for the next occuring trips
     */
    public static String getNextTripsTimeAmount() {
        return nextTripsTimeAmount;
    }

    /**
     * setter
     * @param nextTripsTimeAmount the time to wait until checking for the next occuring trips
     */
    public static void setNextTripsTimeAmount(String nextTripsTimeAmount) {
        Settings.nextTripsTimeAmount = nextTripsTimeAmount;
    }
}
