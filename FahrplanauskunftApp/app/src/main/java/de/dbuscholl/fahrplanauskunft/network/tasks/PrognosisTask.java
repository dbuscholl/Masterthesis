package de.dbuscholl.fahrplanauskunft.network.tasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import de.dbuscholl.fahrplanauskunft.common.Constants;
import de.dbuscholl.fahrplanauskunft.network.Client;
import de.dbuscholl.fahrplanauskunft.network.entities.PrognosisCalculationItem;
import de.dbuscholl.fahrplanauskunft.network.entities.PrognosisCalculationResult;
import de.dbuscholl.fahrplanauskunft.network.entities.Service;

/**
 * This class is responsible for the request of the prognosis in the background from the backend server. As this might
 * take some time, we had to set some parameters for that.
 */
public class PrognosisTask extends AsyncTask<String, Void, String> {

    private SuccessEvent successEvent;
    private String response = null;
    private static String request = null;
    private Context context;
    private ArrayList<PrognosisCalculationResult> items;
    private String error;

    /**
     * "empty" constructor
     * @param context application context
     */
    public PrognosisTask(Context context) {
        this.context = context;
    }

    /**
     * parameterized constructor
     * @param event callback after successful prognosis calculation
     * @param context
     */
    public PrognosisTask(SuccessEvent event, Context context) {
        this.successEvent = event;
        this.context = context;
    }

    /**
     * returns the request which was send to the server as string.
     * @return the request which was send to the server as string.
     */
    public static String getRequest() {
        return request;
    }

    /**
     * things we can do before execution of the actual task. Unused here
     */
    @Override
    protected void onPreExecute() {
    }


    /**
     * The actual task execution. First we initalize the client with the prognosis url. Then the request is send.
     * As soon as response is arriving it gets parsed into the corresponding entity classes.
     * @param strings optional parameters which is the json string to send to server in this case.
     * @return nothing because there is a callback for that
     */
    @Override
    protected String doInBackground(String... strings) {
        try {
            Client c = new Client(Constants.URL_PROGNOSISCALCULATOR);
            c.setLongTimeout();
            request = strings[0];
            String response = c.sendPostJSON(strings[0]);

            ArrayList<PrognosisCalculationResult> items = new ArrayList<>();
            if(response.charAt(0) == '{') {
                JSONObject error = new JSONObject(response);
                if(error.has("Error Description")) {
                    this.error = error.getString("Error Description");
                }
            } else {
                JSONArray array = new JSONArray(response);
                for (int i = 0; i < array.length(); i++) {
                    JSONObject o = array.getJSONObject(i);
                    PrognosisCalculationItem p = new PrognosisCalculationItem(o.getJSONObject("prognosis"));
                    Service s = new Service(o.getJSONObject("service"));
                    items.add(new PrognosisCalculationResult(p, s));
                }
                this.items = items;
            }
            return null;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * This is executed on the main thread again which is important because GUI updates cant be done from different threads
     * than the main one. Calling the callback here.
     * @param response respones of the server. Not used here, we already parsed our entites in background.
     */
    @Override
    protected void onPostExecute(String response) {
        super.onPostExecute(response);
        if(error!= null) {
            Toast.makeText(context, error, Toast.LENGTH_LONG).show();
        }
        if (successEvent != null) {
            successEvent.onSuccess(this.items);
        }
    }

    /**
     * getter for the respones
     * @return the response from the server
     */
    public String getResponse() {
        return response;
    }


    /**
     * sets a callback for the end of the server request.
     * @param e function to be called as soon as the server has send the results.
     */
    public void setOnSuccessEvent(SuccessEvent e) {
        successEvent = e;
    }

    /**
     * Callback interface that can be implemented to have a callback as soon as prognosis calculation by the server is
     * finished and returned to the app.
     */
    public interface SuccessEvent {
        void onSuccess(ArrayList<PrognosisCalculationResult> items);
    }
}
