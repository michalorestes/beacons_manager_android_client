package com.example.michal.lookout_ver00;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

import org.altbeacon.beacon.Beacon;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static com.example.michal.lookout_ver00.NearbyActivity.EXTRA_MESSAGE;

/**
 * Created by Michal on 25/03/2017.
 */

public class Attraction extends AsyncTask<String, Integer, Bitmap> implements View.OnClickListener  {
    private String UUID;
    private String title;
    private double distance;
    private String description;
    private long lastUpdate;
    private Calendar calendar;
    private SimpleDateFormat dateFormat;
    private AttractionCard card;
    private String action;
    private int activated;
    private WebServicesConnection con;
    String imageURL;
    Context context;


    public Attraction(Beacon beacon, final Context context){
        this.context = context;
        this.UUID = beacon.getId1().toString();
        this.distance = beacon.getDistance();
        WebServicesInterface wsInterface = new WebServicesInterface() {
            @Override
            public void recievedData(String results) {
                switch (action){
                    case "GET ATTRACTION":
                        try {
                            JSONObject json = new JSONObject(results);
                            activated = Integer.parseInt(json.getString("Activated"));
                            title = json.getString("Title");

                            action = "GALLERY";
                            con.getGallery(json.getString("AttractionID"));


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;

                    case "GALLERY":
                        try {
                            JSONArray json = new JSONArray(results);
                            imageURL = json.getString(0).toString();
                            setImage();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        break;
                }

            }
        };
        con = new WebServicesConnection(wsInterface);
        action = "GET ATTRACTION";
        con.getAttraction(UUID);
        //time, last time detected
        calendar = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("HH:mm:ss");
        updateTime();

    }

    public int getActivated(){
        return activated;
    }

    public long getLastUpdate(){
        return lastUpdate;
    }
    public String getDescription() {
        return description;
    }

    public String getTitle() {
        return title;
    }

    public String getUUID() {
        return UUID;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        updateTime();
        //card.setDistance(distance);
        this.distance = distance;
    }
/*
    public void loadBeacon(String title){
        this.title = title;
    }
*/
    private void updateTime(){
        calendar = Calendar.getInstance();
        this.lastUpdate = calendar.getTimeInMillis();
    }

    public void createCard(Context context){
        if (card == null){
            card = new AttractionCard(context);
            card.setTitle(title);
           // card.setDistance(distance);
            card.setOnClickListener(this);
        }
    }

    public AttractionCard getCard(){
        return card;
    }

    public void setImage(){
        this.execute(imageURL);
    }

    @Override
    public void onClick(View v) {

        Intent i = new Intent(context, ViewActivity.class);
        i.putExtra(EXTRA_MESSAGE, UUID);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.getApplicationContext().startActivity(i);
    }

    @Override
    protected Bitmap doInBackground(String... params) {
        //Below code has been adapted from
        //http://stackoverflow.com/questions/8464506/how-to-display-an-image-from-an-url-within-textview
        BufferedInputStream bis = null;
        InputStream is = null;
        Bitmap bm = null;
        try {
        /* Open a new URL and get the InputStream to load data from it. */
            URL aURL = new URL(params[0]);
            URLConnection conn = aURL.openConnection();
            conn.connect();
            is = conn.getInputStream();
        /* Buffered is always good for a performance plus. */
            bis = new BufferedInputStream(is);
        /* Decode url-data to a bitmap. */
            bm = BitmapFactory.decodeStream(bis);

        } catch (IOException e) {
            Log.e("DEBUGTAG", "Remote Image Exception", e);
        } catch (Exception e){

        } finally {
            try {
                bis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return bm;
        }
    }

    @Override
    protected void onPostExecute(Bitmap result) {
        card.setImage(result);

    }
}
