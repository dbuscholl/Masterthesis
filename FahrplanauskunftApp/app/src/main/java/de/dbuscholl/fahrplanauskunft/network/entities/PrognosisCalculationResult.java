package de.dbuscholl.fahrplanauskunft.network.entities;

public class PrognosisCalculationResult {
    PrognosisCalculationItem prognosis;
    Service service;

    public PrognosisCalculationResult() {}

    public PrognosisCalculationResult(PrognosisCalculationItem item, Service service) {
        this.prognosis = item;
        this.service = service;
    }

    public PrognosisCalculationItem getPrognosis() {
        return prognosis;
    }

    public void setPrognosis(PrognosisCalculationItem prognosis) {
        this.prognosis = prognosis;
    }

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }
}
