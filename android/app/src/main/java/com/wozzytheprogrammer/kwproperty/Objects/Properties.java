
package com.wozzytheprogrammer.kwproperty.Objects;


import com.google.gson.annotations.SerializedName;

public class Properties {

    @SerializedName("imageUrl")
    private String mImageUrl;
    @SerializedName("info")
    private String mInfo;
    @SerializedName("subTitle")
    private String mAddress;
    @SerializedName("title")
    private String mTitle;

    public Properties(String mImageUrl, String mInfo, String mAddress, String mTitle) {
        this.mImageUrl = mImageUrl;
        this.mInfo = mInfo;
        this.mAddress = mAddress;
        this.mTitle = mTitle;
    }

    public String getImageUrl() {
        return mImageUrl;
    }

    public void setImageUrl(String imageUrl) {
        mImageUrl = imageUrl;
    }

    public String getInfo() {
        return mInfo;
    }

    public void setInfo(String info) {
        mInfo = info;
    }

    public String getSubTitle() {
        return mAddress;
    }

    public void setSubTitle(String subTitle) {
        mAddress = subTitle;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

}
