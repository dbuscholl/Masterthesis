package de.dbuscholl.fahrplanauskunft.network.entities;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * The entity class containing an item of prognosis calculation. An item is usually for a trip leg and the connection
 * has as many prognosis items as it has trip legs.
 */
public class PrognosisCalculationItem {
    private int delayBoardingRegular;
    private int delayAlightingRegular;

    private int delayException;
    private double exceptionPropability;

    /**
     * empty constructor
     */
    public PrognosisCalculationItem() {
    }

    /**
     * parameterized constructor
     * @param delayBoardingRegular the predicted delay for this particular trip leg at boarding in seconds.
     * @param delayAlightingRegular the predicted delay for this particular trip leg at arrival in seconds.
     * @param delayException the maximum delay expected for this trip
     * @param exceptionPropability the probability to reach the maximum possible delay.
     */
    public PrognosisCalculationItem(int delayBoardingRegular, int delayAlightingRegular, int delayException, double exceptionPropability) {
        this.delayBoardingRegular = delayBoardingRegular;
        this.delayAlightingRegular = delayAlightingRegular;
        this.delayException = delayException;
        this.exceptionPropability = exceptionPropability;
    }

    /**
     * Builds the item from a json string
     * @param json the string from which the item should be created
     */
    public PrognosisCalculationItem(JSONObject json) {
        try {
            delayBoardingRegular = json.has("delayBoarding") ? json.getInt("delayBoarding") : 0;
            delayAlightingRegular = json.has("delayAlighting") ? json.getInt("delayAlighting") : 0;
            delayException = json.has("delayException") ? json.getInt("delayException") : 0;
            exceptionPropability = json.has("exceptionPropability") ? json.getDouble("exceptionPropability") : 0;
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    /**
     * sets the delay
     * @param delayBoardingRegular the predicted delay for this particular trip leg at boarding
     */
    protected void setDelayBoardingRegular(int delayBoardingRegular) {
        this.delayBoardingRegular = delayBoardingRegular;
    }

    /**
     * sets the delay
     * @param delayAlightingRegular the predicted delay for this particular trip leg at arrival in seconds
     */
    protected void setDelayAlightingRegular(int delayAlightingRegular) {
        this.delayAlightingRegular = delayAlightingRegular;
    }

    /**
     * sets the delay
     * @param delayException the maximum delay expected for this trip
     */
    protected void setDelayException(int delayException) {
        this.delayException = delayException;
    }

    /**
     * sets the prob.
     * @param exceptionPropability the probability to reach the maximum possible delay
     */
    protected void setExceptionPropability(double exceptionPropability) {
        this.exceptionPropability = exceptionPropability;
    }

    /**
     * gets the delay
     * @return the predicted delay for this particular trip leg at boarding in seconds
     */
    public int getDelayBoardingRegular() {
        return delayBoardingRegular;
    }

    /**
     * gets the delay
     * @return the predicted delay for this particular trip leg at arrival in seconds
     */
    public int getDelayAlightingRegular() {
        return delayAlightingRegular;
    }

    /**
     * gets the delay
     * @return the maximum delay expected for this trip
     */
    public int getDelayException() {
        return delayException;
    }

    /**
     * gets the prob
     * @return the probability to reach the maximum possible delay
     */
    public double getExceptionPropability() {
        return exceptionPropability;
    }
}
