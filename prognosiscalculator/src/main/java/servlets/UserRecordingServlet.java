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

/**
 * This servlet is the entry point of the user data importer and returns if that was successful and how long th eimport
 * took
 */
public class UserRecordingServlet extends HttpServlet {
    private PrintWriter out;

    /**
     * redirect to get
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request,response);
    }

    /**
     * first read and parse the json string and then initialize the {@link UserRecordingImporter} which does the rest. This
     * servlet only returns the execution time when everything is done.
     * @param request request to be parsed and of which the data should be imported
     * @param response response.
     * @throws ServletException
     * @throws IOException
     */
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
