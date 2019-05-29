package Main;

import Database.DataSource;
import Processes.RecorderProcess;
import Static.Chronometer;
import Static.Settings;
import com.mysql.jdbc.exceptions.jdbc4.CommunicationsException;
import org.apache.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.*;

/**
 * This class is the main entry point for this application. The main method is executed on start of the programm. At
 * first the config file is read. All of the attributes stored there are hold in static attributes of the {@link Settings} class.
 * Then it starts both repeating threads: the {@link RecorderProcess} and the {@link Processes.WorkerManager}.
 */
public class Main {
    private static Chronometer chronometer = new Chronometer();

    /**
     * At first the config file is read. All of the attributes stored there are hold in static attributes of the
     * {@link Settings} class. Then it starts both repeating threads: the {@link RecorderProcess} and the
     * {@link Processes.WorkerManager}.
     * @param args cmd args
     */
    public static void main(String[] args) {
        Logger log = Logger.getLogger(Main.class);

        readConfig();
        log.debug("Config file successfully read");

        try {
            log.info(new Date());
            chronometer.addNow();
            String s = DataSource.checkValidDatabaseStructure();
            if (!s.isEmpty()) {
                log.error(s);
                return;
            }
            chronometer.addNow();
            log.debug("No Columns or tables missing. Done in " + (double) chronometer.getLastDifferece() / 1000 + "s");

            Timer recorderProcess = new Timer();
            RecorderProcess tt = new RecorderProcess();
            recorderProcess.schedule(tt, 0, 300000);


        } catch (CommunicationsException e) {
            log.error("Could not establish database connection. Did you provide wrong credentials? Is your database up and running?");
        } catch (SQLException e) {
            log.error("", e);
        } catch (ConcurrentModificationException e) {
            log.error("Big problem with RecorderProcess", e);
        }

    }

    /**
     * this function reads out the config file and puts its contents inside the {@link Settings} class. As the config
     * file might change during the time this method is public and can be called from anywhere to reload the file.
     */
    public static void readConfig() {
        Properties prop = new Properties();
        InputStream input = null;
        try {

            input = new FileInputStream("./config.properties");

            // load a properties file
            prop.load(input);

            // store properties
            Settings.setDbhost(prop.getProperty("dbhost"));
            Settings.setDbport(prop.getProperty("dbport"));
            Settings.setDbuser(prop.getProperty("dbuser"));
            Settings.setDbpass(prop.getProperty("dbpass"));
            Settings.setDbname(prop.getProperty("dbname"));
            Settings.setNextTripsTimeAmount(prop.getProperty("nextTripsTimeAmount"));

        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
