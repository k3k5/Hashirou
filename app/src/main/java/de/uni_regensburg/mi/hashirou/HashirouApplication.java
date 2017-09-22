package de.uni_regensburg.mi.hashirou;

import android.app.Application;
import android.content.Context;

/**
 * Created by k3k5 on 28.07.17.
 */

public class HashirouApplication extends Application {

    private static HashirouApplication sInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
    }

    public static HashirouApplication getInstance() {
        return sInstance;
    }

    public static Context getAppContext() {
        return sInstance.getApplicationContext();
    }

}
