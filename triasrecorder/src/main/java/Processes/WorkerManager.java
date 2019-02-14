package Processes;

import Database.Entities.ScheduledTrip;
import Static.Chronometer;
import Static.UncaughtExceptionHandler;
import org.apache.log4j.Logger;
import org.jdom2.JDOMException;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.*;

/**
 * This process takes care of the delay recording for trips. This means it checks for every trip if it's about time to
 * add a new Delay and removes trips which are done.
 */
public class WorkerManager {
    private Logger log = Logger.getLogger(this.getClass().getName());
    private Timer timer;
    private TimerTask task;
    private List<TripWorker> workers;
    private Chronometer chronometer;

    public WorkerManager() {
        timer = new Timer();
        workers = Collections.synchronizedList(new ArrayList<>());
        chronometer = new Chronometer();
    }

    public void start() {
        task = new TimerTask() {
            @Override
            public void run() {
                Thread.currentThread().setName("WorkerManager");
                Thread.currentThread().setUncaughtExceptionHandler(new UncaughtExceptionHandler());
                int affected = 0;
                chronometer.addNow();
                synchronized (workers) {
                    for (Iterator<TripWorker> iterator = workers.iterator(); iterator.hasNext(); ) {
                        TripWorker w = iterator.next();
                        if (w.isBrokenWorker()) iterator.remove();
                        try {
                            if (w.isDeparted() && w.isMoreThanAfterLastDelay(180)) {
                                w.getNewDelay();
                                affected++;
                                if (w.isStopRecording()) {
                                    ScheduledTrip t = w.getGtfsTripInfo();
                                    if (w.getDelays().size() > 0) {
                                        w.addDelaysToDatabase();
                                    } else {
                                        log.warn(t.getFriendlyName() + " didn't record any realtime data.");
                                    }
                                    iterator.remove();
                                }
                            }
                        } catch (IOException e) {
                            log.error("IO Exception", e);
                        } catch (JDOMException e) {
                            log.error("JDOM Exception", e);
                        } catch (ParseException e) {
                            log.error("ParseException", e);
                            log.debug(w.getFriendlyName() + " (S: " + w.getGtfsTripInfo().getService_id() + ", T: " + w.getGtfsTripInfo().getTrip_id() + ")");
                            log.debug("GTFS Stops: \n" + w.printGtfsTour());
                            log.debug("TRIAS Stops: \n" + w.printTriasTour());
                            iterator.remove();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        } catch (NumberFormatException e) {
                            log.error("NumberFormatException", e);
                            log.debug(w.getFriendlyName() + " (S: " + w.getGtfsTripInfo().getService_id() + ", T: " + w.getGtfsTripInfo().getTrip_id() + ")");
                            log.debug("GTFS Stops: \n" + w.printGtfsTour());
                            log.debug("TRIAS Stops: \n" + w.printTriasTour());
                            iterator.remove();
                        } catch (NullPointerException e) {
                            log.error("NullPointerException", e);
                            log.debug(w.getFriendlyName() + " (S: " + w.getGtfsTripInfo().getService_id() + ", T: " + w.getGtfsTripInfo().getTrip_id() + ")");
                            log.debug("GTFS Stops: \n" + w.printGtfsTour());
                            log.debug("TRIAS Stops: \n" + w.printTriasTour());
                            iterator.remove();
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
                chronometer.addNow();
                double t = (double) chronometer.getLastDifferece() / 1000;
                if (affected > 0) {
                }
                chronometer.clear();
            }
        };
        timer.schedule(task, 0, 10000);
    }

    public void stop() {
        timer.cancel();
    }

    /**
     * add new Trips to the workers queue
     *
     * @param workers
     */
    public synchronized void add(ArrayList<TripWorker> workers) {
        for (Iterator<TripWorker> iterator = workers.iterator(); iterator.hasNext(); ) {
            TripWorker w = iterator.next();
            if (w.isBrokenWorker()) {
                iterator.remove();
            }
        }
        this.workers.addAll(workers);
        log.info("We have " + this.workers.size() + " trips to record now");
    }

    public List<TripWorker> getWorkers() {
        return workers;
    }
}
