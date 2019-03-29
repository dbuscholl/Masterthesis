package common.network;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class UserRecordingData {
    private Connection connection;
    private ArrayList<CustomLocation> locations = new ArrayList<>();
    private ArrayList<Answer> answers = new ArrayList<>();

    public UserRecordingData() {}

    public UserRecordingData(Connection connection, ArrayList<CustomLocation> locations, ArrayList<Answer> answers) {
        this.connection = connection;
        this.locations = locations;
        this.answers = answers;
    }

    public UserRecordingData(JSONObject json) {
        try {
            connection = json.has("connection") ? new Connection(json.getJSONObject("connection")) : null;

            if (json.has("recordingData") && json.getJSONArray("recordingData").length() > 0) {
                JSONArray recordingData = json.getJSONArray("recordingData");
                locations = new ArrayList<>();
                for (int i = 0; i < recordingData.length(); i++) {
                    JSONObject item = recordingData.getJSONObject(i);
                    locations.add(new CustomLocation(item));
                }
            }

            if(json.has("answers") && json.getJSONArray("answers").length() > 0) {
                JSONArray answers = json.getJSONArray("answers");
                this.answers = new ArrayList<>();
                for (int i = 0; i < answers.length(); i++) {
                    JSONObject item = answers.getJSONObject(i);
                    this.answers.add(new Answer(item));
                }
            }
        } catch (JSONException e) {
            throw new NullPointerException(e.getMessage());
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public ArrayList<CustomLocation> getLocations() {
        return locations;
    }

    public ArrayList<Answer> getAnswers() {
        return answers;
    }
}
