package com.wozzytheprogrammer.kwproperty.Customer;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.wozzytheprogrammer.kwproperty.Adapters.PropertyFavsAdapter;
import com.wozzytheprogrammer.kwproperty.Objects.Properties;
import com.wozzytheprogrammer.kwproperty.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CustomerFavoritesListView extends AppCompatActivity implements PropertyFavsAdapter.Callback {

    @BindView(R.id.mRecyclerView)
    RecyclerView mRecyclerView;
    PropertyFavsAdapter mPropertyAdapter;
    LinearLayoutManager mLayoutManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_favorites_list_view);
        ButterKnife.bind(this);
        setUp();

    }

    private void setUp() {
        mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setOrientation(RecyclerView.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mPropertyAdapter = new PropertyFavsAdapter(new ArrayList<>());

        prepareContent();

    }


    /**
     * Pulls users fav Id from database...Looks for it in properties database,
     * then sends data to the adapter to display it.
     */
    private void prepareContent() {
        ArrayList<Properties> listOfFavProperties = new ArrayList<>();
        final String[] ImgUrl = {""};
        final String[] Info = {""};
        final String[] Address = {""};
        final String[] Title = {""};
        final String[] idsToUse = {""};
        final String[] id = {null};

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference testerRef = database.getReference();
        Log.e("tester", String.valueOf(testerRef.child("Properties/test")));

        String currentuser = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference usersFavPropIdNumber = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(currentuser).child("favoriteProperties");
        usersFavPropIdNumber.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    id[0] = child.getKey();
                    Log.e("objvcvfect[0]", String.valueOf(id[0]));
                    testerRef.child("Properties/Id").child(String.valueOf(id[0])).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                            Log.e("dataSnapshot.getChildren()", String.valueOf(dataSnapshot.getValue()));

                            for (DataSnapshot child : children) {
                                ImgUrl[0] = (String) dataSnapshot.child("ImgUrl").getValue();
                                Info[0] = (String) dataSnapshot.child("Info").getValue();
                                Address[0] = (String) dataSnapshot.child("Address").getValue();
                                Title[0] = (String) dataSnapshot.child("Title").getValue();
                                idsToUse[0] = dataSnapshot.child("Id").getValue().toString();


                            }
                            Properties propertyMadeFromData = new Properties(ImgUrl[0], Info[0], Address[0], Title[0], idsToUse[0]);

                            listOfFavProperties.add(propertyMadeFromData);

                            removeDuplicatesFromLooping(listOfFavProperties);

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void removeDuplicatesFromLooping(ArrayList<Properties> passedListOfFavProperties) {
        {
            // ArrayList with duplicate elements
            ArrayList<Properties> numbersList = passedListOfFavProperties;

            System.out.println(numbersList);

            List<Properties> listWithoutDuplicates = numbersList.stream().distinct().collect(Collectors.toList());

            System.out.println(listWithoutDuplicates);


            mPropertyAdapter.addItems(listWithoutDuplicates);
            mRecyclerView.setAdapter(mPropertyAdapter);
        }
    }


    @Override
    public void onEmptyViewRetryClick() {
        prepareContent();
    }


}
