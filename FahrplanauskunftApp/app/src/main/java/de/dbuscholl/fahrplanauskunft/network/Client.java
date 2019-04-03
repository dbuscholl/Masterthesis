package de.dbuscholl.fahrplanauskunft.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;

public class Client {
    private HttpURLConnection http;

    public Client(String url) throws IOException {
        URL urlobject = new URL(url);
        URLConnection con = urlobject.openConnection();
        http = (HttpURLConnection) con;
    }


    public String sendPostXML(String xml) throws IOException {
        http.setRequestMethod("POST");
        http.setDoOutput(true);
        http.setDoInput(true);
        http.setUseCaches(false);

        byte[] out = xml.getBytes(StandardCharsets.UTF_8);
        int length = out.length;

        http.setFixedLengthStreamingMode(length);
        http.setRequestProperty("content-type", "text/xml; charset=UTF-8");

        OutputStreamWriter writer = new OutputStreamWriter( http.getOutputStream());
        writer.write(xml);
        writer.flush();

        StringBuilder s = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(http.getInputStream()));
        for (String line; (line = reader.readLine()) != null;) {
            s.append(line);
        }

        writer.close();
        reader.close();
        http.disconnect();

        return s.toString();
    }

    public String sendPostJSON(String json) throws IOException {
        http.setRequestMethod("POST");
        http.setDoOutput(true);
        http.setDoInput(true);
        http.setUseCaches(false);

        byte[] out = json.getBytes(StandardCharsets.UTF_8);
        int length = out.length;

        http.setFixedLengthStreamingMode(length);
        http.setRequestProperty("content-type", "application/json; charset=UTF-8");

        OutputStreamWriter writer = new OutputStreamWriter( http.getOutputStream());
        writer.write(json);
        writer.flush();

        StringBuilder s = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(http.getInputStream()));
        for (String line; (line = reader.readLine()) != null;) {
            s.append(line);
        }

        writer.close();
        reader.close();
        http.disconnect();

        return s.toString();
    }

}
