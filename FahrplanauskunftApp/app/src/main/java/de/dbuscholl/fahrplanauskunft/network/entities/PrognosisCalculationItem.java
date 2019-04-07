package de.dbuscholl.fahrplanauskunft.network.entities;

import org.json.JSONException;
import org.json.JSONObject;

public class PrognosisCalculationItem {
    private int delayBoardingRegular;
    private int delayAlightingRegular;

    private int delayException;
    private double exceptionPropability;

    public PrognosisCalculationItem() {
    }

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
