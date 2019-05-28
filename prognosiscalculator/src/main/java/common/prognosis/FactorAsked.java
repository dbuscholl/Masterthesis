package common.prognosis;

import common.gtfs.Delay;
import common.network.Connection;
import common.network.Trip;
import database.GTFS;
import org.apache.log4j.Logger;
import utilities.MathToolbox;

import java.util.ArrayList;

/**
 * <p>This class implements the prognosis calculation for a subjective factor which was "asked" from the questionnaire of the
 * android app. That's where the name comes from.</p>
 * <p>The calculation of this factor is pretty simple. It first gets the gtfs trip id if not already done by other factors before
 * and then obtains all the anwer values for a single question which can be set by type. With these values the class calculates
 * the mean and returns it into the PrognosisCalculationItem</p>
 */
public class FactorAsked extends PrognosisFactor {
    Logger logger = Logger.getLogger(this.getClass().getName());
    private ArrayList<String> currentTripIds = new ArrayList<>();

    /**
     * just a Constructor
     * @param connection zhe connection for which the prognosis should be calculated
     */
    public FactorAsked(Connection connection) {
        super(connection);
    }

    /**
     * The calculation of this factor is pretty simple. It first gets the gtfs trip id if not already done by other factors before
     * and then obtains all the anwer values for a single question which can be set by type. With these values the class calculates
     * the mean and returns it into the PrognosisCalculationItem
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

                ArrayList<Integer> values = GTFS.getAnswerValues(currentTripIds, type);
                if (values == null || values.isEmpty()) {
                    notifyExecutionFinished(null);
                    continue;
                }

                PrognosisCalculationItem resultItem = new PrognosisCalculationItem();
                int value = MathToolbox.castToIntWithPossibleLoss(Math.round(MathToolbox.mean(values)));
                resultItem.setDelayBoardingRegular(value);
                resultItem.setDelayAlightingRegular(value);
                result.add(resultItem);
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
