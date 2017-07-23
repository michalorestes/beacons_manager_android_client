package com.example.michal.lookout_ver00;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerFragment;
import org.json.JSONException;
import org.json.JSONObject;

public class ViewActivity extends AppCompatActivity  implements YouTubePlayer.OnInitializedListener, AdapterView.OnItemSelectedListener {

    private Toolbar toolbar;
    String API_KEY = "AIzaSyCToB6GMhgK6aESnpoghR9-m0oK_F-amdg";
    private String VIDEO_ID = "-m3V8w_7vhk";
    String action;
    private static final String EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE";
    WebServicesConnection services;
    JSONObject translations;
    JSONObject json;
    //UI
    TextView txtDescription;
    TextView txtTitle;
    Spinner descriptionLanguages;
    WebServicesInterface wsInterface;
    String message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view);
        action = "";
        message ="";
        //UI SETTINGS
        //set toolbar as a action bar
        toolbar = (Toolbar) findViewById(R.id.nav_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        descriptionLanguages = (Spinner) findViewById(R.id.descriptionLanguages);

        //Get message from previous activity
        Intent intent = getIntent();
        message = intent.getStringExtra(NearbyActivity.EXTRA_MESSAGE);

        txtDescription = (TextView) findViewById(R.id.txtDescription);
        txtTitle = (TextView) findViewById(R.id.txtTitle);

        //Web Services

        wsInterface = new WebServicesInterface() {
            @Override
            public void recievedData(String results) {
                switch (action){
                    case "GET ATTRACTION":
                        updateUI(results);
                        try {
                            if (json != null) {
                                Log.e("TTT", json.getString("AttractionID"));
                                action = "LANG";
                                services.getTranslations(json.getString("AttractionID"));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e("TEST", "mainData not found");
                        }
                        break;
                    case "LANG":
                        try {
                            Log.e("TEST", "getting translations");
                            translations = new JSONObject(results);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e("TEST", "translation not found ");
                        }
                        break;

                }
            }
        };
        services = new WebServicesConnection(wsInterface);
        services.delegate = wsInterface;
        action = "GET ATTRACTION";
        services.getAttraction(message);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                // yourMethod();
            }
        }, 3000);   //5 seconds

        json = null;

        //Other settings
        descriptionLanguages.setOnItemSelectedListener(this);

    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult result) {
        Toast.makeText(this, "Failed to initialize.", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer player, boolean wasRestored) {
        if(null== player) return;

        // Start buffering
        if (!wasRestored) {
            player.cueVideo(VIDEO_ID);
        }
    }

    //action bar buttons
    @Override
    public boolean onOptionsItemSelected(MenuItem item){

        switch (item.getItemId()){
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;

        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (position){
            case 0:
                try {
                    txtDescription.setText(json.getString("Description"));
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (Exception e){}
                break;
            case 1:
                //Request to download translations
                try {
                    String l = translations.getString("Spanish");
                    txtDescription.setText(l);
                } catch (JSONException e) {
                    txtDescription.setText("Language unavailable");
                }
                break;
            case 2:
                try {
                    String l = translations.getString("Polish");
                    txtDescription.setText(l);
                } catch (JSONException e) {
                    txtDescription.setText("Language unavailable");
                }
                break;
            case 3:
                try {
                    String l = translations.getString("French");
                    txtDescription.setText(l);
                } catch (JSONException e) {
                    txtDescription.setText("Language unavailable");
                }
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private void setLaguage(String results, String lang){
        try {
            json = new JSONObject(results);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            txtDescription.setText(json.getString(lang));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void updateUI(String results) {

        try {
            json = new JSONObject(results);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            if (json != null) {
                txtTitle.setText(json.getString("Title"));
                txtDescription.setText(json.getString("Description"));
                VIDEO_ID = json.getString("Video");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        initialisePlayer();
    }

    private void initialisePlayer(){
        // Initializing YouTube player view
        //http://stacktips.com/tutorials/android/youtube-android-player-api-example
        FragmentManager fm = getFragmentManager();
        String tag = YouTubePlayerFragment.class.getSimpleName();
        YouTubePlayerFragment playerFragment = (YouTubePlayerFragment) fm.findFragmentByTag(tag);
        if (playerFragment == null) {
            FragmentTransaction ft = fm.beginTransaction();
            playerFragment = YouTubePlayerFragment.newInstance();

            ft.add(R.id.videoLayout, playerFragment, tag);
            ft.commit();
        }

        playerFragment.initialize(API_KEY, new YouTubePlayer.OnInitializedListener() {
            @Override
            public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
                youTubePlayer.cueVideo(VIDEO_ID);
            }

            @Override
            public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
                Toast.makeText(ViewActivity.this, "Error while initializing YouTubePlayer.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
