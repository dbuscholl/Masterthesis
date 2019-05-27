package de.dbuscholl.fahrplanauskunft;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * This class provides ability to format time string from one often used format to another often used format.
 */
public class FormatTools {

    /**
     * formats a date into a readable string repressentation for the date picker
     * @param year year
     * @param month month as number
     * @param day day as number of month
     * @return
     */
    public static String formatDate(int year, int month, int day) {
        return (day < 10 ? "0" + day : day) + "." + (month < 10 ? "0" + month : month) + "." + year;
    }

    /**
     * formats a date to readable string value from a given java calendar
     * @param calendar date which should be formatted
     * @return dd.MM.yyyy representation of the given format as string
     */
    public static String formatDate(Calendar calendar) {
        DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
        return dateFormat.format(calendar.getTime());
    }

    /**
     * formats a time to a readable string value from a given java celandar instance
     * @param calendar time which should be formatted
     * @return HH:mm representation of the given format as stirng.
     */
    public static String formatTime(Calendar calendar) {
        DateFormat timeFormat = new SimpleDateFormat("HH:mm");
        return timeFormat.format(calendar.getTime());
    }

    /**
     * formats a time to a readable string representation
     * @param hour hours
     * @param minute minutes
     * @return HH:mm format of the given parameters
     */
    public static String formatDate(int hour, int minute) {
        return (hour < 10 ? "0" + hour : hour) + ":" + (minute < 10 ? "0" + minute : minute);
    }

    /**
     * formats a Date Object to TRIAS UTC Time representation
     *
     * @param date Date Object to be formatted
     * @return a Date Object in TRIAS UTC Time representation
     */
    public static String formatTrias(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        timeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        String dateString = dateFormat.format(date);
        String timeString = timeFormat.format(date);

        return dateString + "T" + timeString + "Z";
    }

    /**
     * formats a given TRIAS UTC time representation into a Date object
     * @param time the TRIAS UTC time representation which should be formatted
     * @param sdf configure your custom DateFormatter here or leave it to use ours with yyyy-MM-dd HH:mm:ss
     * @return date object of trias time.
     */
    public static Date parseTrias(String time, DateFormat sdf) {
        try {
            if (sdf == null) {
                sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            }
            String[] ts1 = time.substring(0, time.length() - 1).split("T");
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            String toParse = ts1[0] + " " + ts1[1];
            if (toParse.equals("")) {
                int i = 0;
            }
            return sdf.parse(toParse);
        } catch (ParseException e) {
            int i = 0;
            return null;
        } catch (NumberFormatException e) {
            int i = 0;
            return null;
        }
    }

    /**
     * parses a given TRIAS UTC Timestring into a readable DateString in german Locale format dd.Mm.yyyy
     * @param date the TRIAS UTC Timestring which should be formattes
     * @return the string value dd.MM.yyyy which is used for the datepicker or the TripDetailsActivity in this app
     */
    public static String parseTriasDate(String date) {
        Date d = parseTrias(date, null);
        if (d == null) {
            return null;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY);
        return sdf.format(d);
    }

    /**
     * parses a TRIAC UTC Timestring into a readable time value as string in format HH:mm
     * @param time the TRIAS UTC Timestring which should be converted
     * @return a string time value with format HH:mm
     */
    public static String parseTriasTime(String time) {
        Date d = parseTrias(time, null);
        if (d == null) {
            return null;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.GERMANY);
        sdf.setTimeZone(TimeZone.getTimeZone("Europe/Berlin"));
        return sdf.format(d);
    }

    /**
     * calculates the timedifference in milliseconds between to given TRIAS UTC Timestrings
     * @param startTime the first time which should be calculated
     * @param endTime the second time which should be calculated
     * @return endTime minus startTime difference in milliseconds. This can therefore also be negative!
     */
    public static long getTriasDifference(String startTime, String endTime) {
        Date start = parseTrias(startTime, null);
        Date end = parseTrias(endTime, null);
        return end.getTime() - start.getTime();
    }
}
