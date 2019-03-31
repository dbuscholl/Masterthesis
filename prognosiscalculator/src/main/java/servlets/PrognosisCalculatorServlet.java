package servlets;

import common.network.Connection;
import common.network.Trip;
import common.prognosis.CalculationCompletedEvent;
import common.prognosis.PrognosisCalculationResult;
import common.prognosis.PrognosisCalculator;
import common.network.UserRecordingData;
import common.prognosis.PrognosisFactor;
import database.GTFS;
import database.PrognosisDatabase;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@WebServlet(name = "PrognosisCalculatorServlet")
public class PrognosisCalculatorServlet extends HttpServlet implements CalculationCompletedEvent {
    final Lock lock = new ReentrantLock();
    final Condition data = lock.newCondition();

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request,response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            JSONObject jsonObject = NetworkToolbox.readJSONObjectFromRequestBody(request);

            if (jsonObject == null || !jsonObject.has("connection")) {
                response.getWriter().println(new ErrorResponse("Invalid JSON as POST Data").toString());
                response.getWriter().close();
            } else {
                UserRecordingData urd = new UserRecordingData(jsonObject);
                Connection connection = urd.getConnection();

                // TODO: check if there either already is a result or a result is being calculated at the moment !!!

                for (Trip t : connection.getLegs()) {
                    String tripId = GTFS.getTripId(t);
                    t.setGTFSTripId(tripId);
                    PrognosisDatabase.insertBlank(tripId);

                }
                PrognosisCalculator prognosisCalculator = new PrognosisCalculator(connection);
                prognosisCalculator.setCalculationCompletedEvent(this);
                prognosisCalculator.start();

                // waiting for response
                lock.lock();
                data.await();
                lock.unlock();

                // this is executing after signal is fired
                ArrayList<PrognosisFactor> factory = prognosisCalculator.getFactory();

                System.out.println("lalalala");
                response.getWriter().close();
            }
        } catch (Exception e) {
            response.getWriter().println(new ErrorResponse("Invalid JSON as POST Data").toString());
            response.getWriter().close();
        }
    }

    @Override
    public void onCalculationComplete(PrognosisCalculationResult result) {
        lock.lock();
        System.out.println("Singal fired");
        data.signal();
        lock.unlock();
    }
}
