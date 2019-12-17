package com.wozzytheprogrammer.kwproperty.Adapters;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.firebase.ui.auth.AuthUI;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.wozzytheprogrammer.kwproperty.Chat.ChatMainActivity;
import com.wozzytheprogrammer.kwproperty.Customer.BaseViewHolder;
import com.wozzytheprogrammer.kwproperty.Objects.Properties;
import com.wozzytheprogrammer.kwproperty.R;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PropertyFavsAdapter extends RecyclerView.Adapter<BaseViewHolder> {
    private static final String TAG = "propertyFavsAdapter";
    public static final int VIEW_TYPE_EMPTY = 0;
    public static final int VIEW_TYPE_NORMAL = 1;

    private PropertyFavsAdapter.Callback mCallback;
    private List<Properties> mPropertiesList;
    private Boolean agentFound = false;
    private int MAX_SEARCH_DISTANCE = 100000;

    public PropertyFavsAdapter(List<Properties> propertiesList) {
        mPropertiesList = propertiesList;


    }

    public void setCallback(PropertyFavsAdapter.Callback callback) {
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
                return new PropertyFavsAdapter.ViewHolder(
                        LayoutInflater.from(parent.getContext()).inflate(R.layout.fav_list_item, parent, false));
            case VIEW_TYPE_EMPTY:
            default:
                return new PropertyFavsAdapter.ViewHolder(
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
            return 0;
        }
    }


    public void addItems(List<Properties> propertiesList) {
        mPropertiesList.addAll(propertiesList);
        notifyDataSetChanged();

    }


    public interface Callback {
        void onEmptyViewRetryClick();
    }


    public class ViewHolder extends BaseViewHolder {

        @BindView(R.id.fav_thumbnail)
        ImageView coverImageView;

        @BindView(R.id.fav_title)
        TextView titleTextView;

        @BindView(R.id.fav_newsTitle)
        TextView favnewsTextView;

        @BindView(R.id.fav_newsInfo)
        TextView infoTextView;

        @BindView(R.id.favidTextView)
        TextView idTextView;

        @BindView(R.id.fav_call_agent_button)
        Button callAgent;


        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);


        }

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

        protected void clear() {
            coverImageView.setImageDrawable(null);
            titleTextView.setText("");
            favnewsTextView.setText("");
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
                favnewsTextView.setText(mProperties.getSubTitle());
            }

            if (mProperties.getInfo() != null) {
                infoTextView.setText(mProperties.getInfo());
            }
            if (mProperties.getId() != null) {
                idTextView.setText(mProperties.getId());
            }
            callAgent.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    scaleView(callAgent, 1, 3);
                    scaleView(callAgent, 2, 1);


                    @SuppressLint("RestrictedApi") Toast toast=Toast.makeText(AuthUI.getApplicationContext(),"Finding an agent....",Toast.LENGTH_SHORT);
                    toast.show();

                    String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    DatabaseReference customerLocationRef = FirebaseDatabase.getInstance().getReference("Users/Customers/" + uid + "/location");

                    DatabaseReference agentLocation = FirebaseDatabase.getInstance().getReference().child("agentsAvailable");


                    customerLocationRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                for (DataSnapshot child : dataSnapshot.getChildren()) {
                                    Double customersLat = (Double) dataSnapshot.child("Lat:").getValue();
                                    Double customersLong = (Double) dataSnapshot.child("Long:").getValue();
                                    if (customersLat != null) {
                                        GeoFire geoFire = new GeoFire(agentLocation);
                                        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(customersLat, customersLong), MAX_SEARCH_DISTANCE);

                                        Handler handler = new Handler();
                                        int delay = 5000; //milliseconds

                                        handler.postDelayed(new Runnable() {
                                            public void run() {
                                                if (!agentFound) {
                                                    @SuppressLint("RestrictedApi") Toast toast=Toast.makeText(AuthUI.getApplicationContext(),"No agents online...Try Again Later...",Toast.LENGTH_SHORT);
                                                    toast.show();
                                                    geoQuery.removeAllListeners();

                                                    return;
                                                }
                                                handler.postDelayed(this, delay);
                                            }
                                        }, delay);


                                        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
                                            @Override
                                            public void onKeyEntered(String key, GeoLocation location) {
                                                Map customersId = new HashMap();
                                                Map agentsId = new HashMap();
                                                DatabaseReference foundAgent = FirebaseDatabase.getInstance().getReference().child("agentsAvailable").child(key);
                                                DatabaseReference connectedPeople = FirebaseDatabase.getInstance().getReference().child("connected").child(key);
                                                DatabaseReference wantsConnection = FirebaseDatabase.getInstance().getReference().child("connected").child(key).child("wants Connection");
                                                DatabaseReference connectionsAGo = FirebaseDatabase.getInstance().getReference().child("connected").child(key).child("wants Connection").child("connectCustomer");
                                                agentFound = true;
                                                customersId.put("conectedCustomersId", String.valueOf(uid));
                                                agentsId.put("connectedagentsid", foundAgent);
                                                @SuppressLint("RestrictedApi") Toast toast=Toast.makeText(AuthUI.getApplicationContext(),"Agent Found! Connecting!",Toast.LENGTH_SHORT);
                                                toast.show();
                                                connectedPeople.child("customerFound").updateChildren(customersId);
                                                geoQuery.removeAllListeners();
                                                wantsConnection.addValueEventListener(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                                        for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                                                            connectionsAGo.addValueEventListener(new ValueEventListener() {
                                                                @Override
                                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                    Intent myIntent = new Intent(view.getContext(),
                                                                            ChatMainActivity.class);
                                                                    view.getContext().startActivity(myIntent);
                                                                    geoQuery.removeAllListeners();
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


                                            }

                                            @Override
                                            public void onKeyExited(String key) {

                                            }

                                            @Override
                                            public void onKeyMoved(String key, GeoLocation location) {

                                            }

                                            @Override
                                            public void onGeoQueryReady() {

                                            }

                                            @Override
                                            public void onGeoQueryError(DatabaseError error) {
                                                @SuppressLint("RestrictedApi") Toast toast=Toast.makeText(AuthUI.getApplicationContext(),"Database Error",Toast.LENGTH_SHORT);
                                                toast.show();
                                                geoQuery.removeAllListeners();
                                            }
                                        });
                                    }
                                }

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });


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
            });
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
    }
}