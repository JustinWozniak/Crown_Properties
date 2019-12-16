package com.wozzytheprogrammer.kwproperty.Customer;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.wozzytheprogrammer.kwproperty.Adapters.PropertyFavsAdapter;
import com.wozzytheprogrammer.kwproperty.Adapters.PropertyListViewAdapter;
import com.wozzytheprogrammer.kwproperty.Objects.Properties;
import com.wozzytheprogrammer.kwproperty.R;

import java.util.ArrayList;

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

    private void prepareContent() {
        DatabaseReference propertyReference = FirebaseDatabase.getInstance().getReference().child("Properties").child("Id");
        final long[] numberOfProperties = {0};
        ArrayList<Properties> mProperties = new ArrayList<>();
        propertyReference.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    numberOfProperties[0] = dataSnapshot.getChildrenCount();
                    int propertyCount = -1;

                    String[] addresses = new String[(int) numberOfProperties[0]];
                    String[] imageUrlString = new String[(int) numberOfProperties[0]];
                    String[] propertyInformation = new String[(int) numberOfProperties[0]];


                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        propertyCount++;
                        propertyInformation[propertyCount] = String.valueOf(dataSnapshot.child(String.valueOf(propertyCount)).child("Information").getValue());
                        String key = child.getKey();
                        addresses[propertyCount] = String.valueOf(dataSnapshot.child(String.valueOf(propertyCount)).child("Address").getValue());
                        imageUrlString[propertyCount] = String.valueOf(dataSnapshot.child(String.valueOf(propertyCount)).child("ImgUrl").getValue());

                        mProperties.add(new Properties(imageUrlString[propertyCount], propertyInformation[propertyCount],"Rental Property", addresses[propertyCount], key));
                    }

                    mPropertyAdapter.addItems(mProperties);
                    mRecyclerView.setAdapter(mPropertyAdapter);
                    }

                }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });}

    @Override
    public void onEmptyViewRetryClick() {
        prepareContent();
    }
}
