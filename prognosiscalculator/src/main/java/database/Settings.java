package database;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Settings {
    private String host;
    private String port;
    private String username;
    private String password;
    private String database;

    public Settings() throws IOException {
        Properties prop = new Properties();
        InputStream is = getClass().getResource("config.properties").openStream();
        prop.load(is);

        host = prop.getProperty("dbhost");
        port = prop.getProperty("dbport");
        username= prop.getProperty("dbuser");
        password = prop.getProperty("dbpass");
        database = prop.getProperty("dbname");
    }

    public String getHost() {
        return host;
    }

    public String getPort() {
        return port;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getDatabase() {
        return database;
    }
}
