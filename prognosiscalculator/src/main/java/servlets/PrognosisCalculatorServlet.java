package servlets;

import common.network.Connection;
import common.network.Trip;
import common.prognosis.CalculationCompletedEvent;
import common.prognosis.PrognosisCalculator;
import common.network.UserRecordingData;
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
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@WebServlet(name = "PrognosisCalculatorServlet")
public class PrognosisCalculatorServlet extends HttpServlet implements CalculationCompletedEvent {
    final Lock lock = new ReentrantLock();
    final Condition data = lock.newCondition();

    boolean calculatingPrognosis = true;
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
                for (Trip t : connection.getLegs()) {
                    String tripId = GTFS.getTripId(t);
                    t.setGTFSTripId(tripId);
                    PrognosisDatabase.insertBlank(tripId);

                }
                PrognosisCalculator prognosisCalculator = new PrognosisCalculator(connection);
                prognosisCalculator.setCalculationCompletedEvent(this);
                prognosisCalculator.start();
                lock.lock();
                System.out.println("locking");
                data.await();
                System.out.println("woken up");
                lock.unlock();


                System.out.println("lalalala");
                response.getWriter().close();
            }
        } catch (Exception e) {
            response.getWriter().println(new ErrorResponse("Invalid JSON as POST Data").toString());
            response.getWriter().close();
        }
    }

    @Override
    public void onCalculationComplete(int result) {
        calculatingPrognosis = false;
        lock.lock();
        System.out.println("Singal fired");
        data.signal();
        lock.unlock();
    }
}
