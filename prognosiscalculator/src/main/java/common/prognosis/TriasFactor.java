package common.prognosis;

import common.gtfs.Delay;
import common.network.Connection;
import common.network.Trip;
import database.GTFS;
import database.SQLFormatTools;
import utilities.MathToolbox;

import java.sql.SQLException;
import java.util.*;

public class TriasFactor extends PrognosisFactor {
    private static final int AMOUNT_WEEKRS = 15;
    private Map<Integer, ArrayList<Delay>> delayMap = new HashMap<>();

    private int currentProcessingIndex = 0;
    private ArrayList<String> currentBoardingIds = new ArrayList<>();

    public TriasFactor(Connection connection) {
        super(connection);
    }

    @Override
    protected void execute() {
        try {
            PrognosisCalculationResult result = new PrognosisCalculationResult();
            result.setConnection(connection);

            ArrayList<Trip> legs = connection.getLegs();
            for (currentProcessingIndex = 0; currentProcessingIndex < legs.size(); currentProcessingIndex++) {
                Trip t = legs.get(currentProcessingIndex);
                delayMap.put(currentProcessingIndex,new ArrayList<>());
                ArrayList<Delay> delays = delayMap.get(currentProcessingIndex);

                // possible tripIds for all trips which depart at same time and same stop
                currentBoardingIds = GTFS.getGTFSTripIds(t.getBoarding(), false);
                GTFS.removeTripIdsOfWrongDirection(currentBoardingIds, t);

                if(delays == null || delays.isEmpty()) {
                    notifyExecutionFinished(null);
                    return;
                }

                ArrayList<Delay> delaysAtBoarding = Delay.getDelaysForStopPoint(t, t.getBoarding(), delays);
                ArrayList<Delay> delaysAtAlighting = Delay.getDelaysForStopPoint(t, t.getAlighting(), delays);

                // calculate regular
                PrognosisCalculationResult.Item resultItem = new PrognosisCalculationResult.Item();
                if (calculationModel == PronosisFactorCalculationModel.NEUTRAL || calculationModel == PronosisFactorCalculationModel.OPTIMISTIC) {
                    // get values, get their median, round it, cast to Int, add it to the result item in one line...
                    resultItem.setDelayBoardingRegular(MathToolbox.castToIntWithPossibleLoss(Math.round(MathToolbox.median(Delay.getDelayValues(delaysAtBoarding)))));
                    resultItem.setDelayAlightingRegular(MathToolbox.castToIntWithPossibleLoss(Math.round(MathToolbox.median(Delay.getDelayValues(delaysAtAlighting)))));
                } else if (calculationModel == PronosisFactorCalculationModel.PESSIMISTIC) {
                    // get values, get their mean, ceil it, add it to result item in one line...
                    resultItem.setDelayBoardingRegular((int) Math.ceil(MathToolbox.mean(Delay.getDelayValues(delaysAtBoarding))));
                    resultItem.setDelayAlightingRegular((int) Math.ceil(MathToolbox.mean(Delay.getDelayValues(delaysAtAlighting))));
                }

                // calculate exception
                double threshhold = getThreshholdByInterchangeTime();
                ArrayList<Delay> bigDelays = Delay.getDelaysGreaterThan((int) threshhold, delays);
                int[] mode = MathToolbox.mode(Delay.getDelayValues(bigDelays));

                resultItem.setDelayException(mode[0]);
                resultItem.setExceptionPropability(((double) mode[1] / bigDelays.size()) * 100);

                result.add(resultItem);
            }

            notifyExecutionFinished(this);
            return;
        } catch (Exception e) {
            notifyExecutionFinished(null);
            e.printStackTrace();
        }

        notifyExecutionFinished(null);
    }

    private double getThreshholdByInterchangeTime() {
        Trip t = connection.getLegs().get(currentProcessingIndex);

        if (currentProcessingIndex < connection.getLegs().size() - 1) {
            try {
                Date arrival = SQLFormatTools.parseTriasTime(t.getAlighting().getArrivalTime());
                Date departureNext = SQLFormatTools.parseTriasTime(connection.getLegs().get(currentProcessingIndex + 1).getBoarding().getDepartureTime());
                double min = ((double) departureNext.getTime() - arrival.getTime()) / 1000;
                return min / 2;

            } catch (Exception e) {
                return 3;
            }
        } else {
            return 3;
        }
    }

    private ArrayList<Delay> getDelays() throws SQLException {
        switch (type) {
            case TRIASRECORDING_SAMEDAY:
                return getSamedayDelays();
            case TRIASRECORDING_EVERYDAY:
                return GTFS.getDelaysForIds(currentBoardingIds);
            case TRIASRECORDING_ALLDAY:
                return getAlldayDelays();
            default:
                return new ArrayList<>();
        }
    }

    private ArrayList<Delay> getSamedayDelays() throws SQLException {
        ArrayList<Delay> delaysTemporary = GTFS.getDelaysForIds(currentBoardingIds);
        ArrayList<Delay> delays = new ArrayList<>();

        Calendar calendar = Calendar.getInstance();
        for (int i = 0; i < AMOUNT_WEEKRS; i++) {
            calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) - 7);
            String date = SQLFormatTools.sqlDateFormat.format(calendar.getTime());

            for (Delay d : delaysTemporary) {
                if (d.getTimestamp().contains(date)) {
                    delays.add(d);
                }
            }
        }

        return delays;
    }

    private ArrayList<Delay> getAlldayDelays() throws SQLException {
        ArrayList<Delay> delays;
        Trip t = connection.getLegs().get(currentProcessingIndex);

        ArrayList<String> gtfsTripIds = GTFS.getGTFSTripIds(t.getBoarding(), t.getService(), true);
        delays = GTFS.getDelaysForIds(gtfsTripIds);

        return delays;
    }
}
