package com.wozzytheprogrammer.kwproperty.Login;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.addisonelliott.segmentedbutton.SegmentedButtonGroup;
import com.google.android.material.snackbar.Snackbar;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.wozzytheprogrammer.kwproperty.R;

import java.util.HashMap;
import java.util.Map;


/**
 * Fragment Responsible for registering a new user
 */
public class RegisterFragment extends Fragment implements View.OnClickListener {

    private EditText    mName,
                        mEmail,
                        mPassword;
    private SegmentedButtonGroup mRadioGroup;

    private View view;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (view == null)
            view = inflater.inflate(R.layout.fragment_registration, container, false);
        else
            container.removeView(view);


        return view;
    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeObjects();
    }



    /**
     * Register the user, but before that check if every field is correct.
     * After that registers the user and creates an entry for it oin the Firebase database
     */
    private void register(){
        if(mName.getText().length()==0) {
            mName.setError("please enter your name...");
            return;
        }
        if(mEmail.getText().length()==0) {
            mEmail.setError("please enter your email");
            return;
        }
        if(mPassword.getText().length()==0) {
            mPassword.setError("please enter a password");
            return;
        }
        if(mPassword.getText().length()< 6) {
            mPassword.setError("password must have at least 6 characters");
            return;
        }



        final String name = mName.getText().toString();
        final String email = mEmail.getText().toString();
        final String password = mPassword.getText().toString();
        final String accountType;
        int selectId = mRadioGroup.getPosition();

        switch (selectId){
            case 0:
                accountType = "Customers";
                break;
            case 1:
                accountType = "Agents";
                break;
            default:
                accountType = "Customers";
        }

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password).addOnCompleteListener(getActivity(), task -> {
            if(!task.isSuccessful()){
                Snackbar.make(view.findViewById(R.id.layout), "sign up error", Snackbar.LENGTH_SHORT).show();
            }else{
                String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
                Map newUserMap = new HashMap();
                newUserMap.put("name", name);
                newUserMap.put("email", email);
                newUserMap.put("profileImageUrl", "default");
                if(accountType.equals("Agents")){
                    newUserMap.put("service", "type_1");
                    newUserMap.put("activated", true);
                }
                FirebaseDatabase.getInstance().getReference().child("Users").child(accountType).child(user_id).updateChildren(newUserMap);
            }
        });

    }


    /**
     * Initializes the design Elements and calls clickListeners for them
     */
    private void initializeObjects(){
        mEmail = view.findViewById(R.id.email);
        mName = view.findViewById(R.id.name);
        mPassword = view.findViewById(R.id.password);
        Button mRegister = view.findViewById(R.id.register);
        mRadioGroup = view.findViewById(R.id.radioRealButtonGroup);

        mRadioGroup.setPosition(0, false);
        mRegister.setOnClickListener(this);
    }
    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.register) {
            register();
        }
    }
}