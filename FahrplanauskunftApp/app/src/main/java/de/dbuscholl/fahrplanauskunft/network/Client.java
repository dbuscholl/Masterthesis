package de.dbuscholl.fahrplanauskunft.network;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;

/**
 * This class works as HTTP client. it starts all requests and returns all responses by the server without really processing
 * them.
 */
public class Client {
    private HttpURLConnection http;

    /**
     * Constructor initializing the Client with a URL
     * @param url url to connect to
     * @throws IOException when something while reading goes wrong
     */
    public Client(String url) throws IOException {
        URL urlobject = new URL(url);
        URLConnection con = urlobject.openConnection();
        http = (HttpURLConnection) con;
    }

    /**
     * Tells the client to use a unusual long timeout for it's request. This is sometimes needed when calling the prognosis
     * backend server, because the calculation might take some time depending on the amount of data.
     */
    public void setLongTimeout() {
        http.setConnectTimeout(120000);
        http.setReadTimeout(120000);
    }

    /**
     * Sends the actual Request of an XML String to the server. This is class is primary used to send requests to the
     * TRIAS interface. It takes any xml. It uses the POST Methode with parameter content-type:
     * application/json; charset=UTF-8
     * @param xml string representation of the xml which should be send in the payload of the request.
     * @return the response of the server to which the request was send.
     * @throws IOException when something while sending or receiving goes wrong
     */
    public String sendPostXML(String xml) throws IOException {
        http.setRequestMethod("POST");
        http.setDoOutput(true);
        http.setDoInput(true);
        http.setUseCaches(false);

        byte[] out = xml.getBytes(StandardCharsets.UTF_8);
        int length = out.length;

        http.setFixedLengthStreamingMode(length);
        http.setRequestProperty("content-type", "text/xml; charset=UTF-8");

        OutputStreamWriter writer = new OutputStreamWriter(http.getOutputStream());
        writer.write(xml);
        writer.flush();

        StringBuilder s = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(http.getInputStream()));
        for (String line; (line = reader.readLine()) != null; ) {
            s.append(line);
        }

        writer.close();
        reader.close();
        http.disconnect();

        return s.toString();
    }

    /**
     * This function sends json requests to a given server. This is used to call the prognosis backend for calculation and
     * importing of the user location and questionnaire data. It uses the POST Methode with parameter content-type:
     * application/json; charset=UTF-8
     * @param json the string representation of json object will be send with the request payload.
     * @return  the response of the server to which the request was send
     * @throws IOException when something while sending or receiving goes wrong
     */
    public String sendPostJSON(String json) throws IOException {
        http.setRequestMethod("POST");
        http.setDoOutput(true);
        http.setDoInput(true);
        http.setUseCaches(false);

        byte[] out = json.getBytes(StandardCharsets.UTF_8);
        int length = out.length;

        http.setFixedLengthStreamingMode(length);
        http.setRequestProperty("content-type", "application/json; charset=UTF-8");

        OutputStreamWriter writer = new OutputStreamWriter(http.getOutputStream());
        writer.write(json);
        writer.flush();

        StringBuilder s = new StringBuilder();

        int status = http.getResponseCode();
        if (status != 200) {
            Log.d(this.getClass().getName(), "Received status of " + String.valueOf(status));
        } else {
            BufferedReader reader = new BufferedReader(new InputStreamReader(http.getInputStream()));
            for (String line; (line = reader.readLine()) != null; ) {
                s.append(line);
            }
            reader.close();
        }
        writer.close();
        http.disconnect();

        return s.toString();
    }

}
