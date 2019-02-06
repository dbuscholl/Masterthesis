package Database.Entities;

import java.time.LocalDateTime;

/**
 * Class representing the structure for a delay. A delay contains a TripStop for which it is recorded, the delay in
 * seconds and the Timestamp when it was recorded.
 */
public class Delay {
    private TripStop gtfsStop;
    private int seconds;
    private LocalDateTime timestamp;

    /**
     * Creates a new Delay with timestamp "now"
     *
     * @param gtfsStop The stop for which the delay is recorded. It's recommended to use a stop of Type.GTFS because
     *                 they will be stored in the database where all other GTFS Data is.
     * @param seconds amount of seconds indicating how late the vehicle was at the gtfsStop
     */
    public Delay(TripStop gtfsStop, int seconds) {
        this.gtfsStop = gtfsStop;
        this.seconds = seconds;
        timestamp = LocalDateTime.now();
    }

    /**
     *
     * @return get the reference to the TripStop
     */
    public TripStop getGtfsStop() {
        return gtfsStop;
    }

    /**
     * Sets a TripStop for the Delay
     * @param gtfsStop the TripStop to be set
     */
    public void setGtfsStop(TripStop gtfsStop) {
        this.gtfsStop = gtfsStop;
    }

    /**
     *
     * @return the delay in seconds
     */
    public int getSeconds() {
        return seconds;
    }

    /**
     * sets the delay in seconds
     * @param seconds amount of latency for a vehicle in seconds
     */
    public void setSeconds(int seconds) {
        this.seconds = seconds;
    }

    /**
     *
     * @return the timestamp when this delay was recorded
     */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
