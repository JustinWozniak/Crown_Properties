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
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference testerRef = database.getReference();

        String currentuser = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference usersFavPropIdNumber = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(currentuser).child("favoriteProperties");
        usersFavPropIdNumber.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    final String id = child.getKey();

                    testerRef.child("Properties/Id").child(String.valueOf(id)).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            DatabaseReference propertyReference = FirebaseDatabase.getInstance().getReference().child("Properties").child("Id").child(id);
                            final long[] numberOfProperties = {0};
                            ArrayList<Properties> mProperties = new ArrayList<>();
                            propertyReference.addValueEventListener(new ValueEventListener() {

                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        numberOfProperties[0] = dataSnapshot.getChildrenCount();
                                        int propertyCount = -1;

                                        String[] addresses = new String[(int) numberOfProperties[0]];
                                        String[] imageUrlString = new String[(int) numberOfProperties[0]];
                                        String[] propertyInformation = new String[(int) numberOfProperties[0]];


                                        for (DataSnapshot child : dataSnapshot.getChildren()) {
                                            propertyCount++;
                                            propertyInformation[propertyCount] = (String) dataSnapshot.child("Information").getValue();
                                            String key = child.getKey();
                                            addresses[propertyCount] = (String) dataSnapshot.child("Address").getValue();
                                            imageUrlString[propertyCount] = (String) dataSnapshot.child("ImgUrl").getValue();

                                        }
                                        mProperties.add(new Properties(imageUrlString[propertyCount], propertyInformation[propertyCount], "Rental Property", addresses[propertyCount], id));
                                        mPropertyAdapter.addItems(mProperties);
                                        mRecyclerView.setAdapter(mPropertyAdapter);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }


                            });

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




    @Override
    public void onEmptyViewRetryClick() {
        prepareContent();
    }


}
