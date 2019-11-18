package com.wozzytheprogrammer.kwproperty.Agent;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.addisonelliott.segmentedbutton.SegmentedButtonGroup;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.wozzytheprogrammer.kwproperty.Objects.AgentObject;
import com.wozzytheprogrammer.kwproperty.R;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Activity that displays the profile to the Agent
 */
public class AgentProfileActivity extends AppCompatActivity {

    private EditText mNameField, mCarField;

    private ImageView mProfileImage;

    private DatabaseReference mAgentsDataBase;

    private String userID;

    private Uri resultUri;

    private SegmentedButtonGroup mRadioGroup;

    AgentObject mDriver = new AgentObject();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agent_settings);


        mNameField = findViewById(R.id.name);
        mCarField = findViewById(R.id.car);

        mProfileImage = findViewById(R.id.profileImage);


        Button mConfirm = findViewById(R.id.confirm);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        userID = mAuth.getCurrentUser().getUid();
        mAgentsDataBase = FirebaseDatabase.getInstance().getReference().child("Users").child("Agents").child(userID);

        getUserInfo();

        mProfileImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, 1);
            overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
        });

        mConfirm.setOnClickListener(v -> saveUserInformation());
        setupToolbar();
    }

    /**
     * Sets up toolbar with custom text and a listener
     * to go back to the previous activity
     */
    private void setupToolbar() {
        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle(getString(R.string.your_profile));
        myToolbar.setTitleTextColor(getResources().getColor(R.color.white));
        ActionBar ab = getSupportActionBar();
        assert ab != null;
        ab.setDisplayHomeAsUpEnabled(true);
        myToolbar.setNavigationOnClickListener(v -> finish());
    }


    /**
     * Fetches current user's info and populates the design elements
     */
    private void getUserInfo(){
        mAgentsDataBase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    return;
                }
                mDriver.parseData(dataSnapshot);
                mNameField.setText(mDriver.getName());
                mCarField.setText(mDriver.getCar());

                if (!mDriver.getProfileImage().equals("default"))
                    Glide.with(getApplication()).load(mDriver.getProfileImage()).apply(RequestOptions.circleCropTransform()).into(mProfileImage);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }



    /**
     * Saves current user 's info to the database.
     * If the resultUri is not null that means the profile image has been changed
     * and we need to upload it to the storage system and update the database with the new url
     */
    private void saveUserInformation() {
        String name = mNameField.getText().toString();
        String car = mCarField.getText().toString();
        String service = "type_1";


        Map userInfo = new HashMap();
        userInfo.put("name", name);
        userInfo.put("car", car);
        mAgentsDataBase.updateChildren(userInfo);

        if(resultUri != null) {

            final StorageReference filePath = FirebaseStorage.getInstance().getReference().child("profile_images").child(userID);

            UploadTask uploadTask = filePath.putFile(resultUri);
            uploadTask.addOnFailureListener(e -> {
                finish();
                return;
            });
            uploadTask.addOnSuccessListener(taskSnapshot -> filePath.getDownloadUrl().addOnSuccessListener(uri -> {
                Map newImage = new HashMap();
                newImage.put("profileImageUrl", uri.toString());
                mAgentsDataBase.updateChildren(newImage);

                finish();
                return;
            }).addOnFailureListener(exception -> {
                finish();
                return;
            }));
        }else{
            finish();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1 && resultCode == Activity.RESULT_OK){
            resultUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), resultUri);
                Glide.with(getApplication())
                        .load(bitmap) // Uri of the picture
                        .apply(RequestOptions.circleCropTransform())
                        .into(mProfileImage);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
