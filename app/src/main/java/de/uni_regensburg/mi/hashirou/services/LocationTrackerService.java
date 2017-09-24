package de.uni_regensburg.mi.hashirou.services;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import de.uni_regensburg.mi.hashirou.database.DatabaseAdapter;
import de.uni_regensburg.mi.hashirou.extras.Keys;

/**
 * Created by Raphael on 24/09/2017.
 */

public class LocationTrackerService extends Service{

    private static final String TAG = "MyLocationService";

    private DatabaseAdapter mDatabaseAdapter;

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand");
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onCreate() {
       mDatabaseAdapter = new DatabaseAdapter(LocationTrackerService.this);

       mDatabaseAdapter.open();
       createLocationRequest();

       mLocationCallback = new LocationCallback(){
       @Override
       public void onLocationResult(LocationResult locationResult){
           for(Location location : locationResult.getLocations()){
               Log.e(TAG,"Location saved");
               mDatabaseAdapter.insertLocation(String.valueOf(location.getTime()),location);
           }
       }
       };

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(LocationTrackerService.this);

        if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(new OnSuccessListener<Location>() {
                                @Override
                                public void onSuccess(Location location) {
                                    // Got last known location. In some rare situations this can be null.
                                    // if (location != null) {
                                        mDatabaseAdapter.insertLocation(String.valueOf(location.getTime()),location);
                                    }
                            });
            startLocationUpdates();
        }

        Log.e(TAG,"onCreate");
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "onDestroy");
        stopLocationUpdates();
        mDatabaseAdapter.close();
        super.onDestroy();
    }

    private void createLocationRequest(){
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(Keys.LOCATION_REQUEST_INTERVAL);
        mLocationRequest.setFastestInterval(Keys.LOCATION_REQUEST_FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void startLocationUpdates(){
        if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
        }
    }

    private void stopLocationUpdates(){
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }
}
