package com.wozzytheprogrammer.kwproperty.Customer.FavProps;

public class Model {  public String mId, mAddress, mImgUrl, mInformation;

    public Model() {

    }

    public Model(String mId, String mAddress, String mImgUrl, String mInformation) {
        this.mId = mId;
        this.mAddress = mAddress;
        this.mImgUrl = mImgUrl;
        this.mInformation = mInformation;
    }

    public String getmId() {
        return mId;
    }

    public void setmId(String mId) {
        this.mId = mId;
    }

    public String getmAddress() {
        return mAddress;
    }

    public void setmAddress(String mAddress) {
        this.mAddress = mAddress;
    }

    public String getmImgUrl() {
        return mImgUrl;
    }

    public void setmImgUrl(String mImgUrl) {
        this.mImgUrl = mImgUrl;
    }

    public String getmInformation() {
        return mInformation;
    }

    public void setmInformation(String mId) {
        this.mInformation = mInformation;
    }
}