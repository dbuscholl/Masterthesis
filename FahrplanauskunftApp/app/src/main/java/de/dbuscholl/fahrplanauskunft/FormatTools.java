package de.dbuscholl.fahrplanauskunft;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class FormatTools {

    public static String formatDate(int year, int month, int day) {
        return (day < 10 ? "0" + day : day) + "." + (month < 10 ? "0" + month : month) + "." + year;
    }

    public static String formatDate(Calendar calendar) {
        DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
        return dateFormat.format(calendar.getTime());
    }

    public static String formatTime(Calendar calendar) {
        DateFormat timeFormat = new SimpleDateFormat("HH:mm");
        return timeFormat.format(calendar.getTime());
    }

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

    private static Date parseTrias(String time, DateFormat sdf) {
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

    public static String parseTriasDate(String date) {
        Date d = parseTrias(date, null);
        if (d == null) {
            return null;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.GERMANY);
        return sdf.format(d);
    }

    public static String parseTriasTime(String time) {
        Date d = parseTrias(time, null);
        if (d == null) {
            return null;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.GERMANY);
        return sdf.format(d);
    }

    public static long getTriasDifference(String startTime, String endTime) {
        Date start = parseTrias(startTime, null);
        Date end = parseTrias(endTime, null);
        return end.getTime() - start.getTime();
    }
}
