package com.wozzytheprogrammer.kwproperty.Customer.favoritesData;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class FavoritesData {

    /**
     * An array of sample (dummy) items.
     */
    public static final List<FavoriteProperty> ITEMS = new ArrayList<FavoriteProperty>();

    //this will hold our new fav properties
    static final List<FavoriteProperty> myNewFavPropertyList = new ArrayList<FavoriteProperty>();
    /**
     * A map of sample (dummy) items, by ID.
     */
    public static final Map<String, FavoriteProperty> ITEM_MAP = new HashMap<String, FavoriteProperty>();


    static {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference topLevelRef = FirebaseDatabase.getInstance().getReference();

        topLevelRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                DataSnapshot favPropIdKey;
                String userFavPropIdKey = null;
                DataSnapshot propertyList;
                String propertyListIdKey = null;
                String content = null;
                String details = null;
                String imgUrl = null;
                int propertCount = 0;

                favPropIdKey = dataSnapshot.child("Users").child("Customers").child(uid).child("favoriteProperties");
                for (DataSnapshot childSnapshot : favPropIdKey.getChildren()) {
                    propertCount++;
                    userFavPropIdKey = childSnapshot.getKey();

                    propertyList = dataSnapshot.child("Properties").child("Id");

                    for (DataSnapshot childSnapshot2 : propertyList.getChildren()) {
                        propertyListIdKey = childSnapshot.getKey();
                        content = String.valueOf(childSnapshot2.child("Address").getValue());
                        details = String.valueOf(childSnapshot2.child("Information").getValue());
                        imgUrl = String.valueOf(childSnapshot2.child("ImgUrl").getValue());

                        if (userFavPropIdKey == propertyListIdKey ) {
                            FavoriteProperty propertyToDisplay = new FavoriteProperty(userFavPropIdKey, content, details, imgUrl);
                            myNewFavPropertyList.add(propertyToDisplay);
                            ITEMS.add(propertyToDisplay);
                            ITEM_MAP.put(propertyToDisplay.id, propertyToDisplay);



                        }


                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private static void addItem(FavoriteProperty item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }



    private static String makeDetails(int position) {
        StringBuilder builder = new StringBuilder();
        builder.append("Details about home: ").append(position);
        for (int i = 0; i < position; i++) {
            builder.append("\nMore details information home: ");
        }
        return builder.toString();
    }

    /**
     * A favorite object representing a piece of content.
     */
    public static class FavoriteProperty {
        public final String id;
        public final String content; //address
        public final String details; //info
        public final String imgUrl;

        public FavoriteProperty(String id, String content, String details, String imgUrl) {
            this.id = id;
            this.content = content; //address
            this.details = details;//info
            this.imgUrl = imgUrl;//imgUrl
        }

        @Override
        public String toString() {
            return content;
        }
    }
}
