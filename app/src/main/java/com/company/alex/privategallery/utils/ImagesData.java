package com.company.alex.privategallery.utils;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;


/**
 * Created by Alex on 28/04/2017.
 */

public class ImagesData implements Parcelable {

    public long _id;
    public Uri uri;
    public String imagePath;
    public boolean isPortraitImage;


    public static final Parcelable.Creator<ImagesData> CREATOR = new Parcelable.Creator() {
        public ImagesData createFromParcel(Parcel source) {
            return new ImagesData(source);
        }

        public ImagesData[] newArray(int size) {
            return new ImagesData[size];
        }
    };

    public ImagesData() {
        _id = 0;
        uri = null;
        imagePath = "";
        isPortraitImage = false;
    }
    public ImagesData(long _id, Uri uri, String imagePath, boolean isPortraitImage) {
        this._id = _id;
        this.uri = uri;
        this.imagePath = imagePath;
        this.isPortraitImage = isPortraitImage;
    }

    public void setImagePath(String path) {
        this.imagePath = path;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    public void setID (long id) {
        this._id = id;
    }

    public void setPortraitImage(boolean bool) {
        this.isPortraitImage = bool;
    }
    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this._id);
        dest.writeParcelable(this.uri, flags);
        dest.writeString(this.imagePath);
        dest.writeByte((byte)(this.isPortraitImage?1:0));
    }

    protected ImagesData(Parcel in) {
        this._id = in.readLong();
        this.uri = (Uri)in.readParcelable(Uri.class.getClassLoader());
        this.imagePath = in.readString();
        this.isPortraitImage = in.readByte() != 0;
    }
}
