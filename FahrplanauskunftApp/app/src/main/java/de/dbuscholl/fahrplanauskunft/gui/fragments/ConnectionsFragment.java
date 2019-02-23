package de.dbuscholl.fahrplanauskunft.gui.fragments;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;

import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.filter.ElementFilter;

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
import de.dbuscholl.fahrplanauskunft.common.Constants;
import de.dbuscholl.fahrplanauskunft.gui.adapters.AutoCompleteAdapter;
import de.dbuscholl.fahrplanauskunft.network.Client;
import de.dbuscholl.fahrplanauskunft.network.entities.Service;
import de.dbuscholl.fahrplanauskunft.network.entities.Station;
import de.dbuscholl.fahrplanauskunft.network.entities.StopPoint;
import de.dbuscholl.fahrplanauskunft.network.entities.Trip;
import de.dbuscholl.fahrplanauskunft.network.entities.TripResult;
import de.dbuscholl.fahrplanauskunft.network.xml.TripInfoRequest;
import de.dbuscholl.fahrplanauskunft.network.xml.XMLDocument;

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
                    ArrayList<TripResult> trips = new TripTask().execute(tir.toString()).get();
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

    private class TripTask extends AsyncTask<String, Void, ArrayList<TripResult>> {

        @Override
        protected ArrayList<TripResult> doInBackground(String... strings) {
            try {
                ArrayList<TripResult> results = new ArrayList<>();
                Client c = new Client("http://efastatic.vvs.de/kleinanfrager/trias");
                String response = c.sendPostXML(strings[0]);
                XMLDocument xml = XMLDocument.documentFromString(response);

                for (Element e : xml.getDocument().getDescendants(new ElementFilter("Trip"))) {
                    TripResult t = getTripResult(e);
                    ArrayList<Trip> legs = getTripLegs(e);
                    t.setLegs(legs);
                    results.add(t);
                }
                return results;
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JDOMException e) {
                e.printStackTrace();
            }
            return new ArrayList<>();
        }

        private TripResult getTripResult(Element e) {
            TripResult t = new TripResult();

            Element tripId = e.getChild("TripId", Constants.NAMESPACE);
            Element startTime = e.getChild("StartTime", Constants.NAMESPACE);
            Element endtime = e.getChild("EndTime", Constants.NAMESPACE);

            if (tripId != null) {
                t.setId(tripId.getTextNormalize());
            }
            if (startTime != null) {
                t.setStartTime(startTime.getTextNormalize());
            }
            if (endtime != null) {
                t.setEndTime(endtime.getTextNormalize());
            }
            return t;
        }

        private ArrayList<Trip> getTripLegs(Element xml) {
            ArrayList<Trip> triplegs = new ArrayList<>();


            for (Element e : xml.getDescendants(new ElementFilter("TripLeg"))) {
                int legId = 1;
                Element timedLeg = e.getChild("TimedLeg", Constants.NAMESPACE);
                Element interchangeLeg = e.getChild("InterchangeLeg", Constants.NAMESPACE);
                Element continuousLeg = e.getChild("ContinuousLeg", Constants.NAMESPACE);

                try {
                    Trip leg = null;
                    if (timedLeg != null) {
                        leg = getTimedTrip(legId, timedLeg);
                    } else if (interchangeLeg != null) {
                        leg = getInterchangeTrip(legId, interchangeLeg);
                    } else if (continuousLeg != null) {
                        Toast.makeText(getActivity().getApplicationContext(), "ContinuousLeg", Toast.LENGTH_LONG);
                        leg = new Trip();
                    }
                    if (leg != null) {
                        triplegs.add(leg);
                        legId++;
                    }
                } catch (NullPointerException exception) {
                    Log.e(getClass().getName(), exception.getMessage(), exception);
                    Log.e(getClass().getName(), xml.toString());
                    return null;
                }
            }

            return triplegs;
        }

        private Trip getInterchangeTrip(int legId, Element interchangeElement) {
            Trip t = new Trip();
            t.setLegId(legId);
            t.setType(Trip.TripType.INTERCHANGE);

            Element interchangeMode = interchangeElement.getChild("InterchangeMode", Constants.NAMESPACE);
            Element stopPointRefStart = interchangeElement.getChild("LegStart", Constants.NAMESPACE).getChild("StopPointRef", Constants.NAMESPACE);
            Element stopNameStart = interchangeElement.getChild("LegStart", Constants.NAMESPACE).getChild("LocationName", Constants.NAMESPACE);
            Element stopPointRefEnd = interchangeElement.getChild("LegEnd", Constants.NAMESPACE).getChild("StopPointRef", Constants.NAMESPACE);
            Element stopNameEnd = interchangeElement.getChild("LegEnd", Constants.NAMESPACE).getChild("LocationName", Constants.NAMESPACE);
            Element startTime = interchangeElement.getChild("TimeWindowStart", Constants.NAMESPACE);
            Element endTime = interchangeElement.getChild("TimeWindowStart", Constants.NAMESPACE);

            if (stopPointRefStart == null && stopNameStart == null) {
                throw new NullPointerException("Name and Ref of Start is null");
            }
            if (stopPointRefEnd == null && stopNameEnd == null) {
                throw new NullPointerException("Name and Ref of End is null");
            }

            StopPoint start = new StopPoint();
            if (stopPointRefStart != null) {
                start.setRef(stopPointRefStart.getTextNormalize());
            }
            if (stopNameStart != null) {
                start.setName(stopNameStart.getChildTextNormalize("Text", Constants.NAMESPACE));
            }
            if (startTime != null) {
                start.setArrivalTime(startTime.getTextNormalize());
            }
            t.setBoarding(start);

            StopPoint end = new StopPoint();
            if (stopPointRefEnd != null) {
                end.setRef(stopPointRefEnd.getTextNormalize());
            }
            if (stopNameEnd != null) {
                end.setName(stopNameEnd.getChildTextNormalize("Text", Constants.NAMESPACE));
            }
            if (endTime != null) {
                end.setArrivalTime(endTime.getTextNormalize());
            }
            t.setAlighting(end);

            t.setInterchangeType(interchangeMode.getTextNormalize());

            return t;
        }

        private Trip getTimedTrip(int legId, Element timedLeg) {
            Trip t = new Trip();
            t.setLegId(legId);
            t.setType(Trip.TripType.TIMED);
            Element boardingElement = timedLeg.getChild("LegBoard", Constants.NAMESPACE);

            // boarding
            StopPoint boarding = getStopPoint(boardingElement, 1);
            t.setBoarding(boarding);

            // intermediates
            int position = 2;
            ArrayList<StopPoint> intermediates = new ArrayList<>();
            for (Element intermediate : timedLeg.getDescendants(new ElementFilter("LegIntermediates"))) {
                StopPoint stop = getStopPoint(intermediate, position);
                intermediates.add(stop);
                position++;
            }
            t.setIntermediates(intermediates);

            // alighting
            StopPoint alighting = getStopPoint(timedLeg.getChild("LegAlight", Constants.NAMESPACE), position);
            t.setAlighting(alighting);

            // service info
            Element serviceItem = timedLeg.getChild("Service", Constants.NAMESPACE);
            Service service = getServiceInfo(serviceItem);
            t.setService(service);

            return t;
        }

        private Service getServiceInfo(Element serviceElement) {
            Service service = new Service();

            Element operatingDayRef = serviceElement.getChild("OperatingDayRef", Constants.NAMESPACE);
            Element journeyRef = serviceElement.getChild("JourneyRef", Constants.NAMESPACE);
            Element lineRef = serviceElement.getChild("LineRef", Constants.NAMESPACE);
            Element mode = serviceElement.getChild("Mode", Constants.NAMESPACE);
            Element publishedLineName = serviceElement.getChild("PublishedLineName", Constants.NAMESPACE);
            Element routeDescription = serviceElement.getChild("RouteDescription", Constants.NAMESPACE);
            Element destinationText = serviceElement.getChild("DestinationText", Constants.NAMESPACE);

            service.setOperatingDayRef(operatingDayRef.getTextNormalize());
            service.setJourneyRef(journeyRef.getTextNormalize());
            service.setLineRef(lineRef.getTextNormalize());
            service.setLineName(publishedLineName.getChildTextNormalize("Text", Constants.NAMESPACE));

            // railtype
            Element modeName = mode.getChild("Name", Constants.NAMESPACE);
            if (modeName != null) {
                service.setRailName(modeName.getChild("Text", Constants.NAMESPACE).getTextNormalize());
            }
            service.setRailType(getSubmode(mode));

            if (routeDescription != null) {
                service.setRoute(routeDescription.getChildTextNormalize("RouteDescription", Constants.NAMESPACE));
            }
            if (destinationText != null) {
                service.setDesitnation(destinationText.getChildTextNormalize("DestinationText", Constants.NAMESPACE));
            }

            return service;
        }

        private String getSubmode(Element mode) {
            String[] types = {"RailSubmode", "CoachSubmode", "MetroSubmode", "BusSubmode", "TramSubmode", "WaterSubmode", "AirSubmode", "TelecabinSubmode", "FunicularSubmode", "TaxiSubmode"};

            for (String s : types) {
                Element e = mode.getChild(s, Constants.NAMESPACE);
                if (e != null) {
                    return e.getTextNormalize();
                }
            }
            return null;
        }

        private StopPoint getStopPoint(Element timedLegItem, int position) {
            StopPoint stop = new StopPoint();

            // getting all elements
            Element ref = timedLegItem.getChild("StopPointRef", Constants.NAMESPACE);
            Element name = timedLegItem.getChild("StopPointName", Constants.NAMESPACE).getChild("Text", Constants.NAMESPACE);
            Element bay = timedLegItem.getChild("PlannedBay", Constants.NAMESPACE);
            Element arrival = timedLegItem.getChild("ServiceArrival", Constants.NAMESPACE);
            Element departure = timedLegItem.getChild("ServiceDeparture", Constants.NAMESPACE);
            Element stopSeqNumber = timedLegItem.getChild("StopSeqNumber", Constants.NAMESPACE);

            stop.setRef(ref.getTextNormalize());
            stop.setName(name.getTextNormalize());

            // handling times
            if (arrival == null && departure == null) {
                throw new NullPointerException("Stop has neither Arrival nor Departure!");
            }

            if (arrival != null) {
                Element timetabledTime = arrival.getChild("TimetabledTime", Constants.NAMESPACE);
                Element estimatedTime = arrival.getChild("EstimatedTime", Constants.NAMESPACE);
                if (timetabledTime != null) {
                    stop.setArrivalTime(timetabledTime.getTextNormalize());
                }
                if (estimatedTime != null) {
                    stop.setArrivalTimeEstimated(estimatedTime.getTextNormalize());
                }
            }

            if (departure != null) {
                Element timetabledTime = departure.getChild("TimetabledTime", Constants.NAMESPACE);
                Element estimatedTime = departure.getChild("EstimatedTime", Constants.NAMESPACE);
                if (timetabledTime != null) {
                    stop.setDepartureTime(timetabledTime.getTextNormalize());
                }
                if (estimatedTime != null) {
                    stop.setDepartureTimeEstimated(estimatedTime.getTextNormalize());
                }
            }

            // handling bay
            if (bay != null) {
                stop.setBay(bay.getChild("Text", Constants.NAMESPACE).getTextNormalize());
            }

            if (position <= 0) {
                if (stopSeqNumber != null) {
                    try {
                        stop.setPosition(Integer.parseInt(stopSeqNumber.getTextNormalize()));
                    } catch (NumberFormatException e) {
                        stop.setPosition(0);
                    }
                }
            } else {
                stop.setPosition(position);
            }

            return stop;
        }
    }
}
