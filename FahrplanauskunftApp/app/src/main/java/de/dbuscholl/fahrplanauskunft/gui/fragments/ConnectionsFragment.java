package de.dbuscholl.fahrplanauskunft.gui.fragments;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
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
import de.dbuscholl.fahrplanauskunft.gui.activities.ResultDetailActivity;
import de.dbuscholl.fahrplanauskunft.gui.adapters.AutoCompleteAdapter;
import de.dbuscholl.fahrplanauskunft.gui.adapters.ConnectionsListAdapter;
import de.dbuscholl.fahrplanauskunft.network.TripInfoDownloadTask;
import de.dbuscholl.fahrplanauskunft.network.entities.Station;
import de.dbuscholl.fahrplanauskunft.network.entities.Connection;
import de.dbuscholl.fahrplanauskunft.network.xml.TripInfoRequest;

public class ConnectionsFragment extends Fragment {
    private static ArrayList<Connection> currentResult = new ArrayList<>();
    private static String fromValue;
    private static String toValue;
    private static String dateValue;
    private static String timeValue;

    private static final String[] STATIONS = {"Heumaden", "Heumaden Bockelstraße", "Heumaden Schule",
            "Heumaden Rose", "Schemppstraße", "Sillenbuch", "Silberwald", "Waldau", "Ruhbank (Fernsehturm)",
            "Isegrimweg"};
    private DatePickerDialog.OnDateSetListener dateSetListener;
    private TimePickerDialog.OnTimeSetListener timeSetListener;

    private AutoCompleteTextView fromTextView;
    private AutoCompleteTextView toTextView;
    private Button dateButton;
    private Button timeButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.connections_fragment, null);
    }


    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        fromTextView = getView().findViewById(R.id.startPoint);
        toTextView = getView().findViewById(R.id.destinationPoint);

        dateButton = getView().findViewById(R.id.date);
        timeButton = getView().findViewById(R.id.time);

        Calendar cal = Calendar.getInstance();
        if (fromValue != null && toValue != null) {
            fromTextView.setText(fromValue);
            toTextView.setText(toValue);
            if (dateValue != null) {
                dateButton.setText(dateValue);
            } else {
                dateButton.setText(FormatTools.formatDate(cal));
            }
            if (timeValue != null) {
                timeButton.setText(timeValue);
            } else {
                timeButton.setText(FormatTools.formatTime(cal));
            }
        } else {
            dateButton.setText(FormatTools.formatDate(cal));
            timeButton.setText(FormatTools.formatTime(cal));
        }


        final AutoCompleteAdapter fromAdapter = new AutoCompleteAdapter(getActivity().getApplicationContext(), android.R.layout.simple_dropdown_item_1line);
        fromTextView.setAdapter(fromAdapter);
        final AutoCompleteAdapter toAdapter = new AutoCompleteAdapter(getActivity().getApplicationContext(), android.R.layout.simple_dropdown_item_1line);
        toTextView.setAdapter(toAdapter);

        dateButton.setOnClickListener(new View.OnClickListener() {
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
                dateButton.setText(FormatTools.formatDate(year, month, day));
            }
        };

        timeButton.setOnClickListener(new View.OnClickListener() {
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
                timeButton.setText(FormatTools.formatDate(hour, minute));
            }
        };

        final TextView resultListTextView = getView().findViewById(R.id.resultListText);
        resultListTextView.setVisibility(View.INVISIBLE);
        final ListView resultListView = getView().findViewById(R.id.resultlistview);

        if (currentResult.size() > 0) {
            ConnectionsListAdapter cla = new ConnectionsListAdapter(currentResult, getContext());
            resultListView.setAdapter(cla);
            resultListView.setOnItemClickListener(new ItemClickHandler());
            resultListTextView.setVisibility(View.VISIBLE);
        }

        Button search = getView().findViewById(R.id.search_button);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fromRef = null;
                String toRef = null;
                fromValue = fromTextView.getText().toString();
                for (Station s : fromAdapter.getStations()) {
                    if (fromValue.equals(s.toString())) {
                        fromRef = s.getRef();
                        Log.d(getClass().getName(), fromValue + " - " + s.toString() + " - " + s.getRef());
                    }
                }
                toValue = toTextView.getText().toString();
                for (Station s : toAdapter.getStations()) {
                    if (toValue.equals(s.toString())) {
                        toRef = s.getRef();
                        Log.d(getClass().getName(), toValue + " - " + s.toString() + " - " + s.getRef());
                    }
                }
                dateValue = dateButton.getText().toString();
                timeValue = timeButton.getText().toString();

                if (fromRef == null || toRef == null) {
                    Toast.makeText(getContext(), "Invalid refs. Please retype stations to get results!", Toast.LENGTH_LONG).show();
                    return;
                }

                try {
                    InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
                    sdf.setTimeZone(TimeZone.getTimeZone("Europe/Berlin"));
                    Date guiDate = sdf.parse(dateValue + " " + timeValue);
                    String parse = FormatTools.formatTrias(guiDate);
                    Log.d(getClass().getName(), parse);
                    TripInfoRequest tir = new TripInfoRequest(getActivity().getApplicationContext().getResources().openRawResource(R.raw.trip_info_request));
                    tir.buildRequest(fromRef, toRef, parse);
                    TripInfoDownloadTask.setOnSuccessEvent(new TripInfoDownloadTask.SuccessEvent() {
                        @Override
                        public void onSuccess(final ArrayList<Connection> result) {
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    currentResult = result;
                                    ConnectionsListAdapter cla = new ConnectionsListAdapter(currentResult, getContext());
                                    resultListView.setAdapter(cla);
                                    resultListView.setOnItemClickListener(new ItemClickHandler());
                                }
                            });
                        }
                    });
                    new TripInfoDownloadTask(getActivity()).execute(tir.toString());

                    resultListTextView.setVisibility(View.VISIBLE);
                } catch (JDOMException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static ArrayList<Connection> getCurrentResult() {
        return currentResult;
    }

    private class ItemClickHandler implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Connection c = currentResult.get(position);

            Intent intent = new Intent(getActivity(), ResultDetailActivity.class);
            intent.putExtra("position", position);
            startActivity(intent);
        }
    }

}
