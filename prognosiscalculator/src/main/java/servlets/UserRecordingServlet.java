package servlets;

import database.procedures.UserRecordingImporter;
import org.json.JSONObject;
import utilities.Chronometer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;


public class UserRecordingServlet extends HttpServlet {
    private PrintWriter out;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request,response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        out = response.getWriter();

        Chronometer chronometer = new Chronometer();
        chronometer.addNow();

        JSONObject jsonObject = NetworkToolbox.readJSONObjectFromRequestBody(request);

        if(jsonObject==null) {
            response.getWriter().println(new ErrorResponse("Invalid JSON as POST Data").toString());
            response.getWriter().close();
        } else {
            UserRecordingImporter.doWork(jsonObject);
        }

        chronometer.addNow();

        JSONObject output = new JSONObject();
        output.put("success",true);
        output.put("execution time", ((double) chronometer.getLastDifferece() / 1000));

        out.print(output);

    }
}
