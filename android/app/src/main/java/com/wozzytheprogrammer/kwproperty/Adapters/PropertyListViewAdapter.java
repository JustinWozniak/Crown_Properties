package com.wozzytheprogrammer.kwproperty.Adapters;

import android.content.Intent;
import android.net.Uri;
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

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PropertyListViewAdapter extends RecyclerView.Adapter<BaseViewHolder> {
    private static final String TAG = "PropertyListViewAdapter";
    public static final int VIEW_TYPE_EMPTY = 0;
    public static final int VIEW_TYPE_NORMAL = 1;

    DatabaseReference mCustomer;

    private Callback mCallback;
    private List<Properties> mPropertiesList;


    private Boolean isAFavProperty = false;


    public PropertyListViewAdapter(List<Properties> propertiesList) {
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
//
//    private void getPropertyInformation() {
//        DatabaseReference propertyReference = FirebaseDatabase.getInstance().getReference().child("Properties").child("Id");
//        final long[] numberOfProperties = {0};
//
//        propertyReference.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//
//                if (dataSnapshot.exists()) {
//                    numberOfProperties[0] = dataSnapshot.getChildrenCount();
//                    int propertyCount = -1;
//
//                    String[] addresses = new String[(int) numberOfProperties[0]];
//                    String[] imageUrlString = new String[(int) numberOfProperties[0]];
//                    String[] propertyInformation = new String[(int) numberOfProperties[0]];
//
//                    mPropertiesList.clear();
//
//                    for (DataSnapshot child : dataSnapshot.getChildren()) {
//                            propertyCount++;
//
//                            String key = child.getKey();
//                            addresses[propertyCount] = String.valueOf(propertyReference.child(String.valueOf(propertyCount)).child("Address"));
//                            imageUrlString[propertyCount] = String.valueOf(propertyReference.child(String.valueOf(propertyCount)).child("ImgUrl"));
//                            propertyInformation[propertyCount] = String.valueOf(propertyReference.child(String.valueOf(propertyCount)).child("Information"));
//
//                            Properties properties666 = new Properties(imageUrlString[propertyCount], propertyInformation[propertyCount], propertyInformation[propertyCount], addresses[propertyCount], key);
//
//                            mPropertiesList.add(properties666);
//
//                            }
//                        }
//
//                }
//
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });
//    }
}