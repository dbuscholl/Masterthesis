package Database;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class FormatTools {

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

    public static String makeDepartureTimeForTrias(String departureTime) throws ParseException {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("Europe/Berlin"));
        SimpleDateFormat d = new SimpleDateFormat("yyyy-MM-dd");
        Date stop = sdf.parse(d.format(cal.getTime()) + " " + departureTime);
        return formatTrias(stop);
    }
}
