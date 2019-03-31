package common.prognosis;

public interface CalculationCompletedEvent {
    void onCalculationComplete(PrognosisCalculationResult result);
}