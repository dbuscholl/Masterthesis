package de.dbuscholl.fahrplanauskunft.common;

import android.app.ActivityManager;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

/**
 * <p>This Class holds some important information about the Application. In this case these are the Notification configuration
 * stuff. It creates two Notification Channels. One for the foreground service to be fixed and indicating that a trip is
 * being recorded and the second is for informing the user about the end of recording when he reached the end of the trip.
 * </p>
 * <p>There are seperate channels used because using the same channel for the end-trip-notification would override the fixed
 * one which could be usefull, but sometimes the user has more than one trip to record and for this case it should be a seperate
 * message. Else the foreground service would stop</p>
 */
public class App extends Application {
    public static final String CHANNEL_ID = "mainchannel";
    public static final String CHANNEL_ID_DONE_CHANNEL = "donechannel";

    /**
     * as soon as the app is launched it should create the notification channels
     */
    @Override
    public void onCreate() {
        super.onCreate();

        creatNotificationChannels();
    }

    /**
     * this is called onCreate(). This function creates both notification channels. One which shows the fixed notification
     * indicating that a trip is recording and the second one which informs the user when a trip is done recording.
     */
    private void creatNotificationChannels() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel main = new NotificationChannel(CHANNEL_ID,"Location Tracking", NotificationManager.IMPORTANCE_DEFAULT);
            main.setDescription("All notifications about location tracking and information are displayed here!");

            NotificationChannel done = new NotificationChannel(CHANNEL_ID_DONE_CHANNEL, "Finished Tracks", NotificationManager.IMPORTANCE_HIGH);
            done.setDescription("This channel informs you about tracks that were finished recording and are ready to send!");

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(main);
            manager.createNotificationChannel(done);
        }
    }

    /**
     * This function checks wheter a service given by class reference is already running. This is used for the TripRecordingService
     * which should only run with a single instance. This function is therefore used to check for that
     * @param serviceClass the Class which should be checked for
     * @param context application context
     * @return true if the service is running, false if not
     */
    public static boolean isMyServiceRunning(Class<?> serviceClass, Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                if(service.foreground) {
                    return true;
                }
            }
        }
        return false;
    }
}
