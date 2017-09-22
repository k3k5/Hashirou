package de.uni_regensburg.mi.hashirou.activities;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.UUID;

import de.uni_regensburg.mi.hashirou.R;
import de.uni_regensburg.mi.hashirou.database.DatabaseAdapter;
import de.uni_regensburg.mi.hashirou.database.DatabaseContract.FeedEntry;

public class MainActivity extends AppCompatActivity {

    boolean currentlyRunning = false;
    DatabaseAdapter MyDatabaseAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button btn_startRun = (Button) findViewById(R.id.btn_startRun);
        final Button btn_stopRun = (Button) findViewById(R.id.btn_stopRun);
        final TextView currentSpeed = (TextView) findViewById(R.id.currentSpeed);

        MyDatabaseAdapter = new DatabaseAdapter(this);

        //opening connection to db is expensive => keep it open
        MyDatabaseAdapter.open();

        btn_startRun.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentlyRunning = true;
                btn_startRun.setVisibility(View.GONE);
                btn_stopRun.setVisibility(View.VISIBLE);
            }
        });

        btn_stopRun.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentlyRunning = false;
                btn_startRun.setVisibility(View.VISIBLE);
                btn_stopRun.setVisibility(View.GONE);
                getAllDatabaseValues();
            }
        });

        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                saveNewLocationToDatabase(location, currentSpeed);
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        if(currentlyRunning) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        }
    }

    private void saveNewLocationToDatabase(Location location, TextView currentSpeed) {
        Log.d("Position", location.toString());

        MyDatabaseAdapter.insertLocation(getCurrentTimeStamp(), location);
    }

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

    public String getCurrentTimeStamp() {
        Long tsLong = System.currentTimeMillis()/1000;
        return tsLong.toString();
    }

    private void getAllDatabaseValues() {

        Cursor cursor = MyDatabaseAdapter.getAllLocations();

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
        */

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
    }

    private Map<String, String> getParams(int points) {
        Map<String, String> params = new HashMap<>();

        params.put("uuid", getUUID());
        params.put("points", String.valueOf(points));

        Log.d("NETWORK", params.toString());

        return params;
    }

    @Override
    protected void onDestroy() {
        MyDatabaseAdapter.close();
        super.onDestroy();
    }

    public String getUUID() {
        return UUID.randomUUID().toString();
    }
}
