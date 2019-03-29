package common.prognosis;

import common.network.Connection;
import common.network.Trip;
import database.GTFS;
import database.PrognosisDatabase;

import java.sql.SQLException;
import java.util.ArrayList;

public class PrognosisCalculator extends Thread implements CalculationCompletedEvent {
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

        TriasSamedayFactor tsf = new TriasSamedayFactor();
        factory.add(tsf);
        tsf.setCalculationCompletedEvent(this);

        Thread.sleep(1000);

        onCalculationComplete(1);
    }

    @Override
    public void onCalculationComplete(int result) {
        /*for(PrognosisFactor f : factory) {
            if(!f.isDoneExecuting()) {
                return;
            }
        }*/
        if(calculationCompletedEvent!=null) {
            calculationCompletedEvent.onCalculationComplete(result);
        }
    }

    public void setCalculationCompletedEvent(CalculationCompletedEvent calculationCompletedEvent) {
        this.calculationCompletedEvent = calculationCompletedEvent;
    }

    public void verifyIds() throws SQLException {
        for(Trip t : connection.getLegs()) {
            if(t.getGTFSTripId() == null) {
                String tripId = GTFS.getTripId(t);
                t.setGTFSTripId(tripId);
                PrognosisDatabase.insertBlank(tripId);
            }
        }
    }
}
