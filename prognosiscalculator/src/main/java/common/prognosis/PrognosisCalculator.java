package common.prognosis;

import common.network.Connection;
import common.network.Trip;
import database.GTFS;
import database.PrognosisDatabase;
import org.apache.log4j.Logger;

import java.sql.SQLException;
import java.util.ArrayList;

public class PrognosisCalculator extends Thread implements CalculationCompletedEvent {
    private Logger logger = Logger.getLogger(this.getClass().getName());
    private Connection connection;
    private ArrayList<PrognosisFactor> factory = new ArrayList<>();
    private CalculationCompletedEvent calculationCompletedEvent;

    public PrognosisCalculator(Connection c) {
        this.connection = c;
    }

    @Override
    public void run() {
        try {
            execute();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void execute() throws SQLException, InterruptedException {
        verifyIds();

        FactorTrias triasSameday = new FactorTrias(connection);
        triasSameday.setType(PrognosisFactor.PrognosisFactorType.TRIASRECORDING_SAMEDAY);
        triasSameday.setWeight(3);
        triasSameday.setCalculationCompletedEvent(this);
        factory.add(triasSameday);

        FactorTrias triasEveryday = new FactorTrias(connection);
        triasEveryday.setType(PrognosisFactor.PrognosisFactorType.TRIASRECORDING_EVERYDAY);
        triasEveryday.setCalculationCompletedEvent(this);
        factory.add(triasEveryday);

        FactorTrias triasAllday = new FactorTrias(connection);
        triasAllday.setType(PrognosisFactor.PrognosisFactorType.TRIASRECORDING_ALLDAY);
        triasAllday.setWeight(0.5);
        triasAllday.setCalculationCompletedEvent(this);
        factory.add(triasAllday);

        FactorUserRecording userRecordings = new FactorUserRecording(connection);
        userRecordings.setWeight(10);
        userRecordings.setCalculationCompletedEvent(this);
        factory.add(userRecordings);

        FactorAsked cleanness = new FactorAsked(connection);
        cleanness.setCalculationCompletedEvent(this);
        cleanness.setType(PrognosisFactor.PrognosisFactorType.QUESTIONNAIRE_CLEANNESS);
        factory.add(cleanness);

        FactorAsked capacity = new FactorAsked(connection);
        capacity.setCalculationCompletedEvent(this);
        capacity.setType(PrognosisFactor.PrognosisFactorType.QUESTIONNAIRE_CAPACITY);
        factory.add(capacity);

        FactorAskedDelay askedDelay = new FactorAskedDelay(connection);
        askedDelay.setCalculationCompletedEvent(this);
        askedDelay.setType(PrognosisFactor.PrognosisFactorType.QUESTIONNAIRE_DELAY);
        factory.add(askedDelay);

        FactorAskedInterchange askedInterchange = new FactorAskedInterchange(connection);
        askedInterchange.setCalculationCompletedEvent(this);
        askedInterchange.setType(PrognosisFactor.PrognosisFactorType.QUESTIONNAIRE_INTERCHANGE);
        factory.add(askedInterchange);

        for (PrognosisFactor f : factory) {
            f.start();
        }

    }

    @Override
    public void onCalculationComplete(PrognosisFactor factor) {
        if(factor != null) {
            logger.info(factor.getType().toString() + " has finished execution in " + ((double) factor.getExecutionTime() / 1000) + "s");
        }
        for (PrognosisFactor f : factory) {
            if (!f.isDoneExecuting()) {
                return;
            }
        }
        if (calculationCompletedEvent != null) {
            calculationCompletedEvent.onCalculationComplete(null);
        }
    }

    public void setCalculationCompletedEvent(CalculationCompletedEvent calculationCompletedEvent) {
        this.calculationCompletedEvent = calculationCompletedEvent;
    }

    public void verifyIds() throws SQLException {
        for (Trip t : connection.getLegs()) {
            if (t.getGTFSTripId() == null) {
                String tripId = GTFS.getGTFSTripId(t);
                t.setGTFSTripId(tripId);
                PrognosisDatabase.insertBlank(tripId, t.getService().getOperatingDayRef(), t.getService().getJourneyRef());
            }
        }
    }

    public ArrayList<PrognosisFactor> getFactory() {
        return factory;
    }
}
