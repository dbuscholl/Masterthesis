package common.prognosis;

import common.gtfs.Delay;
import common.network.Connection;
import common.network.Trip;
import database.GTFS;
import org.apache.log4j.Logger;
import utilities.MathToolbox;

import java.util.ArrayList;

public class FactorUserRecording extends PrognosisFactor {
    private Logger logger = Logger.getLogger(this.getClass());

    private ArrayList<String> currentTripIds = new ArrayList<>();


    public FactorUserRecording(Connection connection) {
        super(connection);
        type = PrognosisFactorType.USERRECORDING_EVERYDAY;
    }

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
                notifyExecutionFinished(this);

                return;
            }
        } catch (Exception e) {
            logger.error(type + ": " + e.getMessage(), e);
            notifyExecutionFinished(null);
        }

        notifyExecutionFinished(null);
    }
}
