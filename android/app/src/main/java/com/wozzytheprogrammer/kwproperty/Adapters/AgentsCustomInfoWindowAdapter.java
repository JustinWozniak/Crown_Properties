package com.wozzytheprogrammer.kwproperty.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

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
    private ImageView openHouseImage;
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
        Log.e("markerid",markerId);
        loadPropertyImage(markerId);

    }

    private void loadPropertyImage(String markerId) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference propertyReference = FirebaseDatabase.getInstance().getReference().child("Properties").child("Id");
        final long[] numberOfProperties = {0};

        propertyReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {
                    numberOfProperties[0] = dataSnapshot.getChildrenCount();
                    int propertyCount = 0;
                    int snapshotCount = 0;

                    String[] markerIds = new String[(int) numberOfProperties[0]];
                    String[] imageUrlString = new String[(int) numberOfProperties[0]];


                    for (DataSnapshot child : dataSnapshot.getChildren()) {

                        String id = String.valueOf(child);
                        Log.e("CHILDREED",id);
                        Log.e("Datasnap", String.valueOf(dataSnapshot));
                        DataSnapshot propertyAddress = dataSnapshot.child("");
                        Log.e("propertyAddress", String.valueOf(propertyAddress));


                        imageUrlString[snapshotCount] = String.valueOf(dataSnapshot.child("Id").child(String.valueOf(snapshotCount)).child("ImgUrl").getValue());
                        Log.e("dsadsadddsdsdds",String.valueOf(dataSnapshot.child(String.valueOf(snapshotCount)).child("ImgUrl").getValue()));

                        //LEFT OFF HERE FRIDAY NOT WORKING
//                        Glide.with(mContext.getApplicationContext()).load(imageUrlString).into(openHouseImage1);

                        snapshotCount++;
                        for (int i = 0; i < numberOfProperties[0]; i++) {

                            propertyCount++;

                            String key = child.getKey();
                            if (Integer.parseInt(key) == snapshotCount) {

                                markerIds[i] = key;
                           Log.e("markerid",markerIds[i]);


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



