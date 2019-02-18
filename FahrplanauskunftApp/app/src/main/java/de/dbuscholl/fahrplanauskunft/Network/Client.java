package de.dbuscholl.fahrplanauskunft.Network;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public class Client {
    private HttpURLConnection http;

    public Client(String url) throws IOException {
        URL urlobject = new URL(url);
        URLConnection con = urlobject.openConnection();
        http = (HttpURLConnection) con;
        http.setDoInput(true);
        http.setUseCaches(false);
    }

}
