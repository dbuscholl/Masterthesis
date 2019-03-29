package servlets;

import database.procedures.UserRecordingImporter;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


public class UserRecordingServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request,response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        JSONObject jsonObject = NetworkToolbox.readJSONObjectFromRequestBody(request);

        if(jsonObject==null) {
            response.getWriter().println(new ErrorResponse("Invalid JSON as POST Data").toString());
            response.getWriter().close();
        } else {
            UserRecordingImporter.doWork(jsonObject);
        }


    }
}
