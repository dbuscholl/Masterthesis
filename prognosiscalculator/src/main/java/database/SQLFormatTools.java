package database;

import org.apache.log4j.Logger;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * A class containing some usefull tools for formatting and parsing times and dates between different types (TRIAS, GTFS)
 */
public class SQLFormatTools {
    private static Logger log = Logger.getLogger(SQLFormatTools.class);

    public static final String datePattern = "yyyy-MM-dd";
    public static final String dateTimePattern = "yyyy-MM-dd HH:mm:ss";
    public static final String deDatePattern = "dd.MM.yyyy";
    public static final String deDatetimePattern = "dd.MM.yyyy HH:mm:ss";
    public static final String timePattern = "HH:mm:ss";

    /**
     * returns a String representation of the Calendar's Day which is provided as int
     * @param dow Calendars Day of Week field value
     * @return a String representation of the Calendar's Day of week
     */
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

    /**
     * formats a Date Object to TRIAS UTC Time representation
     * @param date Date Object to be formatted
     * @return a Date Object in TRIAS UTC Time representation
     */
    public static String formatTrias(Date date) {
        SimpleDateFormat datesdf = new SimpleDateFormat(datePattern);
        SimpleDateFormat timesdf = new SimpleDateFormat(timePattern);

        datesdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        timesdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String dateString = datesdf.format(date);
        String timeString = timesdf.format(date);
        String utc = dateString + "T" + timeString + "Z";
        datesdf.setTimeZone(TimeZone.getTimeZone("Europe/Berlin"));
        timesdf.setTimeZone(TimeZone.getTimeZone("Europe/Berlin"));
        return utc;
    }

    /**
     * formats a Time String to TRIAS UTC Time representation with the current date
     * @param time time String in <i>HH:mm:ss</i>
     * @return UTC Timestring for TRIAS interface
     * @throws ParseException
     */
    public static String makeTimeForTrias(String time) throws ParseException {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.SECOND, 0);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("Europe/Berlin"));
        SimpleDateFormat d = new SimpleDateFormat("yyyy-MM-dd");
        Date stop = sdf.parse(d.format(cal.getTime()) + " " + time);
        return formatTrias(stop);
    }

    /**
     * Formats a TRIAS Timestring to GTFS Timestring
     * @param time full UTC Timestamp provided by TRIAS <i>yyyy-MM-ddTHH:mm:ssZ</i>
     * @return GTFS Timestring <i>HH:mm:ss</i>
     * @throws ParseException
     */
    public static String makeTimeForGtfs(String time) {
        SimpleDateFormat timesdf = new SimpleDateFormat(timePattern);
        Date d = parseTriasTime(time);
        timesdf.setTimeZone(TimeZone.getTimeZone("Europe/Berlin"));
        return timesdf.format(d);
    }

    /**
     * Formats a TRIAS Timestring to GTFS Timestring
     * @param datetime full UTC Timestamp provided by TRIAS <i>yyyy-MM-ddTHH:mm:ssZ</i>
     * @return GTFS Timestring <i>HH:mm:ss</i>
     * @throws ParseException
     */
    public static String makeDatetimeForGtfs(String datetime) throws ParseException {
        SimpleDateFormat datetimesdf = new SimpleDateFormat(dateTimePattern);
        datetimesdf.setTimeZone(TimeZone.getTimeZone("Europe/Berlin"));
        return datetimesdf.format(parseTriasTime(datetime));
    }

    /**
     * Parses a Trias datetime String and returns it as Dateobject
     * @param time full UTC Timestamp provided by TRIAS <i>yyyy-MM-ddTHH:mm:ssZ</i>
     * @return Date as Date Objects
     * @throws ParseException
     */
    public static Date parseTriasTime(String time) {
        SimpleDateFormat datetimesdf = new SimpleDateFormat(dateTimePattern);

        try {
            String[] ts1 = time.substring(0, time.length() - 1).split("T");
            datetimesdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            String toParse = ts1[0] + " " + ts1[1];
            if (toParse.equals("")) {
                int i = 0;
            }
            Date parse = datetimesdf.parse(toParse);
            datetimesdf.setTimeZone(TimeZone.getTimeZone("Europe/Berlin"));
            return parse;
        } catch (ParseException e) {
            int i = 0;
            return null;
        }catch (NumberFormatException e) {
            int i = 0;
            return null;
        }
    }

    public static String makeDateForGtfs(String date) {
        SimpleDateFormat datesdf = new SimpleDateFormat(datePattern);
        Date parse = parseTriasTime(date);
        datesdf.setTimeZone(TimeZone.getTimeZone("Europe/Berlin"));
        return datesdf.format(parse);
    }
}
