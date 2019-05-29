package database;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * This class is a singleton which provides the caller with a datasource / database instance. As DBCP is used multiple
 * instances can be open at a time. This settings are set in getConnection.
 */
public class DataSource {
    private static BasicDataSource ds;

    private static Logger log = Logger.getLogger(DataSource.class);

    /**
     * private constructor since this is a singleton class
     */
    private DataSource() {
    }

    /**
     * This function sets all configuration attributes for dbcp and returns a connection instance on which database operations
     * can be executed.
     * @return a connection with which we can verifyIds statements on the database if one is free.
     * @throws SQLException when something goes wrong during creation
     */
    public static Connection getConnection() throws SQLException {
        try {
            Settings s = new Settings();
            if (ds == null) {
                ds = new BasicDataSource();
                ds.setDriverClassName("com.mysql.jdbc.Driver");
                ds.setUrl("jdbc:mysql://" + s.getHost() + ":" + s.getPort() + "/" + s.getDatabase());
                ds.setUsername(s.getUsername());
                ds.setPassword(s.getPassword());
                ds.setMinIdle(2);
                ds.setMaxIdle(5);
                ds.setMaxOpenPreparedStatements(100);
            }
            return ds.getConnection();
        } catch (IOException e) {
            throw new SQLException("Error while parsing Settings File");
        }
    }
}
