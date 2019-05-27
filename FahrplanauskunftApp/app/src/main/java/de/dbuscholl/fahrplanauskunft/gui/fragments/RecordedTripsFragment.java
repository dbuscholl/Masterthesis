package de.dbuscholl.fahrplanauskunft.gui.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import de.dbuscholl.fahrplanauskunft.R;
import de.dbuscholl.fahrplanauskunft.gui.adapters.RecordingListAdapter;
import de.dbuscholl.fahrplanauskunft.gui.services.TripRecordingService.FinishedRecording;

/**
 * <p>This fragment visualizes all the stuff which has to do with recorded trips. It builds the list from the local
 * storage where the recorded trips containing all location data and trip details are stored. This is proven as usefull,
 * because the service has also access to this localstorage even if the actual app was destroyed during recording.
 * Storing the recorded trips is not trivial because the local storage only takes simple datatypes. As parcelables
 * can be more complicated GSON was used there which is a simple JSON serialization framework that has proven to be very
 * useful in thi scase</p>
 */
public class RecordedTripsFragment extends Fragment {

    List<FinishedRecording> finishedRecordings;
    ListView recordingListView;

    /**
     * Callback before the view is inflated. Given by Android
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recordedtrips, null);
    }

    /**
     * Callback given by Android where all assigning takes place. This is called after the layout was successfully inflated.
     * <p>At first it grabs all finished recordings from the local storage and creates an own adapter which assigns the right
     * views with the right values to it. These are being assigned to the listview.</p>
     * @param view
     * @param savedInstanceState
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        BottomNavigationView bottomNavigationView = getActivity().findViewById(R.id.navigation);
        bottomNavigationView.getMenu().getItem(1).setChecked(true);

        finishedRecordings = null;

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        String json = sharedPref.getString("recordings", null);

        recordingListView = getView().findViewById(R.id.recording_listview);

        if (json != null) {
            finishedRecordings = new Gson().fromJson(json, new TypeToken<List<FinishedRecording>>() {
            }.getType());
        }

        if (finishedRecordings != null) {
            buildAdapter();
        }
        super.onViewCreated(view, savedInstanceState);
    }

    /**
     * This function is called to create the actual adapter. This is not as trivial because the local storage only takes
     * simple datatypes. As parcelables can be more complicated GSON was used there which is a simple JSON serialization
     * framework that has proven to be very useful in thi scase.
     * <p>This functions main use is to implement the listener for a solved questionnaire to remove a solved and send
     * questionnaire result from the finished recordings list as they dont need any further processing. After that the
     * adapter is being reinstanciated with the newly updated list.</p>
     */
    public void buildAdapter() {
        RecordingListAdapter rla = new RecordingListAdapter(finishedRecordings, getContext());
        rla.setQuestionnaireSolvedEvent(new RecordingListAdapter.QuestionnaireSolvedEvent() {
            @Override
            public void onQuestionnaireSolved(FinishedRecording f, String response) {
                finishedRecordings.remove(f);

                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
                SharedPreferences.Editor editor = sharedPref.edit();

                String recordings = new Gson().toJson(finishedRecordings);
                editor.putString("recordings", recordings).apply();

                buildAdapter();

                JSONObject json = null;
                double time = -1;
                try {
                    json = new JSONObject(response);
                    if (json.has("success")) {
                        time = json.getDouble("execution time");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }


                new AlertDialog.Builder(getContext())
                        .setTitle("Antwort angekommen!")
                        .setMessage(time >= 0 ? "Die Daten wurden in " + String.valueOf(time) + "s vom Server barbeitet." : "... aber es gab einen Fehler in der Bearbeitung")
                        .setPositiveButton("Okay",null)
                        .show();
            }
        });

        recordingListView.setAdapter(rla);
    }
}