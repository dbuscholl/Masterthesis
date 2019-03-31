package common.prognosis;

import common.gtfs.Delay;
import common.gtfs.TripStop;
import common.network.Connection;
import common.network.StopPoint;
import common.network.Trip;
import database.GTFS;
import database.SQLFormatTools;
import utilities.MathToolbox;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class TriasSamedayFactor extends PrognosisFactor {
    private static final int AMOUNT_WEEKRS = 15;

    public TriasSamedayFactor(Connection connection) {
        super(connection);
    }

    @Override
    protected void execute() {
        try {
            PrognosisCalculationResult result = new PrognosisCalculationResult();
            result.setConnection(connection);

            ArrayList<Trip> legs = connection.getLegs();
            for (int i = 0; i < legs.size(); i++) {
                Trip t = legs.get(i);
                // possible tripIds for all trips which depart at same time and same stop
                ArrayList<String> boardingIds = GTFS.getGTFSTripIds(t.getBoarding(), false);
                removeTripIdsOfWrongDirection(boardingIds, t);

                // all delays for tripIds of departure at given stop at same time and direction
                ArrayList<Delay> delays = getDelays(boardingIds);

                // get stop sequence from GTFS Trip for Delay values
                ArrayList<TripStop> fullTrip = GTFS.getFullTrip(t.getGTFSTripId());
                int stopSequenceBoarding = getStopSequenceOfStop(fullTrip, t.getBoarding());
                int stopSequenceAlighting = getStopSequenceOfStop(fullTrip, t.getAlighting());

                ArrayList<Integer> delaysAtBoarding = new ArrayList<>();
                ArrayList<Integer> delaysAtAlighting = new ArrayList<>();

                // for every date of the delays list get the delay value of the stop sequence
                ArrayList<String> dateValues = Delay.getDateValues(delays);
                for (String value : dateValues) {
                    ArrayList<Delay> delaysAtDate = Delay.getDelaysAtDate(value, delays);
                    if (delaysAtDate != null && !delaysAtDate.isEmpty()) {
                        delaysAtBoarding.add(Delay.getDelayForStopSequence(stopSequenceBoarding, delaysAtDate).getDelay());
                        delaysAtAlighting.add(Delay.getDelayForStopSequence(stopSequenceAlighting, delaysAtDate).getDelay());
                    }
                }


                // calculate regular
                PrognosisCalculationResult.Item resultItem = new PrognosisCalculationResult.Item();
                if (calculationModel == PronosisFactorCalculationModel.NEUTRAL || calculationModel == PronosisFactorCalculationModel.OPTIMISTIC) {
                    // get median, round it, cast to Int, add it to the result item in one line...
                    resultItem.setDelayBoardingRegular(MathToolbox.castToIntWithPossibleLoss(Math.round(MathToolbox.median(delaysAtBoarding))));
                    resultItem.setDelayAlightingRegular(MathToolbox.castToIntWithPossibleLoss(Math.round(MathToolbox.median(delaysAtAlighting))));
                } else if (calculationModel == PronosisFactorCalculationModel.PESSIMISTIC) {
                    // get mean, ceil it, add it to result item in one line...
                    resultItem.setDelayBoardingRegular((int) Math.ceil(MathToolbox.mean(delaysAtBoarding)));
                    resultItem.setDelayAlightingRegular((int) Math.ceil(MathToolbox.mean(delaysAtAlighting)));
                }

                // calculate exception
                double threshhold = getThreshholdByInterchangeTime(i);
                ArrayList<Delay> bigDelays = Delay.getDelaysGreaterThan((int) threshhold, delays);
                int[] mode = MathToolbox.mode(Delay.getDelayValues(bigDelays));

                resultItem.setDelayException(mode[0]);
                resultItem.setExceptionPropability(((double) mode[1] / bigDelays.size()) * 100);

                result.add(resultItem);
            }

            notifyExecutionFinished(result);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        notifyExecutionFinished(null);
    }

    public double getThreshholdByInterchangeTime(int tripIndex) {
        Trip t = connection.getLegs().get(tripIndex);

        if (tripIndex < connection.getLegs().size() - 1) {
            try {
                Date arrival = SQLFormatTools.parseTriasTime(t.getAlighting().getArrivalTime());
                Date departureNext = SQLFormatTools.parseTriasTime(connection.getLegs().get(tripIndex + 1).getBoarding().getDepartureTime());
                double min = ((double) departureNext.getTime() - arrival.getTime()) / 1000;
                return min - 2;

            } catch (Exception e) {
                return 3;
            }
        } else {
            return 3;
        }
    }

    public ArrayList<Delay> getDelays(ArrayList<String> ids) throws SQLException {
        ArrayList<Delay> delaysTemporary = GTFS.getDelaysForIds(ids);
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

    private void removeTripIdsOfWrongDirection(ArrayList<String> boardingIds, Trip t) throws SQLException {
        ArrayList<String> toRemove = new ArrayList<>();

        for (String id : boardingIds) {
            // get full trip so we can check with the next stop
            ArrayList<TripStop> trip = GTFS.getFullTrip(id);

            // find boarding stop and check if the next station after boarding matches the one got by tripId
            // if not matching then it travels in other direction so it has to be removed
            for (int i = 0; i < trip.size(); i++) {
                TripStop ts = trip.get(i);

                // when reached boarding but not reached end of stops in trip
                if (ts.getStop_name().equals(t.getBoarding().getName()) && i < trip.size() - 1) {
                    TripStop next = trip.get(i + 1);
                    StopPoint check;

                    // we might have trips without interchange
                    if (t.getIntermediates().size() > 0) {
                        check = t.getIntermediates().get(0);
                    } else {
                        check = t.getAlighting();
                    }

                    // if name of the next stop doesn't match the one from the connection
                    if (!check.getName().equals(next.getStop_name())) {
                        toRemove.add(id);
                    }
                } else {
                    toRemove.add(id);
                }
            }
        }

        for (String item : toRemove) {
            boardingIds.remove(item);
        }
    }

    private int getStopSequenceOfStop(ArrayList<TripStop> fullTrip, StopPoint s) {
        for (TripStop ts : fullTrip) {
            if (ts.getStop_name().equals(s.getName())) {
                return ts.getStop_sequence();
            }
        }
        return -1;
    }
}
