package de.dbuscholl.fahrplanauskunft.gui.activities;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Debug;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import de.dbuscholl.fahrplanauskunft.R;
import de.dbuscholl.fahrplanauskunft.common.App;
import de.dbuscholl.fahrplanauskunft.gui.fragments.ConnectionsFragment;
import de.dbuscholl.fahrplanauskunft.gui.fragments.DebugFragment;
import de.dbuscholl.fahrplanauskunft.gui.services.TripRecordingService;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {
    private static Activity context;
    BottomNavigationView navigation;
    private Menu menu;

    public static Activity getAppContext() {
        return context;
    }

    TripRecordingService gpsService;
    ServiceConnection gpsConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            TripRecordingService.GpsBinder binder = (TripRecordingService.GpsBinder) service;
            gpsService = binder.getService();
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.context = this;

        navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(this);

        String fragment = getIntent().getStringExtra("fragment");
        if (fragment != null) {
            loadFragment(new DebugFragment());
        } else {
            loadFragment(new ConnectionsFragment());
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        String fragment = intent.getStringExtra("fragment");
        if (fragment != null) {
            loadFragment(new DebugFragment());
        } else {
            loadFragment(new ConnectionsFragment());
        }
    }

    private boolean loadFragment(Fragment f) {
        if (f == null) return false;
        getSupportFragmentManager().beginTransaction().replace(R.id.screenscontainer, f).commitAllowingStateLoss();
        if (f instanceof DebugFragment) {
            if (App.isMyServiceRunning(TripRecordingService.class, getApplicationContext())) {
                Intent intent = new Intent(getApplicationContext(), TripRecordingService.class);
                bindService(intent, gpsConnection, Context.BIND_AUTO_CREATE);
            }
        }
        return true;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        Context c = getApplicationContext();
        switch (menuItem.getItemId()) {
            case R.id.navigation_connections:
                return loadFragment(new ConnectionsFragment());
            case R.id.navigation_debug:
                return loadFragment(new DebugFragment());
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        if (App.isMyServiceRunning(TripRecordingService.class, getApplicationContext())) {
            unbindService(gpsConnection);
        }
        super.onDestroy();
    }
}
