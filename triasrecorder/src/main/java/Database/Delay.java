package Database;

public class Delay {
    private TripStop gtfsStop;
    private int seconds;

    public Delay(TripStop gtfsStop, int seconds) {
        this.gtfsStop = gtfsStop;
        this.seconds = seconds;
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
}
