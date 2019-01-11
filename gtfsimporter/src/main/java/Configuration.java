import java.io.File;

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

    public static File getSelectedFile() {
        return selectedFile;
    }

    public static void setSelectedFile(File selectedFile) {
        Configuration.selectedFile = selectedFile;
    }

    public static String getMysqlHost() {
        return mysqlHost;
    }

    public static void setMysqlHost(String mysqlHost) {
        Configuration.mysqlHost = mysqlHost;
    }

    public static int getMysqlPort() {
        return mysqlPort;
    }

    public static void setMysqlPort(int mysqlPort) {
        Configuration.mysqlPort = mysqlPort;
    }

    public static String getMysqlUsername() {
        return mysqlUsername;
    }

    public static void setMysqlUsername(String mysqlUsername) {
        Configuration.mysqlUsername = mysqlUsername;
    }

    public static String getMysqlDatabase() {
        return mysqlDatabase;
    }

    public static void setMysqlDatabase(String mysqlDatabase) {
        Configuration.mysqlDatabase = mysqlDatabase;
    }

    public static String getMysqlPassword() {
        return mysqlPassword;
    }

    public static void setMysqlPassword(String mysqlPassword) {
        Configuration.mysqlPassword = mysqlPassword;
    }
}
