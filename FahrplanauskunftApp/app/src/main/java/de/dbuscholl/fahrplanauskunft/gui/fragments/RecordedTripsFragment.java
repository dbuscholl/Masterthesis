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

public class RecordedTripsFragment extends Fragment {

    List<FinishedRecording> finishedRecordings;
    ListView recordingListView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recordedtrips, null);
    }

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