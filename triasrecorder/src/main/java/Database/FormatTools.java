package Database;

import org.apache.log4j.Logger;
import org.jdom2.Element;
import org.jdom2.Namespace;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class FormatTools {
    private static Logger log = Logger.getLogger(FormatTools.class);


    public static final DateFormat sqlDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    public static final DateFormat sqlDatetimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static final DateFormat deDateFormat = new SimpleDateFormat("dd.MM.yyyy");
    public static final DateFormat deDatetimeFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
    public static final DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

    public static String getColumnStringDayOfWeek(int dow) {
        switch (dow) {
            case Calendar.SUNDAY:
                return "sunday";
            case Calendar.MONDAY:
                return "monday";
            case Calendar.TUESDAY:
                return "tuesday";
            case Calendar.WEDNESDAY:
                return "wednesday";
            case Calendar.THURSDAY:
                return "thursday";
            case Calendar.FRIDAY:
                return "friday";
            case Calendar.SATURDAY:
                return "saturday";
            default:
                return null;
        }
    }

    public static String formatTrias(Date date) {
        sqlDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        timeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        String dateString = sqlDateFormat.format(date);
        String timeString = timeFormat.format(date);
        String utc = dateString + "T" + timeString + "Z";
        sqlDateFormat.setTimeZone(TimeZone.getTimeZone("Europe/Berlin"));
        timeFormat.setTimeZone(TimeZone.getTimeZone("Europe/Berlin"));
        return utc;
    }

    public static String makeTimeForTrias(String time) throws ParseException {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.SECOND,0);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("Europe/Berlin"));
        SimpleDateFormat d = new SimpleDateFormat("yyyy-MM-dd");
        Date stop = sdf.parse(d.format(cal.getTime()) + " " + time);
        return formatTrias(stop);
    }

    public static String makeTimeForGtfs(String time) throws ParseException {
        String[] ts1 = time.substring(0, time.length() - 1).split("T");
        sqlDatetimeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date d = sqlDatetimeFormat.parse(ts1[0] + " " + ts1[1]);
        timeFormat.setTimeZone(TimeZone.getTimeZone("Europe/Berlin"));
        return timeFormat.format(d);
    }

    public static ArrayList<TripStop> xmlToTripStop(List<Element> stopElements, Namespace namespace) throws NullPointerException, ParseException {
        ArrayList<TripStop> tsarray = new ArrayList<>();

        for (int i = 0; i < stopElements.size(); i++) {
            Element e = stopElements.get(i);
            TripStop ts = new TripStop();

            String stopId = e.getChild("StopPointRef", namespace).getTextNormalize();
            String stopName = e.getChild("StopPointName", namespace).getChild("Text", namespace).getTextNormalize();

            String arrivalTime = null;
            String arrivalTimeEstimated = null;
            try {
                arrivalTime = e.getChild("ServiceArrival", namespace).getChild("TimetabledTime", namespace).getTextNormalize();
                arrivalTimeEstimated = e.getChild("ServiceArrival", namespace).getChild("EstimatedTime", namespace).getTextNormalize();
            } catch (NullPointerException exception) {
            }
            String departureTime = null;
            String departureTimeEstimated = null;
            try {
                departureTime = e.getChild("ServiceDeparture", namespace).getChild("TimetabledTime", namespace).getTextNormalize();
                departureTimeEstimated = e.getChild("ServiceDeparture", namespace).getChild("EstimatedTime", namespace).getTextNormalize();
            } catch (NullPointerException exception) {
            }

            if (arrivalTime == null && departureTime == null) {
                log.error("Neither Arrival time nor departure time defined for " + stopName + " - " + stopId);
                throw new NullPointerException("Neither Arrival time nor departure time defined for \" + stopName + \" - \" + stopId");
            }

            if (arrivalTimeEstimated == null && departureTimeEstimated == null) {
                log.error("Neither Arrival time nor departure time defined for " + stopName + " - " + stopId);
                throw new NullPointerException("Neither Arrival time nor departure time defined for \" + stopName + \" - \" + stopId");
            }

            ts.setStop_id(stopId);
            ts.setStop_name(stopName);
            ts.setArrival_time(arrivalTime == null ? departureTime : arrivalTime);
            ts.setDeparture_time(departureTime == null ? arrivalTime : departureTime);
            ts.setArrival_time_estimated(arrivalTimeEstimated == null ? departureTimeEstimated : arrivalTimeEstimated);
            ts.setDeparture_time_estimated(departureTimeEstimated == null ? arrivalTimeEstimated : departureTimeEstimated);
            ts.setStop_sequence(i + 1);
            ts.setType(TripStop.Type.TRIAS);

            ts.setArrival_time(makeTimeForGtfs(ts.getArrival_time()));
            ts.setDeparture_time(makeTimeForGtfs(ts.getDeparture_time()));
            ts.setArrival_time_estimated(makeTimeForGtfs(ts.getArrival_time_estimated()));
            ts.setDeparture_time_estimated(makeTimeForGtfs(ts.getDeparture_time_estimated()));

            tsarray.add(ts);
        }
        return tsarray;
    }

    public static String timeFromAfterMidnightTime(String timeParameter) {
        int hour = Integer.parseInt((timeParameter).substring(0, 2)) % 24;
        String hourFormatted = hour < 10 ? "0" + hour : String.valueOf(hour);
        return hourFormatted + timeParameter.substring(2);
    }
}
