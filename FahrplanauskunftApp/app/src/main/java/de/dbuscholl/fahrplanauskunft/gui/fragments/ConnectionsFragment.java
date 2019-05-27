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

import de.dbuscholl.fahrplanauskunft.FormatTools;
import de.dbuscholl.fahrplanauskunft.R;
import de.dbuscholl.fahrplanauskunft.common.Constants;
import de.dbuscholl.fahrplanauskunft.gui.activities.TripDetailActivity;
import de.dbuscholl.fahrplanauskunft.gui.adapters.AutoCompleteAdapter;
import de.dbuscholl.fahrplanauskunft.gui.adapters.ConnectionsListAdapter;
import de.dbuscholl.fahrplanauskunft.network.tasks.TripInfoDownloadTask;
import de.dbuscholl.fahrplanauskunft.network.entities.Station;
import de.dbuscholl.fahrplanauskunft.network.entities.Connection;
import de.dbuscholl.fahrplanauskunft.network.xml.TripInfoRequest;

/**
 * this fragment handles everything that has to do with the connections search at app start. It is instantiated as soon as
 * the BottomNavigationView triggers the corresponding event, because for example the user clicked the navigation button for
 * connection.
 * <p>At start it has nothing to visualize, but after the user enters something inside the stations textview the autocomplete
 * process starts and tries to guess stations according to the string which was already entered. This is done by the TRIAS-
 * service Ortsinformationen. It also allows the user to set the time and date by modern date and timepickers which are super
 * useful</p>
 * <p>When the user hits seach the TRIAS service Verbindungsauskunft is triggered and returns a bunch of connection which are
 * visualized by the ConnectionsListAdapter inside the corresponding listview. The user can hit one of  them and gets lots
 * of detailed information about it. This will start the TripDetailActivitym</p>
 */
public class ConnectionsFragment extends Fragment {
    private static ArrayList<Connection> currentResult = new ArrayList<>();
    private static String fromValue;
    private static String toValue;
    private static String dateValue;
    private static String timeValue;

    private DatePickerDialog.OnDateSetListener dateSetListener;
    private TimePickerDialog.OnTimeSetListener timeSetListener;

    private AutoCompleteTextView fromTextView;
    private AutoCompleteTextView toTextView;
    private Button dateButton;
    private Button timeButton;

    /**
     * This is called as soon as this fragment gets inflated. Method given by android
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_connection, null);
    }


    /**
     * <p>This is called as soon as all views inside the fragment were successfully created. Given by Android.</p>
     * <p>It sets all listeners and assigns all values to the corresponding views which are datefield and timefield
     * with their pickers, departure and destination textfield with autocomplete functionality and the search button
     * which fires the TripInfoDownloadTask and assigns its value by the ConnectionsListAdapter into the right listview
     * when the http-request was successful.</p>
     * @param view
     * @param savedInstanceState
     */
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
                int month = cal.get(Calendar.MONTH);
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

        final ListView resultListView = getView().findViewById(R.id.resultlistview);

        if (currentResult.size() > 0) {
            ConnectionsListAdapter cla = new ConnectionsListAdapter(currentResult, getContext());
            resultListView.setAdapter(cla);
            resultListView.setOnItemClickListener(new ItemClickHandler());
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
                    Toast.makeText(getContext(), Constants.ERRORMSG_STOP_REFS_LOST, Toast.LENGTH_LONG).show();
                    fromTextView.setText("");
                    toTextView.setText("");
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
                    TripInfoDownloadTask tidt = new TripInfoDownloadTask(getActivity());
                    tidt.setOnSuccessEvent(new TripInfoDownloadTask.SuccessEvent() {
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
                    tidt.execute(tir.toString());

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

    /**
     * returns the list of connections returned by the trias interface. Getter Method.
     * @return
     */
    public static ArrayList<Connection> getCurrentResult() {
        return currentResult;
    }

    /**
     * The click handler for a connection Item for the ConnectionsListView. When you click on a connection the new
     * activity TripDetailActivity will start.
     */
    private class ItemClickHandler implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Connection c = currentResult.get(position);

            Intent intent = new Intent(getActivity(), TripDetailActivity.class);
            intent.putExtra("position", position);
            startActivity(intent);
        }
    }

}
