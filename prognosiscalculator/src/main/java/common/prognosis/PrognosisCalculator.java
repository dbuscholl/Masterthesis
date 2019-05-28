package common.prognosis;

import common.network.Connection;
import common.network.Trip;
import database.GTFS;
import database.PrognosisDatabase;
import org.apache.log4j.Logger;

import java.sql.SQLException;
import java.util.ArrayList;

/**
 * This class it the main executor and coordinator of the prognosis calculation as the classname might tell. It first
 * takes care of that every tripleg gets its ID and then starts all calculation methods. It stores all factors into the
 * factory which is an arraylist and checks if all factors are done with calculating as soon as one is finished. This is done
 * with the {@link CalculationCompletedEvent}.
 */
public class PrognosisCalculator extends Thread implements CalculationCompletedEvent {
    private Logger logger = Logger.getLogger(this.getClass().getName());
    private Connection connection;
    private ArrayList<PrognosisFactor> factory = new ArrayList<>();
    private CalculationCompletedEvent calculationCompletedEvent;

    /**
     * constructor
     * @param c the connection for which you want this crazy cool calculator to create a prognosis
     */
    public PrognosisCalculator(Connection c) {
        this.connection = c;
    }

    /**
     * the actual start function which is called by Thread.start().
     */
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

    /**
     * this is the exeuction logic. First it initializes all factors and weights them. Then it assigns itself with its
     * own callback as listener and adds all factors to the so called "factory" which stores all into an arraylist.
     * @throws SQLException when something goes wrong during database processes
     * @throws InterruptedException thread stuff
     */
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

        FactorGoogleDirections googleTraffic = new FactorGoogleDirections(connection);
        googleTraffic.setWeight(10);
        googleTraffic.setCalculationCompletedEvent(this);
        factory.add(googleTraffic);

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

    /**
     * The callback function as soon as any factor is finished with calculating. The calculator checks if any other is still
     * calculating and only when all of them are done it fires an own completion event for the servlet who called this.
     * @param factor the factor which is done with calculating
     */
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

    /**
     * allows to set a calculation completed event but for when all factors are done and not only one. this is kind of a
     * delegate.
     * @param calculationCompletedEvent the function which should be called after complete calculation of all factors
     */
    public void setCalculationCompletedEvent(CalculationCompletedEvent calculationCompletedEvent) {
        this.calculationCompletedEvent = calculationCompletedEvent;
    }

    /**
     * zhis function adds the gtfs trip id to all trip legs as they differ from the TRIAS ids and are needed for the
     * database storage.
     * @throws SQLException when something goes wrong during database things.
     */
    public void verifyIds() throws SQLException {
        for (Trip t : connection.getLegs()) {
            if (t.getGTFSTripId() == null) {
                String tripId = GTFS.getGTFSTripId(t);
                t.setGTFSTripId(tripId);
                PrognosisDatabase.insertBlank(tripId, t.getService().getOperatingDayRef(), t.getService().getJourneyRef());
            }
        }
    }

    /**
     * getter
     * @return the factory containing all factors even if they are not done calculating
     */
    public ArrayList<PrognosisFactor> getFactory() {
        return factory;
    }
}
