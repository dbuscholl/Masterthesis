package common.prognosis;

import common.network.Connection;
import utilities.Chronometer;

public abstract class PrognosisFactor extends Thread {
    protected Connection connection;
    protected double weight = 1;
    protected PrognosisFactorType type = null;
    protected PronosisFactorCalculationModel calculationModel = PronosisFactorCalculationModel.NEUTRAL;
    private CalculationCompletedEvent calculationCompletedEvent;
    private boolean doneExecuting = false;
    private PrognosisCalculationResult result = null;
    private long executionTime = 0;

    public PrognosisFactor(Connection connection) {
        super();
        this.connection = connection;
    }

    @Override
    public void run() {
        Chronometer chronometer = new Chronometer();
        chronometer.addNow();
        execute();
        chronometer.addNow();
        executionTime = chronometer.getLastDifferece();
    }

    protected abstract void execute();

    public enum PrognosisFactorType {
        TRIASRECORDING_SAMEDAY,
        TRIASRECORDING_EVERYDAY,
        TRIASRECORDING_ALLDAY,
        USERRECORDING_SAMEDAY,
        USERRECORDING_EVERYDAY,
        GOOGLE_CURRENT_TRAFFIC;
    }

    public enum PronosisFactorCalculationModel {
        OPTIMISTIC,
        NEUTRAL,
        PESSIMISTIC;
    }


    protected void notifyExecutionFinished(PrognosisFactor factor) {
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
}
