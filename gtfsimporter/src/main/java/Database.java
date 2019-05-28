import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * This class connects to the database and is sort of an access point for the importer.
 */
public class Database {
    private static Connection con;
    private static boolean isConnected = false;

    /**
     * empty constructor private for singleton
     */
    private Database() {

    }

    /**
     * access point from outside
     * @return an instance of the database to work with for the import
     * @throws SQLException when something goes wrong usually with the access data
     */
    public static Connection getInstancce() throws SQLException {
        if (con == null || con.isClosed()) {
            //Initializing Driver
            try {
                Class.forName("com.mysql.jdbc.Driver");
                con = DriverManager.getConnection("jdbc:mysql://" + Configuration.getMysqlHost() + ":" + Configuration.getMysqlPort() + "/"
                        + Configuration.getMysqlDatabase() + "?user=" + Configuration.getMysqlUsername() + "&password=" + Configuration.getMysqlPassword());
                isConnected = true;
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (SQLException e) {
                isConnected = false;
                e.printStackTrace();
            }
        }
        return con;
    }

    /**
     * Checks wheter the database is connected or not
     * @return true if connected, false if not
     */
    public static boolean isConnected() {
        return isConnected;
    }

    /**
     * setter
     * @param isConnected sets the connection state of the database
     */
    private static void setIsConnected(boolean isConnected) {
        Database.isConnected = isConnected;
    }
}
