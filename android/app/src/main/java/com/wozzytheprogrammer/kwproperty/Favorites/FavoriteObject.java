package com.wozzytheprogrammer.kwproperty.Favorites;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FavoriteObject {

    /**
     * An array of favorite items.
     */
    public static final List<FavPropertyItem> ITEMS = new ArrayList<FavPropertyItem>();

    /**
     * A map of favorite items, by ID.
     */
    public static final Map<String, FavPropertyItem> ITEM_MAP = new HashMap<String, FavPropertyItem>();


    private static int COUNT = 0;

    static {
        // Add some sample items.

            addItem(createPropertyItem(1));
        }


    private static void addItem(FavPropertyItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    private static FavPropertyItem createPropertyItem(int position) {

        DatabaseReference mUser = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(FirebaseAuth.getInstance().getUid());
        String usersId = mUser.getKey();

        DatabaseReference propertyFavRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(String.valueOf(usersId)).child("favoriteProperties");

        DatabaseReference propertyListFromDatabase = FirebaseDatabase.getInstance().getReference().child("Properties").child("Id");



        final long[] numberOfProperties = {0};

        propertyFavRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String favkey = "";
                if (dataSnapshot.exists()) {
                    numberOfProperties[0] = dataSnapshot.getChildrenCount();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        favkey = snapshot.getKey();
                    }


                    DatabaseReference propertyListFomDatabase = FirebaseDatabase.getInstance().getReference().child("Properties").child("Id");

                    String finalFavkey = favkey;
                    propertyListFromDatabase.addValueEventListener(new ValueEventListener() {

                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                String key = snapshot.getKey();
                                COUNT = Integer.parseInt(snapshot.getKey());
                                if(finalFavkey.equals(key))    {
                                    String matchingPropertyId = finalFavkey;
                                    String matchingPropertyContent = (String) dataSnapshot.child(key).child("Information").getValue();
                                    String matchingPropertydetails = (String) dataSnapshot.child(key).child("Address").getValue();
                                    String matchingPropertImgUrl = (String) dataSnapshot.child(key).child("ImgUrl").getValue();
                                    FavPropertyItem finalPropertyObject = new FavPropertyItem(matchingPropertyId,matchingPropertyContent,matchingPropertydetails,matchingPropertImgUrl);

                                    addItem(finalPropertyObject);

                                }

                            }
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

        return new FavPropertyItem(String.valueOf(position), "Item " + position, makeDetails(position), "blank");
    }

    private static String makeDetails(int position) {
        StringBuilder builder = new StringBuilder();
        builder.append("Details about Item: ").append(position);
        for (int i = 0; i < position; i++) {
            builder.append("\nMore details information here.");
        }
        return builder.toString();
    }

    /**
     * A favorite item representing a piece of content.
     */
    public static class FavPropertyItem {
        public final String id;
        public final String content;
        public final String details;
        public final String imgUrl;

        public FavPropertyItem(String id, String address, String details, String imgUrl) {
            this.id = id;
            this.content = details;
            this.details = address;
            this.imgUrl = imgUrl;
        }

        @Override
        public String toString() {
            return content;
        }
    }
}
