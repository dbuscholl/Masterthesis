package de.dbuscholl.fahrplanauskunft.network.tasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import java.io.IOException;

import de.dbuscholl.fahrplanauskunft.common.Constants;
import de.dbuscholl.fahrplanauskunft.network.Client;

public class QuestionaireResultTask extends AsyncTask<String, Void, String> {

    private SuccessEvent successEvent;
    private ProgressDialog dialog;
    private String response = null;
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
            dialog.setMessage(Constants.MSG_SENDING_QUESTIONNAIRE_RESULTS);
            dialog.show();
        }
    }


    @Override
    protected String doInBackground(String... strings) {
        try {
            Client c = new Client(Constants.URL_USERDATA_IMPORTER);
            request = strings[0];
            String response = c.sendPostJSON(strings[0]);
            this.response = response;
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
        if (successEvent != null) {
            successEvent.onSuccess(this.response);
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
