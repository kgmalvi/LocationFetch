package com.example.kiran.locationfetch;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.List;

import static com.google.android.gms.location.LocationSettingsRequest.*;

/**
 * Copyright (C) Kiran G. Malvi - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Kiran Malvi <kiran.gmalvi@gmail.com>, Nov 2018
 */


public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {


    static Context context;

    static Location location = null;
    static Double latitude, longitude;
    static int locationFetchCounter = 0;


    static LocationManager mlocManager;
    static GoogleApiClient googleApiClient;
    static LocationListener mlocListener;

    /**
     * 1] Requesting location permission.
     * 2] If permission granted then checking if GPS is enabled or not.
     * 3] If GPS not enabled, then switching on the GPS.
     * 4] Finally calling location listener and fetching user's current location.
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = this;
        requestPermission();
    }

    private void requestPermission() {
        Dexter.withActivity(MainActivity.this)
                .withPermissions(Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            logMessage("All permissions given");
                            new fetchLocation().execute();
                        } else if (report.isAnyPermissionPermanentlyDenied()) {
                            logMessage("Some permissions denied.");
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {

                    }
                }).check();
    }

    private void enableGPS() {
        mlocManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
        // getting GPS status
        boolean isGPSEnabled = mlocManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetworkEnabled = mlocManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (!isGPSEnabled /*& !isNetworkEnabled*/) {
            /**
             * Code to enable GPS
             * Below is part in which GPS can be enable directly from app.
             * */

            if (googleApiClient == null) {
                googleApiClient = new GoogleApiClient.Builder(this)
                        .addApi(LocationServices.API).addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(MainActivity.this).build();
                googleApiClient.connect();
                LocationRequest locationRequest = LocationRequest.create();
                locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                locationRequest.setInterval(1000);
                locationRequest.setFastestInterval(5 * 1000);
                Builder builder = new Builder()
                        .addLocationRequest(locationRequest);

                // **************************
                builder.setAlwaysShow(true); // this is the key ingredient
                // **************************

                PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi
                        .checkLocationSettings(googleApiClient, builder.build());
                result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                    @Override
                    public void onResult(LocationSettingsResult result) {
                        final Status status = result.getStatus();
                        final LocationSettingsStates state = result
                                .getLocationSettingsStates();
                        switch (status.getStatusCode()) {
                            case LocationSettingsStatusCodes.SUCCESS:
                                // All location settings are satisfied. The client can
                                // initialize location
                                // requests here.
                                break;
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                // Location settings are not satisfied. But could be
                                // fixed by showing the user
                                // a dialog.
                                try {
                                    // Show the dialog by calling
                                    // startResolutionForResult(),
                                    // and check the result in onActivityResult().
                                    status.startResolutionForResult(MainActivity.this, 1000);
                                } catch (IntentSender.SendIntentException e) {
                                    // Ignore the error.
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                // Location settings are not satisfied. However, we have
                                // no way to fix the
                                // settings so we won't show the dialog.
                                break;
                        }
                    }
                });
            }
        }
    }

    private class fetchLocation extends AsyncTask<Void, Void, Location> {
        @Override
        protected void onPreExecute() {

            super.onPreExecute();
        }

        @Override
        protected Location doInBackground(Void... voids) {
            if (Looper.myLooper() == null)
                Looper.prepare();

            enableGPS();
            Location location = getLocation(mlocManager);

           /* location = getLocation(mlocManager);

            latitude = 0.0;
            longitude = 0.0;

            if (location == null) {
                FusedLocationProviderClient mFusedLocationClient;
                mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                }
                mFusedLocationClient.getLastLocation()
                        .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location mLocation) {
                                Log.e("Location", "success");
                                location = mLocation;
                                // Got last known location. In some rare situations this can be null.
                                if (mLocation != null) {
                                    Log.e("Location", "lat: " + mLocation.getLatitude() + " lng: " + mLocation.getLongitude() + "");
                                    latitude = mLocation.getLatitude();
                                    longitude = mLocation.getLongitude();
                                }
                            }
                        })
                        .addOnFailureListener(this, new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                e.printStackTrace();
                                location = getLocation(mlocManager);
                                Log.e("Location", "fail");
                            }
                        });
            } else if (location != null) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
            }*/

            return location;
        }

        @Override
        protected void onPostExecute(Location location) {
            super.onPostExecute(location);
            if (location == null) {
                if (locationFetchCounter < 5) {
                    new fetchLocation().execute();
                    Log.e("LocationService", "Fetching location " + locationFetchCounter + " attempt.");
                    locationFetchCounter++;
                } else {
                    Log.e("LocationService", "Max limit exceeded, exiting app.");
                    final AlertDialog alertDialog = new AlertDialog.Builder(context)
                            .setTitle("Location not available.")
                            .setMessage("Device location not found, please calibrate the device " +
                                    "and try again. If still problem continue then restart the" +
                                    " device or open google map and try to find location first" +
                                    " and then try again.")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    new fetchLocation().execute();
                                    dialog.dismiss();
                                }
                            }).show();
                }
            } else {
                Log.e("Location", "Latitiude: " + location.getLatitude()
                        + "Logitude: " + location.getLongitude());
                mlocManager.removeUpdates(mlocListener);
            }
        }
    }

    public Location getLocation(LocationManager mlocManager) {
        try {
            long MIN_TIME_BW_UPDATES = 1; // 1 minute

            // The minimum distance to change Updates in meters
            long MIN_DISTANCE_CHANGE_FOR_UPDATES = 1; // 10 meters

           /* if (ActivityCompat.checkSelfPermission(this, mPermission[2])
                    != MockPackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(this, mPermission[3])
                            != MockPackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, mPermission, REQUEST_CODE_PERMISSION);
            }*/

            // mlocManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
            mlocListener = new MyLocationListener();

            // getting GPS status
            boolean isGPSEnabled = mlocManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
            boolean isNetworkEnabled = mlocManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                // no network provider is enabled
                Log.e("LocationListener", "No GPS or network available to provide location.");
            } else {

                // First get location from Network Provider
                if (isNetworkEnabled) {

                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        Log.e("LocationPermission", "Location Permission not given");
                    }
                    mlocManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, mlocListener);
                    mlocManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, mlocListener, Looper.getMainLooper());
                    Log.d("Network", "Network");
                    if (mlocManager != null) {
                        location = mlocManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    }
                    // }
                }
                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled) {
                    if (location == null) {
                        mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, mlocListener);
                        //mlocManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, mlocListener, Looper.getMainLooper());
                        Log.d("GPS Enabled", "GPS Enabled");
                        if (mlocManager != null) {
                            location = mlocManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return location;
    }

    public class MyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location loc) {
            location = loc;
        }

        public void onProviderDisabled(String provider) {
            //nothin
        }


        public void onProviderEnabled(String provider) {
            //nothin
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
            //nothin
        }
    }/* End of Class MyLocationListener */

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void logMessage(String message) {
        Log.e("LocationMessage", message);
    }
}
