package Processes;

import Database.ScheduledTrip;
import Static.Chronometer;
import org.apache.log4j.Logger;
import org.jdom2.JDOMException;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

public class WorkerManager {
    private Logger log = Logger.getLogger(this.getClass().getName());
    private Timer timer;
    private TimerTask task;
    private ArrayList<TripWorker> workers;
    private Chronometer chronometer;

    public WorkerManager() {
        timer = new Timer();
        workers = new ArrayList<>();
        chronometer = new Chronometer();
    }

    public void start() {
        task = new TimerTask() {
            @Override
            public void run() {
                Thread.currentThread().setName("WorkerManager");
                int affected = 0;
                chronometer.addNow();
                synchronized (workers) {
                    for (Iterator<TripWorker> iterator = workers.iterator(); iterator.hasNext(); ) {
                        TripWorker w = iterator.next();
                        try {
                            if (w.isDeparted() && w.isMoreThanAfterLastDelay(180)) {
                                w.getNewDelay();
                                affected++;
                                if (w.isStopRecording()) {
                                    ScheduledTrip t = w.getGtfsTripInfo();
                                    if (w.getDelays().size() > 0) {
                                        w.addToDatabase();
                                    } else {
                                        log.error(t.getFriendlyName() + " didn't record any realtime data.");
                                    }
                                    iterator.remove();
                                    log.debug(t.getFriendlyName() + " is done recordings!");
                                }
                            }
                        } catch (IOException e) {
                            log.error("IO Exception", e);
                        } catch (JDOMException e) {
                            log.error("JDOM Exception", e);
                        } catch (ParseException e) {
                            log.error("ParseException", e);
                        } catch (SQLException e) {
                            e.printStackTrace();
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
                chronometer.addNow();
                double t = (double) chronometer.getLastDifferece() / 1000;
                if (affected > 0) {
                    log.debug("Got " + affected + " new Delays! Processed them in " + t + "s.");
                }
                chronometer.clear();
            }
        };
        timer.schedule(task, 0, 10000);
    }

    public void stop() {
        timer.cancel();
    }

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

    public ArrayList<TripWorker> getWorkers() {
        return workers;
    }
}
