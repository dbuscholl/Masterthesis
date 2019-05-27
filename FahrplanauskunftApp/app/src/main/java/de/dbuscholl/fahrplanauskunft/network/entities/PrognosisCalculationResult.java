package de.dbuscholl.fahrplanauskunft.network.entities;

/**
 * container class for the prognosiscalculation items. This is necessary to provide additional information as this class
 * also stores the service information for which the prognosis is for.
 */
public class PrognosisCalculationResult {
    PrognosisCalculationItem prognosis;
    Service service;

    /**
     * empty constructor
     */
    public PrognosisCalculationResult() {}

    /**
     * parameterized constructor
     * @param item the actual prognosis calculations
     * @param service information about the service like line number and name
     */
    public PrognosisCalculationResult(PrognosisCalculationItem item, Service service) {
        this.prognosis = item;
        this.service = service;
    }

    /**
     * getter
     * @return the actual prognosis calculations
     */
    public PrognosisCalculationItem getPrognosis() {
        return prognosis;
    }

    /**
     * setter
     * @param prognosis actual prognosis calculations
     */
    public void setPrognosis(PrognosisCalculationItem prognosis) {
        this.prognosis = prognosis;
    }

    /**
     * getter
     * @return information about the service like line number and name
     */
    public Service getService() {
        return service;
    }

    /**
     * setter
     * @param service information about the service like line number and name
     */
    public void setService(Service service) {
        this.service = service;
    }
}
