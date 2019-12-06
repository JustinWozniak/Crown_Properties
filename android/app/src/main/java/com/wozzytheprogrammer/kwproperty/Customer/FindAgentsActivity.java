package com.wozzytheprogrammer.kwproperty.Customer;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.wozzytheprogrammer.kwproperty.R;

import java.util.Map;

public class FindAgentsActivity extends AppCompatActivity {
    private ProgressDialog loadingBar;
    Button findAgentButton;

    private LatLng customersLocation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_agents);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setupPage();


    }

    private void setupPage() {
        loadingBar = new ProgressDialog(this);
        findAgentButton = findViewById(R.id.find_agent_confirm);

        findAgentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                findAnAgent();
            }
        });

    }

    private int radius = 1;
    private Boolean agentFound = false;
    int MAX_SEARCH_DISTANCE = 1000;
    boolean requestBol = false;

    GeoQuery geoQuery;

    private void findAnAgent() {


        loadingBar.setTitle("Locating Real Estate Agent...");
        loadingBar.setMessage("Please wait, we are scanning your location...");
        loadingBar.show();

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference customerLocationRef = FirebaseDatabase.getInstance().getReference("Users/Customers/" + uid + "/location");

        DatabaseReference agentLocation = FirebaseDatabase.getInstance().getReference().child("agentsAvailable");

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users/Customers/" + uid + "/location");
        GeoFire geoFire = new GeoFire(ref);

        customerLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        Double customersLat = (Double) dataSnapshot.child("Lat:").getValue();
                        Double customersLong = (Double) dataSnapshot.child("Long:").getValue();
                        if (customersLat != null) {
                            GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(customersLat, customersLong), MAX_SEARCH_DISTANCE);


                            geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
                                @Override
                                public void onKeyEntered(String key, GeoLocation location) {
                                    DatabaseReference agentAvailable = FirebaseDatabase.getInstance().getReference().child("agentsAvailable");
                                    Log.e("agentAvailable", String.valueOf(agentAvailable));
                                    agentAvailable.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.exists()) {
                                                Map<String, Object> agentMap = (Map<String, Object>) dataSnapshot.getValue();
                                                Log.e("MAP", String.valueOf(agentMap));

                                                agentFound = true;
                                                loadingBar.setTitle("Agent Found!...");
                                                loadingBar.setMessage("Please wait, while we contact them!...");

                                            }   else    {
                                                agentFound = false;
                                                loadingBar.setTitle("No Agents Online!...");
                                                loadingBar.setMessage("Please Try Again Later!...");
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {
                                        }
                                    });
                                }

                                @Override
                                public void onKeyExited(String key) {

                                }

                                @Override
                                public void onKeyMoved(String key, GeoLocation location) {

                                }

                                @Override

                                public void onGeoQueryReady() {
                                    Log.e("ready", String.valueOf(MAX_SEARCH_DISTANCE));
                                }

                                @Override
                                public void onGeoQueryError(DatabaseError error) {
                                }
                            });
                        }

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }

        });

    }}