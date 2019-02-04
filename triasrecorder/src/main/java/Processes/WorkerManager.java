package Processes;

import org.apache.log4j.Logger;
import org.jdom2.JDOMException;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class WorkerManager {
    private Logger log = Logger.getLogger(this.getClass().getName());
    private Timer timer;
    private ArrayList<TripWorker> workers;

    public WorkerManager() {
        timer = new Timer();
        workers = new ArrayList<>();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                synchronized (workers) {
                    for (TripWorker w : workers) {
                        try {
                            w.getNewDelay();
                            if(w.isStopRecording()) {
                                w.addToDatabase();
                                workers.remove(w);
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
            }
        };
        timer.schedule(task, 5000);
    }

}
