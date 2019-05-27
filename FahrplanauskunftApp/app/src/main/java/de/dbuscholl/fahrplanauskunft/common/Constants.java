package de.dbuscholl.fahrplanauskunft.common;

import android.graphics.Color;

import org.jdom2.Namespace;

/**
 * This simple class contains lots of attributes which can be configured by developers to adjust the app to their whishes.
 * Although it is not completed to outsource all neccessary attributes you can still use a lot of them already.
 */
public class Constants {
    /*
    ----------------------------------------STRING VALUES-------------------------------------------
     */
    public static final String ERRORMSG_TRIAS_NOREFS = "Name and Ref of Start is null";
    public static final String ERRORMSG_GPS_NOPERMISSION = "Bitte Zugriff auf GPS erlauben!";
    public static final String ERRORMSG_NOT_ADDED_TO_SERVICE = "Fahrt konnte nicht zur Aufnahmeliste hinzugefügt werden.";
    public static final String ERRORMSG_NO_CONNECTION_DATA = "Keine Fahrtdaten vorhanden";
    public static final String ERRORMSG_STOP_REFS_LOST = "Haltestellen IDs sind verloren gegangen. Bitte neu eingeben!";


    public static final String MSG_SEARCHINGTRIPS = "Suche nach Fahrten...";
    public static final String MSG_SENDING_QUESTIONNAIRE_RESULTS = "Sende Fahrtendaten...";
    public static final String MSG_RECORD_TRIP_PROMPT = "Möchtest du diese Verbindung aufzeichnen?";
    public static final String MSG_TAKEN_TRIP_PROMPT = "Bist du mit dieser Verbindung gefahren?";
    public static final String MSG_GPS_ALLOW_PROMPT = "Bitte gewähre uns Zugriff auf deinen Standort, um die Fahrt aufzuzeichnen";


    /*
    ----------------------------------------CONFIGURATION-------------------------------------------
     */
    public static final int TRIP_QUESTIONNAIRE_MINDIFF = 60; // Abstand zur Ankunftszeit in die Zukunft bei der MSG_TAKEN_TRIP_PROMPT ausgelöst wrid
    public static final int TRIP_QUESTIONNAIRE_MAXDIFF = 0; // Abstand zur Ankunftszeit in die Vergangenheit bei der MSG_TAKEN_TRIP_PROMPT ausgelöst wrid
    public static final int TRIP_RECORIDNG_MINDIFF = 3; // Abstand zur Abfahrtszeit in die Zukunft bei der MSG_RECORD_TRIP_PROMPT ausgelöst wrid
    public static final int TRIP_RECORDING_MAXDIFF = 30; // Abstand zur Abfahrtszeit in die Vergangenheit bei der MSG_RECORD_TRIP_PROMPT ausgelöst wrid

    public static final int COLOR_DELAY_LATE = Color.rgb(244, 37, 30);
    public static final int COLOR_DELAY_INTIME = Color.rgb(244, 37, 30);

    public static final Namespace NAMESPACE = Namespace.getNamespace("http://www.vdv.de/trias");

    public static final String[] TRIAS_SUBPMODE_TYPES = {"RailSubmode", "CoachSubmode",
            "MetroSubmode", "BusSubmode", "TramSubmode", "WaterSubmode", "AirSubmode",
            "TelecabinSubmode", "FunicularSubmode", "TaxiSubmode"};

    public static final String URL_PROGNOSISCALCULATOR = "http://busbilder.net:8080/prognosiscalculator/calculate";
    public static final String URL_USERDATA_IMPORTER = "http://busbilder.net:8080/prognosiscalculator/import";
    public static final String URL_TRIASAPI = "http://efastatic.vvs.de/kleinanfrager/trias";

    // TODO: Insert attributes about time values e.g. tracking repetition time or timeouts
}
