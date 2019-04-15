package servlets;

import common.network.Connection;
import common.network.Trip;
import common.prognosis.*;
import common.network.UserRecordingData;
import database.GTFS;
import database.PrognosisDatabase;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import utilities.Chronometer;
import utilities.MathToolbox;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@WebServlet(name = "PrognosisCalculatorServlet")
public class PrognosisCalculatorServlet extends HttpServlet implements CalculationCompletedEvent {
    final Lock lock = new ReentrantLock();
    final Condition data = lock.newCondition();
    private Logger logger = Logger.getLogger(getClass().getName());
    private PrognosisCalculator prognosisCalculator = null;
    private Connection connection = null;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Chronometer chronometer = new Chronometer();
        chronometer.addNow();
        doGet(request, response);
        chronometer.addNow();
        logger.info("Execution done in " + ((double) chronometer.getLastDifferece() / 1000) + "s");
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            JSONObject jsonObject = NetworkToolbox.readJSONObjectFromRequestBody(request);

            if (jsonObject == null || !jsonObject.has("connection")) {
                response.getWriter().println(new ErrorResponse("Invalid JSON as POST Data").toString());
                response.getWriter().close();
            } else {
                UserRecordingData urd = new UserRecordingData(jsonObject);
                connection = urd.getConnection();

                if (checkAlreadyExisting(response)) {
                    return;
                }

                for (Trip t : connection.getLegs()) {
                    String operatingDay = t.getService().getOperatingDayRef();
                    String journeyRef = t.getService().getJourneyRef();

                    String tripId = GTFS.getGTFSTripId(t);
                    t.setGTFSTripId(tripId);
                    PrognosisDatabase.insertBlank(tripId, operatingDay, journeyRef);

                }
                prognosisCalculator = new PrognosisCalculator(connection);
                prognosisCalculator.setCalculationCompletedEvent(this);
                prognosisCalculator.start();

                // waiting for response
                lock.lock();
                data.await();
                lock.unlock();

                // this is executing after signal is fired
                JSONArray output = createOutput(true);
                PrognosisDatabase.update(connection.getLegs(), output);

                response.getWriter().print(output.toString());
                response.getWriter().close();
            }
        } catch (Exception e) {
            response.getWriter().println(new ErrorResponse(e.getMessage()).toString());
            response.getWriter().println(NetworkToolbox.readRequest(request));
            response.getWriter().close();
        }
    }

    private boolean checkAlreadyExisting(HttpServletResponse response) throws SQLException, IOException {
        ArrayList<JSONObject> existingJsons = new ArrayList<>();
        for (Trip t : connection.getLegs()) {
            String operatingDay = t.getService().getOperatingDayRef();
            String journeyRef = t.getService().getJourneyRef();

            PrognosisDatabase.PrognosisItem prognosis = PrognosisDatabase.getBlank(operatingDay, journeyRef);

            if (prognosis == null) {
                break;
            }
            if (prognosis.getJson() == null || prognosis.getJson().equals("")) {
                response.getWriter().print(new ErrorResponse("Prognosis is being calculated. Please be patient!"));
                response.getWriter().close();
                return true;
            }

            long differenceInHour = (new Date().getTime() - prognosis.getTimestamp()) / 1000;
            if (differenceInHour < 24 * 3600) {
                existingJsons.add(new JSONObject(prognosis.getJson()));
            } else {
                if (!PrognosisDatabase.removeEntry(operatingDay, journeyRef)) {
                    throw new SQLException("Seomthing went wrong while removing the cached item " + operatingDay + " - " + journeyRef);
                }
            }
        }
        if (existingJsons.size() > 0) {
            JSONArray output = new JSONArray(existingJsons);
            response.getWriter().print(output.toString());
            response.getWriter().close();
            return true;
        }
        return false;
    }

    private JSONArray createOutput(boolean console) {
        ArrayList<PrognosisFactor> factory = prognosisCalculator.getFactory();
        ArrayList<PrognosisFactor> calculatedFactors = new ArrayList<>();
        ArrayList<PrognosisFactor> askedFactors = new ArrayList<>();

        for (PrognosisFactor f : factory) {
            if (f.getType().toString().trim().toLowerCase().contains("questionnaire")) {
                if (f.getType() == PrognosisFactor.PrognosisFactorType.QUESTIONNAIRE_DELAY) {
                    calculatedFactors.add(f);
                } else {
                    askedFactors.add(f);
                }
            } else {
                calculatedFactors.add(f);
            }
        }

        // TODO: Split factory into two array: asked factors and calculated factors

        ArrayList<Trip> legs = connection.getLegs();

        JSONArray output = new JSONArray();

        for (int i = 0; i < legs.size(); i++) {
            Trip leg = legs.get(i);

            int delayBoarding = 0;
            int delayAlighting = 0;
            ArrayList<Integer> delaysException = new ArrayList<>();
            double exceptionPropability = 0;

            for (PrognosisFactor f : calculatedFactors) {
                ArrayList<PrognosisCalculationItem> items = f.getResult().getItems();

                if (i < items.size()) {
                    PrognosisCalculationItem item = items.get(i);
                    delayBoarding += item.getDelayBoardingRegular() * f.getWeight();
                    delayAlighting += item.getDelayAlightingRegular() * f.getWeight();
                    if (item.getDelayException() > 0) {
                        delaysException.add(item.getDelayException());
                    }
                    exceptionPropability += item.getExceptionPropability() * f.getWeight();

                    if (console) {
                        logger.info("-----------------------------------------------------------------");
                        logger.info(f.getType() + " for " + leg.getService().getLineName() + ":");
                        logger.info("Delay at " + leg.getBoarding().getName() + ": " + item.getDelayBoardingRegular());
                        logger.info("Delay at " + leg.getAlighting().getName() + ": " + item.getDelayAlightingRegular());
                        logger.info("Delay Exception: " + item.getDelayException());
                        logger.info("Exception Propability: " + item.getExceptionPropability());
                    }
                }
            }

            delayAlighting = delayAlighting / factory.size();
            delayBoarding = delayBoarding / factory.size();
            int delayException = (int) MathToolbox.mean(delaysException);
            exceptionPropability = exceptionPropability / factory.size();

            if (console) {
                logger.info("-----------------------------------------------------------------");
                logger.info("Total Boarding Delay for " + leg.getService().getLineName() + " at " + leg.getBoarding().getName() + ": " + delayBoarding + "s!");
                logger.info("Total Alighting Delay for " + leg.getService().getLineName() + " at " + leg.getAlighting().getName() + ": " + delayAlighting + "s!");
                logger.info("Total Exception Delay for " + leg.getService().getLineName() + " at " + leg.getAlighting().getName() + ": " + delayException + "s!");
                logger.info("Propability for Exception for " + leg.getService().getLineName() + " at " + leg.getAlighting().getName() + ": " + new DecimalFormat("#.##").format(exceptionPropability) + "%!");
            }

            JSONObject prognosis = new JSONObject();
            prognosis.put("delayBoarding", delayBoarding);
            prognosis.put("delayAlighting", delayAlighting);
            prognosis.put("delayException", delayException);
            prognosis.put("exceptionPropability", exceptionPropability);

            for (PrognosisFactor f : askedFactors) {
                ArrayList<PrognosisCalculationItem> items = f.getResult().getItems();
                if (i < items.size()) {
                    PrognosisCalculationItem item = items.get(i);
                    switch (f.getType()) {
                        case QUESTIONNAIRE_CAPACITY:
                            prognosis.put("capacity", item.getDelayBoardingRegular());
                            if (console) {
                                logger.info("Capacity Ratio for " + leg.getService().getLineName() + " at " + leg.getAlighting().getName() + ": " + item.getDelayBoardingRegular() + "!");
                            }
                            break;
                        case QUESTIONNAIRE_CLEANNESS:
                            prognosis.put("cleanness", item.getDelayBoardingRegular());
                            if (console) {
                                logger.info("Cleanness Ratio for " + leg.getService().getLineName() + " at " + leg.getAlighting().getName() + ": " + item.getDelayBoardingRegular() + "!");
                            }
                            break;
                        case QUESTIONNAIRE_INTERCHANGE:
                            prognosis.put("successfullInterchange", item.getDelayBoardingRegular());
                            if (console) {
                                logger.info("Capacity Ratio for " + leg.getService().getLineName() + " at " + leg.getAlighting().getName() + ": " + item.getDelayBoardingRegular() + "!");
                            }
                            break;
                    }
                }
            }


            JSONObject outputitem = new JSONObject();
            outputitem.put("service", leg.getService().toJSON());
            outputitem.put("boarding", leg.getBoarding().toJSON());
            outputitem.put("alighting", leg.getAlighting().toJSON());
            outputitem.put("prognosis", prognosis);

            output.put(outputitem);
        }

        return output;
    }

    @Override
    public void onCalculationComplete(PrognosisFactor factor) {
        lock.lock();
        System.out.println("Singal fired");
        data.signal();
        lock.unlock();
    }

    private void printPrognosis(String out, HttpServletResponse response) throws IOException {
        response.getWriter().print(out);
        response.getWriter().close();
    }
}
