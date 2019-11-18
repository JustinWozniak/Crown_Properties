package com.wozzytheprogrammer.kwproperty.Login;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.wozzytheprogrammer.kwproperty.R;


/**
 * Fragment that allows the user to choose between going to the login or registration
 */
public class MenuFragment extends Fragment implements View.OnClickListener {

    private View view;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (view == null)
            view = inflater.inflate(R.layout.fragment_menu, container, false);

        else
            container.removeView(view);


        return view;
    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeObjects();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.registration:
                ((AuthenticationActivity) getActivity()).registrationClick();
                break;
            case R.id.login:
                ((AuthenticationActivity) getActivity()).loginClick();

                break;
        }
    }


    /**
     * initializes the design Elements
     */
    private void initializeObjects(){
        Button mLogin = view.findViewById(R.id.login);
        Button mRegistration = view.findViewById(R.id.registration);

        mRegistration.setOnClickListener(this);
        mLogin.setOnClickListener(this);

    }
}