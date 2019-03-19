package de.dbuscholl.fahrplanauskunft.gui.activities;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.location.Location;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import de.dbuscholl.fahrplanauskunft.R;
import de.dbuscholl.fahrplanauskunft.network.QuestionaireResultTask;
import de.dbuscholl.fahrplanauskunft.network.entities.Connection;
import de.dbuscholl.fahrplanauskunft.network.entities.CustomLocation;
import de.dbuscholl.fahrplanauskunft.network.entities.Service;
import de.dbuscholl.fahrplanauskunft.network.entities.Trip;

public class Questionnaire {
    private Connection connection;
    private Context activity;
    private boolean cancel = false;
    private Dialog currentOpenDialog = null;
    private NextButtonClickHandler nextHandler;
    private int step = 0;
    private int leg = 0;

    private ArrayList<CustomLocation> recordingData;
    private ArrayList<ArrayList<String>> answers;

    public Questionnaire() {

    }

    public Questionnaire(Context context, Connection connection) {
        this.activity = context;
        this.connection = connection;
        answers = new ArrayList<>();
    }

    public void startForPastConnection() {
        nextHandler = new NextButtonClickHandler() {
            @Override
            public void onNextButtonClick() {
                if (step >= 4) {
                    leg++;
                    step = 0;
                    if (leg >= connection.getLegs().size()) {
                        sendResults();
                        return;
                    }
                }

                Trip t = null;
                for (int i = leg; i < connection.getLegs().size(); i++, leg++) {
                    t = connection.getLegs().get(i);
                    if (t.getType() == Trip.TripType.TIMED) {
                        break;
                    } else {
                        answers.add(new ArrayList<String>()); // to make leg value match answers index add empty
                        continue;
                    }
                }

                if (t == null) {
                    return;
                }

                switch (step) {
                    case 0:
                        askCapacity(t);
                        break;
                    case 1:
                        askCleanness(t);
                        break;
                    case 2:
                        if (connection.getLegs().size() < 3) {
                            askDelay(t);
                        } else {
                            step++;
                            onNextButtonClick();
                        }
                        break;
                    case 3:
                        if (leg == connection.getLegs().size() - 1) {
                            step++;
                            onNextButtonClick();
                        } else {
                            askInterchange(leg);
                        }
                        break;
                }
            }
        };
        nextHandler.onNextButtonClick();
    }

    private void sendResults() {
        QuestionaireResultTask qrt = new QuestionaireResultTask(activity);
        qrt.setOnSuccessEvent(new QuestionaireResultTask.SuccessEvent() {
            @Override
            public void onSuccess(String result) {
                Toast.makeText(activity,"Done sending!",Toast.LENGTH_LONG).show();
            }
        });

        JSONObject results = resultsToJSON();
        if(results==null) {
            return;
        }
        Log.d(this.getClass().getName(),results.toString());
        qrt.execute(results.toString());
    }

    public JSONObject resultsToJSON() {
        JSONObject sendingData = new JSONObject();
        try {
            JSONObject c = connection.toJSON();
            sendingData.put("connection", c==null?"":c);

            if(recordingData != null) {
                JSONArray locations = new JSONArray();
                for (CustomLocation l : recordingData) {
                    JSONObject location = new JSONObject();
                    location.put("time", l.getTime());
                    location.put("latitude", l.getLatitude());
                    location.put("longitude", l.getLongitude());
                    location.put("altitude", l.getAltitude());
                    location.put("accuracy", l.getAccuracy());
                    locations.put(location);
                }
                sendingData.put("recordingData", locations);
            } else {
                sendingData.put("recordingData", "");
            }

            JSONArray answersJson = new JSONArray();
            for(ArrayList<String> leg : answers) {
                JSONObject answer = new JSONObject();
                for (int i = 0; i < leg.size(); i++) {
                    String s = leg.get(i);

                    switch (i) {
                        case 0:
                            answer.put("capacity",s);
                            break;
                        case 1:
                            answer.put("cleanness",s);
                            break;
                        case 2:
                            answer.put("delay",s);
                            break;
                        case 3:
                            answer.put("interchangeToNextTrip",s);
                            break;
                    }
                }
                answersJson.put(answer);
            }
            sendingData.put("answers",answersJson);
            return sendingData;

        } catch (JSONException e) {
            return null;
        }
    }

    private void askCapacity(Trip t) {
        Service service = t.getService();
        String railName = service.getRailName();
        String lineName = service.getLineName();
        String destination = service.getDesitnation();

        final Dialog dialog = new Dialog(activity);
        dialog.setContentView(R.layout.dialog_question_capacity);
        dialog.setTitle("Kapazität " + railName + " " + lineName);

        // set the custom dialog components - text, image and button
        TextView header = (TextView) dialog.findViewById(R.id.dialog_question_capacity_text);
        Button cancel = (Button) dialog.findViewById(R.id.dialog_question_cancelbutton);
        Button next = (Button) dialog.findViewById(R.id.dialog_question_continuebutton);

        String text = "Wie <b>voll</b> war das Fahrzeug <b>" + railName + " " + lineName + "</b>?";
        header.setText(Html.fromHtml(text));

        final RadioGroup radio = dialog.findViewById(R.id.radio_capacity_group);

        cancel.setOnClickListener(new CancelButton());
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    int selectedId = radio.getCheckedRadioButtonId();
                    RadioButton radioButton = dialog.findViewById(selectedId);
                    String answer = radioButton.getText().toString();
                    addAnswer(answer);
                } catch (NullPointerException e) {
                    addAnswer("");
                }

                step++;
                dialog.dismiss();
                nextHandler.onNextButtonClick();
            }
        });
        currentOpenDialog = dialog;
        dialog.show();
    }

    private void askCleanness(Trip t) {
        Service service = t.getService();
        String railName = service.getRailName();
        String lineName = service.getLineName();
        String destination = service.getDesitnation();

        final Dialog dialog = new Dialog(activity);
        dialog.setContentView(R.layout.dialog_question_cleanness);
        dialog.setTitle("Sauberkeit " + railName + " " + lineName);

        // set the custom dialog components - text, image and button
        TextView header = dialog.findViewById(R.id.dialog_question_cleanness_text);
        Button cancel = dialog.findViewById(R.id.dialog_question_cancelbutton);
        Button next = dialog.findViewById(R.id.dialog_question_continuebutton);

        String text = "Wie <b>sauber</b> war das Fahrzeug <b>" + railName + " " + lineName + "</b>?";
        header.setText(Html.fromHtml(text));

        final RadioGroup radio = dialog.findViewById(R.id.radio_cleanness_group);

        cancel.setOnClickListener(new CancelButton());
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    int selectedId = radio.getCheckedRadioButtonId();
                    RadioButton radioButton = dialog.findViewById(selectedId);
                    String answer = radioButton.getText().toString();
                    addAnswer(answer);
                } catch (NullPointerException e) {
                    addAnswer("");
                }

                step++;
                dialog.dismiss();
                nextHandler.onNextButtonClick();
            }
        });
        currentOpenDialog = dialog;
        dialog.show();
    }

    private void askDelay(Trip t) {
        Service service = t.getService();
        String railName = service.getRailName();
        String lineName = service.getLineName();
        String destination = service.getDesitnation();

        final Dialog dialog = new Dialog(activity);
        dialog.setContentView(R.layout.dialog_question_delay);
        dialog.setTitle("Verspätung " + railName + " " + lineName);

        // set the custom dialog components - text, image and button
        TextView header = dialog.findViewById(R.id.dialog_question_delay_header);
        final SeekBar seekBar = dialog.findViewById(R.id.dialog_question_delay_seekbar);
        final TextView seekBarText = dialog.findViewById(R.id.dialog_question_delay_text);
        Button cancel = dialog.findViewById(R.id.dialog_question_cancelbutton);
        Button next = dialog.findViewById(R.id.dialog_question_continuebutton);

        String text = "Wie viel <b>Verspätung</b> hatte das Fahrzeug <b>" + railName + " " + lineName + "</b>?";
        header.setText(Html.fromHtml(text));

        cancel.setOnClickListener(new CancelButton());
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String answer = String.valueOf(seekBar.getProgress()) + " Minuten";
                    addAnswer(answer);
                } catch (NullPointerException e) {
                    addAnswer("");
                }

                step++;
                dialog.dismiss();
                nextHandler.onNextButtonClick();
            }
        });
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                String text = String.valueOf(progress) + " Minuten";
                if (progress >= 60) {
                    text = "60 Minuten oder mehr";
                }
                seekBarText.setText(text);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        currentOpenDialog = dialog;
        dialog.show();
    }

    private void askInterchange(int leg) {
        if (leg >= connection.getLegs().size() - 1) {
            step++;
            nextHandler.onNextButtonClick();
        }
        Trip from = connection.getLegs().get(leg);
        Trip to = null;
        for (int i = leg + 1; i < connection.getLegs().size(); i++) {
            to = connection.getLegs().get(i);
            if (to.getType() == Trip.TripType.TIMED) {
                break;
            }
        }
        if (to == null) {
            step++;
            nextHandler.onNextButtonClick();
            return;
        }

        String header = "Wurde der <b>Umstieg</b> von <b>" + from.getService().getRailName() + " " +
                from.getService().getLineName() + "</b> auf <b>" + to.getService().getRailName() + " " +
                to.getService().getLineName() + "</b> erreicht?";

        String title = "Umstieg zur Linie " + to.getService().getLineName();


        final Dialog dialog = new Dialog(activity);
        dialog.setContentView(R.layout.dialog_question_interchange);
        dialog.setTitle(title);

        // set the custom dialog components - text, image and button
        TextView headerview = dialog.findViewById(R.id.dialog_question_interchange_text);
        Button cancel = dialog.findViewById(R.id.dialog_question_cancelbutton);
        Button next = dialog.findViewById(R.id.dialog_question_continuebutton);

        headerview.setText(Html.fromHtml(header));

        final RadioGroup radio = dialog.findViewById(R.id.radio_interchange_group);

        cancel.setOnClickListener(new CancelButton());
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    int selectedId = radio.getCheckedRadioButtonId();
                    RadioButton radioButton = dialog.findViewById(selectedId);
                    String answer = radioButton.getText().toString();
                    addAnswer(answer.equals("Ja") ? "1" : "0");
                } catch (NullPointerException e) {
                    addAnswer("");
                }

                step++;
                dialog.dismiss();
                nextHandler.onNextButtonClick();
            }
        });
        currentOpenDialog = dialog;
        dialog.show();
    }

    private void addAnswer(String value) {
        try {
            if (answers.size() <= leg) {
                int length = leg - (answers.size() - 1);
                for (int i = 0; i < length; i++) {
                    answers.add(new ArrayList<String>());
                }
            }

            ArrayList<String> trip = answers.get(leg);
            if (trip.size() < step) {
                int length = step - (trip.size() - 1);
                for (int i = 0; i < length - 1; i++) {
                    trip.add("");
                }
            }

            trip.add(value);
        } catch (NullPointerException | IndexOutOfBoundsException ignored) {
        }
    }

    public void setRecordingData(ArrayList<CustomLocation> recordingData) {
        this.recordingData = recordingData;
    }

    public void setContext(Context activity) {
        this.activity = activity;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    private class CancelButton implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            currentOpenDialog.dismiss();
            currentOpenDialog = null;
            cancel = true;
        }
    }

    private interface NextButtonClickHandler {
        void onNextButtonClick();
    }
}
