package com.wozzytheprogrammer.kwproperty.Objects;

import com.google.firebase.database.DataSnapshot;

public class AgentObject {


    private String  id = "",
                    name = "",
                    car = "--",
                    profileImage = "default",
                    service;

    private float ratingsAvg = 0;

    private LocationObject mLocation;

    private Boolean active = true;

    public AgentObject(String id) {
        this.id = id;
    }
    public AgentObject() {}


    /**
     * Parse datasnapshot into this object
     */
    public void parseData(DataSnapshot dataSnapshot){

        id = dataSnapshot.getKey();

        if(dataSnapshot.child("name").getValue()!=null){
            name = dataSnapshot.child("name").getValue().toString();
        }
        if(dataSnapshot.child("car").getValue()!=null){
            car = dataSnapshot.child("car").getValue().toString();
        }
        if(dataSnapshot.child("profileImageUrl").getValue()!=null){
            profileImage = dataSnapshot.child("profileImageUrl").getValue().toString();
        }
        if (dataSnapshot.child("activated").getValue() != null) {
            active = Boolean.parseBoolean(dataSnapshot.child("activated").getValue().toString());
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

    public String getCar() {
        return car;
    }

    public void setCar(String car) {
        this.car = car;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }


    public LocationObject getLocation() {
        return mLocation;
    }

    public void setLocation(LocationObject mLocation) {
        this.mLocation = mLocation;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Boolean getActive() {
        return active;
    }
}
