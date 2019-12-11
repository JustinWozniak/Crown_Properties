package com.wozzytheprogrammer.kwproperty.Customer;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.wozzytheprogrammer.kwproperty.Chat.ChatMainActivity;
import com.wozzytheprogrammer.kwproperty.R;

import java.util.HashMap;
import java.util.Map;

public class FindAgentsActivity extends AppCompatActivity {
    private ProgressDialog loadingBar;
    Button findAgentButton;


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

    private Boolean agentFound = false;
    int MAX_SEARCH_DISTANCE = 100000;


    private void findAnAgent() {


        loadingBar.setTitle("Locating Real Estate Agent...");
        loadingBar.setMessage("Please wait, we are scanning your location...");
        loadingBar.show();

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference customerLocationRef = FirebaseDatabase.getInstance().getReference("Users/Customers/" + uid + "/location");

        DatabaseReference agentLocation = FirebaseDatabase.getInstance().getReference().child("agentsAvailable");


        customerLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Log.e("datasnap", String.valueOf(dataSnapshot));
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        Double customersLat = (Double) dataSnapshot.child("Lat:").getValue();
                        Double customersLong = (Double) dataSnapshot.child("Long:").getValue();
                        Log.e("customersLat", String.valueOf(customersLat));
                        Log.e("customersLong", String.valueOf(customersLong));
                        if (customersLat != null) {
                            GeoFire geoFire = new GeoFire(agentLocation);
                            GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(customersLat, customersLong), MAX_SEARCH_DISTANCE);

                            Handler handler = new Handler();
                            int delay = 5000; //milliseconds

                            handler.postDelayed(new Runnable() {
                                public void run() {
                                    if (!agentFound) {
                                        loadingBar.setTitle("No Agents Online!!!!!");
                                        loadingBar.setMessage("Please Try Again Later...");
                                        geoQuery.removeAllListeners();

                                        return;
                                    }
                                    handler.postDelayed(this, delay);
                                }
                            }, delay);



                            geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
                                @Override
                                public void onKeyEntered(String key, GeoLocation location) {
                                    Map customersId = new HashMap();
                                    Map agentsId = new HashMap();
                                    DatabaseReference foundAgent = FirebaseDatabase.getInstance().getReference().child("agentsAvailable").child(key);
                                    DatabaseReference connectedPeople = FirebaseDatabase.getInstance().getReference().child("connected").child(key);
                                    DatabaseReference wantsConnection = FirebaseDatabase.getInstance().getReference().child("connected").child(key).child("wants Connection");
                                    DatabaseReference connectionsAGo = FirebaseDatabase.getInstance().getReference().child("connected").child(key).child("wants Connection").child("connectCustomer");
                                    Log.e("key", String.valueOf(key));
                                    Log.e("location", String.valueOf(location));
                                    agentFound = true;
                                    customersId.put("conectedCustomersId", String.valueOf(uid));
                                    agentsId.put("connectedagentsid", foundAgent);
                                    loadingBar.setTitle("Agent found!!!!!");
                                    loadingBar.setMessage("Please wait, we are contacting your agent!...");
                                    connectedPeople.child("customerFound").updateChildren(customersId);
                                    Log.e("data", String.valueOf(dataSnapshot));

                                    wantsConnection.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                            Log.e("NEW DATASNAP", String.valueOf(dataSnapshot));
                                            Log.e("getval", String.valueOf(dataSnapshot.getValue()));

                                            for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                                                connectionsAGo.addValueEventListener(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                        Intent intent = new Intent(FindAgentsActivity.this, ChatMainActivity.class);
                                                        startActivity(intent);
                                                        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                                    }
                                                });
                                                Log.e("childSnapshot", String.valueOf(childSnapshot.getValue()));
                                                Log.e("getchildren", String.valueOf(dataSnapshot.getChildren()));
                                                if (childSnapshot.getValue() == "yes") {

                                                }
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

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

                                }

                                @Override
                                public void onGeoQueryError(DatabaseError error) {
                                    Log.e("key", String.valueOf(error));
                                    loadingBar.setTitle("No Agents Online!!!!!");
                                    loadingBar.setMessage("Please Try Again Later...");
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