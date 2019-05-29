package servlets;

import database.PrognosisDatabase;
import org.json.JSONObject;
import utilities.Chronometer;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

/**
 * This class initializes the database with all table structures which are needed for importing and calulcating. It does
 * not create the GTFS database. Use the GTFS-Importer program instead.
 */
@WebServlet(name = "InitServlet")
public class InitServlet extends HttpServlet {
    PrintWriter out;

    /**
     * do nothing
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    /**
     * this creates the PrognosisTable, the Answers Table and the Delays table which stores user measured delays.
     * @param request request
     * @param response response
     * @throws ServletException
     * @throws IOException
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Chronometer chronometer = new Chronometer();
        chronometer.addNow();

        out = response.getWriter();

        try {
            PrognosisDatabase.createPrognosisTable();
            PrognosisDatabase.createUserRecordingsTable();
            PrognosisDatabase.createUserAnswersTable();
        } catch (SQLException e) {
            out.print(new ErrorResponse(e.getMessage()));
        }
        chronometer.addNow();

        JSONObject output = new JSONObject();
        output.put("success",true);
        output.put("execution time", ((double) chronometer.getLastDifferece() / 1000));

        out.print(output);
    }
}
