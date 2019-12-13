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

    private static final int COUNT = 1;


    static {
        // Add some sample items.
        for (int i = 1; i <= COUNT; i++) {
            addItem(createFavoriteItem(i));
        }
    }

    private static void addItem(FavoriteProperty item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }


    private static FavoriteProperty createFavoriteItem(int position) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference topLevelRef = FirebaseDatabase.getInstance().getReference();

        topLevelRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                DataSnapshot favPropIdKey;
                String userFavPropIdKey = "";
                DataSnapshot propertyList;
                String propertyListIdKey = "";


                Log.e("gfgf", String.valueOf(dataSnapshot));
                favPropIdKey = dataSnapshot.child("Users").child("Customers").child(uid).child("favoriteProperties");
                Log.e("favPropIdKey", String.valueOf(favPropIdKey));
                for (DataSnapshot childSnapshot : favPropIdKey.getChildren()) {
                    userFavPropIdKey = childSnapshot.getKey();
                    Log.e("userFavPropIdKey", String.valueOf(userFavPropIdKey));


                    propertyList = dataSnapshot.child("Properties").child("Id");
                    Log.e("propertyList", String.valueOf(propertyList));

                    for (DataSnapshot childSnapshot2 : propertyList.getChildren()) {
                        propertyListIdKey = childSnapshot.getKey();
                        Log.e("propertyListIdKey", String.valueOf(propertyListIdKey));
                        String content = String.valueOf(childSnapshot2.child("Address").getValue());
                        String details = String.valueOf(childSnapshot2.child("Information").getValue());
                        String imgUrl = String.valueOf(childSnapshot2.child("ImgUrl").getValue());

                        Log.e("contdfent",(content));
                        Log.e("detagfgils",(details));
                        Log.e("imgUrgfgl",(imgUrl));

                        if (userFavPropIdKey == propertyListIdKey) {
                            FavoriteProperty propertyToDisplay = new FavoriteProperty(userFavPropIdKey, content, details, imgUrl);
                            myNewFavPropertyList.add(propertyToDisplay);
                            createNewPropertyObjects(myNewFavPropertyList);
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

        return new FavoriteProperty(String.valueOf(position), "Address " + position, makeDetails(position), "imgUrl");
    }

    private static void createNewPropertyObjects(List<FavoriteProperty> myNewFavPropertyList) {

        Arrays.stream(new List[]{myNewFavPropertyList}).distinct()
                .collect(Collectors.toList());

        Log.e("myNewFavPropertyList", String.valueOf((myNewFavPropertyList)));

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
