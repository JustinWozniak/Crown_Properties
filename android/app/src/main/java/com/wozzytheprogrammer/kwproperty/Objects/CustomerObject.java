package com.wozzytheprogrammer.kwproperty.Objects;

import com.google.firebase.database.DataSnapshot;


/**
 * Customer object, it contains all the relevant info of the customer user
 */
public class CustomerObject{


    private String  id = "",
                    name = "",
                    phone = "",
                    profileImage = "default";

    public CustomerObject(String id) {
        this.id = id;
    }

    public CustomerObject() {}


    /**
     * Parse datasnapshot into this object
     */
    public void parseData(DataSnapshot dataSnapshot){
        id = dataSnapshot.getKey();
        if(dataSnapshot.child("name").getValue()!=null){
            name = dataSnapshot.child("name").getValue().toString();
        }
        if(dataSnapshot.child("profileImageUrl").getValue()!=null){
            profileImage = dataSnapshot.child("profileImageUrl").getValue().toString();
        }
    }


    public String getId() {
        return id;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }
}
