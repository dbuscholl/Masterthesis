package de.dbuscholl.fahrplanauskunft.gui.adapters;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import org.jdom2.JDOMException;

import java.io.IOException;
import java.util.ArrayList;

import de.dbuscholl.fahrplanauskunft.R;
import de.dbuscholl.fahrplanauskunft.common.Constants;
import de.dbuscholl.fahrplanauskunft.network.Client;
import de.dbuscholl.fahrplanauskunft.network.xml.LocationAutocompleteRequest;
import de.dbuscholl.fahrplanauskunft.network.entities.Station;

/**
 * The Adapter which is used to map Autocomplete Items from the Result of the Request to the Items themselves.
 */
public class AutoCompleteAdapter extends ArrayAdapter<String> implements Filterable {

    ArrayList<Station> stations;
    Context context;


    /**
     * Initializes the Autocomplete Adapter
     * @param context Application Context
     * @param textViewResourceId Resource-ID of textfield for which the Adapter should be used.
     */
    public AutoCompleteAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
        this.context = context;
        stations = new ArrayList<>();
    }

    /**
     *
     * @return the number of stations which will be displayed for autocompletion
     */
    @Override
    public int getCount() {
        return stations.size();
    }

    /**
     * Used by System when inflating
     * @param index items position inside autocompletion
     * @return the items value for a given index
     */
    @Override
    public String getItem(int index) {
        return stations.get(index).toString();
    }

    /**
     * this fires the actual requests. As soon as a char was entered in the textfield it downloads the station results
     * from the trias interface and inserts them here via getItem() Method.
     * @return
     */
    @Override
    public Filter getFilter() {

        Filter myFilter = new Filter() {

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                if (constraint != null) {
                    // A class that queries a web API, parses the data and returns an ArrayList<Style>
//
                    try {
                        stations = new DownloadStations().execute(new String[]{constraint.toString()}).get();
                    } catch (Exception e) {
//                        Log.e("myException", e.getMessage());
                    }
                    // Now assign the values and count to the FilterResults object
                    filterResults.values = stations;
                    filterResults.count = stations.size();
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence contraint, FilterResults results) {
                if (results != null && results.count > 0) {
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }

        };

        return myFilter;

    }

    /**
     *
     * @return ArrayList of Station objects which were found for autocompletion
     */
    public ArrayList<Station> getStations() {
        return stations;
    }

    /**
     * Async task to download the Station items from TRIAS-Interface. This is instanciated by the outer class when
     * the user enters a char inside the textfield.
     */
    private class DownloadStations extends AsyncTask<String, Void, ArrayList<Station>> {


        @Override
        protected ArrayList<Station> doInBackground(String... constraint) {
            ArrayList<Station> stations = new ArrayList<Station>();
            try {
                LocationAutocompleteRequest lar = new LocationAutocompleteRequest(context.getResources().openRawResource(R.raw.stop_autocomplete_request));
                lar.buildRequest(constraint[0]);
                Client client = new Client(Constants.URL_TRIASAPI);
                String response = client.sendPostXML(lar.toString());
                stations = Station.stationListFromTriasResult(response);
            } catch (IOException | JDOMException e) {
                return stations;
            }
            return stations;
        }

        @Override
        protected void onPostExecute(ArrayList<Station> result) {

        }

    }
}
