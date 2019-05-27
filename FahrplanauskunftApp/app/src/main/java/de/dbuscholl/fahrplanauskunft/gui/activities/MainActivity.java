package de.dbuscholl.fahrplanauskunft.gui.activities;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import de.dbuscholl.fahrplanauskunft.R;
import de.dbuscholl.fahrplanauskunft.common.App;
import de.dbuscholl.fahrplanauskunft.gui.fragments.ConnectionsFragment;
import de.dbuscholl.fahrplanauskunft.gui.fragments.RecordedTripsFragment;
import de.dbuscholl.fahrplanauskunft.gui.services.TripRecordingService;

/**
 * this is the main activity where everything starts from. It contains a BottomNavigationView with two fragments. This is
 * also the entry point when a user clicked the notification of trip recording. Therefore the service is also bound here.
 * And thats already it. More functionality is given to the fragments themselves.
 */
public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {
    private static Activity context;
    BottomNavigationView navigation;
    private Menu menu;

    private boolean hasAction = false;
    private boolean isBound = false;

    public static Activity getAppContext() {
        return context;
    }

    TripRecordingService gpsService;
    ServiceConnection gpsConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            TripRecordingService.GpsBinder binder = (TripRecordingService.GpsBinder) service;
            gpsService = binder.getService();
            isBound = true;
            if(hasAction) {
                stopRecording();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Creates the bottomnavigationview with the two fragments depending on which one is clicked
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.context = this;

        navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(this);

        String fragment = getIntent().getStringExtra("fragment");
        if (fragment != null) {
            loadFragment(new RecordedTripsFragment());
        } else {
            loadFragment(new ConnectionsFragment());
        }
    }

    /**
     * this is usually called from the fixed foreground service notification to make the recording stop. Therefore we
     * bind the service heere and check wheter the stop action was send or the user only clicked the notification. Tracking
     * should only stop when it was explicitly clicked inside the notification.
     * @param intent
     */
    @Override
    protected void onNewIntent(Intent intent) {
        String fragment = intent.getStringExtra("fragment");
        if (fragment != null) {
            loadFragment(new RecordedTripsFragment());
        } else {
            loadFragment(new ConnectionsFragment());
        }

        String action = intent.getStringExtra("action");
        if(action != null) {
            hasAction = true;
            if(!isBound) {
                if (App.isMyServiceRunning(TripRecordingService.class, getApplicationContext())) {
                    Intent serviceIntent = new Intent(getApplicationContext(), TripRecordingService.class);
                    bindService(serviceIntent, gpsConnection, Context.BIND_AUTO_CREATE);
                }
            } else {
                stopRecording();
            }
        }
    }

    /**
     * this causes the service to stop the recording of a trip
     */
    private void stopRecording() {
        hasAction = false;
        gpsService.stopRecording();
        Log.d(this.getClass().getName(), "Stopping tracking Service!");
    }

    /**
     * Instantiates the fragments depending of which menuitem was clicked from the bottomnavigationview, the intent from
     * where the activity was started or other actions.
     * @param f the fragment to be loaded
     * @return true where everything succeeded, false if not
     */
    private boolean loadFragment(Fragment f) {
        if (f == null) return false;
        getSupportFragmentManager().beginTransaction().replace(R.id.screenscontainer, f).commitAllowingStateLoss();
        if (f instanceof RecordedTripsFragment) {
            if (App.isMyServiceRunning(TripRecordingService.class, getApplicationContext())) {
                Intent intent = new Intent(getApplicationContext(), TripRecordingService.class);
                bindService(intent, gpsConnection, Context.BIND_AUTO_CREATE);
            }
        }
        return true;
    }

    /**
     * Action Listeners for the BottomNavigationView causing the corresponding fragment to load
     * @param menuItem item which was selected
     * @return true if any fragment was loaded (also the same which already was loaded), false if not.
     */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        Context c = getApplicationContext();
        switch (menuItem.getItemId()) {
            case R.id.navigation_connections:
                return loadFragment(new ConnectionsFragment());
            case R.id.navigation_debug:
                return loadFragment(new RecordedTripsFragment());
        }
        return false;
    }

    /**
     * called by system when app is stopped or crashes. It is important to unbind the service (not stop) so that we can
     * bind again the next time, the user opens the app
     */
    @Override
    protected void onDestroy() {
        if (App.isMyServiceRunning(TripRecordingService.class, getApplicationContext())) {
            if(isBound) {
                unbindService(gpsConnection);
                isBound = false;
            }
        }
        super.onDestroy();
    }
}
