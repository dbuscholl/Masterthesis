package de.dbuscholl.fahrplanauskunft;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;

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
        AutoCompleteTextView from = getView().findViewById(R.id.startPoint);
        AutoCompleteTextView to = getView().findViewById(R.id.destinationPoint);

        final Button date = getView().findViewById(R.id.date);
        final Button time = getView().findViewById(R.id.time);

        Calendar cal = Calendar.getInstance();
        date.setText(FormatTools.formatDate(cal));
        time.setText(FormatTools.formatTime(cal));


        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity().getApplicationContext(), android.R.layout.simple_list_item_1, STATIONS);
        from.setAdapter(adapter);
        to.setAdapter(adapter);

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

    }
}
