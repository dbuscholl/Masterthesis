package de.dbuscholl.fahrplanauskunft.network;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.filter.ElementFilter;

import java.io.IOException;
import java.util.ArrayList;

import de.dbuscholl.fahrplanauskunft.common.Constants;
import de.dbuscholl.fahrplanauskunft.network.entities.Connection;
import de.dbuscholl.fahrplanauskunft.network.entities.Service;
import de.dbuscholl.fahrplanauskunft.network.entities.StopPoint;
import de.dbuscholl.fahrplanauskunft.network.entities.Trip;
import de.dbuscholl.fahrplanauskunft.network.xml.XMLDocument;

public class QuestionaireResultTask extends AsyncTask<String, Void, String> {

    private SuccessEvent successEvent;
    private ProgressDialog dialog;
    private static String response = null;
    private static String request = null;

    public QuestionaireResultTask() {
    }

    public QuestionaireResultTask(Context context) {
        dialog = new ProgressDialog(context);
    }

    public static String getRequest() {
        return request;
    }

    @Override
    protected void onPreExecute() {
        if (dialog != null) {
            dialog.setMessage("Suche nach Fahrten...");
            dialog.show();
        }
    }


    @Override
    protected String doInBackground(String... strings) {
        try {
            Client c = new Client("http://efastatic.vvs.de/kleinanfrager/trias");
            request = strings[0];
            String response = c.sendPostJSON(strings[0]);
            QuestionaireResultTask.response = response;

            if (successEvent != null) {
                successEvent.onSuccess(response);
            }
            return null;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(String response) {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
        super.onPostExecute(response);
    }

    public String getResponse() {
        return response;
    }


    public void setOnSuccessEvent(SuccessEvent e) {
        successEvent = e;
    }

    public interface SuccessEvent {
        void onSuccess(String result);
    }
}
