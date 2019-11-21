package com.wozzytheprogrammer.kwproperty.Customer;

import android.os.Bundle;
import android.os.Handler;
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
import com.wozzytheprogrammer.kwproperty.Adapters.PropertyAdapter;
import com.wozzytheprogrammer.kwproperty.Objects.Properties;
import com.wozzytheprogrammer.kwproperty.R;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CustomerListView extends AppCompatActivity implements PropertyAdapter.Callback {

    @BindView(R.id.mRecyclerView)
    RecyclerView mRecyclerView;
    PropertyAdapter mPropertyAdapter;

    DatabaseReference mCustomer;


    LinearLayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_list_view);
        ButterKnife.bind(this);
        setUp();


    }

    private void setUp() {
        mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setOrientation(RecyclerView.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mPropertyAdapter = new PropertyAdapter(new ArrayList<>());



        prepareContent();


    }

    private void prepareContent() {

        new Handler().postDelayed(() -> {
            //prepare data and show loading

            ArrayList<Properties> mProperties = new ArrayList<>();
            String[] propertyAddressList = getResources().getStringArray(R.array.property_addresses);
            String[] propertyInfo = getResources().getStringArray(R.array.property_info);
            String[] propertyImage = getResources().getStringArray(R.array.property_images);
            String[] id = getResources().getStringArray(R.array.property_info);
            for (int i = 0; i < propertyAddressList.length; i++) {
                mProperties.add(new Properties(propertyImage[i], propertyInfo[i], "Rental Property", propertyAddressList[i],id[i]));
            }
            mPropertyAdapter.addItems(mProperties);
            mRecyclerView.setAdapter(mPropertyAdapter);
        }, 2000);

    getCustomersFavs();
    }
    // Get a reference to our favorite properties
    private void getCustomersFavs() {
        String customersId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference customersFavList = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(customersId).child("favoriteProperties");
        customersFavList.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Log.e("DataSnapshot", String.valueOf(dataSnapshot));
                    if(dataSnapshot.hasChild("favoriteProperties")) {

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    @Override
    public void onEmptyViewRetryClick() {
        prepareContent();
    }
}
