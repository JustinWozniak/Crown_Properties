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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    /**
     * A map of sample (dummy) items, by ID.
     */
    public static final Map<String, FavoriteProperty> ITEM_MAP = new HashMap<String, FavoriteProperty>();

    private static final int COUNT = 25;


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
        DatabaseReference usersFavPropRef = FirebaseDatabase.getInstance().getReference("Users/Customers").child(uid).child("favoriteProperties");
        DatabaseReference propertiesList = FirebaseDatabase.getInstance().getReference("Properties").child("Id");

        usersFavPropRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.e("dat", String.valueOf(dataSnapshot));
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String key = snapshot.getKey();
                    Log.e("key", key);
                    String address = (String) dataSnapshot.child(key).getValue();
                    Log.e("address", address);

                    propertiesList.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot2) {
                            DataSnapshot snap = dataSnapshot2.child(key);
                            Log.e("addresgfgfs", String.valueOf(snap));

                            DataSnapshot addressName = snap.child("Address");
                            Log.e("addresname", String.valueOf(addressName.getValue()));

                            DataSnapshot propertyInformation = snap.child("Information");
                            Log.e("propertyInformation", String.valueOf(propertyInformation.getValue()));

                            DataSnapshot imgUrl = snap.child("ImgUrl");
                            Log.e("ImgUrl", String.valueOf(imgUrl.getValue()));

                            FavoriteProperty newFav = new FavoriteProperty(key, (String) addressName.getValue(),(String) propertyInformation.getValue(),(String) imgUrl.getValue());

                            Log.e("addrfdsfdsesname", String.valueOf(addressName.getValue()));


                            ITEMS.add(newFav);
                            ITEM_MAP.put(newFav.id, newFav);
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


        return new FavoriteProperty(String.valueOf(position), "Address " + position, makeDetails(position), "imgUrl");
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
            this.imgUrl = details;//imgUrl
        }

        @Override
        public String toString() {
            return content;
        }
    }
}
