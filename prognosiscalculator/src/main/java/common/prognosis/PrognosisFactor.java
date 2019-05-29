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

/**
 * This is the class "template" from which all factors must inherit to work properly. It defines several neccessary attributes
 * such as the connection for which the prognosis should be calculated, the CalculationModel, prognosis type or a flag which
 * indicates execution state. All factors must call notifyExecutionFinished with this or null as attribute whether they were
 * successful or not.
 */
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

    /**
     * constructor
     * @param connection the connection for which the prognosis should be calculated
     */
    public PrognosisFactor(Connection connection) {
        super();
        this.connection = connection;
        result.setConnection(connection);
    }

    /**
     * the main entry point when the calculator is startet via Thread.run()
     */
    @Override
    public void run() {
        chronometer.addNow();
        execute();
    }

    /**
     * This method must be implemented by all factors. They define how they calculate their prognosis here. You can use
     * standardCalculation() for median and mean for delays depending on CalculationModel.
     */
    protected abstract void execute();

    /**
     * This enum contains all FactorTypes which can be implemented at the moment. Add your type here or create own.
     */
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

    /**
     * This enum defines all the existing CalculationModels which are OPTIMISTIC, NEUTRAL and PESSIMISTIC
     */
    public enum PronosisFactorCalculationModel {
        OPTIMISTIC,
        NEUTRAL,
        PESSIMISTIC;
    }


    /**
     * this function sets neccessary attributes to the factor after calculation and calls the callback if one was set.
     * @param factor
     */
    protected void notifyExecutionFinished(PrognosisFactor factor) {
        chronometer.addNow();
        executionTime = chronometer.getLastDifferece();
        doneExecuting = true;
        if(calculationCompletedEvent != null) {
            calculationCompletedEvent.onCalculationComplete(factor);
        }
    }

    /**
     * getter
     * @return true if the factor is done with its calculation, false if still running
     */
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

    /**
     * getter
     * @return the result of calculation for the factor
     */
    public PrognosisCalculationResult getResult() {
        return result;
    }

    /**
     * getter
     * @return calculation model which was used for this factor
     */
    public PronosisFactorCalculationModel getCalculationModel() {
        return calculationModel;
    }

    /**
     * getter
     * @return the type of calculation which is predicted
     */
    public PrognosisFactorType getType() {
        return type;
    }

    /**
     * setter
     * @param type the type of calculation which is predicted
     */
    public void setType(PrognosisFactorType type) {
        this.type = type;
    }

    /**
     * getter
     * @return the weight for total calculation
     */
    public double getWeight() {
        return weight;
    }

    /**
     * getter
     * @param weight the weight for total calculation
     */
    public void setWeight(double weight) {
        this.weight = weight;
    }

    /**
     * getter
     * @return time in miliseconds how long the calculation took
     */
    public long getExecutionTime() {
        return executionTime;
    }

    /**
     * standard calculation implementation for a list of delays using median for optimistic and neutral method and mean
     * for pessimistic method.
     * @param delays list of delays for which the calculation should be done
     * @throws SQLException when something goes wrong during database things
     */
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

    /**
     * this function determines how much time should be used as threshold for the exception delay calculation. This is
     * usually interchange time / 2 or 3 minutes when no interchanges are found in the connection
     * @return amount of time in seconds which should be used as threshold
     */
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
