package io.cloudwalk.cots.poc2205031110;

import android.util.Log;

public class Application extends android.app.Application {
    private static final String
            TAG = Application.class.getSimpleName();

    private static Application
            sInstance = null;

    public static Application getInstance() {
        Log.d(TAG, "getInstance");

        return sInstance;
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");

        super.onCreate();

        sInstance = this;
    }
}
