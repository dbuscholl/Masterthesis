package database;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

public class DataSource {
    private static BasicDataSource ds;

    private static Logger log = Logger.getLogger(DataSource.class);

    private DataSource() {
    }

    /**
     * @return a connection with which we can prepare statements on the database if one is free.
     * @throws SQLException
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
