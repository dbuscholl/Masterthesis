package de.dbuscholl.fahrplanauskunft.gui.activities;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import de.dbuscholl.fahrplanauskunft.R;
import de.dbuscholl.fahrplanauskunft.network.entities.Connection;
import de.dbuscholl.fahrplanauskunft.network.entities.Service;
import de.dbuscholl.fahrplanauskunft.network.entities.Trip;

public class Questionnaire {
    private final Connection connection;
    private final Context activity;
    private boolean cancel = false;
    private Dialog currentOpenDialog = null;
    private NextButtonClickHandler nextHandler;
    private int step = 0;
    private int leg = 0;
    private static String tag = Questionnaire.class.getName();

    public Questionnaire(Activity activity, Connection connection) {
        this.activity = activity;
        this.connection = connection;
    }

    public void startForPastConnection() {
        nextHandler = new NextButtonClickHandler() {
            @Override
            public void onNextButtonClick() {
                if (step >= 4) {
                    leg++;
                    step = 0;
                    if (leg >= connection.getLegs().size()) {
                        return;
                    }
                }

                Trip t = null;
                for (int i = leg; i < connection.getLegs().size(); i++, leg++) {
                    t = connection.getLegs().get(i);
                    if (t.getType() == Trip.TripType.TIMED) {
                        break;
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

    public void askCapacity(Trip t) {
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

        cancel.setOnClickListener(new CancelButton());
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                step++;
                dialog.dismiss();
                nextHandler.onNextButtonClick();
            }
        });
        currentOpenDialog = dialog;
        dialog.show();
    }

    public void askCleanness(Trip t) {
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

        cancel.setOnClickListener(new CancelButton());
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
        SeekBar seekBar = dialog.findViewById(R.id.dialog_question_delay_seekbar);
        final TextView seekBarText = dialog.findViewById(R.id.dialog_question_delay_text);
        Button cancel = dialog.findViewById(R.id.dialog_question_cancelbutton);
        Button next = dialog.findViewById(R.id.dialog_question_continuebutton);

        String text = "Wie viel <b>Verspätung</b> hatte das Fahrzeug <b>" + railName + " " + lineName + "</b>?";
        header.setText(Html.fromHtml(text));

        cancel.setOnClickListener(new CancelButton());
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
        for (int i = leg+1; i < connection.getLegs().size(); i++) {
            to = connection.getLegs().get(i);
            if (to.getType() == Trip.TripType.TIMED) {
                break;
            }
        }
        if(to == null) {
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

        cancel.setOnClickListener(new CancelButton());
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                step++;
                dialog.dismiss();
                nextHandler.onNextButtonClick();
            }
        });
        currentOpenDialog = dialog;
        dialog.show();
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
