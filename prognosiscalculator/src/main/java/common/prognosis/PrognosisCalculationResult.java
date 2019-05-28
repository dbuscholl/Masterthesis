package common.prognosis;

import common.network.Connection;

import java.util.ArrayList;

/**
 * This entity class works as container for the http resonse as well as for the results of prognosis calculation. For
 * each trip leg of the connection there must be a {@link PrognosisCalculationItem} which is at the same index as the trip
 * leg to correctly identify for which the prognosis was calculated.
 */
public class PrognosisCalculationResult {
    private Connection connection;
    private ArrayList<PrognosisCalculationItem> items = new ArrayList<>();

    /**
     * delegate method for adding item to the arraylist
     * @param resultItem item to add to the arraylist
     */
    public void add(PrognosisCalculationItem resultItem) {
        items.add(resultItem);
    }

    /**
     * getter
     * @return list of prognosis items
     */
    public ArrayList<PrognosisCalculationItem> getItems() {
        return items;
    }

    /**
     * setter
     * @param connection the connection for which the prognosis was calculated
     */
    protected void setConnection(Connection connection) {
        this.connection = connection;
    }

    /**
     * getter
     * @return the connection for which the prognosis was calculated
     */
    public Connection getConnection() {
        return connection;
    }
}
