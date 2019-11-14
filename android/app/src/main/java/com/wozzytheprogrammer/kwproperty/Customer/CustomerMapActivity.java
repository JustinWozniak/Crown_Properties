package com.wozzytheprogrammer.kwproperty.Customer;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.api.Status;
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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.wozzytheprogrammer.kwproperty.Adapters.CustomersCustomInfoWindowAdapter;
import com.wozzytheprogrammer.kwproperty.History.HistoryActivity;
import com.wozzytheprogrammer.kwproperty.Login.LauncherActivity;
import com.wozzytheprogrammer.kwproperty.Objects.AgentObject;
import com.wozzytheprogrammer.kwproperty.Objects.CustomerObject;
import com.wozzytheprogrammer.kwproperty.Objects.LocationObject;
import com.wozzytheprogrammer.kwproperty.Objects.RideObject;
import com.wozzytheprogrammer.kwproperty.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Main Activity displayed to the customer
 */
public class CustomerMapActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, DirectionCallback, RoutingListener {

    int MAX_SEARCH_DISTANCE = 20;

    private GoogleMap mMap;

    LocationRequest mLocationRequest;

    private FusedLocationProviderClient mFusedLocationClient;

    private Button mRequest, listView;

    private LocationObject pickupLocation, currentLocation, destinationLocation;

    private Boolean requestBol = false, pickupIsCurrent = false;

    private Marker destinationMarker, pickupMarker;

    private SupportMapFragment mapFragment;


    private LinearLayout mAgentInfo,
            mRadioLayout;

    private ImageView mAgentProfileImage, mCurrentLocation;

    private TextView mAgentName,
            mAgentCar,
            mRatingText;


    DrawerLayout drawer;

    LinearLayout mBringUpBottomLayout;

    RideObject mCurrentRide;


    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_customer);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mCurrentRide = new RideObject(CustomerMapActivity.this, null);

        drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        getUserData();

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mAgentInfo = findViewById(R.id.agentInfo);
        mRadioLayout = findViewById(R.id.radioLayout);

        mAgentProfileImage = findViewById(R.id.agentProfileImage);

        mAgentName = findViewById(R.id.agentName);
        mAgentCar = findViewById(R.id.agentCar);

        mRatingText = findViewById(R.id.ratingText);

        mCurrentLocation = findViewById(R.id.current_location);

        mRequest = findViewById(R.id.request);

        listView = findViewById(R.id.viewListViewButton);

        listView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent ListView2 = new Intent(CustomerMapActivity.this, CustomerListView.class);
                startActivity(ListView2);
            }
        });

        mRequest.setOnClickListener(v -> {

            if (requestBol) {
                mCurrentRide.cancelRide();
                endRide();

            } else {

                mCurrentRide.setDestination(destinationLocation);
                mCurrentRide.setPickup(pickupLocation);

                if(mCurrentRide.checkRide() == -1){
                    return;
                }
                mCurrentRide.postRide();


                requestBol = true;

                mRequest.setText(R.string.getting_agent);


                getClosestAgent();
            }
        });


        ImageView mDrawerButton = findViewById(R.id.drawerButton);
        mDrawerButton.setOnClickListener(v -> drawer.openDrawer(Gravity.LEFT));

        mBringUpBottomLayout = findViewById(R.id.bringUpBottomLayout);
        mBringUpBottomLayout.setOnClickListener(v -> {
            if(mBottomSheetBehavior.getState()!= BottomSheetBehavior.STATE_EXPANDED)
                mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            else
                mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        });


        mCurrentLocation.setOnClickListener(view -> {
            pickupIsCurrent = !pickupIsCurrent;

            if(pickupIsCurrent){
                mCurrentLocation.setImageDrawable(getResources().getDrawable(R.drawable.ic_location_on_primary_24dp));
                pickupLocation = currentLocation;
                if(pickupLocation==null){return;}


                pickupMarker = mMap.addMarker(new MarkerOptions().position(pickupLocation.getCoordinates()).title("Pickup").icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_radio)));
                mCurrentRide.setPickup(pickupLocation);
                if(destinationLocation != null){
                    destinationMarker = mMap.addMarker(new MarkerOptions().position(destinationLocation.getCoordinates()).title("Destination").icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_radio_filled)));
                    mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }

                erasePolylines();
                getRouteToMarker();
                getAgentsAround();

                mRequest.setText(getString(R.string.call_agent));
            }else{

                mCurrentLocation.setImageDrawable(getResources().getDrawable(R.drawable.ic_location_on_grey_24dp));
                if(destinationLocation != null){
                    destinationMarker = mMap.addMarker(new MarkerOptions().position(destinationLocation.getCoordinates()).title("Destination").icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_radio_filled)));
                    mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }
                erasePolylines();
                getAgentsAround();
            }
        });


        ViewTreeObserver vto = mBringUpBottomLayout.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(() -> {
            initializeBottomLayout();
            initPlacesAutocomplete();
        });

        initRecyclerView();
    }

    /**
     * Initializes the recyclerview that shows the costumer the
     * available car types
     */
    private void initRecyclerView(){
        mRecyclerView = findViewById(R.id.recyclerView);
        mRecyclerView.setNestedScrollingEnabled(false);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(CustomerMapActivity.this);
        mRecyclerView.setLayoutManager(mLayoutManager);
    }
    boolean previousRequestBol = true;
    View mBottomSheet;
    BottomSheetBehavior mBottomSheetBehavior;
    BottomSheetBehavior.BottomSheetCallback bottomSheetCallback;

    /**
     * Listener for the bottom popup. This will control
     * when it is shown and when it isn't according to the actions of the users
     * of pulling on it or just clicking on it.
     */
    private void initializeBottomLayout() {
        mBottomSheet =findViewById(R.id.bottomSheet);
        mBottomSheetBehavior = BottomSheetBehavior.from(mBottomSheet);
        mBottomSheetBehavior.setHideable(false);
        mBottomSheetBehavior.setPeekHeight(mBringUpBottomLayout.getHeight());
        mBottomSheetBehavior.setBottomSheetCallback(bottomSheetCallback);


        mBottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if(newState==BottomSheetBehavior.STATE_COLLAPSED && requestBol != previousRequestBol){
                    if(!requestBol){
                        mAgentInfo.setVisibility(View.GONE);
                        mRadioLayout.setVisibility(View.VISIBLE);
                        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                        previousRequestBol = requestBol;
                    }
                    else{
                        mAgentInfo.setVisibility(View.VISIBLE);
                        mRadioLayout.setVisibility(View.GONE);
                        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                        previousRequestBol = requestBol;
                    }
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });
    }

    /**
     * Init Places according the updated google api and
     * listen for user inputs, when a user chooses a place change the values
     * of destination and destinationLocation so that the user can call an agent
     */
    void initPlacesAutocomplete() {
        /*
          Initialize Places. For simplicity, the API key is hard-coded. In a production
          environment we recommend using a secure mechanism to manage API keys.
         */
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getResources().getString(R.string.google_maps_key));
        }

    }


    /**
     * Fetches current user's info and populates the design elements
     */
    private void getUserData() {
        DatabaseReference assignedCustomerRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        assignedCustomerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    NavigationView navigationView = findViewById(R.id.nav_view);
                    View header = navigationView.getHeaderView(0);

                    CustomerObject mCustomer = new CustomerObject();
                    mCustomer.parseData(dataSnapshot);

                    TextView mUsername = header.findViewById(R.id.usernameDrawer);
                    ImageView mProfileImage = header.findViewById(R.id.imageViewDrawer);

                    mUsername.setText(mCustomer.getName());

                    if(!mCustomer.getProfileImage().equals("default"))
                        Glide.with(getApplication()).load(mCustomer.getProfileImage()).apply(RequestOptions.circleCropTransform()).into(mProfileImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private Boolean agentFound = false;
    GeoQuery geoQuery;
    /**
     * Get Closest Rider by getting all the agent available
     * within a radius of the customer current location.
     * radius starts with 1 km and goes up to MAX_SEARCH_DISTANCE
     * Where if no agent is found, and error is thrown saying no
     * agent is found.
     */
    private void getClosestAgent(){

        DatabaseReference agentLocation = FirebaseDatabase.getInstance().getReference().child("agentsAvailable");

        GeoFire geoFire = new GeoFire(agentLocation);
        geoQuery = geoFire.queryAtLocation(new GeoLocation(currentLocation.getCoordinates().latitude, currentLocation.getCoordinates().longitude), MAX_SEARCH_DISTANCE);
        geoQuery.removeAllListeners();

        Handler handler = new Handler();
        int delay = 5000; //milliseconds

        handler.postDelayed(new Runnable(){
            public void run(){
                if(!agentFound){
                    requestBol = false;
                    mRequest.setText(R.string.call_agent);
                    Snackbar.make(findViewById(R.id.drawer_layout), R.string.no_agent_near_you, Snackbar.LENGTH_LONG).show();
                    geoQuery.removeAllListeners();
                    mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                    return;
                }
                handler.postDelayed(this, delay);
            }
        }, delay);


        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if (agentFound || !requestBol){return;}

                DatabaseReference mCustomerDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Agents").child(key);
                mCustomerDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                            Map<String, Object> agentMap = (Map<String, Object>) dataSnapshot.getValue();
                            if (agentFound){
                                return;
                            }

                                agentFound = true;
                                mCurrentRide.setAgent(new AgentObject(dataSnapshot.getKey()));

                                mCurrentRide.postRideInfo();

                                mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

                                getAgentLocation();
                                getAgentInfo();
                                getHasRideEnded();
                                mRequest.setText(R.string.looking_agent);

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
            }
            @Override
            public void onGeoQueryError(DatabaseError error) {
            }
        });
    }


    /**
     * Get's most updated agents location and it's always checking for movements.
     * Even though we used geofire to push the location of the agent we can use a normal
     * Listener to get it's location with no problem.
     * 0 -> Latitude
     * 1 -> Longitudde
     */
    private Marker mAgentMarker;
    private DatabaseReference agentLocationRef;
    private ValueEventListener agentLocationRefListener;
    private void getAgentLocation(){
        if(mCurrentRide.getAgent().getId() == null){return;}
        agentLocationRef = FirebaseDatabase.getInstance().getReference().child("agentsWorking").child(mCurrentRide.getAgent().getId()).child("l");
        agentLocationRefListener = agentLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && requestBol){
                    List<Object> map = (List<Object>) dataSnapshot.getValue();
                    double locationLat = 0;
                    double locationLng = 0;
                    if(map.get(0) != null){
                        locationLat = Double.parseDouble(map.get(0).toString());
                    }
                    if(map.get(1) != null){
                        locationLng = Double.parseDouble(map.get(1).toString());
                    }
                    LocationObject mAgentLocation = new LocationObject(new LatLng(locationLat, locationLng), "");
                    if(mAgentMarker != null){
                        mAgentMarker.remove();
                    }
                    Location loc1 = new Location("");
                    loc1.setLatitude(pickupLocation.getCoordinates().latitude);
                    loc1.setLongitude(pickupLocation.getCoordinates().longitude);

                    Location loc2 = new Location("");
                    loc2.setLatitude(mAgentLocation.getCoordinates().latitude);
                    loc2.setLongitude(mAgentLocation.getCoordinates().longitude);

                    float distance = loc1.distanceTo(loc2);

                    if (distance<100){
                        mRequest.setText(R.string.agent_here);
                    }else{
                        mRequest.setText(getString(R.string.agent_found));
                    }

                    mCurrentRide.getAgent().setLocation(mAgentLocation);

                    mAgentMarker = mMap.addMarker(new MarkerOptions().position(mCurrentRide.getAgent().getLocation().getCoordinates()).title("your agent").icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_agent)));
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }

    /**
     * Get all the user information that we can get from the user's database.
     */
    private void getAgentInfo(){
        if(mCurrentRide == null){return;}

        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        
        DatabaseReference mCustomerDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Agents").child(mCurrentRide.getAgent().getId());
        mCustomerDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                    mAgentInfo.setVisibility(View.VISIBLE);
                    mRadioLayout.setVisibility(View.GONE);

                    mCurrentRide.getAgent().parseData(dataSnapshot);

                    mAgentName.setText(mCurrentRide.getAgent().getName());
                    mAgentCar.setText(mCurrentRide.getAgent().getCar());
                    Glide.with(getApplication())
                            .load(mCurrentRide.getAgent().getProfileImage())
                            .apply(RequestOptions.circleCropTransform())
                            .into(mAgentProfileImage);

                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    /**
     * Listen for the customerRequest Node to see if the agent ended it
     * in the mean time.
     */
    private DatabaseReference driveHasEndedRef;
    private ValueEventListener driveHasEndedRefListener;
    private void getHasRideEnded(){
        if(mCurrentRide == null){return;}
        driveHasEndedRef = FirebaseDatabase.getInstance().getReference().child("ride_info").child(mCurrentRide.getId());
        driveHasEndedRefListener = driveHasEndedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()){return;}
                if(!Boolean.parseBoolean(dataSnapshot.child("ended").getValue().toString())){return;}

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
    private void endRide(){
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        requestBol = false;
        if(geoQuery != null)
            geoQuery.removeAllListeners();
        if (agentLocationRefListener != null)
            agentLocationRef.removeEventListener(agentLocationRefListener);
        if (driveHasEndedRefListener != null)
            driveHasEndedRef.removeEventListener(driveHasEndedRefListener);

        if (mCurrentRide != null && agentFound){
            DatabaseReference agentRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Agents").child(mCurrentRide.getAgent().getId()).child("customerRequest");
            agentRef.removeValue();
        }

        if (polylines != null)  {
           erasePolylines();
        }

        pickupLocation = null;
        destinationLocation = null;

        agentFound = false;
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("customerRequest");
        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(userId, (key, error) -> {});

        if(destinationMarker != null){
            destinationMarker.remove();
        }
        if(pickupMarker != null){
            pickupMarker.remove();
        }
        if (mAgentMarker != null){
            mAgentMarker.remove();
        }
        mMap.clear();
        mRequest.setText(getString(R.string.call_agent));

        mAgentInfo.setVisibility(View.GONE);
        mRadioLayout.setVisibility(View.VISIBLE);

        mAgentName.setText("");
        mAgentCar.setText(getString(R.string.destination));
        mAgentProfileImage.setImageResource(R.mipmap.ic_default_user);

        mCurrentLocation.setImageDrawable(getResources().getDrawable(R.drawable.ic_location_on_grey_24dp));

        mCurrentRide = new RideObject(CustomerMapActivity.this, null);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLng Kitchener = new LatLng(43.467831, -80.521872);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(Kitchener, 12));
        mMap.setInfoWindowAdapter(new CustomersCustomInfoWindowAdapter(CustomerMapActivity.this));

        getJSON("https://www.wozzytheprogrammer.com/onlineapi.php");

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                getRoutes(marker);
                return false;
            }
        });

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){

                mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                mMap.setMyLocationEnabled(true);
            }else{
                checkLocationPermission();
            }
        }

    }

    private void getRoutes(Marker marker) {
        LatLng propertiesPos = marker.getPosition();
        Routing routing = new Routing.Builder()
                .travelMode(Routing.TravelMode.DRIVING)
                .alternativeRoutes(false)
                .withListener(this)
                .key("AIzaSyDRMQHpMV2u2cB27aC1q7ejEy74kCb8Y6c")
                .waypoints(new LatLng(currentLocation.getCoordinates().latitude,currentLocation.getCoordinates().longitude), propertiesPos)
                .build();
        routing.execute();

        }



    /**
     Calls the backend api and loads json data from it....
     */
    private void getJSON(final String urlWebService) {

        class GetJSON extends AsyncTask<Void, Void, String> {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
//                Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
                try {
                    loadIntoMaps(s);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            //in this method we are fetching the json string
            @Override
            protected String doInBackground(Void... voids) {


                try {
                    //creating a URL
                    URL url = new URL(urlWebService);

                    //Opening the URL using HttpURLConnection
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();

                    //StringBuilder object to read the string from the service
                    StringBuilder sb = new StringBuilder();

                    //We will use a buffered reader to read the string from service
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));

                    //A simple string to read values from each line
                    String json;

                    //reading until we don't find null
                    while ((json = bufferedReader.readLine()) != null) {

                        //appending it to string builder
                        sb.append(json + "\n");
                    }

                    //finally returning the read string
                    return sb.toString().trim();
                } catch (Exception e) {
                    return null;
                }

            }
        }

        //creating asynctask object and executing it
        GetJSON getJSON = new GetJSON();
        getJSON.execute();


    }
    /**
    Parses the Json when its returned, and sets markers on the maps
     */
    private void loadIntoMaps(String json) throws JSONException {
        //creating a json array from the json string
        JSONArray addressArray = new JSONArray(json);

        //creating a string array for listview
        String[] markerNames = new String[addressArray.length()];
        String[] addresses = new String[addressArray.length()];
        String[] latittudes = new String[addressArray.length()];
        String[] longitutes = new String[addressArray.length()];
        String[] urlString = new String[addressArray.length()];
        final String[] propertyInformation = new String[addressArray.length()];



        //looping through all the elements in json array
        for (int i = 0; i < addressArray.length(); i++) {

            JSONObject obj = addressArray.getJSONObject(i);

            markerNames[i] = obj.getString("name");
            addresses[i] = obj.getString("address");
            latittudes[i] = obj.getString("lat");
            longitutes[i] = obj.getString("lng");
            propertyInformation[i] = obj.getString("information");
            urlString[i] = obj.getString("urlString");

            JSONObject jsonObj = addressArray.getJSONObject(i);
            final double finalLat = Double.valueOf(jsonObj.getString("lat"));
            final double finalLng = Double.valueOf(jsonObj.getString("lng"));
            mMap.addMarker(new MarkerOptions()
                    .title(jsonObj.getString("address"))
                    .snippet(propertyInformation[i])
                    .position(new LatLng(finalLat,
                            finalLng)
                    ));


            final String anotherUrl = urlString[i];
            mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                @Override
                public void onInfoWindowClick(Marker marker) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(anotherUrl));
                    startActivity(browserIntent);

                    marker.setSnippet(propertyInformation.toString());

                }
            });

        }
    }

    boolean zoomUpdated = false;
    LocationCallback mLocationCallback = new LocationCallback(){
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for(Location location : locationResult.getLocations()){
                if(getApplication()!=null){
                    currentLocation = new LocationObject(new LatLng(location.getLatitude(), location.getLongitude()), "");
                    mCurrentRide.setCurrent(currentLocation);

                    if(!zoomUpdated){

                        zoomUpdated = true;
                    }

                    if(!getAgentsAroundStarted)
                        getAgentsAround();

                }
            }
        }
    };


    /**
     * Get permissions for our app if they didn't previously exist.
     * requestCode -> the number assigned to the request that we've made.
     * Each request has it's own unique request code.
     */
    private void checkLocationPermission() {
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION) &&
                    ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION) &&
                    ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CALL_PHONE)) {
                new android.app.AlertDialog.Builder(this)
                        .setTitle("give permission")
                        .setMessage("give permission message")
                        .setPositiveButton("OK", (dialogInterface, i) -> ActivityCompat.requestPermissions(CustomerMapActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.CALL_PHONE}, 1))
                        .create()
                        .show();
            }
            else{
                ActivityCompat.requestPermissions(CustomerMapActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.CALL_PHONE}, 1);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode){
            case 1:
                if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                        mMap.setMyLocationEnabled(true);
                    }
                } else{
                    Toast.makeText(getApplication(), "Please provide the permission", Toast.LENGTH_LONG).show();
                }
                break;
            default:
                return;
        }
    }




    boolean getAgentsAroundStarted = false;
    public static List<Marker> listOfAgentMarkers = new ArrayList<Marker>();
    /**
     * Displays agents around the user's current
     * location and updates them in real time.
     */
    private void getAgentsAround(){
        if(currentLocation==null){return;}
        getAgentsAroundStarted = true;
        DatabaseReference agentsLocation = FirebaseDatabase.getInstance().getReference().child(("agentsAvailable"));


        GeoFire geoFire = new GeoFire(agentsLocation);
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(currentLocation.getCoordinates().latitude, currentLocation.getCoordinates().longitude), 10000);
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                for(Marker markerIt : listOfAgentMarkers){
                    if(markerIt.getTag() == null || key == null){continue;}
                    if(markerIt.getTag().equals(key))
                        return;
                }


                checkAgentLastUpdated(key);
                LatLng agentLocation = new LatLng(location.latitude, location.longitude);

                Marker AgentMarker = mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory
                        .fromResource(R.drawable.agenticon))
                        .position(agentLocation).title(key));
                AgentMarker.setTag("Agents Name Here");
//                AgentMarker.setTitle("Real Estate Agent");
                AgentMarker.setSnippet(AgentMarker.getId() + " is online!");

                listOfAgentMarkers.add(AgentMarker);
            }

            @Override
            public void onKeyExited(String key) {
                for(Marker markerIt : listOfAgentMarkers) {
                    if(markerIt.getTag() == null || key == null){continue;}
                    if(markerIt.getTag().equals(key)){
                        markerIt.remove();
                        listOfAgentMarkers.remove(markerIt);
                        return;
                    }

                }
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
                for(Marker markerIt : listOfAgentMarkers) {
                    if(markerIt.getTag() == null || key == null){continue;}
                    if(markerIt.getTag().equals(key)) {
                        markerIt.setPosition(new LatLng(location.latitude, location.longitude));
                    }
                }
            }

            @Override
            public void onGeoQueryReady() {

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    /**
     * Checks if agent has not been updated in a while, if it has been more than x time
     * since the agent location was last updated then remove it from the database.
     */
    private void checkAgentLastUpdated(String key) {
        FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child("Agents")
                .child(key)
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()){return;}

                if(dataSnapshot.child("last_updated").getValue()!=null){
                    long lastUpdated = Long.parseLong(dataSnapshot.child("last_updated").getValue().toString());
                    long currentTimestamp = System.currentTimeMillis();

                    if(currentTimestamp - lastUpdated > 60000){
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("agentsAvailable");
                        GeoFire geoFire = new GeoFire(ref);
                        geoFire.removeLocation(dataSnapshot.getKey(), (key1, error) -> {});
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }


    private void logOut(){
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(CustomerMapActivity.this, LauncherActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * Get Route from pickup to destination, showing the route to the user
     */
    private void getRouteToMarker() {

        String serverKey = getResources().getString(R.string.google_maps_key);
        if (mCurrentRide.getDestination() != null && mCurrentRide.getPickup() != null){
            GoogleDirection.withServerKey(serverKey)
                    .from(mCurrentRide.getDestination().getCoordinates())
                    .to(mCurrentRide.getPickup().getCoordinates())
                    .transportMode(TransportMode.DRIVING)
                    .execute(this);
        }
    }

    private List<Polyline> polylines  = new ArrayList<>();

    /**
     * Remove route polylines from the map
     */
    private void erasePolylines(){
        if(polylines==null){return;}
        for(Polyline line : polylines){
            line.remove();
        }
        polylines.clear();
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
            Polyline polyline = mMap.addPolyline(DirectionConverter.createPolyline(this, directionPositionList, 5, Color.BLACK));
            polylines.add(polyline);

        } else {
        }
    }

    @Override
    public void onDirectionFailure(Throwable t) {

    }



    /**
     * Override the activity's onActivityResult(), check the request code, and
     * do something with the returned place data (in this example it's place name and place ID).
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {

            LocationObject mLocation;

            if (currentLocation == null) {
                Snackbar.make(findViewById(R.id.drawer_layout), "First Activate GPS", Snackbar.LENGTH_LONG).show();
                return;
            }
            Place place = Autocomplete.getPlaceFromIntent(data);

            mLocation = new LocationObject(place.getLatLng(), place.getName());


            currentLocation = new LocationObject(new LatLng(currentLocation.getCoordinates().latitude, currentLocation.getCoordinates().longitude), "");


            if (requestCode == 1) {
                mMap.clear();
                destinationLocation = mLocation;
                destinationMarker = mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_radio_filled)).position(destinationLocation.getCoordinates()).title("Destination"));
                mCurrentRide.setDestination(destinationLocation);
                if(pickupLocation != null){
                    pickupMarker = mMap.addMarker(new MarkerOptions().position(pickupLocation.getCoordinates()).title("Pickup").icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_radio)));
                    mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }
            }else if(requestCode == 2){
                mMap.clear();
                pickupLocation = mLocation;
                pickupMarker = mMap.addMarker(new MarkerOptions().position(pickupLocation.getCoordinates()).title("Pickup").icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_radio)));
                mCurrentRide.setPickup(pickupLocation);

                if(destinationLocation != null){
                    destinationMarker = mMap.addMarker(new MarkerOptions().position(destinationLocation.getCoordinates()).title("Destination").icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_radio_filled)));
                    mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }
            }

            erasePolylines();
            getRouteToMarker();
            getAgentsAround();

            mRequest.setText(getString(R.string.call_agent));


        } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
            // TODO: Handle the error.
            Status status = Autocomplete.getStatusFromIntent(data);
        } else if (resultCode == RESULT_CANCELED) {
            initPlacesAutocomplete();
        }
        initPlacesAutocomplete();


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

        if (id == R.id.history) {
            Intent intent = new Intent(CustomerMapActivity.this, HistoryActivity.class);
            intent.putExtra("customerOrAgent", "Customers");
            startActivity(intent);
        } else if (id == R.id.profile) {
            Intent intent = new Intent(CustomerMapActivity.this, CustomerProfileActivity.class);
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

            Toast.makeText(getApplicationContext(),"Route "+ (i+1) +": distance - "+ route.get(i).getDistanceValue()+": duration - "+ route.get(i).getDurationValue(),Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public void onRoutingCancelled() {

    }
}