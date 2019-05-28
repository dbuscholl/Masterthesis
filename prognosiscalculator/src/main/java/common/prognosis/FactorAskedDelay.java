package common.prognosis;

import common.gtfs.Delay;
import common.network.Connection;
import common.network.Trip;
import database.GTFS;
import org.apache.log4j.Logger;

import java.util.ArrayList;

/**
 * <p>This class takes care of the delays which were measured by the questionnaire and are subjective </p>
 * <p>The calculation is pretty simple in this case. First the class obtains the gtfs trip id for the trip leg if not
 * done by other factors already. Then it gets all delays from the database which were measured by questionnaire. At last
 * this class uses the standard calciulation which is described in {@link PrognosisFactor}.</p>
 */
public class FactorAskedDelay extends PrognosisFactor {
    Logger logger = Logger.getLogger(this.getClass().getName());
    private ArrayList<String> currentTripIds = new ArrayList<>();

    /**
     * constructor
     * @param connection enter the connection for which you want this factor to calculate the delay
     */
    public FactorAskedDelay(Connection connection) {
        super(connection);
    }

    /**
     * The calculation is pretty simple in this case. First the class obtains the gtfs trip id for the trip leg if not
     * done by other factors already. Then it gets all delays from the database which were measured by questionnaire. At last
     * this class uses the standard calciulation which is described in {@link PrognosisFactor}.
     */
    @Override
    protected void execute() {
        try {
            ArrayList<Trip> legs = connection.getLegs();

            for (currentProcessingIndex = 0; currentProcessingIndex < legs.size(); currentProcessingIndex++) {
                Trip t = legs.get(currentProcessingIndex);
                if (t.getGTFSTripId() == null) {
                    notifyExecutionFinished(null);
                    return;
                }

                // possible tripIds for all trips which depart at same time and same stop
                currentTripIds = GTFS.getGTFSTripIds(t.getBoarding(), false);
                GTFS.removeTripIdsOfWrongDirection(currentTripIds, t);

                ArrayList<Integer> values = GTFS.getAnswerValues(currentTripIds, type);
                ArrayList<Delay> delays = new ArrayList<>();
                for (int i = 0; i < values.size(); i++) {
                    int delay = values.get(i);
                    delays.add(new Delay(0, t.getGTFSTripId(), i, String.valueOf(i), 0));
                }

                standardCalculation(delays);

            }
            notifyExecutionFinished(this);
            return;

        } catch (Exception e) {
            logger.error(type + ": " + e.getMessage(), e);
        }
    }
}
