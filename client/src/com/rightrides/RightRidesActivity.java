/**
 * Author: Johny Urgiles
 * Date: Dec 10, 2012
 * Org: RightRides
 */
package com.rightrides;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.location.*;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class RightRidesActivity extends Activity {

    private static final long TEN_SECONDS = 10000l;
    private static final long FIVE_SECONDS = 5000l;
    private static final int GPS_MIN_MOVEMENT_UPDATE = 0;
    private static final long GPS_MIN_UPDATE_DELAY = 1;

    private LocationManager locationManager;
    private LocationListener locationListener;
    private Location lastLocation;
    private GpsStatus.Listener gpsStatusListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        final Button toggleBroadcastBtn = (Button) findViewById(R.id.broadcast_toggle_btn);
        final TextView gpsStatusView = (TextView) findViewById(R.id.gps_status_txt);
        final TextView deviceIdView = (TextView) findViewById(R.id.device_id_txt);


        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        deviceIdView.setText("Car ID: \n" + deviceId);

        gpsStatusListener = new GpsStatus.Listener() {
            @Override
            public void onGpsStatusChanged(int i) {
                switch (i) {
                    case GpsStatus.GPS_EVENT_STARTED:
                        gpsStatusView.setText("Starting GPS");
                        gpsStatusView.setTextColor(Color.LTGRAY);
                        Log.i("rightrides|gpsstatus", "GPS started");
                        break;
                    case GpsStatus.GPS_EVENT_FIRST_FIX:
                        gpsStatusView.setText("Found GPS Satellites");
                        gpsStatusView.setTextColor(Color.LTGRAY);
                        Log.i("rightrides|gpsstatus", "GPS satellites found");
                        break;
                    case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                        if (lastLocation == null) {
                            gpsStatusView.setText("Searching for GPS Satellites");
                            gpsStatusView.setTextColor(Color.LTGRAY);
                        } else {
                            long timeSinceLastUpdate = System.currentTimeMillis() - lastLocation.getTime();
                            if (timeSinceLastUpdate > TEN_SECONDS) {
                                gpsStatusView.setText("Satellites lost");
                                gpsStatusView.setTextColor(Color.RED);
                                lastLocation = null;
                            } else {
                                gpsStatusView.setText("GPS Connected");
                                gpsStatusView.setTextColor(Color.GREEN);
                            }
                        }
                        break;
                    case GpsStatus.GPS_EVENT_STOPPED:
                        lastLocation = null;
                        gpsStatusView.setText("GPS Stopped");
                        gpsStatusView.setTextColor(Color.LTGRAY);
                        Log.i("rightrides|gpsstatus", "GPS Turned off by event");
                        break;
                }
            }
        };

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                long timeSinceLastUpdate = lastLocation == null ? System.currentTimeMillis() : System.currentTimeMillis() - lastLocation.getTime();

                if (timeSinceLastUpdate > FIVE_SECONDS) {
                    lastLocation = location;
                    new PostLocationTask(RightRidesActivity.this).execute(location);
                    Log.d("rightrides|locationlistener", "new location->" + location);
                }
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {
                switch(i){
                    case LocationProvider.AVAILABLE:
                        Log.d("rightrides|locationlistener", "location tracking available");
                        break;
                    case LocationProvider.OUT_OF_SERVICE:
                        Log.d("rightrides|locationlistener", "");
                        break;
                    case LocationProvider.TEMPORARILY_UNAVAILABLE:
                        Log.d("rightrides|locationlistener", "location temporarily unavailable");
                        break;
                }
                Log.d("rightrides|locationlistener", "status change for " + s);
            }

            @Override
            public void onProviderEnabled(String s) {
                Log.d("rightrides|locationlistener", "provider enabled->" + s);
            }

            @Override
            public void onProviderDisabled(String s) {
                Log.d("rightrides|locationlistener", "provider disabled->" + s);
            }
        };

        toggleBroadcastBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean on = ((ToggleButton) view).isChecked();

                if (on) {
                    startGpsListeners();
                    Toast.makeText(RightRidesActivity.this, "GPS Tracking On", Toast.LENGTH_SHORT).show();
                    Log.i("rightrides|togglegps", "GPS Turned on manually");
                } else {
                    removeGpsListeners();
                    gpsStatusView.setText("GPS Stopped");
                    gpsStatusView.setTextColor(Color.LTGRAY);
                    Toast.makeText(RightRidesActivity.this, "GPS Tracking Off", Toast.LENGTH_SHORT).show();
                    Log.i("rightrides|togglegps", "GPS Turned off manually");
                }
            }
        });

        startGpsListeners();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        removeGpsListeners();
    }

    private void removeGpsListeners() {
        locationManager.removeGpsStatusListener(gpsStatusListener);
        locationManager.removeUpdates(locationListener);
    }

    private void startGpsListeners() {
        locationManager.addGpsStatusListener(gpsStatusListener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                GPS_MIN_UPDATE_DELAY,
                GPS_MIN_MOVEMENT_UPDATE,
                locationListener);
    }
}