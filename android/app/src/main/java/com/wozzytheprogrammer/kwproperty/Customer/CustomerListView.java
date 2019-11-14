package com.wozzytheprogrammer.kwproperty.Customer;

import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.wozzytheprogrammer.kwproperty.R;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CustomerListView extends AppCompatActivity implements PropertyAdapter.Callback {

    @BindView(R.id.mRecyclerView)
    RecyclerView mRecyclerView;
    PropertyAdapter mPropertyAdapter;


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
        mPropertyAdapter = new PropertyAdapter(new ArrayList<>());

        prepareDemoContent();
    }

    private void prepareDemoContent() {

        new Handler().postDelayed(() -> {
            //prepare data and show loading

            ArrayList<Properties> mProperties = new ArrayList<>();
            String[] propertyAddressList = getResources().getStringArray(R.array.property_addresses);
            String[] propertyInfo = getResources().getStringArray(R.array.property_info);
            String[] propertyImage = getResources().getStringArray(R.array.property_images);
            for (int i = 0; i < propertyAddressList.length; i++) {
                mProperties.add(new Properties(propertyImage[i], propertyInfo[i], "News", propertyAddressList[i]));
            }
            mPropertyAdapter.addItems(mProperties);
            mRecyclerView.setAdapter(mPropertyAdapter);
        }, 2000);


    }

    @Override
    public void onEmptyViewRetryClick() {
        prepareDemoContent();
    }
}