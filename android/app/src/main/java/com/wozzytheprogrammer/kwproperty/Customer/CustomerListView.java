package com.wozzytheprogrammer.kwproperty.Customer;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.wozzytheprogrammer.kwproperty.R;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CustomerListView extends AppCompatActivity implements SportAdapter.Callback {

    @BindView(R.id.mRecyclerView)
    RecyclerView mRecyclerView;
    SportAdapter mSportAdapter;


    LinearLayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_list_view);
        ButterKnife.bind(this);
        setUp();
    }

    private void setUp() {
        mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setOrientation(RecyclerView.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        Drawable dividerDrawable = ContextCompat.getDrawable(this, R.drawable.divider_drawable);
//        mRecyclerView.addItemDecoration(new DividerItemDecoration(dividerDrawable));
        mSportAdapter = new SportAdapter(new ArrayList<>());

        prepareDemoContent();
    }

    private void prepareDemoContent() {

        new Handler().postDelayed(() -> {
            //prepare data and show loading

            ArrayList<Properties> mProperties = new ArrayList<>();
            String[] sportsList = getResources().getStringArray(R.array.property_addresses);
            String[] sportsInfo = getResources().getStringArray(R.array.property_info);
            String[] sportsImage = getResources().getStringArray(R.array.property_images);
            for (int i = 0; i < sportsList.length; i++) {
                mProperties.add(new Properties(sportsImage[i], sportsInfo[i], "News", sportsList[i]));
            }
            mSportAdapter.addItems(mProperties);
            mRecyclerView.setAdapter(mSportAdapter);
        }, 2000);


    }

    @Override
    public void onEmptyViewRetryClick() {
        prepareDemoContent();
    }
}
