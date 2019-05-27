package common.prognosis;

import common.network.ApiRequester;
import common.network.Connection;
import common.network.Trip;
import database.GTFS;
import database.SQLFormatTools;
import org.apache.log4j.Logger;
import utilities.MathToolbox;

import java.util.*;

public class FactorGoogleDirections extends PrognosisFactor {
    private ArrayList<String> currentTripIds = new ArrayList<>();
    private Logger logger = Logger.getLogger(this.getClass());

    public FactorGoogleDirections(Connection connection) {
        super(connection);
        type = PrognosisFactorType.GOOGLE_CURRENT_TRAFFIC;
    }

    @Override
    protected void execute() {
        try {
            ArrayList<Trip> legs = connection.getLegs();
            GTFS.getLocationDataForStopList(connection.extractAllStops());

            for (currentProcessingIndex = 0; currentProcessingIndex < legs.size(); currentProcessingIndex++) {
                Trip t = legs.get(currentProcessingIndex);

                // getting coordinates for request
                int half = t.getIntermediates().size() / 2;
                String origin = t.getBoarding().getLatitude() + "," + t.getBoarding().getLongitude();
                String destination = t.getAlighting().getLatitude() + "," + t.getAlighting().getLongitude();
                String via = t.getIntermediates().get(half).getLatitude() + "," + t.getIntermediates().get(half).getLongitude();

                long travelTimeTrias = SQLFormatTools.getTriasDifference(t.getBoarding().getDepartureTime(), t.getAlighting().getArrivalTime()) / 1000;
                int travelTimeGoogle = ApiRequester.getTravelTime(origin, destination, via);
                long travelDiff = travelTimeGoogle - travelTimeTrias;

                PrognosisCalculationItem resultItem = new PrognosisCalculationItem();
                resultItem.setDelayBoardingRegular(0);
                resultItem.setDelayAlightingRegular(travelDiff > 0 ? MathToolbox.castToIntWithPossibleLoss(travelDiff) : 0);
                result.add(resultItem);
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
