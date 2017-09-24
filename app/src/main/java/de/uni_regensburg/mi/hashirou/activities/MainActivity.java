package de.uni_regensburg.mi.hashirou.activities;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.UUID;

import de.uni_regensburg.mi.hashirou.R;

import de.uni_regensburg.mi.hashirou.extras.Keys;
import de.uni_regensburg.mi.hashirou.services.LocationTrackerService;

public class MainActivity extends AppCompatActivity {

    boolean currentlyRunning = false;

    private LocationRequest mLocationRequest;

    private Button btn_startRun;
    private Button btn_stopRun;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initUI();
        createLocationRequest();
    }

    private void initUI(){
        btn_startRun = (Button) findViewById(R.id.btn_startRun);
        btn_stopRun = (Button) findViewById(R.id.btn_stopRun);
        TextView currentSpeed = (TextView) findViewById(R.id.currentSpeed);

        btn_startRun.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                    handleUserSettings();
                }
                else{
                    askforLocationPermission();
                }
            }
        });

        btn_stopRun.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentlyRunning = false;
                btn_startRun.setVisibility(View.VISIBLE);
                btn_stopRun.setVisibility(View.GONE);
                MainActivity.this.stopService(new Intent(MainActivity.this,LocationTrackerService.class));
            }
        });
    }

    private void startRun(){
        currentlyRunning = true;
        btn_startRun.setVisibility(View.GONE);
        MainActivity.this.btn_stopRun.setVisibility(View.VISIBLE);
        //this starts the location tracking
        startService(new Intent(this, LocationTrackerService.class));
    }

    ////////////////////////////
    /// permissions handling ///
    ////////////////////////////

    private void askforLocationPermission(){
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Keys.MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);

            } else {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Keys.MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case Keys.MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    handleUserSettings();
                } else {
                    Toast.makeText(this,"This app needs to access your location in order to work.",Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

    ////////////////////////////
    ///// settings handling ////
    ////////////////////////////

    private void handleUserSettings(){
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest);
        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                startRun();
            }
        });

        client.checkLocationSettings(builder.build());

        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                int statusCode = ((ApiException) e).getStatusCode();
                switch (statusCode) {
                    case CommonStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied, but this can be fixed
                        // by showing the user a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            ResolvableApiException resolvable = (ResolvableApiException) e;
                            resolvable.startResolutionForResult(MainActivity.this, Keys.REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException sendEx) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way
                        // to fix the settings so we won't show the dialog.
                        break;
                }
            }
        });
    }

    protected void createLocationRequest(){
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(Keys.LOCATION_REQUEST_INTERVAL);
        mLocationRequest.setFastestInterval(Keys.LOCATION_REQUEST_FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(resultCode == -1){
            startRun();
        }
        super.onActivityResult(requestCode,resultCode,data);
    }

    ////////////////////////////
    ///// location handling ////
    ////////////////////////////

    public void sendVolleyRequest(Integer method, String url, final Map<String, String> params) {
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest postRequest = new StringRequest(method, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        // response
                        Log.d("Response", response);
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Log.d("Error.Response", String.valueOf(error));
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                return params;
            }
        };
        queue.add(postRequest);
    }

    /*private void getAllDatabaseValues() {

        //Cursor cursor = MyDatabaseAdapter.getAllLocations();

        /*String[] projection = {
                FeedEntry._ID,
                FeedEntry.COLUMN_NAME_TIMESTAMP,
                FeedEntry.COLUMN_NAME_LOCATION_LAT,
                FeedEntry.COLUMN_NAME_LOCATION_LNG,
                FeedEntry.COLUMN_NAME_CURRENT_HEIGHT,
                FeedEntry.COLUMN_NAME_CURRENT_SPEED
        };

        Cursor cursor = db.query(
                FeedEntry.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                null
        );


        if(cursor.getCount()==0){
            return;
        }

        List<Double> altitudes = new ArrayList<>();
        List<Double> locations_lat = new ArrayList<>();
        List<Double> locations_lng = new ArrayList<>();

        while(cursor.moveToNext()) {
            double location_lat = cursor.getDouble(cursor.getColumnIndex(FeedEntry.COLUMN_NAME_LOCATION_LAT));
            double location_lng = cursor.getDouble(cursor.getColumnIndex(FeedEntry.COLUMN_NAME_LOCATION_LNG));
            double height = cursor.getDouble(cursor.getColumnIndex(FeedEntry.COLUMN_NAME_CURRENT_HEIGHT));

            altitudes.add(height);
            locations_lat.add(location_lat);
            locations_lng.add(location_lng);
        }
        cursor.close();

        de.uni_regensburg.mi.hashirou.location.LocationManager manager = new de.uni_regensburg.mi.hashirou.location.LocationManager();
        int points = manager.calculatePoints(altitudes, locations_lat, locations_lng);

        Map<String, String> params = getParams(points);

        sendVolleyRequest(Request.Method.POST, "https://keck.bluelightengine.de/api/android/index.php", params);
    }*/

    private Map<String, String> getParams(int points) {
        Map<String, String> params = new HashMap<>();

        params.put("uuid", getUUID());
        params.put("points", String.valueOf(points));

        Log.d("NETWORK", params.toString());

        return params;
    }

    public String getUUID() {
        return UUID.randomUUID().toString();
    }
}
