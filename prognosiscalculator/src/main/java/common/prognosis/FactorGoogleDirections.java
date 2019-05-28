package common.prognosis;

import common.network.ApiRequester;
import common.network.Connection;
import common.network.Trip;
import database.GTFS;
import database.SQLFormatTools;
import org.apache.log4j.Logger;
import utilities.MathToolbox;

import java.util.*;

/**
 * <p>This class is rather experimental but it is fun to see how this factor affects the prognosis. Let me introduce
 * the Google Directions Factor to you. This class calls the Google Directions API and tries to include the prognosis
 * based on the current traffic situation on the streets. As this is only for streets this should only affect busses.</p>
 * <p>At first we get geocoordinates of origin and destination of a trip leg as well as the coordinates of the halfth of
 * the travel path because google often uses faster travel paths than a bus does. These parameters are given to the
 * {@link ApiRequester} which gets the result of calculation and returns it to this class. If the travel time is predicted
 * to be greater than the planned travel time of the bus, the difference between both will be set as delay for arrival.</p>
 */
public class FactorGoogleDirections extends PrognosisFactor {
    private ArrayList<String> currentTripIds = new ArrayList<>();
    private Logger logger = Logger.getLogger(this.getClass());

    /**
     * constructor
     * @param connection the connection for which the prognosis should be calculated
     */
    public FactorGoogleDirections(Connection connection) {
        super(connection);
        type = PrognosisFactorType.GOOGLE_CURRENT_TRAFFIC;
    }

    /**
     * At first we get geocoordinates of origin and destination of a trip leg as well as the coordinates of the halfth of
     * the travel path because google often uses faster travel paths than a bus does. These parameters are given to the
     * {@link ApiRequester} which gets the result of calculation and returns it to this class. If the travel time is predicted
     * to be greater than the planned travel time of the bus, the difference between both will be set as delay for arrival.
     */
    @Override
    protected void execute() {
        try {
            ArrayList<Trip> legs = connection.getLegs();
            GTFS.getLocationDataForStopList(connection.extractAllStops());

            for (currentProcessingIndex = 0; currentProcessingIndex < legs.size(); currentProcessingIndex++) {
                Trip t = legs.get(currentProcessingIndex);

                //TODO: only calculate if departure is no longer than 1 hour in future
                if(t.getService().getRailType().contains("Bus")) {
                    // getting coordinates for request
                    int half = t.getIntermediates().size() / 2;
                    String origin = t.getBoarding().getLatitude() + "," + t.getBoarding().getLongitude();
                    String destination = t.getAlighting().getLatitude() + "," + t.getAlighting().getLongitude();
                    String via = t.getIntermediates().get(half).getLatitude() + "," + t.getIntermediates().get(half).getLongitude();

                    long travelTimeTrias = SQLFormatTools.getTriasDifference(t.getBoarding().getDepartureTime(), t.getAlighting().getArrivalTime()) / 1000;
                    int travelTimeGoogle = ApiRequester.getTravelTime(origin, destination, via);
                    long travelDiff = travelTimeGoogle - travelTimeTrias;

                    PrognosisCalculationItem resultItem = new PrognosisCalculationItem();
                    resultItem.setDelayAlightingRegular(travelDiff > 0 ? MathToolbox.castToIntWithPossibleLoss(travelDiff) : 0);
                    result.add(resultItem);
                } else {
                    PrognosisCalculationItem resultItem = new PrognosisCalculationItem();
                    result.add(resultItem);
                }
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


}
