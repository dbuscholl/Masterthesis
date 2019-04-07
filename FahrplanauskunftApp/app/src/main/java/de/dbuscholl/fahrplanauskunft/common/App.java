package de.dbuscholl.fahrplanauskunft.common;

import android.app.ActivityManager;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

public class App extends Application {
    public static final String CHANNEL_ID = "mainchannel";
    public static final String CHANNEL_ID_DONE_CHANNEL = "donechannel";

    @Override
    public void onCreate() {
        super.onCreate();

        creatNotificationChannels();
    }

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
