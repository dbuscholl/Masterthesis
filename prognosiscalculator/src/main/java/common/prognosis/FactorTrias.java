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

/**
 * <p>This class is an implementation of a PrognosisFactor which calculates prognosis for delays which were collected
 * by the trias recorder. There are three types with different databases for calculation: <ul><li>TRIAS_SAMEDAY</li><li>
 * TRIAS_EVERYDAY</li><li>TRIAS_ALLDAY</li></ul></p>
 * <p><b>TRIAS_SAMEDAY</b> uses all delays for a trip which were measured at the same day as the departure. For example
 * when a trip is planned to depart on monday 10am then all delays for every monday at 10am are used for calculation</p>
 * <p><b>TRIAS_EVERYDAY</b> uses all delays for a trip which were measure at the same time. For example when a trip is
 * planned to depart on monday 10am then all delays for monday, tuesday, wednesday and so on at 10am are targeted for
 * the calculation of the prognosis.</p>
 * <p><b>TRIAS_ALLDAY</b> uses all delays for a trip without checking when it is planned to depart. It just uses all
 * existing delay measurements for a trip.</p>
 * <p>The calculation for this factor depents whether you choose <b>OPTIMISTIC</b>, <b>NEUTRAL</b> or <b>PESSIMISTIC</b>
 * calculation. When choosing pessimistic the mean is calculated for both boarding and alighting where as for the others
 * the median is used as delay value for boarding and alighting.</p>
 * <p>For the exception value this class uses a threshold that defines what "exception" means. It is usually the half
 * of the interchange time when a connection contains interchanges and at least 3 minutes if it doesnt. The prob is calculated
 * by a formula which has proven to be pretty exact in evaluation.</p>
 */
public class FactorTrias extends PrognosisFactor {
    private static final int AMOUNT_WEEKRS = 15;
    private Map<Integer, ArrayList<Delay>> delayMap = new HashMap<>();

    private ArrayList<String> currentTripIds = new ArrayList<>();

    private Logger logger = Logger.getLogger(this.getClass());

    /**
     * constructor
     * @param connection the connection for which the prognosis should be calculated
     */
    public FactorTrias(Connection connection) {
        super(connection);
    }

    /**
     * <p>The calculation for this factor depents whether you choose <b>OPTIMISTIC</b>, <b>NEUTRAL</b> or <b>PESSIMISTIC</b>
     * calculation. When choosing pessimistic the mean is calculated for both boarding and alighting where as for the others
     * the median is used as delay value for boarding and alighting.</p>
     * <p>For the exception value this class uses a threshold that defines what "exception" means. It is usually the half
     * of the interchange time when a connection contains interchanges and at least 3 minutes if it doesnt. The prob is calculated
     * by a formula which has proven to be pretty exact in evaluation.</p>
     */
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

    /**
     * Gets the delays depending of the type for the prognosis calculation:
     * <p><b>TRIAS_SAMEDAY</b> uses all delays for a trip which were measured at the same day as the departure. For example
     * when a trip is planned to depart on monday 10am then all delays for every monday at 10am are used for calculation</p>
     * <p><b>TRIAS_EVERYDAY</b> uses all delays for a trip which were measure at the same time. For example when a trip is
     * planned to depart on monday 10am then all delays for monday, tuesday, wednesday and so on at 10am are targeted for
     * the calculation of the prognosis.</p>
     * <p><b>TRIAS_ALLDAY</b> uses all delays for a trip without checking when it is planned to depart. It just uses all
     * existing delay measurements for a trip.</p>
     * @return the list of delays which should be used for calculation
     * @throws SQLException when something goes wrong during retrieving them.
     */
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

    /**
     * Gives us all delays for a trip which were measured at the same day as the departure. For example
     * when a trip is planned to depart on monday 10am then all delays for every monday at 10am are used for calculation
     * @return list of delays for a trip which were measured at the same day as the departure
     * @throws SQLException when something goes wrong during retrieving them
     */
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

    /**
     * Gives you all delays for a trip which were measure at the same time. For example when a trip is
     * planned to depart on monday 10am then all delays for monday, tuesday, wednesday and so on at 10am are targeted for
     * the calculation of the prognosis.
     * @return all delays for a trip which were measure at the same time
     * @throws SQLException when something goes wrong during retrieving them
     */
    private ArrayList<Delay> getAlldayDelays() throws SQLException {
        ArrayList<Delay> delays;
        Trip t = connection.getLegs().get(currentProcessingIndex);

        ArrayList<String> gtfsTripIds = GTFS.getGTFSTripIds(t.getBoarding(), t.getService(), true);
        delays = GTFS.getDelaysForIds(gtfsTripIds);

        return delays;
    }

}
