package de.dbuscholl.fahrplanauskunft;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
