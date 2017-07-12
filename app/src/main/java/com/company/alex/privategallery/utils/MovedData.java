package com.company.alex.privategallery.utils;

import android.net.Uri;

/**
 * Created by Alex on 08/06/2017.
 */

public class MovedData {
    private Uri mUri;
    private String mPath;
    private Long mID;

    public MovedData(Uri mUri, String mPath) {
        this.mUri = mUri;
        this.mPath = mPath;
        this.mID = Long.valueOf(mUri.getLastPathSegment());
    }


    public Uri getmUri() {
        return mUri;
    }

    public String getmPath() {
        return mPath;
    }

    public Long getmID() {
        return mID;
    }
}
