package servlets;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


public class UserRecordingServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request,response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        JSONObject jsonObject = readJSONObjectFromRequestBody(request);

        if(jsonObject==null) {
            response.getWriter().println(new ErrorResponse("Invalid JSON as POST Data").toString());
            response.getWriter().close();
        }


    }

    private JSONObject readJSONObjectFromRequestBody(HttpServletRequest request) {
        StringBuilder jb = new StringBuilder();
        String line = null;
        try {
            BufferedReader reader = request.getReader();
            while ((line = reader.readLine()) != null)
                jb.append(line);
        } catch (Exception e) { /*report an error*/ }

        try {
            return new JSONObject(jb.toString());
        } catch (JSONException e) {
            // crash and burn
            return null;
        }
    }
}
