package common.prognosis;

/**
 * Callback interface which is being triggered, when the calculation of a single prognosis factor is done.
 */
public interface CalculationCompletedEvent {
    void onCalculationComplete(PrognosisFactor factor);
}