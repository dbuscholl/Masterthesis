package database;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * This class is a container which holds important setting. These setting are configured in a file and kept there in case
 * the file gets modified.
 */
public class Settings {
    private String host;
    private String port;
    private String username;
    private String password;
    private String database;

    /**
     * This constructor reads out the properties file where all settings are stored and saves the contents into the
     * corresponding attributes.
     * @throws IOException when something goes wrong during reading of the information.
     */
    public Settings() throws IOException {
        Properties prop = new Properties();
        InputStream is = getClass().getResource("/config.properties").openStream();
        prop.load(is);

        host = prop.getProperty("dbhost");
        port = prop.getProperty("dbport");
        username= prop.getProperty("dbuser");
        password = prop.getProperty("dbpass");
        database = prop.getProperty("dbname");
    }

    /**
     * getter
     * @return the host of the database
     */
    public String getHost() {
        return host;
    }

    /**
     * getter
     * @return the port of the database
     */
    public String getPort() {
        return port;
    }

    /**
     * getter
     * @return the username for the database
     */
    public String getUsername() {
        return username;
    }

    /**
     * getter
     * @return the password for the user of the database
     */
    public String getPassword() {
        return password;
    }

    /**
     * getter
     * @return the database in which everything is stored.
     */
    public String getDatabase() {
        return database;
    }
}
