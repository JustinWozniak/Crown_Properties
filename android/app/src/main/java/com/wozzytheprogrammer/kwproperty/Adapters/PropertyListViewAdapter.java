package com.wozzytheprogrammer.kwproperty.Adapters;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.CountDownTimer;
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

import static java.lang.Integer.parseInt;

public class PropertyListViewAdapter extends RecyclerView.Adapter<BaseViewHolder> {
    private static final String TAG = "PropertyListViewAdapter";
    public static final int VIEW_TYPE_EMPTY = 0;
    public static final int VIEW_TYPE_NORMAL = 1;
    private static int clickCount = 0;

    private Callback mCallback;
    private List<Properties> mPropertiesList;


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


            buttonFavorite.setOnClickListener(new View.OnClickListener() {

                DatabaseReference mUser = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(FirebaseAuth.getInstance().getUid());

                DatabaseReference favoritePropertiesRef = mUser.child("favoriteProperties");
                Map<String, Object> favoritesUpdates = new TreeMap<>();


                @RequiresApi(api = Build.VERSION_CODES.N)
                @Override
                public void onClick(View view) {

                    clickCount++;

                    scaleView(buttonFavorite, 1, 3);
                    scaleView(buttonFavorite, 2, 1);
                    propertyAddedToFavs.setText(R.string.is_a_favorite);
                    propertyAddedToFavs.setVisibility(View.VISIBLE);

                    String propertyIdString = String.valueOf(idTextView.getText());

                    String linedUpIds = String.valueOf(parseInt(propertyIdString) - 1);

                    favoritesUpdates.put((String) linedUpIds, titleTextView.getText());
                    favoritePropertiesRef.updateChildren(favoritesUpdates);
                    String key = String.valueOf(idTextView.getText());

                    new CountDownTimer(1000, 1000) {

                        public void onTick(long millisUntilFinished) {

                        }

                        public void onFinish() {
                            propertyAddedToFavs.setVisibility(View.GONE);
                        }
                    }.start();


                    if (clickCount > 1) {
                        propertyAddedToFavs.setText(R.string.is_not_a_favorite);

                        favoritesUpdates.remove((String) idTextView.getText(), titleTextView.getText());
                        Log.e("vifdsew", favoritesUpdates.toString());
                        favoritePropertiesRef.child(key).removeValue();
                        clickCount = 0;
                    }


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