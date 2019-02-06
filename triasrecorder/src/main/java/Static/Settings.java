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

    public static String getDbhost() {
        return dbhost;
    }

    public static void setDbhost(String dbhost) {
        Settings.dbhost = dbhost;
    }

    public static String getDbport() {
        return dbport;
    }

    public static void setDbport(String dbport) {
        Settings.dbport = dbport;
    }

    public static String getDbuser() {
        return dbuser;
    }

    public static void setDbuser(String dbuser) {
        Settings.dbuser = dbuser;
    }

    public static String getDbpass() {
        return dbpass;
    }

    public static void setDbpass(String dbpass) {
        Settings.dbpass = dbpass;
    }

    public static String getDbname() {
        return dbname;
    }

    public static void setDbname(String dbname) {
        Settings.dbname = dbname;
    }

    public static String getNextTripsTimeAmount() {
        return nextTripsTimeAmount;
    }

    public static void setNextTripsTimeAmount(String nextTripsTimeAmount) {
        Settings.nextTripsTimeAmount = nextTripsTimeAmount;
    }
}
