package com.wozzytheprogrammer.kwproperty.Customer.favorites;

import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
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

    private static final int COUNT = 23;


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
        final String[] propertyIds = new String[100];
        String customersId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference customersFavList = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(customersId).child("favoriteProperties");
        customersFavList.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for(DataSnapshot snapshot:dataSnapshot.getChildren()){

//                        Log.e("key", snapshot.getKey());
//                        Log.e("val", (String) snapshot.getValue());
                        propertyIds[0] = snapshot.getKey();

                    }
                    getJSON("https://www.wozzytheprogrammer.com/onlineapi.php",propertyIds[0]);                }
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


    private static void getJSON(final String urlWebService, String passedFavId) {

        class GetJSON extends AsyncTask<Void, Void, String> {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
//                Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
                try {
                    parseJson(s,passedFavId);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            //in this method we are fetching the json string
            @Override
            protected String doInBackground(Void... voids) {


                try {
                    //creating a URL
                    URL url = new URL(urlWebService);

                    //Opening the URL using HttpURLConnection
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();

                    //StringBuilder object to read the string from the service
                    StringBuilder sb = new StringBuilder();

                    //We will use a buffered reader to read the string from service
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));

                    //A simple string to read values from each line
                    String json;

                    //reading until we don't find null
                    while ((json = bufferedReader.readLine()) != null) {

                        //appending it to string builder
                        sb.append(json + "\n");
                    }

                    //finally returning the read string
                    return sb.toString().trim();
                } catch (Exception e) {
                    return null;
                }

            }
        }

        //creating asynctask object and executing it
        GetJSON getJSON = new GetJSON();
        getJSON.execute();


    }
    /**
     Parses the Json when its returned, and sets markers on the maps
     */
    private static void parseJson(String json, String favoriteId) throws JSONException {
        //creating a json array from the json string
        JSONArray addressArray = new JSONArray(json);

        //creating a string array for listview
        String[] ids = new String[addressArray.length()];
        String[] addresses = new String[addressArray.length()];
        String[] urlString = new String[addressArray.length()];
        final String[] propertyInformation = new String[addressArray.length()];


        ITEMS.clear();
        ITEM_MAP.clear();
        //looping through all the elements in json array
        for (int i = 0; i < addressArray.length(); i++) {

            JSONObject obj = addressArray.getJSONObject(i);
            ids[i] = obj.getString("id");
            addresses[i] = obj.getString("address");
            propertyInformation[i] = obj.getString("information");
            urlString[i] = obj.getString("urlString");

            FavoriteItem newestItem = new FavoriteItem(ids[i],addresses[i],propertyInformation[i]);


            ITEMS.add(newestItem);
            ITEM_MAP.put(newestItem.id, newestItem);
            Log.e("ITEMS", String.valueOf(ITEMS));
            Log.e("ITEM_MAP", String.valueOf(ITEM_MAP));

        }
    }
}