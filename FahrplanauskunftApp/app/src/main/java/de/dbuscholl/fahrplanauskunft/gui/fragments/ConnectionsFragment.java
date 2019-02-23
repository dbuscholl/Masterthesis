package de.dbuscholl.fahrplanauskunft.gui.fragments;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import org.jdom2.JDOMException;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;

import de.dbuscholl.fahrplanauskunft.FormatTools;
import de.dbuscholl.fahrplanauskunft.R;
import de.dbuscholl.fahrplanauskunft.gui.adapters.AutoCompleteAdapter;
import de.dbuscholl.fahrplanauskunft.gui.adapters.ConnectionsListAdapter;
import de.dbuscholl.fahrplanauskunft.network.TripInfoDownloadTask;
import de.dbuscholl.fahrplanauskunft.network.entities.Station;
import de.dbuscholl.fahrplanauskunft.network.entities.Connection;
import de.dbuscholl.fahrplanauskunft.network.xml.TripInfoRequest;

public class ConnectionsFragment extends Fragment {

    private static final String[] STATIONS = {"Heumaden", "Heumaden Bockelstraße", "Heumaden Schule",
            "Heumaden Rose", "Schemppstraße", "Sillenbuch", "Silberwald", "Waldau", "Ruhbank (Fernsehturm)",
            "Isegrimweg"};
    private DatePickerDialog.OnDateSetListener dateSetListener;
    private TimePickerDialog.OnTimeSetListener timeSetListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.connections_fragment, null);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        final AutoCompleteTextView from = getView().findViewById(R.id.startPoint);
        final AutoCompleteTextView to = getView().findViewById(R.id.destinationPoint);

        final Button date = getView().findViewById(R.id.date);
        final Button time = getView().findViewById(R.id.time);

        Calendar cal = Calendar.getInstance();
        date.setText(FormatTools.formatDate(cal));
        time.setText(FormatTools.formatTime(cal));


        final AutoCompleteAdapter fromAdapter = new AutoCompleteAdapter(getActivity().getApplicationContext(), android.R.layout.simple_dropdown_item_1line);
        from.setAdapter(fromAdapter);
        final AutoCompleteAdapter toAdapter = new AutoCompleteAdapter(getActivity().getApplicationContext(), android.R.layout.simple_dropdown_item_1line);
        to.setAdapter(toAdapter);

        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH) + 1;
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dpd = new DatePickerDialog(getActivity(), android.R.style.Theme_DeviceDefault_Dialog_MinWidth, dateSetListener, year, month, day);
                dpd.show();
            }
        });
        dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                month = month + 1;
                date.setText(FormatTools.formatDate(year, month, day));
            }
        };

        time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar cal = Calendar.getInstance();
                int hour = cal.get(Calendar.HOUR_OF_DAY);
                int minute = cal.get(Calendar.MINUTE);
                TimePickerDialog tpd = new TimePickerDialog(getActivity(), android.R.style.Theme_DeviceDefault_Dialog_MinWidth, timeSetListener, hour, minute, true);
                tpd.show();
            }
        });
        timeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hour, int minute) {
                time.setText(FormatTools.formatDate(hour, minute));
            }
        };

        final TextView resultListTextView = getView().findViewById(R.id.resultListText);
        resultListTextView.setVisibility(View.INVISIBLE);
        final ListView resultListView = getView().findViewById(R.id.resultlistview);

        Button search = getView().findViewById(R.id.search_button);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fromRef = null;
                String toRef = null;
                String fromText = from.getText().toString();
                for (Station s : fromAdapter.getStations()) {
                    if (fromText.equals(s.toString())) {
                        fromRef = s.getRef();
                        Log.d(getClass().getName(), fromText + " - " + s.toString() + " - " + s.getRef());
                    }
                }
                String toText = to.getText().toString();
                for (Station s : toAdapter.getStations()) {
                    if (toText.equals(s.toString())) {
                        toRef = s.getRef();
                        Log.d(getClass().getName(), toText + " - " + s.toString() + " - " + s.getRef());
                    }
                }
                String depDate = date.getText().toString();
                String depTime = time.getText().toString();

                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
                    sdf.setTimeZone(TimeZone.getTimeZone("Europe/Berlin"));
                    Date guiDate = sdf.parse(depDate + " " + depTime);
                    String parse = FormatTools.formatTrias(guiDate);
                    Log.d(getClass().getName(), parse);
                    TripInfoRequest tir = new TripInfoRequest(getActivity().getApplicationContext().getResources().openRawResource(R.raw.trip_info_request));
                    tir.buildRequest(fromRef, toRef, parse);
                    final ArrayList<Connection> trips = new TripInfoDownloadTask().execute(tir.toString()).get();

                    resultListTextView.setVisibility(View.VISIBLE);
                    ConnectionsListAdapter cla = new ConnectionsListAdapter(trips, getContext());
                    resultListView.setAdapter(cla);
                    resultListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            Connection c = trips.get(position);
                            Toast.makeText(getActivity().getApplicationContext(), c.getLegs().get(0).getBoarding().getName(), Toast.LENGTH_LONG);
                        }
                    });

                } catch (JDOMException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
        });
    }

}
