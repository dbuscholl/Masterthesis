package common.prognosis;

import common.network.Connection;

import java.util.ArrayList;

public class PrognosisCalculationResult {
    private Connection connection;
    private ArrayList<PrognosisCalculationItem> items = new ArrayList<>();

    public void add(PrognosisCalculationItem resultItem) {
        items.add(resultItem);
    }

    public ArrayList<PrognosisCalculationItem> getItems() {
        return items;
    }

    protected void setConnection(Connection connection) {
        this.connection = connection;
    }

    public Connection getConnection() {
        return connection;
    }
}
