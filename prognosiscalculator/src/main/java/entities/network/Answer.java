package entities.network;

import org.json.JSONObject;

public class Answer {
    private int capacity = -1;
    private int cleanness = -1;
    private int delay = Integer.MAX_VALUE;
    private boolean successfullyInterchanged;

    public Answer() {
    }

    public Answer(int capacity, int cleanness, int delay, boolean successfullyInterchanged) {
        this.capacity = capacity;
        this.cleanness = cleanness;
        this.delay = delay;
        this.successfullyInterchanged = successfullyInterchanged;
    }

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

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getCleanness() {
        return cleanness;
    }

    public void setCleanness(int cleanness) {
        this.cleanness = cleanness;
    }

    public int getDelay() {
        return delay;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    public boolean isSuccessfullyInterchanged() {
        return successfullyInterchanged;
    }

    public void setSuccessfullyInterchanged(boolean successfullyInterchanged) {
        this.successfullyInterchanged = successfullyInterchanged;
    }
}
