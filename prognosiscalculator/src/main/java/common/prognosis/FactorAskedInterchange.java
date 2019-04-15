package common.prognosis;

import common.network.Connection;
import common.network.Trip;
import database.GTFS;
import org.apache.log4j.Logger;
import utilities.MathToolbox;

import java.util.ArrayList;

public class FactorAskedInterchange extends PrognosisFactor {
    Logger logger = Logger.getLogger(this.getClass().getName());
    private static final double THRESHOLD = 0.7;

    public FactorAskedInterchange(Connection connection) {
        super(connection);
    }

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
