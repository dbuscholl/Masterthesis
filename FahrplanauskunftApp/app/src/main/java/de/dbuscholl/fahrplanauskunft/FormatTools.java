package de.dbuscholl.fahrplanauskunft;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
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
}
