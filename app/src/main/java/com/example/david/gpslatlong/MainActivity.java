package com.example.david.gpslatlong;

import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.internal.BinderWrapper;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class MainActivity
        extends AppCompatActivity
        implements GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks,
        LocationListener
{
    // vars
    private GoogleApiClient mapsapi;
    private LocationRequest locreq;
    private TextView gps_box;
    private TextView gps_other;
    private TextView gps_time_text;
    private FileOutputStream out;

    private boolean marknext = false;

    private long gps_interval = 1000;

    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        gps_box = (TextView)findViewById(R.id.gps_box);
        gps_other = (TextView)findViewById(R.id.gps_other);
        gps_time_text = (TextView)findViewById(R.id.gps_time);

        mapsapi = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        locreq = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(gps_interval)
                .setFastestInterval(gps_interval);

        String dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
        File logfile = new File(dir,"gpslog");
        try {
            out = new FileOutputStream(logfile);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.i("TAG",dir);


    }
    @Override
    protected void onStart() {
        super.onStart();
    }
    @Override
    protected void onResume() {
        super.onResume();
        mapsapi.connect();
    }
    @Override
    protected void onPause() {
        super.onPause();
        if (mapsapi.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mapsapi,this);
            mapsapi.disconnect();
        }
    }
    @Override
    protected void onStop() {
        super.onStop();
        try {
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Location location = LocationServices.FusedLocationApi.getLastLocation(mapsapi);
        LocationServices.FusedLocationApi.requestLocationUpdates(mapsapi, locreq, this);
        if (location == null) {

        } else {
            loc_callback(location,false);
        }
    }
    @Override
    public void onConnectionSuspended(int i) {

    }
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            Log.i("TAGERINO", "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }
    private void loc_callback (Location location, boolean mark) {
        double gps_lat = location.getLatitude();
        double gps_long = location.getLongitude();
        double gps_alt = location.getAltitude();
        long gps_time = location.getTime();

        String str_lat = String.valueOf(gps_lat);
        String str_long = String.valueOf(gps_long);
        String str_alt = String.valueOf(gps_alt);
        String str_time = String.valueOf(gps_time);

        String str_lat_long = str_lat + "," + str_long;



        gps_box.setText(str_lat_long);
        gps_other.setText(str_alt);
        gps_time_text.setText(str_time);

        String log_str;
        if (mark) {
            log_str = str_time + "," + str_lat + "," + str_long + "," + "MARK" + "\n";
            Log.i("GPS",str_lat_long+"====MARK");
        }
        else {
            log_str =  str_time + "," + str_lat + "," + str_long + "," + "NOMARK" + "\n";
            Log.i("GPS",str_lat_long);
        }
        try {
            out.write(log_str.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
        marknext = false;
    }

    @Override
    public void onLocationChanged(Location location) {
        if (marknext) {
            loc_callback(location,true);
        }
        else {
            loc_callback(location,false);
        }
    }

    public void markButton(View view) {
        marknext = true;
    }
}
