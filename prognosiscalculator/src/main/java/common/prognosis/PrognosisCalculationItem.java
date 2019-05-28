package common.prognosis;

/**
 * This is the entity class which holds a result of prognosis calculation for a trip leg.
 */
public class PrognosisCalculationItem {
    private int delayBoardingRegular;
    private int delayAlightingRegular;

    private int delayException;
    private double exceptionPropability;

    /**
     * setter
     * @param delayBoardingRegular predicted delay at boarding of the trip leg.
     */
    protected void setDelayBoardingRegular(int delayBoardingRegular) {
        this.delayBoardingRegular = delayBoardingRegular;
    }

    /**
     * setter
     * @param delayAlightingRegular predicted delay at arrival of the trip leg
     */
    protected void setDelayAlightingRegular(int delayAlightingRegular) {
        this.delayAlightingRegular = delayAlightingRegular;
    }

    /**
     * setter
     * @param delayException predicted maximum delay which can occur during the travel of the trip leg
     */
    protected void setDelayException(int delayException) {
        this.delayException = delayException;
    }

    /**
     * setter
     * @param exceptionPropability probability indicating how likely it is that the exception really enters
     */
    protected void setExceptionPropability(double exceptionPropability) {
        this.exceptionPropability = exceptionPropability;
    }

    /**
     * getter
     * @return predicted delay at boarding of the trip leg
     */
    public int getDelayBoardingRegular() {
        return delayBoardingRegular;
    }

    /**
     * getter
     * @return predicted delay at arrival of the trip leg
     */
    public int getDelayAlightingRegular() {
        return delayAlightingRegular;
    }

    /**
     * getter
     * @return predicted maximum delay which can occur during the travel of the trip leg
     */
    public int getDelayException() {
        return delayException;
    }

    /**
     * getter
     * @return probability indicating how likely it is that the exception really enters
     */
    public double getExceptionPropability() {
        return exceptionPropability;
    }
}
