package common.prognosis;

public abstract class PrognosisFactor extends Thread {
    protected int weight = 1;
    protected PrognosisFactorType type = null;
    private CalculationCompletedEvent calculationCompletedEvent;
    private boolean doneExecuting = false;

    @Override
    public void run() {
        execute();
    }

    protected abstract void execute();

    public enum PrognosisFactorType {
        TRIASRECORDING_SAMEDAY,
        TRIASRECORDING_EVERYDAY,
        USERRECORDING_SAMEDAY,
        USERRECORDING_EVERYDAY,
        GOOGLE_CURRENT_TRAFFIC;
    }


    protected void notifyExecutionFinished(int result) {
        doneExecuting = true;
        if(calculationCompletedEvent != null) {
            calculationCompletedEvent.onCalculationComplete(result);

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

    public PrognosisFactorType getType() {
        return type;
    }

    public void setType(PrognosisFactorType type) {
        this.type = type;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }
}
