package de.dbuscholl.fahrplanauskunft.gui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import de.dbuscholl.fahrplanauskunft.FormatTools;
import de.dbuscholl.fahrplanauskunft.R;
import de.dbuscholl.fahrplanauskunft.common.Constants;
import de.dbuscholl.fahrplanauskunft.gui.activities.Questionnaire;
import de.dbuscholl.fahrplanauskunft.gui.services.TripRecordingService.FinishedRecording;
import de.dbuscholl.fahrplanauskunft.network.entities.Connection;

/**
 * this class visualizes all recorded trips inside the corresponding listview. It contians information about the takeoff
 * station name and time and the destination station name and time. It also displays a button with which the questionnaire
 * can be started
 */
public class RecordingListAdapter extends ArrayAdapter<FinishedRecording> {

    private List<FinishedRecording> data;
    private Context context;
    private int lastPosition = -1;
    private QuestionnaireSolvedEvent qse;

    /**
     * Constructor...
     *
     * @param data    give me all your finished recordings
     * @param context application context
     */
    public RecordingListAdapter(List<FinishedRecording> data, Context context) {
        super(context, R.layout.listitem_recording, data);
        this.context = context;
        this.data = data;
    }

    /**
     *
     * @return number of items which are finished recordings
     */
    @Override
    public int getCount() {
        return data.size();
    }

    /**
     * returns a view which is displayed as part of the listview
     * @param position position inside the array which should be visualized
     * @param convertView the chache thing
     * @param parent the listview in which it should be inserted
     * @return a view containing all visualising innerviews.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (position > data.size()) {
            return null;
        }
        // Get the data item for this position
        final FinishedRecording f = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag

        final View result;

        if (convertView == null) {

            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.listitem_recording, parent, false);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        // get items
        viewHolder.boardingStopTextView = convertView.findViewById(R.id.recording_boarding);
        viewHolder.boardingTimeTextView = convertView.findViewById(R.id.recording_boarding_time);
        viewHolder.alightingStopTextView = convertView.findViewById(R.id.recording_alighting);
        viewHolder.alightingTimeTextView = convertView.findViewById(R.id.recording_alighting_time);
        viewHolder.questionnaireImageButton = convertView.findViewById(R.id.recording_questionnaire_button);

        lastPosition = position;
        if (f != null) {
            final Connection c = f.getConnection();
            if (c != null) {
                // get text values
                String departureTime = c.getLegs().get(0).getBoarding().getDepartureTime();
                String arrivalTime = c.getLegs().get(c.getLegs().size() - 1).getAlighting().getArrivalTime();
                String startTime = FormatTools.parseTriasTime(departureTime);
                String endTime = FormatTools.parseTriasTime(arrivalTime);
                String departureName = c.getLegs().get(0).getBoarding().getName();
                String arrivalName = c.getLegs().get(c.getLegs().size() - 1).getAlighting().getName();


                //set Values
                viewHolder.boardingStopTextView.setText(departureName);
                viewHolder.boardingTimeTextView.setText(startTime);
                viewHolder.alightingStopTextView.setText(arrivalName);
                viewHolder.alightingTimeTextView.setText(endTime);

                viewHolder.questionnaireImageButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Questionnaire q = new Questionnaire(getContext(), c);
                        q.setFinishedRecording(f);
                        q.setSuccessfullySendHandler(new Questionnaire.SuccessfullySendHandler() {
                            @Override
                            public void onSuccessfullySend(String result) {
                                qse.onQuestionnaireSolved(f, result);
                            }
                        });
                        q.startForPastConnection();
                    }
                });

            } else {
                viewHolder.boardingStopTextView.setText(Constants.ERRORMSG_NO_CONNECTION_DATA);
            }

        } else {
            viewHolder.boardingStopTextView.setText(Constants.ERRORMSG_NO_CONNECTION_DATA);
        }


        // Return the completed view to render on screen
        return convertView;
    }

    // View lookup cache
    private static class ViewHolder {
        TextView boardingStopTextView;
        TextView alightingStopTextView;
        TextView boardingTimeTextView;
        TextView alightingTimeTextView;
        ImageView questionnaireImageButton;
    }

    /**
     * callback which is triggered when the questionnaire was started, completely solved and the results successfully
     * imported by the server. You can handle it here and do some stuff like displaying a dialog etc.
     */
    public interface QuestionnaireSolvedEvent {
        void onQuestionnaireSolved(FinishedRecording f, String response);
    }

    /**
     * set the function which should be used as callback
     * @param qse function to be called when the questionnaire was solved
     */
    public void setQuestionnaireSolvedEvent(QuestionnaireSolvedEvent qse) {
        this.qse = qse;
    }

}
