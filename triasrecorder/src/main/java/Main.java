import Database.Database;
import Database.ScheduledTrip;
import Database.IgnoreService;
import Processes.TripWorker;
import Processes.WorkerManager;
import Static.Chronometer;
import Static.Settings;
import com.mysql.jdbc.exceptions.jdbc4.CommunicationsException;
import org.apache.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.*;

public class Main {
    private static ArrayList<TripWorker> workers = new ArrayList<>();
    private static Chronometer chronometer = new Chronometer();

    public static void main(String[] args) {
        Logger log = Logger.getLogger(Main.class);

        readConfig();
        log.debug("Config file successfully read");

        try {
            chronometer.addNow();
            String s = Database.checkValidDatabaseStructure();
            if (!s.isEmpty()) {
                log.error(s);
                return;
            }
            chronometer.addNow();
            log.debug("No Columns or tables missing. Done in " + (double) chronometer.getLastDifferece() / 1000 + "s");

            ArrayList<IgnoreService> ignoringServices = Database.getIgnoringServiceIds();
            ArrayList<ScheduledTrip> trips = Database.getNextScheduledTrips(ignoringServices);
            chronometer.addNow();
            log.debug("Got next scheduled trips in " + (double) chronometer.getLastDifferece() / 1000 + "s");

            log.debug("Parsing new Trips");
            for (ScheduledTrip t : trips) {
                TripWorker tw = new TripWorker(t);
                tw.prepare();
                workers.add(tw);
            }
            chronometer.addNow();
            log.debug("Parsing done in " + (double) chronometer.getLastDifferece() / 1000 + "s");

            WorkerManager workerManager = new WorkerManager();
            workerManager.add(workers);
            workerManager.start();

        } catch (CommunicationsException e) {
            log.error("Could not establish database connection. Did you provide wrong credentials? Is your database up and running?");
        } catch (SQLException e) {
            log.error("", e);
        }

    }

    private static void readConfig() {
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
