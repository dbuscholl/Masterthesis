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

@WebServlet(name = "InitServlet")
public class InitServlet extends HttpServlet {
    PrintWriter out;
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

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
