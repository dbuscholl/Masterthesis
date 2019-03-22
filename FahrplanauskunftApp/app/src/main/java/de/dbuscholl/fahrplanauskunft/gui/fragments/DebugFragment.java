package de.dbuscholl.fahrplanauskunft.gui.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;

import de.dbuscholl.fahrplanauskunft.R;
import de.dbuscholl.fahrplanauskunft.gui.services.TripRecordingService.FinishedRecording;

public class DebugFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_debug, null);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        BottomNavigationView bottomNavigationView = (BottomNavigationView) getActivity().findViewById(R.id.navigation);
        bottomNavigationView.getMenu().getItem(1).setChecked(true);

        List<FinishedRecording> finishedRecordings = null;

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        String json = sharedPref.getString("recordings", null);

        if (json != null) {
            finishedRecordings = new Gson().fromJson(json, new TypeToken<List<FinishedRecording>>() {
            }.getType());
        }

        if (finishedRecordings != null) {
            Toast.makeText(getContext(), String.valueOf(finishedRecordings.size()), Toast.LENGTH_SHORT).show();
        }
        super.onViewCreated(view, savedInstanceState);
    }
}