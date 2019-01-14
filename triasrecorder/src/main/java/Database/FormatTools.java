package Database;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

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
}
