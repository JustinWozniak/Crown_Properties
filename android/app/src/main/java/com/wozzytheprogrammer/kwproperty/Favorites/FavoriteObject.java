package com.wozzytheprogrammer.kwproperty.Favorites;

import android.util.Log;

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

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class FavoriteObject {

    /**
     * An array of sample favorite items.
     */
    public static final List<FavPropertyItem> ITEMS = new ArrayList<FavPropertyItem>();

    /**
     * A map of favorite items, by ID.
     */
    public static final Map<String, FavPropertyItem> ITEM_MAP = new HashMap<String, FavPropertyItem>();


    private static int COUNT = 0;

    static {
        // Add some sample items.
        for (int i = 0; i <= COUNT; i++) {
            addItem(createPropertyItem(i));
        }
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
                        Log.e("favkeys",favkey);
                    }


                    DatabaseReference propertyListFomDatabase = FirebaseDatabase.getInstance().getReference().child("Properties").child("Id");

                    String finalFavkey = favkey;
                    propertyListFromDatabase.addValueEventListener(new ValueEventListener() {

                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                String key = snapshot.getKey();
                                COUNT = Integer.parseInt(snapshot.getKey());
                                Log.e("key", key);
                                Log.e("datasnap", String.valueOf(dataSnapshot));
                                Log.e("favkey[0]", String.valueOf(finalFavkey));
                                if(finalFavkey.equals(key))    {

                                    String matchingPropertyId = finalFavkey;
                                    Log.e("matchingPropertyid", matchingPropertyId);

                                    String matchingPropertyContent = (String) dataSnapshot.child(key).child("Information").getValue();
                                    Log.e("matchingPropertycontent", matchingPropertyContent);

                                    String matchingPropertydetails = (String) dataSnapshot.child(key).child("Address").getValue();
                                    Log.e("matchingPropertydetails", matchingPropertydetails);

                                    String matchingPropertImgUrl = (String) dataSnapshot.child(key).child("ImgUrl").getValue();
                                    Log.e("matchingPropertyurl", matchingPropertImgUrl);
                                    FavPropertyItem finalPropertyObject = new FavPropertyItem(matchingPropertyId,matchingPropertyContent,matchingPropertydetails,matchingPropertImgUrl);

                                    addItem(finalPropertyObject);
                                    Log.e("WE GOOD?", String.valueOf(finalPropertyObject));
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
            this.content = address;
            this.details = details;
            this.imgUrl = imgUrl;
        }

        @Override
        public String toString() {
            return content;
        }
    }
}
