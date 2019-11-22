package com.wozzytheprogrammer.kwproperty.Customer.favorites;

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


public class FavoritesContent {

    /**
     * An array of sample (favorite) items.
     */
    public static final List<FavoriteItem> ITEMS = new ArrayList<FavoriteItem>();

    /**
     * A map of sample (favorite) items, by ID.
     */
    public static final Map<String, FavoriteItem> ITEM_MAP = new HashMap<String, FavoriteItem>();

    private static final int COUNT = 25;
    private int propertyId;

    static {
        // Add some sample items.
        for (int i = 1; i <= COUNT; i++) {
            addItem(gatherFavoriteProperties(i));
        }
    }

    private static void addItem(FavoriteItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    private static FavoriteItem gatherFavoriteProperties(int position) {

        String customersId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference customersFavList = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(customersId).child("favoriteProperties");
        customersFavList.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Log.e("DataSnapshot", String.valueOf(dataSnapshot));
                    if (dataSnapshot.hasChild("favoriteProperties")) {

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        return new FavoriteItem(String.valueOf(position), "Item " + position, makeDetails(position));
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
    public static class FavoriteItem {
        public final String id;
        public final String content;
        public final String details;

        public FavoriteItem(String id, String content, String details) {
            this.id = id;
            this.content = content;
            this.details = details;
        }

        @Override
        public String toString() {
            return content;
        }


    }
}