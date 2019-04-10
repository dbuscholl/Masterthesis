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

public class PrognosisTask extends AsyncTask<String, Void, String> {

    private SuccessEvent successEvent;
    private String response = null;
    private static String request = null;
    private Context context;
    private ArrayList<PrognosisCalculationResult> items;
    private String error;

    public PrognosisTask(Context context) {
        this.context = context;
    }

    public PrognosisTask(SuccessEvent event, Context context) {
        this.successEvent = event;
        this.context = context;
    }

    public static String getRequest() {
        return request;
    }

    @Override
    protected void onPreExecute() {
    }


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

    public String getResponse() {
        return response;
    }


    public void setOnSuccessEvent(SuccessEvent e) {
        successEvent = e;
    }

    public interface SuccessEvent {
        void onSuccess(ArrayList<PrognosisCalculationResult> items);
    }
}
