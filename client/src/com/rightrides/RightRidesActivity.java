/**
 * Author: Johny Urgiles
 * Date: Dec 10, 2012
 * Org: RightRides
 */
package com.rightrides;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class RightRidesActivity extends Activity {

    private static final long TEN_SECONDS = 10000l;
    private static final int GPS_MIN_MOVEMENT_UPDATE = 0;
    private static final long GPS_MIN_UPDATE_DELAY = 5000;

    private LocationManager locationManager;
    private LocationListener locationListener;
    private Location lastLocation;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        final Button toggleBroadcastBtn = (Button) findViewById(R.id.broadcast_toggle_btn);
        final TextView gpsStatusView = (TextView) findViewById(R.id.gps_status_txt);

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        locationManager.addGpsStatusListener(new GpsStatus.Listener() {
            @Override
            public void onGpsStatusChanged(int i) {
                switch (i) {
                    case GpsStatus.GPS_EVENT_STARTED:
                        gpsStatusView.setText("Starting GPS");
                        gpsStatusView.setTextColor(Color.LTGRAY);
                        break;
                    case GpsStatus.GPS_EVENT_FIRST_FIX:
                        gpsStatusView.setText("Found GPS Satellites");
                        gpsStatusView.setTextColor(Color.LTGRAY);
                        break;
                    case GpsStatus.GPS_EVENT_SATELLITE_STATUS:

                        if (lastLocation == null){
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

                        break;
                }
            }
        });

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                lastLocation = location;
                new PostLocationTask(RightRidesActivity.this).execute(location);
                Log.d("gps", "new location->" + location);
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {
            }

            @Override
            public void onProviderEnabled(String s) {
            }

            @Override
            public void onProviderDisabled(String s) {
            }
        };

        toggleBroadcastBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean on = ((ToggleButton) view).isChecked();

                if (on) {
                    startGpsLocationListener(locationListener);
                    Toast.makeText(RightRidesActivity.this, "GPS Tracking On", Toast.LENGTH_SHORT).show();
                    Log.d("rightrides|togglegps", "GPS Turned on manually");
                } else {
                    locationManager.removeUpdates(locationListener);
                    Toast.makeText(RightRidesActivity.this, "GPS Tracking Off", Toast.LENGTH_SHORT).show();
                    Log.d("rightrides|togglegps", "GPS Turned off manually");
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        startGpsLocationListener(locationListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        locationManager.removeUpdates(locationListener);
    }

    private void startGpsLocationListener(LocationListener locationListener) {
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                GPS_MIN_UPDATE_DELAY,
                GPS_MIN_MOVEMENT_UPDATE,
                locationListener);
    }
}