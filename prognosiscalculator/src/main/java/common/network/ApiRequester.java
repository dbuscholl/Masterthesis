package common.network;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;

public class ApiRequester {
    private static String urlString = "https://maps.googleapis.com/maps/api/directions/json?";
    private static final String API_KEY = "AIzaSyC00tX3To7l7oMrQX2u4mgN3LM2tLZQIt0";

    /**
     * Start this to test the API
     * @param args
     */
    public static void main(String[] args) {
        int travelTime = getTravelTime("48.739848,9.227002", "47.918971,8.187857", "48.727673,9.211101");
        System.out.println((double) travelTime/60);
    }

    public static int getTravelTime(String origin, String destination, String via) {
        try {
            String json = sendRequest(origin, destination, via);
            JSONObject obj = new JSONObject(json);
            if (obj.has("routes")) {
                JSONArray routes = obj.getJSONArray("routes");
                if (routes.length() > 0) {
                    obj = routes.getJSONObject(0);
                    if (obj.has("legs")) {
                        JSONArray legs = obj.getJSONArray("legs");
                        if (routes.length() > 0) {
                            obj = legs.getJSONObject(0);
                            if (obj.has("duration_in_traffic")) {
                                obj = obj.getJSONObject("duration_in_traffic");
                                if (obj.has("text")) {
                                    return obj.getInt("value");
                                }
                            } else if (obj.has("duration")) {
                                obj = obj.getJSONObject("duration");
                                if (obj.has("text")) {
                                    return obj.getInt("value");
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            return -1;
        }
        return -1;
    }

    public static String sendRequest(String origin, String destination, String via) throws IOException {
        urlString += "origin=" + origin + "&destination=" + destination;
        if (via != null) {
            urlString += "&waypoints=via:" + via;
        }
        urlString += "&departure_time=now&mode=driving&traffic_model=pessimistic&key=" + API_KEY;
        URL url = new URL(urlString);
        URLConnection con = url.openConnection();
        HttpURLConnection http = (HttpURLConnection) con;
        http.setRequestMethod("GET");
        http.setUseCaches(false);
        int responseCode = http.getResponseCode();

        StringBuilder s = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(http.getInputStream()));
        for (String line; (line = reader.readLine()) != null; ) {
            s.append(line);
        }

        reader.close();
        http.disconnect();

        return s.toString();
    }
}
