package common.prognosis;

public class PrognosisCalculationItem {
    private int delayBoardingRegular;
    private int delayAlightingRegular;

    private int delayException;
    private double exceptionPropability;

    protected void setDelayBoardingRegular(int delayBoardingRegular) {
        this.delayBoardingRegular = delayBoardingRegular;
    }

    protected void setDelayAlightingRegular(int delayAlightingRegular) {
        this.delayAlightingRegular = delayAlightingRegular;
    }

    protected void setDelayException(int delayException) {
        this.delayException = delayException;
    }

    protected void setExceptionPropability(double exceptionPropability) {
        this.exceptionPropability = exceptionPropability;
    }

    public int getDelayBoardingRegular() {
        return delayBoardingRegular;
    }

    public int getDelayAlightingRegular() {
        return delayAlightingRegular;
    }

    public int getDelayException() {
        return delayException;
    }

    public double getExceptionPropability() {
        return exceptionPropability;
    }
}
