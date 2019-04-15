package common.prognosis;

import common.gtfs.Delay;
import common.network.Connection;
import common.network.Trip;
import database.GTFS;
import database.SQLFormatTools;
import org.apache.log4j.Logger;
import utilities.MathToolbox;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

public class FactorTrias extends PrognosisFactor {
    private static final int AMOUNT_WEEKRS = 15;
    private Map<Integer, ArrayList<Delay>> delayMap = new HashMap<>();

    private ArrayList<String> currentTripIds = new ArrayList<>();

    private Logger logger = Logger.getLogger(this.getClass());

    public FactorTrias(Connection connection) {
        super(connection);
    }

    @Override
    protected void execute() {
        try {
            ArrayList<Trip> legs = connection.getLegs();
            for (currentProcessingIndex = 0; currentProcessingIndex < legs.size(); currentProcessingIndex++) {
                Trip t = legs.get(currentProcessingIndex);
                delayMap.put(currentProcessingIndex, new ArrayList<>());

                // possible tripIds for all trips which depart at same time and same stop
                currentTripIds = GTFS.getGTFSTripIds(t.getBoarding(), false);
                GTFS.removeTripIdsOfWrongDirection(currentTripIds, t);

                ArrayList<Delay> delays = getDelays();

                if (delays == null || delays.isEmpty()) {
                    notifyExecutionFinished(null);
                    continue;
                }
                delayMap.put(currentProcessingIndex, delays);

                standardCalculation(delays);
            }

            notifyExecutionFinished(this);
            return;
        } catch (Exception e) {
            Logger logger = Logger.getLogger(this.getClass().getName());
            logger.info(type + ": " + e.getMessage(), e);
            notifyExecutionFinished(null);
        }

        notifyExecutionFinished(null);
    }

    private ArrayList<Delay> getDelays() throws SQLException {
        switch (type) {
            case TRIASRECORDING_SAMEDAY:
                return getSamedayDelays();
            case TRIASRECORDING_EVERYDAY:
                return GTFS.getDelaysForIds(currentTripIds);
            case TRIASRECORDING_ALLDAY:
                return getAlldayDelays();
            default:
                return new ArrayList<>();
        }
    }

    private ArrayList<Delay> getSamedayDelays() throws SQLException {
        ArrayList<Delay> delaysTemporary = GTFS.getDelaysForIds(currentTripIds);
        ArrayList<Delay> delays = new ArrayList<>();
        SimpleDateFormat datesdf = new SimpleDateFormat(SQLFormatTools.datePattern);
        Trip trip = connection.getLegs().get(currentProcessingIndex);
        Date originalDate = SQLFormatTools.parseTriasTime(trip.getBoarding().getDepartureTime());
        if(originalDate == null) {
            logger.warn(type.toString() + " did not find date for Connection " + trip.getService().getLineName() + " -> " + trip.getService().getDesitnation());
            return delays;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setLenient(true);
        calendar.setTime(originalDate);
        for (int i = 0; i < AMOUNT_WEEKRS; i++) {
            calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) - 7);
            String date = datesdf.format(calendar.getTime());

            for (Delay d : delaysTemporary) {
                if (d.getTimestamp().contains(date)) {
                    delays.add(d);
                }
            }
        }

        return delays;
    }

    private ArrayList<Delay> getAlldayDelays() throws SQLException {
        ArrayList<Delay> delays;
        Trip t = connection.getLegs().get(currentProcessingIndex);

        ArrayList<String> gtfsTripIds = GTFS.getGTFSTripIds(t.getBoarding(), t.getService(), true);
        delays = GTFS.getDelaysForIds(gtfsTripIds);

        return delays;
    }

}
