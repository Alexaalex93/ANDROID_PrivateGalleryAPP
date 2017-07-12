package com.company.alex.privategallery.utils;

import android.app.Application;

import com.company.alex.privategallery.R;
import com.github.orangegangsters.lollipin.lib.managers.LockManager;

/**
 * Created by Alex on 05/06/2017.
 */

public class CustomApplication extends Application {

    @SuppressWarnings("unchecked")
    @Override
    public void onCreate() {
        super.onCreate();

        LockManager<CustomPinActivity> lockManager = LockManager.getInstance();
        lockManager.enableAppLock(this, CustomPinActivity.class);
        lockManager.getAppLock().setTimeout(5000);
        lockManager.getAppLock().setLogoId(R.drawable.security_lock);
    }
}
