package com.wozzytheprogrammer.kwproperty.Adapters;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.wozzytheprogrammer.kwproperty.Customer.BaseViewHolder;
import com.wozzytheprogrammer.kwproperty.Objects.Properties;
import com.wozzytheprogrammer.kwproperty.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PropertyAdapter extends RecyclerView.Adapter<BaseViewHolder> {
    private static final String TAG = "PropertyAdapter";
    public static final int VIEW_TYPE_EMPTY = 0;
    public static final int VIEW_TYPE_NORMAL = 1;

    DatabaseReference mCustomer;

    private Callback mCallback;
    private List<Properties> mPropertiesList;


    private Boolean isAFavProperty = false;


    public PropertyAdapter(List<Properties> propertiesList) {
        mPropertiesList = propertiesList;


    }

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {
        holder.onBind(position);
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        switch (viewType) {

            case VIEW_TYPE_NORMAL:
                return new ViewHolder(
                        LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false));
            case VIEW_TYPE_EMPTY:
            default:
                return new EmptyViewHolder(
                        LayoutInflater.from(parent.getContext()).inflate(R.layout.item_empty_view, parent, false));
        }


    }

    @Override
    public int getItemViewType(int position) {
        if (mPropertiesList != null && mPropertiesList.size() > 0) {
            return VIEW_TYPE_NORMAL;
        } else {
            return VIEW_TYPE_EMPTY;
        }
    }

    @Override
    public int getItemCount() {


        if (mPropertiesList != null && mPropertiesList.size() > 0) {
            return mPropertiesList.size();
        } else {
            return 1;
        }
    }


    public void addItems(List<Properties> propertiesList) {
        getJSON("https://www.wozzytheprogrammer.com/objectApi.php");
        mPropertiesList.addAll(propertiesList);
        notifyDataSetChanged();
    }

    public interface Callback {
        void onEmptyViewRetryClick();
    }


    public class ViewHolder extends BaseViewHolder {

        @BindView(R.id.property_added_text)
        TextView propertyAddedToFavs;

        @BindView(R.id.thumbnail)
        ImageView coverImageView;

        @BindView(R.id.title)
        TextView titleTextView;

        @BindView(R.id.newsTitle)
        TextView newsTextView;

        @BindView(R.id.newsInfo)
        TextView infoTextView;

        @BindView(R.id.idTextView)
        TextView idTextView;

        @BindView(R.id.button_favorite)
        Button buttonFavorite;

        public void scaleView(View v, float startScale, float endScale) {
            Animation anim = new ScaleAnimation(
                    1f, 1f, // Start and end values for the X axis scaling
                    startScale, endScale, // Start and end values for the Y axis scaling
                    Animation.RELATIVE_TO_SELF, 0f, // Pivot point of X scaling
                    Animation.RELATIVE_TO_SELF, 1f); // Pivot point of Y scaling
            anim.setFillAfter(true); // Needed to keep the result of the animation
            anim.setDuration(500);
            v.startAnimation(anim);
        }


        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            DatabaseReference mUser;
            mUser = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(FirebaseAuth.getInstance().getUid());

            DatabaseReference favoritePropertiesRef = mUser.child("favoriteProperties");
            Map<String, Object> favoritesUpdates = new TreeMap<>();


            buttonFavorite.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.N)
                @Override
                public void onClick(View view) {
                    scaleView(buttonFavorite, 1, 3);
                    scaleView(buttonFavorite, 2, 1);
                    if (!isAFavProperty) {
                        propertyAddedToFavs.setText(R.string.is_a_favorite);
                        propertyAddedToFavs.setVisibility(View.VISIBLE);
                        isAFavProperty = true;
                        favoritesUpdates.put((String) idTextView.getText(), titleTextView.getText());

                        favoritePropertiesRef.updateChildren(favoritesUpdates);
                    } else {
                        propertyAddedToFavs.setVisibility(View.GONE);
                        isAFavProperty = false;

                    }
                }
            });
        }

        protected void clear() {
            coverImageView.setImageDrawable(null);
            titleTextView.setText("");
            newsTextView.setText("");
            infoTextView.setText("");
            idTextView.setText("");
        }

        public void onBind(int position) {
            super.onBind(position);

            final Properties mProperties = mPropertiesList.get(position);

            if (mProperties.getImageUrl() != null) {
                Glide.with(itemView.getContext())
                        .load(mProperties.getImageUrl())
                        .into(coverImageView);
            }

            if (mProperties.getTitle() != null) {
                titleTextView.setText(mProperties.getTitle());
            }

            if (mProperties.getSubTitle() != null) {
                newsTextView.setText(mProperties.getSubTitle());
            }

            if (mProperties.getInfo() != null) {
                infoTextView.setText(mProperties.getInfo());
            }
            if (mProperties.getId() != null) {
                idTextView.setText(mProperties.getId());
            }

            itemView.setOnClickListener(v -> {
                if (mProperties.getImageUrl() != null) {
                    try {
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_VIEW);
                        intent.addCategory(Intent.CATEGORY_BROWSABLE);
                        intent.setData(Uri.parse(mProperties.getImageUrl()));
                        itemView.getContext().startActivity(intent);
                    } catch (Exception e) {
                        Log.e(TAG, "onClick: Image url is not correct");
                    }
                }
            });
        }
    }

    public class EmptyViewHolder extends BaseViewHolder {

        @BindView(R.id.tv_message)
        TextView messageTextView;
        @BindView(R.id.buttonRetry)
        TextView buttonRetry;

        EmptyViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            buttonRetry.setOnClickListener(v -> mCallback.onEmptyViewRetryClick());
        }

        @Override
        protected void clear() {

        }

    }

    /**
     * Calls the backend api and loads json data from it....
     */
    private void getJSON(final String urlWebService) {


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
                    parseJsonInfo(s);
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
     * Parses the Json when its returned, and sets markers on the maps
     */
    private void parseJsonInfo(String json) throws JSONException {

        //creating a json array from the json string
        JSONArray addressArray = new JSONArray(json);
        //creating a string array for listview
        String[] imageUrl = new String[addressArray.length()];
        String[] propertyInformation = new String[addressArray.length()];
        String[] addresses = new String[addressArray.length()];
        String[] type = new String[addressArray.length()];
        String[] id = new String[addressArray.length()];

        //looping through all the elements in json array
        for (int i = 0; i < addressArray.length(); i++) {
            JSONObject obj = addressArray.getJSONObject(i);
            imageUrl[i] = obj.getString("imgUrl");
            propertyInformation[i] = obj.getString("information");
            addresses[i] = obj.getString("address");
            type[i] = obj.getString("type");
            id[i] = obj.getString("id");
            Properties properties666 = new Properties(imageUrl[i], propertyInformation[i], type[i], addresses[i], id[i]);

            mPropertiesList.add(properties666);
        }
        Log.e("Pop3cdss", String.valueOf(mPropertiesList));

        //ASYNCHRONOUS ISSUE? THESE ITEMS WONT REMOVE IN A A LOOP, BUT DO EVENTUALLY IN APP
        mPropertiesList.remove(0);
        mPropertiesList.remove(0);
        mPropertiesList.remove(0);

    }
}
