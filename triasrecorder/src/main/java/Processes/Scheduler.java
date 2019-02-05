package Processes;

import Database.Database;
import Database.IgnoreService;
import Database.ScheduledTrip;
import Static.Chronometer;
import org.apache.log4j.Logger;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TimerTask;

public class Scheduler extends TimerTask {
    private Logger log = Logger.getLogger(this.getClass().getName());
    private Chronometer chronometer = new Chronometer();
    private WorkerManager workerManager;

    public Scheduler() {
        workerManager = new WorkerManager();
        workerManager.start();
    }

    @Override
    public void run() {
        try {
            Thread.currentThread().setName("TripScheduler");
            log.info("Getting next Trips");
            chronometer.addNow();

            ArrayList<TripWorker> workers = new ArrayList<>();
            ArrayList<IgnoreService> ignoringServices = Database.getIgnoringServiceIds();
            ArrayList<ScheduledTrip> trips = Database.getNextScheduledTrips(ignoringServices);

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
