package Network;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

/**
 * A Class responsible for all Network Connections which are send to trias (VVS)
 */
public class TriasConnection {
    private static String urlString = "http://efastatic.vvs.de/kleinanfrager/trias";
    private HttpURLConnection http;

    /**
     * Building the URL and already opening the connection
     * @throws IOException
     */
    public TriasConnection() throws IOException {
        URL url = new URL(urlString);
        URLConnection con = url.openConnection();
        http = (HttpURLConnection) con;
    }

    /**
     * sends an XML-String to the TRIAS interface. Check resources for Example Files. For actual requesting themplates are used.
     * @param xml XML-String to be send
     * @return String response from the server
     * @throws IOException
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
}