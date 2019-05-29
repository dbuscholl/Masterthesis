package servlets;

import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;

/**
 * Thic class provides some helper functions which have to do with networking.
 */
public class NetworkToolbox {
    /**
     * this functions reads a json object from the request body and returns it as parsed object
     * @param request the servlet request from which the json should be read out
     * @return the json object with data or null if wrong formatted or empty
     */
    public static JSONObject readJSONObjectFromRequestBody(HttpServletRequest request) {
        try {
            return new JSONObject(readRequest(request));
        } catch (JSONException e) {
            // crash and burn
            return null;
        }
    }

    /**
     * this function reads a json string from the request body and returns it
     * @param request the servlet request from which the json should be read out
     * @return the json string with data or null if wrong formatted or empty
     */
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
