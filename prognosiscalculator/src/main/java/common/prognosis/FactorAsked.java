package common.prognosis;

import common.gtfs.Delay;
import common.network.Connection;
import common.network.Trip;
import database.GTFS;
import org.apache.log4j.Logger;
import utilities.MathToolbox;

import java.util.ArrayList;

public class FactorAsked extends PrognosisFactor {
    Logger logger = Logger.getLogger(this.getClass().getName());
    private ArrayList<String> currentTripIds = new ArrayList<>();

    public FactorAsked(Connection connection) {
        super(connection);
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

                ArrayList<Integer> values = GTFS.getAnswerValues(currentTripIds, type);
                if (values == null || values.isEmpty()) {
                    notifyExecutionFinished(null);
                    continue;
                }

                PrognosisCalculationItem resultItem = new PrognosisCalculationItem();
                int value = MathToolbox.castToIntWithPossibleLoss(Math.round(MathToolbox.mean(values)));
                resultItem.setDelayBoardingRegular(value);
                resultItem.setDelayAlightingRegular(value);

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
