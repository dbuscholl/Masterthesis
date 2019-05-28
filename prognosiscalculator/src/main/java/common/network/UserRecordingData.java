package common.network;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * This is a container class for data which comes from the android app. It contains the class representation of the JSON
 * payload of the http request from the app. This class resp. the request contains the connection for which the data should
 * be processed and the actual data which can be either location or answers or even both of them. Depending on the JSON-object
 * this class builds the attrbiutes. So therefore check the arraylists before accessing them, they might be empty.
 */
public class UserRecordingData {
    private Connection connection;
    private ArrayList<CustomLocation> locations = new ArrayList<>();
    private ArrayList<Answer> answers = new ArrayList<>();

    /**
     * empty constructor
     */
    public UserRecordingData() {}

    /**
     * parameterized constructor
     * @param connection the connection class for which data is for
     * @param locations the list of location given by the android app
     * @param answers the answers from the questionnaire of the android app
     */
    public UserRecordingData(Connection connection, ArrayList<CustomLocation> locations, ArrayList<Answer> answers) {
        this.connection = connection;
        this.locations = locations;
        this.answers = answers;
    }

    /**
     * build this container class from a given JSON Object and fills the attributes with it. It spans the complete class
     * including the connection with all trips and legs and stoppoints. So therefore you don't need to call the seperate
     * constructors of the parts of the connection.
     * @param json
     */
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

    /**
     * getter
     * @return he connection class for which data is for
     */
    public Connection getConnection() {
        return connection;
    }

    /**
     * getter
     * @return he list of location given by the android app
     */
    public ArrayList<CustomLocation> getLocations() {
        return locations;
    }

    /**
     * getter
     * @return the answers from the questionnaire of the android app
     */
    public ArrayList<Answer> getAnswers() {
        return answers;
    }
}
