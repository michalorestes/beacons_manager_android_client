package com.example.michal.lookout_ver00;

import android.content.Context;
import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Michal on 03/04/2017.
 */

class WebServicesConnection extends AsyncTask<String, String, String> {

    public WebServicesInterface delegate = null;
    final static String linkBase = "http://192.168.1.134:8080/BeaconsManager/Services/";
    WebServicesInterface wsInterface;

    public WebServicesConnection(WebServicesInterface wsInterface){
        this.wsInterface = wsInterface;
    }


    @Override
    protected String doInBackground(String... params) {
        HttpURLConnection connection = null;

        BufferedReader bufferedReader = null;
        try {
            URL url = new URL(params[0]);
            connection = (HttpURLConnection) url.openConnection();
            InputStream stream = connection.getInputStream();
            bufferedReader = new BufferedReader(new InputStreamReader(stream));
            StringBuffer buffer = new StringBuffer();
            String line = "";
            while ((line = bufferedReader.readLine()) != null) {
                buffer.append(line);
            }
            return buffer.toString();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public void getAttraction(String uuid){
        WebServicesConnection c = new WebServicesConnection(wsInterface);
        c.delegate = wsInterface;
        c.execute(linkBase + "getAttraction.php?id=" + uuid);
    }

    public void getTranslations(String id){
        WebServicesConnection c = new WebServicesConnection(wsInterface);
        c.delegate = wsInterface;
        c.execute(linkBase + "getTranslations.php?id=" + id);
    }

    public void getGallery(String id){
        WebServicesConnection c = new WebServicesConnection(wsInterface);
        c.delegate = wsInterface;
        c.execute(linkBase + "getGallery.php?id=" + id);
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        delegate.recievedData(s);
    }


}
