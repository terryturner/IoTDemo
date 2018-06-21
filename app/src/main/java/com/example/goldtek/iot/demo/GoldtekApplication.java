package com.example.goldtek.iot.demo;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

/**
 * Created by Terry on 2018/05/31.
 */
public class GoldtekApplication extends Application {
    private static Context mContext = null;

    public static Context getContext() {
        return mContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();



        System.exit(0);
    }
}
