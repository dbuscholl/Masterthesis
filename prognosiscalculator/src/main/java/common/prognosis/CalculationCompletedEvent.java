package common.prognosis;

public interface CalculationCompletedEvent {
    void onCalculationComplete(PrognosisFactor factor);
}