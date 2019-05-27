package de.dbuscholl.fahrplanauskunft.gui.activities;

import android.app.Dialog;
import android.content.Context;
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
import de.dbuscholl.fahrplanauskunft.gui.services.TripRecordingService;
import de.dbuscholl.fahrplanauskunft.network.tasks.QuestionaireResultTask;
import de.dbuscholl.fahrplanauskunft.network.entities.Connection;
import de.dbuscholl.fahrplanauskunft.network.entities.CustomLocation;
import de.dbuscholl.fahrplanauskunft.network.entities.Service;
import de.dbuscholl.fahrplanauskunft.network.entities.Trip;

/**
 * <p>This is a more complex logic and not a real activity, but it shows some dialogs, thats why it is in the activity
 * package. The Questionnaire is a tool to ask the user about his trip experience. It contains the questions and the
 * connection for which the questions are asked.</p>
 * <p>Inside a for loop limited by the amount of trip legs the user
 * gets (currently) four questions.<ol><li>capacity</li><li>cleanliness</li><li>subjective delay</li><li>successful interchange</li></ol>
 * the last question is only given, when it is not the last leg and there are interchanges inside his trip. As soon as
 * a question is asked the programm extracts additional information like line number and builds the layout for the dialog.
 * </p>
 */
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

    private TripRecordingService.FinishedRecording finishedRecording;
    private boolean hasFinishedRecording = false;
    private SuccessfullySendHandler successfullySendHandler;

    /**
     * Constructor... nothing special here
     */
    public Questionnaire() {

    }

    /**
     * Constructor... nothing special except setting the attributes
     * @param context application context
     * @param connection Connection for which the questions are asked
     */
    public Questionnaire(Context context, Connection connection) {
        this.activity = context;
        this.connection = connection;
        answers = new ArrayList<>();
    }

    /**
     * the trigger which start the actual asking process
     * <p>we first check whether the last step was reached. If so, we then check whether the last trip leg has already
     * been reached, because then we send the results to the server! In all other cases we continue asking. We obtain
     * the next leg and check if it is an interchange which should be skipped. Then we ask the questions step by step.
     * Delays should only be asked when there were less than three legs / interchanges because a user cannot remember
     * delays for so many different trip legs. Also we skip interchange question if it is the last trip leg.</p>
     * <p>The process also synchronizes the connection trip leg index with the array index so thtt the server knows
     * for which leg a question was answered.</p>
     */
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

    /**
     * this function sends all collected answers to the server.
     */
    private void sendResults() {
        QuestionaireResultTask qrt = new QuestionaireResultTask(activity);
        qrt.setOnSuccessEvent(new QuestionaireResultTask.SuccessEvent() {
            @Override
            public void onSuccess(String result) {
                if(successfullySendHandler != null) {
                    successfullySendHandler.onSuccessfullySend(result);
                }
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

    /**
     * this function converts all data which should be send to the server into json. This also affects the location tracking
     * data which is send to the questionnaire.
     * @return the json object containing all data which should be send to the server
     */
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

    /**
     * Showing the Dialog with the Capacity-question. First extract parameters from the leg and then build the dialog
     * itself. The click listener for the next-button adds the clicked radio items value to the answers array.
     * @param t the trip (leg) for which the question is asked
     */
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

    /**
     * Showing the Dialog with the Cleanness-question. First extract parameters from the leg and then build the dialog
     * itself. The click listener for the next-button adds the clicked radio items value to the answers array.
     * @param t the trip (leg) for which the question is asked
     */
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

    /**
     * Showing the Dialog with the Capacity-question. First extract parameters from the leg and then build the dialog
     * itself. The click listener for the next-button adds value of the seekbar to the answers array
     * @param t the trip (leg) for which the question is asked
     */
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
            // showing different texts for grammatical correctness
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                String text = String.valueOf(progress) + " Minuten";
                if (progress >= 60) {
                    text = "60 Minuten oder mehr";
                }
                if (progress == 1) {
                    text = String.valueOf(progress) + " Minute";
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

    /**
     * Showing the Dialog with the Interchange-question. First check if last trip was reached because there is no interchange
     * then and after that extract parameters from the leg and then build the dialog
     * itself. The click listener for the next-button adds the clicked radio items value to the answers array.
     * @param leg the trip (leg) for which the question is asked
     */
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

    /**
     * addds an answer value to the answers array. Creates a new Array synchronized with the trip leg if neccessary.
     * @param value
     */
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

    /**
     * allows the questionnaire to add recording data to the sending data for the server
     * @param recordingData
     */
    public void setRecordingData(ArrayList<CustomLocation> recordingData) {
        this.recordingData = recordingData;
    }

    /**
     *
     * @param activity the application context
     */
    public void setContext(Context activity) {
        this.activity = activity;
    }

    /**
     *
     * @param connection Setter for connection for which the questionnaire asks the questions
     */
    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    /**
     * If the questionnaire should be started by a trip which was done recording, this method should be called so the
     * questionnaire sets the right parameters itself.
     * @param finishedRecording
     */
    public void setFinishedRecording(TripRecordingService.FinishedRecording finishedRecording) {
        if(finishedRecording == null) {
            return;
        }

        this.finishedRecording = finishedRecording;
        hasFinishedRecording = true;

        connection = finishedRecording.getConnection();
        recordingData = finishedRecording.getRecordingData();
    }

    /**
     * Sets a callback which is activated when the data has been send successfully to the server.
     * @param ssh handler
     */
    public void setSuccessfullySendHandler (SuccessfullySendHandler ssh) {
        this.successfullySendHandler = ssh;
    }

    /**
     * Cancel button which dissmisses the current open dialog and indicates cancellation
     */
    private class CancelButton implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            currentOpenDialog.dismiss();
            currentOpenDialog = null;
            cancel = true;
        }
    }

    /**
     * Handler for the next-button
     */
    private interface NextButtonClickHandler {
        void onNextButtonClick();
    }

    /**
     * Handler for the when the questionnaire has send its data successfully to the server
     */
    public interface SuccessfullySendHandler {
        void onSuccessfullySend(String result);
    }
}
