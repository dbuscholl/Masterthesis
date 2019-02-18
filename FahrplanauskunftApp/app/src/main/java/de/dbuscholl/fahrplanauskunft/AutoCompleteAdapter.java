package de.dbuscholl.fahrplanauskunft;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import java.io.IOException;
import java.util.ArrayList;

import de.dbuscholl.fahrplanauskunft.Network.Client;

public class AutoCompleteAdapter extends ArrayAdapter<String> implements Filterable {

    ArrayList<String> stations;

    public AutoCompleteAdapter(Context context, int textViewResourceId){
        super(context, textViewResourceId);
        stations = new ArrayList<String>();
    }

    @Override
    public int getCount() {
        return stations.size();
    }

    @Override
    public String getItem(int index){
        return stations.get(index);
    }

    @Override
    public Filter getFilter(){

        Filter myFilter = new Filter(){

            @Override
            protected FilterResults performFiltering(CharSequence constraint){
                FilterResults filterResults = new FilterResults();
                if(constraint != null) {
                    // A class that queries a web API, parses the data and returns an ArrayList<Style>
//
                    try {

                        stations = new DownloadShippers().execute(new String[]{constraint.toString()}).get();
                    }
                    catch(Exception e) {
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
                if(results != null && results.count > 0) {
                    notifyDataSetChanged();
                }
                else {
                    notifyDataSetInvalidated();
                }
            }

        };

        return myFilter;

    }

    private class DownloadShippers extends AsyncTask<String, Void, ArrayList<String>> {

        @Override
        protected ArrayList<String> doInBackground(String... constraint) {
            ArrayList<String> stationNames = new ArrayList<String>();
            try {
                Client client = new Client("");
            } catch (IOException e) {
                return stationNames;
            }



            return stationNames;
        }

        @Override
        protected void onPostExecute(ArrayList<String> result) {

        }

    }
}
