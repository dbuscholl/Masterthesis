package common.prognosis;

import common.network.Connection;
import common.network.Trip;
import database.GTFS;
import org.apache.log4j.Logger;
import utilities.MathToolbox;

import java.util.ArrayList;

/**
 * <p>This class is used to predict if the user will reach the next vehicle or not, in other words successfully interchange.</p>
 * <p>The calculation is also pretty simple. At first this class gets the gtfs trip ids if this wasn't done by an other
 * factor before. Then it retrieves all values which indicate if users reached the interchange or not in the past. At last
 * it calculates the mean and checks if the result is bigger than the threshold which can be set via class attribute.</p>
 */
public class FactorAskedInterchange extends PrognosisFactor {
    Logger logger = Logger.getLogger(this.getClass().getName());
    private static final double THRESHOLD = 0.7;

    /**
     * constructor
     * @param connection set the connection for which you want to calculate the prognosis
     */
    public FactorAskedInterchange(Connection connection) {
        super(connection);
    }

    /**
     *The calculation is also pretty simple. At first this class gets the gtfs trip ids if this wasn't done by an other
     * factor before. Then it retrieves all values which indicate if users reached the interchange or not in the past. At last
     * it calculates the mean and checks if the result is bigger than the threshold which can be set via class attribute
     */
    @Override
    protected void execute() {
        try {
            ArrayList<Trip> legs = connection.getLegs();

            for (currentProcessingIndex = 0; currentProcessingIndex < legs.size()-1; currentProcessingIndex++) {
                Trip currentTrip = legs.get(currentProcessingIndex);
                Trip nextTrip = legs.get(currentProcessingIndex+1);

                if(currentTrip.getGTFSTripId() == null) {
                    GTFS.getGTFSTripId(currentTrip);
                }
                if(nextTrip.getGTFSTripId() == null) {
                    GTFS.getGTFSTripId(nextTrip);
                }

                if(currentTrip.getGTFSTripId() == null || nextTrip.getGTFSTripId() == null) {
                    notifyExecutionFinished(null);
                    return;
                }

                ArrayList<Integer> values = GTFS.getInterchangeValues(currentTrip.getGTFSTripId(),nextTrip.getGTFSTripId());

                PrognosisCalculationItem resultItem = new PrognosisCalculationItem();
                int value = MathToolbox.mean(values) > THRESHOLD ? 1 : 0;
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
