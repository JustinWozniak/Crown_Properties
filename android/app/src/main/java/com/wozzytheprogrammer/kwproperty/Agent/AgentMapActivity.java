package com.wozzytheprogrammer.kwproperty.Agent;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.TransportMode;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.model.Route;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.wozzytheprogrammer.kwproperty.Adapters.AgentsCustomInfoWindowAdapter;
import com.wozzytheprogrammer.kwproperty.Login.LauncherActivity;
import com.wozzytheprogrammer.kwproperty.Objects.AgentObject;
import com.wozzytheprogrammer.kwproperty.Objects.RideObject;
import com.wozzytheprogrammer.kwproperty.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;


/**
 * Main Activity displayed to the agent
 */
public class AgentMapActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, DirectionCallback, RoutingListener {

    Location mLastLocation;
    LocationRequest mLocationRequest;
    DatabaseReference mUser;
    RideObject mCurrentRide;
    Marker pickupMarker, destinationMarker;
    AgentObject mAgent = new AgentObject();
    TextView mUsername;
    ImageView mProfileImage;
    View mBottomSheet;
    BottomSheetBehavior mBottomSheetBehavior;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationClient;
    private Button mRideStatus, mMaps;
    private Switch mWorkingSwitch;
    private int status = 0;
    private LinearLayout mCustomerInfo, mBringUpBottomLayout;
    private ImageView mCustomerProfileImage;
    private TextView mCustomerName, mLocation;
    private Boolean isWorking = false;

    /**
     * Listen for the customerRequest Node to see if the agent ended it
     * in the mean time.
     */
    private DatabaseReference driveHasEndedRef;
    private ValueEventListener driveHasEndedRefListener;
    private List<Polyline> polylines;
    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            if (locationResult == null) {
                return;
            }

            for (Location location : locationResult.getLocations()) {
                if (getApplicationContext() != null) {

                    String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    DatabaseReference refAvailable = FirebaseDatabase.getInstance().getReference("agentsAvailable");
                    DatabaseReference refWorking = FirebaseDatabase.getInstance().getReference("agentsWorking");
                    GeoFire geoFireAvailable = new GeoFire(refAvailable);
                    GeoFire geoFireWorking = new GeoFire(refWorking);

                    if (!mWorkingSwitch.isChecked()) {
                        geoFireWorking.removeLocation(userId, (key, error) -> {
                        });

                        return;
                    }

                    if (mCurrentRide != null && mLastLocation != null && location != null) {
                        mCurrentRide.setRideDistance(mCurrentRide.getRideDistance() + mLastLocation.distanceTo(location) / 1000);
                    }
                    mLastLocation = location;

                    Map newUserMap = new HashMap();
                    newUserMap.put("last_updated", ServerValue.TIMESTAMP);
                    mUser.updateChildren(newUserMap);

                    if (mCurrentRide == null) {
                        geoFireWorking.removeLocation(userId, (key, error) -> {
                        });
                        if (mWorkingSwitch.isChecked()) {
                            geoFireAvailable.setLocation(userId, new GeoLocation(location.getLatitude(), location.getLongitude()), (key, error) -> {
                            });
                        }
                    } else {

                        geoFireAvailable.removeLocation(userId, (key, error) -> {
                        });
                        geoFireWorking.setLocation(userId, new GeoLocation(location.getLatitude(), location.getLongitude()), (key, error) -> {
                        });
                    }
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        Toolbar toolbar = findViewById(R.id.toolbar);

        polylines = new ArrayList<>();


        final DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);


        mUser = FirebaseDatabase.getInstance().getReference().child("Users").child("Agents").child(FirebaseAuth.getInstance().getUid());
        mCustomerInfo = findViewById(R.id.customerInfo);

        mCustomerProfileImage = findViewById(R.id.customerProfileImage);
        mBringUpBottomLayout = findViewById(R.id.bringUpBottomLayout);

        mCustomerName = findViewById(R.id.name);
        mUsername = navigationView.getHeaderView(0).findViewById(R.id.usernameDrawer);
        mProfileImage = navigationView.getHeaderView(0).findViewById(R.id.imageViewDrawer);

        mWorkingSwitch = findViewById(R.id.workingSwitch);
        mWorkingSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!mAgent.getActive()) {
                Toast.makeText(AgentMapActivity.this, R.string.not_approved, Toast.LENGTH_LONG).show();
                mWorkingSwitch.setChecked(false);
                return;
            }
            if (isChecked) {
                connectAgent();
            } else {
                disconnectAgent();
            }
        });

        mRideStatus = findViewById(R.id.rideStatus);

        mRideStatus.setOnClickListener(v -> {
            switch (status) {
                case 1:
                    if (mCurrentRide == null) {
                        endRide();
                        return;
                    }
                    status = 2;
                    erasePolylines();
                    if (mCurrentRide.getDestination().getCoordinates().latitude != 0.0 && mCurrentRide.getDestination().getCoordinates().longitude != 0.0) {
                        getRouteToMarker(mCurrentRide.getDestination().getCoordinates());
                    }
                    mRideStatus.setText(R.string.agent_complete);
                    mLocation.setText(mCurrentRide.getDestination().getName());

                    break;
                case 2:
                    if (mCurrentRide != null)
                        mCurrentRide.recordRide();
                    endRide();
                    break;
            }
        });


        ImageView mDrawerButton = findViewById(R.id.drawerButton);
        mDrawerButton.setOnClickListener(v -> drawer.openDrawer(Gravity.LEFT));

        mBringUpBottomLayout = findViewById(R.id.bringUpBottomLayout);
        mBringUpBottomLayout.setOnClickListener(v -> {
            if (mBottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED)
                mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            else
                mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

            if (status == 0) {
                mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            }
        });

        getUserData();
        getAssignedCustomer();

        ViewTreeObserver vto = mBringUpBottomLayout.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(() -> initializeBottomLayout());

    }

    /**
     * Listener for the bottom popup. This will control
     * when it is shown and when it isn't according to the actions of the users
     * of pulling on it or just clicking on it.
     */
    private void initializeBottomLayout() {
        mBottomSheet = findViewById(R.id.bottomSheet);
        mBottomSheetBehavior = BottomSheetBehavior.from(mBottomSheet);
        mBottomSheetBehavior.setHideable(true);
        mBottomSheetBehavior.setPeekHeight(mBringUpBottomLayout.getHeight());


        mBottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (status == 0) {
                    mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });

    }

    /**
     * Fetches current user's info and populates the design elements
     */
    private void getUserData() {
        String agentId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference assignedCustomerRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Agents").child(agentId);
        assignedCustomerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    mAgent.parseData(dataSnapshot);

                    mUsername.setText(mAgent.getName());
                    if (!mAgent.getProfileImage().equals("default"))
                        Glide.with(getApplication()).load(mAgent.getProfileImage()).apply(RequestOptions.circleCropTransform()).into(mProfileImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        FirebaseDatabase.getInstance().getReference("agentsAvailable").child(agentId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    connectAgent();
                } else
                    disconnectAgent();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        FirebaseDatabase.getInstance().getReference("agentsWorking").child(agentId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                    connectAgent();
                } else
                    disconnectAgent();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    /**
     * Is always listening to the ride_info table to see if the current agents's id
     * pops up in there.
     * <p>
     * If it does then it means the agent has been assigned a new job and must complete it.
     */
    private void getAssignedCustomer() {
        String agentId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Query query = FirebaseDatabase.getInstance().getReference().child("ride_info").orderByChild("agentId").equalTo(agentId);

        query.addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.exists()) {
                    status = 1;
                    mCurrentRide = new RideObject();
                    mCurrentRide.parseData(dataSnapshot);

                    if (mCurrentRide.getEnded() || mCurrentRide.getCancelled()) {
                        mCurrentRide = null;
                        return;
                    }
                    destinationMarker = mMap.addMarker(new MarkerOptions().position(mCurrentRide.getDestination().getCoordinates()).title("Destination").icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_radio_filled)));
                    pickupMarker = mMap.addMarker(new MarkerOptions().position(mCurrentRide.getPickup().getCoordinates()).title("Pickup").icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_radio)));


                    mLocation.setText(mCurrentRide.getPickup().getName());
                    mCustomerName.setText(mCurrentRide.getDestination().getName());


                    getAssignedCustomerInfo();
                    getHasRideEnded();
                } else {
                    endRide();
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    /**
     * Get Route from pickup to destination, showing the route to the user
     */
    private void getRouteToMarker(LatLng pickupLatLng) {
        String serverKey = getResources().getString(R.string.google_maps_key);
        if (pickupLatLng != null && mLastLocation != null) {
            GoogleDirection.withServerKey(serverKey)
                    .from(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()))
                    .to(pickupLatLng)
                    .transportMode(TransportMode.DRIVING)
                    .execute(this);
        }
    }

    /**
     * Fetch assigned customer's info and display it in the Bottom sheet
     */
    private void getAssignedCustomerInfo() {
        if (mCurrentRide.getCustomer().getId() == null) {
            return;
        }
        DatabaseReference mCustomerDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(mCurrentRide.getCustomer().getId());
        mCustomerDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    return;
                }

                if (mCurrentRide != null) {

                    mCurrentRide.getCustomer().parseData(dataSnapshot);

                    mCustomerName.setText(mCurrentRide.getCustomer().getName());
                    if (!mCurrentRide.getCustomer().getProfileImage().equals("default"))
                        Glide.with(getApplication()).load(mCurrentRide.getCustomer().getProfileImage()).apply(RequestOptions.circleCropTransform()).into(mCustomerProfileImage);

                }

                mCustomerInfo.setVisibility(View.VISIBLE);
                mBottomSheetBehavior.setHideable(false);
                mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void getHasRideEnded() {
        if (mCurrentRide == null) {
            return;
        }
        driveHasEndedRef = FirebaseDatabase.getInstance().getReference().child("ride_info").child(mCurrentRide.getId()).child("cancelled");
        driveHasEndedRefListener = driveHasEndedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    return;
                }

                endRide();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    /**
     * End Ride by removing all of the active listeners,
     * returning all of the values to the default state
     * and clearing the map from markers
     */
    private void endRide() {
        if (mCurrentRide == null) {
            return;
        }

        mRideStatus.setText(getString(R.string.picked_customer));
        erasePolylines();

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference agentRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Agents").child(userId).child("customerRequest");
        agentRef.removeValue();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("customerRequest");
        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(mCurrentRide.getCustomer().getId(), (key, error) -> {
        });

        mCurrentRide = null;

        status = 0;

        if (pickupMarker != null) {
            pickupMarker.remove();
        }
        if (destinationMarker != null) {
            destinationMarker.remove();
        }
        if (driveHasEndedRefListener != null)
            driveHasEndedRef.removeEventListener(driveHasEndedRefListener);

        mBottomSheetBehavior.setHideable(true);
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        mCustomerName.setText("");
        mLocation.setText("");
        mCustomerProfileImage.setImageResource(R.mipmap.ic_default_user);

        mMap.clear();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        getPropertyInformation();
        LatLng Kitchener = new LatLng(43.467831, -80.521872);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(Kitchener, 12));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(11));
        mMap.setInfoWindowAdapter(new AgentsCustomInfoWindowAdapter(AgentMapActivity.this));

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                if (isWorking) {
                    getRoutes(marker);
                } else {
//                    Toast.makeText(getApplicationContext(), "You must be available to view properties", Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        });

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            } else {
                checkLocationPermission();
            }
        }
    }

    /**
     * Gathers property information from Firebase to display open houses on our map
     */
    private void getPropertyInformation() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference propertyReference = FirebaseDatabase.getInstance().getReference().child("Properties").child("Id");
        final long[] numberOfProperties = {0};

        propertyReference.addValueEventListener(new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

            //    Generates a random colour for the markers
            final int random = new Random().nextInt(0 + 360);
            float hue = random;


            if (dataSnapshot.exists()) {
                numberOfProperties[0] = dataSnapshot.getChildrenCount();
                int propertyCount = 0;
                int snapshotCount = 0;

                String[] markerNames = new String[(int) numberOfProperties[0]];
                String[] addresses = new String[(int) numberOfProperties[0]];
                String[] imageUrlString = new String[(int) numberOfProperties[0]];
                String[] propertyInformation = new String[(int) numberOfProperties[0]];
                Double[] latitudes = new Double[(int) numberOfProperties[0]];
                Double[] longitudes = new Double[(int) numberOfProperties[0]];

                Double latitude;
                Double longitude;


                for (DataSnapshot child : dataSnapshot.getChildren()) {

                    snapshotCount++;
                    for (int i = 0; i < numberOfProperties[0]; i++) {

                        propertyCount++;

                        String key = child.getKey();
                        if (Integer.parseInt(key) == snapshotCount) {
                            latitude = child.child("Lat").getValue(Double.class);
                            longitude = child.child("Long").getValue(Double.class);

                            markerNames[i] = String.valueOf(propertyReference.child(String.valueOf(propertyCount)).child("Address"));
                            addresses[i] = String.valueOf(propertyReference.child(String.valueOf(propertyCount)).child("Address"));
                            imageUrlString[i] = String.valueOf(propertyReference.child(String.valueOf(propertyCount)).child("ImgUrl"));
                            propertyInformation[i] = String.valueOf(propertyReference.child(String.valueOf(propertyCount)).child("Information"));
                            latitudes[i] = latitude;
                            longitudes[i] = longitude;

                            mMap.addMarker(new MarkerOptions()
                                    .title(markerNames[i])
                                    .snippet(propertyInformation[i])
                                    .icon(BitmapDescriptorFactory
                                            .defaultMarker(hue))
                                    .position(new LatLng(latitudes[i], longitudes[i])
                                    ));
                        }
                    }
                }
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    });
}


    private void getRoutes(Marker marker) {
        LatLng propertiesPos = marker.getPosition();
        Routing routing = new Routing.Builder()
                .travelMode(Routing.TravelMode.DRIVING)
                .alternativeRoutes(false)
                .withListener(this)
                .key("AIzaSyDRMQHpMV2u2cB27aC1q7ejEy74kCb8Y6c")
                .waypoints(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()), propertiesPos)
                .build();
        routing.execute();


    }



    /**
     * Get permissions for our app if they didn't previously exist.
     */
    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION) && ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                new android.app.AlertDialog.Builder(this)
                        .setTitle("give permission")
                        .setMessage("give permission message")
                        .setPositiveButton("OK", (dialogInterface, i) -> ActivityCompat.requestPermissions(AgentMapActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1))
                        .create()
                        .show();
            } else {
                ActivityCompat.requestPermissions(AgentMapActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                        mMap.setMyLocationEnabled(true);
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Please provide the permission", Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }

    private void logOut() {
        disconnectAgent();

        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(AgentMapActivity.this, LauncherActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        finish();
        return;
    }

    /**
     * Connects agent, waking up the code that fetches current location
     */
    private void connectAgent() {
        isWorking = true;
        mWorkingSwitch.setChecked(true);
        checkLocationPermission();
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
        mMap.setMyLocationEnabled(true);
    }

    /**
     * Disconnects agent, putting to sleep the code that fetches current location
     */
    private void disconnectAgent() {
        mWorkingSwitch.setChecked(false);
        if (mFusedLocationClient != null) {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        }
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("agentsAvailable").child(userId);
        ref.removeValue();
        isWorking = false;
    }

    /**
     * Remove route polylines from the map
     */
    private void erasePolylines() {
        for (Polyline line : polylines) {
            line.remove();
        }
        polylines.clear();
    }

    /**
     * Show map within the pickup and destination marker,
     * This will make sure everything is displayed to the user
     */
    private void setCameraWithCoordinationBounds(Route route) {
        LatLng southwest = route.getBound().getSouthwestCoordination().getCoordination();
        LatLng northeast = route.getBound().getNortheastCoordination().getCoordination();
        LatLngBounds bounds = new LatLngBounds(southwest, northeast);
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));

    }

    /**
     * Checks if route where fetched successfully, if yes then
     * add them to the map
     */
    @Override
    public void onDirectionSuccess(Direction direction, String rawBody) {
        if (direction.isOK()) {
            Route route = direction.getRouteList().get(0);

            ArrayList<LatLng> directionPositionList = route.getLegList().get(0).getDirectionPoint();
            Polyline polyline = mMap.addPolyline(DirectionConverter.createPolyline(this, directionPositionList, 5, Color.GREEN));
            polylines.add(polyline);
            setCameraWithCoordinationBounds(route);

        } else {
        }
    }

    @Override
    public void onDirectionFailure(Throwable t) {

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();


        if (id == R.id.profile) {
            Intent intent = new Intent(AgentMapActivity.this, AgentProfileActivity.class);
            startActivity(intent);
        } else if (id == R.id.logout) {
            logOut();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    private static final int[] COLORS = new int[]{R.color.primary_dark_material_light};

    @Override
    public void onRoutingFailure(RouteException e) {
        if (e != null) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Something went wrong, Try again", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(ArrayList<com.directions.route.Route> route, int shortestRouteIndex) {
        if (polylines != null) {
            if (polylines.size() > 0) {
                for (Polyline poly : polylines) {
                    poly.remove();
                }
            }
        }

        polylines = new ArrayList<>();
        //add route(s) to the map.
        for (int i = 0; i < route.size(); i++) {

            //In case of more than 5 alternative routes
            int colorIndex = i % COLORS.length;

            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(getResources().getColor(COLORS[colorIndex]));
            polyOptions.width(10 + i * 3);
            polyOptions.addAll(route.get(i).getPoints());
            Polyline polyline = mMap.addPolyline(polyOptions);
            polylines.add(polyline);

            Toast.makeText(getApplicationContext(), "Route " + (i + 1) + ": distance: " + route.get(i).getDistanceValue() + " KM " + ": duration: " + route.get(i).getDurationValue() + " Mins", Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public void onRoutingCancelled() {

    }
}