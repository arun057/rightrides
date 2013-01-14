/**
 * Author: Johny Urgiles
 * Date: Dec 10, 2012
 * Org: RightRides
 */
package com.rightrides;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.*;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;

public class MainActivity extends Activity {

    private static final long GPS_TIMEOUT = 10000l;
    private static final long MIN_DELAY_BTWN_LOC_POSTS = 30000l;
    private static final int GPS_MIN_MOVEMENT_UPDATE = 0;
    private static final long GPS_MIN_UPDATE_DELAY = 1;
    private static final String RR = "rightrides|";

    private LocationManager locationManager;
    private LocationListener locationListener;
    private Location lastLocation;
    private GpsStatus.Listener gpsStatusListener;
    private PostLocationTask currentPostLocationTask;
    private int STATUS_SET_COLOR = Color.BLACK;
    private int STATUS_PENDING_COLOR = Color.GRAY;
    private int STATUS_OKAY_COLOR = R.color.rr_darkgreen;
    private int STATUS_ERROR_COLOR = Color.RED;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.main);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title_bar);

        final ToggleButton toggleBroadcastBtn = (ToggleButton) findViewById(R.id.broadcast_toggle_btn);
        final TextView gpsStatusView = (TextView) findViewById(R.id.gps_status_txt);
        final TextView deviceIdView = (TextView) findViewById(R.id.device_id_txt);

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            toggleBroadcastBtn.setEnabled(false);
            toggleBroadcastBtn.setChecked(false);
            requestUserToEnableGPS();
        }

        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        deviceIdView.setText("Car ID: \n" + deviceId);

        gpsStatusListener = new GpsStatus.Listener() {
            @Override
            public void onGpsStatusChanged(int i) {
                switch (i) {
                    case GpsStatus.GPS_EVENT_STARTED:
                        gpsStatusView.setText("Starting GPS");
                        gpsStatusView.setTextColor(STATUS_SET_COLOR);
                        Log.i(RR + "gpsstatus", "GPS started");
                        break;
                    case GpsStatus.GPS_EVENT_FIRST_FIX:
                        gpsStatusView.setText("Found GPS Satellites");
                        gpsStatusView.setTextColor(STATUS_SET_COLOR);
                        Log.i(RR + "gpsstatus", "GPS satellites found");
                        break;
                    case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                        if (lastLocation == null) {
                            gpsStatusView.setText("Searching for GPS Satellites");
                            STATUS_SET_COLOR = STATUS_PENDING_COLOR;
                            gpsStatusView.setTextColor(STATUS_SET_COLOR);
                        } else {
                            long timeSinceLastUpdate = System.currentTimeMillis() - lastLocation.getTime();
                            if (timeSinceLastUpdate > GPS_TIMEOUT) {
                                gpsStatusView.setText("Satellites lost");
                                gpsStatusView.setTextColor(STATUS_ERROR_COLOR);
                                lastLocation = null;
                            } else {
                                gpsStatusView.setText("GPS Connected");
                                gpsStatusView.setTextColor(STATUS_OKAY_COLOR);
                            }
                        }
                        break;
                    case GpsStatus.GPS_EVENT_STOPPED:
                        lastLocation = null;
                        gpsStatusView.setText("GPS Stopped");
                        gpsStatusView.setTextColor(STATUS_SET_COLOR);
                        Log.i(RR + "gpsstatus", "GPS Turned off by event");
                        break;
                }
            }
        };

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                lastLocation = location;

                long lastServerPost = currentPostLocationTask == null ? 0 : currentPostLocationTask.getStartTime();
                if (System.currentTimeMillis() - lastServerPost > MIN_DELAY_BTWN_LOC_POSTS) {
                    cancelCurrentPostLocationTask();
                    currentPostLocationTask = new PostLocationTask(MainActivity.this);
                    currentPostLocationTask.execute(location);
                    Log.d(RR + "locationlistener", "new location->" + location);
                }
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {
                switch (i) {
                    case LocationProvider.AVAILABLE:
                        Log.d(RR + "locationlistener", "location tracking available");
                        break;
                    case LocationProvider.OUT_OF_SERVICE:
                        Log.d(RR + "locationlistener", "");
                        break;
                    case LocationProvider.TEMPORARILY_UNAVAILABLE:
                        Log.d(RR + "locationlistener", "location temporarily unavailable");
                        break;
                }
                Log.d(RR + "locationlistener", "status change for " + s);
            }

            @Override
            public void onProviderEnabled(String s) {
                Log.d(RR + "locationlistener", "provider enabled->" + s);
            }

            @Override
            public void onProviderDisabled(String s) {
                Log.d(RR + "locationlistener", "provider disabled->" + s);
            }
        };

        toggleBroadcastBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean on = ((ToggleButton) view).isChecked();

                if (on) {
                    startGpsListeners();
                    Log.i(RR + "togglegps", "GPS Turned on manually");
                } else {
                    removeGpsListeners();
                    gpsStatusView.setText("GPS Stopped");
                    gpsStatusView.setTextColor(STATUS_SET_COLOR);
                    Log.i(RR + "togglegps", "GPS Turned off manually");
                }
            }
        });

        startGpsListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            final ToggleButton toggleBroadcastBtn = (ToggleButton) findViewById(R.id.broadcast_toggle_btn);
            toggleBroadcastBtn.setEnabled(true);
            toggleBroadcastBtn.setChecked(true);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelCurrentPostLocationTask();
        removeGpsListeners();
    }

    private void requestUserToEnableGPS() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("GPS is disabled. Enable it to allow driver tracking.")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent gpsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(gpsIntent);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                        final TextView textView = (TextView) MainActivity.this.findViewById(R.id.gps_status_txt);
                        textView.setText("GPS is disabled");
                        final ToggleButton toggleBroadcastBtn = (ToggleButton) findViewById(R.id.broadcast_toggle_btn);
                        toggleBroadcastBtn.setEnabled(false);
                    }
                });

        final AlertDialog alert = builder.create();
        alert.show();
    }

    private void cancelCurrentPostLocationTask() {
        if (currentPostLocationTask != null && !currentPostLocationTask.isDone()) {
            currentPostLocationTask.cancel(true);
            Log.i(RR + "posttaskstatus", "post request taking too long, cancelling request");
        }
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