package Processes;

import Database.DataSource;
import Database.Entities.IgnoreService;
import Database.Entities.ScheduledTrip;
import Static.Chronometer;
import Static.UncaughtExceptionHandler;
import org.apache.log4j.Logger;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TimerTask;

/**
 * <p>>This Process gets the next scheduled trips, prepares them and inserts them into the tripworker, which collects delays.
 * First it requests all ServiceIds which should be ignored. Then it collects all trips in the next x minutes (x specified
 * in config-file). All Trips with the ServiceId which should be ignored are removed.</p>
 * <p>In the next Step the trips get prepared. Meaning that we check if they exist in TRIAS etc.. Check Javadoc inside
 * WorkerManager for more information</p>
 * <p>Finally we check if our WorkerManager already takes care of these trips and remove them. Rest will be added for
 * delay recording!/p>
 */
public class RecorderProcess extends TimerTask {
    private Logger log = Logger.getLogger(this.getClass().getName());
    private Chronometer chronometer = new Chronometer();
    private WorkerManager workerManager;

    public RecorderProcess() {
        workerManager = new WorkerManager();
        workerManager.start();
    }

    @Override
    public void run() {
        try {
            Thread.currentThread().setName("RecorderProcess");
            Thread.currentThread().setUncaughtExceptionHandler(new UncaughtExceptionHandler());
            log.info("Getting next Trips");
            chronometer.addNow();

            ArrayList<TripWorker> workers = new ArrayList<>();
            ArrayList<IgnoreService> ignoringServices = DataSource.getIgnoringServiceIds();
            ArrayList<ScheduledTrip> trips = DataSource.getNextScheduledTrips(ignoringServices);

            chronometer.addNow();
            log.debug("Got next scheduled trips in " + (double) chronometer.getLastDifferece() / 1000 + "s");
            log.debug("Parsing new Trips...");

            for (ScheduledTrip t : trips) {
                TripWorker tw = new TripWorker(t);
                tw.prepare();
                workers.add(tw);
            }

            chronometer.addNow();
            log.debug("Parsing done in " + (double) chronometer.getLastDifferece() / 1000 + "s");
            log.debug("Cleaning...");

            // removing trips which are already in our recording list
            for (Iterator<TripWorker> iScheduler = workers.iterator(); iScheduler.hasNext(); ) {
                TripWorker newWorker = iScheduler.next();
                for (Iterator<TripWorker> iManager = workerManager.getWorkers().iterator(); iManager.hasNext(); ) {
                    TripWorker existingWorker = iManager.next();
                    if (existingWorker.getGtfsTripInfo().getTrip_id().equals(newWorker.getGtfsTripInfo().getTrip_id())) {
                        iScheduler.remove();
                        break;
                    }
                }
            }
            chronometer.addNow();
            log.debug("Cleaning done in " + (double) chronometer.getLastDifferece() / 1000 + "s");
            workerManager.add(workers);
            chronometer.clear();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
