package common.network;

import org.json.JSONObject;

/**
 * This class represents an answer from the questionnaire by the android app. It contains information about the cleanness,
 * capacity, subjective delay and the succesfully interchanged flag.
 */
public class Answer {
    private int capacity = -1;
    private int cleanness = -1;
    private int delay = Integer.MAX_VALUE;
    private boolean successfullyInterchanged;

    /**
     * empty constructor
     */
    public Answer() {
    }

    /**
     * parameterized constructor
     * @param capacity Giving information about the capacity of the vehicle. The higher the fuller. Only accepting values
     *                 between 0 and 2
     * @param cleanness Giving information about the cleannes of the vehicle. The lower the cleaner. Only accepting values
     *                  between 0 and 2
     * @param delay the amount of subjective delay
     * @param successfullyInterchanged 0 if interchange failed, 1 if interchange was successfull
     */
    public Answer(int capacity, int cleanness, int delay, boolean successfullyInterchanged) {
        this.capacity = capacity;
        this.cleanness = cleanness;
        this.delay = delay;
        this.successfullyInterchanged = successfullyInterchanged;
    }

    /**
     * JSON Contructor. Assigns all values by a JSON Object. Very handy for http requests.
     * @param json the object from which the attributes should be set.
     */
    public Answer(JSONObject json) {
        if (json.has("capacity")) {
            capacity = getValue(json.getString("capacity"));
        }
        if (json.has("cleanness")) {
            cleanness = getValue(json.getString("cleanness"));
        }

        if (json.has("delay")) {
            String value = json.getString("delay");
            value = value.replaceAll("[^0-9]", "").trim();
            try {
                delay = Integer.parseInt(value);
            } catch (NumberFormatException e) {
                delay = Integer.MAX_VALUE;
            }
        }

        if (json.has("interchangeToNextTrip")) {
            String value = json.getString("interchangeToNextTrip");
            switch (value) {
                case "0":
                    successfullyInterchanged = false;
                case "1":
                    successfullyInterchanged = true;
            }
        }
    }

    /**
     * changes a text value into a integer value for better storage and statistics calculation.
     * <p>Changing following: <table><tr><td>Leer</td><td>0</td></tr><tr><td>Mittel</td><td>1</td></tr><tr><td>Voll</td><td>2</td></tr>
     * <tr><td>Besonders sauber</td><td>0</td></tr><tr><td>In Ordnung</td><td>1</td></tr><tr><td>Schmutzig</td><td>2</td></tr></table></p>
     * @param item the text value which should be converted into an integer
     * @return a number representing the given text value.
     */
    private int getValue(String item) {
        if(item.contains("Leer"))
            return 0;
        if(item.contains("Mittel"))
            return 1;
        if(item.contains("Voll"))
            return 2;

        if(item.equals("Besonders sauber"))
            return 0;
        if(item.equals("In Ordnung"))
            return 1;
        if(item.equals("Schmutzig"))
            return 2;

        return -1;
    }

    /**
     * getter
     * @return Giving information about the capacity of the vehicle. The higher the fuller. Only accepting valuesbetween 0 and 2
     */
    public int getCapacity() {
        return capacity;
    }

    /**
     * setter
     * @param capacity Giving information about the capacity of the vehicle. The higher the fuller. Only accepting valuesbetween 0 and 2
     */
    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    /**
     * getter
     * @return Giving information about the cleannes of the vehicle. The lower the cleaner. Only accepting values between 0 and 2
     */
    public int getCleanness() {
        return cleanness;
    }

    /**
     * setter
     * @param cleanness Giving information about the cleannes of the vehicle. The lower the cleaner. Only accepting values between 0 and 2
     */
    public void setCleanness(int cleanness) {
        this.cleanness = cleanness;
    }

    /**
     * getter
     * @return the amount of subjective delay
     */
    public int getDelay() {
        return delay;
    }

    /**
     * setter
     * @param delay the amount of subjective delay
     */
    public void setDelay(int delay) {
        this.delay = delay;
    }

    /**
     * getter
     * @return 0 if interchange failed, 1 if interchange was successfull
     */
    public boolean isSuccessfullyInterchanged() {
        return successfullyInterchanged;
    }

    /**
     * setter
     * @param successfullyInterchanged 0 if interchange failed, 1 if interchange was successfull
     */
    public void setSuccessfullyInterchanged(boolean successfullyInterchanged) {
        this.successfullyInterchanged = successfullyInterchanged;
    }
}
