import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {
    private static Connection con;
    private static boolean isConnected = false;

    private Database() {

    }

    public static Connection getInstancce() throws SQLException {
        if (con == null || con.isClosed()) {
            //Initializing Driver
            try {
                Class.forName("com.mysql.jdbc.Driver");
                con = DriverManager.getConnection("jdbc:mysql://" + Configuration.getMysqlHost() + ":" + Configuration.getMysqlPort() + "/"
                        + Configuration.getMysqlDatabase() + "?" + "user=" + Configuration.getMysqlUsername() + "&password=" + Configuration.getMysqlPassword());
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

    public static boolean isConnected() {
        return isConnected;
    }

    public static void setIsConnected(boolean isConnected) {
        Database.isConnected = isConnected;
    }
}
