package com.ericksilva.cuacuapp.services;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.ericksilva.cuacuapp.activities.alert.AlertActivity;
import com.ericksilva.cuacuapp.models.Cuac;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class TrackerService extends Service {

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocationClient;
    private Handler handler;
    private Runnable runnable;
    private CollectionReference cuacsRef;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String uid;

    private List<Cuac> cuacList = new ArrayList<>();

    private void restartWaiting(){
        Log.i("TRACKER", "RESTAR WAITING");
        if (handler != null && runnable != null)
            handler.removeCallbacks(runnable);
        runnable = null;
        handler.postDelayed(runnable = new Runnable() {
            @Override
            public void run() {
                sendLocationRequest();
                Log.i("TRACKER", "ACTUALY RESTARTING");
                restartWaiting();
                startService(new Intent(getApplicationContext(), TrackerService.class));

            }
        },5*60*1000);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Log.i("TRACKER", "ON START");
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        handler = new Handler();
        Log.i("TRACKER", "CREATE");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(mConnectionCallbacks)
                .addOnConnectionFailedListener(mConnectionFailedCallbacks)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
        restartWaiting();

        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        cuacsRef = db.collection("users/"+ uid +"/cuacs");
        Query query = cuacsRef.whereEqualTo("type", "geo");
        query.addSnapshotListener(mFireStoreListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("TRACKER","ON DESTROY");
    }

    //Service
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        mGoogleApiClient.connect();
        Log.i("TRACKER", "BINDER");
        return null;
    }

    private GoogleApiClient.ConnectionCallbacks mConnectionCallbacks = new GoogleApiClient.ConnectionCallbacks() {
        @Override
        public void onConnected(@Nullable Bundle bundle) {
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());
            sendLocationRequest();
        }

        @Override
        public void onConnectionSuspended(int i) {
            Log.e("TRACKER", "No conecto");
        }
    };

    private void sendLocationRequest(){

        if (mLocationRequest != null){
            mFusedLocationClient.removeLocationUpdates(mLocatrionCallback);
        }

        if(mLocationRequest == null) {
            mLocationRequest = LocationRequest.create();
            mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
            mLocationRequest.setInterval(5 * 1000);
            mLocationRequest.setFastestInterval(2 * 1000);
        }
        if (ActivityCompat.checkSelfPermission(TrackerService.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(TrackerService.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.w("TRACKER", "NoTeniaPermisos");
            return;
        }
        Log.w("TRACKER", "SEND LOCATION REQUEST");
        mFusedLocationClient.requestLocationUpdates(mLocationRequest,mLocatrionCallback,null);
    }

    private LocationCallback mLocatrionCallback = new LocationCallback(){

        @Override
        public void onLocationResult(LocationResult locationResult) {
            restartWaiting();
            Location myPosition = locationResult.getLastLocation();
            Log.e("LOCATION","geo:" + myPosition.getLatitude() + "," + myPosition.getLongitude());

            for (Cuac cuac:cuacList){

                if (cuac.point !=null){
                    double distance = latLonDistance(myPosition,cuac.point);

                    Log.e("LOCATION", "--------------------");
                    Log.e("LOCATION", "point:" + cuac.point);
                    Log.e("LOCATION", "radio:" + cuac.radius);
                    Log.e("LOCATION", "distance:" + distance);
                    Log.e("LOCATION", "--------------------");

                    if (distance <= cuac.radius){
                        Date now = new Date();
                        if (cuac.lastCuac == null || cuac.lastCuac.getTime() + 30*60*1000 < now.getTime()){
                            Log.w("CUAC!", "Estamos en un cuac!");

                            Intent i = AlertActivity.createIntent(getApplicationContext(),cuac);
                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            getApplicationContext().startActivity(i);
                        }
                    }
                }
            }
        }
    };

    private GoogleApiClient.OnConnectionFailedListener mConnectionFailedCallbacks = new GoogleApiClient.OnConnectionFailedListener() {
        @Override
        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
            int code = connectionResult.getErrorCode();
            String message = connectionResult.getErrorMessage();
            Log.e("TRACKER","Fallo la conexion a google: " + code + ": " + message);
        }
    };

    EventListener mFireStoreListener = new EventListener<QuerySnapshot>() {
        @Override
        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
            cuacList.clear();

            if (e!= null) Log.e("FireError",e.getLocalizedMessage());
            if (queryDocumentSnapshots == null || queryDocumentSnapshots.isEmpty()) return;

            for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()){
                Cuac cuac = new Cuac(documentSnapshot);
                cuac.key(documentSnapshot.getId());
                cuacList.add(cuac);
            }
        }
    };

    private double degreesToRadians(double degrees) {
        return degrees * Math.PI / 180;
    }


    private double latLonDistance(Location loc1, GeoPoint loc2){

        double earthRadiusKm = 6371;
        double dLat = degreesToRadians(loc1.getLatitude() - loc2.getLatitude());
        double dLon = degreesToRadians(loc1.getLongitude() - loc2.getLongitude());
        double latRad1 = degreesToRadians(loc1.getLatitude());
        double latRad2 = degreesToRadians(loc2.getLatitude());

        double a = Math.sin(dLat/2) * Math.sin(dLat/2) + Math.sin(dLon/2) * Math.sin(dLon/2) * Math.cos(latRad1) * Math.cos(latRad2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return earthRadiusKm * c * 1000;
    }
}
