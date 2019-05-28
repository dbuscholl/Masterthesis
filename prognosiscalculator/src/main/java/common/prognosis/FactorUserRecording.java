package common.prognosis;

import common.gtfs.Delay;
import common.network.Connection;
import common.network.Trip;
import database.GTFS;
import org.apache.log4j.Logger;
import utilities.MathToolbox;

import java.util.ArrayList;

/**
 * This class works analog to {@link FactorTrias} but uses other delay data. The delays come from userrecordings which
 * were measured by the android app.However the calculation works similar to {@link FactorTrias} but without differentiating
 * between OPTIMISTIC, NEUTRAL or PESSIMISTIC calculation method. It uses the median for all types.
 */
public class FactorUserRecording extends PrognosisFactor {
    private Logger logger = Logger.getLogger(this.getClass());

    private ArrayList<String> currentTripIds = new ArrayList<>();

    /**
     * constructor
     * @param connection the connection for which the prognosis should be calculated
     */
    public FactorUserRecording(Connection connection) {
        super(connection);
        type = PrognosisFactorType.USERRECORDING_EVERYDAY;
    }

    /**
     * First the function gets the GTFS Trip IDs. Then it retrieves all delays which were measured for it. At last it
     * calculates the median of the data and returns it's results.
     */
    @Override
    protected void execute() {
        try {
            ArrayList<Trip> legs = connection.getLegs();

            for (currentProcessingIndex = 0; currentProcessingIndex < legs.size(); currentProcessingIndex++) {
                Trip t = legs.get(currentProcessingIndex);

                // possible tripIds for all trips which depart at same time and same stop
                currentTripIds = GTFS.getGTFSTripIds(t.getBoarding(), false);
                GTFS.removeTripIdsOfWrongDirection(currentTripIds, t);

                ArrayList<Delay> delays = GTFS.getUserDelaysForIds(currentTripIds);
                if (delays == null || delays.isEmpty()) {
                    notifyExecutionFinished(null);
                    continue;
                }

                standardCalculation(delays);
            }
            notifyExecutionFinished(this);

            return;
        } catch (Exception e) {
            logger.error(type + ": " + e.getMessage(), e);
            notifyExecutionFinished(null);
        }

        notifyExecutionFinished(null);
    }
}
