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
import org.apache.log4j.Logger;
import org.json.JSONObject;
import utilities.MathToolbox;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@WebServlet(name = "PrognosisCalculatorServlet")
public class PrognosisCalculatorServlet extends HttpServlet implements CalculationCompletedEvent {
    final Lock lock = new ReentrantLock();
    final Condition data = lock.newCondition();
    private Logger logger = Logger.getLogger(getClass().getName());

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
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

                ArrayList<Trip> legs = connection.getLegs();
                for (int i = 0; i < legs.size(); i++) {
                    Trip leg = legs.get(i);
                    int delayBoarding = 0;
                    int delayAlighting = 0;
                    ArrayList<Integer> delaysException = new ArrayList<>();
                    double exceptionPropability = 0;

                    for (PrognosisFactor f : factory) {
                        ArrayList<PrognosisCalculationResult.Item> items = f.getResult().getItems();

                        if (i < items.size()) {
                            PrognosisCalculationResult.Item item = items.get(i);
                            delayBoarding += item.getDelayBoardingRegular() * f.getWeight();
                            delayAlighting += item.getDelayAlightingRegular() * f.getWeight();
                            if (item.getDelayException() > 0) {
                                delaysException.add(item.getDelayException());
                            }
                            exceptionPropability += item.getExceptionPropability() * f.getWeight();

                            logger.info("-----------------------------------------------------------------");
                            logger.info(f.getType() + " for " + leg.getService().getLineName() + ":");
                            logger.info("Delay at " + leg.getBoarding().getName() + ": " + item.getDelayBoardingRegular());
                            logger.info("Delay at " + leg.getAlighting().getName() + ": " + item.getDelayAlightingRegular());
                            logger.info("Delay Exception: " + item.getDelayException());
                            logger.info("Exception Propability: " + item.getExceptionPropability());
                        }
                    }

                    delayAlighting = delayAlighting / factory.size();
                    delayBoarding = delayBoarding / factory.size();
                    int delayException = (int) MathToolbox.mean(delaysException);
                    exceptionPropability = exceptionPropability / factory.size();

                    logger.info("-----------------------------------------------------------------");
                    logger.info("Total Boarding Delay for " + leg.getService().getLineName() + " at " + leg.getBoarding().getName() + ": " + delayBoarding + "s!");
                    logger.info("Total Alighting Delay for " + leg.getService().getLineName() + " at " + leg.getAlighting().getName() + ": " + delayAlighting + "s!");
                    logger.info("Total Exception Delay for " + leg.getService().getLineName() + " at " + leg.getAlighting().getName() + ": " + delayException + "s!");
                    logger.info("Propability for Exception for " + leg.getService().getLineName() + " at " + leg.getAlighting().getName() + ": " + new DecimalFormat("#.##").format(exceptionPropability) + "%!");
                }

                System.out.println("lalalala");
                response.getWriter().close();
            }
        } catch (Exception e) {
            response.getWriter().println(new ErrorResponse("Invalid JSON as POST Data").toString());
            response.getWriter().close();
        }
    }

    @Override
    public void onCalculationComplete(PrognosisFactor factor) {
        lock.lock();
        System.out.println("Singal fired");
        data.signal();
        lock.unlock();
    }
}
