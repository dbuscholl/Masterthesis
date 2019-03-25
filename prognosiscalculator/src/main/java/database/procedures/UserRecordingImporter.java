package database.procedures;

import database.DataSource;
import entities.network.UserRecordingData;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.SQLException;

public class UserRecordingImporter {
    private static void doWork(JSONObject json) {
        UserRecordingData userRecordingData = new UserRecordingData(json);
    }
}
