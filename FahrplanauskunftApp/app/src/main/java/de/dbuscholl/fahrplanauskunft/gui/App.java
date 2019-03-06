package de.dbuscholl.fahrplanauskunft.gui;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class App extends Application {
    public static final String CHANNEL_ID = "mainchannel";

    @Override
    public void onCreate() {
        super.onCreate();

        creatNotificationChannels();
    }

    private void creatNotificationChannels() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel main = new NotificationChannel(CHANNEL_ID,"Location Tracking", NotificationManager.IMPORTANCE_LOW);
            main.setDescription("All notifications about location tracking and information are displayed here!");

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(main);
        }
    }
}
