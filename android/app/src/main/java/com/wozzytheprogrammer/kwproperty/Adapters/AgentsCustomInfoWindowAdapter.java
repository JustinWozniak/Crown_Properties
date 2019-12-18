package com.wozzytheprogrammer.kwproperty.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.wozzytheprogrammer.kwproperty.R;

public class AgentsCustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    private final View mWindow;
    private Context mContext;
    private ImageView openHouseImage1;
    private TextView informationText;
    private TextView addressText;

    public AgentsCustomInfoWindowAdapter(Context context) {
        mContext = context;
        mWindow = LayoutInflater.from(context).inflate(R.layout.agent_custom_info_window, null);
    }

    private void renderWindowText(Marker marker, View view) {

        String title = marker.getTitle();
        openHouseImage1 = view.findViewById(R.id.agentsCustomWindowImage1);
        informationText = view.findViewById(R.id.agentOpenHouseInformation);
        addressText = view.findViewById(R.id.agentAddressOpenHouse);
        addressText.setText(title);
        String snippet = marker.getSnippet();
        informationText.setText(snippet);
        String markerId = marker.getTitle();
        loadPropertyImage(markerId);

    }

    private void loadPropertyImage(String markerId) {
        DatabaseReference propertyReference = FirebaseDatabase.getInstance().getReference().child("Properties").child("Id");
        final long[] numberOfProperties = {0};

        propertyReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {
                    numberOfProperties[0] = dataSnapshot.getChildrenCount();
                    int snapshotCount = 0;

                    String[] markerIds = new String[(int) numberOfProperties[0]];
                    String[] imageUrlString = new String[(int) numberOfProperties[0]];


                    for (DataSnapshot child : dataSnapshot.getChildren()) {

                        String id = String.valueOf(child);
                        imageUrlString[snapshotCount] = String.valueOf(dataSnapshot.child(String.valueOf(snapshotCount)).child("ImgUrl").getValue());
                        Glide.with(mContext.getApplicationContext()).load(imageUrlString[snapshotCount]).into(openHouseImage1);

                        snapshotCount++;

                    }
                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public View getInfoWindow(Marker marker) {
        renderWindowText(marker, mWindow);
        return mWindow;
    }

    @Override
    public View getInfoContents(Marker marker) {
        renderWindowText(marker, mWindow);
        return mWindow;
    }


    private Context getApplication() {
        throw new RuntimeException("error!");
    }
}



