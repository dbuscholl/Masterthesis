package de.dbuscholl.fahrplanauskunft.network.tasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import java.io.IOException;

import de.dbuscholl.fahrplanauskunft.common.Constants;
import de.dbuscholl.fahrplanauskunft.network.Client;

/**
 * This is the Background task for sending the Results of the questionnaire to the server. It doesn't process the data
 * itself, so in this case this class is rather simple.
 */
public class QuestionaireResultTask extends AsyncTask<String, Void, String> {

    private SuccessEvent successEvent;
    private ProgressDialog dialog;
    private String response = null;
    private static String request = null;

    /**
     * empty constructor
     */
    public QuestionaireResultTask() {
    }

    /**
     * parameterized constructor. This should be used if you want to show a progress dialog indicating that the request
     * is being processed by the server.
     * @param context
     */
    public QuestionaireResultTask(Context context) {
        dialog = new ProgressDialog(context);
    }

    /**
     * getter
     * @return the request that has been send to the server as string.
     */
    public static String getRequest() {
        return request;
    }

    /**
     * things can be done before the actual execution in background works. This is executed on the main thread and used
     * to display the prograss dialog if it was set in this case.
     */
    @Override
    protected void onPreExecute() {
        if (dialog != null) {
            dialog.setMessage(Constants.MSG_SENDING_QUESTIONNAIRE_RESULTS);
            dialog.show();
        }
    }


    /**
     * actual task execution. A new connection to the backend server is created and the response is set after success.
     * @param strings optional parameters. In this case the request body is transfered with this parameter.
     * @return nothing because there is onPostExecute and a callback for that.
     */
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

    /**
     * This is executed on the main thread again, because GUI updates are only allowed on the main thread. This function
     * triggers the callback with the response of the server.
     * @param response
     */
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

    /**
     * getter
     * @return the response of the server as string
     */
    public String getResponse() {
        return response;
    }

    /**
     * sets a callback which is called as soon as the server returned the response
     * @param e callback which should be called after execution
     */
    public void setOnSuccessEvent(SuccessEvent e) {
        successEvent = e;
    }

    /**
     * Callback interface. You can set a function here which will be executed after the server returned it's response.
     * This is executed on the main thread!
     */
    public interface SuccessEvent {
        void onSuccess(String result);
    }
}
