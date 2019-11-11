package com.wozzytheprogrammer.kwproperty.Utils;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.firebase.geofire.GeoFire;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * On App Killed disconnects driver from the database when app is closed
 */
public class onAppKilled extends Service {

    void disconnectAgent(){
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("agentsAvailable");
        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(userId);


        DatabaseReference refWorking = FirebaseDatabase.getInstance().getReference("agentsWorking");
        GeoFire geoFireWorking = new GeoFire(refWorking);
        geoFireWorking.removeLocation(userId);
    }
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        //Code here
        disconnectAgent();
    }
}

