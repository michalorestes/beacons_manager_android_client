package com.example.michal.lookout_ver00;

import android.Manifest;
import android.bluetooth.BluetoothClass;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.ToggleButton;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

public class NearbyActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, BeaconConsumer {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle button;
    private Toolbar toolbar;
    private String TAG = "Nearby Activity - ";
    //Beacons variables
    private BeaconManager beaconManager;
    private ArrayList<Attraction> attractions;
    private LinearLayout mainPanel;
    private double maxRangeNearby;
    private double maxRangeStart;
    private int STATUS;
    public final static int START = 0;
    public final static int NEARBY = 1;
    public static final String EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE";
    private boolean activityOpened;
    ToggleButton toggle;
    Region region;
    WebServicesConnection services;
    WebServicesInterface wsInterface;
    String action;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearby);
        //get main panel
        mainPanel = (LinearLayout) findViewById(R.id.cardsLayout);
        maxRangeNearby = 1.5;
        maxRangeStart = 0.5;
        STATUS = 1;
        //set toolbar as a action bar
        toolbar = (Toolbar) findViewById(R.id.nav_toolbar);
        setSupportActionBar(toolbar);

        //set up button for opening/closing the side drawer
        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        button = new ActionBarDrawerToggle(this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(button);
        button.syncState();

        //set up event listener for item clicks in navigation drawer
        navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        attractions = new ArrayList<Attraction>();

        // requesting runtime location permission
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
        } else {
            configureBeaconsLibrary();
        }




        activityOpened = false;

        toggle = (ToggleButton) findViewById(R.id.switchViews);
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    STATUS = START;

                } else {
                    STATUS = NEARBY;
                }
            }
        });


        wsInterface = new WebServicesInterface() {

            @Override
            public void recievedData(String results) {
                //download attraction information to check if beacon is activated
                String UUID = "";
                int activated = 0;
                try {
                    JSONObject json = new JSONObject(results);
                    UUID = json.getString("UUID");
                    activated = Integer.parseInt(json.getString("Activated"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (activated == 1){
                    if (STATUS == NEARBY){
                        Attraction a = find(UUID);
                        if (a != null){
                            a.createCard(getApplicationContext());
                            mainPanel.addView(a.getCard());
                        }
                    } else if (STATUS == START){
                        activityOpened = true;
                        initialiseView(UUID);
                    }
                } else {
                    //remove empty attraction
                    remove(UUID);
                }
            }
        };

        services = new WebServicesConnection(wsInterface);
        action = "";

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //beacons set up
                    configureBeaconsLibrary();
                }
                return;
            }
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (beaconManager != null) {
            try {
                beaconManager.stopRangingBeaconsInRegion(region);
                beaconManager.stopMonitoringBeaconsInRegion(region);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            beaconManager.unbind(this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (beaconManager != null) {
            try {
                beaconManager.stopRangingBeaconsInRegion(region);
                beaconManager.stopMonitoringBeaconsInRegion(region);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            beaconManager.unbind(this);
        }
    }


    @Override
    public void onStart(){
        super.onStart();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean status = preferences.getBoolean("status", true);
        toggle.setChecked(status);
    }


    @Override
    public void onStop(){
        super.onStop();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("status", toggle.isChecked());
        editor.commit();
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()){
            case R.id.nav_nearby:

                break;

            case R.id.nav_saved:
                break;
        }

        drawerLayout.closeDrawer(navigationView);
        return true;
    }

    @Override
    public void onBeaconServiceConnect() {
        beaconManager.addRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                if (beacons.size() > 0) {
                    Beacon closest = null;
                    for(final Beacon beacon : beacons){
                       // Log.e("*********", beacon.getId1() + " - Distance: " + beacon.getDistance());
                        if(beacon.getDistance() < maxRangeNearby || beacon.getDistance() < maxRangeStart){
                            //run if nearby mode is enabled
                            if (STATUS == NearbyActivity.NEARBY){
                                //adding new nearby attractions to the display
                                Log.e(TAG, "SCANNING NEARBY");
                                if (!checkForDuplicates(beacon.getId1()+"")){
                                    //add an attraction
                                    attractions.add(new Attraction(beacon, getApplicationContext()));
                                    addToDisplay(beacon.getId1().toString());
                                } else {
                                    //updating distance display-- depricated code
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            find(beacon.getId1().toString()).setDistance(beacon.getDistance());
                                        }
                                    });
                                }
                            }
                            //Run if Immidiate mode is enabled
                            if (STATUS == NearbyActivity.START){
                                //Scanning for immediate attractions
                                if(beacon.getDistance() < maxRangeStart){
                                    //if no closest beacon has been found yet, set current as closest
                                    //or if current beacon is closer
                                    if (closest == null || (closest != null && closest.getDistance() > beacon.getDistance())){
                                        Log.e(TAG, "Closest beacon found");
                                        closest = beacon;
                                    }
                                }
                                //check if closest beacon has been found
                                if (closest != null && activityOpened == false){
                                    //start new activity displaying information about closest beacon
                                    services.getAttraction(closest.getId1().toString());
                                }
                            }
                        }
                    }
                }
                cleanUp();
            }
        });

        try {
            beaconManager.startMonitoringBeaconsInRegion(region);
            beaconManager.startRangingBeaconsInRegion(region);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    private void initialiseView(String UUID){
        Log.e(TAG, "Starting a new intent");
        Intent i = new Intent(this, ViewActivity.class);
        i.putExtra(EXTRA_MESSAGE, UUID);
        startActivity(i);
    }

    //checks if beacon (attraction) is already in the list
    private boolean checkForDuplicates(String uuid){
        for (Attraction a: attractions) {
            if (a.getUUID().equals(uuid)) {
                return true;
            }
        }
        return false;
    }

    //get attraction from list based on UUID
    private Attraction find(String UUID){
        for(Attraction a: attractions){
            if (a.getUUID().equals(UUID.toLowerCase())){
                return a;
            }
        }
        return  null;
    }

    //remove attraction
    private void remove(String UUID){
        for (int i = 0; i < attractions.size(); i++){
            if (attractions.get(i).getUUID().equals(UUID)){
                attractions.remove(i);
            }
        }
    }

    private void cleanUp(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for(int i = 0 ; i < attractions.size(); i++){
                    Calendar cal = Calendar.getInstance();
                    long lastUpdate = cal.getTimeInMillis() - attractions.get(i).getLastUpdate();
                    if (attractions.get(i).getDistance() > maxRangeNearby || lastUpdate > 10000 ){
                       try{
                           mainPanel.removeView(attractions.get(i).getCard());
                           attractions.remove(i);
                       } catch (Exception e){

                       }
                    }
                }
            }
        });

    }

    private void addToDisplay(final String UUID){
        services.getAttraction(UUID);

    }

    private void configureBeaconsLibrary(){
        beaconManager = BeaconManager.getInstanceForApplication(this);
        //set up beacon layout to iBeacon format
        beaconManager.getBeaconParsers().add(new
                BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));
        //for development purposes only
        beaconManager.setRegionStatePeristenceEnabled(false);
        try {
            beaconManager.setForegroundScanPeriod(200l); // 100 mS
            beaconManager.setForegroundBetweenScanPeriod(100); // 0ms
            beaconManager.updateScanPeriods();
        }
        catch (RemoteException e) {
            Log.e(TAG, "Cannot talk to service");
        }

        region = new Region("defaultRegion", null, null, null);
        beaconManager.bind(this);
    }
}
