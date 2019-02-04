package Database;

import java.time.LocalDateTime;
import java.util.Date;

public class Delay {
    private TripStop gtfsStop;
    private int seconds;
    private LocalDateTime timestamp;

    public Delay(TripStop gtfsStop, int seconds) {
        this.gtfsStop = gtfsStop;
        this.seconds = seconds;
        timestamp = LocalDateTime.now();
    }

    public TripStop getGtfsStop() {
        return gtfsStop;
    }

    public void setGtfsStop(TripStop gtfsStop) {
        this.gtfsStop = gtfsStop;
    }

    public int getSeconds() {
        return seconds;
    }

    public void setSeconds(int seconds) {
        this.seconds = seconds;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
