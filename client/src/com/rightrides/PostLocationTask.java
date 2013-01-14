/**
 * Author: Johny Urgiles
 * Date: Dec 10, 2012
 * Org: RightRides
 */

package com.rightrides;

import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;
import android.provider.Settings;
import android.text.method.DateTimeKeyListener;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

public class PostLocationTask extends AsyncTask<Location, Void, Void> {

    public static final String RIGHT_RIDES_POST_LOCATION_URL = "http://dispatcher.rightrides.org/api/update/location";

    private Context context;
    private boolean done;
    private long startTime;

    public PostLocationTask(Context context) {
        this.startTime = System.currentTimeMillis();
        this.context = context;
    }

    @Override
    protected Void doInBackground(Location... locations) {

        Location location = locations[0];

        try {
            URL url = new URL(RIGHT_RIDES_POST_LOCATION_URL);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setDoOutput(true);

            String android_id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);

            String params = String.format("id=%s&lat=%s&lng=%s",
                    URLEncoder.encode(android_id , "UTF-8"),
                    URLEncoder.encode(String.valueOf(location.getLatitude()), "UTF-8"),
                    URLEncoder.encode(String.valueOf(location.getLongitude()), "UTF-8"));

            urlConnection.setFixedLengthStreamingMode(params.getBytes().length);
            urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            OutputStreamWriter out = new OutputStreamWriter(urlConnection.getOutputStream());
            out.write(params);
            out.flush();
            out.close();

            int responseCode;
            try{
                responseCode = urlConnection.getResponseCode();
                Log.i("rightrides|POST",  "" + responseCode + "| " + RIGHT_RIDES_POST_LOCATION_URL + "?" + params);
            }catch(IOException e){
                Log.w("rightrides|POST",  "Could not parse response code." );
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public long getStartTime() {
        return startTime;
    }
}
