import Database.Database;
import Database.ScheduledTrip;
import Database.FormatTools;
import Network.DepartureBoardRequest;
import Network.XMLDocument;
import Processes.TripWorker;
import Static.Settings;
import org.apache.log4j.Logger;
import org.jdom2.Element;
import org.jdom2.JDOMException;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.*;

public class Main {
    private static ArrayList<TripWorker> workers = new ArrayList<>();

    public static void main(String[] args) {
        Logger log = Logger.getLogger(Main.class);

        readConfig();
        log.debug("Config file successfully read");

        try {
            Database db = new Database();
            String s = db.checkValidDatabaseStructure();
            if (!s.isEmpty()) {
                log.error(s);
                return;
            }
            log.debug("No Columns or tables missing");

            db.getIgnoringServiceIds();
            ArrayList<ScheduledTrip> trips = db.getNextScheduledTrips();

            for(ScheduledTrip t : trips) {
                TripWorker tw = new TripWorker(t);
                tw.run();
                workers.add(tw);
                break;
            }
        } catch (SQLException e) {
            log.error("", e);
        } catch (ClassNotFoundException e) {
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
