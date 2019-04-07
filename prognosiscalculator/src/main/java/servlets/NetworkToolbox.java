package servlets;

import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;

public class NetworkToolbox {
    public static JSONObject readJSONObjectFromRequestBody(HttpServletRequest request) {
        try {
            return new JSONObject(readRequest(request));
        } catch (JSONException e) {
            // crash and burn
            return null;
        }
    }

    public static String readRequest(HttpServletRequest request) {
        StringBuilder jb = new StringBuilder();
        String line = null;
        try {
            BufferedReader reader = request.getReader();
            while ((line = reader.readLine()) != null)
                jb.append(line);
        } catch (Exception e) { /*report an error*/ }

        return jb.toString();
    }
}
