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

public class PropertyFavsAdapter  extends RecyclerView.Adapter<BaseViewHolder> {
    private static final String TAG = "propertyFavsAdapter";
    public static final int VIEW_TYPE_EMPTY = 0;
    public static final int VIEW_TYPE_NORMAL = 1;

    int position = 0;

    private static int clickCount = 0;

    private PropertyFavsAdapter.Callback mCallback;
    private List<Properties> mPropertiesList;


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
                return new PropertyFavsAdapter.EmptyViewHolder(
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



        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);


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