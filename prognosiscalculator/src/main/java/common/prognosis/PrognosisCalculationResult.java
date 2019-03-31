package common.prognosis;

import common.network.Connection;

import java.util.ArrayList;

public class PrognosisCalculationResult {
    private Connection connection;
    private ArrayList<Item> items;

    public void add(Item resultItem) {
        items.add(resultItem);
    }

    public ArrayList<Item> getItems() {
        return items;
    }

    protected void setConnection(Connection connection) {
        this.connection = connection;
    }

    public Connection getConnection() {
        return connection;
    }

    public static class Item {
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
}
