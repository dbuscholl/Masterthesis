package Network;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class Connection {
    private static String urlString = "http://efastatic.vvs.de/kleinanfrager/trias";
    private HttpURLConnection http;

    public Connection() throws IOException {
        URL url = new URL(urlString);
        URLConnection con = url.openConnection();
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

        return s.toString();
    }
}