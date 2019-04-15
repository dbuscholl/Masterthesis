package common.prognosis;

import common.gtfs.Delay;
import common.network.Connection;
import common.network.Trip;
import database.SQLFormatTools;
import utilities.Chronometer;
import utilities.MathToolbox;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;

public abstract class PrognosisFactor extends Thread {
    protected Connection connection;
    protected double weight = 1;
    protected PrognosisFactorType type = null;
    protected PronosisFactorCalculationModel calculationModel = PronosisFactorCalculationModel.NEUTRAL;
    protected PrognosisCalculationResult result = new PrognosisCalculationResult();
    protected long executionTime = 0;
    protected int currentProcessingIndex = 0;
    private Chronometer chronometer = new Chronometer();
    private CalculationCompletedEvent calculationCompletedEvent;
    private boolean doneExecuting = false;

    public PrognosisFactor(Connection connection) {
        super();
        this.connection = connection;
        result.setConnection(connection);
    }

    @Override
    public void run() {
        chronometer.addNow();
        execute();
    }

    protected abstract void execute();

    public enum PrognosisFactorType {
        TRIASRECORDING_SAMEDAY,
        TRIASRECORDING_EVERYDAY,
        TRIASRECORDING_ALLDAY,
        USERRECORDING_EVERYDAY,
        QUESTIONNAIRE_CLEANNESS,
        QUESTIONNAIRE_CAPACITY,
        QUESTIONNAIRE_DELAY,
        QUESTIONNAIRE_INTERCHANGE,
        GOOGLE_CURRENT_TRAFFIC;
    }

    public enum PronosisFactorCalculationModel {
        OPTIMISTIC,
        NEUTRAL,
        PESSIMISTIC;
    }


    protected void notifyExecutionFinished(PrognosisFactor factor) {
        chronometer.addNow();
        executionTime = chronometer.getLastDifferece();
        doneExecuting = true;
        this.result = result;
        if(calculationCompletedEvent != null) {
            calculationCompletedEvent.onCalculationComplete(factor);
        }
    }

    public boolean isDoneExecuting() {
        return doneExecuting;
    }

    /**
     * There is only one handler per Factor. The old one will be overwritten!
     * @param calculationCompletedEvent
     */
    public void setCalculationCompletedEvent(CalculationCompletedEvent calculationCompletedEvent) {
        this.calculationCompletedEvent = calculationCompletedEvent;
    }

    public PrognosisCalculationResult getResult() {
        return result;
    }

    public PronosisFactorCalculationModel getCalculationModel() {
        return calculationModel;
    }

    public PrognosisFactorType getType() {
        return type;
    }

    public void setType(PrognosisFactorType type) {
        this.type = type;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public long getExecutionTime() {
        return executionTime;
    }

    protected void standardCalculation(ArrayList<Delay> delays) throws SQLException {
        Trip t = connection.getLegs().get(currentProcessingIndex);
        ArrayList<Delay> delaysAtBoarding = Delay.getDelaysForStopPoint(t, t.getBoarding(), delays);
        ArrayList<Delay> delaysAtAlighting = Delay.getDelaysForStopPoint(t, t.getAlighting(), delays);


        // calculate regular
        PrognosisCalculationItem resultItem = new PrognosisCalculationItem();
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
        if (bigDelays.size() != 0) {
            int[] mode = MathToolbox.mode(Delay.getDelayValues(bigDelays));

            resultItem.setDelayException(mode[0]);
            double thisBigProp = (double) mode[1] / bigDelays.size();
            double thisinAny = (double) mode[1] / delays.size();
            double anyBigProp = (double) bigDelays.size() / delays.size();
            resultItem.setExceptionPropability(((thisBigProp + thisinAny * 2 + anyBigProp) / 4) * 100);
        } else {
            resultItem.setDelayException(0);
            resultItem.setExceptionPropability(0);
        }
        result.add(resultItem);
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
                return 180;
            }
        } else {
            return 180;
        }
    }
}
