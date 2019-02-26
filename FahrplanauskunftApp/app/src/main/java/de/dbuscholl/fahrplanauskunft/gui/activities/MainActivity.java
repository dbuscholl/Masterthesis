package de.dbuscholl.fahrplanauskunft.gui.activities;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import de.dbuscholl.fahrplanauskunft.R;
import de.dbuscholl.fahrplanauskunft.gui.fragments.ConnectionsFragment;
import de.dbuscholl.fahrplanauskunft.gui.fragments.DebugFragment;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {
    private static Activity context;

    public static Activity getAppContext() {
        return context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.context = this;

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(this);

        loadFragment(new ConnectionsFragment());
    }

    private boolean loadFragment(Fragment f) {
        if (f == null) return false;
        getSupportFragmentManager().beginTransaction().replace(R.id.screenscontainer, f).commit();
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
}
